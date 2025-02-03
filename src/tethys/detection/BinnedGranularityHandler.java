package tethys.detection;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import java.util.Set;

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
import tethys.species.DataBlockSpeciesCodes;
import tethys.species.DataBlockSpeciesManager;
import tethys.species.DataBlockSpeciesMap;
import tethys.species.SpeciesMapItem;

/**
 * Binned granularity
 * Will have to collect different counts for each type of call for each datablock (if there 
 * are such things) so a little more complicated than might be expected. 
 * @author dg50
 *
 */
public class BinnedGranularityHandler extends GranularityHandler {

	private long binDurationMillis;
	
	private TethysDataProvider dataProvider;

	private DataBlockSpeciesManager speciesManager;
	
	private HashMap<String, Detection> currentDetections;

	public BinnedGranularityHandler(TethysControl tethysControl, PamDataBlock dataBlock,
			TethysExportParams tethysExportParams, StreamExportParams streamExportParams) {
		super(tethysControl, dataBlock, tethysExportParams, streamExportParams);
		
		binDurationMillis = (long) (streamExportParams.binDurationS*1000.);
		dataProvider = dataBlock.getTethysDataProvider(tethysControl);
		speciesManager = dataBlock.getDatablockSpeciesManager();
		
		currentDetections = new HashMap<String, Detection>();
	}

	@Override
	public void prepare(long timeMillis) {
//		long binStart = DetectionsHandler.roundDownBinStart(timeMillis, binDurationMillis);
//		startBin(binStart);
//		startBin(timeMillis);
		currentDetections.clear();
	}
	
//	private void startBin(long timeMillis) {
//		binStartMillis = timeMillis;
//		binEndMillis = binStartMillis + binDurationMillis;
//		/*
//		 *  now make a Detection object for every possible species that
//		 *  this might throw out. 
//		 */
//		ArrayList<String> speciesCodes = speciesManager.getAllSpeciesCodes();
//		String defaultCode = speciesManager.getDefaultSpeciesCode();
//		Detection det;
//		currentDetections.put(defaultCode, det = new Detection());
//		det.setStart(TethysTimeFuncs.xmlGregCalFromMillis(binStartMillis));
//		det.setEnd(TethysTimeFuncs.xmlGregCalFromMillis(binEndMillis));
//		det.setCount(BigInteger.ZERO);
//		det.setChannel(BigInteger.ZERO);
//		// add codes at end, just before output. 
//		if (speciesCodes != null) {
//			for (String code : speciesCodes) {
//				currentDetections.put(code, det = new Detection());
//				det.setStart(TethysTimeFuncs.xmlGregCalFromMillis(binStartMillis));
//				det.setEnd(TethysTimeFuncs.xmlGregCalFromMillis(binEndMillis));
//				det.setCount(BigInteger.ZERO);
//				det.setChannel(BigInteger.ZERO);
//			}
//		}
//	}

	@Override
	public Detection[] addDataUnit(PamDataUnit dataUnit) {
		Detection[] completeDetections = closeBins(dataUnit.getTimeMilliseconds());
		// now look for new ones. First get the species of the dataUnit and find it in the hashmap
		String groupName = getCallGroupName(dataUnit);
		Detection det = currentDetections.get(groupName);
		if (det == null) {
			// need to make a new one. 
			det = new Detection();
			long binStart = DetectionsHandler.roundDownBinStart(dataUnit.getTimeMilliseconds(), binDurationMillis);
			det.setStart(TethysTimeFuncs.xmlGregCalFromMillis(binStart));
			det.setEnd(TethysTimeFuncs.xmlGregCalFromMillis(binStart + binDurationMillis));
			det.setCount(BigInteger.ONE);
//			det.setChannel(BigInteger.valueOf(dataUnit.getChannelBitmap()));
			addChannelsToMap(det, dataUnit.getChannelBitmap());
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
			int count = det.getCount().intValue() + 1;
			det.setCount(BigInteger.valueOf(count));
			int chan = det.getChannel().intValue();
			chan |= dataUnit.getChannelBitmap();
			det.setChannel(BigInteger.valueOf(chan));
		}


		return completeDetections;
	}

	/**
	 * Called when units arrive after end of current bin, and also 
	 * at end of deployment output, to get that last bine. 
	 * @param timeMilliseconds
	 * @return
	 */
	private synchronized Detection[] closeBins(long timeMilliseconds) {
		Set<String> speciesKeys = currentDetections.keySet();
		int n = speciesKeys.size();
		int nGood = 0;
		DataBlockSpeciesMap speciesMap = speciesManager.getDatablockSpeciesMap();
		Detection detections[] = new Detection[n];
		Iterator<Entry<String, Detection>> iter = currentDetections.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, Detection> entry = iter.next();
			Detection det = entry.getValue();
			long detEnd = TethysTimeFuncs.millisFromGregorianXML(det.getEnd());
			if (timeMilliseconds < detEnd) {
				// we're not at the end of the bin, so carry on. 
				continue;
			}
			// we've reached the end of the bin, so remove it from the map
			iter.remove();
			// now decide if we want to keep it or not. 
			int callCount = det.getCount().intValue();
			if (callCount < Math.max(streamExportParams.minBinCount,1)) {
				continue; // won't add to output list
			}
			finaliseChannels(det);
			detections[nGood++] = det;
		}
		
		/*
		 * Clean up the end of the array and return detections that have enough calls.  
		 */
		if (nGood == 0) {
			return null;
		}
		detections = Arrays.copyOf(detections, nGood);
		return detections;
	}

	@Override
	public Detection[] cleanup(long timeMillis) {
		return closeBins(timeMillis);
	}

	@Override
	protected boolean autoEffortFix(Detections detections, Detection det) {
		return contractDetection(detections, det);
	}

}
