package loggerForms.effort;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import PamController.PamController;
import PamView.symbol.PamSymbolManager;
import PamguardMVC.PamDataBlock;
import PamguardMVC.dataSelector.DataSelector;
import dataMap.OfflineDataMap;
import effort.EffortDataUnit;
import effort.EffortProvider;
import loggerForms.FormDescription;
import loggerForms.FormsDataBlock;
import loggerForms.FormsDataUnit;

public class LoggerEffortProvider extends EffortProvider {

	private FormsDataBlock formsDataBlock;

	public LoggerEffortProvider(FormsDataBlock parentDataBlock) {
		super(parentDataBlock);
		this.formsDataBlock = parentDataBlock;
		FormDescription formsDescription = parentDataBlock.getFormDescription();
	}

	@Override
	public EffortDataUnit getEffort(long timeMilliseconds) {
		ListIterator<FormsDataUnit> iterator = formsDataBlock.getListIterator(timeMilliseconds, 0xFFFFFFFF, PamDataBlock.MATCH_BEFORE, PamDataBlock.POSITION_BEFORE);
		FormsDataUnit currentUnit = null;
		FormsDataUnit nextUnit = null;
				
		if (iterator.hasNext()) {
			currentUnit = iterator.next();
		}
		if (iterator.hasNext()) {
			nextUnit = iterator.next();
		}
		if (currentUnit == null) {
			return null;
		}
		long endTime = getEndTime(currentUnit, nextUnit);
		
		return new FormsEffortUnit(currentUnit, endTime);
	}

	private long getEndTime(FormsDataUnit currentUnit, FormsDataUnit nextUnit) {
		Long end = currentUnit.getSetEndTime();
		if (end != null) {
			return end;
		}
		if (nextUnit == null) {
			return getLastDatasetTime();
		}
		else {
			return nextUnit.getTimeMilliseconds();
		}
	}
	
	@Override
	public List<EffortDataUnit> getAllEffortThings() {
		ArrayList<EffortDataUnit> allList = new ArrayList();
		ListIterator<FormsDataUnit> iterator = formsDataBlock.getListIterator(0);
		FormsDataUnit currentUnit = null;
		FormsDataUnit nextUnit = null;
		if (iterator.hasNext()) {
			currentUnit = iterator.next();
		}
		while (iterator.hasNext()) {
			nextUnit = iterator.next();
			long end = getEndTime(currentUnit, nextUnit);
			allList.add(new FormsEffortUnit(currentUnit, end));
			currentUnit = nextUnit;
		}
		if (currentUnit != null) {
			long end = getEndTime(currentUnit, null);
			allList.add(new FormsEffortUnit(currentUnit, end));
		}
		return allList;		
	}

	@Override
	public DataSelector getDataSelector(String selectorName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PamSymbolManager getSymbolManager() {
		return formsDataBlock.getPamSymbolManager();
	}
	
	/**
	 * Get the last time of any data in this dataset from any data map. 
	 * @return
	 */
	private long getLastDatasetTime() {
		long lastTime = Long.MIN_VALUE;
		ArrayList<PamDataBlock> allData = PamController.getInstance().getDataBlocks();
		for (PamDataBlock aBlock : allData) {
			OfflineDataMap dataMap = aBlock.getPrimaryDataMap();
			if (dataMap != null) {
				lastTime = Math.max(lastTime, dataMap.getLastDataTime());
			}
		}
		
		return lastTime;
	}

}
