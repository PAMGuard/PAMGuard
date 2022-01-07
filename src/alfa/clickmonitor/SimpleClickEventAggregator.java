package alfa.clickmonitor;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import PamDetection.AbstractLocalisation;
import PamDetection.LocContents;
import PamUtils.LatLong;
import alfa.clickmonitor.eventaggregator.ClickEventAggregate;
import alfa.clickmonitor.eventaggregator.EventAggregator;
import clickDetector.offlineFuncs.OfflineEventDataUnit;
import detectiongrouplocaliser.DetectionGroupDataUnit;

public class SimpleClickEventAggregator implements EventAggregator<DetectionGroupDataUnit, ClickEventAggregate> {

	private List<ClickEventAggregate> growingList = new LinkedList<>();
	
	private double minSeparationMetres;
	
	/**
	 * @param minSeparationMetres
	 */
	public SimpleClickEventAggregator(double minSeparationMetres) {
		super();
		this.minSeparationMetres = minSeparationMetres;
	}

	@Override
	public ClickEventAggregate aggregateData(DetectionGroupDataUnit detectionGroupDataUnit) {
		if (wantDataUnit(detectionGroupDataUnit) == false) {
			return null;
		}
		ClickEventAggregate currentAggregate = findInCurrent(detectionGroupDataUnit);
		if (currentAggregate != null) {
			/**
			 * Data is already part of an aggregate event, so no need to do anything at all. 
			 */
		}
		else {
			/*
			 * Try to match into existing developing aggregations. 
			 */
			currentAggregate = matchToAggregate(detectionGroupDataUnit);
			if (currentAggregate != null) {
				currentAggregate.addSubDetection(detectionGroupDataUnit);
			}
		}
		if (currentAggregate == null) {
			currentAggregate = new ClickEventAggregate(detectionGroupDataUnit);
			growingList.add(currentAggregate);
		}
		
		currentAggregate.setLastUpdateTime(detectionGroupDataUnit.getLastUpdateTime());
		
		if (wantAggregate(currentAggregate)) {
			return currentAggregate;
		}
		return null;
	}

	private boolean wantAggregate(ClickEventAggregate currentAggregate) {
		return true;
	}

	private ClickEventAggregate matchToAggregate(DetectionGroupDataUnit detectionGroupDataUnit) {
		ClickEventAggregate bestAggregate = null;
		double bestRange = Double.MAX_VALUE;
		Iterator<ClickEventAggregate> it = growingList.iterator();
		while (it.hasNext()) {
			ClickEventAggregate agg = it.next();
			Double d = minRangeToAggregate(detectionGroupDataUnit, agg);
			if (d == null) {
				continue;
			}
			if (d < bestRange) {
				bestRange = d;
				bestAggregate = agg;
			}
		}
		if (bestRange < minSeparationMetres) {
			return bestAggregate;
		}
		else {
			return null;
		}
	}
	
	private Double minRangeToAggregate(DetectionGroupDataUnit detectionGroupDataUnit, ClickEventAggregate agg) {
		AbstractLocalisation dLoc = detectionGroupDataUnit.getLocalisation();
		if (dLoc == null || dLoc.hasLocContent(LocContents.HAS_LATLONG) == false) {
			return null;
		}
		int nLL = dLoc.getAmbiguityCount();
		double closestOther = Double.MAX_VALUE;
		int nSub = agg.getSubDetectionsCount();
		for (int i = 0; i < nSub; i++) {
			AbstractLocalisation aggLoc = agg.getSubDetection(i).getLocalisation();
			int nALL = aggLoc.getAmbiguityCount();
			for (int is = 0; is < Math.min(nLL,nALL); is++) {
				LatLong uLL = dLoc.getLatLong(is);
				LatLong aLL = aggLoc.getLatLong(is);
				if (aLL == null || uLL == null) {
					continue;
				}
				double d = aLL.distanceToMetres(uLL);
				closestOther = Math.min(closestOther, d);
			}
		}
		return closestOther;
	}

	/**
	 * Do we want this data unit ? 
	 * Only if it's got a lat long already. 
	 * @param detectionGroupDataUnit
	 * @return true if we want to use this in the aggregator. 
	 */
	private boolean wantDataUnit(DetectionGroupDataUnit detectionGroupDataUnit) {
		AbstractLocalisation localisation = detectionGroupDataUnit.getLocalisation();
		if (localisation == null) {
			return false;
		}
		if (localisation.getLocContents().hasLocContent(LocContents.HAS_LATLONG)) {
			return true;
		}
		return false;
	}

	/**
	 * Find the current data unit in any of the currently growing aggregate lists.  
	 * @param dataUnit
	 * @return
	 */
	private ClickEventAggregate findInCurrent(DetectionGroupDataUnit dataUnit) {
		synchronized (growingList) {
			Iterator<ClickEventAggregate> it = growingList.iterator();
			while (it.hasNext()) {
				ClickEventAggregate agg = it.next();
				int foundUnit = agg.findSubdetectionInfo(dataUnit);
				if (foundUnit >= 0) {
					return agg;
				}
			}
		}
		return null;
	}

	@Override
	public void reset() {
		growingList.clear();
	}

}
