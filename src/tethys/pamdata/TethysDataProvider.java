package tethys.pamdata;

import PamguardMVC.PamDataUnit;
import nilus.AlgorithmType;
import nilus.Deployment;
import nilus.DescriptionType;
import tethys.output.TethysExportParams;

/**
 * Any PAMGuard data stream which can provide Detection data to PAMGuard will 
 * be able to return one of these. It will provide a schema and a function which 
 * can turn individual data units into data formatted for Tethys. The nature of how these 
 * work TBD. 
 * @author dg50
 *
 */
public interface TethysDataProvider {

	/**
	 * This gets the Tethys schema for this type of data in whatever
	 * form we decide it's best stored in, an XML string, or what ? 
	 * @return
	 */
	public TethysSchema getSchema();
	

	/**
	 * This will convert a data unit for this provider into whatever format we need the 
	 * data to be in for Tethys. Some base function but also bespoke stuff depending on the
	 * data type. Will probably need writing for every module individually?
	 * @param pamDataUnit
	 * @return
	 */
	public TethysDataPoint getDataPoint(PamDataUnit pamDataUnit);


	public DescriptionType getDescription(Deployment deployment, TethysExportParams tethysExportParams);


	public AlgorithmType getAlgorithm();
	
}
