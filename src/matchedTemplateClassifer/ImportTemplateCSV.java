package matchedTemplateClassifer
;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import PamUtils.PamArrayUtils;
import PamUtils.TxtFileUtils;

/**
 * Import a template from a csv file. 
 * @author Jamie Macaulay 
 *
 */
public class ImportTemplateCSV implements TemplateImport {
	
	private String[] extensionStrings = new String[] {"csv"};
	
	private int errorCode=0;
	
	@Override
	public MatchTemplate importTemplate(File filePath){
		
		ArrayList<ArrayList<Double>> data = TxtFileUtils.importCSVData(filePath.getAbsolutePath()); 
		
		if (data==null || data.size()!=2 || data.get(1).size()<1) {
			//need two rows, waveform must be at least 10 bins long. 
			errorCode=TemplateImport.INCORRECT_FILE_FORMAT; 
			System.err.println("There is a problem with the .CSV file format");
			return null; 
		}
		
		
		if ( data.get(0).size()<TemplateImport.MIN_WAVEFORM_LENGTH) {
			errorCode=TemplateImport.ERROR_WAVEFORM_LENGTH; 
			System.err.println("There is a problem with the .CSV waveform: It is is less than " + TemplateImport.ERROR_WAVEFORM_LENGTH 
					+ " bins long");
			return null;
		}
		
		//the first row is the waveform 
		double[] waveform = new double[data.get(0).size()];
		for (int i=0; i<waveform.length; i++) {
			//System.out.println("i: " + i + " : " +  data.get(0).get(i));
			waveform[i]=data.get(0).get(i); 
		}
//		System.out.println("String sR = " +  data.get(1).get(0)); 


		//used big decimal here because String.,floatValue did not handle numbers like 3.85e05
		float sR = new BigDecimal(data.get(1).get(0)).floatValue();
		
//		float sR=data.get(1).get(0).floatValue();
		
		//System.out.println("imported waveform"); 
		//PamArrayUtils.printArrayRaw(waveform);
		
		
		//now create waveform
		System.out.println("Import a waveform with " + waveform.length + " samples with a sample rate of "
				+ sR + " Hz ");
		MatchTemplate matchedTemplate = new MatchTemplate(filePath.getName(), waveform, sR);
		//TODO		
		return matchedTemplate; 
	}
	
	/**
	 * Get extension
	 * @return the extension
	 */
	public String[] getExtension() {
		return extensionStrings; 
	}

	@Override
	public int getErrorCode() {
		return errorCode;
	}

}
