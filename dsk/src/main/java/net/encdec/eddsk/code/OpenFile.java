package net.encdec.eddsk.code;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

public class OpenFile {
	// open filechooser to Documents tree
	JFileChooser filechooser = new JFileChooser(FileSystemView.getFileSystemView().getDefaultDirectory().toString());

	// get file from dialog box
	public String PickMe(String str) {

		filechooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

		// filechooser.setFileFilter(filter);
		if (filechooser.showOpenDialog(MAIN.frame) == JFileChooser.APPROVE_OPTION) {

			// get file
			File file = filechooser.getSelectedFile();

			// read text from file
			str = file.getAbsolutePath();

		} else {
			// no file selected
			str = "No file was selected";
		}

		// return full path of file
		return str;

	}

}
