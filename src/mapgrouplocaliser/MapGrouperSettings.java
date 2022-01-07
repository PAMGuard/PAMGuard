package mapgrouplocaliser;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Hashtable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;
import PamView.paneloverlay.OverlayDataInfo;
import PamView.paneloverlay.overlaymark.MarkDataSelectorParams;
import PamView.paneloverlay.overlaymark.OverlayMarkDataInfo;
import PamguardMVC.PamDataBlock;
import annotation.handler.AnnotationChoices;

/**
 * Settings for Map Grouper. 
 * Trying to keep a lot of this as simple as possible so it can 
 * be used in other modules. 
 * @author dg50
 *
 */
public class MapGrouperSettings implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 0L;
	
//	private Hashtable<String, OverlayMarkDataInfo> overlayChoices = new Hashtable<>();
	private MarkDataSelectorParams markDataSelectorParams = new MarkDataSelectorParams();
	
	private AnnotationChoices annotationChoices = new AnnotationChoices();
	

	/**
	 * 
	 * @return Annotation configuration information
	 */
	public AnnotationChoices getAnnotationChoices() {
		if (annotationChoices == null) {
			annotationChoices = new AnnotationChoices();
		}
		annotationChoices.setAllowMany(false);
		return annotationChoices;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected MapGrouperSettings clone() {
		try {
			return (MapGrouperSettings) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	public OverlayMarkDataInfo getOverlayInfo(PamDataBlock dataBlock) {
		if (markDataSelectorParams == null) {
			markDataSelectorParams = new MarkDataSelectorParams();
		}
		return markDataSelectorParams.getOverlayInfo(dataBlock);
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		try {
			Field field = this.getClass().getDeclaredField("markDataSelectorParams");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return markDataSelectorParams;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

}
