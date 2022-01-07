package loggerForms.controls;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.Types;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.Timer;

import NMEA.NMEADataBlock;
import NMEA.NMEADataUnit;
import PamController.PamController;
import PamView.dialog.PamDialog;
import loggerForms.LoggerForm;
import loggerForms.LoggerFormPanel;
import loggerForms.controlDescriptions.ControlDescription;

public abstract class LoggerControl {
	
//	protected HoverListener hoverListener;
	protected JPopupMenu controlMenu=new JPopupMenu();
	protected ControlDescription controlDescription;
	protected LoggerForm loggerForm;
	protected JPanel component;
	
	/**
	 * defect described will stop form being allowed to be saved.
	 */
	protected String dataError;

	/*
	 * used only by controls which subscribe to NMEA data
	 */
	private NMEADataBlock nmeaDataBlock;
	/**
	 * form can still save but with defect described
	 */
	protected String dataWarning;
	protected Class dataType;
	protected Types sqlType;
//	protected Action hoverAction;
	private LoggerControl loggerControl;
	
//	Object dataToSql(Object data){
//		return 
//	}
//	
//	Object sqlToData(Object sql){
//		
//		return dataType.cast(sql);
//	}
	
	
	
	
	
	public static final int AUTO_UPDATE_SUCCESS = 0;
	public static final int AUTO_UPDATE_FAIL = 1;
	public static final int AUTO_UPDATE_CANT = 2;
//	Object lastData = null;;
	
	public LoggerControl(ControlDescription controlDescription,LoggerForm loggerForm){
		HoverListener hoverListener = new HoverListener();
		
		this.controlDescription = controlDescription;
		this.loggerForm = loggerForm;
		this.loggerControl=this;
		component=new LoggerFormPanel(loggerForm){
//			@Override
//			public Component add(Component comp) {
////				comp.addMouseListener(hoverListener);
//				addMouseListenerToAllSubComponants(hoverListener, comp);
//				addFocusListenerToAllSubComponants(new ComponentFocusListener(), comp);
//				return super.add(comp);
//			};
			
		};
		
		
		JMenuItem editMI = new JMenuItem("Edit ItemDescrition");
		editMI.addActionListener(new EditInput());
		//controlMenu.add(editMI);
		
		RightClickListener rclistener = new RightClickListener();
		addMouseListenerToAllSubComponants(rclistener, component);
		addMouseListenerToAllSubComponants(hoverListener, component);
//		setToolTipToAllSubJComponants(controlDescription.getHint(), component);

		setupAutoUpdate();
		
		if (controlDescription.getNmeaString() != null) {
			nmeaDataBlock = findNMEADataBlock();
		}
//		hoverAction=new AutoUpdateAction()
	}
	private class EditInput implements ActionListener{

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			EditItemDialog a = new EditItemDialog(null);
			a.setVisible(true);
		}
		
	}
	private class EditItemDialog extends PamDialog{

		/**
		 * @param parentFrame
		 * @param title
		 * @param hasDefault
		 */
		public EditItemDialog(Window parentFrame) {
			super(parentFrame, "Edit Item", false);
//			add(controlDescription.getEditPanel());
			setResizable(true);
			pack();
		}

		/* (non-Javadoc)
		 * @see PamView.PamDialog#getParams()
		 */
		@Override
		public boolean getParams() {
			// TODO Auto-generated method stub
			return false;
		}

		/* (non-Javadoc)
		 * @see PamView.PamDialog#cancelButtonPressed()
		 */
		@Override
		public void cancelButtonPressed() {
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see PamView.PamDialog#restoreDefaultSettings()
		 */
		@Override
		public void restoreDefaultSettings() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private class RightClickListener extends MouseAdapter{

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseClicked(MouseEvent e) {
//			System.out.println("but: "+e.getButton());
			if (e.getButton()==e.BUTTON3){
				
//				pasteMI.setEnabled(pastableFromMap());
				
				controlMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}
		
	}
		
	/**
	 * for popup forms, the AutoUpdate can be set < 0 in which case
	 * it will update the control ONCE when the form is opened (i.e. 
	 * when the control is created) and then perform no further updates. 
	 * If Autoupdate is > 0, then it will set up the timer accordingly. 
	 * If it's zero or null, don't do anything. 
	 * 
	 */
	private void setupAutoUpdate() {
		
		Integer autoUp = controlDescription.getAutoUpdate();
		if (autoUp == null || autoUp == 0) {
			return;
		}
		else if (autoUp < 0) {
			/*
			 *  can't actually do this here since the control elements may not
			 *  be complete - so invoke later by using a timer with no repeats. 
			 */
			Timer autoUpdateTimer = new Timer(100, new AutoUpdateAction());
			autoUpdateTimer.setInitialDelay(0);
			autoUpdateTimer.setRepeats(false);
			autoUpdateTimer.start();
		} else {
			int updateTime = Math.max(controlDescription.getAutoUpdate()*1000, 1000);
			Timer autoUpdateTimer = new Timer(updateTime, new AutoUpdateAction());
			autoUpdateTimer.setInitialDelay(0);
			autoUpdateTimer.start();
		}
	}
	
	/**
	 * 
	 * @param listener
	 * @param jComponent
	 */
	public  void setToolTipToAllSubJComponants(Component component){
		
		
		try{
			JComponent jComponent=(JComponent) component;
			jComponent.setToolTipText(controlDescription.getHint());
			for(Component subComponent :jComponent.getComponents()){
				setToolTipToAllSubJComponants(subComponent);
			}
			
//			addMouseListenerToAllSubComponants(l, subJComponent);
		}catch (ClassCastException e) {
			// cannot add
		}
		
		
	}
	/**
	 * 
	 * @param listener
	 * @param jComponent
	 */
	public static void addMouseListenerToAllSubComponants(MouseListener l,Component component){
		component.addMouseListener(l);
		
		try{
			JComponent jComponent=(JComponent) component;
			
			for(Component subComponent :jComponent.getComponents()){
				addMouseListenerToAllSubComponants(l, subComponent);
			}
			
//			addMouseListenerToAllSubComponants(l, subJComponent);
		}catch (ClassCastException e) {
			// cannot add
		}
		
		
	}
	
	public static void addFocusListenerToAllSubComponants(FocusListener l,Component component){
		component.addFocusListener(l);
		
		try{
			JComponent jComponent=(JComponent) component;
			
			for(Component subComponent :jComponent.getComponents()){
				addFocusListenerToAllSubComponants(l, subComponent);
			}
			
//			addMouseListenerToAllSubComponants(l, subJComponent);
		}catch (ClassCastException e) {
			// cannot add
		}
		
		
	}
	
	
	
	class HoverListener extends MouseAdapter{
		
		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			loggerForm.setHintAndMessage(loggerControl);
			
		}
		
		/* (non-Javadoc)
		 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseMoved(MouseEvent e) {
			// TODO Auto-generated method stub
			loggerForm.setHintAndMessage(loggerControl);
			
		}
		
	}
	
	
	
	/**
	 * Class for auto update action listener. 
	 * @author Doug Gillespie
	 *
	 */
	private class AutoUpdateAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			autoUpdate();
		}
	}

	public void addF1KeyListener(JComponent component) {
		try{
			JComponent jComponent=(JComponent) component;
			jComponent.addKeyListener(new F1KeyListener());
			for(Component subComponent :jComponent.getComponents()){
				setToolTipToAllSubJComponants(subComponent);
			}
			
//			addMouseListenerToAllSubComponants(l, subJComponent);
		}catch (ClassCastException e) {
			// cannot add
		}
//		component.addKeyListener(new F1KeyListener());
	}
	
	
	
	
	public void f1Pressed() {
		autoUpdate();
	}
	
	
	class F1KeyListener extends KeyAdapter {

		@Override
		public void keyPressed(KeyEvent k) {
//			System.out.println("Key pressed" + k.getKeyCode());
			if (k.getKeyCode() == 112) {
				f1Pressed();
			}
		}
		
	}
	
	
	
	
	
	
	/**
	 * 
	 * @return
	 */
	public abstract String getDataError();
	
	/**
	 * 
	 * @return data from field(s) of component
	 */
	public abstract Object getData();
	
	/**
	 * this will populate the Field with either last entered data or most up to date entry
	 * @return
	 */
	public abstract void setData(Object data);
	
	/**
	 * 
	 */
//	public abstract moveDataToTableItemOrItems();
	
	/**
	 * Return to the default value based on the associated control description
	 */
	public abstract void setDefault();
	
	/**
	 * Automatically update a control. This is only possible
	 * for controls such as time, timestamps and NMEA items. 
	 * 
	 * @return AUTO_UPDATE_SUCCESS, AUTO_UPDATE_FAIL or AUTO_UPDATE_CANT 
	 */
	public int autoUpdate() {
		if (nmeaDataBlock != null) {
			return updateNMEAData();
		}
		return AUTO_UPDATE_CANT;
	}
	
	public void clear() {
		setData(null);
	}
	
	/**
	 * validates data on field by field basis
	 * @return true if data is valid, false if not and notify user.
	 */
//	public boolean validateField()//{return true;}
//	{
//		boolean valid=false;
//		Object data = getData();
//		if (controlDescription.getRequired()==null||!controlDescription.getRequired()){//not required
//			if (data==null||data.toString().length()==0){
//				
//				valid=true;
//			}else{
//				//do check
//				if (!(getDataError()==null)){//there are errors
//					valid= false;
//				}else {
//					if (!(getDataWarning()==null)){//there are warnings
//						
//					}
//					valid= true;
//				}
//			}
//			
//		}else{//required
//			
//			if (data==null||data.toString().length()==0){
//				System.out.println(controlDescription.getTitle()+"is a required field");
//				valid=false;
//			}else{
//				if (!(getDataError()==null)){//there are errors
//					valid= false;
//				}else {
//					if (!(getDataWarning()==null)){//there are warnings
//						
//					}
//					valid= true;
//				}
//			}
//		}
//		
//		return valid;
//	}
	
	

	

	/**
	 * @return the controlDescription
	 */
	public ControlDescription getControlDescription() {
		return controlDescription;
	}

	/**
	 * @return the loggerForm
	 */
	public LoggerForm getLoggerForm() {
		return loggerForm;
	}

	/**
	 * @return the component
	 */
	public JPanel getComponent(){
		return component;
	}
	
	
	public void lFocusGained(FocusEvent fe) {
		loggerForm.focusGained(fe, this);
	}
	
	class ComponentFocusListener implements FocusListener {

		@Override
		public void focusGained(FocusEvent fe) {
			lFocusGained(fe);
		}

		@Override
		public void focusLost(FocusEvent arg0) {
			
			
		}
		
	}
	
	/**
	 * @return the dataWarning
	 */
	public String getDataWarning() {
		return dataWarning;
	}
	
	/**
	 * clear DataWarning field
	 */
	public void clearDataWarning() {
		dataWarning=null;
	}

	public void clearDataError() {
		dataError=null;
	}

	/**
	 * 
	 */
//	public void moveDataToTableItems() {
//		controlDescription.getFormsTableItems()[0].setValue(getData());
//	}
	/*
	 * Put all the NMEA reading stuff into general controls so that anything can receive data 
	 * from NMEA (jncluding tiems and lat longs !
	 *//**
	 * Tries to find the appropriate NMEA string by searching 
	 * multiple data blocks if necessary. IF the previous call was 
	 * Successful, then it will just go straight to that same data
	 * block, otherwise, it will search all available datablocks. 
	 * @return
	 */
	public int updateNMEAData() {
		if (nmeaDataBlock == null) {
			nmeaDataBlock = findNMEADataBlock();
			if (nmeaDataBlock == null) {
				clear();
				return AUTO_UPDATE_FAIL;
			}
		}
		NMEADataUnit dataUnit = nmeaDataBlock.findNMEADataUnit(controlDescription.getNmeaString());
		if (dataUnit == null) {
			clear();
			return AUTO_UPDATE_FAIL;
		}
		
		return fillNMEAControlData(dataUnit);

	}

	/**
	 * Called once a correct NMEA data unit has been found
	 * to write the data into the control. 
	 * @param dataUnit NMEA data unit
	 * @return success flag. 
	 */
	abstract public int fillNMEAControlData(NMEADataUnit dataUnit);

	/**
	 * Used when NMEA data is being updated in response to the arrival of 
	 * a new NMEA string (i.e. on forms which save all NMEA data from a 
	 * single string). An actual string is passed in, but there is just
	 * a chance that it will be the wrong type in which case the default 
	 * is used. 
	 * @param nmeaData
	 */
	public int updateNMEAData(NMEADataUnit nmeaData) {
		if (controlDescription.getNmeaString() == null) {
			return AUTO_UPDATE_CANT;
		}
		// TODO Auto-generated method stub
		StringBuffer nmeaString = nmeaData.getCharData();
		String stringId = nmeaData.getStringId();
		if (stringId == null) {
			return AUTO_UPDATE_FAIL;
		}
		if (stringId.endsWith(controlDescription.getNmeaString())) {
			return updateNMEAData(); // use the one where itfinds its own string
		}
		return fillNMEAControlData(nmeaData);
		
	}

	private NMEADataBlock foundNMEADataBlock = null;
	/**
	 * The NMEA datablock name must be stored somewhere - how about as Topic ?	
	 * @return
	 */
	public NMEADataBlock findNMEADataBlock() {
		if (controlDescription.getNmeaString() == null) {
			return null;
		}
		if (foundNMEADataBlock != null) {
			return foundNMEADataBlock;
		}
		String nmeaModuleName = controlDescription.getNmeaModule();
		NMEA.NMEAControl nmeaUnit = null;
		if (nmeaModuleName == null) {
			nmeaUnit = (NMEA.NMEAControl) PamController.getInstance().findControlledUnit("NMEA Data");
		}
		else {
			 nmeaUnit =  (NMEA.NMEAControl) PamController.getInstance().findControlledUnit("NMEA Data", nmeaModuleName);
		}
		if (nmeaUnit == null){
			return null;
		}
		foundNMEADataBlock = (NMEADataBlock) nmeaUnit.getPamProcess(0).getOutputDataBlock(0);
		return foundNMEADataBlock;
	}

}
