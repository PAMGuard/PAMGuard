package cpod.dataSelector;

import cpod.CPODClick;

/**
 * Basic data fitler for a CPOD 
 * @author au671271
 *
 */
public class StandardCPODataFilter implements CPODDataFilter {


	/**
	 * Get the data value form the CPODClick
	 * @param cpodClick - the CPODClick
	 * @return the data value. 
	 */
	public double getDataValue(CPODClick cpodClick, int type) {
		switch (type) {
		case StandardCPODFilterParams.AMPLITUDE :
			return cpodClick.getAmplitudeDB();
		case StandardCPODFilterParams.NCYCLES:
			return cpodClick.getnCyc(); 
		case StandardCPODFilterParams.BW:
			return cpodClick.getBw();
		case StandardCPODFilterParams.PEAK_FREQ:
			return cpodClick.getkHz(); 
		case StandardCPODFilterParams.END_F:
			return cpodClick.getEndF();
		} 
		//must be an error in the code. 
		return Double.NaN; 
	}


	@Override
	public int scoreData(CPODClick cpodClick, StandardCPODFilterParams cpodFilterParams) {
		
		StandardCPODFilterParams standardParams = (StandardCPODFilterParams) cpodFilterParams; 
	
		double dataValue = getDataValue(cpodClick, standardParams.dataType); 

		if (dataValue>=standardParams.min && dataValue<=standardParams.max) {
			return 1; 
		}
		else {
//			System.out.println("Do not plot data: " +cpodClick.getTimeMilliseconds());
			return 0; 
		}

	}


}
