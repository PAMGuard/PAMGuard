package matchedTemplateClassifer;

import java.awt.Color;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;
import PamView.PamSymbolType;
import PamView.symbol.SymbolData;
import fftFilter.FFTFilterParams;

/**
 * Matched template classifier parameters. 
 * 
 * @author Jamie Macaulay
 *
 */
public class MatchedTemplateParams implements Serializable, Cloneable, ManagedParameters {
	
	/**
	 * Do not normalise
	 */
	public final  static int NORMALIZATION_NONE = 2; 
	
	/**
	 * Peak normalisation
	 */
	public final  static int NORMALIZATION_PEAK = 0; 

	/**
	 * RMS normalisation
	 */
	public final  static int NORMALIZATION_RMS = 1; 

	
	/**
	 * 
	 */
	protected static final long serialVersionUID = 5L;
	
	/**
	 * 
	 */
	public MatchedTemplateParams(){
		classifiers= new ArrayList<MTClassifier>(); 
		//add at least one default classifier
		classifiers.add(new MTClassifier()); 
	}

	/**
	 * Symbol data for the classifier. 
	 */
	public SymbolData pamSymbol = new SymbolData(PamSymbolType.SYMBOL_CIRCLE, 5, 5, true,
			Color.PINK, Color.PINK); 

	/**
	 * The source data block
	 */
	public String dataSourceName = null; 
	
	/**
	 * The index of the data source. 
	 */
	public int dataSourceIndex=0;
	
	/**
	 * The type to set the click to. 
	 */
	public byte type = 101; 
	
	/**
	 * The MT classifiers. Each has two templates; a match template and reject template
	 */
	public ArrayList<MTClassifier> classifiers;
	
	/**
	 * Enable filtering of data before classificatiom
	 */
	public boolean enableFFTFilter = false;
	
	/**
	 * The FFT filter params to use. 
	 */
	public FFTFilterParams fftFilterParams;

//	/**
//	 * Number of bins around click centre to use for classification
//	 */
//	public int restrictedBins=128;
//	
//	/**
//	 * True to use click bins. 
//	 */
//	public boolean useRestrictedBins=true;

	/**
	 * Integer flag for how to perform channel classification.
	 *  0) Require positive identification on all channels individually
	 *  1) Only one channel requires classiifciation
	 *  
	 * classification, 2) Use the mean of the channels.
	 */
	public int channelClassification=0;


	/**
	 * The amount of smoothing to do when searching for a click peak. 
	 */
	public int peakSmoothing=5;

	/**
	 * The number of bins to sample around. 
	 */
	public int restrictedBins=128;

	/**
	 * True to search for for wave peak and sample around it. 
	 */
	public boolean peakSearch= true;

	/**
	 * dB drop in waveform for peak search. 
	 */
	public double lengthdB =6; 
	
	/**
	 * The normalisation type to use. 
	 */
	public int normalisationType = 1;  


	
	@Override
	public MatchedTemplateParams clone() {
		MatchedTemplateParams newParams = null;
		try {
			newParams = (MatchedTemplateParams) super.clone();
		}
		catch(CloneNotSupportedException Ex) {
			Ex.printStackTrace(); 
			return null;
		}
		return newParams;
	}



	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		try {
			Field field = this.getClass().getDeclaredField("fftFilterParams");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return fftFilterParams;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			try {
				Field field = this.getClass().getSuperclass().getDeclaredField("fftFilterParams");
				ps.put(new PrivatePamParameterData(this, field) {
					@Override
					public Object getData() throws IllegalArgumentException, IllegalAccessException {
						return fftFilterParams;
					}
				});
			} catch (NoSuchFieldException | SecurityException e2) {
				e2.printStackTrace();
			}
		}
			
		return ps;
	}
}
