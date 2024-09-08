package IshmaelLocator;

import java.sql.Types;

import IshmaelDetector.IshDetection;
import PamUtils.LatLong;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.EmptyTableDefinition;
//import pamDatabase.SQLLogging;
//import PamguardMVC.RecyclingDataBlock;
import generalDatabase.PamDetectionLogging;
import generalDatabase.PamTableItem;
import generalDatabase.SQLTypes;

public class IshLocSqlLogging extends PamDetectionLogging {
	IshLocControl ishLocControl;
	PamTableItem systemDate, durationSecs, secSinceStart, peakHeight;
	private PamTableItem latitude, longitude, refLatitude, refLongitude, x, y, z;
	
	public IshLocSqlLogging(IshLocControl ishDetControl, PamDataBlock pamDataBlock) 
	{
		super(pamDataBlock, UPDATE_POLICY_WRITENEW);
		this.ishLocControl = ishDetControl;
		
		EmptyTableDefinition tableDefinition = getTableDefinition();
		tableDefinition.addTableItem(latitude = new PamTableItem("Latitude", Types.DOUBLE)); 
		tableDefinition.addTableItem(longitude = new PamTableItem("Longitude", Types.DOUBLE));
		tableDefinition.addTableItem(refLatitude = new PamTableItem("ReferenceLatitude", Types.DOUBLE)); 
		tableDefinition.addTableItem(refLongitude = new PamTableItem("ReferenceLongitude", Types.DOUBLE));
		tableDefinition.addTableItem(x = new PamTableItem("x", Types.DOUBLE)); 
		tableDefinition.addTableItem(y = new PamTableItem("y", Types.DOUBLE));
		tableDefinition.addTableItem(z = new PamTableItem("z", Types.DOUBLE)); 
		
		tableDefinition.addTableItem(peakHeight = new PamTableItem("PeakHeight", Types.DOUBLE));
		tableDefinition.addTableItem(secSinceStart = new PamTableItem("SecSinceStart", Types.DOUBLE));
		tableDefinition.addTableItem(durationSecs      = new PamTableItem("DurationSeconds",      Types.DOUBLE));		

	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		super.setTableData(sqlTypes, pamDataUnit);

		IshDetection detUnit = (IshDetection)pamDataUnit;
		IshLocalisation ishLoc = (IshLocalisation) detUnit.getLocalisation();
		long dur = detUnit.getSampleDuration();					//in det samples (e.g., slices)
		float dRate = ishLocControl.ishLocProcessHy.getSampleRate();
		LatLong ll = ishLoc.getLatLong(0);
		LatLong llRef = ll.addDistanceMeters(-ishLoc.x, -ishLoc.y);
	
		latitude.setValue(ll.getLatitude());
		longitude.setValue(ll.getLongitude());
		refLatitude.setValue(llRef.getLatitude());
		refLongitude.setValue(llRef.getLongitude());
		x.setValue(ishLoc.x);
		y.setValue(ishLoc.y);
		z.setValue(ishLoc.z);
	}

	/* (non-Javadoc)
	 * @see generalDatabase.SQLLogging#createDataUnit(long, int)
	 */
	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds,
			int databaseIndex) {
		long duration = getDuration().getIntegerValue();
		double durationS = durationSecs.getDoubleValue();
		long endMillis = timeMilliseconds + (long) (durationS*1000);
		int chanMap = getChannelMap().getIntegerValue(); 
		long startSam = getStartSample().getIntegerValue();
		long durationSam = getDuration().getIntegerValue();
		double pHeight = peakHeight.getDoubleValue();
		IshDetection id = new IshDetection(timeMilliseconds, endMillis, (float)getLowFreq().getDoubleValue(), 
				(float)getHighFreq().getDoubleValue(), 0, pHeight, getPamDataBlock(), chanMap, startSam, durationSam);
		id.setDatabaseIndex(databaseIndex);
		IshLocalisation ishLoc = new IshLocalisation(id,192,chanMap);
		ishLoc.x = x.getDoubleValue();
		ishLoc.y = y.getDoubleValue();
		ishLoc.z = z.getDoubleValue();
		ishLoc.timeMsec = id.getTimeMilliseconds();
		id.setLocalisation(ishLoc);
		getPamDataBlock().addPamData(id);
		return id;
	}

}
