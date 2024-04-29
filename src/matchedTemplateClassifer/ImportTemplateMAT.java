package matchedTemplateClassifer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import PamUtils.PamArrayUtils;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.format.Mat5File;
import us.hebi.matlab.mat.types.Matrix;
import us.hebi.matlab.mat.types.Struct;


/**
 * Import a .mat file of a click template. 
 * @author Jamie Macaulay
 */
public class ImportTemplateMAT implements TemplateImport {
	
	private String[] extensionStrings = new String[] {"mat"};
	
	public int errorCode=0; 
	
	/**
	 * The matlab file rader.
	 */
	private Mat5File mfr;

	@Override
	public MatchTemplate importTemplate(File filePath){
		errorCode = 0; //reset error code
		try {
			//System.out.println("Import MAT file waveform");

			//the MATLAB file reader. 
			mfr =  Mat5.readFromFile(filePath);
			
			MatchTemplate matchTemplate;
			
			
			//try standard template first
			matchTemplate = getTemplateStandard(mfr);
			
			//use the first click from a structure of clicks. 
			if (matchTemplate == null && errorCode==0) {
				matchTemplate = getTemplateStruct(mfr);
			}
			
			//if the match template is still null then the file is the wrong format. 
			if (matchTemplate == null && errorCode==0) {
				errorCode=TemplateImport.INCORRECT_FILE_FORMAT; 
				return null; 
			}

			matchTemplate.name =  filePath.getName();
						
			return matchTemplate;
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return null; 
	}
	
	/**
	 * Get a template which has a simple sR and waveform field. 
	 * @param mfr - .mat file reader.
	 * @return the match template. 
	 */
	private MatchTemplate getTemplateStruct(Mat5File mfr2) {
		
		//the MATLAB file reader. 
		Struct clicksStruct = mfr.getStruct("clicks"); 
		Double sampleRateML = getDouble(mfr,"clicks_sR");

//		if (clicksStruct==null) {
//			clicksStruct = (MLStructure) mfr.getMLArray("raw_data_units"); 
//			sampleRateML = (MLDouble) mfr.getMLArray("raw_data_units_sR");
//		}
		if (sampleRateML==null || clicksStruct==null) {
			return null;
		}
		
		Matrix waveML = clicksStruct.get("wave", 0); 
		double[][] waveform = PamArrayUtils.matrix2array(waveML);
		
		MatchTemplate matchedTemplate = new MatchTemplate(null, waveform[0], (float) sampleRateML.doubleValue());

		return matchedTemplate; 
	}
	
	
	private Double getDouble(Mat5File mfr, String field) {
		Matrix data = mfr.getMatrix(field); 
		if (data==null) return null;
		return data.getDouble(0);
	}


	/**
	 * Get a template which has a simple sR and waveform field. 
	 * @param mfr - .mat file reader.
	 * @return the match template. 
	 */
	private MatchTemplate getTemplateStandard(Mat5File mfr) {
		
			//System.out.println("Import MAT file waveform");
		
			//the MATLAB file reader. 
			Double sampleRateML =  getDouble(mfr,"sR");
			if (sampleRateML==null) sampleRateML = getDouble(mfr,"sr"); //try a different name for the sample rate
			if (sampleRateML==null) sampleRateML = getDouble(mfr,"fs"); //try a different name for the sample rate
			if (sampleRateML==null) sampleRateML = getDouble(mfr,"samplerate");//try a different name for the sample rate
			if (sampleRateML==null) sampleRateML = getDouble(mfr,"sample_rate"); //try a different name for the sample rate
			if (sampleRateML==null) sampleRateML = getDouble(mfr,"sampleRate"); //try a different name for the sample rate
			if (sampleRateML==null) sampleRateML = getDouble(mfr,"clicks_sR"); //try a different name for the sample rate

			
			//get the waveform or spectrum
			Matrix waveformML = mfr.getMatrix("waveform");
			
			if (waveformML==null) waveformML =  mfr.getMatrix("wave"); //try a different name for the waveform
			if (waveformML==null) waveformML =  mfr.getMatrix("spectrum"); //might be a spectrum
			

			if (sampleRateML==null || waveformML==null) {
				return null;
			}
			
			//import a wave in column or row dimension
			int size = Math.max(waveformML.getNumRows(), 	waveformML.getNumCols());
			double[] waveform = new double[size];
			
			if (size<TemplateImport.MIN_WAVEFORM_LENGTH) {
				errorCode=TemplateImport.ERROR_WAVEFORM_LENGTH; 
				return null;
			}
			
			for (int i = 0 ; i<size; i++) {
				waveform[i]= waveformML.getNumRows()>waveformML.getNumCols() ? waveformML.getDouble(i, 0): waveformML.getDouble(0, i);
			}
			float sR= (float) sampleRateML.doubleValue();
			
			
			//now create waveform
//			System.out.println("Create a waveform with " + waveform.length + " samples with a sample rate of "
//					+ sR + " Hz");
			MatchTemplate matchedTemplate = new MatchTemplate(null, waveform, sR);
			return matchedTemplate; 
			
	}


	@Override
	public String[] getExtension() {
		return extensionStrings;
	}
	
	/**
	 * Test the importing of ML data. 
	 */
	@SuppressWarnings("unused")
	private void testMLWaveformImport() {
		File filePath = new File("C:\\Users\\Jamie\\Desktop\\simporpclick.mat"); 
		MatchTemplate template = importTemplate(filePath);
	}


	@Override
	public int getErrorCode() {
		return errorCode;
	}

}
