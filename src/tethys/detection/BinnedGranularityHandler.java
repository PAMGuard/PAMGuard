package tethys.detection;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import nilus.Detection;
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

	private double binDurationSeconds;
	
	private long binStartMillis, binEndMillis;

	private TethysDataProvider dataProvider;

	private DataBlockSpeciesManager speciesManager;
	
	private HashMap<String, Detection> currentDetections;

	public BinnedGranularityHandler(TethysControl tethysControl, PamDataBlock dataBlock,
			TethysExportParams tethysExportParams, StreamExportParams streamExportParams) {
		super(tethysControl, dataBlock, tethysExportParams, streamExportParams);
		
		binDurationSeconds = streamExportParams.binDurationS;
		dataProvider = dataBlock.getTethysDataProvider(tethysControl);
		speciesManager = dataBlock.getDatablockSpeciesManager();
		
		currentDetections = new HashMap<String, Detection>();
	}

	@Override
	public void prepare(long timeMillis) {
		long binStart = DetectionsHandler.roundDownBinStart(timeMillis, (long) (binDurationSeconds*1000));
		startBin(binStart);
	}
	
	private void startBin(long timeMillis) {
		binStartMillis = timeMillis;
		binEndMillis = binStartMillis + (long) (binDurationSeconds*1000.);
		/*
		 *  now make a Detection object for every possible species that
		 *  this might throw out. 
		 */
		ArrayList<String> speciesCodes = speciesManager.getAllSpeciesCodes();
		String defaultCode = speciesManager.getDefaultSpeciesCode();
		Detection det;
		currentDetections.put(defaultCode, det = new Detection());
		det.setStart(TethysTimeFuncs.xmlGregCalFromMillis(binStartMillis));
		det.setEnd(TethysTimeFuncs.xmlGregCalFromMillis(binEndMillis));
		det.setCount(BigInteger.ZERO);
		det.setChannel(BigInteger.ZERO);
		// add codes at end, just before output. 
		if (speciesCodes != null) {
			for (String code : speciesCodes) {
				currentDetections.put(code, det = new Detection());
				det.setStart(TethysTimeFuncs.xmlGregCalFromMillis(binStartMillis));
				det.setEnd(TethysTimeFuncs.xmlGregCalFromMillis(binEndMillis));
				det.setCount(BigInteger.ZERO);
				det.setChannel(BigInteger.ZERO);
			}
		}
	}

	@Override
	public Detection[] addDataUnit(PamDataUnit dataUnit) {
		Detection[] detections = null;
		if (dataUnit.getTimeMilliseconds() >= binEndMillis) {
			detections = closeBins(dataUnit.getTimeMilliseconds());
		}
		String speciesCode = speciesManager.getSpeciesCode(dataUnit);
		Detection det = currentDetections.get(speciesCode);
		if (det != null) {
			/*
			 * Increase the detection count
			 */
			int count = det.getCount().intValue();
			count++;
			det.setCount(BigInteger.valueOf(count));
			/*
			 * Add to the channel map too ... 
			 */
			int channel = det.getChannel().intValue();
			channel |= dataUnit.getChannelBitmap();
			det.setChannel(BigInteger.valueOf(channel));
		}
		return detections;
	}

	/**
	 * Called when units arrive after end of current bin, and also 
	 * at end of deployment output, to get that last bine. 
	 * @param timeMilliseconds
	 * @return
	 */
	private Detection[] closeBins(long timeMilliseconds) {
		Set<String> speciesKeys = currentDetections.keySet();
		int n = speciesKeys.size();
		int nGood = 0;
		DataBlockSpeciesMap speciesMap = speciesManager.getDatablockSpeciesMap();
		Detection detections[] = new Detection[n];
		for (String key : speciesKeys) {
			Detection det = currentDetections.get(key);
			int callCount = det.getCount().intValue();
			if (callCount < Math.max(streamExportParams.minBinCount,1)) {
				continue;
			}
			SpeciesMapItem speciesStuff = speciesMap.getItem(key); // should be non null!
			if (speciesStuff == null) {
				continue;
			}
			SpeciesIDType species = new SpeciesIDType();
			species.setValue(BigInteger.valueOf(speciesStuff.getItisCode()));
			det.setSpeciesId(species);
			if (speciesStuff.getCallType() != null) {
				det.getCall().add(speciesStuff.getCallType());
			}
			detections[nGood++] = det;
		}
		
		
		// finally, start new bins (not really needed on last call, but do anyway). 
		startBin(binEndMillis);
		
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

}
