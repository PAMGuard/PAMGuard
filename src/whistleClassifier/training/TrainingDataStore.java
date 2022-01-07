package whistleClassifier.training;

/**
 * Storage for whistle classifier training data.
 * NB this class if for a single storage unit, such as a file.  
 * @author Doug Gillespie
 *
 */
public interface TrainingDataStore  {

	/**
	 * Open store for writing
	 * @param storeName name
	 * @return true if open
	 */
	public boolean openStore(String storeName);
	
	/** 
	 * Close store for writing
	 * @return true if OK
	 */
	public boolean closeStore();
	
	/**
	 * Write data to the opened store
	 * @param trainingDataSet data to write
	 * @return true if OK
	 */
	public boolean writeData(TrainingDataSet trainingDataSet);
	
	/**
	 * Open, read data and close a data store 
	 * @param storeName store name
	 * @return reference to TrainingDataSet
	 * @see TrainingDataSet
	 */
	public TrainingDataSet readData(String storeName);
	
	/**
	 * IS a store correctly opened.
	 * @return true if ok
	 */
	public boolean isStoreOk();
	
}
