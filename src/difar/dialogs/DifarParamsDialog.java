package difar.dialogs;

import generalDatabase.lookupTables.LookUpTables;
import generalDatabase.lookupTables.LookupEditDialog;
import generalDatabase.lookupTables.LookupItem;
import generalDatabase.lookupTables.LookupList;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import difar.DemuxObserver;
import difar.DifarControl;
import difar.DifarParameters;
import difar.DifarParameters.DifarDemuxTypes;
import difar.DifarParameters.DifarOutputTypes;
import difar.DifarParameters.DifarTriggerParams;
import difar.DifarParameters.FirstOrders;
import difar.DifarParameters.SpeciesParams;
import Filters.FilterDialog;
import Filters.FilterParams;
import GPS.GPSControl;
import GPS.GpsDataUnit;
import NMEA.NMEADataUnit;
import PamController.PamController;
import PamController.UsedModuleInfo;
import PamDetection.PamDetection;
import PamDetection.RawDataUnit;
import PamView.dialog.PamButton;
import PamView.dialog.PamDialog;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.SourcePanel;
import PamView.panel.PamPanel;
import PamView.panel.VerticalLayout;
import PamguardMVC.AcousticDataUnit;
import PamguardMVC.PamDataBlock;

public class DifarParamsDialog extends PamDialog {
	
	private DifarControl difarControl;
	private DifarParameters difarParameters;
	private static DifarParamsDialog singleInstance;
	
	private SourcePanel rawDataSource, triggerSource;
	
	private DifarVesselPanel vesselPanel;
	
	private DifarDetectorPanel triggerPanel;

	private JPanel automationPanel = new JPanel();
	
	private JPanel advancedPanel = new JPanel();
	
	private JTextField saveTextField = new JTextField();
	private JTextField saveWithoutCrossTextField = new JTextField();
	private JTextField deleteTextField = new JTextField();
	private JTextField nextClassTextField = new JTextField();
	private JTextField prevClassTextField = new JTextField();
	
//	Classification and Calibration Panels
	AudioPanel[] audioPanels;
	
//	Data
	JComboBox<DifarParameters.DifarDemuxTypes> demuxType;
	
	//	BufferTimes
	private JTextField secondsToPreceed,keepRawDataTime,
	queuedDataKeepTime,
	processedDataKeepTime;
	private JCheckBox clearQueueAtStart,
	clearProcessedDataAtStart;
	
//	Favrorites Params
	JComboBox[] favSpecies;
	JPanel favSpeciesPanel;
	
//	JComboBox autoProcessSpecies; // in future this will have to be dependant on number of detectors/classifyers/ for now there is only 1
	JComboBox firstOrder,secondOrder;
	JCheckBox autoSaveDResult; //moved to actions panel
	JTextField autoSaveTime;
	JCheckBox autoSaveAngleOnly;
	JCheckBox autoProcess;
	
	KeyboardShortcutPanel keyboardShortcutPanel;
	
	private final Object nullItem= new Object(){
		public String toString() {
			return "-- none --";
		};
	};
	
	private final Object defaultItem= new Object(){
		public String toString() {
			return DifarParameters.Default;
		};
	};

	private final Object noAutoItem= new Object(){
		public String toString() {
			return "--Don't Auto Process--";
		};
	};
	private JCheckBox useSummaryLine;
	private SourcePanel calibrationSourcePanel;
	private JCheckBox loadViewerClips;
	
	private DifarParamsDialog(Window parentFrame, DifarControl difarControl) {
		super(parentFrame, difarControl.getUnitName() + " settings", true);
		this.difarControl = difarControl;
		
		JTabbedPane mainPane= new JTabbedPane();
		
		rawDataSource = new SourcePanel(this, "Data Source", RawDataUnit.class, false, false);
		
		keepRawDataTime = new JTextField(5);
		keepRawDataTime.setName("Time to keep Raw Data (s)");
		keepRawDataTime.setToolTipText("Time to keep Raw Data - note that memory may fill up if this is too long");
		
		//Vessel / Classification(All) Settings Choices
		
		audioPanels=new AudioPanel[2];
		audioPanels[0]=new AudioPanel(DifarParameters.CalibrationClip);
		audioPanels[1]=new AudioPanel(null);
		
		JPanel clips = new JPanel();
		BoxLayout cbl = new BoxLayout(clips, BoxLayout.Y_AXIS);
		clips.setLayout(cbl);
		clips.add(audioPanels[1].getDialogComponent());
		
		//Demux Algorithm
		JPanel demuxPanel = new JPanel(new GridBagLayout());
		demuxPanel.setBorder(new TitledBorder("Demultiplexing Settings"));
		demuxType = new JComboBox(DifarParameters.DifarDemuxTypes.values());
		demuxType.setName("Demultiplexer");
		demuxType.setToolTipText("Type of demultiplexing software should be used");
		JComponent[] demuxAry = {demuxType};
		PamPanel.layoutGrid(demuxPanel,demuxAry);
		
		//Buffer Times
		JPanel buffersPanel = new JPanel(new GridBagLayout());
		buffersPanel.setBorder(new TitledBorder("Buffers"));
		
		secondsToPreceed = new JTextField();
		secondsToPreceed.setName("Prepend to clip (s)");
		secondsToPreceed.setToolTipText("Seconds to prepend to clip to allow the demux algorithm lock");
		
		queuedDataKeepTime = new JTextField();
		queuedDataKeepTime.setName("Queued Data Buffer (m)");
		queuedDataKeepTime.setToolTipText("Time to keep data data unprocessed difar clips for in minutes, These require quite a bit of memory so should be kept to the minimum providing it gives the operator enough time to work through them");
		
		processedDataKeepTime = new JTextField();
		processedDataKeepTime.setName("Processed Data Buffer (m)");
		processedDataKeepTime.setToolTipText("Time to keep processed difar localisations in memory for in minutes");
		
		clearQueueAtStart = new JCheckBox();
		clearQueueAtStart.setName("Clear queued data at start");
		clearQueueAtStart.setToolTipText("This will clear all difar clips awaiting processing when Pamguard starts processing after having been stopped");
		
		clearProcessedDataAtStart = new JCheckBox();
		clearProcessedDataAtStart.setName("Clear processed data at start");
		clearProcessedDataAtStart.setToolTipText("This will clear all processed difar clips when Pamguard starts processing after having been stopped");
		
		loadViewerClips = new JCheckBox();
		loadViewerClips.setName("Load clip waveforms in Viewer Mode");
		loadViewerClips.setToolTipText("If checked DIFAR clips are loaded in viewer mode. Uncheck to save memory when viewing long timespans in viewer.");
		JComponent[] bufAry={secondsToPreceed,keepRawDataTime,queuedDataKeepTime,processedDataKeepTime,clearQueueAtStart,clearProcessedDataAtStart, loadViewerClips};
		PamPanel.layoutGrid(buffersPanel, bufAry);

		//Calibration Settings
		JPanel calibrationFull = new JPanel();
		PamPanel intensityPanel = new PamPanel();
		intensityPanel.setBorder(new TitledBorder("Sonobuoy Frequency Response (Intensity Calibration)"));
		intensityPanel.setToolTipText("These parameters can be used to correct for the non-flat frequency response of most sonobuoys.");
		PamButton intensityButton = new PamButton("View/Edit Freq. Response");
		intensityButton.addActionListener(new IntensityButtonAction((Frame) parentFrame));
		intensityPanel.add(intensityButton);
		
		calibrationFull.setLayout(new BoxLayout(calibrationFull, BoxLayout.Y_AXIS));
		calibrationFull.add(intensityPanel);
		
		calibrationSourcePanel = new SourcePanel(null, "Calibration GPS Source", GpsDataUnit.class, false, false);
		calibrationFull.add(calibrationSourcePanel.getPanel());
		
		calibrationFull.add(vesselPanel = new DifarVesselPanel());
		calibrationFull.add(audioPanels[0]);
		
		calibrationFull.setName("Calibration");
		
		// Detectors and triggers
		triggerPanel = new DifarDetectorPanel();
		
		// Favorite species classifications
		favSpeciesPanel = new JPanel();
		updateFavSpeciesPanel();
		
		//Auto-saving
//		firstOrder = new JComboBox(DifarParameters.FirstOrders.values());
//		firstOrder.setName("Auto Process Order");
		
		autoProcess = new JCheckBox();
		autoProcess.setName("Auto-process classified clips");
		autoProcess.setToolTipText("When checked queued clips will be automatically sent to DIFAR Localisation display for processing.");
		
		autoSaveDResult=new JCheckBox();
		autoSaveDResult.setName("Auto save processed clips");
		
		autoSaveTime=new JTextField(3);
		autoSaveTime.setName("After how many seconds");
		
		JComponent[] autoComps = {autoProcess,/*firstOrder,*/autoSaveDResult,autoSaveTime,/*autoSaveAngleOnly*/};
		
		JPanel autoSavePanel = new JPanel(new GridBagLayout());
		PamPanel.layoutGrid(autoSavePanel, autoComps);
		autoSavePanel.setBorder(new TitledBorder("Auto Saving"));
		
// Data Panel
		JPanel dataPanel = new JPanel();
		dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));
		
		dataPanel.add(rawDataSource.getPanel());
		dataPanel.add(demuxPanel);
		dataPanel.add(buffersPanel);

// Automation Panel		
		automationPanel.setLayout(new BoxLayout(automationPanel, BoxLayout.Y_AXIS));
		automationPanel.add(triggerPanel);
		automationPanel.add(favSpeciesPanel);
		automationPanel.add(autoSavePanel);

		keyboardShortcutPanel = new KeyboardShortcutPanel();
		
		automationPanel.add(keyboardShortcutPanel);
			
		mainPane.add("Data", new PushUp(dataPanel));
		mainPane.add("Classification", new PushUp(clips));
		
		mainPane.add(calibrationFull.getName(),new PushUp(calibrationFull));
		mainPane.add("Automation",new PushUp(automationPanel));
		
// Advanced settings panel -- stuff that doesn't really fit anywhere else
		
		useSummaryLine = new JCheckBox();
		useSummaryLine.setName("Use DifarGram summary line");
		JComponent[] advanced = {useSummaryLine};
		PamPanel advProcessPanel = new PamPanel(new GridBagLayout());
		advProcessPanel.setBorder(new TitledBorder("DIFARGram"));
		PamPanel.layoutGrid(advProcessPanel, advanced);
		advancedPanel.setLayout(new BoxLayout(advancedPanel, BoxLayout.Y_AXIS));
		advancedPanel.add(advProcessPanel);
		
		mainPane.add("Advanced", new PushUp(advancedPanel));
		setDialogComponent(mainPane);
		//*************************************************************************
		//Add these lines to enable context sensitive help at the specified target
		
		this.setHelpPoint("localisation.difar.docs.difar_Overview");
		this.enableHelpButton(true);
		//*************************************************************************
		
	}
	class PushUp extends JPanel{
		PushUp(JComponent jComponent){
			super();
			setLayout(new BorderLayout());
			add(jComponent,BorderLayout.NORTH);
		}
	}
	
	class EditSpecies implements ActionListener{

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			editSpecies();
		}
		/**
		 * brings up the dialog and updates the fav species and autoprocess 
		 */
		private void editSpecies() {
			LookupList newList = LookupEditDialog.showDialog(difarControl.getGuiFrame(), difarControl.getDifarParameters().getSpeciesList(difarControl));
			if (newList != null) {
				difarControl.getDifarParameters().setSpeciesList(newList);
				boolean includeDefaultItem = false;
				for (int i=0;i<difarControl.getDifarParameters().numberOfFavouriteWhales;i++){
					updateSpeciesCombo(favSpecies[i], newList, includeDefaultItem);
					favSpecies[i].setName("Favourite Classification "+i);
					favSpecies[i].setToolTipText("Favourite Classification option number "+i+". To be quick options on the East panel of the difar clip");
					favSpecies[i].invalidate();
				}
				includeDefaultItem=true;
				updateSpeciesCombo(audioPanels[1].speciesSelect,newList,includeDefaultItem);
//				updateSpeciesList(difarControl.getDefaultClassificationSelector(),newList);
				difarControl.updateSidePanel();
				triggerPanel.enableControls();
			}
		}
		
		void updateSpeciesCombo(JComboBox jcombobox, LookupList newList, boolean includeDefault){
			Object item = jcombobox.getSelectedItem();
			jcombobox.removeAllItems();
			for (LookupItem li:newList.getSelectedList()){
				jcombobox.addItem(li);
			}
			if (includeDefault){
				jcombobox.insertItemAt(defaultItem, 0);
			}
			else {
				jcombobox.insertItemAt(nullItem, 0);
			}
			jcombobox.setSelectedItem(item);
		}

	}
	
	void updateFavSpeciesPanel(){
		favSpeciesPanel.removeAll(); 
		favSpeciesPanel.setBorder(new TitledBorder("Quick classification"));
		JButton addFavorite = new JButton("More Favorites");
		JButton delFavorite = new JButton("Fewer Favorites");
		addFavorite.addActionListener(new AddFavorite());
		delFavorite.addActionListener(new DelFavorite());
		GridBagConstraints esgc = new PamGridBagContraints();
		favSpeciesPanel.setLayout(new GridBagLayout());
		esgc.gridy=0;
		esgc.gridx=0;
		esgc.fill = GridBagConstraints.HORIZONTAL;
		esgc.anchor = GridBagConstraints.LAST_LINE_END;
		
		favSpecies = new JComboBox[difarControl.getDifarParameters().numberOfFavouriteWhales];
		esgc.weightx = 1;
		esgc.gridwidth = 1;
		favSpeciesPanel.add(delFavorite,esgc);
		esgc.weightx = 0.001;
		JLabel filler=new JLabel();
		filler.setPreferredSize(new JLabel("Enable").getPreferredSize());
		favSpeciesPanel.add(filler,esgc);
		esgc.gridx++;
		esgc.weightx=0.0;
		esgc.gridx++;
		favSpeciesPanel.add(addFavorite,esgc);
		esgc.gridy++;
		
		for (int i=0;i<favSpecies.length;i++){
			int j = i+1;
			esgc.gridx=0;
			esgc.weightx=1.0;
			favSpecies[i] = new JComboBox(difarControl.getDifarParameters().getSpeciesList(difarControl).getSelectedList());
			favSpecies[i].insertItemAt(nullItem, 0);
			
			favSpecies[i].setName("Favourite Classification "+j);
			favSpecies[i].setToolTipText("Favourite #"+j+" on each clip for quick manual classification");
			
			JLabel lab=new JLabel(favSpecies[i].getName());
			lab.setHorizontalAlignment(JLabel.RIGHT);
			lab.setToolTipText(favSpecies[i].getToolTipText());
			favSpeciesPanel.add(lab,esgc);
			esgc.gridx++;
			esgc.weightx = 0.001;
			JLabel fill=new JLabel();
			fill.setPreferredSize(new JLabel("Enable").getPreferredSize());
			favSpeciesPanel.add(fill,esgc);
			esgc.gridx++;
			esgc.gridwidth=1;
			esgc.weightx=0.0;
			if (i==favSpecies.length-1)esgc.weighty=1.0;
			favSpeciesPanel.add(favSpecies[i],esgc);
			esgc.gridy++;
			
			if (difarParameters != null){
				if (i < difarControl.getDifarParameters().getFavSpecies().length){
					favSpecies[i].setSelectedItem(difarControl.getDifarParameters().getFavSpecies()[i]);
				}else{
					favSpecies[i].setSelectedItem(null);
				}
			}
			
		}
	
		favSpeciesPanel.revalidate();

	}
	
	class AddFavorite implements ActionListener{
		
		@Override
		public void actionPerformed(ActionEvent e){
			difarControl.getDifarParameters().numberOfFavouriteWhales++;
			updateFavSpecies();
			updateFavSpeciesPanel();
		}
	}

	class DelFavorite implements ActionListener{
		
		@Override
		public void actionPerformed(ActionEvent e){
			difarControl.getDifarParameters().numberOfFavouriteWhales--;
			if (difarControl.getDifarParameters().numberOfFavouriteWhales < 1){
				difarControl.getDifarParameters().numberOfFavouriteWhales = 1;
			}
			updateFavSpecies();
			updateFavSpeciesPanel();
		}
	}
	
	void updateFavSpecies(){
		LookupItem[] newFavSpecies = new LookupItem[difarParameters.numberOfFavouriteWhales];

		for (int j = 0; j < difarControl.getDifarParameters().numberOfFavouriteWhales; j++){
			if (j < difarControl.getDifarParameters().getFavSpecies().length){
				newFavSpecies[j] = difarControl.getDifarParameters().getFavSpecies()[j];
			}else{
				newFavSpecies[j] = null;
			}
		}
		difarControl.getDifarParameters().setFavSpecies(newFavSpecies);
	}
	
	/**
	 * brings up the import dialog and updates the fav species and autoprocess 
	 */
	class LoadClassifications implements ActionListener{

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			DifarParameters newParameters = difarControl.loadClassificationParams(null, difarControl.getDifarParameters());
			if (newParameters == null){
				return;
			}
			difarControl.getDifarParameters().setSpeciesParams(newParameters.getSpeciesParams());
			LookupList newList = newParameters.getSpeciesList(difarControl);
			difarControl.getDifarParameters().setSpeciesList(newList);

			for (int i=0;i<difarControl.getDifarParameters().numberOfFavouriteWhales;i++){
				updateSpeciesCombo(favSpecies[i], newList);
				favSpecies[i].setName("Favourite Classification "+i);
				favSpecies[i].setToolTipText("A button to quickly select this classification will appear next to the queued clip.");
				favSpecies[i].invalidate();
			}
			updateSpeciesCombo(audioPanels[1].speciesSelect,newList);
			triggerPanel.enableControls();
		}


		void updateSpeciesCombo(JComboBox jcombobox, LookupList newList){
			Object item = jcombobox.getSelectedItem();
			jcombobox.removeAllItems();
			for (LookupItem li:newList.getSelectedList()){
				jcombobox.addItem(li);
			}
			if (jcombobox==audioPanels[1].speciesSelect){
				jcombobox.insertItemAt(defaultItem, 0);
			}
			else {
				jcombobox.insertItemAt(nullItem, 0);
			}
			jcombobox.setSelectedItem(item);
		}
	}
	
	class SaveClassifications implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			
			difarControl.saveClassificationParams(null, 
					difarControl.getDifarParameters().getSpeciesList(difarControl), 
					difarControl.getDifarParameters().getSpeciesParams());
		}
	}
	
	
	class UpdateAudioClip implements ItemListener{
	
		@Override
		public void itemStateChanged(ItemEvent ie) {
			updateOldItem(ie);
			loadNewItem(ie);
		}
		private void updateOldItem(ItemEvent ie){
			if(ie.getStateChange() == ItemEvent.DESELECTED) {
				try{
					audioPanels[1].getParams();
				}catch(Exception e){
					showWarning("The Clip Settings are incorrect");
				}
			} 
		}
		
		private void loadNewItem(ItemEvent ie){
		if(ie.getStateChange() == ItemEvent.SELECTED)
			{
			SpeciesParams whaleParams = difarParameters.findSpeciesParams(ie.getItem().toString());
			audioPanels[1].speciesParams=whaleParams;
			audioPanels[1].setParams();
			}
		}
	
	}
	
	public static final DifarParameters showDialog(Window frame, DifarControl difarControl, DifarParameters difarParameters) {
		
		if (singleInstance == null || singleInstance.difarControl != difarControl || singleInstance.getOwner() != frame) {
			singleInstance = new DifarParamsDialog(frame, difarControl);
		}
		
		singleInstance.difarParameters = difarParameters;
		singleInstance.setParams();
		singleInstance.validate();
		singleInstance.pack();
		singleInstance.setVisible(true);
		return singleInstance.difarParameters;
	}

	private void setParams() {
		rawDataSource.setSource(difarParameters.rawDataName);

		// Update the automation panel in case the detectors have changed
		Component[] components = singleInstance.automationPanel.getComponents();
		for (int i = 0; i<components.length; i++){
			if(components[i].getClass() == DifarDetectorPanel.class){
				singleInstance.automationPanel.remove(i);
				triggerPanel = new DifarDetectorPanel();
				singleInstance.automationPanel.add(triggerPanel, i);
				triggerPanel.setParams();
			}
		}
		
		
		SpeciesParams vesselParams = difarParameters.findSpeciesParams(DifarParameters.CalibrationClip);
		audioPanels[0].speciesParams=vesselParams;
		audioPanels[0].setParams();

		SpeciesParams whaleParams;
		// If for some reason audioPanels[1] has not been instantiated, so set the defaults.
		if (audioPanels[1].speciesSelect.getSelectedItem() == null) {
			whaleParams = difarParameters.findSpeciesParams(DifarParameters.Default);
		} else{
			whaleParams = difarParameters.findSpeciesParams(audioPanels[1].speciesSelect.getSelectedItem().toString());
		}
		audioPanels[1].speciesParams=whaleParams;
		audioPanels[1].setParams();
		
		
		demuxType.setSelectedItem(difarParameters.demuxType);
		
		secondsToPreceed.setText(new Double(difarParameters.secondsToPreceed).toString());
		keepRawDataTime.setText(new Integer(difarParameters.keepRawDataTime).toString());
		queuedDataKeepTime.setText(new Integer(difarParameters.queuedDataKeepTime).toString());
		processedDataKeepTime.setText(new Integer(difarParameters.processedDataKeepTime).toString());
		clearQueueAtStart.setSelected(difarParameters.clearQueueAtStart);
		clearProcessedDataAtStart.setSelected(difarParameters.clearProcessedDataAtStart);
		loadViewerClips.setSelected(difarParameters.loadViewerClips);
		
		calibrationSourcePanel.setSource(difarParameters.calibrationGpsSource);
		vesselPanel.setParams(difarParameters);
		
		for(int j=0;j<favSpecies.length;j++){
			if (j<difarParameters.getFavSpecies().length){
				favSpecies[j].setSelectedItem(difarParameters.getFavSpecies()[j]);
			}else{
				favSpecies[j].setSelectedItem(null);
			}
		}

//		firstOrder.setSelectedItem(difarParameters.firstOrder);
		useSummaryLine.setSelected(difarParameters.useSummaryLine);
		autoProcess.setSelected(difarParameters.autoProcess);
		autoSaveDResult.setSelected(difarParameters.autoSaveDResult);
		autoSaveTime.setText(new Float(difarParameters.autoSaveTime).toString());
		

		// Keyboard commands
		keyboardShortcutPanel.setParams();
	}
	
	@Override
	public boolean getParams() {
		
		PamDataBlock rawBlock = rawDataSource.getSource();
		if (rawBlock == null) {
			return showWarning("You must select a raw data source of DIFAR data");
		}
		
		difarParameters.rawDataName = rawBlock.getDataName();

		if (!triggerPanel.getParams()){
			return showWarning("Problem with Trigger Parameters");
		}
				
		try {
			SpeciesParams vesselParams = difarParameters.findSpeciesParams(DifarParameters.CalibrationClip);
			if (!audioPanels[0].getParams()){
				return false;
			}
		}catch(Exception e){
			return showWarning("The Calibration settings are incorrect");
		}
		
		try{
			String lookupItemName = audioPanels[1].speciesSelect.getSelectedItem().toString();
			SpeciesParams whaleParams = difarParameters.findSpeciesParams(lookupItemName);
			
			if (!audioPanels[1].getParams()){
				return false;
			}
		}catch(Exception e){
			return showWarning("The Classification settings are incorrect " + e.getStackTrace());
		}
		
		//Processing Params
		try{
			difarParameters.demuxType = (DifarDemuxTypes) demuxType.getSelectedItem();
		}catch(Exception e ){
			
			return showWarning("Processing Parameter Problem");
		}
		try{
			difarParameters.secondsToPreceed = new Double(secondsToPreceed.getText());
			difarParameters.keepRawDataTime = new Integer(keepRawDataTime.getText());
			difarParameters.queuedDataKeepTime = new Integer(queuedDataKeepTime.getText());
			difarParameters.processedDataKeepTime= new Integer(processedDataKeepTime.getText());
			
			difarParameters.clearQueueAtStart = clearQueueAtStart.isSelected();
			difarParameters.clearProcessedDataAtStart = clearProcessedDataAtStart.isSelected();
			difarParameters.loadViewerClips = loadViewerClips.isSelected();
		}catch(Exception e ){
			
			return showWarning("Buffer Parameter Problem");
		}
		
		try {
			difarParameters.calibrationGpsSource = calibrationSourcePanel.getSourceName();
		}catch(Exception e){
			return showWarning("Calibration GPS Problem");
		}
		
		if (vesselPanel.getParams(difarParameters)==false){
			return false;
		}
		
		difarParameters.numberOfFavouriteWhales = difarControl.getDifarParameters().numberOfFavouriteWhales;
		LookupItem[] newFavSpecies = new LookupItem[favSpecies.length];
		for (int i=0;i<favSpecies.length;i++){
			Object item = favSpecies[i].getSelectedItem();
			if (item==null||item.equals(nullItem)){
				newFavSpecies[i]=null;
			}else if(LookupItem.class.isInstance(item)){
				newFavSpecies[i]=(LookupItem) item;
			}else {
				return showWarning("Problem with FavSpecies");
			}
		}
		difarParameters.setFavSpecies(newFavSpecies);
		
		
		try{
//			difarParameters.firstOrder=(FirstOrders) firstOrder.getSelectedItem();
			difarParameters.useSummaryLine = useSummaryLine.isSelected();
			difarParameters.autoProcess = autoProcess.isSelected();
			difarParameters.autoSaveDResult=autoSaveDResult.isSelected();
			difarParameters.autoSaveTime=(float) new Float(autoSaveTime.getText());
		}catch(Exception e){
			return showWarning(e.getMessage());
		}
		
		if (keyboardShortcutPanel.getParams() == false){
			return false;
		}
		
		return true;
		
	}
	
	/**
	 * 
	 * @param f1 denomonator
	 * @param f2 numerator
	 * @return true if divides exactly
	 */
	boolean divides(float f1, float f2){
		
		float f3=f2/f1;
		if (f3<0) f3=-f3;
		int divded = Float.compare((float)Math.floor(f3)-f3,(float)0.);
		boolean divdes=divded==0;
//		System.out.println(String.format("F1 %f F2 %f F3 %f divided %s divides %s",f1,f2,f3,divded,divdes));
		return divdes;
	}
	
	/**
	 * 
	 * @param num
	 */
	boolean isPowerOfTwo(int num){
		if (num<1){
			return false;
		}
		try {
			while (num>1){
				if (num%2==0){
					num = num/2;
				}else{
					return false;
				}
			}
		}catch (Exception e){
			return false;
		}
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		difarParameters = null;
	}

	@Override
	public void restoreDefaultSettings() {
		
		DifarParameters newDifarParameters = new DifarParameters();
		
		rawDataSource.setSource(newDifarParameters.rawDataName);
		
		newDifarParameters.setSpeciesParams(newDifarParameters.getSpeciesDefaults());
		difarParameters.restoreDefaultSpeciesParams();
		SpeciesParams vesselParams = newDifarParameters.findSpeciesParams(DifarParameters.CalibrationClip);
		audioPanels[0].speciesParams=vesselParams;
		audioPanels[0].setParams();

		SpeciesParams whaleParams = newDifarParameters.findSpeciesParams(DifarParameters.Default);
		audioPanels[1].speciesParams=whaleParams;
		audioPanels[1].setParams();
		
		demuxType.setSelectedItem(newDifarParameters.demuxType);
		
		secondsToPreceed.setText(new Double(newDifarParameters.secondsToPreceed).toString());
		keepRawDataTime.setText(new Integer(newDifarParameters.keepRawDataTime).toString());
		
		queuedDataKeepTime.setText(new Integer(newDifarParameters.queuedDataKeepTime).toString());
		processedDataKeepTime.setText(new Integer(newDifarParameters.processedDataKeepTime).toString());
		
		clearQueueAtStart.setSelected(newDifarParameters.clearQueueAtStart);
		clearProcessedDataAtStart.setSelected(newDifarParameters.clearProcessedDataAtStart);
		
		vesselPanel.setParams(newDifarParameters);
		
		for(int j=0;j<favSpecies.length;j++){
			favSpecies[j].setSelectedItem(null);
		}
		
//		autoProcessSpecies.setSelectedItem(newDifarParameters.autoProcessSpecies==null?noAutoItem:newDifarParameters.autoProcessSpecies); // in future this will have to be dependant on number of detectors/classifyers/ for now there is only 1
		firstOrder.setSelectedItem(newDifarParameters.firstOrder);
		autoProcess.setSelected(newDifarParameters.autoProcess);
		autoSaveDResult.setSelected(newDifarParameters.autoSaveDResult);
		autoSaveTime.setText(new Float(newDifarParameters.autoSaveTime).toString());
		autoSaveAngleOnly.setSelected(newDifarParameters.autoSaveAngleOnly);
	}
	
	class AudioPanel extends JPanel implements PamDialogPanel{
		/**
		 * param to hold data while beeing checked/saved
		 */
		public SpeciesParams speciesParams;
		
		public String lookupItemName;
		
		private JTextField nAngleSections,intensityScale;
		JComboBox<DifarParameters.DifarOutputTypes> difarOutputType;
		private JTextField processFreqMax;
		private JTextField processFreqMin;
		private JTextField sampleRate;
		private JTextField FFTLength;
		private JTextField FFTHop;
		private JTextField angleBins;
		private JTextField dgIntensity;
		private ButtonGroup markedClipFreqLimits;
		private JRadioButton setLimitsForMarked,markLimitsForMarked;
		
		private ButtonGroup autoDetFreqLimits;
		private JRadioButton setLimitsForAutoDet,detLimitsForAutoDet;
		private TitledBorder border;
		private JComboBox speciesSelect;
		private JButton editSpecies, importList, exportList;
		
		/**
		 * 
		 */
		public AudioPanel(String lookupItemName) {
			super();
			Vector<LookupItem> speciesList = difarControl.getDifarParameters().getSpeciesList(difarControl).getSelectedList();
			speciesSelect = new JComboBox(speciesList);
			this.lookupItemName = (lookupItemName==null) ? defaultItem.toString(): lookupItemName;
			speciesSelect.addItemListener(new UpdateAudioClip());
			speciesSelect.insertItemAt(defaultItem, 0);
			editSpecies = new JButton("Edit Classification List");
			editSpecies.addActionListener(new EditSpecies());
			editSpecies.setToolTipText("Add or remove classifications and view/edit symbols and line colours.");
			importList = new JButton("Load List");
			importList.addActionListener(new LoadClassifications());
			importList.setToolTipText("Load classifications from a file. WARNING: This will erase all existing classifications.");
			exportList = new JButton("Save List");
			exportList.addActionListener(new SaveClassifications());
			exportList.setToolTipText("Save the present classifications to a file.");
//			exportList.setEnabled(false);;
			processFreqMax=new JTextField();
			processFreqMax.setName("Classification Frequency Max. (Hz)");
			processFreqMin=new JTextField();
			processFreqMin.setName("Classification Frequency Min. (Hz)");
			sampleRate=new JTextField();
			sampleRate.setName("Sample Rate.");
			FFTLength=new JTextField();
			FFTLength.setName("FFT Length (samples)");
			FFTLength.setToolTipText("FFT length for beamforming (must be power of 2)");
			FFTHop=new JTextField();
			FFTHop.setName("FFT Hop (samples)");
			nAngleSections = new JTextField();
			nAngleSections.setName("Number of Angle Bins");
			nAngleSections.setToolTipText("Number of bins of angles over which to calculate the DifarGram");
			
			difarOutputType= new JComboBox(DifarParameters.DifarOutputTypes.values());
			difarOutputType.setName("Difar Calculation");
			difarOutputType.setToolTipText("Output type of the DifarGram Calculation");

			intensityScale = new JTextField();
			intensityScale.setName("Intensity Scale");
			intensityScale.setToolTipText("Factor that controls color scaling in the DIFARGram.\nTry values between 2 and 10000");
			
			markedClipFreqLimits = new ButtonGroup();
			markedClipFreqLimits.add(setLimitsForMarked = new JRadioButton("Classification"));
			
			markedClipFreqLimits.add(markLimitsForMarked = new JRadioButton("Marked"));
			
			autoDetFreqLimits = new ButtonGroup();
			autoDetFreqLimits.add(setLimitsForAutoDet = new JRadioButton("Classification"));
			autoDetFreqLimits.add(detLimitsForAutoDet = new JRadioButton("Detection"));
			
			JPanel topP, bottomP, freqLimits;
			border = new TitledBorder(this.lookupItemName+" Audio");
			setBorder(border);
			setLayout(new VerticalLayout(5, VerticalLayout.CENTER, VerticalLayout.TOP));
			
			JComponent[] top={difarOutputType,sampleRate,FFTLength,FFTHop,nAngleSections,intensityScale,processFreqMax,processFreqMin};
			PamPanel.layoutGrid(topP = new JPanel(new GridBagLayout()) ,top );
			add(topP);
			
			if (lookupItemName!=DifarParameters.CalibrationClip){
				bottomP=new JPanel(new GridBagLayout());
				freqLimits = new JPanel(new GridBagLayout());
				TitledBorder freqBorder = new TitledBorder("Frequency Limits");
				freqBorder.setTitleJustification(TitledBorder.CENTER);
				freqLimits.setBorder(freqBorder);
				GridBagConstraints gc = new PamGridBagContraints();
				gc.gridx = gc.gridy = 0;
				gc.gridwidth = 1;
				freqLimits.add(new JLabel("Marked Clips:", JLabel.RIGHT),gc);
				gc.gridx++;
				freqLimits.add(setLimitsForMarked, gc);
				gc.gridx++;
				freqLimits.add(markLimitsForMarked, gc);
				gc.gridy++;
				gc.gridx = 0;
				gc.anchor = GridBagConstraints.EAST;
				freqLimits.add(new JLabel("Auto Detections:", JLabel.RIGHT),gc);
				gc.gridx++;
				freqLimits.add(setLimitsForAutoDet, gc);
				gc.gridx++;
				freqLimits.add(detLimitsForAutoDet, gc);
				
				gc.gridx = gc.gridy = 0;
				gc.gridwidth = 3;
				bottomP.add(freqLimits,gc);
				
				gc.gridwidth=1;
				gc.gridx=0;
				gc.gridy++;
				bottomP.add(new JLabel("Select Classification"), gc);
				gc.gridx++;
				gc.gridwidth=2;
				bottomP.add(speciesSelect, gc);
				gc.gridy++;
				gc.gridx = 1;
				bottomP.add(editSpecies, gc);
				gc.gridwidth=1;
				gc.gridx=1;
				gc.weightx = 0.5;
				gc.gridy++;
				bottomP.add(importList,gc);
				gc.gridx++;
				gc.weightx = 0.5;
				bottomP.add(exportList,gc);
				add(bottomP);
			}
			
		//end constructor
		}
		
		
		/* (non-Javadoc)
		 * @see PamView.PamDialogPanel#getDialogComponent()
		 */
		@Override
		public JComponent getDialogComponent() {
			return this;
		}

		/* (non-Javadoc)
		 * @see PamView.PamDialogPanel#setParams()
		 */
		@Override
		public void setParams() {
			if (speciesParams==null){
				System.out.println("Cannot find params for "+lookupItemName);
			}
			border.setTitle("Classification Parameters: " + speciesParams.lookupItemName);
			setBorder(border);
			this.repaint();
			processFreqMax.setText(	speciesParams==null ? "" : new Float(speciesParams.processFreqMax).toString());
			processFreqMin.setText(	speciesParams==null ? "" : new Float(speciesParams.processFreqMin).toString());
			sampleRate.setText(		speciesParams==null ? "" : new Float(speciesParams.sampleRate).toString());
			FFTLength.setText(		speciesParams==null ? "" : new Integer(speciesParams.FFTLength).toString());
			FFTHop.setText(			speciesParams==null ? "" : new Integer(speciesParams.FFTHop).toString());
			nAngleSections.setText( speciesParams==null ? "" : new Integer(speciesParams.getnAngleSections()).toString());
			difarOutputType.setSelectedItem(speciesParams==null ? "" : speciesParams.difarOutputType);
			intensityScale.setText( speciesParams==null ? "" : new Double(speciesParams.getDifarGramIntensityScaleFactor()).toString());
			if (speciesParams.useMarkedBandsForSpectrogramClips){
				markLimitsForMarked.setSelected(true);
			}else{
				setLimitsForMarked.setSelected(true);
			}
			if (speciesParams.useDetectionLimitsForTriggeredDetections){
				detLimitsForAutoDet.setSelected(true);
				
			}else{
				setLimitsForAutoDet.setSelected(true);
			}
			
		}
		
		

		/* (non-Javadoc)
		 * @see PamView.PamDialogPanel#getParams()
		 */
		@Override
		public boolean getParams() {
			SpeciesParams newSpeciesParams=  speciesParams;
			try{
				newSpeciesParams.sampleRate=Float.valueOf(sampleRate.getText());
				newSpeciesParams.processFreqMax=Float.valueOf(processFreqMax.getText());
				newSpeciesParams.processFreqMin=Float.valueOf(processFreqMin.getText());
				
				if (newSpeciesParams.processFreqMax>(newSpeciesParams.sampleRate/2.0)){
					return showWarning(String.format("%s should be less than half %s, (<%s)",
							processFreqMax.getName(),
							sampleRate.getName(),
							newSpeciesParams.sampleRate/2.0));
				}
				if (newSpeciesParams.processFreqMax< newSpeciesParams.processFreqMin){
					return showWarning(String.format("%s should be less than %s, %s",
							processFreqMin.getName(),
							processFreqMax.getName(),
							newSpeciesParams.processFreqMax));
				}
				
				if (!divides(newSpeciesParams.sampleRate, difarControl.getDifarProcess().getSampleRate())){
					return showWarning(String.format("%s Sample Rate %f doesn't divide %f",lookupItemName,newSpeciesParams.sampleRate,difarControl.getDifarProcess().getSampleRate()));
				}
				newSpeciesParams.FFTLength=(int)Integer.valueOf(FFTLength.getText());
				if (!isPowerOfTwo(newSpeciesParams.FFTLength)){
					return showWarning(String.format("%s FFT %s isn't a power of 2", lookupItemName,newSpeciesParams.FFTLength));
				}
				newSpeciesParams.FFTHop=(int)Integer.valueOf(FFTHop.getText());
				
				if ((!markLimitsForMarked.isSelected())&&(!setLimitsForMarked.isSelected())){
					return showWarning("Select which frequency limits to use for user-marked clips");
				}
				
				newSpeciesParams.useMarkedBandsForSpectrogramClips=markLimitsForMarked.isSelected();
				
				if ((!detLimitsForAutoDet.isSelected())&&(!setLimitsForAutoDet.isSelected())){
					return showWarning("Select which frequency limits to use for auto-detected clips");
				}
				newSpeciesParams.useDetectionLimitsForTriggeredDetections=detLimitsForAutoDet.isSelected();
				
				newSpeciesParams.setnAngleSections(new Integer(nAngleSections		.getText()));
				newSpeciesParams.difarOutputType = (DifarOutputTypes) difarOutputType.getSelectedItem();
				newSpeciesParams.setDifarGramIntensityScaleFactor(new Double(intensityScale.getText()));

				
				boolean added = difarParameters.addSpeciesParams( newSpeciesParams, true);
				if (added){
					return true;
				}else{
					return showWarning("New species params for "+lookupItemName+ " were unable to be added");
				}
			}catch(Exception e){
				e.printStackTrace();
				return showWarning("Classification Params \""+lookupItemName+"\" problem: "+System.getProperty("line.separator")
						+e.getMessage()+System.getProperty("line.separator"));
			}

		}
		
	}
	
	
	
	class DifarVesselPanel extends JPanel implements PamDialogPanel{
		
		private JTextField vesselClipLengthBox;
		private JTextField vesselClipStartSeparationBox;
		private JTextField vesselClipNumberBox;
		
		public DifarVesselPanel() {
			super();
			
			setLayout(new GridBagLayout());
			setName("Compass Calibration");
			setBorder(new TitledBorder(getName()));
			setToolTipText("Parameters to control calibration of sonobuoy compass using sounds from a known platform (e.g. vessel noise)");//TODO
		
			vesselClipLengthBox = new JTextField();
			vesselClipLengthBox.setPreferredSize(new Dimension(30, vesselClipLengthBox.getPreferredSize().height));
			vesselClipLengthBox.setName("Clip Length (s)");
			vesselClipLengthBox.setToolTipText("Length of each clip in seconds");
			
			vesselClipStartSeparationBox = new JTextField();

			vesselClipLengthBox.setPreferredSize(new Dimension(30, vesselClipLengthBox.getPreferredSize().height));
			
			vesselClipStartSeparationBox.setName("Interval between clips (s)");
			vesselClipStartSeparationBox.setToolTipText("Take a clip every n seconds");
			
			vesselClipNumberBox = new JTextField();
			vesselClipLengthBox.setPreferredSize(new Dimension(30, vesselClipLengthBox.getPreferredSize().height));
			vesselClipNumberBox.setName("Number of clips in sequence");
			vesselClipNumberBox.setToolTipText("Take n clips for each initial buoy calibration");
			
			JComponent[] tfs = {vesselClipLengthBox,vesselClipStartSeparationBox,vesselClipNumberBox};
			
			PamPanel.layoutGrid(this, tfs);
		}
		
		public boolean getParams(DifarParameters params){
			try{
				params.vesselClipLength = Integer.valueOf(vesselClipLengthBox.getText());
				params.vesselClipSeparation = Integer.valueOf(vesselClipStartSeparationBox.getText());
				params.vesselClipNumber = Integer.valueOf(vesselClipNumberBox.getText());

			}catch(Exception e){
				PamDialog.showWarning(null, "Vessel Parameters", "Check Parameters for "+getName());
				return false;
			}
			
			return true;
		}
		
		void setParams(DifarParameters params){
			vesselClipLengthBox.setText(Integer.toString(params.vesselClipLength));
			vesselClipStartSeparationBox.setText(Integer.toString(params.vesselClipSeparation));
			vesselClipNumberBox.setText(Integer.toString(params.vesselClipNumber));
//			vesselClipAutoProcess.setSelected(params.vesselClipAutoProcess);
		}
		
		@Override
		public JComponent getDialogComponent() {
			return this;
		}
		
		@Override
		public void setParams() {
		}
		
		@Override
		public boolean getParams() {
			return false;
		}
		
		
		
	}
	
	/**
	 * Contains user interface for selecting which Detectors should be
	 * used as input into the DIFAR module. 
	 *
	 */
	public class DifarDetectorPanel extends PamPanel {

		ArrayList<PamDataBlock> acousticDataBlocks;
		JLabel[] dataLabels;
		JCheckBox[] enableBoxes;
		JButton[] settingsButtons;
		DifarTriggerParams[] triggerParams;
		JComboBox[] speciesLookups;
		
		public DifarDetectorPanel() {
			super();

			this.setBorder(new TitledBorder("Automated detectors"));
			acousticDataBlocks = setDifarTriggerDataBlocks();
			int n = difarControl.getDifarProcess().getNumTriggers();
			dataLabels = new JLabel[n];
			enableBoxes = new JCheckBox[n];
			settingsButtons = new JButton[n];
			triggerParams = new DifarTriggerParams[n];
			speciesLookups = new JComboBox[n];
			PamDataBlock aDataBlock;
			
			this.setLayout(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();
			this.add(new JLabel("Name", SwingConstants.CENTER), c);
			c.gridx++;
			this.add(new JLabel("Enable", SwingConstants.CENTER), c);
			c.gridx++;
			c.fill = GridBagConstraints.HORIZONTAL;
			this.add(new JLabel(" Classification ", SwingConstants.CENTER), c);

			for (int i = 0; i < n; i++) {
				aDataBlock = acousticDataBlocks.get(i);
				if (aDataBlock.isCanClipGenerate() == false) {
					System.out.println("DataBlock " + aDataBlock.getDataName() +  " cannot be used by DIFAR module.");
					continue;
				}
				c.gridy++;
				c.gridx = 0;
				this.add(dataLabels[i] = new JLabel(aDataBlock.getDataName() + " ", SwingConstants.RIGHT), c);
				c.gridx++;
				c.anchor = GridBagConstraints.CENTER;
				this.add(enableBoxes[i] = new JCheckBox(), c);
				enableBoxes[i].addActionListener(new ClipEnableButton(i));
				c.gridx ++;
				speciesLookups[i] = new JComboBox(difarControl.getDifarParameters().getSpeciesList(difarControl).getSelectedList());
				speciesLookups[i].insertItemAt(defaultItem, 0);
				speciesLookups[i].setSelectedIndex(0);
				speciesLookups[i].setEnabled(false);
				this.add(speciesLookups[i], c);
			}
		}

		private ArrayList<PamDataBlock> setDifarTriggerDataBlocks() {
			ArrayList<PamDataBlock> detectionDataBlocks = PamController.getInstance().getDataBlocks(PamDetection.class, true);
			ArrayList<PamDataBlock> clipDataBlock = new ArrayList<PamDataBlock>();
			for (int i = 0; i < detectionDataBlocks.size(); i++ ){
				PamDataBlock db = detectionDataBlocks.get(i);
				if (db.isCanClipGenerate()) {
					System.out.println("DataBlock " + db.getDataName() +  " can be used by DIFAR module.");
					clipDataBlock.add(db);
				}				
			}
			return clipDataBlock;
		}
		

		public void setParams() {
			DifarTriggerParams cgs;
			Vector<LookupItem> speciesList = difarControl.getDifarParameters().getSpeciesList(difarControl).getSelectedList();
			
			for (int i = 0; i < acousticDataBlocks.size(); i++) {
				PamDataBlock aDataBlock = acousticDataBlocks.get(i);
				if (aDataBlock.isCanClipGenerate() == false) {
					continue;
				}
				cgs = difarParameters.findTriggerParams(acousticDataBlocks.get(i).getDataName());
				triggerParams[i] = cgs;
				if (triggerParams[i] == null) continue;
				enableBoxes[i].setSelected(cgs != null && cgs.enable);
				for (int j = 0; j<speciesList.size(); j++ ) {
					if (speciesList.get(j).toString().equals(triggerParams[i].speciesName)){
						speciesLookups[i].setSelectedItem(speciesList.get(j));
					}
				}
				
				enableControls();
				createToolTip(i);
			}
		}

		public boolean getParams() {

			difarParameters.clearAllTriggerParams();
			for (int i = 0; i < difarControl.getDifarProcess().getNumTriggers(); i++) {
				PamDataBlock aDataBlock = acousticDataBlocks.get(i);
				if (aDataBlock.isCanClipGenerate() == false) {
					continue;
				}
				if (triggerParams[i] == null) {
					triggerParams[i] = difarParameters.new DifarTriggerParams(aDataBlock.getDataName());
				}
				triggerParams[i].enable = enableBoxes[i].isSelected();
				triggerParams[i].dataName = aDataBlock.getDataName();
				triggerParams[i].speciesName = speciesLookups[i].getSelectedItem().toString();
				if (speciesLookups[i].getSelectedItem() == defaultItem){
						triggerParams[i].speciesLookupItem = null;
				} else {
					triggerParams[i].speciesLookupItem = (LookupItem) speciesLookups[i].getSelectedItem();	
				}

				difarParameters.addTriggerParams(triggerParams[i]);
			}

			return true;
		}

		void enableControls() {
			for (int i = 0; i < acousticDataBlocks.size(); i++) {
				PamDataBlock aDataBlock = acousticDataBlocks.get(i);
				if (aDataBlock.isCanClipGenerate() == false) {
					continue;
				}
				LookupList newList =  difarControl.getDifarParameters().getSpeciesList(difarControl);
				if (newList != null) {
					Object item = speciesLookups[i].getSelectedItem();
					speciesLookups[i].removeAllItems();
					for (LookupItem li:newList.getSelectedList()){
						speciesLookups[i].addItem(li);
					}
					speciesLookups[i].insertItemAt(defaultItem, 0);
					speciesLookups[i].setSelectedItem(item);
				}
			
				speciesLookups[i].setEnabled(enableBoxes[i].isSelected());
			}
		}
		

		/**
		 * Runs when a trigger has been enabled. Presently does nothing.
		 * @param iDataStream
		 */
		void fireSettings(int iDataStream) {
		}

		private void createToolTip(int iDataStream) {
			String tipText;
			if (enableBoxes[iDataStream].isSelected() && triggerParams[iDataStream] != null) {
				tipText = "Clip generation enabled";
			}
			else {
				tipText = null;
			}
			dataLabels[iDataStream].setToolTipText(tipText);
			enableBoxes[iDataStream].setToolTipText(tipText);
		}

		class ClipEnableButton implements ActionListener {
			int iDataStream;
			/**
			 * @param iDataStream
			 */
			public ClipEnableButton(int iDataStream) {
				super();
				this.iDataStream = iDataStream;
			}
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (enableBoxes[iDataStream].isSelected()) {
					if (triggerParams[iDataStream] == null) {
						fireSettings(iDataStream);
					}
				}
				else {
					if (triggerParams[iDataStream] != null) {
						triggerParams[iDataStream].enable = false;
					}
				}
				enableControls();
			}

		}
		class ClipSettingsButton implements ActionListener {
			int iDataStream;
			/**
			 * @param iDataStream
			 */
			public ClipSettingsButton(int iDataStream) {
				super();
				this.iDataStream = iDataStream;
			}
			@Override
			public void actionPerformed(ActionEvent arg0) {
				fireSettings(iDataStream);
			}

		}

	}

	private class KeyboardShortcutPanel extends JPanel {

		private static final long serialVersionUID = 1L;

		public KeyboardShortcutPanel() {
			setLayout(new GridBagLayout());
			setBorder(new TitledBorder("Global Keyboard Shortcuts"));
			GridBagConstraints c = new GridBagConstraints();
			c.fill = c.HORIZONTAL;
			c.anchor = c.FIRST_LINE_START;
			c.weightx = 1;
			c.gridy = 0;
			c.gridwidth = 1;

			c.gridx = 0;
			add(new JLabel("Delete DIFAR Bearing",JLabel.RIGHT), c);
			c.gridx++;
			add(deleteTextField, c);
			c.gridy++;

			c.gridx = 0;
			add(new JLabel("Save Without Crossbearing",JLabel.RIGHT), c);
			c.gridx++;
			add(saveWithoutCrossTextField, c);
			c.gridy++;

			c.gridx = 0;
			add(new JLabel("Save DIFAR Bearing",JLabel.RIGHT), c);
			c.gridx++;
			add(saveTextField, c);
			c.gridy++;

			c.gridx = 0;
			add(new JLabel("Next Classification in Sidebar",JLabel.RIGHT), c);
			c.gridx++;
			add(nextClassTextField, c);
			c.gridy++;
			
			c.gridx = 0;
			add(new JLabel("Previous Classification in Sidebar",JLabel.RIGHT), c);
			c.gridx++;
			add(prevClassTextField, c);
			c.gridy++;
		}

		public void setParams() {
			try {
				saveTextField.setText(difarParameters.saveKey);
				saveWithoutCrossTextField.setText(difarParameters.saveWithoutCrossKey);
				deleteTextField.setText(difarParameters.deleteKey);
				nextClassTextField.setText(difarParameters.nextClassKey);
				prevClassTextField.setText(difarParameters.prevClassKey);
			}catch(Exception e){
				
			}
		}
		
		public boolean getParams() {
			try{
				String[] keys = {saveTextField.getText(), saveWithoutCrossTextField.getText(),
						deleteTextField.getText(), nextClassTextField.getText(), prevClassTextField.getText()};
				for (int i = 0; i < keys.length; i++){
					if (keys[i].isEmpty())
						continue;
					
					KeyStroke k = KeyStroke.getKeyStroke(keys[i]);
					if (k != null) {
						keys[i] = k.toString().replace("pressed ", "");
					}else {
						String s = "Keyboard shortcut \"" + keys[i] + "\" is not a valid keyboard command.\n";
						s += "Examples of valid keyboard shortcuts include: F1, HOME, ctrl S, shift F12, ctrl alt 1\n";
						s += "NB: The above examples are case sensitive.";
						showWarning(s);
						return false;
					}
				}
				difarParameters.saveKey = keys[0];
				difarParameters.saveWithoutCrossKey = keys[1];
				difarParameters.deleteKey = keys[2];
				difarParameters.nextClassKey = keys[3];
				difarParameters.prevClassKey = keys[4];
			}catch(Exception e){
				e.printStackTrace();
				return false;
			}
			return true;
		}
	}

	
	class IntensityButtonAction implements ActionListener {

		Frame parentFrame;
		
		public IntensityButtonAction(Frame parentFrame) {
			super();
			this.parentFrame = parentFrame;
		}
		public void actionPerformed(ActionEvent e) {
			FilterParams newParams = FilterDialog.showDialog(parentFrame,
					difarParameters.getDifarFreqResponseFilterParams(), difarControl.getDifarProcess().getSampleRate());
			if (newParams != null) {
				difarParameters.difarFreqResponseFilterParams = newParams.clone();
			} 
		}
		
	}
	
}
