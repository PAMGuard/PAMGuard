/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation 
 * of marine mammals (cetaceans).
 *  
 * Copyright (C) 2006 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package PamController;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.commons.io.input.ClassLoaderObjectInputStream;

import PamModel.PamModel;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamView.dialog.warn.WarnOnce;


public class PamControlledUnitSettings implements Serializable, ManagedParameters {

	private static final long serialVersionUID = 6793059135083717980L; // never change this !!!

	long versionNo;

	private String unitType;

	private String unitName;

	private Object settings;
	
	private byte[] serialisedData;

//	private Class ownerClass; // this will not serialise well !!!!!!
	private String ownerClassName;

	//	private PamSettings owner;

	/**
	 * Make a settings object with unpacked data. This must still be used with old style psf files
	 * @param unitType
	 * @param unitName
	 * @param ownerClass
	 * @param versionNo
	 * @param settings
	 */
	public PamControlledUnitSettings(String unitType, String unitName, String ownerClass,
			long versionNo, Object settings) {
		this.versionNo = versionNo;
		this.unitType = unitType;
		this.unitName = unitName;
		this.ownerClassName = ownerClass;
		this.settings = settings;
	}

	/**
	 * Make a settings object with byte[] data from a psfx file or read from the database. These 
	 * will only be deserialized at the moment they are needed.<p>
	 * This is required by the new plugin system, whereby settings for plugins will be loaded from file
	 * before the java class for those settings has been loaded. 
	 * @param unitType
	 * @param unitName
	 * @param ownerClass
	 * @param versionNo
	 * @param serialisedData
	 */
	public PamControlledUnitSettings(String unitType, String unitName, String ownerClass,
			long versionNo, byte[] serialisedData) {
		this.versionNo = versionNo;
		this.unitType = unitType;
		this.unitName = unitName;
		this.ownerClassName = ownerClass;
		this.serialisedData = serialisedData;
	}

	public PamControlledUnitSettings(byte[] data) {
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 * @return The unit settings. 
	 */
	public Object getSettings() {
//		if (unitName.equals("NARW UDP Classifier")) {
//			System.out.println(unitName);
////			Object des = createFromByteArray(serialisedData);
////			try {
////				Class.forName("udpnarwclassifier.UdpNARWParams");
////			} catch (ClassNotFoundException e) {
////				// TODO Auto-generated catch block
////				e.printStackTrace();
////			}
//		}
		if (settings == null) {
			Object settingData = unpackSettings();
			if (settingData instanceof PamControlledUnitSettings) {
				// this happens unpacking a psfx
				this.settings = ((PamControlledUnitSettings) settingData).getSettings();
			}
			else {
				// this happens unpacking database settings. 
				this.settings = settingData;
			}
		}
		return settings;
	}

	/**
	 * Deserialise byte settings. 
	 */
	private Object unpackSettings() {
		if (serialisedData == null) {
			return settings;
		}
		return unpackByteArray(serialisedData);
//		ByteArrayInputStream inputBuffer = new ByteArrayInputStream(serialisedData);
//		Object readObject = null;
//		boolean deserialisationError = false;
//		try {
//			ObjectInputStream in = new ObjectInputStream(inputBuffer);
//			readObject = in.readObject();
//			in.close();
//		}
//		catch (IOException ex) {
//			System.out.println("Database deserialisation IOException: " + ex.getMessage());
////			dsWarning.addMissingClass(ex.getMessage());
//			deserialisationError = true;
//			//						continue;
//		}
//		catch (ClassNotFoundException ex) {
//			System.out.println("Database deserialisation Class not found: " + ex.getMessage());
////			dsWarning.addMissingClass(ex.getMessage());
////			cx.printStackTrace();
//		}
//		return readObject;
	}

	/**
	 * @param settings the settings to set
	 */
	public void setSettings(Object settings) {
		this.settings = settings;
	}

	/**
	 * 
	 * @return The unit name
	 */
	public String getUnitName() {
		return unitName;
	}

	/**
	 * @param unitName the unitName to set
	 */
	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}

	/**
	 * 
	 * @return The unit type
	 */
	public String getUnitType() {
		return unitType;
	}

	/**
	 * 
	 * @return the version number for these unit settings. 
	 */
	public long getVersionNo() {
		return versionNo;
	}

	/**
	 * Find out if this settings unit is that for the given type and name
	 * @param unitType Unit Type
	 * @param unitName Unit Name
	 * @return true if these settings correspond to that unit. 
	 */
	public boolean isSettingsOf(String unitType, String unitName) {
		return (this.unitType.equals(unitType) && this.unitName.equals(unitName));
	}

	/**
	 * Find out if a set of settings are compatible with another set. 
	 * @param p another set of PamControlledUnitSettings. 
	 * @return true if they have the same name type and version number
	 */
	public boolean isSame(PamControlledUnitSettings p) {
		return (this.versionNo == p.versionNo
				&& this.unitType.equals(p.unitType) && this.unitName
				.equals(p.unitName));
	}

	/**
	 * Get a byte array of the serialised data in this object. Note that this is 
	 * generally the entire object, not it's settings, though that may change in a
	 * future release. 
	 * @return a byte array of the serialised data in this object
	 */
	public byte[] getSerialisedByteArray() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(bos);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		try {
//			System.out.printf("Writing %s %s\n", this.getUnitType(), getUnitName());
			oos.writeObject(this);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		byte[] byteArray = bos.toByteArray();

		try {
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return byteArray;
	}

	/**
	 * Get a byte array of the serialised data but with a small
	 * header giving the unitType, unitName, versionNO and the size
	 * of the serialised data object
	 * @return byte array. 
	 */
	public byte[] getNamedSerialisedByteArray() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream oos = null;
		oos = new DataOutputStream(bos);

		byte[] serialisedData = getSerialisedByteArray();
		try {
			oos.writeUTF(unitType);
			if (unitName == null) {
				oos.writeUTF(unitType);
			}
			else {
				oos.writeUTF(unitName);
			}
			oos.writeLong(versionNo);
			oos.writeInt(serialisedData.length);
			oos.write(serialisedData);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



		byte[] byteArray = bos.toByteArray();

		try {
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return byteArray;
	}

	/**
	 * Used when unpacking data from psfx files. These seem to have the type, name, etc
	 * encoded, then repeated in the serialised data which is the entire PamControlledUnitSettings
	 * object, which contains those same data again. This will fail if loading a plugin module which 
	 * is not actually present
	 * @param byteArray
	 * @return
	 */
	public static PamControlledUnitSettings createFromNamedByteArray(byte[] byteArray) {
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(byteArray));
		//		oos.writeUTF(unitType);
		//		oos.writeUTF(unitName);
		//		oos.writeLong(versionNo);
		//		oos.writeInt(serialisedData.length);
		String unitType;
		String unitName;
		long versionNumber;
		int dataLength;
		byte[] data;
		try {
			unitType = dis.readUTF();
			unitName = dis.readUTF();
			versionNumber = dis.readLong();
			dataLength = dis.readInt();
			data = new byte[dataLength];
			dis.read(data);
			/**
			 * New, don't deserilaise until somthing calls getSettings(). 
			 */
			PamControlledUnitSettings pcus = new PamControlledUnitSettings(unitType, unitName, unitName, versionNumber, data);
			return pcus;
//			return createFromByteArray(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Create an object from a serialised byte array
	 * @param byteArray byte array
	 * @return new object (or null if invalid byte array)
	 */
	public static Object unpackByteArray(byte[] byteArray) {
		ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
//		ObjectInputStream ois;
		ClassLoaderObjectInputStream ois;
		ClassLoader classLoader;
		if (PamModel.getPamModel() != null) {
			classLoader = PamModel.getPamModel().getClassLoader();
		}
		else {
			classLoader = new URLClassLoader(new URL[0], PamControlledUnitSettings.class.getClassLoader()) {

			    @Override
				public void addURL(URL url) {
			        super.addURL(url);
			    }
			};
		}
		try {
//			ois = new ObjectInputStream(bis);
			ois = new ClassLoaderObjectInputStream(classLoader, bis);
			Object ob =  ois.readObject();
//			PamControlledUnitSettings pcus = (PamControlledUnitSettings) ob;
			ois.close();
			return ob;
		} 
		catch (InvalidClassException e) {
			System.out.println("Invalid class in Control setting");
			System.out.println(e.getMessage());
			String title = "Error loading module";
			String msg = "<p>This psfx is trying to load <em>" + e.classname + "</em> but is having problems.</p><br>" +
					"<p>This may be because of an incompatiblility between the version of the module in the psf and " +
					"the version of the module currently in PAMGuard.  Check the console window for an error message " +
					"with the version details.</p><br>" +
					"<p>The module may or may not load, and even if it " +
					"loads it may have lost it's settings.  Please check before performing any analysis.</p>";
			String help = null;
			int ans = WarnOnce.showWarning(title, msg, WarnOnce.WARNING_MESSAGE, help);
			
			return null;
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			System.out.println("Class not found: " + e.getMessage());
//						e.printStackTrace();
			return null;
		}
		//		return null;
	}

	/**
	 * 
	 * Get the class type of the module that owned these settings. 
	 * Gets used when importing settings so that modules can be easily identified. 
	 * @return the ownerClass
	 */
	public String getOwnerClassName() {
		return ownerClassName;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}

	@Override
	public String toString() {
		return String.format("Type %s; Name %s, Data ", getUnitType(), getUnitName()) + getSettings();
	}

}
