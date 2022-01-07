package binaryFileStorage;

import java.io.File;

public interface FileCopyObserver {
	public boolean copyProgress(File source, File dest, int unitsCopied);
}
