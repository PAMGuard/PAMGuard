package PamView.hidingpanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignP;
import org.kordamp.ikonli.swing.FontIcon;

import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamView.component.PamSettingsIconButton;
import PamView.dialog.PamButtonAlpha;
import PamView.dialog.PamDialog;
import PamView.panel.CornerLayoutContraint;
import PamView.panel.PamPanel;

public class HidingDialog extends PamDialog {

	private HidingDialogPanel hidingDialogPanel;

	JCheckBox pinButton;
	
//	private ImageIcon settings=new ImageIcon(ClassLoader
//			.getSystemResource("Resources/SettingsButtonSmallWhite.png"));
//	
//	private ImageIcon pinImage = new ImageIcon(ClassLoader
//			.getSystemResource("Resources/pinbuttonwhite.png"));
//	
//	private ImageIcon pinHide = new ImageIcon(ClassLoader
//			.getSystemResource("Resources/deletewhite.png"));
	
	private static final FontIcon settings =  FontIcon.of(PamSettingsIconButton.SETTINGS_IKON, PamSettingsIconButton.SMALL_SIZE, Color.WHITE);
	private static final FontIcon pinImage =  FontIcon.of(MaterialDesignP.PIN, PamSettingsIconButton.SMALL_SIZE, Color.WHITE);
	private static final FontIcon pinHide =  FontIcon.of(MaterialDesignP.PIN_OFF, PamSettingsIconButton.SMALL_SIZE, Color.WHITE);


	public HidingDialog(Window parentFrame, HidingDialogPanel hidingDialogPanel, String title, boolean hasDefault) {
		super(parentFrame, title, hasDefault);
		this.hidingDialogPanel = hidingDialogPanel;
		setUndecorated(true); // this just needs calling before anything is added. 
		//set the opacity
		setOpacity(hidingDialogPanel.getOpacity());
		getButtonPanel().setVisible(false);
		JPanel borderPanel = new JPanel(new BorderLayout());
		//		borderPanel.setBackground(PamColors.getInstance().getColor(PamColor.BACKGROUND_ALPHA));
		borderPanel.setOpaque(hidingDialogPanel.getOpacity() >= 1);
		borderPanel.add(BorderLayout.CENTER, hidingDialogPanel.getHidingDialogComponent().getComponent());
		setDialogComponent(borderPanel);
		// and add a pin button somewhere depending on the layout. 
		pinButton = new JCheckBox(pinImage);
		pinButton.setOpaque(false);
		pinButton.addActionListener(new PinButtonListener());
		pinButton.setToolTipText("Pin hiding dialog");
		
		//Create a panel to hold the pin button and potentialy a settings (more) button
		PamPanel pinPanel = new PamPanel();
		pinPanel.setLayout(new BoxLayout(pinPanel, BoxLayout.Y_AXIS));
		pinPanel.setOpaque(hidingDialogPanel.getOpacity() >= 1);
		pinButton.setAlignmentY(TOP_ALIGNMENT);
		pinPanel.add(pinButton);
		
		
		// check for more options. 
		if (hidingDialogPanel.getHidingDialogComponent().hasMore()) {
			JButton moreButton = new PamButtonAlpha(settings);
			moreButton.setBackground(new Color(0,0,0,0));
			//reduce the size og the more button to that of the pin button. 
			moreButton.setPreferredSize(pinButton.getSize());
			pinButton.setAlignmentY(BOTTOM_ALIGNMENT);
			moreButton.addActionListener(new MoreButton());
			moreButton.setToolTipText("More options");
			pinPanel.add(moreButton);
		}
		
		switch (hidingDialogPanel.getStartLocation()) {
		case CornerLayoutContraint.FIRST_LINE_END:
			borderPanel.add(BorderLayout.WEST, pinPanel);
			break;
		case CornerLayoutContraint.FIRST_LINE_START:
			borderPanel.add(BorderLayout.EAST, pinPanel);
			break;
		case CornerLayoutContraint.LAST_LINE_END:
			borderPanel.add(BorderLayout.WEST, pinPanel);
			break;
		case CornerLayoutContraint.LAST_LINE_START:
			borderPanel.add(BorderLayout.EAST, pinPanel);
			break;
		case CornerLayoutContraint.NORTH:
			pinPanel.add(BorderLayout.SOUTH, pinButton);
			borderPanel.add(BorderLayout.WEST, pinPanel);
			break;
		default:
			pinPanel.add(BorderLayout.EAST, pinButton);
			borderPanel.add(BorderLayout.EAST, pinPanel);
			break;
		}

		setComponentOpaqueness(borderPanel, hidingDialogPanel.getOpacity() >= 1);
		setModalityType(Dialog.ModalityType.MODELESS);
		setModal(hidingDialogPanel.isModality());
		setAlwaysOnTop(false);
//		invalidate();
	}
	
	

	public void setComponentOpaqueness(JComponent component, boolean opaque) {
		// set all but final top level components to given opaqueness. 
//		if (component.get)
		if (component == null) return;
		int nSub = component.getComponentCount();
		if (nSub == 0) {
			return;
		}
		component.setOpaque(opaque);
		for (int i = 0; i < nSub; i++) {
			if (JComponent.class.isAssignableFrom(component.getComponent(i).getClass())) {
				setComponentOpaqueness((JComponent) component.getComponent(i), opaque);
			}
		}
	}

	private class MoreButton implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			hidingDialogPanel.getHidingDialogComponent().showMore(HidingDialog.this);
		}
	}
	
	@Override
	public void setOpacity(float opacity){
		setBackground(getOpacityBackground(opacity));
	}
	
	/**
	 * Gets a default dark grey background colour with the alpha proportional to the opacity. 
	 * @param opacity
	 * @return
	 */
	public static Color getOpacityBackground(float opacity){
		Color background=PamColors.getInstance().getColor(PamColor.BACKGROUND_ALPHA);
		float[] colours=background.getColorComponents(null);
		background=new Color(colours[0],colours[1],colours[2],opacity);
		return background; 
	};
	
	private class PinButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (pinButton.isSelected()) {
				pinButton.setIcon(pinHide);
				pinButton.setToolTipText("Hide dialog");
			}
			else {
				pinButton.setIcon(pinImage);
				hidingDialogPanel.showHidingDialog(false);
				pinButton.setToolTipText("Pin hiding dialog");
			}
		}

	}

	@Override
	public boolean getParams() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub

	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

	public void repackDialog() {
		pack();
		invalidateEverything(this);
		pack();
//		hidingDialogPanel.s
	}

	/**
	 * Work through tree of subcomponents, invalidating everything
	 * to try to solve layout problem. 
	 * @param hidingDialog
	 */
	public static void invalidateEverything(Component component) {
		component.invalidate();
		if (Container.class.isAssignableFrom(component.getClass())) {
			Container container = (Container) component;
			for (int i = 0; i < container.getComponentCount(); i++) {
				invalidateEverything(container.getComponent(i));
			}
		}
	}



	/* (non-Javadoc)
	 * @see PamView.PamDialog#setVisible(boolean)
	 */
	@Override
	public synchronized void setVisible(boolean visible) {
		// putting this here, wrecks teh vertical size of
		// the dialog and still doesn't fix the scale 
		// problem on the Spectrogram
//		repackDialog();
		super.setVisible(visible);
	}

}
