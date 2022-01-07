/**
 * 
 */
package asiojni;

import java.util.ArrayList;

/**
 * @author Dajo
 *
 */
public class AsioDriverInfos {

	private ArrayList<AsioDriverInfo> asioDriverList = new ArrayList<AsioDriverInfo>();
	
	public AsioJniInterface asioJniInterface;
	

	AsioDriverInfos(AsioJniInterface asioJniInterface){
		this.asioJniInterface = asioJniInterface;
	}
	
	private void clearDriverList(){
		asioDriverList.clear();
		
	}
	
	public void addDriverToList(String driverName, int[] maxChannels, int[]sampleRateInfo){
		ArrayList<Integer>sampleRates = new ArrayList<Integer>();
		ArrayList<Integer>maxAvailableChannels = new ArrayList<Integer>();
		for(int i = 0;i<sampleRateInfo.length;i++){
			maxAvailableChannels.add(maxChannels[i]);
			sampleRates.add(sampleRateInfo[i]);				
		}		
		AsioDriverInfo asioDriverInfo = new AsioDriverInfo(driverName, maxAvailableChannels,sampleRates);
		asioDriverList.add(asioDriverInfo);
	}

	private ArrayList<AsioDriverInfo> getAsioDriverList() {
		clearDriverList();
		asioJniInterface.callJniGetAsioDrivers(this);
		for(int i = 0; i<asioDriverList.size();i++){
			System.out.printf("ASIO drivers: " + asioDriverList.get(i).driverName + "  maxChannels: " + asioDriverList.get(i).maxChannels + " Can sample at ");
			boolean firstHz = true;
				for(int j = 0;j<asioDriverList.get(i).sampleRateInfo.size();j++){
					if (firstHz == false) {
						System.out.printf(", ");
					}
					else {
						
					}
					System.out.printf("%d", asioDriverList.get(i).sampleRateInfo.get(j));
					firstHz = false;
				}
				System.out.printf(" Hz\n");
			}
//			System.out.println();
//			System.out.println("Java:: just called asioJniInterface.callJniGetAsioDrivers(this);");
//			System.out.flush();
		
		return asioDriverList;
	}

	public ArrayList<AsioDriverInfo> getCurrentAsioDriverList() {
//		System.out.println("getCurrentAsioDriverList():: asioDriverList.size()=" + asioDriverList.size());
//		System.out.flush();
		if(asioDriverList.size()==0){
			return asioDriverList = getAsioDriverList();
		}
		else{
			return asioDriverList;
		}
		
	}
	
	
}

