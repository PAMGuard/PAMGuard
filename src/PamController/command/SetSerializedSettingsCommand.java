package PamController.command;

import java.util.ArrayList;

import org.postgresql.shaded.com.ongres.scram.common.bouncycastle.pbkdf2.Arrays;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;

/**
 * 
 * Didn't work. Commands are so set up around String data that there is little hope
 * for this to work. 
 * Deploy serialized settings. These are settings that would normally be held in 
 * the psfx manager and had been turned into a byte array with 
 * PamControlledUnitSettings.getNamedSerialisedByteArray(). Here they can be
 * turned back into valid settings using PamControlledUnitSettings.createFromNamedByteArray(...)
 * then the appropriate owner found and the settings restored. 
 * First written for use with batch processor which needs to send updated array data. 
 * @author dg50
 *
 */
public class SetSerializedSettingsCommand extends ExtCommand {

	public static String commandId = "serializedsettings";
	
	private SetSerializedSettingsCommand() {
		super(commandId, true);
//		setBinary(true);
	}

//	@Override
//	public String executeBinary(byte[] command) {
//		/* 
//		 * this will now be a horrid mix of string and binary data. Will need to strip 
//		 * off the first three spaces. 
//		 */
//		System.out.println("SetSErializedSettings: Unpacking binary command");
//		String asStr = new String(command);
//		int sp = asStr.indexOf(":::");
//		if (sp < 0) {
//			return null;
//		}
//		byte[] binData = Arrays.copyOfRange(command, sp+3, command.length);
//
//		// try to read that now as an object again. 
//		PamControlledUnitSettings ps = PamControlledUnitSettings.createFromNamedByteArray(binData);
//		if (ps == null) {
//			return "SetSerializedSettingsCommand: Unable to unpack serialised settings data";
//		}
//		// now find the owner and send it the settings. 
//		PamSettingManager settingManager = PamSettingManager.getInstance();  
//		ArrayList<PamSettings> owner = settingManager.findPamSettings(ps.getUnitType(), ps.getUnitName());
//		if (owner == null || owner.size() == 0) {
//			return String.format("SetSerializedSettingsCommand: unable to find owner of %s:%s", ps.getUnitType(), ps.getUnitName());
//		}
//		PamSettings aSet = owner.get(0);
//		boolean ok = aSet.restoreSettings(ps);
//
//		return String.format("SetSerializedSettingsCommand: sucessfully applied settings to  %s:%s %s", ps.getUnitType(), 
//				ps.getUnitName(), ok ? "TRUE" : "ERROR");
//	}

	@Override
	public String execute(String command) {
		// TODO Auto-generated method stub
		return null;
	}


}
