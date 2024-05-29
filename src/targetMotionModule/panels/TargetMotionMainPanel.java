package targetMotionModule.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.swing.FontIcon;

import targetMotionModule.TargetMotionControl;
import targetMotionModule.TargetMotionInformation;
import targetMotionModule.TargetMotionLocaliser;
import targetMotionModule.TargetMotionLocaliserProvider;
import targetMotionModule.TargetMotionResult;
import targetMotionModule.algorithms.TargetMotionModel;


import PamDetection.PamDetection;
import PamView.ColorManaged;
import PamView.PamColors.PamColor;
import PamView.component.PamSettingsIconButton;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamPanel;
import PamguardMVC.PamDataUnit;
import PamView.PamLoadBar;
import PamView.PamTabPanel;

public class TargetMotionMainPanel<T extends PamDataUnit> implements PamTabPanel, ColorManaged {
	
	private TargetMotionLocaliser targetMotionLocaliser;
	private TargetMotionControl targetMotionContol; 
	private ModelControlPanel modelControlPanel;
	private ModelResultPanel modelResultPanel;
	private MapPanel<T> dialogMap2D, dialogMap3D, currentMap;
	TMDialogComponent[] dialogComponents = new TMDialogComponent[5];
	private PamPanel mapBorder;
	private PamPanel mainPanel;
	private PamPanel northPanel;
	/**
	 * Load bar which shows progress of data load 
	 */
	private PamLoadBar loadBar;
	/*
	 *Indicate whether the loadbar is showing or not;
	 */
	private  boolean isShowingloadBar=false; 

	
	public final static int  northPanelHeight=150;
	
	//an arrayList containing all the control panels; 
	private ArrayList<AbstractControlPanel> controlPanels;
	AbstractControlPanel currentControlPanel; 
	//localiser control
	public JTextField currentLocalisation;
	private JButton runButton, runAllButton, save, saveAll, setNull, stop;
	public JTextField comment;
	private PamPanel southPanel;
	
//	public ImageIcon settings = new ImageIcon(ClassLoader
//			.getSystemResource("Resources/SettingsButtonSmall2.png"));
	public static FontIcon settings =  FontIcon.of(PamSettingsIconButton.SETTINGS_IKON, PamSettingsIconButton.NORMAL_SIZE, Color.DARK_GRAY);


	public TargetMotionMainPanel(TargetMotionLocaliser<T> targetMotionLocaliser) {

		this.targetMotionLocaliser = targetMotionLocaliser;
		this.targetMotionContol=targetMotionLocaliser.getTargetMotionControl();
		
		//create the localiser control buttons
		createLocaliserControls();
		
		modelControlPanel = new ModelControlPanel(targetMotionLocaliser, this);
		modelResultPanel = new ModelResultPanel(targetMotionLocaliser, this);
	
		//create a list of control panels
		updateControlPanelList();
		currentControlPanel=controlPanels.get(0);
		

		
		northPanel=new PamPanel(new BorderLayout());
		northPanel.add(BorderLayout.CENTER, currentControlPanel.getMainPanel());
		northPanel.add(BorderLayout.WEST, createLocaliserControls());

		southPanel=new PamPanel(new BorderLayout());
		southPanel.add(BorderLayout.CENTER, modelResultPanel.getPanel());
		southPanel.add(BorderLayout.WEST,createSavePanel() );
//		southPanel.add(BorderLayout.EAST,createCommentPanel());
		
		
		dialogComponents[0] = modelControlPanel;
		dialogComponents[1] = modelResultPanel;
		dialogComponents[2]= dialogMap2D;
		dialogComponents[3]= dialogMap3D;


		mainPanel = new PamPanel(new BorderLayout());
		mainPanel.add(BorderLayout.NORTH, northPanel);
		mainPanel.add(BorderLayout.WEST, modelControlPanel.getPanel());
		mainPanel.add(BorderLayout.SOUTH, southPanel);
		

		loadBar=new PamLoadBar();

	
		
		enableControls();
	}
	
	
	/**
	 * Create the maps. This needs to be done after pamguard has initialised because of 3D components. 
	 */
	public void createMaps(){
		
		dialogMap2D = new MapPanel2D<T>(targetMotionLocaliser, this);
//		dialogMap3D = new MapPanel3D<T>(targetMotionLocaliser, this);
	
		currentMap=dialogMap3D;
		
		mapBorder = new PamPanel(new BorderLayout());
		setMap(dialogMap3D);
		setMap(dialogMap2D);
		PamPanel mapCtrl = new PamPanel();
		mapCtrl.setLayout(new FlowLayout());
		JRadioButton b;
		ButtonGroup bg = new ButtonGroup();
		mapCtrl.add(b = new JRadioButton("2D Map"));
		b.addActionListener(new MapSelAction(dialogMap2D));
		bg.add(b);
		mapCtrl.add(b = new JRadioButton("3D Map"));
		b.setSelected(true);
		b.addActionListener(new MapSelAction(dialogMap3D));
		bg.add(b);
		mapBorder.add(BorderLayout.SOUTH, mapCtrl);
		
		setMap(dialogMap3D);
		mainPanel.add(BorderLayout.CENTER, mapBorder);

	}
	
	public MapPanel getMap3D(){
		return dialogMap3D;
	}
	
	public MapPanel getMap2D(){
		return dialogMap2D;
	}
	
	
	
	/*
	 *Show the load bar.  
	 */
	public void showLoadBar(boolean show){
		
		this.isShowingloadBar=show; 
		
		if (show==true){
			mapBorder.add(BorderLayout.NORTH,loadBar.getPanel());
		}
		else{
			mapBorder.remove(loadBar.getPanel());
		}
		mapBorder.revalidate();
		mapBorder.validate();

	}
	

	/**
	 * Get the current control panels. 
	 */
	public void updateControlPanelList(){
		controlPanels=new ArrayList<AbstractControlPanel>();
		for (int i=0; i<targetMotionContol.getDataBlocks().size(); i++){
			controlPanels.add(((TargetMotionLocaliserProvider) targetMotionContol.getDataBlocks().get(i)).getTMControlPanel(targetMotionContol));
		}
	}
	
	public void changeControlPanel(){
		
	}
	
	/**
	 * Create the buttons which allow users to start locaisation algorithms, stop, batch process etc. 
	 * @return
	 */
	private PamPanel createLocaliserControls(){
	
		PamPanel localiserControlButtons=new PamPanel(new GridLayout(0,1)); 
		localiserControlButtons.setBorder(new TitledBorder("Localiser Controls"));
		localiserControlButtons.add(runButton = new JButton("Localise"));
		localiserControlButtons.add(runAllButton = new JButton("Batch Process"));
		localiserControlButtons.add( stop = new JButton("Stop"));
		
		runButton.addActionListener(new RunButton());
		runAllButton.addActionListener(new RunAllButton());
		stop.addActionListener(new StopButton());
		
		localiserControlButtons.setPreferredSize(new Dimension(150,northPanelHeight));
	
		return localiserControlButtons;
	}
	
	

	
	/*
	 *Create a panel which allows users to insert comments.  
	 */
	public PamPanel createCommentPanel(){
	
		PamPanel commentsPanel=new PamPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		c.gridx = 0;
		c.gridwidth = 2;
		PamDialog.addComponent(commentsPanel, new JLabel("Current ", SwingConstants.RIGHT), c);
		c.gridx += c.gridwidth;
		c.gridwidth = 8;
		PamDialog.addComponent(commentsPanel, currentLocalisation = new JTextField(35) , c);
		currentLocalisation.setEditable(false);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 2;
		PamDialog.addComponent(commentsPanel, new JLabel("Comment ", SwingConstants.RIGHT), c);
		c.gridx += c.gridwidth;
		c.gridwidth = 8;
		PamDialog.addComponent(commentsPanel, comment = new JTextField(35), c);

		PamPanel commentPanel=new PamPanel(new BorderLayout());
		commentPanel.add(BorderLayout.EAST,commentsPanel);
		
		return commentPanel;
	}
	
	/**
	 * Create panel which allows users to save results
	 * @return
	 */
	public PamPanel createSavePanel(){

		
		PamPanel saveControlButtons=new PamPanel(new GridLayout(0,1)); 
		saveControlButtons.setBorder(new TitledBorder("Save Controls"));
		saveControlButtons.setPreferredSize(new Dimension(150,150));
		saveControlButtons.add(save = new JButton("Save"));
		saveControlButtons.add(saveAll = new JButton("Keep Old"));
		saveControlButtons.add(setNull = new JButton("Set Null"));
		save.addActionListener(new SaveButton());
		saveAll.addActionListener(new SaveAll());
		setNull.addActionListener(new SetNullButton());
		
		PamPanel savePanel=new PamPanel(new BorderLayout());
		savePanel.add(BorderLayout.CENTER,saveControlButtons);
	
		
		return savePanel;
		
	}
	
	
	private void enableLocaliserControls(int flag) {

		// see how many results are available. 
		ArrayList<TargetMotionResult> results = targetMotionLocaliser.getResults();
		int nResult = results.size();
		
	 
		 if (TargetMotionControl.LOCALISATION_STARTED==flag || TargetMotionControl.LOCALISATION_WAITING==flag ||  TargetMotionControl.LOCALISATION_DONE ==flag){
			 
			 	runButton.setEnabled(canRun() && (flag !=TargetMotionControl.LOCALISATION_STARTED && flag!=TargetMotionControl.LOCALISATION_WAITING));
			 	currentControlPanel.setlayerPanelEnabled(flag !=TargetMotionControl.LOCALISATION_STARTED && flag!=TargetMotionControl.LOCALISATION_WAITING);
				runAllButton.setEnabled(flag==TargetMotionControl.LOCALISATION_DONE);
				stop.setEnabled(flag!=TargetMotionControl.LOCALISATION_DONE);
				
				save.setEnabled(flag==TargetMotionControl.LOCALISATION_DONE && nResult>0);
				saveAll.setEnabled(flag==TargetMotionControl.LOCALISATION_DONE && nResult>0);
				setNull.setEnabled(flag==TargetMotionControl.LOCALISATION_DONE);
				
		 }
	 
		 
		 if (flag==TargetMotionControl.ALGORITHM_SELECTION_CHANGED){
			 	if (flag!=TargetMotionControl.LOCALISATION_STARTED|| (flag!=TargetMotionControl.LOCALISATION_WAITING) ){
			 	runButton.setEnabled(canRun());
				runAllButton.setEnabled(canRun());
			 	}
		 }
		
	}
	

	public void update(int flag) {
		
		enableLocaliserControls(flag);
		
		switch (flag){
		
		case TargetMotionControl.CURRENT_DETECTIONS_CHANGED:
			dialogMap3D.update(flag);
			dialogMap2D.update(flag);
			break ;
		case TargetMotionControl.DETECTION_INFO_CALC_START:
			showLoadBar(true);
			loadBar.setIntermediate(true);
			currentControlPanel.update(flag);
			break ;
		case TargetMotionControl.DETECTION_INFO_CALC_END:
			showLoadBar(false);
			currentControlPanel.update(flag);
			break ;
		case TargetMotionControl.DETECTION_INFO_CALC_PROGRESS:
			if (!isShowingloadBar) showLoadBar(true);
			loadBar.setIntermediate(false);
			int progress=targetMotionContol.getTaregtMotionManager().getCurrentThread().getTMProgress();
			loadBar.setProgress(progress);
			loadBar.setTextUpdate(targetMotionContol.getTaregtMotionManager().getCurrentThread().getCalcString() + " Load progress "+progress+" % ");
			break;
		}
		
	}
	

	private class RunButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			start(false);
		}
	}
	
	private class RunAllButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			targetMotionContol.getOfflineFunctions().openBatchRunDialog();
		}
	}
	
	private class SaveButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			targetMotionContol.save(false);
			
		}
	}
	
	private class SetNullButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			//TODO - do we even keep this?
		}
	}
	
	private class SaveAll implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			//TODO
		}
	}
	
	private class StopButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
		}
	}
	
	
	

	private int visCount = 0;
	
	
	private static int mapErrorCount = 0;
	void setMap(MapPanel<T> maptype){
		if (currentMap != null) {
			currentMap.showMap(false);
			mapBorder.remove(currentMap.getPanel());
			
		}
		try {
			mapBorder.add(BorderLayout.CENTER, maptype.getPanel());
		}
		catch (IllegalArgumentException e) {
			System.out.println("Map graphics error: " + e.getMessage());
//			if (maptype.getClass() == MapPanel3D.class) {
//				dialogMap3D = new MapPanel3D<T>(targetMotionLocaliser, this);
//				if (++mapErrorCount < 2) {
//					setMap(dialogMap3D);
//				}
//				return;
//			}
			if (maptype.getClass() == MapPanel2D.class) {
				dialogMap2D = new MapPanel2D<T>(targetMotionLocaliser, this);
				if (++mapErrorCount < 2) {
					setMap(dialogMap2D);
				}
				return;
			}
		}
		mapBorder.revalidate();
		currentMap=maptype;
		maptype.showMap(true);
		mapErrorCount = 0;
	}
	

	/**
	 * Called when data in the main source data block are changed
	 * @param pamDetection
	 */
	public void dataChanged(T pamDetection) {
		if (pamDetection == null) {
			return;
		}
	}
	
	public void updateCurrentControlPanel() {
		currentControlPanel.refreshData();
	}
	
	public AbstractControlPanel getCurrentControlPanel() {
		return currentControlPanel;
	}

	public void enableControls() {
		for (int i = 0; i < dialogComponents.length; i++) {
			if (dialogComponents[i] == null) continue;
			dialogComponents[i].enableControls();
		}
		
	}
	
	public boolean canRun() {
		for (int i = 0; i < dialogComponents.length; i++) {
			if (dialogComponents[i] == null) continue;
			if (dialogComponents[i].canRun() == false) {
				return false;
			}
		}
		return true;
	}


	/**
	 * Start a run. Will need to get options from various sub panels to 
	 * work out what to actually do.
	 * <p>
	 * Currently just do it all in this thread. In future, will try to rethread 
	 * so multiple models can run concurrently.  
	 */
	public void start(boolean runAll) {

		TargetMotionModel[] modelList = new TargetMotionModel[0];

		ArrayList<TargetMotionModel> models = targetMotionLocaliser.getModels();
		int nModels = models.size();
		TargetMotionModel model;
		TargetMotionResult[] results;
		TargetMotionResult[] allResults = new TargetMotionResult[0];
		long t1, t2;
		for (int i = 0; i < nModels; i++) {
			model = models.get(i);
			if (modelControlPanel.isEnabled(i)) {
				modelList = Arrays.copyOf(modelList, modelList.length+1);
				modelList[modelList.length-1] = model;
			}
		}
		
		if (modelList.length==0) return;
		//update the gui
		
		update(TargetMotionControl.LOCALISATION_STARTED);
		
		
			
		ArrayList<TargetMotionInformation> targetMotionInfoList=new ArrayList<TargetMotionInformation>();
		//TODO- NO LONGER USE THIS OPTION.
		if (runAll==false){
		targetMotionInfoList.add(targetMotionContol.getCurrentTMinfo());
		}
		else{
		targetMotionInfoList.add(targetMotionContol.getCurrentTMinfo());
		}
		
		targetMotionLocaliser.localiseEventList(targetMotionInfoList, modelList, true);		
	}
	
	public void notifyNewResults() {
		currentMap.notifyNewResults();
		modelResultPanel.notifyNewResults();
	}

//	/**
//	 * Called from the localisation worker thread each time an event is loaded, started, 
//	 * completed, etc. 
//	 * @param eventLocalisationProgress
//	 */
//	public void progressReport(EventLocalisationProgress eventLocalisationProgress) {
//		eventControlPanel.progressReport(eventLocalisationProgress);
//		switch (eventLocalisationProgress.progressType) {
//		case EventLocalisationProgress.GOT_RESULTS:
//			notifyNewResults();
//			break;
//		case EventLocalisationProgress.LOADED_EVENT_DATA:
//			notifyNewResults();
//			break;
//		case EventLocalisationProgress.LOADING_EVENT_DATA:
//			
//			break;
//
//		default:
//			notifyNewResults();
//			break;
//		}
//	}

	public String getUserComment() {
		return "";
	}

	class MapSelAction implements ActionListener {

		private MapPanel dialogMap;
		public MapSelAction(MapPanel dialogMap) {
			super();
			this.dialogMap = dialogMap;
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			setMap(dialogMap);
			
		}
		
	}

	class MapSettings implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			currentMap.settings();
		}
	}

	@Override
	public PamColor getColorId() {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public JMenu createMenu(Frame parentFrame) {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public JComponent getPanel() {
		return mainPanel;
	}



	@Override
	public JToolBar getToolBar() {
		// TODO Auto-generated method stub
		return null;
	}


	public TargetMotionControl getTargetMotionControl() {
		return targetMotionContol;
	}


}
