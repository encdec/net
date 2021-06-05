package net.encdec.edcmd.code;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Reset extends MAIN {
	private static DecimalFormat df2 = new DecimalFormat("#.##");
	private static LogFiles lg = new LogFiles();

	protected void findOS() {
		// detect OS
		if (slash == null) {
			String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
			if ((OS.indexOf("mac") >= 0) || (OS.indexOf("darwin") >= 0)) {
				slash = "/";
				OSset = 2;
			} else if (OS.indexOf("win") >= 0) {
				slash = "\\";
				OSset = 0;
			} else if (OS.indexOf("nux") >= 0) {
				slash = "/";
				OSset = 1;
			} else if (OS.indexOf("nix") >= 0) {
				slash = "/";
				OSset = 3;
			} else {
				slash = "/";
				OSset = 4;
			}
		}
	}

	// discover operating system and EncDec create folders
	protected void createFolders() throws IOException {
		// create EncDec folders
		File myDocs = lg.getCmd();
		encFldr = new File(myDocs.getAbsolutePath() + slash + "Encrypted");
		decFldr = new File(myDocs.getAbsolutePath() + slash + "Decrypted");

		if (!encFldr.exists()) {
			encFldr.mkdirs();
		}
		if (!decFldr.exists()) {
			decFldr.mkdirs();
		}
	}

	protected void repaint(String fn, boolean pnt, double calc, int stage, boolean last) {
		if (fn.length() > 50) {
			fn = fn.substring(0, 50) + "   ..........   " + fn.substring(fn.lastIndexOf(".") - 5);
		}
		if (pnt) {
			try {
				String[] stages = { "Processing ", "Zipping ", "Retrieving " };
				if (calc <= 99.00 && !last) {
					System.out.write("\r".getBytes());
					System.out.write((stages[stage] + fn + " (" + df2.format(calc) + " %)").getBytes());
					System.out.write("\b\b\b".getBytes());
				} else if (calc == 100.00) {
					System.out.write("\r".getBytes());
					System.out.write((stages[stage] + fn + " (100.00 %)").getBytes());
					System.out.write("\n".getBytes());
				}
				TimeUnit.MILLISECONDS.sleep(5);
			} catch (IOException | InterruptedException e) {
				// nothing
			}
		}
	}
}
