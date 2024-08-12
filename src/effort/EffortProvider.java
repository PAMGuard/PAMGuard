package effort;

import java.awt.Frame;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import Map.MapParametersDialog;
import PamController.PamController;
import PamView.GeneralProjector;
import PamView.PamSymbol;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.PamSymbolManager;
import PamView.symbol.SwingSymbolOptionsPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelectDialog;
import PamguardMVC.dataSelector.DataSelector;

/**
 * Set of functions that can be returned from any datablock which can 
 * give information about effort. For detectors with binary storage, they 
 * will return a standard binary store effort provider. Others, e.g. 
 * logger forms, acquisition, etc. can so something much more bespoke. <p>
 * For real time, will need quite different behaviour to offline. Datablocks
 * with a effortProvider are going to notify this and a local list will be 
 * kept of starts and ends for the entire operation period. This will be 
 * overridden for differeing offline scenarios and bespoke cases such as 
 * logger forms (so need to get a notification in here for every data unit too !)
 * @author dg50
 *
 */
public abstract class EffortProvider {

	private PamDataBlock parentDataBlock;
	private boolean isViewer;

	public EffortProvider(PamDataBlock parentDataBlock) {
		super();
		this.parentDataBlock = parentDataBlock;
		this.isViewer = PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW;
	}
	
	/**
	 * Notified at real time start. 
	 * @param timeMilliseconds
	 */
	public abstract void realTimeStart(long timeMilliseconds);
	
	/**
	 * notified at real time end
	 * @param timeMilliseconds
	 */
	public abstract void realTimeStop(long timeMilliseconds); ;
	
	/**
	 * Notified for real time data. 
	 * @param pamDataUnit
	 */
	public abstract void newData(PamDataUnit pamDataUnit); ;
	
	/**
	 * Get the effort for a specific time. 
	 * @param timeMilliseconds 
	 * @return Effort thing. Can be null if off effort. 
	 */
	public EffortDataUnit getEffort(long timeMilliseconds) {
		List<EffortDataUnit> allEfforts = getAllEffortThings();
		if (allEfforts == null) {
			return null;
		}
		Iterator<EffortDataUnit> it = allEfforts.iterator();
		while (it.hasNext()) {
			EffortDataUnit next = it.next();
			if (timeMilliseconds >= next.getEffortStart() && timeMilliseconds <= next.getEffortEnd()) {
				return next;
			}
		}
		return null;
	}
	
	/**
	 * Called when viewer data have been loaded for the parent datablock. 
	 */
	public abstract void viewerLoadData();
	
	/**
	 * Get all effort things. e.g. for binary data this is more or less a copy of 
	 * the datamap (perhaps new units without the gaps). 
	 * @return
	 */
	public abstract List<EffortDataUnit> getAllEffortThings();

	/**
	 * @return the parentDataBlock
	 */
	public PamDataBlock getParentDataBlock() {
		return parentDataBlock;
	}
	
	/**
	 * Get a data selector. For specialist effort modules, this will probably call
	 * through to the datablocks data selection system. Others, such as binaryEffortProvider
	 * can return null since they are either on or off. 
	 * @param selectorName
	 * @return 
	 */
	public abstract DataSelector getDataSelector(String selectorName);
	
	/**
	 * Get the name of this effort provider. 
	 */
	public abstract String getName();

	/**
	 * Get a symbol manager. For specialist effort modules, this will probably call
	 * through to the datablocks existing symbol management system. Others, such as binaryEffortProvider
	 * will need a chooser for the simple on/off data types. . 
	 * @return
	 */
	public abstract PamSymbolManager getSymbolManager(); 
	
	public PamSymbolChooser getSymbolChooser(String chooserName, GeneralProjector projector) {
		PamSymbolManager symbolManager = getSymbolManager();
		if (symbolManager == null) {
			return null;
		}
		return symbolManager.getSymbolChooser(chooserName, projector);
	}
	
	public PamSymbol getPamSymbol(String symbolChooserName, PamDataUnit dataUnit, GeneralProjector projector) {
		PamSymbolChooser chooser = getSymbolChooser(symbolChooserName, projector);
		return getPamSymbol(chooser, dataUnit);
	}

	public PamSymbol getPamSymbol(PamSymbolChooser chooser, PamDataUnit dataUnit) {
		if (chooser == null) {
			return null;
		}
		// possible that the data is an Effort unit that wraps a real data unit. Need to 
		// probably pass the original. 
		if (dataUnit instanceof EffortDataUnit) {
			PamDataUnit refData = ((EffortDataUnit) dataUnit).getReferenceDataUnit();
			if (refData != null) {
				dataUnit = refData;
			}
		}
		
		return chooser.getPamSymbol(chooser.getProjector(), dataUnit);
	}

	public boolean showOptionsDialog(Window parent, String observerName) {
		PamSymbolChooser symbolChooser = getSymbolChooser(observerName, null);
		if (symbolChooser == null) {
			return false;
		}
		SwingSymbolOptionsPanel panel = symbolChooser.getSwingOptionsPanel(null);
		if (panel == null) {
			return false;
		}
		DataSelectDialog dataSelectDialog = new DataSelectDialog(parent, parentDataBlock, null, symbolChooser);
		boolean ans = dataSelectDialog.showDialog();
		
		return ans;
	}

	/**
	 * @return the isViewer
	 */
	public boolean isViewer() {
		return isViewer;
	}

	public EffortDataUnit getLastEffort() {
		List<EffortDataUnit> all = getAllEffortThings();
		if (all == null || all.size() == 0) {
			return null;
		}
		return all.get(all.size()-1);
	}
	
}
