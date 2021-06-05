package net.encdec.eddsk.code;


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
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.FilenameUtils;

public class EncDec {
	private final static String KEYALG = "AES/CBC/PKCS5Padding";
	private String outDir = null;
	private int failfound = 0;
	private int successfound = 0;
	private int ctr2 = 1;
	private FileOutputStream fos = null;
	private ZipOutputStream zos = null;
	private File directoryToZip = null;
	private File zipFile = null;
	private Reset r = new Reset();
	private int bufferLength = 1024;

	MAIN main = new MAIN();

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

			Cipher c = Cipher.getInstance(KEYALG);
			c.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
			byte[] fn_ed_iv_enc = c.doFinal(fn_ed);
			out.write(fn_ed_iv_enc);
			out = new CipherOutputStream(out, c);

			double pert = filePertcentage(is, bufferLength);
			byte[] buffer = new byte[bufferLength];
			int count = 0;

			ctr2 = 1;
			while ((count = is.read(buffer)) >= 0) {
				out.write(buffer, 0, count);
				r.repaint(pert * ctr2, ofn, 0);
				ctr2++;
			}
			enc = true;
		} catch (Exception e) {
			MAIN.exception = e.getMessage();
			LogFiles.lg.logErrors(e, this.getClass());
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

	private boolean decrypt(String key, InputStream is, String outDir, String zipFilePath) {

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
				double pert = filePertcentage(is, bufferLength);

				ctr2 = 1;
				while ((count = is.read(buffer)) >= 0) {
					out.write(buffer, 0, count);

					// update file percentage conversion
					r.repaint(pert * ctr2, ofn, 0);
					ctr2++;
				}

			}
			dec = true;
		} catch (Exception e) {
			MAIN.exception = e.getMessage();
			LogFiles.lg.logErrors(e, this.getClass());
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

			ctr2 = 1;
			while ((length = is.read(bytes)) >= 0) {
				zos.write(bytes, 0, length);
				r.repaint(pert * ctr2, repaintFN, 1);
				ctr2++;
			}

			zos.closeEntry();
			if (is != null) {
				is.close();
			}
		} catch (Exception e) {
			MAIN.exception = e.getMessage();
			LogFiles.lg.logErrors(e, this.getClass());
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
	 * @param Console
	 *            output switch (true for print, false for no print)
	 */
	// process file for encryption or decryption
	public String[] process(File processFile, String pw, String newFn, String outDir, boolean eMode)
			throws IOException {

		String[] returns = null;

		try {
			returns = run(processFile, pw, newFn, outDir, eMode, false);
		} catch (Exception e) {
			MAIN.exception = e.getMessage();
			LogFiles.lg.logErrors(e, this.getClass());
		}

		return returns;
	}

	// process file for encryption or decryption
	private void runContinued(File processFile, String pw, String newFn, String outDir, boolean eMode, String base)
			throws IOException {

		String[] returns = new String[2];
		returns[0] = "0";
		returns[1] = "file";
		InputStream is = null;

		// check if directory
		boolean dir = false;
		if (processFile.isDirectory()) {
			dir = true;
		} else {
			dir = false;
		}

		if (MAIN.subCurrentFile == 0 && MAIN.subTotalFiles == 0) {
			if (dir) {
				MAIN.subTotalFiles = processFile.list().length;
			} else {
				MAIN.subTotalFiles = 1;
			}
		} else {
			if (dir) {
				MAIN.subTotalFiles += processFile.list().length;
			} else {
				MAIN.subTotalFiles += 1;
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

			// create output location for new file output
			if (outDir == null) {
				this.outDir = MAIN.encFldr.getAbsolutePath();
			}

			boolean success = false;
			// call encrypt method
			if (dir) {
				File[] files = processFile.listFiles();
				for (File f : files) {
					MAIN.subCurrentFile++;
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
				MAIN.subCurrentFile++;
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
					MAIN.subCurrentFile++;
					if (!f.isHidden()) {
						if (f.isDirectory()) {
							runContinued(f, pw, newFn, outDir, eMode, base);
						} else {
							success = presetFileDec(pw, f, outDir, false, base, 1);
							ifFileProcessedSuccessfully(success);
						}
					}
				}
			} else {
				MAIN.subCurrentFile++;
				if (!processFile.isHidden()) {
					success = presetFileDec(pw, processFile, outDir, false, base, 2);
					ifFileProcessedSuccessfully(success);
				}
			}

		}

	}

	// process file for encryption or decryption
	private String[] run(File processFile, String pw, String newFn, String outDir, boolean eMode,
			boolean encdecZipConfirmed) {

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
				MAIN.multiple = true;
				MAIN.totalFiles = processFile.list().length;
			} else {
				dir = false;
				MAIN.multiple = false;
				MAIN.totalFiles = 1;
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

				// create output location for new file output
				if (outDir == null) {
					this.outDir = MAIN.encFldr.getAbsolutePath();
				}

				boolean success = false;
				// call encrypt method
				if (dir) {
					File[] files = processFile.listFiles();
					String base = processFile.getName();
					for (File f : files) {
						MAIN.currentFile++;
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
					MAIN.currentFile++;
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
					MAIN.bar.setString("Opening files to decrypt, may take a few minutes. Please wait ...");
					MAIN.currentFile++;
					Path tempDirectory = Files.createTempDirectory("EncDecZipDir");
					ifEncDecZip(tempDirectory, processFile, pw);
				} else if (processFile.isDirectory()) {
					MAIN.multiple = true;
					MAIN.totalFiles = processFile.list().length;
					File[] files = processFile.listFiles();
					String base = processFile.getName();
					for (File f : files) {
						if (isZipFile(f)) {
							MAIN.bar.setString("Found zip file, verifying if EncDec package. Please wait ...");
							Path tempSubDirectory = Files.createTempDirectory("EncDecZipSubDir");
							ifEncDecZip(tempSubDirectory, f, pw);
						} else {
							MAIN.currentFile++;
							if (!f.isHidden()) {
								if (f.isDirectory()) {
									runContinued(f, pw, newFn, outDir, eMode, base);
								} else {
									success = presetFileDec(pw, f, outDir, false, base, 1);
									ifFileProcessedSuccessfully(success);
								}
							}
						}
					}

				} else {
					MAIN.currentFile = 1;
					MAIN.totalFiles = 1;
					if (!processFile.isHidden()) {
						success = presetFileDec(pw, processFile, outDir, false, "", 2);
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
		} catch (Exception e) {
			MAIN.exception = e.getMessage();
			LogFiles.lg.logErrors(e, this.getClass());
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
			MAIN.totalFiles += zipFileSub.size();
			int count = 0;
			byte[] buffer = new byte[bufferLength];
			Enumeration<? extends ZipEntry> entries = zipFileSub.entries();
			while (entries.hasMoreElements()) {
				MAIN.currentFile++;
				ZipEntry entry = entries.nextElement();
				InputStream stream = zipFileSub.getInputStream(entry);
				File zFile = new File(tempDirectory.toFile().getAbsolutePath() + MAIN.slash + entry.getName());
				if (!entry.isDirectory()) {
					File zFileParent = new File(zFile.getParent());
					if (!zFileParent.exists()) {
						zFileParent.mkdirs();
					}

					double pert = filePertcentage(stream, bufferLength);
					OutputStream out = new FileOutputStream(zFile);
					while ((count = stream.read(buffer)) >= 0) {
						out.write(buffer, 0, count);
						r.repaint(pert * ctr2, zFile.getName(), 2);
						ctr2++;
					}
					out.close();

					if (isZipFile(zFile)) {
						MAIN.bar.setString("Found zip file, verifying if EncDec package. Please wait ...");
						Path tempSubDirectory = Files.createTempDirectory("EncDecZipSubDir");
						ifEncDecZip(tempSubDirectory, zFile, pw);
					} else {
						success = presetFileDec(pw, zFile, outDir, true, base, 0);
						ifFileProcessedSuccessfully(success);
					}
				} else {
					zFile.mkdirs();
				}
			}
			zipFileSub.close();
			tempZip.delete();
		} catch (Exception e) {
			MAIN.exception = e.getMessage();
			LogFiles.lg.logErrors(e, this.getClass());
		}
	}

	private boolean isZipFile(File file) {
		boolean good = false;
		try {
			if (file.isDirectory()) {
				return false;
			}
			if (!file.canRead()) {
				// throw new IOException("Cannot read file "+file.getAbsolutePath());
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
			MAIN.exception = e.getMessage();
			LogFiles.lg.logErrors(e, this.getClass());
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
			MAIN.exception = e.getMessage();
			LogFiles.lg.logErrors(e, this.getClass());
			return false;
		}
	}

	private boolean presetFileDec(String pw, File f, String out, boolean encdecZipCheck, String base, int type)
			throws IOException {
		try {
			// create output location for new file output
			if (out == null) {
				out = MAIN.decFldr.getAbsolutePath();
			}

			// call decrypt method
			InputStream is = new FileInputStream(f);
			return decrypt(pw, is, out, getZipFileParent(f, base, type));

		} catch (Exception e) {
			MAIN.exception = e.getMessage();
			LogFiles.lg.logErrors(e, this.getClass());
			return false;
		}
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

}
