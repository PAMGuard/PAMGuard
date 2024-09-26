package annotation.dataselect;

import PamView.dialog.PamDialogPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelectParams;
import annotation.DataAnnotation;
import annotation.DataAnnotationType;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;

public abstract class ScalarDataSelector<TDataAnnotation extends DataAnnotation<?>> extends AnnotationDataSelector<TDataAnnotation> {
	
	/**
	 * @return the scalarDataParams
	 */
	public ScalarDataParams getScalarDataParams() {
		return scalarDataParams;
	}

	/**
	 * @param scalarDataParams the scalarDataParams to set
	 */
	public void setScalarDataParams(ScalarDataParams scalarDataParams) {
		this.scalarDataParams = scalarDataParams;
	}

	/**
	 * @return the useMinMax
	 */
	public int getUseMinMax() {
		return useMinMax;
	}

	public static final int USE_MINIMUM = 0x1;
	public static final int USE_MAXIMUM = 0x2;
	private int useMinMax;
	private ScalarDataParams scalarDataParams;

	public ScalarDataSelector(DataAnnotationType<TDataAnnotation> annotationType, PamDataBlock pamDataBlock,
			String selectorName, boolean allowScores, int useMinMax) {
		super(annotationType, pamDataBlock, selectorName, allowScores);
		this.useMinMax = useMinMax;
		scalarDataParams = new ScalarDataParams();
	}

	@Override
	protected double scoreData(PamDataUnit pamDataUnit, TDataAnnotation annotation) {
		if (annotation == null) {
			return 0;
		}
		double scalarVal = getScalarValue(annotation);
		if (isAllowScores()) {
			return scalarVal;
		}
		
		/**
		 * Might want to mess with these later to have a range as a veto rather than a selection
		 */
		if ((useMinMax & USE_MINIMUM) != 0) {
			if (scalarVal < scalarDataParams.minValue) {
				return 0;
			}
		}
		if ((useMinMax & USE_MAXIMUM) != 0) {
			if (scalarVal > scalarDataParams.maxValue) {
				return 0;
			}
		}
		return 1;
	}

	public abstract double getScalarValue(TDataAnnotation annotation);

	@Override
	public void setParams(DataSelectParams dataSelectParams) {
		if (dataSelectParams instanceof ScalarDataParams) {
			scalarDataParams = (ScalarDataParams) dataSelectParams;
		}
	}

	@Override
	public DataSelectParams getParams() {
		return scalarDataParams;
	}

	@Override
	public PamDialogPanel getDialogPanel() {
		return new ScalarDialogPanel(this);
	}

	@Override
	public DynamicSettingsPane<Boolean> getDialogPaneFX() {
		// TODO Auto-generated method stub
		return null;
	}

}
