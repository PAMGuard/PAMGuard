package clickTrainDetector.clickTrainAlgorithms.mht;

/**
 * The MHTChi2 provider manages the mhtchi2 algorithms for each track possibility. 
 * @author Jamie Macaulay. 
 *
 * @param <T>
 */
public interface MHTChi2Provider<T> {
	
	/**
	 * Called every time a new detection is added. 
	 * @param kcount - the total number of detection so far. 
	 * @param detection - detection
	 */
	public void addDetection(T detection, int kcount);

	/**
	 * Create a new MHTChi2 instance. If mhtChi2 is non null then this is cloned
	 * with all current saved data and a new instance added. 
	 * @param mhtChi2 - current instance of mhtChi2. Can be null. 
	 * @return - new instance of MHTChi2
	 */
	public MHTChi2<T> newMHTChi2(MHTChi2<T> mhtChi2);
	
	
	/**
	 * Save any settings from the settings pane. 
	 */
	public MHTChi2Params getSettingsObject();
	
	/**
	 * Called whenever the MHTKernel is reset and/or a completely new set of click trains are 
	 * to be analysed. 
	 */
	public void clear();

	/**
	 * Set the MHT parameters fro the provider. 
	 * @param mhtParams - the parameters to set. 
	 */
	public void setMHTParams(MHTParams mhtParams); 
	
	/**
	 * 
	 * @return
	 */
	public MHTChi2Params getChi2Params();
	
	
	
	
	/**
	 * Print the settings to the console 
	 */
	public void printSettings();

	/**
	 * Called whenever a clear Kernel Garbage command is sent to start at a new data
	 * unit. i.e. the kernel now starts from a new data unit and so some settings in
	 * the chi^2 may need to change.
	 * 
	 * @param newRefIndex - the index of the new reference data unit with respect to
	 *                    the old list.
	 */
	public void clearKernelGarbage(int newRefIndex);

	
}