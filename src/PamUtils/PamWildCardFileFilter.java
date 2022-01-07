package PamUtils;

import java.io.File;
import java.util.List;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.WildcardFileFilter;

public class PamWildCardFileFilter extends WildcardFileFilter {
	
	private boolean acceptFolders;

	public PamWildCardFileFilter(List<String> wildcards, IOCase caseSensitivity) {
		super(wildcards, caseSensitivity);
	}

	public PamWildCardFileFilter(List<String> wildcards) {
		super(wildcards);
	}

	public PamWildCardFileFilter(String wildcard, IOCase caseSensitivity) {
		super(wildcard, caseSensitivity);
	}

	public PamWildCardFileFilter(String wildcard) {
		super(wildcard);
	}

	public PamWildCardFileFilter(String[] wildcards, IOCase caseSensitivity) {
		super(wildcards, caseSensitivity);
	}

	public PamWildCardFileFilter(String[] wildcards) {
		super(wildcards);
	}

	@Override
	public boolean accept(File dir, String name) {
		return super.accept(dir, name);
	}

	@Override
	public boolean accept(File file) {
		if (file.isDirectory() && acceptFolders) {
			return true;
		}
		else {
			return super.accept(file);
		}
	}

	/**
	 * @return the acceptFolders
	 */
	public boolean isAcceptFolders() {
		return acceptFolders;
	}

	/**
	 * @param acceptFolders the acceptFolders to set
	 */
	public void setAcceptFolders(boolean acceptFolders) {
		this.acceptFolders = acceptFolders;
	}

}
