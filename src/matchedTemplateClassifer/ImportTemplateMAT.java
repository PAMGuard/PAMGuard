package matchedTemplateClassifer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLStructure;


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
	private MatFileReader mfr;

	@Override
	public MatchTemplate importTemplate(File filePath){
		errorCode = 0; //reset error code
		try {
			//System.out.println("Import MAT file waveform");

			//the MATLAB file reader. 
			mfr = new MatFileReader(filePath);
			
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
	private MatchTemplate getTemplateStruct(MatFileReader mfr2) {
		
		//the MATLAB file reader. 
		MLStructure clicksStruct = (MLStructure) mfr.getMLArray("clicks"); 
		MLDouble sampleRateML = (MLDouble) mfr.getMLArray("clicks_sR");


		
		if (clicksStruct==null) {
			clicksStruct = (MLStructure) mfr.getMLArray("raw_data_units"); 
			sampleRateML = (MLDouble) mfr.getMLArray("raw_data_units_sR");
		}
		

		
		if (sampleRateML==null || clicksStruct==null) {
			return null;
		}
		
		MLDouble waveML = (MLDouble) clicksStruct.getField("wave", 0); 
		double[][] waveform = waveML.getArray(); 
		

		MatchTemplate matchedTemplate = new MatchTemplate(null, waveform[0], (float) sampleRateML.get(0).doubleValue());
		

		return matchedTemplate; 
		
	}


	/**
	 * Get a template which has a simple sR and waveform field. 
	 * @param mfr - .mat file reader.
	 * @return the match template. 
	 */
	private MatchTemplate getTemplateStandard(MatFileReader mfr) {
		
			//System.out.println("Import MAT file waveform");
		


			//the MATLAB file reader. 
			MLDouble sampleRateML = (MLDouble) mfr.getMLArray("sR");
			if (sampleRateML==null) sampleRateML = (MLDouble) mfr.getMLArray("sr"); //try a different name for the sample rate
			if (sampleRateML==null) sampleRateML = (MLDouble) mfr.getMLArray("fs"); //try a different name for the sample rate
			if (sampleRateML==null) sampleRateML = (MLDouble) mfr.getMLArray("samplerate"); //try a different name for the sample rate
			if (sampleRateML==null) sampleRateML = (MLDouble) mfr.getMLArray("sample_rate"); //try a different name for the sample rate
			if (sampleRateML==null) sampleRateML = (MLDouble) mfr.getMLArray("sampleRate"); //try a different name for the sample rate
			if (sampleRateML==null) sampleRateML = (MLDouble) mfr.getMLArray("clicks_sR"); //try a different name for the sample rate

			
			//get the waveform or spectrum
			MLDouble waveformML = (MLDouble) mfr.getMLArray("waveform");
			
			if (waveformML==null) waveformML = (MLDouble) mfr.getMLArray("wave"); //try a different name for the waveform
			if (waveformML==null) waveformML = (MLDouble) mfr.getMLArray("spectrum"); //might be a spectrum
			

			if (sampleRateML==null || waveformML==null) {
				return null;
			}
			
			//import a wave in column or row dimension
			int size = Math.max(waveformML.getM(), 	waveformML.getN());
			double[] waveform = new double[size];
			
			if (size<TemplateImport.MIN_WAVEFORM_LENGTH) {
				errorCode=TemplateImport.ERROR_WAVEFORM_LENGTH; 
				return null;
			}
			
			for (int i = 0 ; i<size; i++) {
				waveform[i]= waveformML.getM()>waveformML.getN() ? waveformML.get(i, 0) : waveformML.get(0, i);
			}
			float sR= Float.valueOf((float) sampleRateML.getArray()[0][0]);
			
			
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
