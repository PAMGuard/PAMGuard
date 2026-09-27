package PamController.pamWizard.configurations;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * Supplies the species icons shown in the import wizard.
 * <p>
 * Icons are per {@link ConfigSpeciesGroup}, so a configuration which targets a
 * specific species (e.g. North Atlantic right whale) is represented by the icon
 * of the group it belongs to. The wizard shows every group at once and picks out
 * the ones the selected configuration targets, so there is no per configuration
 * artwork.
 * <p>
 * The images live in {@value ConfigSpeciesGroup#ICON_FOLDER} within the packaged
 * resources: plain black silhouettes, trimmed to the animal and all stored at the
 * same width. They are sized by <i>width</i> rather than fitted into a square, so
 * that a long flat animal such as a whale is drawn as large as a compact one such
 * as a bat rather than shrinking to a sliver in the middle of an empty box.
 * Replacing them with better artwork needs no code change. If an image is missing
 * the caller should fall back to the group's Ikonli glyph
 * ({@link ConfigSpeciesGroup#getGlyphName()}).
 * <p>
 * Both Swing and JavaFX are supported, since the wizard runs under either GUI.
 * The two sets of methods are kept separate, and the JavaFX ones are only
 * referenced from JavaFX code, so that a Swing-only run never needs the JavaFX
 * image classes.
 *
 * @author Jamie Macaulay
 */
public class SpeciesIconFactory {

	private static SpeciesIconFactory singleInstance;

	/**
	 * Cache of Swing icons, keyed on the resource path plus size and colour.
	 */
	private final Map<String, ImageIcon> swingIcons = new HashMap<>();

	/**
	 * Cache of JavaFX images, keyed on the resource path plus colour. Held as Object
	 * so that this class does not force the JavaFX image classes to load in a
	 * Swing-only run.
	 */
	private final Map<String, Object> fxImages = new HashMap<>();

	private SpeciesIconFactory() {
	}

	/**
	 * Get the single instance of the icon factory.
	 * @return the icon factory.
	 */
	public static synchronized SpeciesIconFactory getInstance() {
		if (singleInstance == null) {
			singleInstance = new SpeciesIconFactory();
		}
		return singleInstance;
	}

	/**
	 * Get a Swing icon for a species group, recoloured to the given colour.
	 * <p>
	 * The stored images are flat silhouettes, so only their transparency carries any
	 * shape. Recolouring therefore just means painting the wanted colour through the
	 * image's alpha channel, which lets one image serve both the highlighted and the
	 * greyed out state rather than needing two sets of artwork.
	 *
	 * @param group the species group.
	 * @param width the required width in pixels; the height follows the artwork.
	 * @param tint  the colour to paint the silhouette.
	 * @return the recoloured icon, or null if the image resource is missing.
	 */
	public ImageIcon getSwingIcon(ConfigSpeciesGroup group, int width, Color tint) {
		if (group == null) {
			group = ConfigSpeciesGroup.OTHER;
		}
		String key = group.getIconResource() + "@" + width + "#" + tint.getRGB();
		if (swingIcons.containsKey(key)) {
			return swingIcons.get(key);
		}
		ImageIcon plain = getSwingIcon(group, width);
		ImageIcon tinted = null;
		if (plain != null) {
			int w = plain.getIconWidth();
			int h = plain.getIconHeight();
			BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = image.createGraphics();
			g.drawImage(plain.getImage(), 0, 0, null);
			// paint the colour over the silhouette, keeping the existing alpha.
			g.setComposite(AlphaComposite.SrcAtop);
			g.setColor(tint);
			g.fillRect(0, 0, w, h);
			g.dispose();
			tinted = new ImageIcon(image);
		}
		swingIcons.put(key, tinted);
		return tinted;
	}

	/**
	 * Get a Swing icon for a species group, scaled to the given width.
	 *
	 * @param group the species group.
	 * @param width the required width in pixels; the height follows the artwork.
	 * @return the icon, or null if the image resource is missing.
	 */
	public ImageIcon getSwingIcon(ConfigSpeciesGroup group, int width) {
		if (group == null) {
			group = ConfigSpeciesGroup.OTHER;
		}
		return scale(loadSwingFromResource(group.getIconResource()), width);
	}

	/**
	 * Get a JavaFX image for a species group, recoloured to the given colour.
	 *
	 * @param group the species group.
	 * @param tint  the colour to paint the silhouette.
	 * @return a recoloured {@code javafx.scene.image.Image}, or null if the resource
	 *         is missing.
	 * @see #getSwingIcon(ConfigSpeciesGroup, int, Color)
	 */
	public javafx.scene.image.Image getFXImage(ConfigSpeciesGroup group, javafx.scene.paint.Color tint) {
		if (group == null) {
			group = ConfigSpeciesGroup.OTHER;
		}
		String key = group.getIconResource() + "#" + tint.toString();
		if (fxImages.containsKey(key)) {
			return (javafx.scene.image.Image) fxImages.get(key);
		}
		javafx.scene.image.Image plain = getFXImage(group);
		javafx.scene.image.WritableImage tinted = null;
		if (plain != null) {
			int w = (int) plain.getWidth();
			int h = (int) plain.getHeight();
			tinted = new javafx.scene.image.WritableImage(w, h);
			javafx.scene.image.PixelReader reader = plain.getPixelReader();
			javafx.scene.image.PixelWriter writer = tinted.getPixelWriter();
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					double alpha = reader.getColor(x, y).getOpacity();
					writer.setColor(x, y, new javafx.scene.paint.Color(
							tint.getRed(), tint.getGreen(), tint.getBlue(), alpha * tint.getOpacity()));
				}
			}
		}
		fxImages.put(key, tinted);
		return tinted;
	}

	/**
	 * Get a JavaFX image for a species group.
	 *
	 * @param group the species group.
	 * @return a {@code javafx.scene.image.Image}, or null if the resource is missing.
	 */
	public javafx.scene.image.Image getFXImage(ConfigSpeciesGroup group) {
		if (group == null) {
			group = ConfigSpeciesGroup.OTHER;
		}
		return loadFXFromResource(group.getIconResource());
	}

	/**
	 * Load a Swing icon from a packaged resource, caching the result.
	 */
	private ImageIcon loadSwingFromResource(String resourcePath) {
		if (swingIcons.containsKey(resourcePath)) {
			return swingIcons.get(resourcePath);
		}
		ImageIcon icon = null;
		try (InputStream stream = getClass().getResourceAsStream(resourcePath)) {
			if (stream != null) {
				Image image = ImageIO.read(stream);
				if (image != null) {
					icon = new ImageIcon(image);
				}
			}
		}
		catch (Exception e) {
			System.out.println("Unable to load species icon " + resourcePath + ": " + e.getMessage());
		}
		if (icon == null) {
			System.out.println("Species icon resource not found: " + resourcePath);
		}
		swingIcons.put(resourcePath, icon);
		return icon;
	}

	/**
	 * Load a JavaFX image from a packaged resource, caching the result.
	 */
	private javafx.scene.image.Image loadFXFromResource(String resourcePath) {
		if (fxImages.containsKey(resourcePath)) {
			return (javafx.scene.image.Image) fxImages.get(resourcePath);
		}
		javafx.scene.image.Image image = null;
		try (InputStream stream = getClass().getResourceAsStream(resourcePath)) {
			if (stream != null) {
				image = new javafx.scene.image.Image(stream);
			}
		}
		catch (Exception e) {
			System.out.println("Unable to load species icon " + resourcePath + ": " + e.getMessage());
		}
		if (image == null) {
			System.out.println("Species icon resource not found: " + resourcePath);
		}
		fxImages.put(resourcePath, image);
		return image;
	}

	/**
	 * Scale an icon to the given width, keeping its proportions and leaving the
	 * cached original untouched.
	 *
	 * @param icon  the icon to scale, may be null.
	 * @param width the required width in pixels.
	 * @return the scaled icon, or null if the input was null.
	 */
	private ImageIcon scale(ImageIcon icon, int width) {
		if (icon == null || icon.getIconWidth() <= 0) {
			return null;
		}
		if (icon.getIconWidth() == width) {
			return icon;
		}
		int height = Math.max(1, Math.round(icon.getIconHeight() * width / (float) icon.getIconWidth()));
		return new ImageIcon(icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
	}
}
