package videoRangePanel.vrmethods;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import videoRangePanel.VRCalibrationData;
import videoRangePanel.VRControl;
import videoRangePanel.VRHeightData;
import PamController.masterReference.MasterReferencePoint;
import PamUtils.LatLong;
import PamUtils.PamUtils;
import PamView.dialog.PamLabel;
import PamView.panel.PamBorderPanel;

/**
 * A panel which allows users to manually add a heading and roll angle.  
 * @author Doug Gillespie, modified Jamie Macaulay
 *
 */
public class ImageAnglePanel extends PamBorderPanel {

	private VRControl vrControl;
	
	private VRMethod vrMethod;
	
	private JTextField bearing;
	
	private JSpinner tiltSpinner;
	
	private JButton leftBig, leftSmall, rightBig, rightSmall;
	
	private JLabel imageWidthInfo, shoreInfo;
	
	private double smallStep = 0.1;
	
	private double bigStep = 1.0;

	private Double imageHeading;
	private Double tilt;

	private PamLabel tiltLabel; 


	public ImageAnglePanel(VRControl vrControl, VRMethod vrMethod) {
		super();
		this.vrControl = vrControl;
		this.vrMethod=vrMethod;
		setLayout(new FlowLayout(FlowLayout.LEFT));
//		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
//		flowLayout.setVgap(0);
		add(new PamLabel("Bearing  "));
		add(leftBig = new JButton("<<"));
		add(leftSmall = new JButton("<"));
		add(bearing = new JTextField(5));
		add(new PamLabel("\u00B0T"));
		add(rightSmall = new JButton(">"));
		add(rightBig = new JButton(">>"));
		add(tiltLabel=new PamLabel("Horizon Tilt (\u00B0)"));
		tiltSpinner = new JSpinner(new SpinnerNumberModel(0,-60,60,0.1));
		tiltSpinner.addChangeListener(new TiltListener());
		add(tiltSpinner);
		
		add(imageWidthInfo = new PamLabel(" "));
		add(shoreInfo = new PamLabel(" "));
		
		Dimension eD = bearing.getPreferredSize();
		Dimension sD = tiltSpinner.getPreferredSize();
		sD.height = eD.height;
//		sD.width = eD.width * 3/2;
//		tiltSpinner.setPreferredSize(sD);
		Dimension eD2 = tiltSpinner.getEditor().getPreferredSize();
		eD2.width = eD.width;
		eD2.height = eD.height;
		tiltSpinner.getEditor().setPreferredSize(eD2);
		
		leftBig.setToolTipText(String.format("Decrease angle by %.1f\u00B0", bigStep));
		leftSmall.setToolTipText(String.format("Decrease angle by %.1f\u00B0", smallStep));
		rightSmall.setToolTipText(String.format("Increase angle by %.1f\u00B0", smallStep));
		rightBig.setToolTipText(String.format("Increase angle by %.1f\u00B0", bigStep));
		
		leftBig.addMouseListener(new AngleButtonListener(leftBig, -bigStep));
		leftSmall.addMouseListener(new AngleButtonListener(leftSmall, -smallStep));
		rightSmall.addMouseListener(new AngleButtonListener(rightSmall, smallStep));
		rightBig.addMouseListener(new AngleButtonListener(rightBig, bigStep));
		
		bearing.addFocusListener(new AngleFocus());
//		angle.addKeyListener(AngleKe)
		
		setHeadingTxt(getImageHeading());
	}
	
	public void removeTiltSpinner(){
		this.remove(tiltSpinner);
		this.revalidate();
	}
	
	
	public Double getAngle() {
		try {
			return Double.valueOf(bearing.getText());
		}
		catch (NumberFormatException e) {
			return null;
		}
	}
	
	public void setHeadingTxt(Double angleValue) {
		if (angleValue != null) {
			bearing.setText(String.format("%3.1f", angleValue));
		}
	}
	
	/**
	 * Called for new calibration or new height data
	 */
	public void newCalibration() {
		sayHorizonInfo();
		sayShoreInfo();
	}
	
	/**
	 * Called when a new VR Method is selected
	 */
	public void newRefractMethod() {
		sayHorizonInfo();
		sayShoreInfo();
	}
	
	public void sayHorizonInfo() {
		int imageWidth = vrControl.getVRPanel().getImageWidth();
		if (imageWidth == 0) {
			imageWidthInfo.setText(" No Image");
			return;
		}
		VRHeightData heightData = vrControl.getVRParams().getCurrentheightData();
		if (heightData == null) {
			imageWidthInfo.setText(" No Height Data");	
			return;
		}
		VRCalibrationData calData = vrControl.getVRParams().getCurrentCalibrationData();
		if (calData == null) {
			imageWidthInfo.setText(" No Calibration Data");			
			return;
		}
		double imageAngle = imageWidth * calData.degreesPerUnit;
		double horizonDistance = vrControl.getRangeMethods().getCurrentMethod().getHorizonDistance(heightData.height);
		double horizonLength = horizonDistance * imageAngle * Math.PI / 180;
		
		imageWidthInfo.setText(String.format("Field of View = %.2f\u00B0; Horizon Distance = %.0fm; Horizon length = %.0f m",
				imageAngle, horizonDistance, horizonLength));
	}
	
	public void clearHorizonInfo(){
		imageWidthInfo.setText("");
	}
		
	private void sayShoreInfo() {
		Double shoreAngle = getAngle();
		if (shoreAngle == null) {
			shoreInfo.setText("");
			return;
		}
		LatLong globalReference = MasterReferencePoint.getLatLong();
		if (globalReference == null) {
			shoreInfo.setText("No master reference position - We've no idea where we are !");
			return;
		}
		double[] shoreIntercepts = {0,0,0};//vrControl.getShoreRanges();
		String str = "Land intercepts at ";
		for (int i = 0; i < shoreIntercepts.length; i++) {
			str += String.format("%.0f m  ", shoreIntercepts[i]);
		}
		shoreInfo.setText(str);
	}
	

	
	class TiltListener implements ChangeListener {

		public void stateChanged(ChangeEvent e) {
			setTilt((Double) tiltSpinner.getValue());
		}
		
	}
	
	class AngleButtonListener extends MouseAdapter  {

		private Object pressedButton;
		private double step;
		private int initialTime = 300;
		private int subsequentTimes = 100;
		Timer timer;
		public AngleButtonListener(Object pressedButton, double step) {
			super();
			this.pressedButton = pressedButton;
			this.step = step;
			timer = new Timer(subsequentTimes, new TimerAction());
			timer.setInitialDelay(initialTime);
		}
		

		@Override
		public void mousePressed(MouseEvent e) {
			super.mousePressed(e);
			timer.start();
			stepImageHeading(step);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			super.mouseReleased(e);
			timer.stop();
		}
		
		class TimerAction implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				stepImageHeading(step);
			}
		}
	}
	
	class AngleFocus extends FocusAdapter {

		@Override
		public void focusLost(FocusEvent e) {
			setImageHeading(getAngle());
		}
		
	}

	
	protected void stepImageHeading(double step) {
		Double currentAngle = getImageHeading();
		if (currentAngle == null) {
			currentAngle = 0.;
		}
		setImageHeading(PamUtils.constrainedAngle(currentAngle + step));
	}
	

	public Double getImageHeading() {
		return imageHeading;
	}
	
	public void setImageHeading(Double headingAngle) {
		this.imageHeading = headingAngle;
		setHeadingTxt(headingAngle);
		vrMethod.update(VRControl.HEADING_UPDATE);
	}
	
	
	

	public Double getTilt() {
		return tilt;
	}
	
	public void setTilt(Double tilt) {
		this.tilt = PamUtils.roundNumber(tilt, 0.1);
		vrMethod.update(VRControl.TILT_UPDATE);
	}
	
	public void setTiltLabel(Double tilt) {
		if (tilt==null) tiltLabel.setText("");
		else{
			String tiltStr=String.format("%.2f\u00B0", tilt);
			tiltLabel.setText("Image Tilt: "+tiltStr+ "   ");
		}
	}
	
	/**
	 * Clears all text on the panel
	 * 
	 */
	public void clearPanel(){
		setTiltLabel(null);
		clearHorizonInfo();
	}

	
}
