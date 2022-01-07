/**
 * 
 */
package loggerForms.controls;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.MenuItem;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JMenuItem;
import javax.swing.text.MaskFormatter;


import Array.ArrayManager;
import GPS.GPSControl;
import GPS.GPSDataBlock;
import GPS.GpsDataUnit;
import NMEA.NMEADataBlock;
import NMEA.NMEADataUnit;
import PamController.PamControlledUnit;
import PamController.PamController;
import PamController.masterReference.MasterReferencePoint;
import PamUtils.LatLong;
import PamUtils.LatLongDialog;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamView.ClipboardCopier;
import PamView.PamGui;
import loggerForms.FormsControl;
import loggerForms.LoggerForm;
import loggerForms.controlDescriptions.ControlDescription;
import loggerForms.controls.LatLongControl.F4KeyListener;
import loggerForms.controls.LoggerControl;
import loggerForms.controls.LoggerControl.ComponentFocusListener;

/**
 * 
 * 
 * <p>
 * 
 * holds gps stamp, both lat and lon will save separately in the data base but
 * will display in between 1 to 3(if time too) fields on the form and in one in
 * the data table. will probably extend this to give GPSTimestamp which will
 * bundle in a timestamp field too.
 * 
 * @author GrahamWeatherup
 *
 */
public class LatLongControl extends LoggerControl implements ClipboardOwner{

	//	public static enum autoupdateOptions{
	private static String deg="\u00B0";
	private static String min="'";
	private static String sec="\"";
	private static String pt=".";
	private static String latTemplate = String.format("##%s##%s###%s *",deg,pt,min);
	private static String lonTemplate = String.format("###%s##%s###%s *",deg,pt,min);
	//			"#"+latTemplate;
	private Character[] latSign={'N','S'};
	private Character[] lonSign={'E','W'};

	public static int latDegMax=90,latDegMin=-90;
	public static int lonDegMax=180,lonDegMin=-180;
	public static int minNsecMax=60,minNsecMin=0;
	//	minutesMin=0;



	//	public static String template=String.format("000%s00%s00%s00%s00",deg,min,pt,sec);
	protected static int LAT_FIELD_LENGTH = 10;
	protected static int LON_FIELD_LENGTH = 10;
	protected JFormattedTextField textFieldLat;
	protected JFormattedTextField textFieldLon;
	protected MaskFormatter latFor;
	protected MaskFormatter lonFor;

	/**
	 * channel map of hydrophones to get mean position of for autoupdates eg.
	 * <p>0-boat GPS
	 * <p>1-1st hydrophone-hydrophone0
	 * <p>2-2nd hydrophone-hydrophone1
	 * <p>3-1st & 2nd hydrophones-hydrophones 0 and 1
	 * <p>4-3rd hydrophone-hydrophone 2
	 * <p>5-1st & 3rd hydrophones-hydrophones 0 and 2
	 * <p>6-2nd & 3rd hydrophones-hydrophones 1 and 2
	 */
	private Integer channelMap;
	//	/**
	//	 * used to hold channels in use IFF channelMap>0
	//	 */
	//	private int[] channels;
	JMenuItem pasteMI;

	/**
	 * @param controlDescription
	 * @param loggerForm
	 */
	public LatLongControl(ControlDescription controlDescription,
			LoggerForm loggerForm) {
		super(controlDescription, loggerForm);

		if (LatLong.getFormatStyle()==LatLong.FORMAT_DECIMALMINUTES){
			latTemplate=String.format("##%s##%s###%s",deg,pt,min);
		} 
		else if(LatLong.getFormatStyle()==LatLong.FORMAT_MINUTESSECONDS){
			latTemplate=String.format("##%s##%s##%s##%s",deg,min,pt,sec);
		}
		lonTemplate="#"+latTemplate;

		latTemplate+=" *";
		lonTemplate+=" *";

		makeComponent();

		controlMenu.add(pasteMI=new JMenuItem("Paste Lat Long From Map"));
		pasteMI.addActionListener(new PasteGPS());
		JMenuItem clearMI;
		controlMenu.add(clearMI=new JMenuItem("Clear "+controlDescription.getTitle()));
		clearMI.addActionListener(new ClearControl());
		RightClickListener listener = new RightClickListener();
		addMouseListenerToAllSubComponants(listener, component);
		String topic = controlDescription.getTopic();
		if (topic != null) {
			topic = topic.trim();
			if (topic.equals("null")) {
				topic = null;
			}
		}
		if(topic==null||topic.length()==0){
			channelMap=null;
		}else{// if (controlDescription.getTopic().trim()=="0"){
			try {
				channelMap=Integer.valueOf(topic);
			}
			catch (NumberFormatException e) {
				channelMap = null;
			}
		}
		//		}else{
		////			String[] channels = controlDescription.getTopic().split(",");
		//			
		//			int[] chans=new int[channels.length];
		//			for (int i=0;i<channels.length;i++){
		//				chans[i]=Integer.valueOf(channels[i].trim());
		//			}
		//			channelMap=PamUtils.makeChannelMap(chans);
		//			this.channels=PamUtils.getChannelArray(channelMap);
		//			
		//		}


	}

	//	getLatTemplate(){
	//		LatLong.
	//	}

	GPSDataBlock findGpsDataBlock() {
		GPSControl gpsControl = (GPSControl) PamController.getInstance().findControlledUnit(GPSControl.gpsUnitType);
		if (gpsControl == null) {
			return null;
		}
		return gpsControl.getGpsDataBlock();
	}



	private class RightClickListener extends MouseAdapter{

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseClicked(MouseEvent e) {
			//			System.out.println("but: "+e.getButton());
			if (e.getButton()==e.BUTTON3){

				pasteMI.setEnabled(pastableFromMap());

				controlMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}

	}



	/**
	 * 
	 * @param channelsStr
	 * @return
	 */
	public Integer[] getChannelArray(String channelsStr){
		if (channelsStr==null) return null;
		String[] chStrs = channelsStr.split(",");
		if (chStrs==null) return null;
		Integer[] chs= new Integer[chStrs.length];
		for (int i=0;i<chStrs.length;i++){
			chs[i]=Integer.valueOf(chStrs[i]);
		}
		return chs;
	}

	private void makeComponent() {

		component.add(new LoggerFormLabel(loggerForm,controlDescription.getTitle()));
		component.add(new LoggerFormLabel(loggerForm,"Lat"));
		textFieldLat = new JFormattedTextField(getLatAbstractformatter());
		component.add(textFieldLat);

		component.add(new LoggerFormLabel(loggerForm,"Lon"));
		textFieldLon = new JFormattedTextField(getLonAbstractformatter());
		component.add(textFieldLon);

		addF1KeyListener(textFieldLat);
		addF1KeyListener(textFieldLon);
		F4KeyListener f4 = new F4KeyListener();
		textFieldLat.addKeyListener(f4);
		textFieldLon.addKeyListener(f4);

		Font font = getControlDescription().getFormDescription().getFONT();
		textFieldLat.setFont(font);
		textFieldLon.setFont(font);
		Dimension lat = textFieldLat.getPreferredSize();
		Dimension lon = textFieldLon.getPreferredSize();
		double charwid = textFieldLat.getFontMetrics(font).getMaxAdvance()/2;
		lat.width = (int) (LAT_FIELD_LENGTH * charwid);
		lon.width = (int) (LON_FIELD_LENGTH * charwid);
		textFieldLat.setPreferredSize(new Dimension(lat));
		textFieldLon.setPreferredSize(new Dimension(lon));
		setDefault();
		component.add(new LoggerFormLabel(loggerForm,controlDescription.getPostTitle()));

		//		textFieldLat.setToolTipText(controlDescription.getHint());
		//		textFieldLon.setToolTipText(controlDescription.getHint());

		//		textFieldLat.setEnabled(false);
		//		textFieldLon.setEnabled(false);
		//		textFieldLat.setEditable(false);
		//		textFieldLon.setEditable(false);
		//		textFieldLat.setFocusable(true);
		//		textFieldLon.setFocusable(true);

		//		textFieldLat.addPropertyChangeListener("value", new LatChangeListener());
		//		textFieldLon.addPropertyChangeListener("value", new LonChangeListener());

		JMenuItem pasteMenuItem = new JMenuItem("Paste from Map");

		pasteMenuItem.addActionListener(new PasteGPS());

		JMenuItem inputMenuItem = new JMenuItem("Input Lat Long...");
		inputMenuItem.addActionListener(new InputGPS());
		controlMenu.add(inputMenuItem);

		textFieldLat.addFocusListener(new ComponentFocusListener());
		textFieldLon.addFocusListener(new ComponentFocusListener());
		setToolTipToAllSubJComponants(component);
		//		component.addComponentListener(new )

	}

	//	class show LatLong (time) Dialog

	class F4KeyListener extends KeyAdapter {

		@Override
		public void keyPressed(KeyEvent k) {
			//			System.out.println("Key pressed" + k.getKeyCode());
			if (k.getKeyCode() == 115) {
				//				f4Pressed();
				inputLatLon();
			}
		}
	}

	void inputLatLon(){
		long timeMilliseconds = PamCalendar.getTimeInMillis();

		int frame = controlDescription.getFormDescription().getFormsControl().getFrameNumber();

		LatLong defLatLong=(LatLong) getData();
		if (defLatLong==null){

			if(channelMap==null||channelMap<1){
//				defLatLong=ArrayManager.getArrayManager().getCurrentArray().getHydrophoneLocator().getStreamerLatLong(timeMilliseconds);
				defLatLong = MasterReferencePoint.getLatLong();
			}else{
				defLatLong=getArrayMeanPosition(timeMilliseconds);
			}
		}
		LatLong newLatLong = LatLongDialog.showDialog(PamController.getInstance().getGuiFrameManager().getFrame(frame), defLatLong, controlDescription.getTitle());
		if (newLatLong!=null){
			setData(newLatLong);
		}
	}

	class PasteGPS implements ActionListener{

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {

			setData(pasteFromMap());
		}

	}

	class ClearControl implements ActionListener{

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {

			clear();
		}



	}

	/**
	 * 
	 */
	private LatLong pasteFromMap() {
		Clipboard clipboard =Toolkit.getDefaultToolkit().getSystemClipboard();
		//		clipboard.
		Object data =null;
		LatLong dataVal=null;
		Transferable data2=null;

		data2= clipboard.getContents(null);
		try {
			//				System.out.println("d2Cls:"+data2.getTransferData(LatLong.getDataFlavor()).getClass());
			data= data2.getTransferData(LatLong.getDataFlavor());
			dataVal=(LatLong)data;
			return dataVal;
		} catch (UnsupportedFlavorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		System.out.println("CBLLdatV:"+dataVal);

		try {
			data = clipboard.getData(DataFlavor.stringFlavor);
//			System.out.println("CBStdata:"+data);
			dataVal= new LatLong((String)data);
//			System.out.println("CBStdatV:"+dataVal);
		} catch (UnsupportedFlavorException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ClassCastException e3) {
			// TODO: handle exception
			e3.printStackTrace();
		}

		setData(dataVal);

		return dataVal;

	}

	/**
	 * checks to see if there is data in the clipboard 
	 * @return
	 */
	private boolean pastableFromMap(){
		Clipboard clipboard =Toolkit.getDefaultToolkit().getSystemClipboard();
		//		
		//		DataFlavor latLongFlavor = LatLong.getDataFlavor();
		//		System.out.println(clipboard.isDataFlavorAvailable(latLongFlavor));
		//		DataFlavor[] flvs = clipboard.getAvailableDataFlavors();
		//		for (int flv=0;flv<flvs.length;flv++){
		//			System.out.println(flvs[flv].getMimeType());
		//		}
		//		return clipboard.isDataFlavorAvailable(latLongFlavor);

		DataFlavor[] a = clipboard.getContents(null).getTransferDataFlavors();
		boolean ans = clipboard.getContents(this).isDataFlavorSupported(LatLong.getDataFlavor());
		return ans;
		//		for(DataFlavor b:a){
		//			System.out.println(b);
		//		}
		//		System.out.println("hi");
		//		
		//		return false;
	}

	class InputGPS implements ActionListener{


		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {

			//			int frame = controlDescription.getFormDescription().getFormsControl().getFrameNumber();
			//			LatLong newLatLong = LatLongDialog.showDialog(PamController.getInstance().getGuiFrameManager().getFrame(frame), null, controlDescription.getTitle());
			//			setData(newLatLong);
			inputLatLon();
			//			setData(LatLongDialog.latLong);
		}

	}


	class PasteKeyListener extends KeyAdapter {

		@Override
		public void keyPressed(KeyEvent e) {
			if ((e.getKeyCode() == KeyEvent.VK_V) && ((e.getModifiers() & 
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()
					//					KeyEvent.CTRL_MASK
					) != 0)) {

				pasteFromMap();
			}
		}
	}


	//	private AbstractFormatter getLatAbstractformatter() {
	//		
	//		MaskFormatter latFor = null;
	//		try {
	//			latFor = new MaskFormatter(latTemplate);
	//			latFor.setValidCharacters("0123456789NnSs-"+deg+min+sec+pt);
	//		} catch (ParseException e) {
	//			//  Auto-generated catch block
	//			dataError="Latitude in " +controlDescription.getTitle() +" could not be parsed:-"+e.getMessage();
	//			e.printStackTrace();
	//		}
	//		
	////		may need to override these too
	//		
	////		@Override
	////		public String valueToString(Object value) throws ParseException {
	////			//  Auto-generated method stub
	////			return null;
	////		}
	////		
	////		@Override
	////		public Object stringToValue(String text) throws ParseException {
	////			//  Auto-generated method stub
	////			return null;
	////		}
	//		
	//		
	////		DateFormat format= new SimpleDateFormat("HH:mm:ss"); //("H:m:s")
	////		DateFormatter formatter = new DateFormatter(format);
	////		return null;
	//		return latFor;
	//	}

	private AbstractFormatter getLatAbstractformatter() {
		if (latFor==null){


			try {
				latFor = new MaskFormatter(latTemplate){
					/* (non-Javadoc)
					 * @see javax.swing.text.MaskFormatter#stringToValue(java.lang.String)
					 */
					@Override
					public Object stringToValue(String value) throws ParseException {
						if (value==null||value=="") return null;
						try {
							return LatLong.valueOfSubstring(value);
						}
						catch (NumberFormatException e) {
							return null;
						}

					}
					/* (non-Javadoc)
					 * @see javax.swing.text.MaskFormatter#valueToString(java.lang.Object)
					 */
					@Override
					public String valueToString(Object value) throws ParseException {
						if (value==null) return "";
						try {
							return LatLong.formatLatitude((Double) value);
						}catch (Exception e) {
							return "";
							//						e.printStackTrace();
						}
						//					return super.valueToString(value);
					}
				};
				latFor.setValidCharacters("0123456789NnSs"+deg+min+sec+pt);

			} catch (ParseException e) {
				// TODO Auto-generated catch block
				dataError="Latitude in " +controlDescription.getTitle() +" could not be parsed:-"+e.getMessage();
				e.printStackTrace();
			}


			//		may need to override these too

			//		@Override
			//		public String valueToString(Object value) throws ParseException {
			//			//  Auto-generated method stub
			//			return null;
			//		}
			//		
			//		@Override
			//		public Object stringToValue(String text) throws ParseException {
			//			//  Auto-generated method stub
			//			return null;
			//		}


			//		DateFormat format= new SimpleDateFormat("HH:mm:ss"); //("H:m:s")
			//		DateFormatter formatter = new DateFormatter(format);
			//		return null;
		}
		return latFor;
	}

	private AbstractFormatter getLonAbstractformatter() {

		if (lonFor==null){
			try {
				lonFor = new MaskFormatter(lonTemplate){
					/* (non-Javadoc)
					 * @see javax.swing.text.MaskFormatter#stringToValue(java.lang.String)
					 */
					@Override
					public Object stringToValue(String value) throws ParseException {
						if (value==null||value=="") return null;
						try {
							return LatLong.valueOfSubstring(value);
						}
						catch (NumberFormatException e) {
							return null;
						}

					}
					/* (non-Javadoc)
					 * @see javax.swing.text.MaskFormatter#valueToString(java.lang.Object)
					 */
					@Override
					public String valueToString(Object value) throws ParseException {
						if (value==null||((Double) value)==null) return "";
						try {
							//							Double val = (Double) value;
							//							if val==null
							return LatLong.formatLongitude((Double)value);
						}catch (Exception e) {
							return "";
							//						e.printStackTrace();
						}
						//					return super.valueToString(value);
					}
				};
				lonFor.setValidCharacters("0123456789EeWw"+deg+min+sec+pt);

			} catch (ParseException e) {
				// TODO Auto-generated catch block
				dataError="Longitude in " +controlDescription.getTitle() +" could not be parsed:-"+e.getMessage();
				e.printStackTrace();
			}


			//		may need to override these too

			//		@Override
			//		public String valueToString(Object value) throws ParseException {
			//			//  Auto-generated method stub
			//			return null;
			//		}
			//		
			//		@Override
			//		public Object stringToValue(String text) throws ParseException {
			//			//  Auto-generated method stub
			//			return null;
			//		}


			//		DateFormat format= new SimpleDateFormat("HH:mm:ss"); //("H:m:s")
			//		DateFormatter formatter = new DateFormatter(format);
			//		return null;
		}
		return lonFor;
	}

	@Override
	public Object getData() {

		//		String lat = textFieldLat.getText();
		//		String lon = textFieldLon.getText();
		Double lat,lon;
		lat =  (Double) textFieldLat.getValue();
		lon =  (Double) textFieldLon.getValue();

		if (lat == null || lon == null) return null;

		//		System.out.println("text: "+date+" "+time);
		//		System.out.println("mili: "+PamCalendar.msFromDateString(date+" "+time));
		//		System.out.println("date: "+PamCalendar.dateFromDateString(date+" "+time));
		//		System.out.println("tstp: "+PamCalendar.getTimeStamp(PamCalendar.msFromDateString(date+" "+time)));
		try {
			return new LatLong(lat,lon);
		}
		catch (Exception e) {
			if (dataWarning==null) dataWarning="";
			dataWarning+="\n Unable to create Lat Long point";
			return null;
		}

		//		return PamCalendar.getTimeStamp(PamCalendar.msFromDateString));

	}

	@Override
	public int autoUpdate() {
		if (findNMEADataBlock() != null) {
			return updateNMEAData();
		}
		long timeMilliseconds = PamCalendar.getTimeInMillis();
		if(channelMap==null||channelMap==0){

//			setData(ArrayManager.getArrayManager().getCurrentArray().getHydrophoneLocator().getStreamerLatLong(timeMilliseconds));
			setData(MasterReferencePoint.getLatLong());
		}else{
			setData(getArrayMeanPosition(timeMilliseconds));
		}
		return AUTO_UPDATE_SUCCESS;

	}

	//	private class LatChangeListener implements PropertyChangeListener{
	//
	//		/* (non-Javadoc)
	//		 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	//		 */
	//		@Override
	//		public void propertyChange(PropertyChangeEvent arg0) {
	//
	//			System.out.println("old:"+arg0.getOldValue());
	//			System.out.println("new:"+arg0.getNewValue());
	//			try {
	//				textFieldLat.commitEdit();
	//			} catch (ParseException e) {
	//				// TODO Auto-generated catch block
	////				e.printStackTrace();
	//			}
	//		}
	//		
	//	}
	//	private class LonChangeListener implements PropertyChangeListener{
	//
	//		/* (non-Javadoc)
	//		 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	//		 */
	//		@Override
	//		public void propertyChange(PropertyChangeEvent arg0) {
	//
	////			System.out.println("old:"+arg0.getOldValue());
	////			System.out.println("new:"+arg0.getNewValue());
	//			try {
	//				textFieldLon.commitEdit();
	//			} catch (ParseException e) {
	//				// TODO Auto-generated catch block
	////				e.printStackTrace();
	//			}
	//		}
	//		
	//	}

	@Override
	public void setData(Object latLong) {

		LatLong a = (LatLong)latLong;
		if (a==null){
			//			System.out.println("latLon null");
			textFieldLat.setValue(null);
			textFieldLon.setValue(null);
			return;
		}
		//		System.out.println("a       :"+a.toString());
		textFieldLat.setValue(a.getLatitude());
		//		textFieldLat.setText(a.formatLatitude());

		//		System.out.println("a latVal:"+textFieldLat.getValue());
		//		System.out.println("a latTxt:"+textFieldLat.getText());
		textFieldLon.setValue(a.getLongitude());
		//		textFieldLon.setText(a.formatLongitude());

		//		addPropertyChangeListener("value", this);
		//		try {
		//			textFieldLat.commitEdit();
		//			textFieldLon.commitEdit();
		//		} catch (ParseException e) {
		//			if (dataWarning==null) dataWarning="";
		//			dataWarning+=" Unable to setData(...) ";
		//			e.printStackTrace();
		//		}
		//		if (latLong==null){
		//			System.out.println("LatLont null");
		//			textFieldLat.setText("");
		//			textFieldLon.setText("");
		//			return;
		//		}
		//		
		//		if (latLong.getClass().isAssignableFrom(LatLong.class)){
		//			System.out.println("LatLont assignable");
		//			LatLong latLon = (LatLong) latLong;
		//			
		//			if (latLon==null){
		//				System.out.println("LatLont still null");
		//				textFieldLat.setText("");
		//				textFieldLon.setText("");
		//				return;
		//			}
		//			
		//			textFieldLat.setText(LatLong.formatLatitude(latLon.getLatitude()));
		//			System.out.println(latLon.toString());
		//			textFieldLon.setText(LatLong.formatLongitude(latLon.getLongitude()));
		//		}



	}

	@Override
	public void setDefault() {

	}

	LatLong getArrayMeanPosition(long timeMilliseconds) {
		int[] channels = PamUtils.getChannelArray(channelMap);
		//		System.out.println(channels);
		double lat=0;
		double lon=0;
		double height=0;
		int no=channels.length;
		for (int i=0;i<no;i++){
			LatLong ll = ArrayManager.getArrayManager().getCurrentArray().getHydrophoneLocator().getPhoneLatLong(timeMilliseconds, channels[i]);
			lat+=ll.getLatitude();
			lon+=ll.getLongitude();
			height+=ll.getHeight();
		}
		return new LatLong(lat/no, lon/no, height/no);
	}



	/* (non-Javadoc)
	 * @see java.awt.datatransfer.ClipboardOwner#lostOwnership(java.awt.datatransfer.Clipboard, java.awt.datatransfer.Transferable)
	 */
	@Override
	public void lostOwnership(Clipboard arg0, Transferable arg1) {

	}

	/* (non-Javadoc)
	 * @see loggerForms.controls.LoggerControl#getDataError()
	 */
	@Override
	public String getDataError() {
		LatLong data= (LatLong) getData();
		if (data == null && controlDescription.getRequired() == false) {
			return null;
		}
		else if (data == null && controlDescription.getRequired() == true) {
			return controlDescription.getTitle()+" is a required field.";
		}
		else if (data!=null){

			Double lat = LatLong.valueOfSubstring(textFieldLat.getText());
			Double lon = LatLong.valueOfSubstring(textFieldLon.getText());

			if (lat==null){
				if (dataError==null)dataError="";
				dataError+="\n"+ controlDescription.getTitle()+" -Latitude is incorrect";
			}
			if (lon==null){
				if (dataError==null)dataError="";
				dataError+="\n"+ controlDescription.getTitle()+" -Latitude is incorrect";
			}

		}
		if (dataError==null||dataError.length()==0) dataError=null;
		return dataError;



		//		
		//		if (data==null){
		//			String lat=textFieldLat.getText();
		//			String lon=textFieldLon.getText();
		//			if (lat==null||lat.length()==0||lon==null||lon.length()==0){
		//				Boolean req = controlDescription.getRequired();
		//				if (req==true){
		//					return controlDescription.getTitle()+" is a required field.";
		//				}
		//				if (dataError==null||dataError.length()==0){
		//					return null;
		//				}
		//				return dataError;
		//				
		//			}else{
		//				return dataError;
		//			}
		//			
		//		}
		//		return dataError;
	}

/**
 * The end-result of an auto-update of Lat-Long
 */
	@Override
	public int fillNMEAControlData(NMEADataUnit dataUnit) {
		/** 
		 * Seems this was originally coded to grab lat-long from an NMEA module,
		 * but this has been broken for ages (if it ever worked in the first place).
		 * Now it grabs the lat-long directly from the latest GPS data unit, so 
		 * now auto-update actually works -- provided there's a working GPS module.
		 * 
		 * Still todo: A more complete fix would actually go through and remove all 
		 * the other broken/unneccessary code as well as clean up the user-facing
		 * options  (e.g. LatLon Properties for specifying NMEA module string).   
		 */
		double latitude = 0;
		double longitude = 0;
		
		try {
			GpsDataUnit gpsUnit = findGpsDataBlock().getLastUnit();
			latitude = gpsUnit.getGpsData().getLatitude();
			longitude = gpsUnit.getGpsData().getLongitude();

		}
		catch (NumberFormatException e) {
			return AUTO_UPDATE_FAIL;
		}
		LatLong ll = new LatLong(latitude, longitude);
		setData(ll);
		return AUTO_UPDATE_SUCCESS;
	}

}
