package clickTrainDetector.layout;

import java.awt.Frame;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import PamController.SettingsPane;
import PamView.PamControlledGUISwing;
import annotation.AnnotationSettingsDialog;
import annotation.localise.targetmotion.TMAnnotationType;
import clickTrainDetector.ClickTrainControl;
import clickTrainDetector.ClickTrainParams;
import clickTrainDetector.localisation.CTLocParams;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX2AWT;

/**
 * The PAMControlled unit GUI bits when using Swing. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class CTSwingGUI extends PamControlledGUISwing {
	
	
	private PamDialogFX2AWT<ClickTrainParams> settingsDialog;
	
	/**
	 * Reference to the click train control
	 */
	private ClickTrainControl clickTrainControl;

	/**
	 * The click settings pane. 
	 */
	private ClickTrainAlgorithmPaneFX settingsPane;

	/**
	 * The side panel 
	 */
	private CTSidePanelSwing ctSidePanel;
	
	/**
	 * 
	 * @param clickTrainControl
	 */
	public CTSwingGUI(ClickTrainControl clickTrainControl) {
		this.clickTrainControl=clickTrainControl; 
	}


	/***************Swing GUI******************/
	
	/**
	 * Show settings dialog. 
	 * @param parentFrame - the frame. 
	 */
	public void showSettingsDialog(Frame parentFrame) {
		showSettingsDialog(parentFrame, null);
	}


	/**
	 * Show settings dialog. 
	 * @param parentFrame - the frame. 
	 * @param classificationTab - true to open on classification tab, false to open on detection tab. Null to open on last tab.  
	 */
	public void showSettingsDialog(Frame parentFrame, Boolean classificationTab) {
		
		if (settingsPane==null || settingsDialog == null || parentFrame != settingsDialog.getOwner()) {
			SettingsPane<ClickTrainParams> setPane = (SettingsPane<ClickTrainParams>) getSettingsPane();
			setPane.setParams(this.clickTrainControl.getClickTrainParams());
			settingsDialog = new PamDialogFX2AWT<ClickTrainParams>(parentFrame, setPane, false);
		}
		
		if (classificationTab!=null && classificationTab) settingsPane.setTab(2); //set the tab to the classification tab
		
		ClickTrainParams newParams = settingsDialog.showDialog(this.clickTrainControl.getClickTrainParams());
		
		clickTrainControl.updateParams(newParams); 
		
		ctSidePanel.update(clickTrainControl.getCurrentCTAlgorithm()); 
	}
	

	
	@Override
	public CTSidePanelSwing getSidePanel() {
		if (super.getSidePanel()==null) {
			this.setSidePanel(ctSidePanel = new CTSidePanelSwing(clickTrainControl));
		}
		return (CTSidePanelSwing) super.getSidePanel();
	}

	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenu submenu = new JMenu(clickTrainControl.getUnitName());

		JMenuItem menuItem = new JMenuItem("Detection Settings ..."); 

		menuItem.addActionListener((action)->{
			showSettingsDialog(parentFrame, null); 
		});
		submenu.add(menuItem);
		
		menuItem = new JMenuItem("Localisation Settings ..."); 
		menuItem.addActionListener((action)->{
			showLocDialog(parentFrame, null); 
		});
		submenu.add(menuItem);

		if (clickTrainControl.isViewer()) {
			menuItem = new JMenuItem("Reanalyse Click Trains ..."); 
			menuItem.addActionListener((action)->{
				clickTrainControl.getClickTrainsOffline().showOfflineDialog(parentFrame);
			});
		}
		submenu.add(menuItem);

		return submenu; 
	}
	
	/**
	 * Show the localisation dialog. 
	 * @param parentFrame - the parent frame. 
	 * @param object - data object to optionally pass on. Can be null. 
	 */
	private void showLocDialog(Frame parentFrame, Object object) {
//		AnnotationSettingsPanel settingsPanel = cbLocaliser.getTmAnnotationType().getSettingsPanel();
		boolean asd = AnnotationSettingsDialog.showDialog(parentFrame, this.getTmAnnotationType());
		if (asd) {
			//set in the main algorithm params. 
			this.clickTrainControl.getClickTrainParams().ctLocParams =
					(CTLocParams) this.getTmAnnotationType().getAnnotationOptions(); 
		}
		else {
			return;
		}
		
	}


	/**
	 * Get the target motion localisation type. This handles the target motion loclaisaiton for CT data units. 
	 * @return the taerget motion type. 
	 */
	private TMAnnotationType getTmAnnotationType() {
		return this.clickTrainControl.getCTLocalisationProccess().getTmAnnotationType();
	}


	/**
	 * Get the settings pane. 
	 * @return the settings pane. 
	 */
	public ClickTrainAlgorithmPaneFX getSettingsPane(){
		if (this.settingsPane==null){
			settingsPane= new ClickTrainAlgorithmPaneFX(clickTrainControl); 
		}
		return settingsPane; 
	}
	
	/**
	 * Allows the GUI to be notified of changes, e.g. in the PAMControlle.r 
	 * @param flag - the change flag. 
	 */
	public void notifyGUIChange(int flag) {
		getSidePanel().update(clickTrainControl.getCurrentCTAlgorithm()); 
	}

}
