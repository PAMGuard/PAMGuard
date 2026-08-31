package deepWhistle;


/**
 * JavaFX settings pane for DeepWhistleParameters.
 * <p>
 * All mask selection and mask specific controls are handled by the parent
 * {@link MaskedFFTSettingsPane}. The DeepWhistle mask specific controls (e.g.
 * the confidence spinner) are provided by {@link DeepWhistleMaskPane} which is
 * shown when the DeepWhistle mask is selected.
 */
public class DeepWhistleSettingsPane extends MaskedFFTSettingsPane {

	public DeepWhistleSettingsPane(Object owner) {
		super(owner);
	}

}
