package detectiongrouplocaliser;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Hashtable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;
import PamView.paneloverlay.overlaymark.OverlayMarkDataInfo;
import annotation.handler.AnnotationChoices;
import detectiongrouplocaliser.dialogs.DisplayOptionsHandler;

public class DetectionGroupSettings implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	private Hashtable<String, OverlayMarkDataInfo> overlayMarkInfo;

	private AnnotationChoices annotationChoices = new AnnotationChoices();
	
	private int offlineShowOption = DisplayOptionsHandler.SHOW_CURRENT;
	
	private boolean autoCrossedBearings = false;
	
	private int minCrossedBearings = 2;

	
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

	@Override
	protected DetectionGroupSettings clone() {
		try {
			return (DetectionGroupSettings) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}


	public OverlayMarkDataInfo getMarkerInfo(String longDataName) {
		if (overlayMarkInfo == null) {
			overlayMarkInfo = new Hashtable<>();
		}
		OverlayMarkDataInfo di = overlayMarkInfo.get(longDataName);
		if (di == null) {
			di = new OverlayMarkDataInfo(longDataName);
			overlayMarkInfo.put(longDataName, di);
		}
		return di;
	}

	/**
	 * @return the offlineShowOption
	 */
	public int getOfflineShowOption() {
		return offlineShowOption;
	}

	/**
	 * @param offlineShowOption the offlineShowOption to set
	 */
	public void setOfflineShowOption(int offlineShowOption) {
		this.offlineShowOption = offlineShowOption;
	}

	/**
	 * Automatic grouping and bearing crossing
	 * @return the autoCrossedBearings
	 */
	public boolean isAutoCrossedBearings() {
		return autoCrossedBearings;
	}

	/**
	 * Automatic grouping and bearing crossing
	 * @param autoCrossedBearings the autoCrossedBearings to set
	 */
	public void setAutoCrossedBearings(boolean autoCrossedBearings) {
		this.autoCrossedBearings = autoCrossedBearings;
	}

	/**
	 * Min groups for automatic grouping and bearing crossing (normally 2)
	 * @return the minCrossedBearings
	 */
	public int getMinCrossedBearings() {
		return minCrossedBearings;
	}

	/**
	 * Min groups for automatic grouping and bearing crossing (normally 2)
	 * @param minCrossedBearings the minCrossedBearings to set
	 */
	public void setMinCrossedBearings(int minCrossedBearings) {
		this.minCrossedBearings = minCrossedBearings;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		try {
			Field field = this.getClass().getDeclaredField("overlayMarkInfo");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return overlayMarkInfo;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}


}
