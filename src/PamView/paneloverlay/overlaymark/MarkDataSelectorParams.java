package PamView.paneloverlay.overlaymark;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Hashtable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamModel.parametermanager.PrivatePamParameterData;
import PamguardMVC.PamDataBlock;

/**
 * PArameter set for handling mark overlays selections. This does NOT have much to 
 * say about what's in and what's out of the mark, it's more about what the receiving
 * observer of the mark actually wants. 
 * @author dg50
 *
 */
public class MarkDataSelectorParams implements Serializable, Cloneable, ManagedParameters {

	private static final long serialVersionUID = 1L;

	private Hashtable<String, OverlayMarkDataInfo> overlayChoices;

	/**
	 * Get the information for a specific data block. 
	 * @param dataBlock data block
	 * @return Selection choices. 
	 */
	public OverlayMarkDataInfo getOverlayInfo(PamDataBlock dataBlock) {
		return getOverlayInfo(dataBlock.getLongDataName());
	}
	
	private OverlayMarkDataInfo getOverlayInfo(String dataName) {
		if (overlayChoices == null) {
			overlayChoices = new Hashtable<>();
		}
		OverlayMarkDataInfo dataInfo = overlayChoices.get(dataName);
		if (dataInfo == null) {
			dataInfo = new OverlayMarkDataInfo(dataName);
			overlayChoices.put(dataName, dataInfo);
		}
		return dataInfo;
	}
	
	/**
	 * Set information for a given data block. 
	 * @param dataBlock
	 * @param markDataInfo
	 */
	public void setOverlayInfo(PamDataBlock dataBlock, OverlayMarkDataInfo markDataInfo) {
		if (overlayChoices == null) {
			overlayChoices = new Hashtable<>();
		}
		if (markDataInfo == null) {
			overlayChoices.remove(dataBlock.getLongDataName());
		}
		else {
			overlayChoices.put(dataBlock.getLongDataName(), markDataInfo);
		}
	}

	@Override
	protected MarkDataSelectorParams clone() {
		try {
			MarkDataSelectorParams newOb = (MarkDataSelectorParams) super.clone();
			return newOb;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DISPLAY);
		try {
			Field field = this.getClass().getDeclaredField("overlayChoices");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return overlayChoices;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

}
