package PamController.settings;

public class ImportChoice {

	public static final int DONT_IMPORT = 0;
	public static final int IMPORT_NEW = 1;
	public static final int IMPORT_REPLACE = 2;
	private int importChoice;
	private String replaceModule;
	
	public ImportChoice(int importChoice, String existingModule) {
		this.importChoice = importChoice;
		this.replaceModule = existingModule;
	}

	/**
	 * @return the importChoice
	 */
	public int getImportChoice() {
		return importChoice;
	}

	/**
	 * @return the replaceModule
	 */
	public String getReplaceModule() {
		return replaceModule;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		switch (importChoice) {
		case IMPORT_NEW:
			return "Import as new module";
		case DONT_IMPORT:
			return "Don't Import";
		case IMPORT_REPLACE:
			return "Replace existing " + replaceModule;
		}
		return super.toString();
	}

}
