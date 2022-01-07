package matchedTemplateClassifer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLDouble;


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
		
		try {
			//System.out.println("Import MAT file waveform");

			//the matlab file reader. 
			mfr = new MatFileReader(filePath);
			MLDouble sampleRateML = (MLDouble) mfr.getMLArray("sR");
			
			
			MLDouble waveformML = (MLDouble) mfr.getMLArray("waveform");
			if (waveformML==null) waveformML = (MLDouble) mfr.getMLArray("spectrum"); //might be a spectrum
			
			if (sampleRateML==null || waveformML==null) {
				errorCode=TemplateImport.INCORRECT_FILE_FORMAT; 
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
			float sR= new Double(sampleRateML.getArray()[0][0]).floatValue();
			
			//now create waveform
//			System.out.println("Create a waveform with " + waveform.length + " samples with a sample rate of "
//					+ sR + " Hz");
			MatchTemplate matchedTemplate = new MatchTemplate("Imported Waveform", waveform, sR);
			return matchedTemplate; 
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null; 
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
