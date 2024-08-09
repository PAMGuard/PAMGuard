package effort;

import java.awt.Frame;
import java.awt.Window;
import java.util.List;

import Map.MapParametersDialog;
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
 * logger forms, acquisition, etc. can so something much more bespoke. 
 * @author dg50
 *
 */
public abstract class EffortProvider {

	private PamDataBlock parentDataBlock;

	public EffortProvider(PamDataBlock parentDataBlock) {
		super();
		this.parentDataBlock = parentDataBlock;
	}
	
	/**
	 * Get the effort for a specific time. 
	 * @param timeMilliseconds 
	 * @return Effort thing. Can be null if off effort. 
	 */
	public abstract EffortDataUnit getEffort(long timeMilliseconds);
	
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
	
}
