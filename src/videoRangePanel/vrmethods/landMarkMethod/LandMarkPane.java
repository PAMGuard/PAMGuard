package videoRangePanel.vrmethods.landMarkMethod;

import PamController.SettingsPane;
import PamUtils.LatLong;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;
import pamViewFX.fxNodes.utilityPanes.LatLongPane;
import videoRangePanel.VRControl;

/**
 * Pane which allows users to create a land mark. 
 * @author Jamie Macaulay 
 *
 */
public class LandMarkPane extends SettingsPane<LandMark>  {
	
	/**
	 * The main pane. 
	 */
	private	Pane mainPane; 
	
	/**
	 * Reference ot the VR control
	 */
	private VRControl vrControl;

	private TextField name;
	private TextField heightTxtBox;
	private TextField bearing;
	private TextField pitch;

	
	private LatLongPane latLonPane;

	private LatLongPane latLonOriginPane;

	private TextField heightOriginTxtBox;

	private TabPane tabPane;

	public LandMarkPane(Object ownerWindow) {
		super(ownerWindow);
		this.mainPane=createPane() ; 
	}
	
	/**
	 * Create the LandMark input pane. 
	 */
	private Pane createPane() {
		
		PamBorderPane mainPane= new PamBorderPane(); 
		mainPane.setPadding(new Insets(10,10,10,10));
		
		PamVBox nameHolder = new PamVBox(); 
		nameHolder.setSpacing(5);
		name = new TextField();
		name.setPrefColumnCount(15);
		Label labelName=new Label("Landmark Name"); 
		PamGuiManagerFX.titleFont2style(labelName);
		//labelName.setFont(PamGuiManagerFX.titleFontSize2);
		
		Label lndMrkMeasureName=new Label("Landmark Measurements"); 
		PamGuiManagerFX.titleFont2style(lndMrkMeasureName);
		//lndMrkMeasureName.setFont(PamGuiManagerFX.titleFontSize2);
		
		nameHolder.getChildren().addAll(labelName, name, lndMrkMeasureName); 
		nameHolder.setPadding(new Insets(0,0,5,0));
		
		// the latitude/longitude landmark pane
		
		Label lndmarkLabel = new Label("LandMark Location"); 
		
		latLonPane= new LatLongPane("Latitude/Longitude");
		
		PamHBox height= new PamHBox();
		height.setAlignment(Pos.CENTER_LEFT);
		height.setSpacing(5);
		heightTxtBox=new TextField(); 
		height.getChildren().addAll(new Label("Height"), heightTxtBox, new Label("m")); 
		
		PamVBox latlonholder = new PamVBox();
		latlonholder.setSpacing(5); 
		latlonholder.getChildren().addAll( latLonPane.getContentNode(), height); 
		latlonholder.setPadding(new Insets(5,0,5,0));
		
		//the bearing, pitch, and origin latitude and longitude. 
		
		Label bearingLabel = new Label("Bearings to LandMark"); 
		
		PamGridPane bearingPane = new PamGridPane(); 
		bearingPane.setHgap(5);
		bearingPane.setVgap(5);
		bearingPane.add(new Label("Bearing"), 0, 0);
		bearingPane.add(bearing=new TextField(), 1, 0);
		bearing.setPrefColumnCount(7);
		bearingPane.add(new Label("\u00b0 (0\u00b0 to 360\u00b0)"), 2, 0);

		bearingPane.add(new Label("Pitch"), 0, 1);
		bearingPane.add(pitch=new TextField(), 1, 1);
		pitch.setPrefColumnCount(7);
		bearingPane.add(new Label("\u00b0 (-90\u00b0 to 90\u00b0)"), 2, 1);

		latLonOriginPane= new LatLongPane("Origin Latitude/Longitude");
		
		Label orignLabel = new Label("Bearings to LandMark"); 
		
		PamHBox heightOrigin= new PamHBox();
		heightOrigin.setAlignment(Pos.CENTER_LEFT);
		heightOrigin.setSpacing(5);
		heightOriginTxtBox=new TextField(); 
		heightOrigin.getChildren().addAll(new Label("Origin Height"), heightOriginTxtBox, new Label("m")); 
		
		
		PamVBox bearingHolder = new PamVBox();
		bearingHolder.setSpacing(5); 
		bearingHolder.getChildren().addAll(bearingPane, orignLabel, latLonOriginPane.getContentNode(), heightOrigin); 
		bearingHolder.setPadding(new Insets(5,0,5,0));
		
		// tab pane 
		tabPane = new TabPane();
		Tab tab = new Tab("Lat/Long LandMark"); 
		tab.setContent(latlonholder);
		tabPane.getTabs().add(tab); 

		tab = new Tab("Bearing LandMark"); 
		tab.setContent(bearingHolder);
		tabPane.getTabs().add(tab); 
		
		mainPane.setTop(nameHolder); 
		mainPane.setCenter(tabPane); 
		
		return mainPane; 
		
	}

	@Override
	public String getName() {
		return "LandMark";
	}

	@Override
	public Node getContentNode() {
		return this.mainPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
		
	}
	
	
	/*
	 *Check whether a field is empty or not.  
	 */
	private boolean isEmptyString(String string){
		if (string == null || string.trim().equals("")){
			return true;
		}
		return false; 
	}
	
	
	@Override
	public LandMark getParams(LandMark vrLandMarkData) {
		
		if (vrLandMarkData==null) {
			vrLandMarkData= new LandMark(); 
		}
		
		boolean gps=false;
		boolean angle=false;
		try {
			if (name.getText()==null) return null;
			vrLandMarkData.setName(name.getText());
		}
		catch (NumberFormatException e) {
			System.out.println("No Name");
			return null;
		}
		//Do we have GPS info?
		try {
			LatLong lndMrkPosition= this.latLonPane.getParams(null);
			
			if (lndMrkPosition==null || isEmptyString(this.heightTxtBox.getText())){
				vrLandMarkData.setPosition(null);
			}
			else {
				lndMrkPosition.setHeight(Double.valueOf(heightTxtBox.getText()));
				vrLandMarkData.setPosition(lndMrkPosition);
				gps=true;
			}
		}
		catch (NumberFormatException e) {
			e.printStackTrace();
			System.out.println("No LandMark GPS position - there was a number format error");
		}
		
		//If no GPS info do we have angle info?
		try {
			if (!gps) {
				Double bearingAng=Double.valueOf(bearing.getText());
				if (bearingAng<0 || bearingAng>360) return null;
				vrLandMarkData.setBearing(bearingAng);
				Double pitchAng=Double.valueOf(pitch.getText());
				if (pitchAng<-90 || pitchAng>90) return null;
				vrLandMarkData.setPitch(pitchAng);

				LatLong positionOrigin=this.latLonOriginPane.getParams(null); 
				if (positionOrigin!=null) {
					positionOrigin.setHeight(Double.valueOf(this.heightOriginTxtBox.getText()));
					vrLandMarkData.setLatLongOrigin(positionOrigin);
					angle=true;
				}
				else angle=false; 
			}
		}
		catch (NumberFormatException e) {
			e.printStackTrace();
			System.out.println("No bearing info");
		}
		
		if (!gps && !angle){
			PamDialogFX.showWarning(null, "No Landmarks", " The landmark data is either incomplete or data was enterred incorrectly");
			return null; 
		}
		
		if (gps && angle){
			PamDialogFX.showWarning(null, "Multiple Landmark Type", " There are two entries for the landmark. "
					+ "The GPS point will be used in preference to the bearing. If you wish the bearing to be used in calculations "
					+ "delete the GPS location of the Landmark"); 
			return null; 
		}
		
		
		return vrLandMarkData;
	}

	/**
	 * Set params 
	 */
	public void setParams(LandMark vrLandMarkData) {
		
		if (vrLandMarkData.getName()!=null){
			name.setText(vrLandMarkData.getName());
		}
		
		if (vrLandMarkData.getPosition()!=null){
			latLonPane.setParams(vrLandMarkData.getPosition());
			heightTxtBox.setText(String.format("%.3f", vrLandMarkData.getHeight()));
			this.tabPane.getSelectionModel().select(0); //select the correct tab
		}
		
		if (vrLandMarkData.getBearing() !=null) bearing.setText(String.format("%.7f", vrLandMarkData.getBearing()));
		if (vrLandMarkData.getPitch() !=null) pitch.setText(String.format("%.7f", vrLandMarkData.getPitch()));
		if (vrLandMarkData.getLatLongOrigin() !=null){
			latLonOriginPane.setParams(vrLandMarkData.getLatLongOrigin());
			heightOriginTxtBox.setText(String.format("%.3f", vrLandMarkData.getHeightOrigin()));
			this.tabPane.getSelectionModel().select(1); //select the correct tab.
		}
//		//if origin is not null then force the user to use that as their gps origin.
//		if (origin!=null){
//			measurementLat.setText(String.format("%.7f", origin.getLatitude()));
//			measurementLong.setText(String.format("%.7f", origin.getLongitude()));
//			measurementHeight.setText(String.format("%.2f", origin.getHeight()));
//			measurementLat.setEnabled(false);
//			measurementLong.setEnabled(false);
//			measurementHeight.setEnabled(false);
//		}
		
	}

}
