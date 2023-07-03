package tethys.pamdata;

import PamguardMVC.PamDataUnit;
import nilus.AlgorithmType;
import nilus.AlgorithmType.Parameters;
import nilus.Deployment;
import nilus.DescriptionType;
import nilus.Detection;
import tethys.output.StreamExportParams;
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


	/**
	 * Get DescriptionType object to include in a Tethys Detections document. 
	 * @param deployment
	 * @param tethysExportParams
	 * @return Tethys DescriptionType object, which contains infromation about detections
	 */
	public DescriptionType getDescription(Deployment deployment, TethysExportParams tethysExportParams);


	/**
	 * Get Algorithm information for a Tethys Detections document
	 * @return Algorithm information
	 */
	public AlgorithmType getAlgorithm();


	/**
	 * Create a Tethys Detection object from a PamDataUnit.<br>
	 * It's OK for this to return null if for some reason the unit shouldn't be stored. 
	 * @param dataUnit PAMGuard data unit
	 * @param tethysExportParams 
	 * @param streamExportParams
	 * @return Detection Tethys Detection object. 
	 */
	public Detection createDetection(PamDataUnit dataUnit, TethysExportParams tethysExportParams,
			StreamExportParams streamExportParams);


	public Parameters getAlgorithmParameters();
	
	
}
