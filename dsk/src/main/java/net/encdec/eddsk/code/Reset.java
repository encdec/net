package net.encdec.eddsk.code;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Locale;

public class Reset extends MAIN {
	private static final long serialVersionUID = 1L;
	private static DecimalFormat df2 = new DecimalFormat("#.##");

	public void findOS() {
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
	public void createFolders() throws IOException {
		// create EncDec folders
		File myDocs = LogFiles.lg.getDsk();
		encFldr = new File(myDocs.getAbsolutePath() + slash + "Encrypted");
		decFldr = new File(myDocs.getAbsolutePath() + slash + "Decrypted");

		if (!encFldr.exists()) {
			encFldr.mkdirs();
		}
		if (!decFldr.exists()) {
			decFldr.mkdirs();
		}
	}

	// reset all components
	@SuppressWarnings("deprecation")
	public static void resetAll() throws IOException {

		try {
			// clear output labels and check boxes
			out1Lbl.setForeground(Color.black);
			out1Lbl.setText("Output Status:");
			out3Lbl.setForeground(Color.black);
			out3Lbl.setText("");
			fileTxtBx.setText("");
			passcodeTxtBx.setText("");
			fileNameTxtBx.setText("");
			fileNameResponseTxtBx.setText("");

			// stop progress bar
			open = false;
			bar.setVisible(false);
			bar.setStringPainted(false);
			bar.setString("");
			bar.setIndeterminate(false);
			th.stop();

			// enable buttons
			upldBtn.setEnabled(true);
			encDecBtn.setEnabled(true);
			switchBtn.setEnabled(true);
			clrBtn.setEnabled(true);
			clsBtn.setEnabled(true);

			currentFile = 0;
			totalFiles = 0;
			subCurrentFile = 0;
			subTotalFiles = 0;

			// set current EncDec result to false
			result = false;
		} catch (Exception e) {
			exception = e.getMessage();
			LogFiles.lg.logErrors(e, Reset.class);
		}

	}

	// enable all buttons
	public static void resetBtns() throws IOException {

		try {
			upldBtn.setEnabled(true);
			encDecBtn.setEnabled(true);
			switchBtn.setEnabled(true);
			clrBtn.setEnabled(true);
			clsBtn.setEnabled(true);

			currentFile = 0;
			totalFiles = 0;
			subCurrentFile = 0;
			subTotalFiles = 0;

			// set current EncDec result to false
			result = false;
		} catch (Exception e) {
			exception = e.getMessage();
			LogFiles.lg.logErrors(e, Reset.class);
		}

	}

	// stop progress bar
	@SuppressWarnings("deprecation")
	public static void resetPBar() throws IOException {

		try {
			open = false;
			bar.setVisible(false);
			bar.setStringPainted(false);
			bar.setString("");
			bar.setIndeterminate(false);
			th.stop();
		} catch (Exception e) {
			exception = e.getMessage();
			LogFiles.lg.logErrors(e, Reset.class);
		}

	}

	// enable/disable buttons when EncDec call is made
	public static void runningED(long fs) throws IOException {

		try {
			// clear output labels
			out1Lbl.setForeground(Color.BLACK);
			out1Lbl.setText("Output Status:");
			out3Lbl.setText("");
			fileNameResponseTxtBx.setText("");

			// disable buttons
			upldBtn.setEnabled(false);
			encDecBtn.setEnabled(false);
			switchBtn.setEnabled(false);
			clrBtn.setEnabled(false);
			clsBtn.setEnabled(false);

			// set current EncDec result to false
			result = false;

			bar.setVisible(true);
			bar.setStringPainted(true);
			bar.setIndeterminate(true);
		} catch (Exception e) {
			exception = e.getMessage();
			LogFiles.lg.logErrors(e, Reset.class);
		}

	}

	/*
	 * public void repaint(double pert, String fn) {
	 * 
	 * if (pert >= 100) { pert = 100; }
	 * 
	 * if (fn.length() > 50) { fn = fn.substring(0, 50) + "   ..........   " +
	 * fn.substring(fn.lastIndexOf(".") - 5); } bar.setString(fn + " (" +
	 * df2.format(pert) + "%)");
	 * 
	 * }
	 */

	public void repaint(double pert, String fn, int stage) {

		int percentage = 100;
		int wordLength = 100;

		if (pert >= percentage) {
			pert = percentage;
		}

		if (fn.length() > wordLength) {
			fn = fn.substring(0, wordLength) + "   ..........   " + fn.substring(fn.lastIndexOf(".") - 5);
		}
		String[] stages = { "Processing ", "Zipping ", "Retrieving " };
		bar.setString(stages[stage] + fn + " (" + df2.format(pert) + "%)");
		out1Lbl.setText("Files: " + currentFile + " of " + totalFiles);
		if (multiple) {
			out3Lbl.setText("Sub Directory Files: " + subCurrentFile + " of " + subTotalFiles);
		}
	}

}