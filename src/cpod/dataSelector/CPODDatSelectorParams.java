package cpod.dataSelector;


import java.util.ArrayList;

import PamguardMVC.dataSelector.DataSelectParams;

/**
 * Parameters for the CPOD data data selectors. Thus allows users to filter
 * CPOD data based on the paramters fo each click. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class CPODDatSelectorParams extends DataSelectParams {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5L;
	
	//create the default list of params; 
	public ArrayList<StandardCPODFilterParams> cpodDataFilterParams;
	
	public CPODDatSelectorParams(){
		cpodDataFilterParams = new 	ArrayList<StandardCPODFilterParams>(); 
		
		cpodDataFilterParams.add(new StandardCPODFilterParams(StandardCPODFilterParams.PEAK_FREQ, 0, 255));
		cpodDataFilterParams.add(new StandardCPODFilterParams(StandardCPODFilterParams.AMPLITUDE, 90, 150));
		cpodDataFilterParams.add(new StandardCPODFilterParams(StandardCPODFilterParams.NCYCLES, 0, 255));
		cpodDataFilterParams.add(new StandardCPODFilterParams(StandardCPODFilterParams.BW, 0, 255));
		cpodDataFilterParams.add(new StandardCPODFilterParams(StandardCPODFilterParams.END_F, 0, 255));
	
	}
	


}
