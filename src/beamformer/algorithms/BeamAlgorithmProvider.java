package beamformer.algorithms;

import PamController.SettingsPane;
import beamformer.BeamAlgorithmParams;
import beamformer.BeamFormerBaseProcess;
import beamformer.BeamFormerParams;
import beamformer.continuous.BeamFormerProcess;
import pamViewFX.fxNodes.PamBorderPane;

/**
 * Provider of beam former algorithms - may need > 1 algorithm object if
 * channels are grouped into separate array sections, e.g. for crossed 
 * pair localisation. 
 * @author dg50
 *
 */
public interface BeamAlgorithmProvider {

	/**
	 * Get fixed properties such as the algorithm name and whether it 
	 * can output a beamogram
	 * @return
	 */
	public StaticAlgoProperties getStaticProperties();
	
	/**
	 * Create an instance of the algorithm for a set of channels. 
	 * @param beamFormerBaseProcess Beam former process
	 * @param parameters the parameters to use in this algorithm (provider will need to cast this to parameter type that matches the algorithm)
	 * @param firstSeqNum the sequence number to start with when numbering beams
	 * @param beamogramNum the sequence number of the beamogram
	 * @return new Beam Former Algorithm. 
	 */
	public BeamFormerAlgorithm makeAlgorithm(BeamFormerBaseProcess beamFormerBaseProcess, BeamAlgorithmParams parameters, int firstSeqNum, int beamogramNum);
	
	
	/**
	 * Create an instance of the algorithm parameters
	 * @param algorithmName the algorithm name
	 * @param groupNumber the group number
	 * @param channelMap channel map for channels used in this group
	 * @return
	 */
	public BeamAlgorithmParams createNewParams(String algorithmName, int groupNumber, int channelMap);
	
	/**
	 * Return the algorithm-specific parameters dialog, populated with the parameters passed
	 * @param overallParams the current parameters used by the BeamformerSettingsPane object - contains the current source/group information for this settings GUI,
	 * and may not necessarily be the same as the parameters stored in the BeamFormerBaseControl object.  
	 * @param algoParams the algorithm parameters to display in the dialog
	 * @return the dialog
	 */
	public SettingsPane<?> getParamsDialog(BeamFormerParams overallParams, BeamAlgorithmParams algoParams);
	
	/**
	 * Retrieve the current set of parameters.  These will be the last parameters that were displayed in the dialog
	 * 
	 * @return
	 */
	public BeamAlgorithmParams getCurrentParams();

}
