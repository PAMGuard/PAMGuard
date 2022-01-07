package backupmanager.stream;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;


public class FileStreamItem extends StreamItem {

	private File file;

	private BasicFileAttributes attr = null;
		
	public FileStreamItem(File file, Long startTime, Long endTime) {
		super(file.getAbsolutePath(), startTime, endTime, file.length());
		this.file = file;
	}

	/**
	 * Construct a FileStremItem that will take it's start and end times
	 * from the creation and modified times in the file attributes. 
	 * @param file
	 */
	public FileStreamItem(File file) {
		super(file.getAbsolutePath(), null, null, null);
		this.file = file;
		try {
			attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
		} catch (IOException e) {
			
		}
	}

	@Override
	public String getName() {
		return file.getAbsolutePath();
	}

	/**
	 * @return the file
	 */
	public File getFile() {
		return file;
	}

	@Override
	public Long getStartUTC() {
		if (super.getStartUTC() != null) {
			return super.getStartUTC();
		}
		if (attr != null) {
			FileTime c = attr.creationTime();
			if (c != null) {
				return c.toMillis();
			}
		}
		return null;
	}

	@Override
	public Long getEndUTC() {
		if (super.getEndUTC() != null) {
			return super.getEndUTC();
		}
		if (attr != null) {
			FileTime m = attr.lastModifiedTime();
			if (m != null) {
				return m.toMillis();
			}
		}
		return null;
	}

	@Override
	public Long getSize() {
		if (super.getSize() != null) {
			return super.getSize();
		}
		Long len = file.length();
		return len;
	}
	
	

}
