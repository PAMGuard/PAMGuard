package loggerForms;

import PamController.PamController;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;
import PamguardMVC.dataSelector.DataSelector;
import PamguardMVC.dataSelector.DataSelectorCreator;
import loggerForms.dataselect.FormDataSelCreator;
import loggerForms.monitor.FormsDataSelectorCreator;
/**
 * 
 * @author Graham Weatherup
 *
 */
public class FormsDataBlock extends PamDataBlock<FormsDataUnit> {
	
	private FormDescription formDescription;

	public FormsDataBlock(FormDescription formDescription, String dataName,
			PamProcess parentProcess, int channelMap) {
		super(FormsDataUnit.class, dataName, parentProcess, channelMap);
		this.formDescription = formDescription;
		setNaturalLifetime(600);
		setDataSelectCreator(new FormDataSelCreator(this, formDescription));
//		setBinaryDataSource(new FormsBinaryIO(formDescription.getFormsControl(), this));
//		setNaturalLifetimeMillis(60000);
	}

	public FormDescription getFormDescription() {
		return formDescription;
	}
	
//	@Override
//	public DataSelector getDataSelector(String selectorName, boolean allowScores, String selectorType) {
//		// TODO Auto-generated method stub
//		return super.getDataSelector(selectorName, allowScores, selectorType);
//	}
//
//	@Override
//	public DataSelectorCreator getDataSelectCreator() {
//
//		dataSelectorCreator = new FormDataSelCreator(this, formDescription);
//	}

	/**
	 * Override this for Logger forms so that they always save.
	 * @param pamDataUnit dataunit to consider
	 * @return true if data should be logged. 
	 */
	@Override
	public boolean getShouldLog(PamDataUnit pamDataUnit) {
		if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
			return false;
		}
		else {
			return true;
		}
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#addPamData(PamguardMVC.PamDataUnit)
	 */
	@Override
	public void addPamData(FormsDataUnit pamDataUnit) {
		super.addPamData(pamDataUnit);
		formDescription.dataBlockChanged();
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#updatePamData(PamguardMVC.PamDataUnit, long)
	 */
	@Override
	public void updatePamData(FormsDataUnit pamDataUnit, long timeMillis) {
		super.updatePamData(pamDataUnit, timeMillis);
		formDescription.dataBlockChanged();
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#removeOldUnitsT(long)
	 */
	@Override
	protected synchronized int removeOldUnitsT(long currentTimeMS) {
		int n = super.removeOldUnitsT(currentTimeMS);
		if (n > 0) {
			formDescription.dataBlockChanged();
		}
		return n;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#removeOldUnitsS(long)
	 */
	@Override
	protected synchronized int removeOldUnitsS(long mastrClockSample) {
		int n = super.removeOldUnitsS(mastrClockSample);
		if (n > 0) {
			formDescription.dataBlockChanged();
		}
		return n;
	}

}
