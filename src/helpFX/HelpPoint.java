package helpFX;

/**
 * Identifies a location in the PAMGuardFX help system: an absolute classpath
 * path to a Markdown file and an optional heading anchor within that file.
 *
 * <p>The path should be an absolute classpath path, e.g.:
 * <pre>
 *     new HelpPoint("/clickDetector/click_detector_help.md");
 *     new HelpPoint("/clickDetector/click_detector_help.md", "configuration");
 *     new HelpPoint("/helpFX/index.md");
 * </pre>
 * </p>
 *
 * @author PAMGuard team
 */
public class HelpPoint {

	/** Absolute classpath path to the Markdown file (e.g. {@code /clickDetector/click_detector_help.md}). */
	private final String markdownFile;

	/**
	 * Optional anchor (heading id) to scroll to inside the file.
	 * May be {@code null} if no specific heading is targeted.
	 */
	private final String anchor;

	/**
	 * Create a help point pointing at the top of a help file.
	 *
	 * @param markdownFile absolute classpath path to the Markdown file
	 */
	public HelpPoint(String markdownFile) {
		this(markdownFile, null);
	}

	/**
	 * Create a help point pointing at a specific heading anchor inside a help file.
	 *
	 * @param markdownFile absolute classpath path to the Markdown file
	 * @param anchor       heading anchor, or {@code null} for the top of the page
	 */
	public HelpPoint(String markdownFile, String anchor) {
		this.markdownFile = markdownFile;
		this.anchor = anchor;
	}

	/** @return the absolute classpath path to the Markdown file */
	public String getMarkdownFile() {
		return markdownFile;
	}

	/**
	 * @return the optional heading anchor, or {@code null}
	 */
	public String getAnchor() {
		return anchor;
	}

	@Override
	public String toString() {
		return anchor == null ? markdownFile : markdownFile + "#" + anchor;
	}
}