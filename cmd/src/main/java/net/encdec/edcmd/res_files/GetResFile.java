package net.encdec.edcmd.res_files;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.FileUtils;

public class GetResFile {

	// load EncDec icon
	public Object loadfile(String dest, int num) {
		Object obj = null;
		try {
			ResourceLoader rl = new ResourceLoader();
			String name = rl.getFile(num);
			URL url = GetResFile.class.getClassLoader().getResource(name);
			if (num <= 0) {
				obj = url;
			} else {
				File file = new File(dest, name);
				FileUtils.copyURLToFile(url, file);
				obj = file;
			}
			return obj;
		} catch (Exception e) {
			return obj;
		}
	}

}
