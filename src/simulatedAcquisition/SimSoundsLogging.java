package simulatedAcquisition;

import java.sql.Types;

import pamMaths.PamVector;
import simulatedAcquisition.sounds.SimSound;
import Array.ArrayManager;
import GPS.GpsData;
import PamUtils.LatLong;
import PamUtils.PamUtils;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

public class SimSoundsLogging extends SQLLogging {

	private SimSoundDataBlock simSoundDataBlock;
	private PamTableItem lat, lon, height, head, slant, sound;
	private PamTableItem x, y, z;
	protected SimSoundsLogging(SimSoundDataBlock simSoundDataBlock) {
		super(simSoundDataBlock);
		this.simSoundDataBlock = simSoundDataBlock;
		PamTableDefinition tableDef = new PamTableDefinition("Simulated Sounds", SQLLogging.UPDATE_POLICY_WRITENEW);
		tableDef.addTableItem(lat = new PamTableItem("Latitude", Types.DOUBLE));
		tableDef.addTableItem(lon = new PamTableItem("Longitude", Types.DOUBLE));
		tableDef.addTableItem(height = new PamTableItem("Height", Types.DOUBLE));
		tableDef.addTableItem(x = new PamTableItem("x", Types.DOUBLE));
		tableDef.addTableItem(y = new PamTableItem("y", Types.DOUBLE));
		tableDef.addTableItem(z = new PamTableItem("z", Types.DOUBLE));
		tableDef.addTableItem(head = new PamTableItem("Heading", Types.DOUBLE));
		tableDef.addTableItem(slant = new PamTableItem("Slant", Types.DOUBLE));
		tableDef.addTableItem(sound = new PamTableItem("Sound", Types.CHAR, 20));
		
		setTableDefinition(tableDef);
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		SimSoundDataUnit ssdu = (SimSoundDataUnit) pamDataUnit;
		SimSound simSound = ssdu.getSimSound();
		LatLong ll = simSound.getLatLong();
		if (ll != null) {
			lat.setValue(ll.getLatitude());
			lon.setValue(ll.getLongitude());
		}
		else {
			lat.setValue(null);
			lon.setValue(null);
		}
		height.setValue(simSound.getHeight());
		PamVector headVec = simSound.getHeading();
		if (head != null) {
			headVec = headVec.getUnitVector();
			double h = Math.toDegrees(Math.atan2(headVec.getElement(0), headVec.getElement(1)));
			double s = Math.toDegrees(Math.asin(headVec.getElement(2)));
			head.setValue(h);
			slant.setValue(s);
		}
		else {
			head.setValue(null);
			slant.setValue(null);
		}
		sound.setValue(simSound.getSimSignal().getName());
		

		GpsData arrayLatLong = null;
		try {
			arrayLatLong = ArrayManager.getArrayManager().getCurrentArray().getStreamer(0).getHydrophoneOrigin().getLastStreamerData().getGpsData();
		}
		catch (Exception e) {
		}
		if (arrayLatLong == null) {
			arrayLatLong = new GpsData();
		}
		double xP, yP, zP;
		if (ll == null) {
			ll = new LatLong();
		}
		xP = arrayLatLong.distanceToMetresX(ll);
		yP = arrayLatLong.distanceToMetresY(ll);
		zP = simSound.getHeight() - arrayLatLong.getHeight();
		x.setValue(PamUtils.roundNumber(xP, .1));
		y.setValue(PamUtils.roundNumber(yP, .1));
		z.setValue(PamUtils.roundNumber(zP, .1));
			
	}
	
	

}
