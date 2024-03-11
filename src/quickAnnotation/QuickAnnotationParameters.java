package quickAnnotation;

import java.awt.Color;
import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;

import PamController.PamSettingManager;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import difar.DifarControl;
import difar.DifarParameters;
import generalDatabase.lookupTables.LookupItem;
import generalDatabase.lookupTables.LookupList;

public class QuickAnnotationParameters implements Serializable, Cloneable, ManagedParameters {
	
	public static final long serialVersionUID = 10L;
	
	protected boolean exportClips;
	
	protected String folderName;
	
	protected String fileNamePrefix;
	
	protected  LookupList quickList;
	
	public boolean assignLabels;

	public LookupItem selectedClassification = null;

	public boolean shouldPopupDialog;
	
	public QuickAnnotationParameters(){
		exportClips = false;
		folderName = PamSettingManager.getInstance().getDefaultFile();
		folderName = folderName.substring(0,folderName.lastIndexOf(File.separator));
		fileNamePrefix = "annotation";
		quickList = new LookupList("");
		assignLabels = false;
		shouldPopupDialog = false;
	}
	
	public QuickAnnotationParameters clone() {
		try {
			QuickAnnotationParameters np = (QuickAnnotationParameters) super.clone();
			return np;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public LookupList getQuickList(QuickAnnotationModule qam) {
		if (quickList == null) {
			quickList = new LookupList(qam.getUnitName() + " list");
		}
		return quickList;
	}

	public void setQuickList(LookupList quickList) {
		this.quickList = quickList;
	}
	
	public String getFolderName(){
		if (folderName == null | folderName.isEmpty()) {
			folderName = PamSettingManager.getInstance().getDefaultFile();
			folderName = folderName.substring(0,folderName.lastIndexOf(File.separator));
		}
		return folderName;
	}
	
	public LookupList sorpList(QuickAnnotationModule qam) {
		int id, row, order;
		id = row = order = 1;
		boolean t = true;
		String topic = qam.getUnitName() + " list";
		LookupList ll = new LookupList(topic);
		ll.addItem(new LookupItem(id,row,topic,order,"unid","Unidentified sound",
				t,Color.BLACK,Color.BLACK,"o")); id++; row++; order++;
		ll.addItem(new LookupItem(id,row,topic,order,"Bm Ant-A", "Antarctic blue whale unit A",
				t,Color.BLUE,Color.BLUE, "o")); id++; row++; order++;
		ll.addItem(new LookupItem(id,row,topic,order,"Bm Ant-B", "Antarctic blue whale units A & B",
				t,Color.MAGENTA,Color.MAGENTA, "o")); id++; row++; order++;
		ll.addItem(new LookupItem(id,row,topic,order,"Bm Ant-Z", "Antarctic blue whale Z-call",
				t,Color.RED,Color.RED, "+")); id++; row++; order++;
		ll.addItem(new LookupItem(id,row,topic,order,"Bm D", "Blue whale D-call",
				t,Color.GREEN,Color.GREEN, "v")); id++; row++; order++;
		ll.addItem(new LookupItem(id,row,topic,order,"Bp 20", "Fin whale 20 Hz pulse",
				t,Color.ORANGE,Color.ORANGE, "s")); id++; row++; order++;
		ll.addItem(new LookupItem(id,row,topic,order,"Bp Downsweep", "Fin whale downsweep",
				t,Color.YELLOW,Color.YELLOW, "^")); id++; row++; order++;		
		return ll;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		try {
			Field field = this.getClass().getDeclaredField("exportClips");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return exportClips;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("fileNamePrefix");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return fileNamePrefix;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("quickList");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return quickList;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

}
