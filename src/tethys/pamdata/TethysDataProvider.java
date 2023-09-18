package tethys.pamdata;

import java.util.List;

import PamguardMVC.PamDataUnit;
import nilus.AlgorithmType;
import nilus.AlgorithmType.Parameters;
import nilus.Deployment;
import nilus.DescriptionType;
import nilus.Detection;
import nilus.DetectionEffortKind;
import nilus.GranularityEnumType;
import tethys.niluswraps.PDeployment;
import tethys.output.StreamExportParams;
import tethys.output.TethysExportParams;
import tethys.swing.export.ExportWizardCard;

/**
 * Any PAMGuard data stream which can provide Detection data to PAMGuard will 
 * be able to return one of these. It will provide a schema and a function which 
 * can turn individual data units into data formatted for Tethys. The nature of how these 
 * work TBD. 
 * @author dg50
 *
 */
public interface TethysDataProvider {

//	/**
//	 * This gets the Tethys schema for this type of data in whatever
//	 * form we decide it's best stored in, an XML string, or what ? 
//	 * @return
//	 */
//	public TethysSchema getSchema();
//	

	/**
	 * This will convert a data unit for this provider into whatever format we need the 
	 * data to be in for Tethys. Some base function but also bespoke stuff depending on the
	 * data type. Will probably need writing for every module individually?
	 * @param pamDataUnit
	 * @return
	 */
//	public TethysDataPoint getDataPoint(PamDataUnit pamDataUnit);

	/**
	 * Get a standard Method string for each detector. This can be a bit 
	 * verbose and might even have a reference to a paper ? Is this the best place for this ? 
	 * @return
	 */
	public String getDetectionsMethod();

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
	 * Get a list of allowed granularity types for this output 
	 * @return list of granularities. 
	 */
	public GranularityEnumType[] getAllowedGranularities();
	
//	public String getGranularityName GranularityEnumType);
	
	/**
	 * Get a name for the detections documents. This will be appended
	 * to the Deployment name and may also have a number after it. <br>
	 * Note that the name isn't really important since all the matching between 
	 * different documents is done internally, but it helps to make everything 
	 * human readable. 
	 * @return A name, similar to datablock.getLongDataName(), but no spaces. 
	 */
	public String getDetectionsName();


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


	/**
	 * Get the algorithm parameters. 
	 * @return
	 */
	public Parameters getAlgorithmParameters();


	/**
	 * Fill in the effort kind list for the top of a Detections document. This must contain a list
	 * of every species that's going to be output within this effort period. Any species assigned
	 * to an actual detection must be in this list, or the document will be rejected. 
	 * @param pDeployment
	 * @param effortKinds tethys object list to add to. 
	 * @param exportParams 
	 */
	public void getEffortKinds(PDeployment pDeployment, List<DetectionEffortKind> effortKinds, StreamExportParams exportParams);

	/**
	 * See if a particular card should be used in the export wizard. This may
	 * not be the best way of doing this, but will do for now. 
	 * @param wizPanel
	 * @return
	 */
	public boolean wantExportDialogCard(ExportWizardCard wizPanel);
	
	
}
