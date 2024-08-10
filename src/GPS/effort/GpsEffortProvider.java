package GPS.effort;

import java.util.ArrayList;
import java.util.List;

import GPS.GPSDataBlock;
import PamView.symbol.PamSymbolManager;
import PamguardMVC.dataSelector.DataSelector;
import dataMap.OfflineDataMap;
import effort.EffortDataUnit;
import effort.EffortProvider;

public class GpsEffortProvider extends EffortProvider {

	private GPSDataBlock gpsDataBlock;
	
	private GpsEffortSymbolManager effortSymbolManager;

	public GpsEffortProvider(GPSDataBlock parentDataBlock) {
		super(parentDataBlock);
		this.gpsDataBlock = parentDataBlock;
		effortSymbolManager = new GpsEffortSymbolManager(gpsDataBlock);
	}

	@Override
	public EffortDataUnit getEffort(long timeMilliseconds) {
		return makeSingleEffort();
	}

	@Override
	public List<EffortDataUnit> getAllEffortThings() {
		ArrayList<EffortDataUnit> effList = new ArrayList<>(1);
		effList.add(makeSingleEffort());
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
	
	private EffortDataUnit makeSingleEffort() {
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

}
