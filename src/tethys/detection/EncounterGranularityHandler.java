package tethys.detection;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.Map.Entry;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import nilus.Detection;
import nilus.Detections;
import nilus.SpeciesIDType;
import tethys.TethysControl;
import tethys.TethysTimeFuncs;
import tethys.output.StreamExportParams;
import tethys.output.TethysExportParams;
import tethys.pamdata.TethysDataProvider;
import tethys.species.DataBlockSpeciesManager;
import tethys.species.SpeciesMapItem;

/**
 * As with the binned Detections, this may generate multiple encounters
 * at the same time for different types of sounds. 
 * @author dg50
 *
 */
public class EncounterGranularityHandler extends GranularityHandler {


	private HashMap<String, Detection> currentDetections;
	private TethysDataProvider dataProvider;
	private DataBlockSpeciesManager speciesManager;
	private long maxGapMillis;

	public EncounterGranularityHandler(TethysControl tethysControl, PamDataBlock dataBlock,
			TethysExportParams tethysExportParams, StreamExportParams streamExportParams) {
		super(tethysControl, dataBlock, tethysExportParams, streamExportParams);

		dataProvider = dataBlock.getTethysDataProvider(tethysControl);
		speciesManager = dataBlock.getDatablockSpeciesManager();

		maxGapMillis = (long) (streamExportParams.encounterGapS*1000);

		currentDetections = new HashMap<String, Detection>();
	}

	@Override
	public void prepare(long timeMillis) {
		currentDetections.clear();
	}

	@Override
	public Detection[] addDataUnit(PamDataUnit dataUnit) {
		Detection[] completeDetections = checkCurrentEncounters(dataUnit.getTimeMilliseconds());
		// now look for new ones. First get the species of the dataUnit and find it in the hashmap
		String groupName = getCallGroupName(dataUnit);
		Detection det = currentDetections.get(groupName);
		if (det == null) {
			// need to make a new one. 
			det = new Detection();
			det.setStart(TethysTimeFuncs.xmlGregCalFromMillis(dataUnit.getTimeMilliseconds()));
			det.setEnd(TethysTimeFuncs.xmlGregCalFromMillis(dataUnit.getEndTimeInMilliseconds()));
			det.setCount(BigInteger.ONE);
			det.setChannel(BigInteger.valueOf(dataUnit.getChannelBitmap()));
			// this should always return something, so am going to crash if it doesn't. 
			// may revisit this later on if we've unassigned things we don't want to label
			// in which case they should be rejected earlier than this. 
			SpeciesMapItem speciesStuff = speciesManager.getSpeciesItem(dataUnit);
			SpeciesIDType species = new SpeciesIDType();
			species.setValue(BigInteger.valueOf(speciesStuff.getItisCode()));
			det.setSpeciesId(species);
			if (speciesStuff.getCallType() != null) {
				det.getCall().add(speciesStuff.getCallType());
			}
			currentDetections.put(groupName, det);
		}
		else {
			
			// add to current detection. Set new end time and increment count
			det.setEnd(TethysTimeFuncs.xmlGregCalFromMillis(dataUnit.getEndTimeInMilliseconds()));
			int count = det.getCount().intValue() + 1;
			det.setCount(BigInteger.valueOf(count));
			int chan = det.getChannel().intValue();
			chan |= dataUnit.getChannelBitmap();
			det.setChannel(BigInteger.valueOf(chan));
		}


		return completeDetections;
	}

	/**
	 * See if it's time to close off any encounters. 
	 * @param timeMilliseconds current time
	 * @return list of complete encounters. 
	 */
	private Detection[] checkCurrentEncounters(long timeMilliseconds) {
		Set<String> keys = currentDetections.keySet();
		int nGood = 0;
		Detection[] newDetections = new Detection[currentDetections.size()];
		Iterator<Entry<String, Detection>> iter = currentDetections.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, Detection> entry = iter.next();
			Detection aDet = entry.getValue();
			long detEnd = TethysTimeFuncs.millisFromGregorianXML(aDet.getEnd());
			if (timeMilliseconds-detEnd > maxGapMillis) {
				// only keep if it's got a min number of calls. 
				if (aDet.getCount().intValue() >= streamExportParams.minBinCount) {
					newDetections[nGood++] = aDet;
				}
				// remove from set. A new one will be created only when required. 
				iter.remove();
			}
		}

		if (nGood == 0) {
			return null;
		}
		else {
			return Arrays.copyOf(newDetections, nGood);
		}
	}


	@Override
	public Detection[] cleanup(long timeMillis) {
		// get everything still on the go. 
		return checkCurrentEncounters(timeMillis + maxGapMillis*10);
	}

	@Override
	protected boolean autoEffortFix(Detections detections, Detection det) {
		return expandEffort(detections, det);
	}


}
