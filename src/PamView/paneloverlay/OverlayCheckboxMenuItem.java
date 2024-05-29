package PamView.paneloverlay;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;

import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.swing.FontIcon;

import PamView.component.PamSettingsIconButton;
import PamView.symbol.PamSymbolManager;
import PamguardMVC.PamDataBlock;
import PamguardMVC.dataSelector.DataSelector;

/**
 * Special checkbox for overlay drawing. Will generate a menu item which 
 * either has a normal check box, or if the associated data has either a 
 * dataselector or managed symbol the checkbox will be replaced 
 * with a setting symbol, greyed if the data are not curretnly selected for display.  
 * @author dg50
 *
 */
public class OverlayCheckboxMenuItem  extends JCheckBoxMenuItem {


//	public static final ImageIcon settingsIcon = new ImageIcon(ClassLoader.getSystemResource("Resources/SettingsButtonSmall2.png"));
//	public static final ImageIcon settingsIconNot = new ImageIcon(ClassLoader.getSystemResource("Resources/SettingsButtonSmallWhite.png"));
	
	private static final FontIcon settingsIcon =  FontIcon.of(PamSettingsIconButton.SETTINGS_IKON, PamSettingsIconButton.SMALL_SIZE, Color.DARK_GRAY);
	private static final FontIcon settingsIconNot =  FontIcon.of(PamSettingsIconButton.SETTINGS_IKON, PamSettingsIconButton.SMALL_SIZE, Color.WHITE);

	
	private static final long serialVersionUID = 1L;

	private Point locOnScreen; // keep this - it's needed later !
	private PamDataBlock dataBlock;
	private String displayName;
	private DataSelector dataSelector;
	private PamSymbolManager symbolManager;

	public OverlayCheckboxMenuItem(PamDataBlock dataBlock, String displayName, boolean selected, boolean includeSymbolManagement) {
		super(dataBlock.getLongDataName());
		this.dataBlock = dataBlock;
		this.displayName = displayName;
		dataSelector = dataBlock.getDataSelector(displayName, true);
		if (includeSymbolManagement) {
			symbolManager = dataBlock.getPamSymbolManager();
		}		
		if (symbolManager != null || dataSelector != null) {
			if (selected) {
				this.setIcon(settingsIcon);
			}
			else {
				this.setIcon(settingsIconNot);
			}
			addMouseListener(new PlotMenuMouse());
		}
		this.setSelected(selected);
		this.setToolTipText(dataBlock.getLongDataName());
	}

	private String getToolTip() {
		String unitName = dataBlock.getParentProcess().getPamControlledUnit().getUnitName();
		if (symbolManager != null && dataSelector != null) {
			return unitName + " Data selection and symbol options";
		}
		else if (symbolManager != null) {
			return unitName + " Plot symbol options";
		}
		else if (dataSelector != null) {
			return unitName + " Data selection options";
		}
		else {
			return unitName + " Click to plot data";
		}
	}

//	/**
//	 * @param text
//	 * @param icon
//	 */
//	public OverlayCheckboxMenuItem(String text, Icon icon) {
//		super(text, icon);
//		addMouseListener(new PlotMenuMouse());
//		setToolTipText(" ");
//	}
//
//	/**
//	 * @param text
//	 */
//	public OverlayCheckboxMenuItem(String text) {
//		super(text);
//	}


	class PlotMenuMouse extends MouseAdapter {

		@Override
		public void mouseEntered(MouseEvent e) {
			/*
			 * We need this or the popup box can't work out where 
			 */
			locOnScreen = getLocationOnScreen();
			//				System.out.printf("mouseEntered %s = %d,%d\n", getText(), locOnScreen.x, locOnScreen.y);
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#getToolTipText(java.awt.event.MouseEvent)
	 */
	@Override
	public String getToolTipText(MouseEvent event) {
		boolean hasIcon = (getIcon() != null);
		if (hasIcon && event.getX() < getIcon().getIconWidth()) {
			return getToolTip();
		}
		return super.getToolTipText();
	}

	/**
	 * @return the locOnScreen
	 */
	public Point getLocOnScreen() {
		return locOnScreen;
	}


}

