package networkTransfer.emulator;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;

import PamController.PamControlledUnitSettings;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;
import PamUtils.LatLong;

public class EmulatorParams implements Cloneable, Serializable, ManagedParameters {


	public static final long serialVersionUID = 1L;
	
	LatLong gpsCentre = new LatLong(52.5, 3.087);
	
	double circleRadius = 1000;
	
	int nBuoys = 10;
	
	int firstBuoyId = 201;
	
	int statusIntervalSeconds = 30;
	
	boolean[] usedBlocks;
	
	public boolean[] getUsedBlocks(int nBlocks) {
		if (usedBlocks == null) {
			usedBlocks = new boolean[nBlocks];
		}
		else if (usedBlocks.length < nBlocks) {
			usedBlocks = Arrays.copyOf(usedBlocks, nBlocks);
		}
		return usedBlocks;
	}


	@Override
	protected EmulatorParams clone() {
		try {
			return (EmulatorParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		try {
			Field field = this.getClass().getDeclaredField("circleRadius");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return circleRadius;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("firstBuoyId");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return firstBuoyId;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("gpsCentre");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return gpsCentre;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("nBuoys");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return nBuoys;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("statusIntervalSeconds");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return statusIntervalSeconds;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("usedBlocks");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return usedBlocks;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

	
}
