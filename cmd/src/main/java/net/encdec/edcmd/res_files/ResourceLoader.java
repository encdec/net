package net.encdec.edcmd.res_files;

public class ResourceLoader {

	// file resource paths
	public String getFile(int num) {

		String[] filepaths = new String[2];
		filepaths[0] = "EDLogo.png";
		filepaths[1] = "log4j.properties";

		return filepaths[num];
	}

}
