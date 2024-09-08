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
	
	/**
	 * Gets called back from the jni call to callJniGetAsioDrivers
	 * @param driverName
	 * @param maxChannels
	 * @param sampleRateInfo
	 */
	public void addDriverToList(String driverName, int[] maxChannels, int[]sampleRateInfo){
		lastDriverEnumeration = System.currentTimeMillis();
		System.out.printf("Getting driver detail for ASIO %s took %3.1fs\n", driverName, (double) (lastDriverEnumeration-driverEnumerationStart)/1000.);
		ArrayList<Integer>sampleRates = new ArrayList<Integer>();
		ArrayList<Integer>maxAvailableChannels = new ArrayList<Integer>();
		for(int i = 0;i<sampleRateInfo.length;i++){
			maxAvailableChannels.add(maxChannels[i]);
			sampleRates.add(sampleRateInfo[i]);				
		}		
		AsioDriverInfo asioDriverInfo = new AsioDriverInfo(driverName, maxAvailableChannels,sampleRates);
		asioDriverList.add(asioDriverInfo);
		driverEnumerationStart = System.currentTimeMillis();
	}

	long driverEnumerationStart;
	long lastDriverEnumeration;
	private ArrayList<AsioDriverInfo> getAsioDriverList() {
		clearDriverList();
		long t1 = System.currentTimeMillis();
		driverEnumerationStart = t1;
		asioJniInterface.callJniGetAsioDrivers(this);
		long t2 = System.currentTimeMillis();
		System.out.printf("Call to asioJniInterface.callJniGetAsioDrivers took %3.1fs\n", (double) (t2-t1)/1000.);
		
		for(int i = 0; i<asioDriverList.size();i++){
			System.out.printf("ASIO drivers: " + asioDriverList.get(i).driverName + "  maxChannels: " + asioDriverList.get(i).maxChannels + " Can sample at ");
			long tic = System.currentTimeMillis();
			boolean firstHz = true;
			for(int j = 0;j<asioDriverList.get(i).sampleRateInfo.size();j++){
				if (!firstHz) {
					System.out.printf(", ");
				}
				else {

				}
				System.out.printf("%d", asioDriverList.get(i).sampleRateInfo.get(j));
				firstHz = false;
			}
			System.out.printf(" Hz\n");
			long toc = System.currentTimeMillis();
			if (toc-tic > 1000) {
				System.out.printf("Checking cababilities for ASIO %s took %3.1fs\n", asioDriverList.get(i).driverName, 
						(double) (toc-tic)/1000.);
			}
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
			long tic = System.currentTimeMillis();
			asioDriverList = getAsioDriverList();
			long toc = System.currentTimeMillis();
			if (toc-tic>1000) {
				System.out.printf("Enumerating ASIO driver list took %3.1fs\n", (double)(toc-tic)/1000.);
			}
		}
		return asioDriverList;
		
	}
	
	
}

