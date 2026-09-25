package difar.offline;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

/**
 * Copies binary files to a backup folder before the viewer replaces them.
 * <p>
 * The folder sits beside the binary store's folder, named from it and the
 * time of the first copy, and keeps the store's subfolders. One folder is
 * used for a whole viewer session, and each file is copied at most once, so
 * the copy is always the original.
 */
public class ViewerBackup {

	private final File storeRoot;
	private File backupRoot;
	private final Set<File> copied = new HashSet<>();

	/**
	 * @param storeRoot the binary store's top folder.
	 */
	public ViewerBackup(File storeRoot) {
		this.storeRoot = storeRoot.getAbsoluteFile();
	}

	/**
	 * @return the backup folder, or null if nothing has been copied yet.
	 */
	public File getBackupRoot() {
		return backupRoot;
	}

	/**
	 * Copy a file into the backup folder, unless it is already there or
	 * does not exist.
	 * @param file a file inside the binary store.
	 * @throws IOException if the copy fails.
	 */
	public void copy(File file) throws IOException {
		File source = file.getAbsoluteFile();
		if (!source.exists() || !copied.add(source)) {
			return;
		}
		if (backupRoot == null) {
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
			format.setTimeZone(TimeZone.getTimeZone("UTC"));
			backupRoot = new File(storeRoot.getParentFile(),
					storeRoot.getName() + "_backup_" + format.format(new Date()));
		}
		Path relative = storeRoot.toPath().relativize(source.toPath());
		Path target = backupRoot.toPath().resolve(relative);
		Files.createDirectories(target.getParent());
		Files.copy(source.toPath(), target, StandardCopyOption.COPY_ATTRIBUTES);
	}
}
