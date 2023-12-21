package Array;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ListIterator;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import Array.sensors.ArrayParameterType;
import Array.sensors.ArraySensorFieldType;
import Array.sensors.swing.SensorSourceComponent;
import Array.streamerOrigin.HydrophoneOriginMethod;
import Array.streamerOrigin.HydrophoneOriginMethods;
import Array.streamerOrigin.HydrophoneOriginSystem;
import Array.streamerOrigin.OriginDialogComponent;
import Array.streamerOrigin.OriginSettings;
import PamController.PamController;
import PamUtils.LatLong;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamguardMVC.PamDataBlock;

public class StreamerDialog extends PamDialog {


	private static final long serialVersionUID = 1L;

	private JTextField x, y, z, dx, dy, dz;
	private JTextField heading, pitch, roll;
	//	private JCheckBox enableOrientation;

	static public final int textLength = 4;
	static public final int errorTextLength = 6;

	public Streamer defaultStreamer;

	private JTextField streamerName; 

	private JPanel originPanel;

	private JComponent currentOriginComponent;

	protected static StreamerDialog singleInstance;

	private JComboBox<HydrophoneOriginSystem> originMethod;

	private JComboBox<HydrophoneLocatorSystem> localiserMethod;

	private HydrophoneOriginMethod currentOriginMethod;

	private InterpolationDialogPanel interpolationPanel;

	private SensorSourceComponent[] sensorComponents;

	public PamArray currentArray;

	private boolean constructed;

	private JPanel mainPanel;

	//Labels and borders that might be changed depdnding on medium 
	private JLabel streamerHeightLabel;

	private TitledBorder locateRecieverBorder;

	protected StreamerDialog(Window parentFrame) {
		super(parentFrame, PamController.getInstance().getGlobalMediumManager().getRecieverString() + " Streamer", false);
		setMainPanel(new JPanel());
		getMainPanel().setLayout(new BoxLayout(getMainPanel(), BoxLayout.Y_AXIS));

		interpolationPanel = new InterpolationDialogPanel(this);

		ArraySensorFieldType[] sensorFields = ArraySensorFieldType.values();
		sensorComponents = new SensorSourceComponent[sensorFields.length];
		EnableOrientation eo = new EnableOrientation();
		for (int i = 0; i < sensorFields.length; i++) {
			sensorComponents[i] = new SensorSourceComponent(sensorFields[i], true, sensorFields[i] != ArraySensorFieldType.HEIGHT);
			sensorComponents[i].addActionListenr(eo);
		}
		/*
		 * Reference position panel
		 */
		JPanel oPanel = new JPanel(new BorderLayout());
		oPanel.setBorder(new TitledBorder("Reference Position"));
		oPanel.add(BorderLayout.NORTH, originMethod = new JComboBox<HydrophoneOriginSystem>());
		oPanel.add(BorderLayout.WEST, originPanel = new JPanel());
		int n = HydrophoneOriginMethods.getInstance().getCount();
		for (int i = 0; i < n; i++) {
			originMethod.addItem(HydrophoneOriginMethods.getInstance().getMethod(i));
		}
		originMethod.addActionListener(new OriginMethodListener());

		/*
		 * Relative position panel
		 */
		JPanel relPanel = new JPanel(new BorderLayout());
		//		relPanel.setBorder(new TitledBorder("Location from Reference"));
		Insets ins = PamGridBagContraints.getEmptyBorderInsets();
		ins.right *= 2;
		relPanel.setBorder(new EmptyBorder(ins));
		GridBagConstraints c = new PamGridBagContraints();
		GridBagLayout sharedGridBag;
		JPanel p = new JPanel(sharedGridBag = new GridBagLayout());
		relPanel.add(BorderLayout.CENTER, p);
		c = new PamGridBagContraints();
		c.gridx = 1;
		addComponent(p, new JLabel("Position"), c);
		c.gridx+=2;
		addComponent(p, new JLabel("Error"), c);
		c.gridx = 0;
		c.gridy ++;
		addComponent(p, new JLabel("x ", JLabel.RIGHT), c);
		c.gridx++;
		addComponent(p, x = new JTextField(textLength), c);
		c.gridx++;
		addComponent(p, new JLabel(" +/- "), c);
		c.gridx++;
		addComponent(p, dx = new JTextField(errorTextLength), c);

		c.gridx = 0;
		c.gridy ++;
		addComponent(p, new JLabel("y ", JLabel.RIGHT), c);
		c.gridx++;
		addComponent(p, y = new JTextField(textLength), c);
		c.gridx++;
		addComponent(p, new JLabel(" +/- "), c);
		c.gridx++;
		addComponent(p, dy = new JTextField(errorTextLength), c);

		c.gridx = 0;
		c.gridy ++;
		addComponent(p, streamerHeightLabel = new JLabel(" ", JLabel.RIGHT), c); // set in setStreamerLabels
		c.gridx++;
		addComponent(p, z = new JTextField(textLength), c);
		c.gridx++;
		addComponent(p, new JLabel(" +/- "), c);
		c.gridx++;
		addComponent(p, dz = new JTextField(errorTextLength), c);
		c.gridx = 0;
		c.gridy++;
		addComponent(p, new JLabel("Sensor "), c);
		c.gridx++;
		c.gridwidth = 3;
		addComponent(p, sensorComponents[ArraySensorFieldType.HEIGHT.ordinal()].getComponent(), c);

		//		c.gridx = 0;
		//		c.gridy++;
		//		c.gridwidth = 2;
		//		addComponent(p, new JLabel("Location method", JLabel.RIGHT), c);
		//		c.gridx+=c.gridwidth;
		//		c.gridwidth = 3;
		//		addComponent(p,  , c);

		localiserMethod = new JComboBox<>();
		relPanel.add(BorderLayout.NORTH, localiserMethod);
		n = HydrophoneLocators.getInstance().getCount();
		for (int i = 0; i < n; i++) {
			localiserMethod.addItem(HydrophoneLocators.getInstance().getSystem(i));
		}
		//		localiserMethod.addItem("Rigid");
		//		localiserMethod.addItem("Threading");

		JPanel headPanel = new JPanel();
		//		headPanel.setBorder(new TitledBorder("Orientation"));
		ins = PamGridBagContraints.getEmptyBorderInsets();
		ins.left*=2;
		headPanel.setBorder(new EmptyBorder(ins));
		headPanel.setLayout(sharedGridBag);
		c = new PamGridBagContraints();
		relPanel.add(BorderLayout.SOUTH, headPanel);
		//		c.gridwidth = 5;
		//		addComponent(headPanel, enableOrientation = new JCheckBox("Use orientation data"), c);
		//		enableOrientation.addActionListener(new EnableOrientation());
		c.gridx = 3;
		c.gridwidth = 1;
		addComponent(headPanel, new JLabel("Sensor inputs"), c);
		int orWidth = 4;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy++;
		//		addComponent(headPanel, new JLabel("  "), c);
		String degsLab = LatLong.deg + " ";
		c.gridx = 0;
		c.gridy++;
		addComponent(headPanel, new JLabel("Heading ", JLabel.RIGHT), c);
		c.gridx++;
		addComponent(headPanel, heading = new JTextField(orWidth), c);
		c.gridx++;
		addComponent(headPanel, new JLabel(degsLab, JLabel.LEFT), c);
		c.gridx++;
		addComponent(headPanel, sensorComponents[ArraySensorFieldType.HEADING.ordinal()].getComponent(), c);
		c.gridx = 0;
		c.gridy++;
		addComponent(headPanel, new JLabel("Pitch ", JLabel.RIGHT), c);
		c.gridx++;
		addComponent(headPanel, pitch = new JTextField(orWidth), c);
		c.gridx++;
		addComponent(headPanel, new JLabel(degsLab, JLabel.LEFT), c);
		c.gridx++;
		addComponent(headPanel, sensorComponents[ArraySensorFieldType.PITCH.ordinal()].getComponent(), c);
		c.gridx = 0;
		c.gridy++;
		addComponent(headPanel, new JLabel("Roll ", JLabel.RIGHT), c);
		c.gridx++;
		addComponent(headPanel, roll = new JTextField(orWidth), c);
		c.gridx++;
		addComponent(headPanel, new JLabel(degsLab, JLabel.LEFT), c);
		c.gridx++;
		addComponent(headPanel, sensorComponents[ArraySensorFieldType.ROLL.ordinal()].getComponent(), c);


		JPanel bPanel = new JPanel(new BorderLayout());
		c = new PamGridBagContraints();
		bPanel.setBorder(new TitledBorder("Streamer Name"));
		bPanel.add(BorderLayout.CENTER, streamerName = new JTextField(20));
		//		addComponent(bPanel, new JLabel("Streamer Name "), c);
		//		c.gridx++;
		//		addComponent(bPanel, streamerName = new JTextField(20), c);
		streamerName.setToolTipText("Streamer Name can be null");
		getMainPanel().add(bPanel);

		getMainPanel().add(oPanel);
		JPanel locPanel = new JPanel();
		//		BoxLayout bl;
		//		locPanel.setLayout(bl = new BoxLayout(locPanel, BoxLayout.X_AXIS));
		locPanel.setLayout(new BorderLayout());
		locPanel.setBorder(locateRecieverBorder = new TitledBorder("")); //set by steStreamerLabels
		locPanel.add(BorderLayout.WEST, relPanel);
		locPanel.add(BorderLayout.CENTER, new JSeparator(JSeparator.VERTICAL));
		locPanel.add(BorderLayout.EAST, headPanel);
		getMainPanel().add(locPanel);
		getMainPanel().add(interpolationPanel.getComponent(getOwner()));
		setDialogComponent(getMainPanel());

		constructed = true;

		//set borders and labels
		setStreamerLabels();
		setHelpPoint("utilities.hydrophoneArrayManagerHelp.docs.Array_NewStreamer");
	}

	public static Streamer showDialog(Window window, PamArray currentArray, Streamer streamer) {
		//		if (singleInstance == null || singleInstance.getOwner() != window) {
		singleInstance = new StreamerDialog(window);
		//		}
		singleInstance.currentArray = currentArray;
		singleInstance.defaultStreamer = streamer;//.clone();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.defaultStreamer;
	}

	/**
	 * Set changeable labels for the reciever - e.g. if microphone or hydrophones 
	 */
	private void setStreamerLabels() {

		String recieverString = PamController.getInstance().getGlobalMediumManager().getRecieverString();  
		String recieverZString = PamController.getInstance().getGlobalMediumManager().getZString();

		locateRecieverBorder.setTitle(recieverString + " Locator Method");
		streamerHeightLabel.setText(recieverZString);
		if (singleInstance!=null) singleInstance.setTitle(recieverString + " Streamer");
	}

	public void setParams() {
		// origin methods
		//		MasterLocator masterLocator = currentArray.getMasterLocator();
		//		int streamerIndex = currentArray.indexOfStreamer(streamer);
		HydrophoneLocator hLocator = defaultStreamer.getHydrophoneLocator();
		if (hLocator != null) {
			int locatorIndex = HydrophoneLocators.getInstance().indexOfClass(hLocator.getClass());
			localiserMethod.setSelectedIndex(locatorIndex);

			HydrophoneOriginMethod originMethod = defaultStreamer.getHydrophoneOrigin();
			if (originMethod != null) {
				int originIndex = HydrophoneOriginMethods.getInstance().indexOfClass(originMethod.getClass());
				this.originMethod.setSelectedIndex(originIndex);
			}
		}
		streamerName.setText(defaultStreamer.getStreamerName());

		x.setText(formatDouble(defaultStreamer.getX()));
		y.setText(formatDouble(defaultStreamer.getY()));
		z.setText(formatDouble(PamController.getInstance().getGlobalMediumManager().getZCoeff()*defaultStreamer.getZ()));
		dx.setText(formatDouble(defaultStreamer.getDx()));
		dy.setText(formatDouble(defaultStreamer.getDy()));
		dz.setText(formatDouble(defaultStreamer.getDz()));
		//		if (streamer.getBuoyId1() != null) {
		//			buoyId.setText(streamer.getBuoyId1().toString());
		//		}
		//		else {
		//			buoyId.setText("");
		//		}

		HydrophoneOriginMethod mth = defaultStreamer.getHydrophoneOrigin();
		OriginDialogComponent mthDialogComponent = mth.getDialogComponent();
		if (mthDialogComponent != null) {
			mthDialogComponent.setParams();
		}

		//		enableOrientation.setSelected(defaultStreamer.isEnableOrientation());
		PamDialog.setDoubleValue(heading, defaultStreamer.getHeading(), "%3.1f");
		PamDialog.setDoubleValue(pitch, defaultStreamer.getPitch(), "%3.1f");
		PamDialog.setDoubleValue(roll, defaultStreamer.getRoll(), "%3.1f");
		
		interpolationPanel.setSelection(currentArray.getOriginInterpolation());

		ArraySensorFieldType[] sensorFields = ArraySensorFieldType.values();
		for (int i = 0; i < sensorFields.length; i++) {
			ArrayParameterType fieldType = defaultStreamer.getOrientationTypes(sensorFields[i]);
			String fieldDataBlock = defaultStreamer.getSensorDataBlocks(sensorFields[i]);
			sensorComponents[i].setParameterType(fieldType);
			if (fieldType == ArrayParameterType.SENSOR && fieldDataBlock != null) {
				sensorComponents[i].setDataBlock(PamController.getInstance().getDataBlockByLongName(fieldDataBlock));
			}
		}

		setStreamerLabels() ;
		enableControls();
	}

	private void setDoubleVal(JTextField textField, Double value) {
		if (value == null) {
			textField.setText("");
		}
		else {
			textField.setText(value.toString());
		}
	}

	private Double getDoubleValue(JTextField textField) {
		String txt = textField.getText();
		if (txt == null || txt.length() == 0) {
			return null;
		}
		Double val;
		try {
			val = Double.valueOf(txt);
			return val;
		}
		catch (NumberFormatException e) {
			showWarning("Invalid orientation information: " + txt);
			return null;
		}
	}

	@Override
	public boolean getParams() {
		try {
			defaultStreamer.setX(Double.valueOf(x.getText()));
			defaultStreamer.setY(Double.valueOf(y.getText()));
			defaultStreamer.setZ(-Double.valueOf(z.getText()));
			defaultStreamer.setDx(Double.valueOf(dx.getText()));
			defaultStreamer.setDy(Double.valueOf(dy.getText()));
			defaultStreamer.setDz(Double.valueOf(dz.getText()));		
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid parameter");
		}

		defaultStreamer.setStreamerName(streamerName.getText());
		int im = interpolationPanel.getSelection();
		
		System.out.println("GetParams: INTERPOLATION SELECTION: " + currentArray.getOriginInterpolation());

		if (im < 0) {
			return showWarning("Invalid interpolation selection");
		}
		currentArray.setOriginInterpolation(im);
		//			try {
		//				streamer.setBuoyId1(Integer.valueOf(buoyId.getText()));
		//			}
		//			catch (NumberFormatException e) {
		//				streamer.setBuoyId1(null);
		//			}
		HydrophoneLocator locator = HydrophoneLocators.getInstance().
				getSystem(localiserMethod.getSelectedIndex()).getLocator(currentArray, defaultStreamer);
		if (originPanel != null) 
			//			MasterLocator masterLocator = currentArray.getMasterLocator();
			//			int streamerIndex = currentArray.indexOfStreamer(streamer);
			//			if (streamerIndex < 0) {
			//				streamerIndex = currentArray.getNumStreamers();
			//			}
			//			masterLocator.setHydrophoneLocator(streamerIndex, locator);
			if (currentOriginMethod == null) {
				return showWarning("Not hydrophoneorigin method selected");
			}
		OriginDialogComponent mthDialogComponent = currentOriginMethod.getDialogComponent();
		if (mthDialogComponent != null) {
			if (mthDialogComponent.getParams() == false) {
				return false;
			}
		}

		//			defaultStreamer.setEnableOrientation(enableOrientation.isSelected());
		//			if (enableOrientation.isSelected()) {
		defaultStreamer.setHeading(getDoubleValue(heading));
		defaultStreamer.setPitch(getDoubleValue(pitch));
		defaultStreamer.setRoll(getDoubleValue(roll));
		//			}
		if (heading.isEnabled() && defaultStreamer.getHeading() == null) {
			return showWarning("You must enter a fixed value for the streamer heading");
		}
		if (pitch.isEnabled() && defaultStreamer.getPitch() == null) {
			return showWarning("You must enter a fixed value for the streamer pitch");
		}
		if (roll.isEnabled() && defaultStreamer.getRoll() == null) {
			return showWarning("You must enter a fixed value for the streamer roll");
		}


		/**
		 * We may have large lists of the streamers which we mwant to use the orientation data from or not. The enable orientation check box will
		 * enable or disable orientation for ALL streamers which are loaded into memory. 
		 */
		//			setEnableRotation(enableOrientation.isSelected(), defaultStreamer.getStreamerIndex());

		defaultStreamer.setHydrophoneOrigin(currentOriginMethod);
		defaultStreamer.setHydrophoneLocator(locator);
		defaultStreamer.setOriginSettings(currentOriginMethod.getOriginSettings());
		defaultStreamer.setLocatorSettings(locator.getLocatorSettings());

		ArraySensorFieldType[] sensorFields = ArraySensorFieldType.values();
		for (int i = 0; i < sensorFields.length; i++) {
			ArrayParameterType fieldType = sensorComponents[i].getParameterType();
			defaultStreamer.setOrientationTypes(sensorFields[i], fieldType);
			if (fieldType == ArrayParameterType.SENSOR) {
				PamDataBlock dataBlock = sensorComponents[i].getDataBlock();
				defaultStreamer.setSensorDataBlocks(sensorFields[i], dataBlock == null ? null : dataBlock.getLongDataName());
			}
		}
		return true;

	}

//	/**
//	 * Sets whether roatation is enabled and change for all streamers loaded into memory;
//	 * @params index- index of streamers to change
//	 * @param isRotationEnabled
//	 */
//	private void setEnableRotation(boolean isRotationEnabled, int index){
//		ListIterator<StreamerDataUnit> sIterator=ArrayManager.getArrayManager().getStreamerDatabBlock().getListIterator(0);
//		StreamerDataUnit sDataUnit;
//		if (sIterator!=null){
//			while (sIterator.hasNext()){
//				//				System.out.println("ChangeStreamer: "+index+ " "+isRotationEnabled);
//				sDataUnit=sIterator.next();
//				if (sDataUnit.getStreamerData().getStreamerIndex()==index){
//					sDataUnit.getStreamerData().setEnableOrientation(isRotationEnabled);
//				}
//			}
//		}
//	}

	@Override
	public void cancelButtonPressed() {
		defaultStreamer = null;
	}

	private class EnableOrientation implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			enableControls();
		}
	}

	private void enableControls() {
		//		heading.setEnabled(enableOrientation.isSelected());
		//		pitch.setEnabled(enableOrientation.isSelected());
		//		roll.setEnabled(enableOrientation.isSelected());

		ArraySensorFieldType[] sensorFields = ArraySensorFieldType.values();
		for (int i = 0; i < sensorFields.length; i++) {
			//			if (sensorComponents[i].getParameterType() == ArrayParameterType.FIXED)
		}
		enableField(z, ArraySensorFieldType.HEIGHT);
		enableField(heading, ArraySensorFieldType.HEADING);
		enableField(pitch, ArraySensorFieldType.PITCH);
		enableField(roll, ArraySensorFieldType.ROLL);

		if (currentOriginMethod != null) {
			interpolationPanel.setAllowedValues(currentOriginMethod.getAllowedInterpolationMethods());
		}
	}

	private void enableField(JTextField textField, ArraySensorFieldType fieldType) {
		int ord = fieldType.ordinal();
		textField.setEnabled(sensorComponents[ord].getParameterType() == ArrayParameterType.FIXED);
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}
	private class OriginMethodListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			newOriginMethod();
		}
	}

	public void newOriginMethod() {
		if (constructed == false) {
			return;
		}
		int methInd = originMethod.getSelectedIndex();
		if (methInd < 0) {
			return;
		}
		HydrophoneOriginSystem currentSystem = HydrophoneOriginMethods.getInstance().getMethod(this.originMethod.getSelectedIndex());
		currentOriginMethod = currentSystem.createMethod(currentArray, defaultStreamer);
		try {
			OriginSettings os = defaultStreamer.getOriginSettings(currentOriginMethod.getClass());
			if (os != null) {
				currentOriginMethod.setOriginSettings(os);
			}
		}
		catch (Exception e) {
			// will throw if it tries to set the wrong type of settings. 
		}

		OriginDialogComponent mthDialogComponent = currentOriginMethod.getDialogComponent();
		if (mthDialogComponent == null) {
			originPanel.removeAll();
			currentOriginComponent = null;
			pack();
		}
		else {
			JComponent newComponent = mthDialogComponent.getComponent(getOwner());
			if (currentOriginComponent != newComponent) {
				originPanel.removeAll();
				currentOriginComponent = newComponent;
				originPanel.add(newComponent);
				mthDialogComponent.setParams();

				pack();
			}
		}
		enableControls();
	}

	public JPanel getMainPanel() {
		return mainPanel;
	}

	public void setMainPanel(JPanel mainPanel) {
		this.mainPanel = mainPanel;
	}
}
