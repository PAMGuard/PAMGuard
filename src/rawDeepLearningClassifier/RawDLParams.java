package rawDeepLearningClassifier;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;

import PamView.GroupedSourceParameters;
import rawDeepLearningClassifier.dlClassification.DLClassName;

/**
 * Basic parameters for deep learning module.
 * 
 * @author Jamie Macaulay
 *
 */
public class RawDLParams implements Serializable, Cloneable {

	/**
	 * 
	 */
	public static final long serialVersionUID = 4L;

	/**
	 * The currently selected Deep Learning model.
	 * (Models are now automatically selected)
	 */
	@Deprecated
	public int modelSelection = 0;
	
	/**
	 * The current model URI. The deep learning model must have some sort of external file to run. 
	 * This might be a model, a .exe file etc. 
	 * 
	 */
	public URI modelURI; 

	/**
	 * Holds channel and grouping information
	 */
	public GroupedSourceParameters groupedSourceParams = new GroupedSourceParameters();

	/**
	 * The number of raw samples to send to the classifier.
	 */
	public int rawSampleSize = 192000; // sample bins

	/**
	 * The hop size i.e. how far to move forward in raw data before sending another
	 * chunk of data to the classifier.
	 */
	public int sampleHop = 96000; // sample bins

	/**
	 * The maximum number of samples a merged classified data unit can be. if this
	 * is the same as rawSampleSize then data units are never merged. It must be a
	 * multiple of rawSampleSize.
	 */
	public int maxMergeHops = 5; // N

	/**
	 * The deep learning classifier can accept multiple types of data unit that
	 * contain a raw data chunk e.g. raw data, clicks, clips etc. By default the
	 * classifier saves new data units if the source is raw data. However, if the
	 * data unit is an already processed data unit, e.g. a click detection, then the
	 * results are saved as an annotation to that unit. If forceSave is st true then
	 * new data units are created no matter what the source data is.
	 */
	public boolean forceSave = false;

	/**
	 * The maximum buffer time. This is the maximum time between the start of the
	 * first and last grouped raw data unit in the classification buffer before all
	 * data is sent to the deep learning classifier.
	 */
	public double maxBufferTime = 1000.0; // milliseconds
	
	
	/**
	 * True to use the data selector to pre-filter detections 
	 * (but only if the input is a detection data block)
	 */
	public boolean useDataSelector = false; 

	/**
	 * Uuuruggh. Spent a lot of time thinking about this. Different models have
	 * different class names. If we change model then the class names may change.
	 * Previously annotated data will then be messed up. But, in a giant dataset
	 * that may be an issue. Perhaps users wish to run a new model on some chunk of
	 * data without messing up all the other classified detection which have used
	 * that module. So store the data in binary files? That is super inefficient as
	 * the same string is stored many times. So instead store a short which
	 * identifies the string that sits in this table. Everytime a new model is added
	 * add new classnames. Note that this means we have a maximum of 32k names - we
	 * will come to that issue when it arises...
	 */
	public ArrayList<DLClassName> classNameMap = new ArrayList<DLClassName>();

	/**
	 * The class name index. i.e. the number of unique class names that have been
	 * added.
	 */
	public short classNameIndex = 0;

	@Override
	public RawDLParams clone() {
		RawDLParams newParams = null;
		try {
			newParams = (RawDLParams) super.clone();
			// if (newParams.spectrogramNoiseSettings == null) {
			// newParams.spectrogramNoiseSettings = new SpectrogramNoiseSettings();
			// }
			// else {
			// newParams.spectrogramNoiseSettings = this.spectrogramNoiseSettings.clone();
			// }
		} catch (CloneNotSupportedException Ex) {
			Ex.printStackTrace();
			return null;
		}
		return newParams;
	}

}
