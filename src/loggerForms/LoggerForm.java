package loggerForms;

import generalDatabase.DBControlUnit;
import generalDatabase.EmptyTableDefinition;
import generalDatabase.PamConnection;
import generalDatabase.SQLTypes;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.Timer;

import Array.streamerOrigin.GPSOriginMethod;
import Array.streamerOrigin.GPSOriginSystem;
import Array.streamerOrigin.HydrophoneOriginMethod;
import Array.streamerOrigin.HydrophoneOriginMethods;
import Array.streamerOrigin.OriginIterator;
import Array.streamerOrigin.StaticOriginSystem;
import loggerForms.PropertyTypes;
import loggerForms.controlDescriptions.ControlDescription;
import loggerForms.controlDescriptions.ControlTypes;
import loggerForms.controlDescriptions.InputControlDescription;
import loggerForms.controls.CounterControl;
import loggerForms.controls.LoggerControl;
import loggerForms.controls.NMEAControl;
import GPS.GPSDataBlock;
import GPS.GpsData;
import GPS.GpsDataUnit;
import NMEA.NMEADataBlock;
import NMEA.NMEADataUnit;
import PamController.PamController;
import PamUtils.PamCalendar;
import PamView.PamColors;
import PamView.dialog.PamButton;
import PamView.dialog.PamDialog;
import PamView.dialog.PamLabel;
import PamView.panel.PamPanel;
import PamView.panel.VerticalLayout;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.PamObserverAdapter;

/**
 * A LoggerForm is the central component of all form types, inclucing normal forms
 * popup forms and subtab forms. Therefore the form itself is capable of providing a single
 * JComponent (a JPanel). some other part of the software will either place this into the main tab
 * panel, a sub tab panel or it's own dialog frame (for a pop-up). 
 * @author Graham Weatherup
 *
 */
public class LoggerForm{


	private LoggerFormPanel formPanel;
	private LoggerFormPanel northPanel, centrePanel, innerCenterPanel, southPanel;
	private FormDescription formDescription;

	/**
	 * holds reference to the controls on this LoggerForm
	 */
	private ArrayList<LoggerControl> inputControls;


	/**
	 * @return the inputControls
	 */
	public ArrayList<LoggerControl> getInputControls() {
		return inputControls;
	}

	private EmptyTableDefinition outputTableDef;

	private JButton saveButton = new PamButton("Save");
	private JButton clearButton = new PamButton("Clear");
	private JButton cancelButton = new PamButton("Cancel");

	private PamLabel ctrlHint, ctrlMesssage, lastSaved ;//, formMessage;
	private NMEAMonitor nmeaMonitor;

	/**
	 * holds refernce to itsself so close button can find it's self in the tabPane
	 */
	protected LoggerForm loggerForm = this;
	protected JComponent thingToClose;

	private boolean hasCounter=false;
	private CounterControl counter;
	

//	private HydrophoneOriginMethods origins = HydrophoneOriginMethods.getInstance();
	
	/**
	 * @return the hasCounter
	 */
	public boolean hasCounter() {
		return hasCounter;
	}
	/**
	 * @return the CounterControl
	 */
	public CounterControl getCounter() {
		return counter;
	}

	/**
	 * sets hasCounter to true
	 */
	public void setHasCounter(CounterControl cc) {
		this.hasCounter = true;
		if (counter==null){
			this.counter=cc;
		}
	}

	//	public static enum NewOrEdit{New,Edit}
	public static final int NewDataForm = 0;
	public static final int EditDataForm = 1;
	public static final int PreviewDataForm = 2;
	private int NewOrEdit;
	private LoggerFormPanel lastRow;

	////	/**
	////	 * used to hold reference to dataUnit being edited in "edit" mode
	////	 */
	//	private FormsDataUnit formsDataUnit;

	/**
	 * called when creating a form for new data input(normal/subtabs/popup), or to edit old data
	 * 
	 * JComponent component that the form is put in should maybe be included to say whether it will go in window/frame to rename/remove as necessary 
	 * 
	 * @param formDescription
	 */
	public LoggerForm(FormDescription formDescription, int NewOrEdit) {
		this.NewOrEdit=NewOrEdit;
		this.formDescription = formDescription;
		formPanel = new LoggerFormPanel(this, new BorderLayout());

		northPanel = new LoggerFormPanel(this);
		northPanel.setLayout(new GridLayout(1,3));
		northPanel.add(ctrlHint = new PamLabel("Control Hint"));
		northPanel.add(ctrlMesssage = new PamLabel("Control Messsage"));
		//		northPanel.add(lastSaved = new PamLabel("Last Saved:" + PamCalendar.formatDateTime2(formDescription.getLastSaveTimeFromDB())));


		formPanel.add(BorderLayout.NORTH, northPanel);
		centrePanel = new LoggerFormPanel(this, new GridLayout(1,1));


		formPanel.add(BorderLayout.CENTER, centrePanel);
		inputControls = new ArrayList<LoggerControl>();


		fillCentrePanel();

		setFormColour();
		
		setFormFont(formPanel, formDescription.getFONT());

		if(NewOrEdit==NewDataForm && PamController.getInstance().getRunMode() == PamController.RUN_NORMAL){
			if (formDescription.findProperty(PropertyTypes.READONGPS) != null) {
				setupGpsObserver();
			}
			if (formDescription.findProperty(PropertyTypes.READONTIMER) != null) {
				setupReadTimer();
			}
			PropertyDescription nmeaProperty;
			if ((nmeaProperty = formDescription.findProperty(PropertyTypes.READONNMEA)) != null) {
				setupNMEAReading(nmeaProperty);
			}
		}
	}

	private void setFormFont(Container formComponent, Font font) {
		formComponent.setFont(font);
		formComponent.invalidate();
		formComponent.repaint();
		for (int i = 0; i < formComponent.getComponentCount(); i++) {
			setFormFont((Container) formComponent.getComponent(i), font);
		}
	}
	/**
	 * @return the NewOrEdit
	 */
	public int getNewOrEdit() {
		return NewOrEdit;
	}


	/**
	 * Set up auto reading of an entire form on the arrival of an NMEA string. 
	 * 
	 * @param nmeaProperty NMEA Property description. The Title will be the Module Name for the NMEA module 
	 * (there may be > 1) and the Topic will the the NMEA sentence identifier. 
	 */
	private void setupNMEAReading(PropertyDescription nmeaProperty) {
		String nmeaModuleName = nmeaProperty.getNmeaModule();
		NMEA.NMEAControl nmeaUnit = null;
		if (nmeaModuleName == null) {
			nmeaUnit = (NMEA.NMEAControl) PamController.getInstance().findControlledUnit("NMEA Data");
		}
		else {
			nmeaUnit =  (NMEA.NMEAControl) PamController.getInstance().findControlledUnit("NMEA Data", nmeaModuleName);
		}
		if (nmeaUnit == null) {
			PamDialog.showWarning(null, getFormDescription().getFormNiceName(), 
					"Unable to find NMEA input " + nmeaModuleName + " for User form");
			return;
		}
		String nmeaSentence = nmeaProperty.getNmeaString();
		NMEADataBlock nmeaDataBlock = (NMEADataBlock) nmeaUnit.getPamProcess(0).getOutputDataBlock(0);
		nmeaDataBlock.addObserver(nmeaMonitor = new NMEAMonitor(nmeaDataBlock, nmeaSentence));
	}

	private class NMEAMonitor extends PamObserverAdapter {

		private String nmeaSentence;

		private NMEADataBlock nmeaDataBlock;

		public NMEAMonitor(NMEADataBlock nmeaDataBlock, String nmeaSentence) {
			this.nmeaDataBlock = nmeaDataBlock;
			this.nmeaSentence = nmeaSentence;
		}

		@Override
		public long getRequiredDataHistory(PamObservable o, Object arg) {
			return 0;
		}

		public void disconnect() {
			nmeaDataBlock.deleteObserver(this);
		}

		@Override
		public void addData(PamObservable o, PamDataUnit arg) {
			NMEADataUnit nmeaData = (NMEADataUnit) arg;
			//			NMEADataBlock nmeaDataBlock = (NMEADataBlock) nmeaData.getParentDataBlock();
			StringBuffer nmeaString = nmeaData.getCharData();
			String stringId = NMEADataBlock.getSubString(nmeaString, 0);
			stringId = stringId.substring(1);

			if (stringId.equalsIgnoreCase(nmeaSentence) == false) {
				return;
			}

			/**
			 * Have now got a copy of the latest string we want, so 
			 * use THIS string to fill in all the NMEA controls. Don't let 
			 * them get another one, since there may be a later control in the 
			 * queue before this process is complete. 
			 * All fields on this form should be using the same string, but there
			 * is just a chance that they won't. If they are using data from 
			 * a different string, then they will have to get the data they want
			 * from the latest string of that type themselves. 
			 */
			NMEAControl nmeaControl;
			int nSuccess = 0;
			int nFail = 0;
			int nCant = 0;
			int ans;
			for (LoggerControl aControl:inputControls) {
				//				if (NMEAControl.class.isAssignableFrom(aControl.getClass())) {
				//					nmeaControl = (NMEAControl) aControl;
				if (aControl.getControlDescription().getNmeaString() != null) {
					ans = aControl.updateNMEAData(nmeaData);
				}
				else {
					ans = aControl.autoUpdate();
				}
				switch (ans) {
				case LoggerControl.AUTO_UPDATE_SUCCESS:
					nSuccess++;
					break;
				case LoggerControl.AUTO_UPDATE_FAIL:
					nFail++;
					break;
				case LoggerControl.AUTO_UPDATE_CANT:
					nCant++;
					break;
				}
				//				}
				//				else {
				//					nCant++;
				//				}
			}

			/**
			 * Then save the form
			 */		
			String errors = getFormErrors();
			if (errors == null) {
				save();
			}
			else {
				System.out.println(String.format("Form %s cannot save the NMEA data since it contains errors: %s",
						formDescription.getFormNiceName(), errors));
			}
		}
		
		@Override
		public void removeObservable(PamObservable o) {			
		}

		@Override
		public void setSampleRate(float sampleRate, boolean notify) {
		}

		@Override
		public void noteNewSettings() {
		}

		@Override
		public String getObserverName() {
			return getFormDescription().getFormNiceName();
		}

		@Override
		public void masterClockUpdate(long milliSeconds, long sampleNumber) {
		}

		@Override
		public PamObserver getObserverObject() {
			return this;
		}

	}

	/**
	 * This gets called if the form is to be read every time new gps data are created 
	 * and stored. Will attempt to find the GPS data block and subscribe an oberver to it, 
	 * then all we have to do is call the save function each time new GPS data arrive.  
	 */
	private boolean setupGpsObserver() {
		GPSDataBlock gpsData = (GPSDataBlock) PamController.getInstance().getDataBlock(GpsDataUnit.class, 0);
		if (gpsData == null) {
			String warn = String.format(
					"The form %s is trying to subscribe to the GPS data, but GPS data cannot be found in the PAMGUARD model", 
					formDescription.getFormNiceName());
			System.out.println(warn);
			JOptionPane.showMessageDialog(null, warn, "Logger form error", JOptionPane.WARNING_MESSAGE);
			return false;
		}
		gpsData.addObserver(new GPSObserver());
		return true;
	}

	private class GPSObserver extends PamObserverAdapter {

		@Override
		public String getObserverName() {
			return "Logger form " + formDescription.getFormNiceName();
		}

		@Override
		public void addData(PamObservable o, PamDataUnit arg) {
			readOnGps();
		}


	}

	public SQLTypes getSqlTypes() {
		PamConnection con = DBControlUnit.findConnection();
		if (con == null) return null;
		return con.getSqlTypes();
	}
	
	public void readOnGps() {
		autoUpdateAll();
		String errors = getFormErrors();
		if (errors == null) {
			save();
		}
		else {
			System.out.println(String.format("Form %s cannot save with GPS since it contains errors: %s",
					formDescription.getFormNiceName(), errors));
		}
	}


	/**
	 * This gets called if the form is to saved on a timer. Whatever the time interval, the 
	 * time will be set to ping every second and a save time will be calculated which rounds
	 * to the nearest "nice" time, e.g. the complete half hours, etc. 
	 */
	private void setupReadTimer() {
		Timer formTimer = new Timer(1000, new FormTimerAction());
		formTimer.start();
	}

	/**
	 * Update all controls. this is used with readongps and readontimer. 
	 * All controls used in forms that use these two directives should 
	 * be of a type which can autoupdate (e.g. times, NMEA data, etc). 
	 */
	private boolean autoUpdateAll() {
		int nSuccess = 0;
		int nFail = 0;
		int nCant = 0;
		for (LoggerControl aControl:inputControls) {
			switch (aControl.autoUpdate()) {
			case LoggerControl.AUTO_UPDATE_SUCCESS:
				nSuccess++;
				break;
			case LoggerControl.AUTO_UPDATE_FAIL:
				nFail++;
				break;
			case LoggerControl.AUTO_UPDATE_CANT:
				nCant++;
				break;
			}
		}
		return (nCant == 0 && nFail == 0);
	}

	private class FormTimerAction implements ActionListener {

		long intervalMillis = 10000L;
		long nextActionTime = 0;
		public FormTimerAction() {
			PropertyDescription timerProperty = formDescription.findProperty(PropertyTypes.READONTIMER);
			if (timerProperty.getAutoUpdate() != null) {
				intervalMillis = timerProperty.getAutoUpdate() * 1000;
				intervalMillis = Math.max(1000, intervalMillis);
			}
			long now = PamCalendar.getTimeInMillis();
			nextActionTime = now;
			// now round that to the preceding action time...
			nextActionTime -= nextActionTime%intervalMillis;
			//			nextActionTime /= intervalMillis;
			//			nextActionTime *= intervalMillis;
			/*
			 * If we're closer to the next time than the previous time, 
			 * rack up the time point, if not leave as is to force an immediate read.
			 */
			if (now - nextActionTime < intervalMillis / 2) {
				nextActionTime += intervalMillis;
			}
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (PamCalendar.getTimeInMillis() >= nextActionTime) {
				readOnTimer();
				nextActionTime += intervalMillis;
			}
		}
	}

	private void readOnTimer() {
		autoUpdateAll();
		String errors = getFormErrors();
		if (errors == null) {
			save();
		}
		else {
			System.out.println(String.format("Form %s cannot save on the timer since it contains errors: %s",
					formDescription.getFormNiceName(), errors));
		}
	}

	private void setFormColour() {
		PamColors.getInstance().notifyContianer(formPanel);
	}


	/**
	 * @return the formDescription
	 */
	public FormDescription getFormDescription() {
		return formDescription;
	}



	// create and layout all the form components.
	private void fillCentrePanel() {
		innerCenterPanel = new LoggerFormPanel(this);
		ArrayList<ControlDescription> controlDescriptions = formDescription.getControlDescriptions();

		innerCenterPanel.setLayout(new BoxLayout(innerCenterPanel, BoxLayout.Y_AXIS));

		innerCenterPanel.setLayout(new VerticalLayout(0,VerticalLayout.LEFT,VerticalLayout.TOP));

		//		VerticalFlowLayout vert;
		//		innerCenterPanel.setLayout(vert=new VerticalFlowLayout(VerticalFlowLayout.TOP,0,0));
		////		vert.setAlignment(align)
		//		vert.set
		//		new FlowLayout().set

		LoggerFormPanel currentRow = new LoggerFormPanel(this, new FlowLayout(FlowLayout.LEFT));

		/*
		 * start loop. whenever you get a newline, add to centrePanel and create a new currentRow
		 */

		for (ControlDescription c:controlDescriptions){
			//			System.out.println(c.getType()+"::"+c.getTitle());
			if (c.getItemErrors() > 0) {
				continue;
			}
			if(c instanceof InputControlDescription){
				LoggerControl currentControl = c.makeControl(this);
				if (currentControl == null) {
					boolean tst = true; // why isthis here ? Must have been for a debug point
				}
				inputControls.add(currentControl);
				currentRow.add(currentControl.getComponent());
			}else if(c.getEType()==ControlTypes.NEWLINE){
				//				currentRow.add(new JLabel("|"));
				innerCenterPanel.add(currentRow);
				currentRow = new LoggerFormPanel(this, new FlowLayout(FlowLayout.LEFT));
			}else{
				JPanel component = c.makeComponent(this);
				if (component != null) {
					currentRow.add(component);
				}
			}

		}
		//		currentRow.add(new JLabel("|"));
		innerCenterPanel.add(currentRow);
		//		innerCenterPanel.getLayout().

		//		System.out.println(String.format("%s %s %s",innerCenterPanel.getMinimumSize(), innerCenterPanel.getPreferredSize(),innerCenterPanel.getMaximumSize()));
		//		innerCenterPanel.setPreferredSize(innerCenterPanel.getMinimumSize());
		//		innerCenterPanel.add(Box.createVerticalGlue()); //did not work so made similar filler

		//		Box.Filler vertFiller = new Box.Filler(new Dimension(0,0), new Dimension(0,9999), new Dimension(0,9999));
		//		innerCenterPanel.add(vertFiller);

		//		centrePanel.setAutoscrolls(true);
		JScrollPane scrollPane = new JScrollPane(innerCenterPanel);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		centrePanel.add(scrollPane);

		lastRow = new LoggerFormPanel(this, new FlowLayout(FlowLayout.RIGHT));

		lastRow.add(saveButton);


		boolean isSubTabs = (formDescription.findProperty(PropertyTypes.SUBTABS) != null);
//		boolean isSubForm = (formDescription.findProperty(PropertyTypes.SUBFORM) != null);
		boolean isPopup = (formDescription.findProperty(PropertyTypes.POPUP) != null);
		boolean isNormal;
		if (isSubTabs==false && isPopup==false){
			isNormal=true;
		}else{
			isNormal=false;
		}

		boolean noClear = (formDescription.findProperty(PropertyTypes.NOCLEAR)!= null);
		boolean noCancel= (formDescription.findProperty(PropertyTypes.NOCANCEL)!= null);

		if (isSubTabs || isPopup){
			if (!noCancel){
				lastRow.add(cancelButton);
				cancelButton.addActionListener(new CancelButtonListener());
			}
			saveButton.addActionListener(new SaveAndCloseButtonListener());
		}else{
			if (!noClear){
				lastRow.add(clearButton);
				clearButton.addActionListener(new ClearButtonListener());
			}
			saveButton.addActionListener(new SaveButtonListener());
		}

		saveButton.setDefaultCapable(true);
		if (saveButton.getRootPane() != null) {
			// currently rootpane is null - need to work out how  / when to set 
			// default button once the form is displayed.  
			JRootPane rp = saveButton.getRootPane();
			rp.setDefaultButton(saveButton);
		}

		if (NewOrEdit == NewDataForm) {
			southPanel=lastRow;
			formPanel.add(BorderLayout.SOUTH, southPanel);
		}

		enableControls();
	}

	/**
	 * Enable / disable buttons
	 * <p> for now this is basically just disabling buttons if
	 * we're in viewer mode. A More sophisticated function 
	 * might consider enabling  / diabling depending on whether 
	 * or not a form can be saved. 
	 */
	public void enableControls() {
		boolean isViewer = PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW;
		if (saveButton != null) saveButton.setEnabled(!isViewer);
		if (clearButton != null) clearButton.setEnabled(!isViewer);
		if (cancelButton != null) cancelButton.setEnabled(!isViewer);
	}

	class SaveButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("Save")) {
				String er = getFormErrors();

				getFormWarnings();


				if (er==null){
					save();
				}
			}
		}
	}

	class SaveAndCloseButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("Save")) {
				String er = getFormErrors();

				getFormWarnings();


				if (er==null){
					save();
					formDescription.removeSubtabform(loggerForm);
				}
			}
		}
	}

	class CancelButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			if (e.getActionCommand().equals("Cancel")) {

				/*
				 * remove and delete form (this)
				 */
				//TODO

				formDescription.removeSubtabform(loggerForm);


			}
		}
	}

	class ClearButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			if (e.getActionCommand().equals("Clear")) {
				clear();
			}
		}
	}

	private void clear() {
		for (int i=0;i<inputControls.size();i++){
			//			if (inputControls.get(i).getControlDescription().getType()=="LATLONG"){
			//				System.out.println("pause");
			//			}
			inputControls.get(i).clear();

		}
	}



	public void displayMessage(String message) {
		JOptionPane.showMessageDialog(formPanel, message);
	}

	public String getFormWarnings(){
		String ers= "";
		int erno=0;
		for (LoggerControl lc:inputControls){
			if (!(lc.getDataWarning()==null)){
				ers+=lc.getDataWarning()+"\n";
				lc.clearDataWarning();
				erno+=1;
			}
		}
		if (erno>0){
			displayMessage(ers);
			return ers;
		}else{
			return null;
		}
	}

	public String getFormErrors(){
		String ers= "";
		int erno=0;
		for (LoggerControl lc:inputControls){
			if (!(lc.getDataError()==null)){
				ers+=lc.getDataError()+"\n";
				lc.clearDataError();
				erno+=1;
			}
		}
		if (erno>0){
			displayMessage(ers);
			return ers;
		}else{
			return null;
		}
	}

	/**
	 * Mostly only called when updating a form from the editor. 
	 * @param formsDataUnit
	 */
	void restoreData(FormsDataUnit formsDataUnit){
		//		this.formsDataUnit = formsDataUnit;
		Object[] formData = formsDataUnit.getFormData();
		transferDataArrayToForm(formData);
	}

	/**
	 * Transfers data from the from controls to the data array which will 
	 * get saved in the data unit. 
	 * @return array of data objects
	 */
	public Object[] transferControlDataToArray(SQLTypes sqlTypes) {
		Object[]  formData = new Object[inputControls.size()];//plus a couple?		

		for (int i=0;i<inputControls.size();i++){
			LoggerControl con = inputControls.get(i);
			formData[i]=con.getData();
			if(con.getControlDescription().getAutoclear()){
				con.clear();
			}

		}
		return formData;
	}

	/**
	 * Transfers data from the array within a data unit back into the form 
	 * controls. 
	 * @param dataArray
	 * @return true
	 */
	public boolean transferDataArrayToForm(Object[] dataArray) {
		try {
			for (int i=0;i<inputControls.size();i++){
				Object data = dataArray[i];
				//			ControlDescription cdes = inputControls.get(i).getControlDescription();
				//			if (cdes.getEType()==controlTypes.TIMESTAMP||cdes.getEType()==controlTypes.TIME){
				//				if (data != null) {
				//					data=PamCalendar.millisFromTimeStamp((Timestamp)data);
				//				}
				//			}

				inputControls.get(i).setData(data);
			}
		}
		catch (Exception e) {
			System.out.println("Error transfering data back into Logger form " + loggerForm.getFormDescription().getFormNiceName());
			return false;
		}
		return true;
	}

	Double intToDouble(Object val) {
		if (val == null) return null;
		if (val.getClass() == Double.class) return (Double) val;
		String str = val.toString();
		try {
			return Double.valueOf(str);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}
	
	/**
	 * Extract the data from the from into an Object array, one object per
	 * active control. These match up with a list in the SQL logging
	 * @return list of data objects extracted from the form. 
	 */
	public Object[] extractFormData() {
		boolean isSubTabs = (formDescription.findProperty(PropertyTypes.SUBTABS) != null);
//		boolean isSubForm = (formDescription.findProperty(PropertyTypes.SUBFORM) != null);
		boolean isPopup = (formDescription.findProperty(PropertyTypes.POPUP) != null);
		boolean isNormal;
		if (isSubTabs==false && isPopup==false){
			isNormal=true;
		}else{
			isNormal=false;
		}

		/**
		 * the moving of data from controls to the array has been separated 
		 * out so that it can happen elsewhere. 
		 */
		Object[] formData = transferControlDataToArray(getSqlTypes());


		for (int i=0;i<inputControls.size();i++){
			LoggerControl con = inputControls.get(i);
			if(con.getControlDescription().getAutoclear()){
				con.clear();
			}
			if (isNormal&&NewOrEdit==NewDataForm&&con.getControlDescription().getEType()==ControlTypes.COUNTER){
				CounterControl couCon = (CounterControl)con;
				couCon.updateCounter();
			}
		}
		return formData;
	}

	/**
	 * Extract and save teh data inot a new PAmDAtaUnit. 
	 */
	private void save() {
		//create form data object v
		Object[] formData = extractFormData();

		if (NewOrEdit==NewDataForm){
			FormsDataUnit formDataUnit = new FormsDataUnit(loggerForm,PamCalendar.getTimeInMillis(), formDescription, formData);
			//		dU.setParentDataBlock(formDescription.getFormsDataBlock());
//			System.out.println(formDescription.getXMLData(formDataUnit));
			formDescription.getFormsDataBlock().addPamData(formDataUnit);
		}
		//		else if(NewOrEdit==EditDataForm && formsDataUnit != null){
		//			formsDataUnit.setFormsData(formData);
		//		}
	}



	public boolean printErrors(ArrayList<String> errors){
		String fullError = null;
		if (errors.size()>0){
			fullError="";
			for (String error:errors)
				fullError+=error+"\n";
			JOptionPane.showMessageDialog(formPanel, fullError);
			return true;
		}
		return false;
	}

	public PamPanel getComponent(){
		return formPanel;
	}

	public void setHintAndMessage(LoggerControl loggerControlComponent){
		ctrlHint.setText(loggerControlComponent.getControlDescription().getHint());

		String type = loggerControlComponent.getControlDescription().getType();
		String req = "";
		String name = loggerControlComponent.getControlDescription().getTitle()+": ";

		if (loggerControlComponent.getControlDescription().getRequired()==true){
			req = ": Required";
		}

		ctrlMesssage.setText(name+type.substring(0, 1).toUpperCase()+type.substring(1).toLowerCase()+req);
	}

	public void focusGained(FocusEvent fe,
			LoggerControl loggerControlComponent) {
		setHintAndMessage(loggerControlComponent);


	}

	/**
	 * Destroy the form - which for now means killing 
	 * any NMEA observer. 
	 */
	public void destroyForm() {
		if (nmeaMonitor != null) {
			nmeaMonitor.disconnect();
		}
	}
	/**
	 * @return the lastRow
	 */
	public PamPanel getLastRow() {
		return lastRow;
	}
	/**
	 * @return the saveButton
	 */
	public JButton getSaveButton() {
		return saveButton;
	}
	
//	public GpsData getOriginLatLong(FormsDataUnit formsDataUnit) {
//		GpsData gps = getOrigin(GPSOriginSystem.class, formsDataUnit);
//		if (gps != null) {
//			return gps;
//		}
//		gps = getOrigin(StaticOriginSystem.class, formsDataUnit);
//		return gps;
//	}
//	
//	private GpsData getOrigin(Class originClass, FormsDataUnit formsDataUnit) {
//		HydrophoneOriginMethod origin = origins.getMethod(GPSOriginMethod.class, null, null);
//		if (origin == null) {
//			return null;
//		}
//		OriginIterator gpsIter = origin.getGpsDataIterator(PamDataBlock.ITERATOR_END);
//		GpsData prev = null;
//		while (gpsIter.hasPrevious()) {
//			prev = gpsIter.previous();
//			if (prev.getTimeInMillis() < formsDataUnit.getTimeMilliseconds()) {
//				break;
//			}
//		}
//		return prev;		
//	}
	

	//	/**
	//	 * @return formsDataUnit
	//	 */
	//	public FormsDataUnit getFormsDataUnit() {
	//		// TODO Auto-generated method stub
	//		return formsDataUnit;
	//	}
	//	

}
