package annotation.localise.targetmotion;

import javax.swing.JComponent;

import annotation.AnnotationSettingsPanel;
import annotation.handler.AnnotationOptions;
import clickDetector.localisation.ClickLocDialogPanel;
import clickDetector.localisation.GeneralGroupLocaliser;

public class TMSettingsPanel extends ClickLocDialogPanel implements AnnotationSettingsPanel {

	private TMAnnotationOptions annotationOptions;
	
	public TMSettingsPanel(GeneralGroupLocaliser groupLocaliser) {
		super(groupLocaliser);
	}

	@Override
	public JComponent getSwingPanel() {
		return getDialogComponent();
	}

	@Override
	public void setSettings(AnnotationOptions annotationOptions) {
		if (TMAnnotationOptions.class.isAssignableFrom(annotationOptions.getClass())) {
			this.annotationOptions = (TMAnnotationOptions) annotationOptions;
		}
		else {
			this.annotationOptions = new TMAnnotationOptions(annotationOptions);
		}
		this.setParams();
	}

	@Override
	public AnnotationOptions getSettings() {
		if (this.getParams() == false) {
			return null;
		}
		else {
			this.annotationOptions.setLocalisationParams(getGroupLocaliser().getClickLocParams());
			return annotationOptions;
		}
	}
	
	/**
	 * Get annotation  options. 
	 * @return annotation options
	 */
	public TMAnnotationOptions getAnnotationOptions() {
		return annotationOptions;
	}


}
