package Map;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.Timer;

import GPS.GPSControl;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.LatLong;
import PamUtils.MapContourValues;
import PamView.PamGui;
import userDisplay.UserDisplayControl;


public class MapController extends PamControlledUnit implements PamSettings {

//	private MapParameters defaultMapParameters = new MapParameters();
	GetMapFile getMapFile = new GetMapFile();
	ArrayList<MapContourValues> contourPoints = new ArrayList<MapContourValues>();
	boolean mapContoursAvailable = false;
	boolean mapContoursDeliveredToMap = false;
	public static final String unitType = "Map";
	MapTabPanelControl mapTabPanelControl;
	MapProcess mapProcess;
	
	private MapControlSettings mapControlSettings = new MapControlSettings();

	private Timer timer;

	private static LatLong mouseClickLatLong;

	static public final int MOUSE_PAN = 0;
	static public final int MOUSE_MEASURE = 1;

	/*
	 * Stuff to make maps available for user displays. 
	 */
	private MapDisplayProvider mapDisplayProvider;
	
	private ArrayList<SimpleMap> currentMaps = new ArrayList<SimpleMap>();
	
	public MapController(String name) {

		super(unitType, name);
		addPamProcess(mapProcess = new MapProcess(this));
		setTabPanel(mapTabPanelControl = new MapTabPanelControl(this));
//		timer = new Timer(1000, new TimerListener());
//		timer.start();
		PamSettingManager.getInstance().registerSettings(this);
		
		mapDisplayProvider = new MapDisplayProvider(this);
		UserDisplayControl.addUserDisplayProvider(mapDisplayProvider);
	}
	
	@Override
	public boolean removeUnit() {
		UserDisplayControl.removeDisplayProvider(mapDisplayProvider);
		return super.removeUnit();
	}
//	class TimerListener implements ActionListener {
//
//		public void actionPerformed(ActionEvent e) {
//			PamArray currentArray = ArrayManager.getArrayManager().getCurrentArray();
//			if (currentArray == null){
//				return;
//			}
//			if (currentArray.getHydrophoneLocator().isStatic() && currentArray.getFixedPointReferenceBlock() != null) {
//				// static array
//				PamDataUnit pamDataUnit = currentArray.getFixedPointReferenceBlock().getLastUnit();
//				if (pamDataUnit!= null) {
//					GpsData gpsData = ((GpsDataUnit) pamDataUnit).getGpsData();
//					GpsTextDisplay gpsTextDisplay = mapTabPanelControl.getSimpleMap().gpsTextPanel;
//					SimpleMap simpleMap = mapTabPanelControl.getSimpleMap();
//					MapPanel mapPanel = simpleMap.mapPanel;
////					gpsTextDisplay.updateGpsTextAreaWithStaticData(gpsData);
//					gpsTextDisplay.updateGpsTextArea();
//					gpsTextDisplay.newShipGps();
////					gpsTextDisplay.setPixelsPerMetre(mapPanel.getPixelsPerMetre());
//					//					gpsTextDisplay.setShipPosition(mapPanel.getRectProj().
//					//							getCoord3d(gpsData.getLatitude(), gpsData.getLongitude(), 0));
//					simpleMap.repaint();
//				}
//			}
//			else {
//				// towed array. 
//				mapTabPanelControl.getSimpleMap().timerActions();
//			}
//
//		}
//
//	}
	



	class menuMapFile implements ActionListener {

		Frame parentFrame;

		public menuMapFile(Frame parentFrame) {
			super();
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent ev) {
			//getMapFile.openMapDialog();
			if (getMapFile.openMapDialog() != null){
				contourPoints=getMapFile.getMapContours();
				mapContoursAvailable=true;
			}

			else{

				mapContoursAvailable=false;	
			}

//			System.out.println("MapController: mapContoursAvailable: " + mapContoursAvailable);

		}
	}	

	JMenuBar mapTabMenu = null;
	private boolean initialisationComplete;
	@Override
	public JMenuBar getTabSpecificMenuBar(Frame parentFrame, JMenuBar standardMenu, PamGui pamGui) {

		// start bymaking a completely new copy.
		//if (mapTabMenu == null) {
		mapTabMenu = pamGui.makeGuiMenu();
		for (int i = 0; i < mapTabMenu.getMenuCount(); i++) {
			if (mapTabMenu.getMenu(i).getText().equals("Display")) {

				//mapTabMenu.remove(mapTabMenu.getMenu(i));

				JMenu aMenu = mapTabPanelControl.simpleMap.createDisplayMenu(parentFrame);
				aMenu.setText(getUnitName());

				addRelatedMenuItems(parentFrame, aMenu, "Map");

				mapTabMenu.add(aMenu, i+1);
				break;
			}
		}
		//}
		return mapTabMenu;
	}

	public ArrayList<MapContourValues> getContourPoints() {
		if(mapContoursAvailable){
			mapContoursDeliveredToMap=true;
			return contourPoints;

		}
		else{
			return null;
		}


	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#notifyModelChanged(int)
	 */
	@Override
	public void notifyModelChanged(int changeType) {

		super.notifyModelChanged(changeType);

		for (SimpleMap aMap:currentMaps) {
			aMap.notifyModelChanged(changeType);
		}
		
	}

	public int getMaxInterpolationTime() {
		int interpTime = 0;
		GPSControl gpsControl = (GPSControl) PamController.getInstance().findControlledUnit(GPSControl.class, null);
		if (gpsControl != null) {
			interpTime = gpsControl.getGpsParameters().readInterval * 2;
		}
		return Math.max(interpTime, 120);
	}


	public void addMouseAdapter(MouseAdapter mouseAdapter){
		this.getTabPanel();


	}

	public MapTabPanelControl getMapTabPanelControl() {
		return mapTabPanelControl;
	}

	public double getMapStuff(){
		return mapTabPanelControl.getSimpleMap().getMapPanel().getPixelsPerMetre();
	}

	public void addMouseAdapterToMapPanel(MouseAdapter mouseAdapter){
		//System.out.println("SimController::addMouseAdapterToMap");
		for (SimpleMap eachMap:currentMaps) {
			eachMap.addMouseAdapterToMapPanel(mouseAdapter);
		}
//		mapTabPanelControl.addMouseAdapterToMapPanel(mouseAdapter);


	}

	public void mapCanScroll(boolean b) {
		mapTabPanelControl.mapCanScroll(b);
		//System.out.println("Mapontroller::mapCanScroll = " + b);
	}

	/**
	 * 
	 */
	public LatLong getMapCentreLatLong() {
		// TODO Auto-generated method stub
		return mapTabPanelControl.simpleMap.getMapPanel().getMapCentreDegrees();
	}

	public void refreshDetectorList(){
		mapTabPanelControl.refreshDetectorList();
	}

	public static LatLong getMouseClickLatLong() {
		return mouseClickLatLong;
	}

	protected static void setMouseClickLatLong(LatLong mouseClickLatLong) {
		MapController.mouseClickLatLong = mouseClickLatLong.clone();
	}

	private int mouseMoveAction = MOUSE_PAN;
	protected void setMouseMoveAction(int mouseMoveAction) {
		this.mouseMoveAction = mouseMoveAction;
	}

	public int getMouseMoveAction() {
		return mouseMoveAction;
	}
	
	protected void addSimpleMap(SimpleMap aMap) {
		currentMaps.add(aMap);
	}
	
	protected void removeSimpleMap(SimpleMap aMap) {
		currentMaps.remove(aMap);
	}
	@Override
	public Serializable getSettingsReference() {
		return mapControlSettings;
	}
	
	@Override
	public long getSettingsVersion() {
		return MapControlSettings.serialVersionUID;
	}
	
	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		if (pamControlledUnitSettings.getSettings().getClass() != MapControlSettings.class) {
			return false;
		}
		this.mapControlSettings = (MapControlSettings) pamControlledUnitSettings.getSettings();
		return true;
	}
}
