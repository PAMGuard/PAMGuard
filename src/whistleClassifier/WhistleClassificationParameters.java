package whistleClassifier;

import java.io.Serializable;
import java.lang.reflect.Field;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamModel.parametermanager.PrivatePamParameterData;

public class WhistleClassificationParameters implements Cloneable, Serializable, ManagedParameters {

	static public final long serialVersionUID = 0;

	public String dataSource;
	
	/**
	 * Analyse new data using existing settings. 
	 */
	public static final int ANALYSE_DATA = 0;
	/**
	 * Collect training data. 
	 */
	public static final int COLLECT_TRAINING_DATA = 1;
	
	public int operationMode = ANALYSE_DATA;
	
	/**
	 * Folder for output training files. 
	 */
	public String trainingDataFolder;
	
	/**
	 * Species id for current training run. 
	 */
	public String trainingSpecies;
	
	/**
	 * when analysing wav files to generate training data use
	 * the folder name for species. 
	 */
	public boolean wavFolderNameAsSpecies;
	
	/**
	 * When analysing training data, use the folder name as species 
	 * and override what's in the training set. 
	 */
	public boolean trainingDataFolderAsSpecies;
	
	/**
	 * The actual parameters for the classifier. These are 
	 * held in a separate class so that they can be written 
	 * more easily to separate files for distribution / sharing
	 */
	public FragmentClassifierParams fragmentClassifierParams;
	
	/**
	 * If fewer than lowWhistleNumber whistles collected over a period, then clear
	 * and start again. 
	 */
	public int lowWhistleClearTime = 60;
	
	/**	 
	 * If fewer than lowWhistleNumber whistles collected over a period, then clear
	 * and start again. 
	 */
	int lowWhistleNumber = 3;
	
	/**
	 * classify anyway, even if not enough fragments when quitting due to low 
	 * whistle numbers. 
	 */
	public boolean alwaysClassify;
	
	@Override
	public WhistleClassificationParameters clone() {
		try {
			return (WhistleClassificationParameters) super.clone();
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		try {
			Field field = this.getClass().getDeclaredField("lowWhistleNumber");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return lowWhistleNumber;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
			
		return ps;
	}
	
}
