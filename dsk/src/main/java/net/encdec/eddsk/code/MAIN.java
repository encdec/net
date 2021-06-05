package net.encdec.eddsk.code;

/*
* This EncDec program takes a file from the operating user and processes
* its bytes through either an encryption or decryption method. For encrypting,
* the data bytes are converted into an encrypted byte stream using a user-created 
* passcode and written to an output result file. For decrypting, encrypted files 
* originally created and saved can be decrypted successfully with its original 
* passcode. All successfully processed files will be saved to either the program's 
* root folder or a user-specified location. The .jar file for this program can run as 
* a GUI (Graphical User Interface), in the command line (windows) / shell (linux), or
* as a dependency within another java program. However, the compiled program for 
* windows and linux can only run as a GUI.
*
* @author  EncDec, LLC
* @version 1.0.1
* @since   2019-05-01 
*/

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;

import net.encdec.eddsk.res_files.GetResFile;

public class MAIN extends JFrame {

	private static final long serialVersionUID = 1L;

	// frames and panels
	private static JFrame aFrame = new JFrame("About");
	private static JPanel aPanel = new JPanel();
	private static JLabel aLabel = new JLabel();
	private static JFrame iFrame = new JFrame("Command Line");
	private static JPanel iPanel = new JPanel();
	private static JLabel iLabel1 = new JLabel();
	private static JLabel iLabel2 = new JLabel();
	protected static JFrame frame = new JFrame();
	private static JPanel panel = new JPanel();

	// menu bar and items
	private static JMenuBar menuBar = new JMenuBar();
	private static JMenuItem help, exit;
	private static JMenuItem instructions, about, close;

	// labels
	private static JLabel modeLbl = new JLabel("Encrypt Mode");
	private static JLabel selectLbl = new JLabel("Select a File or Folder to upload");
	private static JLabel codeLbl = new JLabel("Enter the passcode (min. 8 alphanumeric chars)");
	private static JLabel fileNameLbl = new JLabel("New File Name (optional for Encrypt Mode)");
	protected static JLabel out1Lbl = new JLabel("Output Status: ");
	protected static JLabel out3Lbl = new JLabel();
	protected static JLabel alertlabel = new JLabel();

	// text boxes
	protected static JTextField fileTxtBx = new JTextField();
	protected static JTextField passcodeTxtBx = new JTextField();
	protected static JTextField fileNameTxtBx = new JTextField();
	protected static JTextField fileNameResponseTxtBx = new JTextField();

	// buttons
	protected static JButton upldBtn = new JButton("Upload");
	protected static JButton encDecBtn = new JButton("Encrypt File");
	protected static JButton switchBtn = new JButton("Switch Mode");
	protected static JButton clrBtn = new JButton("Clear");
	protected static JButton clsBtn = new JButton("Close");

	// File chooser
	private static OpenFile opf = new OpenFile();

	// progress bar
	protected final static JProgressBar bar = new JProgressBar();

	// Encrypt/Decrypt action
	private static ActionListener al;
	protected static Runnable run;
	protected static Thread th;
	protected static boolean open = false;

	// Encrypt/Decrypt result
	protected static boolean result = false;

	// EncDec mode
	protected static boolean eMode = false;

	// Operating System
	protected static int OSset = -1;
	protected static String slash = null;

	// Error String
	protected static String exception = null;

	// EncDec Folders
	protected static File encFldr = null;
	protected static File decFldr = null;

	// regex
	static String alphanumeric = "^[a-zA-Z0-9!@#$%^&*_=+?/,.;:-]*$";
	static String pattern = "^[a-zA-Z0-9._\\s-]*$";

	// file counters
	protected static int currentFile = 0;
	protected static int totalFiles = 0;
	protected static int subCurrentFile = 0;
	protected static int subTotalFiles = 0;

	// finished vals
	protected static boolean multiple = false;

	@SuppressWarnings({ "unused", "rawtypes" })
	private static void run() throws IOException, InterruptedException {
		// frame(s) constructor
		panel.setLayout(null);
		panel.setSize(new Dimension(700, 425));

		// menu
		help = new JMenu("Help");
		menuBar.add(help);
		instructions = new JMenuItem("Instructions");
		instructions.setMnemonic(KeyEvent.VK_D);
		instructions.addActionListener(new InstLstnr());
		help.add(instructions);
		about = new JMenuItem("About");
		about.setMnemonic(KeyEvent.VK_D);
		about.addActionListener(new aboutLstnr());
		help.add(about);

		exit = new JMenu("Exit");
		exit.setMnemonic(KeyEvent.VK_X);
		menuBar.add(exit);
		close = new JMenuItem("Close");
		close.setMnemonic(KeyEvent.VK_D);
		close.addActionListener(new clsAction(frame));
		exit.add(close);

		frame.setJMenuBar(menuBar);

		// attributes
		Border border = BorderFactory.createLineBorder(Color.BLACK);
		Font font;
		Map attributes;

		// labels, textboxes, buttons
		// set bounds x, y, len, hgt
		modeLbl.setBounds(277, 10, 185, 50);
		modeLbl.setForeground(Color.RED);
		modeLbl.setFont(new Font("Dialog", Font.BOLD, 22));
		panel.add(modeLbl);

		selectLbl.setBounds(75, 50, 400, 25);
		selectLbl.setFont(new Font("Dialog", Font.BOLD, 12));
		panel.add(selectLbl);

		fileTxtBx.setBounds(75, 70, 400, 25);
		panel.add(fileTxtBx);
		fileTxtBx.setEditable(false);

		upldBtn.setBounds(500, 70, 140, 28);
		panel.add(upldBtn);
		upldBtn.addActionListener(new upldAction());

		codeLbl.setBounds(75, 110, 400, 25);
		codeLbl.setFont(new Font("Dialog", Font.BOLD, 12));
		panel.add(codeLbl);

		passcodeTxtBx.setBounds(75, 130, 400, 25);
		panel.add(passcodeTxtBx);

		fileNameLbl.setBounds(75, 165, 400, 25);
		fileNameLbl.setFont(new Font("Dialog", Font.BOLD, 12));
		panel.add(fileNameLbl);

		fileNameTxtBx.setBounds(75, 185, 400, 25);
		panel.add(fileNameTxtBx);
		fileNameTxtBx.setEditable(true);

		encDecBtn.setBounds(75, 245, 125, 35);
		encDecBtn.setForeground(Color.RED);
		panel.add(encDecBtn);

		switchBtn.setBounds(225, 245, 125, 35);
		panel.add(switchBtn);
		switchBtn.addActionListener(new switchMode());

		clrBtn.setBounds(370, 245, 125, 35);
		panel.add(clrBtn);
		clrBtn.addActionListener(new clrAction());

		clsBtn.setBounds(520, 245, 125, 35);
		panel.add(clsBtn);
		clsBtn.addActionListener(new clsAction(frame));

		out1Lbl.setBounds(75, 300, 650, 25);
		panel.add(out1Lbl);

		out3Lbl.setBounds(75, 320, 650, 25);
		panel.add(out3Lbl);

		fileNameResponseTxtBx.setBounds(75, 340, 570, 25);
		panel.add(fileNameResponseTxtBx);
		fileNameResponseTxtBx.setEditable(false);

		al = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				run = new Runnable() {
					@Override
					public void run() {
						try {
							edAction();
						} catch (Exception e) {
							try {
								LogFiles.lg.logErrors(e, this.getClass());
							} catch (Exception e1) {
								// e1.printStackTrace();
							}
						}
					}
				};
				open = true;
				th = new Thread(run);
				th.start();
			};
		};
		encDecBtn.addActionListener(al);

		eMode = true;

		// get EncDec icon
		GetResFile grf = new GetResFile();
		Object obj = grf.loadfile(null, 0);

		URL imgURL = (URL) obj;
		if (imgURL != null) {
			ImageIcon img = new ImageIcon(imgURL);
			frame.setIconImage(img.getImage());
			aFrame.setIconImage(img.getImage());
			iFrame.setIconImage(img.getImage());
		}
		UIManager.put("OptionPane.minimumSize", new Dimension(300, 120));

		// instructions frame
		iFrame.setTitle("Instructions");
		iFrame.setSize(575, 650);
		iFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		iFrame.setLocationRelativeTo(null);
		iFrame.setResizable(false);
		String ilbl1 = "Thank you for using EncDec!";
		iLabel1.setFont(new Font("Dialog", Font.BOLD, 16));
		String ilbl2 = "<html>This README file explains how to use the EncDec executable program from EncDec, LLC, "
				+ "available for download on the Internet at https://www.encdec.net<br>" + "<br>"
				+ "PROGRAM STARTUP<br>"
				+ "On startup, the EncDec program will create four folders onto your file system in the Documents tree: "
				+ "'EncDec Files', 'Encrypted', 'Decrypted', and 'Logs'. The EncDec Files folder is the root folder of the program. "
				+ "The Encrypted and Decrypted folders are the subfolders that will store your created files. "
				+ "The Logs folder logs all file transactions, both standard and error.<br>" + "<br>" + "SET MODE<br>"
				+ "Set the mode you desire using the Switch Mode button (Encrypt or Decrypt, defaults "
				+ "to Encrypt on startup). Upload a file or folder you want to process in the first textbox using "
				+ "the Upload button. The upload textbox is disabled to prevent changes to the file "
				+ "location obtained through your OS system.<br>" + "<br>" + "ENCRYPT MODE<br>"
				+ "Enter a passcode string of at least 8 alphanumeric chars to encrypt the file. Some special chars are "
				+ "accepted (!@#$%^&*_=+?/,.;:-). DO NOT lose or forget your passcode. If the new encrypted file is successfully created, "
				+ "the original passcode will be needed to decrypt it. Otherwise, the encrypted file or files will be irrecoverable. "
				+ "Optionally, you can give the new encrypted file(s) a new file name. If not, the "
				+ "file name will default to the name 'file'. Click the Encrypt File button. If successful, the program will "
				+ "produce a success message in green color at the bottom of the panel. It will also print the "
				+ "full location of the new file(s) in the bottom disabled textbox. You may copy this location "
				+ "and paste it into a file explorer window to retrieve your new file output.<br>" + "<br>"
				+ "DECRYPT MODE<br>"
				+ "Enter the known alphanumeric passcode that was used to encrypt the file(s). The New File Name textbox "
				+ "is disabled in decrypt mode because it is not needed. Click the Decrypt File button. "
				+ "If successful, the program will produce a success message in green color at the bottom "
				+ "of the panel. It will also print the full location of the decrypted file(s) in the bottom disabled "
				+ "textbox. You may copy this location and paste it into a file explorer window to retrieve your new file output.</html>";

		iLabel1.setText(ilbl1);
		iLabel2.setText(ilbl2);
		iLabel1.setBounds(175, 5, 510, 35);
		iLabel2.setBounds(5, 35, 560, 570);
		iPanel.setLayout(null);
		iPanel.setSize(new Dimension(575, 650));
		iPanel.add(iLabel1);
		iPanel.add(iLabel2);
		iFrame.add(iPanel);

		// About frame
		aFrame.setTitle("About");
		aFrame.setSize(250, 300);
		aFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		aFrame.setLocationRelativeTo(null);
		aFrame.setResizable(false);
		String albl = "<html>Owner:<br>" + "EncDec, LLC<br><br>" + "Version:<br>" + "1.0.2<br><br>"
				+ "Company Name:<br>" + "EncDec, LLC<br><br>" + "Website:<br>" + "https://www.encdec.net<br><br>"
				+ "Copyright:<br>" + "2019</html>";
		aLabel.setText(albl);
		aLabel.setBounds(10, 12, 200, 220);
		aPanel.setLayout(null);
		aPanel.setSize(new Dimension(250, 300));
		aPanel.add(aLabel);
		aFrame.add(aPanel);

		// progress bar settings
		bar.setVisible(false);
		bar.setStringPainted(false);
		bar.setString("");
		bar.setIndeterminate(false);
		bar.setLocation(75, 370);
		bar.setSize(570, 25);
		panel.add(bar);

		// main frame
		frame.setTitle("EncDec");
		frame.setSize(700, 455);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(panel);
		frame.setLocation(100, 200);
		frame.hasFocus();
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setVisible(true);

	}

	static class upldAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			// file upload
			String path = null;
			path = opf.PickMe(path);
			fileTxtBx.setText(path);
			fileTxtBx.setEditable(false);

		}
	}

	static class switchMode implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			// switch between enc and dec
			if (eMode == true) {
				eMode = false;
				encDecBtn.setText("Decrypt File");
				encDecBtn.setForeground(Color.BLUE);
				modeLbl.setForeground(Color.BLUE);
				modeLbl.setText("Decrypt Mode");
				fileNameTxtBx.setEditable(false);
				fileNameTxtBx.setText("");
			} else if (eMode == false) {
				eMode = true;
				encDecBtn.setText("Encrypt File");
				encDecBtn.setForeground(Color.RED);
				modeLbl.setForeground(Color.RED);
				modeLbl.setText("Encrypt Mode");
				fileNameTxtBx.setEditable(true);
			}

		}
	}

	// GUI
	private static void edAction() throws IOException {

		try {
			// check folder creation again
			Reset r = new Reset();
			// create folders
			r.createFolders();

			String nfs = "No file was selected";
			String blank1 = "";
			String blank2 = null;
			String ftb = fileTxtBx.getText();
			String ptb = passcodeTxtBx.getText();
			String fileName = fileNameTxtBx.getText();

			// check if file textbox is empty
			if (ftb.trim().equals(nfs) || ftb.trim().equals(blank1) || ftb == blank2) {
				if (OSset == 0) {
					JOptionPane.showMessageDialog(frame, "Please upload a file.", "Upload Error",
							JOptionPane.ERROR_MESSAGE, null);
				}
				out1Lbl.setForeground(Color.RED);
				out1Lbl.setText("Output Status: Upload Error");
				out3Lbl.setText("Please upload a file.");
				Reset.resetBtns();
				return;
			}

			// check if passcode textbox is empty
			if (ptb.trim().equals(nfs) || ptb.trim().equals(blank1) || ptb == blank2) {
				if (OSset == 0) {
					JOptionPane.showMessageDialog(frame, "Please enter a passocde.", "Passcode Error",
							JOptionPane.ERROR_MESSAGE, null);
				}
				out1Lbl.setForeground(Color.RED);
				out1Lbl.setText("Output Status: Passcode Error");
				out3Lbl.setText("Please enter a passocde.");
				Reset.resetBtns();
				return;
			}
			// validate if passcode meets alphanumeric requirement
			else if (ptb.length() < 8 || !ptb.matches(alphanumeric)) {
				if (OSset == 0) {
					JOptionPane.showMessageDialog(frame,
							"Minimum 8 alphanumeric chars for passocde. \r\n"
									+ "Some special chars accepted (!@#$%^&*_=+?/,.;:-).",
							"Passcode Message", JOptionPane.ERROR_MESSAGE, null);
				}
				out1Lbl.setForeground(Color.RED);
				out1Lbl.setText("Output Status: Passcode Error");
				out3Lbl.setText("Min. 8 alphanumeric chars. Some special chars accepted (!@#$%^&*_=+?/,.;:-).");
				Reset.resetBtns();
				return;
			}

			boolean chars = true;
			File processFile = new File(ftb);

			try {
				// validate filename for Encrypt Mode only
				if (eMode == true) {
					// no more than 50 characters
					if (fileName.length() > 50) {
						if (OSset == 0) {
							JOptionPane.showMessageDialog(frame, "File name is too long. Reduce to 50 chars or less.",
									"File Name Error", JOptionPane.ERROR_MESSAGE, null);
						}
						out1Lbl.setForeground(Color.RED);
						out1Lbl.setText("Output Status: File Name Error");
						out3Lbl.setText("File name is too long. Reduce to 50 chars or less.");
						Reset.resetBtns();
						return;
					}
					// passes alphanumeric filename character set
					else if (!fileName.matches(pattern)) {
						if (OSset == 0) {
							JOptionPane
									.showMessageDialog(frame,
											"File name can only contain letters, numbers, and some special chars "
													+ ".,<space>,-,_",
											"File Name Error", JOptionPane.ERROR_MESSAGE, null);
						}
						out1Lbl.setForeground(Color.RED);
						out1Lbl.setText("Output Status: File Name Error");
						out3Lbl.setText(
								"File name can only contain letters, numbers, and some special chars .,<space>,-,_).");
						Reset.resetBtns();
						return;
					}
				}
			} catch (Exception e) {
				chars = false;
				MAIN.exception = e.getMessage();
				LogFiles.lg.logErrors(e, MAIN.class);
			}

			// check if file location exists
			if (!processFile.exists()) {
				if (OSset == 0) {
					JOptionPane.showMessageDialog(frame, "The selected file does not exist at the current location.",
							"File Location Error", JOptionPane.ERROR_MESSAGE, null);
				}
				out1Lbl.setForeground(Color.RED);
				out1Lbl.setText("Output Status: File Location Error");
				out3Lbl.setText("The selected file does not exist at the current location.");
				Reset.resetBtns();
				return;
			}

			// enable/disable some GUI components
			Reset.runningED(processFile.length());
			String[] results = null;

			if (chars) {
				results = new String[2];
				// Call Encryption/Decryption program
				String outDir = null;
				if (eMode) {
					outDir = MAIN.encFldr.getAbsolutePath();
				} else if (!eMode) {
					outDir = MAIN.decFldr.getAbsolutePath();
				}
				EncDec ed = new EncDec();
				String outDirWithDate = outDir + MAIN.slash + getDateString(null);
				results = ed.process(processFile, ptb, fileName, outDirWithDate, eMode);
			}

			boolean partial = false;
			if (results != null) {
				if (results[0].equals("4")) {
					MAIN.result = false;
				} else if (results[0].equals("3")) {
					MAIN.result = true;
					partial = true;
				} else if (results[0].equals("2")) {
					MAIN.result = true;
					partial = false;
				} else if (results[0].equals("1")) {
					MAIN.result = true;
				} else if (results[0].equals("0")) {
					MAIN.result = false;
				}
			}
			File fileFinished = new File(results[1]);

			// if file process successfully
			if (MAIN.result == true) {
				// new output status
				Color dg = new Color(54, 185, 105);
				// encrypt mode
				if (MAIN.eMode == true) {
					if (!partial) {
						if (MAIN.OSset == 0) {
							alertlabel.setText("<html><center>Output Status: Success!<br>"
									+ "Your file was successfully encrypted.</html>");
							JOptionPane.showMessageDialog(frame, alertlabel, "Encryption Success",
									JOptionPane.INFORMATION_MESSAGE, null);
						}
						MAIN.out1Lbl.setForeground(dg);
						MAIN.out1Lbl.setText("Output Status: Success!");
						MAIN.out3Lbl.setText("Your file was successfully encrypted. New file available at location: ");
						MAIN.fileNameResponseTxtBx.setText(fileFinished.getAbsolutePath());
						String out = "The file, " + fileFinished.getName()
								+ ", was successfully encrypted. New file is available at drive location: "
								+ fileFinished.getAbsolutePath();
						Desktop.getDesktop().open(fileFinished);
						LogFiles.lg.logInfo(out, MAIN.class);
					} else if (partial) {
						if (MAIN.OSset == 0) {
							alertlabel.setText(
									"<html><center>Output Status: Partial Success?<br>Only some of your files were successfully encrypted.</html>");
							JOptionPane.showMessageDialog(frame, alertlabel, "Partial Success",
									JOptionPane.INFORMATION_MESSAGE, null);
						}
						MAIN.out1Lbl.setForeground(dg);
						MAIN.out1Lbl.setText("Output Status: Partial Success?");
						MAIN.out3Lbl.setText(
								"Only some of your files were successfully encrypted. New file available at location: ");
						MAIN.fileNameResponseTxtBx.setText(fileFinished.getAbsolutePath());
						String out = "Only some of your files were successfully encrypted. New file available at location: "
								+ fileFinished.getAbsolutePath();
						Desktop.getDesktop().open(fileFinished);
						LogFiles.lg.logInfo(out, MAIN.class);
					}

				}
				// decrypt mode
				else if (MAIN.eMode == false) {
					if (!partial) {
						if (MAIN.OSset == 0) {
							alertlabel.setText("<html><center>Output Status: Created!<br>"
									+ "Your file was successfully decrypted.</html>");
							JOptionPane.showMessageDialog(frame, alertlabel, "Decryption Success",
									JOptionPane.INFORMATION_MESSAGE, null);
						}
						MAIN.out1Lbl.setForeground(dg);
						MAIN.out1Lbl.setText("Output Status: Created!");
						MAIN.out3Lbl.setText("Your file was successfully decrypted. New file available at location: ");
						MAIN.fileNameResponseTxtBx.setText(fileFinished.getAbsolutePath());
						String out = "The file, " + fileFinished.getName()
								+ ", was successfully decrypted. New file is available at drive location: "
								+ fileFinished.getAbsolutePath();
						Desktop.getDesktop().open(fileFinished);
						LogFiles.lg.logInfo(out, MAIN.class);
					} else if (partial) {
						if (MAIN.OSset == 0) {
							alertlabel.setText(
									"<html><center>Output Status: Partial Success?<br>Only some of your files were successfully decrypted.</html>");
							JOptionPane.showMessageDialog(frame, alertlabel, "Partial Success",
									JOptionPane.INFORMATION_MESSAGE, null);
						}
						MAIN.out1Lbl.setForeground(dg);
						MAIN.out1Lbl.setText("Output Status: Partial Creation?");

						MAIN.out3Lbl.setText(
								"Only some of your files were successfully decrypted. New file available at location: ");
						MAIN.fileNameResponseTxtBx.setText(fileFinished.getAbsolutePath());
						String out = "Only some of your files were successfully decrypted. New file available at location: "
								+ fileFinished.getAbsolutePath();
						Desktop.getDesktop().open(fileFinished);
						LogFiles.lg.logInfo(out, MAIN.class);
					}
				}

			}

			// if file failed to process
			else if (MAIN.result == false) {
				// new output status
				if (MAIN.OSset == 0) {
					alertlabel.setText("<html><center>Output Status: Failure!<br>"
							+ "The file was not processed.<br>Please check the passcode and if the correct file was chosen for the right mode.</html>");
					JOptionPane.showMessageDialog(frame, alertlabel, "Failed Operation", JOptionPane.ERROR_MESSAGE,
							null);
				}
				MAIN.out1Lbl.setForeground(Color.red);
				MAIN.out1Lbl.setText("Output Status: Failure!");
				MAIN.out3Lbl.setText("The file was not processed.");
				MAIN.fileNameResponseTxtBx.setText("Error: " + MAIN.exception);
				MAIN.exception = null;
				String out = "The file, " + fileFinished.getName()
						+ ", failed to be processed. Please check the passcode and if the "
						+ "correct file was chosen for the right mode.";
				LogFiles.lg.logInfo(out, MAIN.class);
			}

			// enable/disable some GUI components
			Reset.resetBtns();
			Reset.resetPBar();

		} catch (Exception e) {
			MAIN.exception = e.getMessage();
			LogFiles.lg.logErrors(e, MAIN.class);
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

	static class clrAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			// reset output status
			try {
				Reset.resetAll();
			} catch (IOException e1) {
				// e1.printStackTrace();
			}

		}
	}

	// close program
	static class clsAction implements ActionListener {
		private JFrame toBeClosed;

		public clsAction(JFrame toBeClosed) {
			this.toBeClosed = toBeClosed;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			toBeClosed.setVisible(false);
			toBeClosed.dispose();
		}
	}

	// open About frame
	static class aboutLstnr implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				aFrame.hasFocus();
				aFrame.setVisible(true);
			} catch (Exception e1) {
				try {
					LogFiles.lg.logErrors(e1, aboutLstnr.class);
				} catch (Exception e2) {
					// e2.printStackTrace();
				}
			}
		}
	}

	// open Instructions frame
	static class InstLstnr implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				iFrame.hasFocus();
				iFrame.setVisible(true);
			} catch (Exception e1) {
				try {
					LogFiles.lg.logErrors(e1, InstLstnr.class);
				} catch (Exception e2) {
					// e2.printStackTrace();
				}
			}
		}
	}

	// main method
	public static void main(String[] args) throws IOException, InterruptedException {

		Reset r = new Reset();
		// determine os
		r.findOS();

		// create log files
		LogFiles.lg.create();
		LogFiles.lg.logFiles();

		// create folders
		r.createFolders();

		if (args.length == 0) {
			run();
		}

	}

}