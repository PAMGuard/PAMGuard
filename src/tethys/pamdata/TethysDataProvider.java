package tethys.pamdata;

import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;

import Localiser.LocalisationAlgorithm;
import PamDetection.LocalisationInfo;
import PamguardMVC.PamDataUnit;
import nilus.AlgorithmType;
import nilus.AlgorithmType.Parameters;
import nilus.Detection.Parameters.UserDefined;
import nilus.Deployment;
import nilus.DescriptionType;
import nilus.Detection;
import nilus.DetectionEffortKind;
import nilus.GranularityEnumType;
import nilus.Helper;
import tethys.Collection;
import tethys.localization.TethysLocalisationInfo;
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
public abstract class TethysDataProvider {

	private Helper helper;

	/**
	 * 
	 */
	public TethysDataProvider() {
		super();
		
		try {
			helper = new Helper();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}

	public  Element addUserDefined(Detection.Parameters parameters, String parameterName, String parameterValue) {
		UserDefined userDefined = parameters.getUserDefined();
		if (userDefined == null) {
			userDefined = new UserDefined();
			parameters.setUserDefined(userDefined);
		}
		Element el = null;
		try {
			el = getHelper().AddAnyElement(userDefined.getAny(), parameterName, parameterValue);
		} catch (JAXBException e) {
			e.printStackTrace();
			return null;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}
		return el;
	}

	/**
	 * Get a standard Method string for each detector. This can be a bit 
	 * verbose and might even have a reference to a paper ? Is this the best place for this ? 
	 * @return
	 */
	public abstract String getDetectionsMethod();

	/**
	 * Get DescriptionType object to include in a Tethys Detections document. 
	 * @param deployment
	 * @param tethysExportParams
	 * @return Tethys DescriptionType object, which contains infromation about detections
	 */
	public abstract DescriptionType getDescription(Deployment deployment, TethysExportParams tethysExportParams);


	/**
	 * Get Algorithm information for a Tethys Detections document
	 * @param collection Detections or Localisations may have different parameter sets. 
	 * @return Algorithm information
	 */
	public abstract AlgorithmType getAlgorithm(Collection collection);
	
	/**
	 * Get a list of allowed granularity types for this output 
	 * @return list of granularities. 
	 */
	public abstract GranularityEnumType[] getAllowedGranularities();
	
//	public String getGranularityName GranularityEnumType);
	
	/**
	 * Get a name for the detections documents. This will be appended
	 * to the Deployment name and may also have a number after it. <br>
	 * Note that the name isn't really important since all the matching between 
	 * different documents is done internally, but it helps to make everything 
	 * human readable. 
	 * @return A name, similar to datablock.getLongDataName(), but no spaces. 
	 */
	public abstract String getDetectionsName();
	
	/**
	 * True if the datablock is detections. This will (nearly) always 
	 * be true or the block wouldn't have a TethysDataProvider, however
	 * there may be one or two localisers that should really only output
	 * localisation information. 
	 * @return
	 */
	public abstract boolean hasDetections();
	
	/**
	 * See if it's possible for this block to export localisations. This may 
	 * depend on the selected granularity. 
	 * @param granularityType
	 * @return
	 */
	public abstract boolean canExportLocalisations(GranularityEnumType granularityType);

	/**
	 * Create a Tethys Detection object from a PamDataUnit.<br>
	 * It's OK for this to return null if for some reason the unit shouldn't be stored. 
	 * @param dataUnit PAMGuard data unit
	 * @param tethysExportParams 
	 * @param streamExportParams
	 * @return Detection Tethys Detection object. 
	 */
	public abstract Detection createDetection(PamDataUnit dataUnit, TethysExportParams tethysExportParams,
			StreamExportParams streamExportParams);


	/**
	 * Get the algorithm parameters. 
	 * @return
	 */
	public abstract Parameters getAlgorithmParameters();


	/**
	 * Fill in the effort kind list for the top of a Detections document. This must contain a list
	 * of every species that's going to be output within this effort period. Any species assigned
	 * to an actual detection must be in this list, or the document will be rejected. 
	 * @param pDeployment
	 * @param effortKinds tethys object list to add to. 
	 * @param exportParams 
	 */
	public abstract void getEffortKinds(PDeployment pDeployment, List<DetectionEffortKind> effortKinds, StreamExportParams exportParams);

	/**
	 * See if a particular card should be used in the export wizard. This may
	 * not be the best way of doing this, but will do for now. 
	 * @param wizPanel
	 * @return
	 */
	public abstract boolean wantExportDialogCard(ExportWizardCard wizPanel);
	
	/**
	 * Get the localisation algorithm (if there is one). This is generally 
	 * found automatically from the datablock, but it may be necessary to override. 
	 * @return Localisation Algorithm, or null. 
	 */
	public abstract LocalisationAlgorithm getLocalisationAlgorithm();

	/**
	 * Get localisation info for the datablock. Can be null, but probably never is. More likely to have a zero of available types;
	 * @return
	 */
	public abstract TethysLocalisationInfo getLocalisationInfo();

	/**
	 * @return the helper
	 */
	public Helper getHelper() {
		return helper;
	}
	
			
	
}
