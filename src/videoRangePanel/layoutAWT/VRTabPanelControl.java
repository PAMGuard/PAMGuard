package videoRangePanel.layoutAWT;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import videoRangePanel.VRControl;
import videoRangePanel.VRPane;
import videoRangePanel.VRTabPane;
import PamView.hidingpanel.TabbedHidingPane;
import PamView.panel.CornerLayout;
import PamView.panel.CornerLayoutContraint;
import PamView.panel.PamBorderPanel;
import PamView.panel.PamPanel;

public class VRTabPanelControl extends VRTabPane {
	
	private VRControl vrControl;
	
	protected VRPanel vrPanel;
	
	public PamPanel imageRibbonPanel;
	
	private PamBorderPanel videoPanel;

	private VRSidePanel vrSidePanel;

	private PamPanel mainPanel;

	private TabbedHidingPane imageSidePanels;

	private VRImageEditPanel imageControls;

	private VRMetaDataPanel metaDataDisplay;

//	private HidingPanel iEhp;
//
//	public HidingPanel mDhp;

	JLayeredPane imageAndSidePanel; 
	

	public VRTabPanelControl(VRControl vrControl) {
		super();
		this.vrControl = vrControl;
		
		videoPanel = new PamBorderPanel();
		videoPanel.setLayout(new BorderLayout());
		
		vrPanel = new VRPanel(vrControl);
//		System.out.println("Current method: " + vrControl.getCurrentMethod().getName());
		imageRibbonPanel = vrControl.getCurrentMethod().getOverlayAWT().getRibbonPanel(); 
		vrSidePanel = new VRSidePanel(vrControl);
		
		imageSidePanels=new TabbedHidingPane();
		//create hiding panels
		imageControls=new VRImageEditPanel(vrControl, this);
		imageControls.setOpaque(false);
		metaDataDisplay=new VRMetaDataPanel(vrControl);
		//TODO- using border layout here is a bit messy but it works very well 
		metaDataDisplay.setPreferredSize(new Dimension(550,1));
		imageSidePanels.addTab("Image Metadata",new ImageIcon(ClassLoader
				.getSystemResource("Resources/SettingsButtonSmallWhite.png")),metaDataDisplay,false);
		imageSidePanels.addTab("Image Controls",new ImageIcon(ClassLoader
				.getSystemResource("Resources/SettingsButtonSmallWhite.png")),imageControls,false);
		imageSidePanels.setOpaque(false);
		imageSidePanels.revalidate();
		
		//need to use corner layout in order to have panels layered on top of each other
		CornerLayoutContraint c = new CornerLayoutContraint();
		imageAndSidePanel = new JLayeredPane();
//		imageAndSidePanel.setLayout(new CornerLayout(c));
		c.anchor = CornerLayoutContraint.EAST;
		imageAndSidePanel.add(imageSidePanels, c,JLayeredPane.POPUP_LAYER);
		c.anchor = CornerLayoutContraint.FILL;	
		imageAndSidePanel.add(vrPanel, c,JLayeredPane.FRAME_CONTENT_LAYER);
		imageAndSidePanel.setOpaque(false);

			
		videoPanel.add(BorderLayout.CENTER, imageAndSidePanel);
		videoPanel.add(BorderLayout.NORTH, imageRibbonPanel);
		
		mainPanel=new PamPanel(new BorderLayout());
		mainPanel.add(BorderLayout.CENTER,videoPanel);
		mainPanel.add(BorderLayout.WEST,vrSidePanel.getPanel());
		
		imageSidePanels.addTabChangeListener(new TabListener());

		
	}
	
	
	public void changeMethod() {
		//change the ribbon
		changeRibbonPanel(vrControl.getCurrentMethod().getOverlayAWT().getRibbonPanel()); 
		//change side panel
		vrSidePanel.update(VRControl.METHOD_CHANGED);
		//change the vrPanel 
		vrPanel.changeMethod();
		//need to validate and repaint everything together 
		mainPanel.validate();
		mainPanel.repaint();
		
	}
	
	class TabListener implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent arg0) {
//			System.out.println("Tab Listener Triggered!");
			imageSidePanels.invalidate();
			imageSidePanels.repaint();
			imageAndSidePanel.invalidate();
			imageAndSidePanel.validate();
			imageAndSidePanel.repaint();
		}
		
	}

	
	public void repaintSide(){
		imageAndSidePanel.repaint();
	}
	
	private void changeRibbonPanel(PamPanel panel){
		//change the ribbon panel. 
		videoPanel.remove(imageRibbonPanel);
		if (panel!=null) imageRibbonPanel= panel;
		else imageRibbonPanel=new PamPanel(new BorderLayout());
		videoPanel.add(BorderLayout.NORTH, imageRibbonPanel);
		//need to revalidate as ribbon bar size may have cahnged.
		imageRibbonPanel.validate();
		videoPanel.validate();
		videoPanel.repaint();
	}
	
	public boolean loadFile(File file) {
//		imageAndSidePanel.invalidate();
		boolean imageOk = vrPanel.loadImageFromFile(file);
//		imageAndSidePanel.invalidate();
		return imageOk;
	}
	
	protected void showHidePanels(boolean show){
//		iEhp.showPanel(show);
//		mDhp.showPanel(show);
	}
	
	public boolean pasteImage() {
		boolean imageOk =  vrPanel.pasteImage();
		return imageOk;
	}
	
	public JMenu createMenu(Frame parentFrame) {
		return null;
	}

	public JComponent getPanel() {
		return mainPanel;
	}

	public JToolBar getToolBar() {
		return null;
	}
	
	public void update(int updateType) {
		switch (updateType){
		case VRControl.SETTINGS_CHANGE:
			vrSidePanel.update(updateType);
			imageControls.update(updateType);
			break;
		case VRControl.IMAGE_CHANGE:
			vrPanel.sortScales();
			vrSidePanel.update(updateType);
			imageControls.update(updateType);
			if (vrControl.getCurrentImage()!=null) {
				metaDataDisplay.setMetaText(vrControl.getCurrentImage().getMetaDataText());
			}
			repaintSide();
			break;
		case VRControl.METHOD_CHANGED:
			changeMethod();
			vrPanel.repaint();
			break;
		case VRControl.REPAINT:
			this.repaintSide();
			break;
		}
	}



	public void showComponents() {
		imageRibbonPanel.setVisible(true);
	}
	
	public void enableControls() {
//		if (imageAnglePanel != null) 
//			imageAnglePanel.enableControls();
	}

	public VRSidePanel getVRSidePanel() {
		return vrSidePanel;
	}
	
	public VRMetaDataPanel getMetadataPanel(){
		return metaDataDisplay; 
	}


	public VRPanel getVRPanel() {
		return vrPanel;
	}
	
	@Override
	public boolean openImageFile(File file) {
		return vrPanel.loadImageFromFile(file);
	}
	
	/**
	 * New mouse point has occurred
	 * @param mousePoint the new mouse point. 
	 */
	@Override
	public void newMousePoint(Point mousePoint) {
		this.getVRSidePanel().newMousePoint(mousePoint);
	}
	


	@Override
	public VRPane getVRPane() {
		return this.vrPanel;
	}

}
