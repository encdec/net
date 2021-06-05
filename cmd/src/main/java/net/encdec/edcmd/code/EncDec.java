package net.encdec.edcmd.code;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.FilenameUtils;

public class EncDec extends Reset {
	// statics
	private final static String KEYALG = "AES/CBC/PKCS5Padding";
	private final static String alphanumeric = "^[a-zA-Z0-9!@#$%^&*_=+?/,.;:-]*$";
	private final static String pattern = "^[a-zA-Z0-9._\\s-]*$";
	private static LogFiles lg = new LogFiles();

	String outDir = null;
	int ctr = 1;
	FileOutputStream fos = null;
	ZipOutputStream zos = null;
	File directoryToZip = null;
	File zipFile = null;
	int bufferLength = 1024;
	boolean pnt = false;
	String exception = null;

	// file counters
	int currentFile = 0;
	int totalFiles = 0;
	int subCurrentFile = 0;
	int subTotalFiles = 0;
	int failfound = 0;
	int successfound = 0;

	// finished vals
	boolean multiple = false;

	// encrypt method
	private boolean encrypt(String key, InputStream is, String fn, String ofn, String zipFilePath) {

		boolean enc = false;
		int ivSize = 16;
		int keySize = 32;
		OutputStream out = null;
		File temp = null;

		try {
			String fileNameWithOutExt = FilenameUtils.removeExtension(fn) + "_";
			temp = File.createTempFile(fileNameWithOutExt, "");
			out = new FileOutputStream(temp);

			// Generate IV
			byte[] iv = new byte[ivSize];
			SecureRandom random = new SecureRandom();
			random.nextBytes(iv);
			IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

			// write original file name and ed tag to file's first two MBs
			out.write(iv);

			// Hashing key.
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			digest.update(key.getBytes());
			byte[] keyBytes = new byte[keySize];
			System.arraycopy(digest.digest(), 0, keyBytes, 0, keyBytes.length);
			SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");

			// add filename to full file bytes
			byte[] fn_ed = new byte[2048];
			System.arraycopy(ofn.getBytes(), 0, fn_ed, 0, ofn.length());

			// ed tag
			byte[] edTag = "encdectag!!encdectag!!".getBytes();
			System.arraycopy(edTag, 0, fn_ed, 1024, edTag.length);

			int count = 0;
			byte[] buffer = new byte[bufferLength];

			ctr = 1;
			long filesize = is.available();
			long fSize = filesize / bufferLength;

			double pert = 0.0;
			double cycles = filesize / buffer.length;
			pert = 100 / cycles;
			boolean last = false;

			Cipher c = Cipher.getInstance(KEYALG);
			c.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
			byte[] fn_ed_iv_enc = c.doFinal(fn_ed);
			out.write(fn_ed_iv_enc);
			out = new CipherOutputStream(out, c);

			while ((count = is.read(buffer)) >= 0) {
				out.write(buffer, 0, count);

				if ((ctr % 100 == 0 || ctr == fSize) && !last) {
					double calc = (pert * ctr);
					if (calc >= 98.5) {
						calc = 100.0;
						last = true;
					}
					repaint(ofn, pnt, calc, 0, last);
				}
				ctr++;
			}
			enc = true;
		} catch (Exception e) {
			exception = e.getMessage();
			lg.logErrors(e, this.getClass());
			enc = false;
		} finally {
			try {
				if (is != null) {
					is.close();
				}
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				// do nothing
			}

			if (enc && temp != null && temp.exists()) {
				addToZip(temp, temp.getName(), 0, zipFilePath);
				temp.delete();
			}
		}

		return enc;
	}

	private boolean decrypt(String key, InputStream is, String zipFilePath) {

		boolean dec = false;
		int ivSize = 16;
		int keySize = 32;
		OutputStream out = null;
		File temp = null;
		String ofn = null;

		try {
			// Extract IV
			byte[] iv = new byte[ivSize];
			is.read(iv, 0, ivSize);
			IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

			// Hash key
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			digest.update(key.getBytes());
			byte[] keyBytes = new byte[keySize];
			System.arraycopy(digest.digest(), 0, keyBytes, 0, keyBytes.length);
			SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");

			Cipher c = Cipher.getInstance(KEYALG);
			c.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
			int fn_ed_iv_size = /* filename */ 1024 + /* ed tag */ 1024 + /* IV Padding block */ 16;
			byte[] fn_ed_iv_enc = new byte[fn_ed_iv_size];
			is.read(fn_ed_iv_enc, 0, fn_ed_iv_size);
			byte[] fn_ed_iv_dec = c.doFinal(fn_ed_iv_enc);

			// extract file name from full file bytes
			byte[] fnSlot = new byte[1024];
			System.arraycopy(fn_ed_iv_dec, 0, fnSlot, 0, 1024);
			ofn = new String(fnSlot);
			ofn = ofn.trim();

			String fileNameWithOutExt = FilenameUtils.removeExtension(ofn) + "__";
			String fileNameWithExt = "." + FilenameUtils.getExtension(ofn);
			temp = File.createTempFile(fileNameWithOutExt, fileNameWithExt);
			out = new FileOutputStream(temp);

			// extract ed tag
			byte[] edTagSlot = new byte[1024];
			System.arraycopy(fn_ed_iv_dec, 1024, edTagSlot, 0, 1024);
			String edTag = new String(edTagSlot);
			edTag = edTag.trim();

			if (edTag.trim().equals("encdectag!!encdectag!!")) {
				c = Cipher.getInstance(KEYALG);
				c.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
				out = new CipherOutputStream(out, c);

				int count = 0;
				byte[] buffer = new byte[bufferLength];

				ctr = 1;
				long filesize = is.available();
				long fSize = filesize / bufferLength;

				double pert = 0.0;
				double cycles = filesize / buffer.length;
				pert = 100 / cycles;
				boolean last = false;

				while ((count = is.read(buffer)) >= 0) {
					out.write(buffer, 0, count);

					// update file percentage conversion
					if ((ctr % 100 == 0 || ctr == fSize) && !last) {
						double calc = (pert * ctr);
						if (calc >= 98.5) {
							calc = 100.0;
							last = true;
						}
						repaint(ofn, pnt, calc, 0, last);
					}
					ctr++;
				}

			}
			dec = true;
		} catch (Exception e) {
			exception = e.getMessage();
			lg.logErrors(e, this.getClass());
			dec = false;
		} finally {
			try {
				if (is != null) {
					is.close();
				}
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				// do nothing
			}

			if (dec && temp != null && temp.exists()) {
				addToZip(temp, ofn, 1, zipFilePath);
				temp.delete();
			}
		}
		return dec;
	}

	private double filePertcentage(InputStream is, int bufferLength) throws IOException {
		// create numerics for file processing
		double pert = 0.0;
		int filesize = is.available();
		double cycles = filesize / bufferLength;
		pert = 100 / cycles;
		return pert;
	}

	private void addToZip(File file, String fn, int mode, String zipFilePath) {
		try {
			String zfn = zipFilePath + MAIN.slash + fn;
			if (zfn.startsWith(MAIN.slash)) {
				zfn = zfn.substring(1);
			}
			InputStream is = new FileInputStream(file);
			ZipEntry zipEntry = new ZipEntry(zfn);
			zipEntry.setSize(is.available());
			zos.putNextEntry(zipEntry);

			String repaintFN = "";
			switch (mode) {
			case 0: // encrypt
				repaintFN = file.getName();
				break;
			case 1: // decrypt
				repaintFN = fn;
				break;
			default:
				repaintFN = file.getName();
				break;
			}

			double pert = filePertcentage(is, bufferLength);
			byte[] bytes = new byte[bufferLength];
			int length;

			long filesize = is.available();
			long fSize = filesize / bufferLength;
			boolean last = false;

			ctr = 1;
			while ((length = is.read(bytes)) >= 0) {
				zos.write(bytes, 0, length);
				if ((ctr % 100 == 0 || ctr == fSize) && !last) {
					double calc = (pert * ctr);
					if (calc >= 98.5) {
						calc = 100.0;
						last = true;
					}
					repaint(repaintFN, pnt, calc, 1, last);
				}
				ctr++;
			}
			// space for new file
			System.out.write("\r\n".getBytes());

			zos.closeEntry();
			if (is != null) {
				is.close();
			}
		} catch (Exception e) {
			exception = e.getMessage();
			lg.logErrors(e, this.getClass());
		}
	}

	/**
	 * Encryption / Decryption method for EncDec Program
	 * 
	 * @param processFile
	 *            The file or folder to process
	 * @param pw
	 *            User created secret passcode
	 * @param newFn
	 *            An optional new file name (encrypt mode only)
	 * @param outDir
	 *            The output folder where the new processed file will be saved
	 * @param eMode
	 *            The encryption mode (true for encryption, false for decryption)
	 * @param pnt
	 *            Console output switch (true for print, false for no print)
	 */
	// process file for encryption or decryption
	public void process(File processFile, String pw, String newFn, String outDir, boolean eMode, boolean pnt)
			throws IOException {

		config();
		// create output location for new file output
		if (outDir == null) {
			if (eMode) {
				outDir = MAIN.encFldr.getAbsolutePath();
			} else {
				outDir = MAIN.decFldr.getAbsolutePath();
			}
		}
		String outDirWithDate = outDir + MAIN.slash + getDateString(null);

		this.pnt = pnt;
		try {
			// precheck user args
			boolean good = precheckArgs(processFile, pw, newFn, outDirWithDate, eMode);

			// Call Encryption/Decryption program
			if (good) {
				if (pnt) {
					System.out.println("Processing file(s). Please wait ...");
				}
				run(processFile, pw, newFn, outDirWithDate, eMode, false);
			}

		} catch (Exception e) {
			exception = e.getMessage();
			lg.logErrors(e, EncDec.class);
		}

	}

	// process file for encryption or decryption
	private void runContinued(File processFile, String pw, String newFn, String outDir, boolean eMode, String base)
			throws IOException {

		InputStream is = null;

		// check if directory
		boolean dir = false;
		if (processFile.isDirectory()) {
			dir = true;
		} else {
			dir = false;
		}

		if (currentFile == 0 && subTotalFiles == 0) {
			if (dir) {
				subTotalFiles = processFile.list().length;
			} else {
				subTotalFiles = 1;
			}
		} else {
			if (dir) {
				subTotalFiles += processFile.list().length;
			} else {
				subTotalFiles += 1;
			}
		}

		// encrypt
		if (eMode == true) {
			// check if user entered a file name for encrypt mode
			if (newFn != null) {
				if (newFn.trim().toLowerCase().equals("")) {
					newFn = "file";
				}
			} else {
				newFn = "file";
			}

			boolean success = false;
			// call encrypt method
			if (dir) {
				File[] files = processFile.listFiles();
				for (File f : files) {
					currentFile++;
					if (!f.isHidden()) {
						if (f.isDirectory()) {
							runContinued(f, pw, newFn, outDir, eMode, base);
						} else {
							is = new FileInputStream(f);
							success = presetFileEnc(pw, is, f, outDir, newFn, dir, base, 1);
							ifFileProcessedSuccessfully(success);
						}
					}
				}
			} else {
				// convert uploaded file to bytes
				currentFile++;
				if (!processFile.isHidden()) {
					is = new FileInputStream(processFile);
					success = presetFileEnc(pw, is, processFile, outDir, newFn, dir, base, 2);
					ifFileProcessedSuccessfully(success);
				}
			}

		}

		// decrypt
		else if (eMode == false) {
			boolean success = false;
			if (dir) {
				File[] files = processFile.listFiles();
				for (File f : files) {
					currentFile++;
					if (!f.isHidden()) {
						if (f.isDirectory()) {
							runContinued(f, pw, newFn, outDir, eMode, base);
						} else {
							success = presetFileDec(pw, f, false, base, 1);
							ifFileProcessedSuccessfully(success);
						}
					}
				}
			} else {
				currentFile++;
				if (!processFile.isHidden()) {
					success = presetFileDec(pw, processFile, false, base, 2);
					ifFileProcessedSuccessfully(success);
				}
			}

		}

	}

	// process file for encryption or decryption
	private String[] run(File processFile, String pw, String newFn, String outDir, boolean eMode,
			boolean encdecZipConfirmed) throws IOException {

		String[] returns = new String[2];
		try {
			returns[0] = "0";
			returns[1] = "file";
			InputStream is = null;
			boolean encDec = false;
			boolean partial = false;

			// check if directory
			boolean dir = false;
			if (processFile.isDirectory()) {
				dir = true;
				multiple = true;
				totalFiles = processFile.list().length;
			} else {
				dir = false;
				multiple = false;
				totalFiles = 1;
			}

			directoryToZip = new File(outDir);
			if (!directoryToZip.exists()) {
				directoryToZip.mkdirs();
			}

			// encrypt
			if (eMode == true) {
				// check if user entered a file name for encrypt mode
				if (newFn != null) {
					if (newFn.trim().toLowerCase().equals("")) {
						newFn = "file";
					}
				} else {
					newFn = "file";
				}
				zipFile = new File(directoryToZip.getAbsolutePath() + MAIN.slash + newFn + ".zip");
				fos = new FileOutputStream(zipFile);
				zos = new ZipOutputStream(fos);

				boolean success = false;
				// call encrypt method
				if (dir) {
					File[] files = processFile.listFiles();
					String base = processFile.getName();
					for (File f : files) {
						currentFile++;
						if (!f.isHidden()) {
							if (f.isDirectory()) {
								runContinued(f, pw, newFn, outDir, eMode, base);
							} else {
								is = new FileInputStream(f);
								success = presetFileEnc(pw, is, f, outDir, newFn, dir, base, 1);
								ifFileProcessedSuccessfully(success);
							}
						}
					}
				} else {
					currentFile++;
					// convert uploaded file to bytes
					if (!processFile.isHidden()) {
						is = new FileInputStream(processFile);
						success = presetFileEnc(pw, is, processFile, outDir, newFn, dir, "", 2);
						ifFileProcessedSuccessfully(success);
					}
				}

				if (failfound == 0) {
					encDec = true;
				} else if (successfound == 0) {
					encDec = false;
				} else if (successfound > 0 && successfound > 0) {
					encDec = true;
					partial = true;
				}

				// create return values
				if (!dir && encDec) {
					returns[0] = "1";
					returns[1] = zipFile.getAbsolutePath();
				} else if (dir && encDec && !partial) {
					returns[0] = "2";
					returns[1] = zipFile.getAbsolutePath();
				} else if (dir && encDec && partial) {
					returns[0] = "3";
					returns[1] = zipFile.getAbsolutePath();
				} else if (dir && !encDec) {
					returns[0] = "4";
					returns[1] = zipFile.getAbsolutePath();
				}

			}

			// decrypt
			else if (eMode == false) {
				boolean encdecZip = false;
				String fileNameWithOutExtDecrypted = FilenameUtils.removeExtension(processFile.getName())
						+ "_decrypted";
				zipFile = new File(directoryToZip + MAIN.slash + fileNameWithOutExtDecrypted + ".zip");
				fos = new FileOutputStream(zipFile);
				zos = new ZipOutputStream(fos);

				boolean success = false;
				if (isZipFile(processFile) && !encdecZipConfirmed) {
					currentFile++;
					Path tempDirectory = Files.createTempDirectory("EncDecZipDir");
					ifEncDecZip(tempDirectory, processFile, pw);
				} else if (processFile.isDirectory()) {
					multiple = true;
					totalFiles = processFile.list().length;
					File[] files = processFile.listFiles();
					String base = processFile.getName();
					for (File f : files) {
						if (isZipFile(f)) {
							Path tempSubDirectory = Files.createTempDirectory("EncDecZipSubDir");
							ifEncDecZip(tempSubDirectory, f, pw);
						} else {
							currentFile++;
							if (!f.isHidden()) {
								if (f.isDirectory()) {
									runContinued(f, pw, newFn, outDir, eMode, base);
								} else {
									success = presetFileDec(pw, f, false, base, 1);
									ifFileProcessedSuccessfully(success);
								}
							}
						}
					}

				} else {
					currentFile = 1;
					totalFiles = 1;
					if (!processFile.isHidden()) {
						success = presetFileDec(pw, processFile, false, "", 2);
						ifFileProcessedSuccessfully(success);
					}
				}

				if (encdecZip) {
					processFile.delete();
				}

				if (failfound == 0) {
					encDec = true;
				} else if (successfound == 0) {
					encDec = false;
				} else if (successfound > 0 && failfound > 0) {
					encDec = true;
					partial = true;
				}

				// create return values
				if (!dir && encDec) {
					returns[0] = "1";
					returns[1] = zipFile.getAbsolutePath();
				} else if (dir && encDec && !partial) {
					returns[0] = "2";
					returns[1] = zipFile.getAbsolutePath();
				} else if (dir && encDec && partial) {
					returns[0] = "3";
					returns[1] = zipFile.getAbsolutePath();
				} else if (dir && !encDec) {
					returns[0] = "4";
					returns[1] = zipFile.getAbsolutePath();
				}
			}

			if (zos != null) {
				zos.close();
			}
			if (fos != null) {
				fos.close();
			}

			zipFile.setExecutable(true);
			zipFile.setReadable(true);
			zipFile.setWritable(false);

			failfound = 0;
			successfound = 0;

			output(returns, eMode, this.outDir, processFile);
		} catch (Exception e) {
			exception = e.getMessage();
			lg.logErrors(e, this.getClass());
		}
		return returns;

	}

	private void ifEncDecZip(Path tempDirectory, File processFile, String pw) {
		boolean success = false;
		try {
			File tempZip = new File(tempDirectory.toFile().getAbsolutePath() + MAIN.slash + processFile.getName());
			Files.copy(processFile.toPath(), tempZip.toPath(), StandardCopyOption.REPLACE_EXISTING);
			String base = tempZip.getParent();

			ZipFile zipFileSub = new ZipFile(tempZip.getAbsolutePath());
			totalFiles += zipFileSub.size();
			int count = 0;
			byte[] buffer = new byte[bufferLength];
			Enumeration<? extends ZipEntry> entries = zipFileSub.entries();
			while (entries.hasMoreElements()) {
				currentFile++;
				ZipEntry entry = entries.nextElement();
				InputStream stream = zipFileSub.getInputStream(entry);
				File zFile = new File(tempDirectory.toFile().getAbsolutePath() + MAIN.slash + entry.getName());
				if (!entry.isDirectory()) {
					File zFileParent = new File(zFile.getParent());
					if (!zFileParent.exists()) {
						zFileParent.mkdirs();
					}

					ctr = 1;
					double pert = filePertcentage(stream, bufferLength);
					long filesize = stream.available();
					long fSize = filesize / bufferLength;
					boolean last = false;

					OutputStream out = new FileOutputStream(zFile);
					while ((count = stream.read(buffer)) >= 0) {
						out.write(buffer, 0, count);
						if ((ctr % 100 == 0 || ctr == fSize) && !last) {
							double calc = (pert * ctr);
							if (calc >= 98.5) {
								calc = 100.0;
								last = true;
							}
							repaint(zFile.getName(), pnt, calc, 2, last);
						}
						ctr++;
					}
					out.close();

					if (isZipFile(zFile)) {
						Path tempSubDirectory = Files.createTempDirectory("EncDecZipSubDir");
						ifEncDecZip(tempSubDirectory, zFile, pw);
					} else {
						success = presetFileDec(pw, zFile, true, base, 0);
						ifFileProcessedSuccessfully(success);
					}
				} else {
					zFile.mkdirs();
				}
			}
			zipFileSub.close();
			tempZip.delete();
		} catch (Exception e) {
			exception = e.getMessage();
			lg.logErrors(e, this.getClass());
		}
	}

	private boolean isZipFile(File file) {
		boolean good = false;
		try {
			if (file.isDirectory()) {
				return false;
			}
			if (!file.canRead()) {
				return false;
			}
			if (file.length() < 4) {
				return false;
			}
			DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
			int test = in.readInt();
			in.close();
			good = (test == 0x504b0304);
		} catch (Exception e) {
			exception = e.getMessage();
			lg.logErrors(e, this.getClass());
		}
		return good;
	}

	private void ifFileProcessedSuccessfully(boolean success) {
		if (!success) {
			failfound++;
		} else if (success) {
			successfound++;
		}
	}

	private boolean presetFileEnc(String pw, InputStream is, File f, String out, String newFn, boolean dir, String base,
			int type) throws IOException {
		try {
			// get file name
			String ofn = f.getName();

			return encrypt(pw, is, newFn, ofn, getZipFileParent(f, base, type));
		} catch (Exception e) {
			exception = e.getMessage();
			lg.logErrors(e, this.getClass());
			return false;
		}
	}

	// new
	private boolean presetFileDec(String pw, File f, boolean encdecZipCheck, String base, int type) throws IOException {

		try {
			// call decrypt method
			InputStream is = new FileInputStream(f);
			return decrypt(pw, is, getZipFileParent(f, base, type));

		} catch (Exception e) {
			exception = e.getMessage();
			lg.logErrors(e, this.getClass());
			return false;
		}
	}

	private boolean precheckArgs(File processFile, String passcode, String filename, String outDir, boolean eMode)
			throws IOException {

		boolean good = false;
		boolean fl = false;
		boolean ofl = true;
		boolean pc = false;
		boolean fn = true;

		try {
			// check if file location exists
			if (processFile != null) {
				String wd = System.getProperty("user.dir");
				String[] locs = { wd, processFile.getAbsolutePath() };
				for (int i = 0; i < 2; i++) {
					if (i == 0) {
						processFile = new File(locs[i], processFile.getAbsolutePath());
					} else if (i == 1) {
						processFile = new File(locs[i]);
					}
					if (processFile.exists()) {
						fl = true;
						break;
					}
				}
			} else {
				if (pnt) {
					System.out.println("The input file to process is null.");
				}
				System.exit(0);
			}
			if (!fl) {
				if (pnt) {
					System.out.println("The selected file does not exist at the current location below:");
					System.out.println(processFile.getAbsolutePath());
				}
				System.exit(0);
			}

			// check if passcode meets length and char conditions
			if (passcode != null) {
				if (passcode.matches(alphanumeric) && passcode.length() >= 8) {
					pc = true;
				}
			}
			if (!pc) {
				if (pnt) {
					System.out.println("Passcode must be at least 8 alphanumeric chars. "
							+ "Some special chars are accepted for passcode (!@#$%^&*_=+?/,.;:-).");
				}
				System.exit(0);
			}

			// check if filename meets length and char conditions (encrypt mode only)
			if (eMode) {
				if (filename != null) {
					if (!filename.matches(pattern) || filename.length() > 50) {
						fn = false;
					}
				} else {
					filename = "file";
				}
			}
			if (!eMode) {
				fn = true;
			}

			if (!fn) {
				if (pnt) {
					System.out.println("Filename cannot be larger than 50 alphanumeric chars. "
							+ "Some special chars are accepted for passcode (._-)");
				}
				System.exit(0);
			}

			// check if output folder exists
			File processedOutFile = null;
			if (outDir != null) {
				processedOutFile = new File(outDir);

				if (!processedOutFile.exists()) {
					processedOutFile.mkdirs();
				}

				if (!processedOutFile.isDirectory()) {
					ofl = false;

					if (pnt) {
						System.out.println(
								"The set output location below does not exist, is not a directory, or could not be created:");
						System.out.println(outDir);
					}
					System.exit(0);
				} else {
					this.outDir = outDir;
				}
			}

			// if all required args result in true
			if (fl && pc && fn && ofl) {
				good = true;
			}

			// program received invalid arguments
			else {
				if (pnt) {
					System.out.println("Invalid arguments. Please check you inputs and try again,");
					System.out.println("Option --help for help menu");
					System.out.println(
							"Example: -file path/to/file/or/folder -name filename (Encrypt Mode only) -mode enc -passcode abcd1234 -out path/to/folder -noprint");
					System.out.println("Passcode must be at least 8 alphanumeric chars.");
					System.out.println("Some special chars are accepted for passcode (!@#$%^&*_=+?/,.;:-).");
				}
				System.exit(0);
			}
		} catch (Exception e) {
			exception = e.getMessage();
			lg.logErrors(e, EncDec.class);
		}

		return good;

	}

	private String getZipFileParent(File f, String base, int type) {
		String zipFilePath = null;
		try {
			String fParentFolder = "";
			if (base.contains(MAIN.slash)) {
				fParentFolder = base.substring(base.lastIndexOf(MAIN.slash));
			} else {
				fParentFolder = base;
			}
			int start = f.getAbsolutePath().lastIndexOf(fParentFolder);
			int startAppend = fParentFolder.length();
			zipFilePath = f.getParent().substring(start + startAppend);
		} catch (Exception e) {
			zipFilePath = "";
		}
		return zipFilePath;
	}

	private void output(String[] results, boolean eMode, String outDir, File processFile) throws IOException {

		try {
			boolean result = false;
			boolean partial = false;
			if (results != null) {
				if (results[0].equals("4")) {
					result = false;
				} else if (results[0].equals("3")) {
					result = true;
					partial = true;
				} else if (results[0].equals("2")) {
					result = true;
					partial = false;
				} else if (results[0].equals("1")) {
					result = true;
				} else if (results[0].equals("0")) {
					result = false;
				}
			}

			File fileFinished = new File(results[1]);

			// File file, String pw, String newFn, String outDir, boolean dir, boolean eMode
			// if file process successfully
			if (result == true) {
				// encrypt mode
				if (eMode == true) {
					if (!partial) {
						if (pnt) {
							System.out.println("Output Status: Success! Your file was successfully encrypted.");
							System.out.println(
									"The new file is available at location: " + fileFinished.getAbsolutePath());

							String out = "The file, " + processFile.getName()
									+ ", was successfully encrypted. New file available at drive location: "
									+ fileFinished.getAbsolutePath();
							lg.logInfo(out, EncDec.class);
						}
						System.exit(0);
					} else if (partial) {
						if (pnt) {
							System.out.println(
									"Output Status: Partial Success? Only some of your files were successfully encrypted.");
							System.out.println(
									"Only some of your files were successfully encrypted. The new file is available at location: "
											+ fileFinished.getAbsolutePath());

							String out = "The file, " + processFile.getName()
									+ ", was successfully encrypted. New file available at drive location: "
									+ fileFinished.getAbsolutePath();
							lg.logInfo(out, EncDec.class);
						}

						System.exit(0);
					}
				}
				// decrypt mode
				else if (eMode == false) {
					if (!partial) {
						if (pnt) {
							System.out.println("Output Status: Created! Your file was successfully decrypted.");
							System.out.println(
									"The new file is available at location: " + fileFinished.getAbsolutePath());

							String out = "The file, " + processFile.getName()
									+ ", was successfully decrypted. The new file is available " + "at drive location: "
									+ fileFinished.getAbsolutePath();
							lg.logInfo(out, EncDec.class);
						}
					} else if (partial) {
						if (pnt) {
							System.out.println(
									"Output Status: Partial Creation? Only some of your files were successfully decrypted.");
							System.out.println(
									"The new file is available at location: " + fileFinished.getAbsolutePath());

							String out = "The file, " + processFile.getName()
									+ ", were successfully decrypted. The new file is available "
									+ "at drive location: " + fileFinished.getAbsolutePath();
							lg.logInfo(out, EncDec.class);
						}
					}
				}
			}
			// if file failed to process
			else if (result == false) {
				// new output status
				if (pnt) {
					System.out.println("Output Status: Failure! The file was not processed. Please check the passcode "
							+ "and if the correct file was chosen for the right mode.");
					System.out.println("Error: " + exception);

					String out = "The file, " + processFile.getName()
							+ ", failed to be processed. Please check the passcode "
							+ "and if the correct file was chosen for the right mode.";
					lg.logInfo(out, EncDec.class);
					System.exit(0);
				}
			}

		} catch (Exception e) {
			exception = e.getMessage();
			lg.logErrors(e, EncDec.class);
		}

	}

	private static String getDateString(String dateStr) {
		try {
			if (dateStr == null) {
				SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH.mm.ss");
				Date date = new Date();
				dateStr = sdf.format(date);
			}
		} catch (Exception e) {
			// nothing
		}
		return dateStr;
	}

	public void config() {
		// create log files
		try {
			Reset r = new Reset();
			// determine os
			r.findOS();

			lg.create();
			lg.logFiles();

			// create folders
			r.createFolders();
		} catch (IOException e) {
			exception = e.getMessage();
			lg.logErrors(e, EncDec.class);
		}
	}
}