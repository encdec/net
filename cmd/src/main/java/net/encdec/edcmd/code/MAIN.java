package net.encdec.edcmd.code;

import java.io.File;
import java.io.IOException;

public class MAIN {

	// Operating System
	protected static int OSset = -1;
	protected static String slash = null;

	// EncDec Folders
	protected static File encFldr = null;
	protected static File decFldr = null;

	// command line
	private void help(String[] args) throws InterruptedException {

		// help command arg
		if (args[0].trim().equals("--help")) {
			System.out.println(
					"!Thank you for using EncDec!, helping you to programmatically protect sensitive data with ease.\r\n");
			System.out.println(
					"The EncDec program performs 2 primary tasks against your files: Encryption and Decryption. "
							+ "To perform these tasks, EncDec requires 3 user input options for both methods. These input options are a file or folder, "
							+ "passode, and mode. 3 non-requried options include 'new file name' (encryption only), 'new processed file location', and "
							+ "'no print output'. The EncDec program .jar file can also be used as a dependency within "
							+ "another java development program. As a dependency program, the EncDec.java file can run the 'process' method independently given "
							+ "the correct fields. To run via command line, you can provide the input options requested onto "
							+ "the display panel. Below are a list of command line input options and corresponding arguments to "
							+ "successfully run the program within and Windows cmd or Linux shell.\r\n");
			System.out.println(
					"Syntax\r\n" + "$DRIVE:java -jar /path/to/jar/file/encdec-1.0.1.jar -[option] [argument]\r\n");
			System.out.println("Options\r\n" + "--help \r\n"
					+ "	Provides a welcome message, basic instructions, and a list of options with argument examples\r\n"
					+ "	No argument needed\r\n" + "\r\n" + "-file \r\n"
					+ "	File path argument for the program to perform an encryption/decrption task\r\n"
					+ "	Required, use double quotes if path contains spaces\r\n"
					+ "	Ex. -file \"/full/path/to/file/or/folder\"\r\n" + "\r\n" + "-passcode \r\n"
					+ "	Passcode used for the encryption/decrption algorithm\r\n"
					+ "	Required, at least 8 alphanumeric chars, special chars limited (!@#$%^&*_=+?/,.;:-)\r\n"
					+ "	Ex. -passcode abcd1234\r\n" + "\r\n" + "-name \r\n"
					+ "	New file name of a processed file in Encrypt Mode\r\n"
					+ "	Not required. For Encrypt Mode only, default name='file' if not set, \r\n"
					+ "	use double quotes if name contains spaces\r\n" + "	Ex. -name \"My New File\"\r\n" + "\r\n"
					+ "-mode \r\n" + "	Mode selection to perform an encryption/decrption task\r\n"
					+ "	Required, only 2 inputs ('enc' for Encryption, 'dec' for Decryption)\r\n"
					+ "	Ex. -mode enc \r\n" + "\r\n" + "-out \r\n"
					+ "	New output folder location for a successfully processed file in either mode\r\n"
					+ "	Not required. If not set, program will place processed files in the default locations:\r\n"
					+ "	$DRIVE:Documents/EncDec Files/Encrypted/, $DRIVE:Documents/EncDec Files/Decrypted/\r\n"
					+ "	Ex. -out /path/to/desired/folder/\r\n" + "\r\n" + "-noprint \r\n"
					+ "	Set program to turn off print output to console\r\n" + "	Not required. no inputs needed"
					+ "	If not used, program is set to print all output to console.\r\n" + "	Ex. -noprint");
			System.out.println("");
		}
		// program receive invalid arguments
		else {
			System.out.println("Invalid arguments. Please check you inputs and try again,");
			System.out.println("Option --help for help menu");
			System.out.println(
					"Example: -file path/to/file/or/folder -name filename (Encrypt Mode only) -mode enc -passcode abcd1234 -out path/to/folder -noprint");
			System.out.println("Passcode must be at least 8 alphanumeric chars.");
			System.out.println("Some special chars are accepted for passcode (!@#$%^&*_=+?/,.;:-).");
			System.exit(0);
		}

	}

	// command line
	private void cmd(String[] args) throws InterruptedException, IOException {

		// encdec command arg
		String fileLoc = null;
		String passcode = null;
		String filename = null;
		String mode = null;
		String outDir = null;

		boolean md = true;
		boolean pnt = true;
		boolean argFlag = false;

		/*
		 * encdec command args -passcode passcode -mode mode -file
		 * full/path/to/file/or/folder -name new file name (Encrypt mode only) -out
		 * full/path/to/out/folder -noprint option to turn on system prints
		 */
		if (args.length >= 6 && args.length <= 11) {
			for (int i = 0; i < (args.length); i++) {
				String arg = args[i];
				int valSlot = i + 1;
				if (arg != null) {
					switch (arg) {
					case "-passcode":
						if (args[valSlot] != null) {
							passcode = args[valSlot].replace("\"", "").replaceAll("'", "");
						}
						break;
					case "-mode":
						if (args[valSlot] != null) {
							mode = args[valSlot].replace("\"", "").replaceAll("'", "");
						}
						break;
					case "-file":
						if (args[valSlot] != null) {
							fileLoc = args[valSlot].replace("\"", "").replaceAll("'", "");
						}
						break;
					case "-name":
						if (args[valSlot] != null) {
							filename = args[valSlot].replace("\"", "").replaceAll("'", "");
						}
						break;
					case "-out":
						if (args[valSlot] != null) {
							outDir = args[valSlot].replace("\"", "").replaceAll("'", "");
						}
						break;
					case "-noprint":
						pnt = false;
						break;
					default:
						if (arg.startsWith("-")) {
							argFlag = true;
						} else {
							String[] argsGood = { "-passcode", "-mode", "-file", "-name", "-out" };
							for (String ag : argsGood) {
								if (args[i - 1] != null) {
									if (args[i - 1].equals(ag)) {
										argFlag = false;
										break;
									}
								} else {
									argFlag = true;
									break;
								}
							}
						}
					}
				}

				if (argFlag) {
					break;
				}

			}

			// invalid args found
			if (argFlag) {
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

			// EncDec mode
			boolean eMode = false;

			// set mode
			if (mode != null) {
				if (mode.trim().toLowerCase().equals("enc")) {
					eMode = true;
				} else if (mode.trim().toLowerCase().equals("dec")) {
					eMode = false;
				} else {
					md = false;
				}
			} else {
				md = false;
			}

			if (md) {
				EncDec ed = new EncDec();
				ed.config();
				ed.process(new File(fileLoc), passcode, filename, outDir, eMode, pnt);
			} else {
				if (pnt) {
					System.out.println("The mode is not set. (Ex. -mode enc for encryption, -mode dec for decryption.");
				}
				System.exit(0);
			}

		}

	}

	public static void main(String[] args) throws IOException, InterruptedException {

		// args = new String[11];
		// args[0] = "-file";
		// args[1] = "/path/to/desired/file/or/folder";
		// args[2] = "-mode";
		// args[3] = "dec";
		// args[4] = "-passcode";
		// args[5] = "abcd1234";
		// args[6] = "-out";
		// args[7] = "/path/to/desired/folder";
		// args[8] = "-name";
		// args[9] = "filename";
		// args[10] = "-noprint";

		// args = new String[1];
		// args[0] = "--help";

		EncDec ed = new EncDec();
		ed.config();

		if (args.length >= 6) {
			new MAIN().cmd(args);
		} else if (args.length == 1) {
			new MAIN().help(args);
		}
		// program receive invalid arguments
		else {
			System.out.println("Invalid arguments. Please check you inputs and try again,");
			System.out.println("Option --help for help menu");
			System.out.println(
					"Example: -file path/to/file/or/folder -name filename (Encrypt Mode only) -mode enc -passcode abcd1234 -out path/to/folder -noprint");
			System.out.println("Passcode must be at least 8 alphanumeric chars.");
			System.out.println("Some special chars are accepted for passcode (!@#$%^&*_=+?/,.;:-).");
			System.exit(0);
		}

	}

}