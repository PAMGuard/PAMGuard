package GPS.effort;

import java.util.ArrayList;
import java.util.List;

import GPS.GPSDataBlock;
import PamView.symbol.PamSymbolManager;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelector;
import dataMap.OfflineDataMap;
import effort.EffortDataUnit;
import effort.EffortProvider;

public class GpsEffortProvider extends EffortProvider {

	private GPSDataBlock gpsDataBlock;
	
	private GpsEffortSymbolManager effortSymbolManager;
	
	private EffortDataUnit realTimeData;

	public GpsEffortProvider(GPSDataBlock parentDataBlock) {
		super(parentDataBlock);
		this.gpsDataBlock = parentDataBlock;
		effortSymbolManager = new GpsEffortSymbolManager(gpsDataBlock);
	}

	@Override
	public EffortDataUnit getEffort(long timeMilliseconds) {
		return getSingleEffort();
	}

	@Override
	public List<EffortDataUnit> getAllEffortThings() {
		EffortDataUnit singleEff = getSingleEffort();
		ArrayList<EffortDataUnit> effList = new ArrayList<>(1);
		if (singleEff != null) {
			effList.add(singleEff);
		}
		return effList;
	}

	@Override
	public DataSelector getDataSelector(String selectorName) {
		return null;
	}

	@Override
	public PamSymbolManager getSymbolManager() {
		return effortSymbolManager;
	}
	
	private EffortDataUnit getSingleEffort() {
		if (!isViewer()) {
			return realTimeData;
		}
		OfflineDataMap dataMap = gpsDataBlock.getPrimaryDataMap();
		if (dataMap == null) {
			return null;
		}
		EffortDataUnit effData = new EffortDataUnit(this, null, dataMap.getFirstDataTime(), dataMap.getLastDataTime());
		return effData;
	}

	@Override
	public String getName() {
		return gpsDataBlock.getDataName();
	}

	@Override
	public void realTimeStart(long timeMilliseconds) {
		// Do nothing
	}

	@Override
	public void realTimeStop(long timeMilliseconds) {
		// Do nothing
	}

	@Override
	public void newData(PamDataUnit pamDataUnit) {
		if (realTimeData == null) {
			realTimeData = new EffortDataUnit(this,null,pamDataUnit.getTimeMilliseconds(), pamDataUnit.getTimeMilliseconds());
		}
		else {
			realTimeData.setEffortEnd(pamDataUnit.getTimeMilliseconds());
		}
	}

	@Override
	public void viewerLoadData() {
		// TODO Auto-generated method stub
		
	}

}
