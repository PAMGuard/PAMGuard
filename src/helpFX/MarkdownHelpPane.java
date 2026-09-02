package helpFX;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.AttributeProvider;
import org.commonmark.renderer.html.AttributeProviderContext;
import org.commonmark.renderer.html.AttributeProviderFactory;
import org.commonmark.renderer.html.HtmlRenderer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import javafx.concurrent.Worker.State;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * A JavaFX pane that renders a Markdown help file as HTML inside a
 * {@link WebView}.
 *
 * <p>Markdown is converted to HTML with the <a href=
 * "https://github.com/commonmark/commonmark-java">commonmark-java</a> library
 * and wrapped in a GitHub-flavoured stylesheet ({@code github-markdown.css}) so
 * the rendered help looks like Markdown on GitHub. Using a {@code WebView} means
 * images scale to the available width (via {@code img &#123;max-width:100%&#125;}
 * in the CSS), which the previous renderer could not do.</p>
 *
 * <p>Markdown files are loaded from the classpath using an absolute path (e.g.
 * {@code /clickDetector/click_detector_help.md}). Images referenced relative to
 * the Markdown file's package directory are resolved from the classpath and
 * embedded directly into the HTML as {@code data:} URIs, so they render
 * regardless of how PAMGuard is packaged (IDE classes folder or jar). External
 * {@code http(s)} links open in the system browser rather than navigating the
 * help pane away from its content.</p>
 *
 * @author PAMGuard team
 */
public class MarkdownHelpPane extends VBox {

	/** Classpath root for the help index page (in the helpFX package). */
	static final String HELP_FX_ROOT = "/helpFX/";

	/** GitHub-flavoured stylesheet applied to the rendered HTML. */
	private static final String GITHUB_CSS = "/helpFX/github-markdown.css";

	private final WebView webView;
	private final WebEngine engine;

	private final Parser parser;
	private final HtmlRenderer renderer;

	/** The classpath directory prefix for the currently displayed file, e.g. {@code /rawDeepLearningClassifier/} */
	private String currentBaseDir = HELP_FX_ROOT;

	/** Cached contents of {@link #GITHUB_CSS}, inlined into every rendered page. */
	private final String cssText;

	/** Whether the rendered content uses the dark palette. Dark by default. */
	private boolean darkMode = true;

	/** The most recently rendered body HTML, kept so the theme can be re-applied without re-parsing. */
	private String lastBodyHtml;

	public MarkdownHelpPane() {
		webView = new WebView();
		engine = webView.getEngine();
		VBox.setVgrow(webView, Priority.ALWAYS);
		getChildren().add(webView);

		// GitHub-flavoured pipe tables (used by e.g. cpod_help.md).
		List<Extension> extensions = List.of(TablesExtension.create());
		parser = Parser.builder().extensions(extensions).build();
		renderer = HtmlRenderer.builder()
				.extensions(extensions)
				.attributeProviderFactory(new ImageAttributeProviderFactory())
				.build();

		String css = loadResource(GITHUB_CSS);
		cssText = (css != null) ? css : "";

		installLinkHandler();
	}

	/**
	 * Load and display the given {@link HelpPoint}.
	 *
	 * @param helpPoint the page (and optional anchor) to display
	 */
	public void display(HelpPoint helpPoint) {
		if (helpPoint == null) {
			displayIndex();
			return;
		}
		displayMarkdown(helpPoint.getMarkdownFile());
	}

	/** Display the help index / overview page. */
	public void displayIndex() {
		displayMarkdown(HELP_FX_ROOT + "index.md");
	}

	/**
	 * Load and display a Markdown file by its absolute classpath path.
	 *
	 * @param classpathPath absolute classpath path, e.g. {@code /clickDetector/click_detector_help.md}
	 */
	public void displayMarkdown(String classpathPath) {
		String path = classpathPath.startsWith("/") ? classpathPath : "/" + classpathPath;

		// Derive the base directory for resolving relative image paths
		int lastSlash = path.lastIndexOf('/');
		currentBaseDir = (lastSlash > 0) ? path.substring(0, lastSlash + 1) : "/";

		String markdownText = loadResource(path);
		if (markdownText == null) {
			markdownText = "# Help not found\n\nCould not load `" + path + "`.";
		}

		Node document = parser.parse(markdownText);
		lastBodyHtml = renderer.render(document);
		engine.loadContent(buildHtmlDocument(lastBodyHtml));
	}

	/**
	 * Switch the rendered content between the dark and light palette and re-render
	 * the current page. The chrome of {@link HelpViewerFX} is themed separately.
	 *
	 * @param dark {@code true} for the dark palette, {@code false} for light
	 */
	public void setDarkMode(boolean dark) {
		if (this.darkMode == dark) {
			return;
		}
		this.darkMode = dark;
		if (lastBodyHtml != null) {
			engine.loadContent(buildHtmlDocument(lastBodyHtml));
		}
	}

	/** @return {@code true} if the content is currently rendered with the dark palette. */
	public boolean isDarkMode() {
		return darkMode;
	}

	// -------------------------------------------------------------------------
	// HTML assembly
	// -------------------------------------------------------------------------

	/**
	 * Wrap rendered Markdown HTML in a full HTML document with the GitHub
	 * stylesheet inlined. A {@code dark} body class is added in night mode so the
	 * stylesheet can switch to GitHub's dark palette.
	 */
	private String buildHtmlDocument(String bodyHtml) {
		String bodyClass = darkMode ? "markdown-body dark" : "markdown-body";

		return "<!DOCTYPE html><html><head><meta charset=\"utf-8\">"
				+ "<style>\n" + cssText + "\n</style></head>"
				+ "<body class=\"" + bodyClass + "\">\n" + bodyHtml + "\n</body></html>";
	}

	// -------------------------------------------------------------------------
	// Image handling – embed classpath images as data: URIs
	// -------------------------------------------------------------------------

	private class ImageAttributeProviderFactory implements AttributeProviderFactory {
		@Override
		public AttributeProvider create(AttributeProviderContext context) {
			return new ImageAttributeProvider();
		}
	}

	/**
	 * Rewrites the {@code src} of every {@code <img>} so that classpath-relative
	 * images are embedded as {@code data:} URIs. Absolute web URLs are left
	 * untouched.
	 */
	private class ImageAttributeProvider implements AttributeProvider {
		@Override
		public void setAttributes(Node node, String tagName, Map<String, String> attributes) {
			if (!"img".equals(tagName)) {
				return;
			}
			String dataUri = resolveImageToDataUri(attributes.get("src"));
			if (dataUri != null) {
				attributes.put("src", dataUri);
			}
		}
	}

	/**
	 * Resolve an image {@code src} to a {@code data:} URI by reading it from the
	 * classpath. Returns {@code null} (leaving the original {@code src} in place)
	 * for absolute web URLs or when the resource cannot be found.
	 */
	private String resolveImageToDataUri(String src) {
		if (src == null || src.isEmpty()) {
			return null;
		}
		if (src.startsWith("http://") || src.startsWith("https://") || src.startsWith("data:")) {
			return null; // keep original src
		}
		URL url = src.startsWith("/")
				? MarkdownHelpPane.class.getResource(src)
				: MarkdownHelpPane.class.getResource(currentBaseDir + src);
		if (url == null) {
			return null;
		}
		try (InputStream is = url.openStream()) {
			byte[] bytes = is.readAllBytes();
			return "data:" + guessMimeType(src) + ";base64," + Base64.getEncoder().encodeToString(bytes);
		} catch (IOException e) {
			return null;
		}
	}

	private static String guessMimeType(String name) {
		String n = name.toLowerCase();
		if (n.endsWith(".jpg") || n.endsWith(".jpeg")) return "image/jpeg";
		if (n.endsWith(".gif")) return "image/gif";
		if (n.endsWith(".svg")) return "image/svg+xml";
		if (n.endsWith(".bmp")) return "image/bmp";
		if (n.endsWith(".webp")) return "image/webp";
		return "image/png";
	}

	// -------------------------------------------------------------------------
	// Link handling – open external links in the system browser
	// -------------------------------------------------------------------------

	/**
	 * After each page load, attach a click listener to every {@code <a>} so that
	 * external links open in the system browser and other links do not blank the
	 * page (the WebView has no document base for relative navigation).
	 */
	private void installLinkHandler() {
		engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
			if (newState != State.SUCCEEDED) {
				return;
			}
			Document doc = engine.getDocument();
			if (doc == null) {
				return;
			}
			NodeList anchors = doc.getElementsByTagName("a");
			for (int i = 0; i < anchors.getLength(); i++) {
				if (anchors.item(i) instanceof EventTarget) {
					((EventTarget) anchors.item(i)).addEventListener("click", linkListener, false);
				}
			}
		});
	}

	private final EventListener linkListener = new EventListener() {
		@Override
		public void handleEvent(Event ev) {
			EventTarget target = ev.getCurrentTarget();
			if (!(target instanceof Element)) {
				return;
			}
			String href = ((Element) target).getAttribute("href");
			if (href == null || href.isEmpty() || href.startsWith("#")) {
				// allow normal in-page anchor scrolling
				return;
			}
			// Do not let the WebView navigate away from the help content.
			ev.preventDefault();
			if (href.startsWith("http://") || href.startsWith("https://") || href.startsWith("mailto:")) {
				openInBrowser(href);
			}
		}
	};

	/** Open a URL in the system default browser, off the JavaFX thread. */
	private static void openInBrowser(String url) {
		try {
			if (java.awt.Desktop.isDesktopSupported()
					&& java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
				new Thread(() -> {
					try {
						java.awt.Desktop.getDesktop().browse(new URI(url));
					} catch (Exception ignored) {
					}
				}, "help-open-browser").start();
			}
		} catch (Exception ignored) {
		}
	}

	// -------------------------------------------------------------------------
	// Private helpers
	// -------------------------------------------------------------------------

	/** Load a classpath resource as a UTF-8 string; returns {@code null} if not found. */
	private static String loadResource(String path) {
		try (InputStream is = MarkdownHelpPane.class.getResourceAsStream(path)) {
			if (is == null) return null;
			try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
				StringBuilder sb = new StringBuilder();
				char[] buf = new char[4096];
				int n;
				while ((n = reader.read(buf)) != -1) sb.append(buf, 0, n);
				return sb.toString();
			}
		} catch (IOException e) {
			return null;
		}
	}
}
