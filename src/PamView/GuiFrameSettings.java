package PamView;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;

import PamController.PamControlledUnit;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamModel.parametermanager.PrivatePamParameterData;

/**
 * Information about GUI frames. Is created once when settings are saved and used once
 * when settings are loaded. 
 * @author dg50
 *
 */
public class GuiFrameSettings implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	private ArrayList<UnitFrameInfo> unitFrameInfo = new ArrayList<>();
	
	public void addUnitInfo(PamControlledUnit pamControlledUnit) {
		unitFrameInfo.add(new UnitFrameInfo(pamControlledUnit.getUnitType(), pamControlledUnit.getUnitName(), pamControlledUnit.getFrameNumber()));
	}
	
	public int getUnitFrameNumber(PamControlledUnit pamControlledUnit) {
		UnitFrameInfo ufi = findUnitInfo(pamControlledUnit);
		if (ufi == null) {
			return -1;
		}
		return ufi.guiFrame;
	}
	
	private UnitFrameInfo findUnitInfo(PamControlledUnit pamControlledUnit) {
		for (UnitFrameInfo ufi:unitFrameInfo) {
			if (ufi.unitType.equals(pamControlledUnit.getUnitType()) &&
					ufi.unitName.equals(pamControlledUnit.getUnitName())) {
				return ufi;
			}
		}
		return null;
	}
	
	@Override
	protected GuiFrameSettings clone() {
		try {
			return (GuiFrameSettings) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return new GuiFrameSettings();
		}
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DISPLAY);
		try {
			Field field = this.getClass().getDeclaredField("unitFrameInfo");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return unitFrameInfo;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

	
	class UnitFrameInfo implements Serializable, Cloneable, ManagedParameters {
		/**
		 * 
		 */
		private static final long serialVersionUID = 2765126298876039947L;
		
		String unitType;
		public UnitFrameInfo(String unitType, String unitName, int guiFrame) {
			super();
			this.unitType = unitType;
			this.unitName = unitName;
			this.guiFrame = guiFrame;
		}
		String unitName;
		int guiFrame;
		
		@Override
		public PamParameterSet getParameterSet() {
			PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DISPLAY);
			try {
				Field field = this.getClass().getDeclaredField("guiFrame");
				ps.put(new PrivatePamParameterData(this, field) {
					@Override
					public Object getData() throws IllegalArgumentException, IllegalAccessException {
						return guiFrame;
					}
				});
			} catch (NoSuchFieldException | SecurityException e) {
				e.printStackTrace();
			}
			try {
				Field field = this.getClass().getDeclaredField("unitName");
				ps.put(new PrivatePamParameterData(this, field) {
					@Override
					public Object getData() throws IllegalArgumentException, IllegalAccessException {
						return unitName;
					}
				});
			} catch (NoSuchFieldException | SecurityException e) {
				e.printStackTrace();
			}
			try {
				Field field = this.getClass().getDeclaredField("unitType");
				ps.put(new PrivatePamParameterData(this, field) {
					@Override
					public Object getData() throws IllegalArgumentException, IllegalAccessException {
						return unitType;
					}
				});
			} catch (NoSuchFieldException | SecurityException e) {
				e.printStackTrace();
			}
			return ps;
		}

	}

	
}
