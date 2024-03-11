package clickDetector;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import clickDetector.ClickClassifiers.ClickTypeProvider;
import PamController.PamController;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import binaryFileStorage.BinaryHeader;
import binaryFileStorage.BinaryObjectData;
import binaryFileStorage.ModuleFooter;
import binaryFileStorage.ModuleHeader;

public class ClickBinaryModuleFooter extends ModuleFooter implements ManagedParameters {
	
	private static final long serialVersionUID = 6993205718227319638L;

	transient private ClickDetector clickDetector;
	
	private int[] typesCount;
	
	private String[] speciesList;
	
	transient private ClickTypeProvider clickIdentifier;
	
	private String clickDetectorName;

	public ClickBinaryModuleFooter(ClickDetector clickDetector) {
		super();
		this.clickDetector = clickDetector;
		clickDetectorName = clickDetector.getClickControl().getUnitName();
		setupTypeCount();
	}
	
	private ClickDetector findClickDetector() {
		if (clickDetector == null) {
			ClickControl pcu = (ClickControl) PamController.getInstance().findControlledUnit(ClickControl.class, clickDetectorName);
			clickDetector = pcu.clickDetector;
		}
		return clickDetector;
	}

	private void setupTypeCount() {
		clickIdentifier = findClickDetector().getClickControl().getClickTypeMasterManager(); 
		if (clickIdentifier == null) {
			return;
		}
		speciesList = clickIdentifier.getSpeciesList();
		if (speciesList != null) {
			typesCount = new int[speciesList.length];
		}
	}
	
	public boolean newClick(ClickDetection click) {
		if (clickIdentifier == null || typesCount == null) {
			return false;
		}
		if (click.getClickType() == 0) {
			return false;
		}
		int code = clickIdentifier.codeToListIndex(click.getClickType());
		if (code >= 0 && code < typesCount.length) {
			typesCount[code]++;
			return true;
		}
		return false;
	}
	
	@Override
	public byte[] getByteArray() {
		if (typesCount == null) {
			return null;
		}
		byte[] outputBytes = null;
		ByteArrayOutputStream bos;
		DataOutputStream dos = new DataOutputStream(bos = new ByteArrayOutputStream());
		try {
			dos.writeShort((short)typesCount.length);
			for (int i = 0; i < typesCount.length; i++) {
				dos.writeInt(typesCount[i]);
			}
			outputBytes = bos.toByteArray();
			dos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return outputBytes;
	}

	/* (non-Javadoc)
	 * @see binaryFileStorage.ModuleFooter#createFooter(binaryFileStorage.BinaryObjectData, binaryFileStorage.BinaryHeader, binaryFileStorage.ModuleHeader)
	 */
	@Override
	public boolean createFooter(BinaryObjectData binaryObjectData,
			BinaryHeader binaryHeader, ModuleHeader moduleHeader) {
		byte[] byteArray = binaryObjectData.getData();
		if (byteArray == null || byteArray.length < 2) {
			return false;
		}
		ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
		DataInputStream dis = new DataInputStream(bis);
		int nTypes = 0;
		try {
			nTypes = dis.readShort();
			typesCount = new int[nTypes];
			for (int i = 0; i < nTypes; i++) {
				typesCount[i] = dis.readInt();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * @return the typesCount
	 */
	public int[] getTypesCount() {
		return typesCount;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		try {
			Field field = this.getClass().getDeclaredField("clickDetectorName");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return clickDetectorName;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("speciesList");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return speciesList;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

	
}
