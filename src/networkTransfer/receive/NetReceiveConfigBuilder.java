package networkTransfer.receive;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;

import PamController.PSFXReadWriter;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingsGroup;
import PamController.UsedModuleInfo;
import networkTransfer.send.NetworkSender;

/**
 * Static functions, available to call from separate applications over application bridges..
 * 
 * Exposes an easy way for users to generate a network receiving configuration corresponding to their network sending configuration
 */
public class NetReceiveConfigBuilder {

	public static void generateAndSaveNetRxPsfx(String existingConfigFilePath, String newConfigPath, String ipAddress, String userId,String password, int portNumber, String baseTopic) throws Exception {
		
		if(!Paths.get(existingConfigFilePath).toFile().exists()) {
			throw new Exception("Configuration "+existingConfigFilePath+" does not exist. Cannot generate a base station file for this config.");
		}
		
		NetworkReceiveParams netRxParams = generateNetRxParams(ipAddress,userId,password,portNumber,baseTopic);
		
		PamSettingsGroup existingPSG = loadPSG(existingConfigFilePath);
		
		PamSettingsGroup newPSG = removeTxAndEnsureCorrectNetRx(existingPSG, netRxParams);
		
		PSFXReadWriter.getInstance().writePSFX(newConfigPath,newPSG);
	}
	
	public static boolean configContainsNetworkSender(String existingConfigFilePath) throws Exception {
		if(!Paths.get(existingConfigFilePath).toFile().exists()) {
			throw new Exception("Configuration "+existingConfigFilePath+" does not exist. Cannot determine if this config has a network sender configured.");
		}
		
		PamSettingsGroup existingPSG = loadPSG(existingConfigFilePath);
		ArrayList<UsedModuleInfo> usedModuleInfo = existingPSG.getUsedModuleInfo();
		for(UsedModuleInfo usedModule:usedModuleInfo) {
			if(usedModule.getUnitType().equals(NetworkSender.UNIT_TYPE)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static String[] listConfigModules(String existingConfigFilePath) throws Exception {
		if(!Paths.get(existingConfigFilePath).toFile().exists()) {
			throw new Exception("Configuration "+existingConfigFilePath+" does not exist. Cannot list active modules.");
		}
		
		PamSettingsGroup existingPSG = loadPSG(existingConfigFilePath);
		ArrayList<UsedModuleInfo> usedModuleInfo = existingPSG.getUsedModuleInfo();
		String[] moduleList = new String[usedModuleInfo.size()];
		for(int i=0;i<usedModuleInfo.size();i++) {
			moduleList[i] = usedModuleInfo.get(i).getUnitType()+" - "+usedModuleInfo.get(i).getUnitName();
		}
		return moduleList;
	}
	
	private static PamSettingsGroup loadPSG(String existingConfigFilePath) {
		File standardConfigFilePath = new File(existingConfigFilePath);
		PamSettingsGroup existingPamSettings = PSFXReadWriter.getInstance().loadFileSettings(standardConfigFilePath);
		return existingPamSettings;
	}
	
	private static PamSettingsGroup removeTxAndEnsureCorrectNetRx(PamSettingsGroup existingPamSettings, NetworkReceiveParams netRxParams) {
		
		PamSettingsGroup newSettingsGroup = new PamSettingsGroup(System.currentTimeMillis());

		for (PamControlledUnitSettings unitSettings : existingPamSettings.getUnitSettings()) {
			//Skip the network sender and the network receiver. Net receiver will be added manually to ensure correctness.
			if (unitSettings.getUnitType().equals(NetworkSender.UNIT_TYPE) || unitSettings.getUnitType().equals(NetworkReceiver.unitTypeString)) {
				continue;
			}
			if (unitSettings.isSettingsOf(PamController.unitType,PamController.unitName)) {
				@SuppressWarnings("unchecked")
				ArrayList<UsedModuleInfo> usedModuleInfo = (ArrayList<UsedModuleInfo>) unitSettings.getSettings();
				ArrayList<UsedModuleInfo> updatedModuleInfo = new ArrayList<UsedModuleInfo>();
				for(UsedModuleInfo moduleInfo:usedModuleInfo) {
					//Skip the network sender and the network receiver. Net receiver will be added manually to ensure correctness.
					if(!moduleInfo.getUnitType().equals(NetworkSender.UNIT_TYPE) && !moduleInfo.getUnitType().equals(NetworkReceiver.unitTypeString)) {
						updatedModuleInfo.add(moduleInfo);
					}
				}
				updatedModuleInfo.add(new UsedModuleInfo(NetworkReceiver.class.getName(),NetworkReceiver.unitTypeString,"Network Receiver"));
				unitSettings.setSettings(updatedModuleInfo);
			}
			newSettingsGroup.addSettings(unitSettings);
		}
		PamControlledUnitSettings netRxUnitSettings = new PamControlledUnitSettings(NetworkReceiver.unitTypeString,
																					"Network Receiver",
																					NetworkReceiver.class.getName(),
																					NetworkReceiveParams.serialVersionUID,
																					netRxParams);
				
		newSettingsGroup.addSettings(netRxUnitSettings);
		return newSettingsGroup;
	}

	private static NetworkReceiveParams generateNetRxParams(String ipAddress, String userId, String password, int portNumber, String baseTopic) {
		NetworkReceiveParams configuredNetRxParams = new NetworkReceiveParams();
		configuredNetRxParams.baseTopic = baseTopic;
		configuredNetRxParams.ipAddress = ipAddress;
		configuredNetRxParams.password = password;
		configuredNetRxParams.portNumber = portNumber;
		configuredNetRxParams.userId = userId;
		
		configuredNetRxParams.savePassword = true;
		configuredNetRxParams.stationId = "BaseStation";
		configuredNetRxParams.useSSL = false;
		configuredNetRxParams.useSystemTrustStore = true;
		configuredNetRxParams.mqtt = true;
		configuredNetRxParams.channelNumberOption = NetworkReceiveParams.CHANNELS_RENUMBER;
		configuredNetRxParams.connectionType = NetworkReceiveParams.CONNECTIONTYPE_MQTT;
		
		return configuredNetRxParams;
	}

//	private static 
	
}
