package net.encdec.eddsk.code;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.filechooser.FileSystemView;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import net.encdec.eddsk.res_files.GetResFile;

public class LogFiles {
	public static LogFiles lg = new LogFiles();
	// subdirs in EncDec Files root folder
	private static File edFiles = new File(FileSystemView.getFileSystemView().getDefaultDirectory().toString(),
			"EncDec Files");
	private static File dsk = new File(edFiles.getAbsolutePath(), "dsk");
	private static File cmd = new File(edFiles.getAbsolutePath(), "cmd");
	private static File edconfig = new File(dsk.getAbsolutePath(), "edconfig");
	private static File logs = new File(edconfig.getAbsolutePath(), "logs");

	public void create() throws IOException {
		if (!edFiles.exists()) {
			edFiles.mkdirs();
		}
		if (!dsk.exists()) {
			dsk.mkdirs();
		}
		if (!cmd.exists()) {
			cmd.mkdirs();
		}
		if (!edconfig.exists()) {
			edconfig.mkdirs();
		}
		if (!logs.exists()) {
			logs.mkdirs();
		}
	}

	public File getDsk() {
		return dsk;
	}

	public File getCmd() {
		return cmd;
	}

	public File getLogs() {
		return logs;
	}

	@SuppressWarnings("rawtypes")
	public void logErrors(Exception e, Class cl) {
		Logger.getRootLogger().setLevel(Level.ERROR);
		final Logger logger = Logger.getLogger(cl);
		logger.error(e.getMessage());
		StringWriter writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		e.printStackTrace(printWriter);
		logger.error(writer.toString());
		Logger.getRootLogger().setLevel(Level.OFF);
	}

	@SuppressWarnings("rawtypes")
	public void logInfo(String info, Class cl) {
		Logger.getRootLogger().setLevel(Level.INFO);
		final Logger logger = Logger.getLogger(cl);
		logger.info(info);
		Logger.getRootLogger().setLevel(Level.OFF);
	}

	public void logFiles() {
		try {
			String edLogDir = getLogs().getAbsolutePath() + MAIN.slash;
			System.setProperty("ed.logging.logfile", edLogDir);
			// BasicConfigurator.configure();
			boolean logFileSet = false;
			String log4jConfPath = edLogDir + "log4j.properties";
			File logConf = new File(log4jConfPath);
			if (!logConf.exists()) {
				new GetResFile().loadfile(new LogFiles().getLogs().getAbsolutePath(), 1);
				if (logConf.exists()) {
					logFileSet = true;
				}
			} else {
				logFileSet = true;
			}
			if (logFileSet) {
				PropertyConfigurator.configure(log4jConfPath);
			}
			Logger.getRootLogger().setLevel(Level.OFF);
		} catch (Exception e) {
			// nothing
		}
	}

}
