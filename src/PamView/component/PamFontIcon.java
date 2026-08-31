package PamView.component;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.UIManager;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.swing.FontIcon;

import PamView.ColourScheme;
import PamView.PamColors;
import PamView.PamColors.PamColor;

/**
 * An Ikonli {@link FontIcon} which follows the current colour scheme.
 * <p>
 * A plain FontIcon is given its colour once, when it's created, and most of
 * PAMGuard's icons are held in static fields, so they were being coloured at
 * class load time and then stayed that colour for the rest of the session. That
 * doesn't matter for a light look and feel, but a dark grey cog is invisible on
 * a dark grey button, which is why the Swing icons all stayed black when the
 * scheme was switched to Dark or Night while the JavaFX ones (which take their
 * colour from CSS) changed.
 * <p>
 * Rather than track every icon which has ever been created and repaint it on a
 * scheme change, this resolves the colour in {@link #paintIcon}, so an icon is
 * always drawn in the colour which goes with whatever is on screen at that
 * moment. It also means a single static icon instance can be shared between
 * components with different foregrounds and still look right in each.
 * <p>
 * Icons which are deliberately a fixed colour - the red record button, the amber
 * warning triangle - should carry on using {@link FontIcon} directly.
 *
 * @author Jamie Macaulay
 */
public class PamFontIcon extends FontIcon {

	/**
	 * Where an icon takes its colour from.
	 */
	public enum IconColour {
		/**
		 * The foreground of the component the icon is painted on, i.e. the same colour
		 * as the text next to it. This is what nearly every icon wants.
		 */
		FOREGROUND,
		/**
		 * Greyed out, to match the look and feel's disabled text. For
		 * {@link javax.swing.AbstractButton#setDisabledIcon(javax.swing.Icon)}.
		 */
		DISABLED,
		/**
		 * The background of the component the icon is painted on, so that the icon is
		 * effectively invisible. A couple of menus use an icon of the background colour
		 * to hold the space of one which isn't currently applicable.
		 */
		BACKGROUND,
		/**
		 * A light colour, for icons painted on the translucent dark panels
		 * ({@link PamColor#BACKGROUND_ALPHA}) which overlay the maps and plots. Those
		 * panels are dark in every scheme, so the icon stays light in day mode too.
		 */
		ALPHA_PANEL,
		/**
		 * The scheme's accent colour, for the few icons which are deliberately picked
		 * out from the ones around them - the paging buttons on the scrollers, which
		 * have always been blue to set them apart from the plain menu triangle beside
		 * them.
		 */
		ACCENT
	}

	/**
	 * Accent for the light schemes. This is the blue the scroller paging buttons
	 * have always been drawn in.
	 */
	private static final Color DAY_ACCENT = Color.BLUE;

	/**
	 * Accent for the dark scheme. Plain blue is all but invisible against a dark
	 * button - it comes out at a contrast ratio of 1.06:1 against FlatLaf dark's
	 * #4E5052 - so the dark scheme uses a much lighter blue, which manages 3.6:1.
	 */
	private static final Color DARK_ACCENT = new Color(120, 175, 245);

	/**
	 * Accent for the night scheme, which is a brighter red rather than a blue: the
	 * whole point of the night scheme is to keep to reds, and a blue bright enough
	 * to be seen would undo that. It is light enough to stand out from the ordinary
	 * red foreground next to it.
	 */
	private static final Color NIGHT_ACCENT = new Color(255, 110, 110);

	/**
	 * Size for the small show / hide chevrons on the hiding panels. The images
	 * these replaced were 11 x 8 pixels, so this keeps the hiding strips much the
	 * same thickness as before.
	 */
	public static final int ARROW_SIZE = 14;

	/**
	 * Size for the icons on the main menu bar items. Matches FlatLaf's
	 * MenuItem.minimumIconSize, so the icons sit in the space the look and feel
	 * already reserves for a check mark and the menu text stays where it was.
	 */
	public static final int MENU_SIZE = 16;

	private final IconColour iconColour;

	private PamFontIcon(Ikon ikon, int iconSize, IconColour iconColour) {
		this.iconColour = iconColour;
		setIkon(ikon);
		setIconSize(iconSize);
	}

	/**
	 * Create an icon which takes the foreground colour of whatever it's painted on.
	 *
	 * @param ikon     the icon, e.g. MaterialDesignC.COG
	 * @param iconSize size in pixels
	 * @return a colour scheme aware icon.
	 */
	public static PamFontIcon of(Ikon ikon, int iconSize) {
		return new PamFontIcon(ikon, iconSize, IconColour.FOREGROUND);
	}

	/**
	 * Create an icon which takes its colour from the current colour scheme.
	 *
	 * @param ikon       the icon, e.g. MaterialDesignC.COG
	 * @param iconSize   size in pixels
	 * @param iconColour where the icon should take its colour from
	 * @return a colour scheme aware icon.
	 */
	public static PamFontIcon of(Ikon ikon, int iconSize, IconColour iconColour) {
		return new PamFontIcon(ikon, iconSize, iconColour);
	}

	/**
	 * Set the colour for this paint and hand over to Ikonli.
	 * <p>
	 * Note that this makes any call to {@link #setIconColor(Color)} pointless - the
	 * colour set here is used instead.
	 */
	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		Color colour = resolveColour(c);
		if (colour != null) {
			setIconColor(colour);
		}
		super.paintIcon(c, g, x, y);
	}

	/**
	 * Work out what colour to draw in, given the component about to be painted.
	 *
	 * @param c the component the icon is being painted on. May be null, e.g. when
	 *          the icon is being rendered to an image.
	 * @return the colour to draw in, or null to leave the current one alone.
	 */
	private Color resolveColour(Component c) {
		switch (iconColour) {
		case DISABLED:
			return disabledColour();
		case BACKGROUND:
			Color background = c == null ? null : c.getBackground();
			return background != null ? background : uiColour("Panel.background", Color.WHITE);
		case ALPHA_PANEL:
			return alphaPanelColour();
		case ACCENT:
			return accentColour();
		case FOREGROUND:
		default:
			Color foreground = c == null ? null : c.getForeground();
			return foreground != null ? foreground : uiColour("Label.foreground", Color.DARK_GRAY);
		}
	}

	/**
	 * The look and feel's disabled text colour. Both FlatLaf and the Windows look
	 * and feel define these, but fall back to a grey rather than risk a null.
	 *
	 * @return colour for a disabled icon.
	 */
	private static Color disabledColour() {
		Color colour = UIManager.getColor("Label.disabledForeground");
		if (colour == null) {
			colour = UIManager.getColor("Button.disabledText");
		}
		return colour != null ? colour : Color.LIGHT_GRAY;
	}

	/**
	 * Colour for icons sitting on the translucent dark overlay panels. White
	 * everywhere except the night scheme, which uses its red foreground so that a
	 * bright white icon can't spoil anybody's night vision.
	 *
	 * @return colour for an icon on a BACKGROUND_ALPHA panel.
	 */
	private static Color alphaPanelColour() {
		ColourScheme scheme = currentScheme();
		if (scheme != null && ColourScheme.NIGHTSCHEME.equalsIgnoreCase(scheme.getName())) {
			return PamColors.getInstance().getColor(PamColor.AXIS);
		}
		return Color.WHITE;
	}

	/**
	 * The accent colour for the scheme currently in force.
	 *
	 * @return colour for an ACCENT icon.
	 */
	private static Color accentColour() {
		ColourScheme scheme = currentScheme();
		if (scheme != null) {
			if (ColourScheme.NIGHTSCHEME.equalsIgnoreCase(scheme.getName())) {
				return NIGHT_ACCENT;
			}
			if (ColourScheme.DARKSCHEME.equalsIgnoreCase(scheme.getName())) {
				return DARK_ACCENT;
			}
		}
		return DAY_ACCENT;
	}

	/**
	 * @return the colour scheme currently in force, or null if it can't be had.
	 */
	private static ColourScheme currentScheme() {
		try {
			return PamColors.getInstance().getColourScheme();
		}
		catch (Exception e) {
			// colours aren't worth throwing out of a paint method for
			return null;
		}
	}

	private static Color uiColour(String key, Color defaultColour) {
		Color colour = UIManager.getColor(key);
		return colour != null ? colour : defaultColour;
	}

}
