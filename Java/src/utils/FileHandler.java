package utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileHandler {
	private BufferedWriter bw;
	private FileWriter fw;
	private File file;
	public final String filePath;
	private final boolean append;
	private boolean clear = false;
	private boolean cleared = false;
	
	
	
	public FileHandler(String filePath, boolean append) {
		this.append = append;
		this.filePath = filePath;
	}
	
	public FileHandler(String filePath, boolean append, boolean clear) {
		this.filePath = filePath;
		this.append = append;
		this.clear = clear;
	}
	
	public void writeFile(String data) {
		try {
			this.openFile();
			this.bw.write(data);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			this.closeFile();
		}
	}
	
	private void openFile() {
		this.bw = null;
		this.fw = null;
		try {
			file = new File(this.filePath);

			if (!file.exists()) {
				file.createNewFile();
				this.cleared = true;
			} else if(this.clear && !this.cleared) {
				fw = new FileWriter(file.getAbsoluteFile());
				bw = new BufferedWriter(fw);
				bw.write("");
				this.cleared = true;
			}

			// true = append file
			fw = new FileWriter(file.getAbsoluteFile(), this.append);
			bw = new BufferedWriter(fw);

		} catch (IOException e) {
			e.printStackTrace();
			this.closeFile();
		} 
	}
	
	private void closeFile() {
		try {
			if (this.bw != null)
				this.bw.close();

			if (this.fw != null)
				this.fw.close();

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	

}
