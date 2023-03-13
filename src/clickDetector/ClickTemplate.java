package clickDetector;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**Class for storing click templates. 
 * <p>
 * Note that the average log spectrum must also be saved as this cannot be back calculated from the average spectrum (see ClickSpectrumTemplateEditDialog class for details) 
 * <p>
 * @author Jamie Macaulay
 *
 */
public class ClickTemplate   {
	
	float sampleRate; //samplerate of spectrum
	String species;		//user input species
	ArrayList<Double> fft; //fft of spectrum
	ArrayList<Double> std; //standard deviation of each fft bin
	ArrayList<Double> fftLog; //mean of each 10*log(b10)(fft) bin
	ArrayList<Double> stdLog; //std of each 10*log(b10)(fft) bin
	ArrayList<Double> freqUnits; //freq units (unused)
	Color color; //color to output template as.
	Integer N; //number of clicks which made up template

	
	public ClickTemplate(){
		this.fft=null;
		this.fftLog=null;
		this.sampleRate=0;
		species="unknown";
	}
	
//	public ClickTemplate(ArrayList<Double> fft, float samplerate ){
//		this.fft=fft;
//		this.sampleRate=samplerate;
//		species=null;
//	}
//	
//	
//	public ClickTemplate(ArrayList<Double> fft, float samplerate, String species){
//		this.fft=fft;
//		this.sampleRate=samplerate;
//		this.species=species;
//	}
//	
	public ClickTemplate(ArrayList<Double> fft, ArrayList<Double> std, ArrayList<Double> fftLog, ArrayList<Double> stdLog, float samplerate, String species){
		this.std=std;
		this.fft=fft;
		this.fftLog=fftLog;
		this.stdLog=stdLog; 
		this.sampleRate=samplerate;
		this.species=species;
	}
	
	public ClickTemplate(ArrayList<Double> fft, ArrayList<Double> std, ArrayList<Double> fftLog, ArrayList<Double> stdLog, float samplerate, String species, Color color){
		this.std=std;
		this.fft=fft;
		this.fftLog=fftLog;
		this.stdLog=stdLog; 
		this.sampleRate=samplerate;
		this.species=species;
		this.color=color;
	}
	
	
	public int getN(){
		return N;
	}
	
	public float getSampleRate(){
		return sampleRate;
	}
	
	public String getSpecies(){
		return species;
	}
	
	public ArrayList<Double>  getSpectrum(){
		return  fft;
	}
	
	public ArrayList<Double>  getSpectrumLog(){
		return  fftLog;
	}
	
	public ArrayList<Double>  getSpectrumStd(){
		return  std;
	}
	
	public ArrayList<Double>  getSpectrumStdLog(){
		return  stdLog;
	}
	
	public void setN(int N){
		 this.N=N;
	}
	
	public void setSampleRate(float sampleRate){
		this.sampleRate=sampleRate;
	}
	
	public void setSpecies(String species){
		this.species=species;
	}
	
	public void  setSpectrum(ArrayList<Double> fft){
		this.fft=fft;
	}
	
	public void  setSpectrumLog(ArrayList<Double> fftLog){
		this.fftLog=fftLog;
	}
	
	public void  setSpectrum(double[] spectrum1D){
		ArrayList<Double> spectrum=new ArrayList<Double>();
		for (int i=0; i<spectrum1D.length; i++){
			spectrum.add(spectrum1D[i]);
		}
		
		this.fft=spectrum;
	}
	
	public void  setSpectrumLog(double[] spectrum1D){
		ArrayList<Double> spectrumLog=new ArrayList<Double>();
		for (int i=0; i<spectrum1D.length; i++){
			spectrumLog.add(spectrum1D[i]);
		}
		
		this.fftLog=spectrumLog;
	}
	
	public void  setSpectrumStd(ArrayList<Double> std){
		this.std=std;
	}
	
	public void  setSpectrumStdLog(ArrayList<Double> stdLog){
		this.stdLog=stdLog;
	}
	
	
	public void  setSpectrumStd(double[] spectrum1D){
		ArrayList<Double> spectrum=new ArrayList<Double>();
		for (int i=0; i<spectrum1D.length; i++){
			spectrum.add(spectrum1D[i]);
		}
		
		this.std=spectrum;
	}
	
	public void  setSpectrumStdLog(double[] spectrum1DLog){
		ArrayList<Double> spectrumLog=new ArrayList<Double>();
		for (int i=0; i<spectrum1DLog.length; i++){
			spectrumLog.add(spectrum1DLog[i]);
		}
		
		this.stdLog=spectrumLog;
	}
	
	public void setColor(Color fftColour) {
		this.color=fftColour;
	}

	public Color getColour() {
		if (color==null){
			return new Color(0.5f,0.5f,0.5f);
		}
		else{
		return color;
		}
	}
	

	
	/**
	 * Creates an ArrayList of zeros.
	 * @param size. Number of zeros in the ArrayList;
	 * @return ArrayList of zeros. 
	 */
	private static ArrayList<Double> createZeroArray(int size){
		ArrayList<Double> zeros=new ArrayList<Double>();
		for (int i=0; i<size; i++){
			zeros.add(0.0);
		}
		return zeros; 
	}
	
	/**
	 * <p>
	 * Writes a .csv with mean fft and standard deviation of each fft bin. First line==mean fft, second line==standard deviation FFT,third line==mean 10*log(FFT), fourth line==standard deviation 10*log(FFT). All lines have the following format.
	 * <p>
	 * Format:
	 * <p>
	 * 0=Samplerate.
	 * <p>
	 * 1=Possible species.
	 * <p>
	 * 2=Color1=red.
	 * <p>
	 * 3=Color2=blue.
	 * <p>
	 * 4=Color3=green.
	 * <p>
	 * 5-N=fft result (usually 256 bins long).
	 * <p>
	 * @param clickTemp- a click template.
	 * @param allFFT- an arraylist of fft's which made up the click spectrum. 
	 * @param outputFileForAnalysedResults. File name for the output.csv file
	 */
	public static void writeClickTemptoFile(ClickTemplate clickTemp, ArrayList<ArrayList<Double>> allFFT, String outputFileForAnalysedResults){
		
		ArrayList<ArrayList<Double>> fftTemplate=new 	ArrayList<ArrayList<Double>>();

		fftTemplate.add(clickTemp.getSpectrum());
		if (clickTemp.getSpectrumStd()!=null) 	fftTemplate.add(clickTemp.getSpectrumStd());
		else fftTemplate.add(createZeroArray(clickTemp.getSpectrumStd().size()));
		fftTemplate.add(clickTemp.getSpectrumLog());
		if (clickTemp.getSpectrumStdLog()!=null) fftTemplate.add(clickTemp.getSpectrumStdLog());
		else fftTemplate.add(createZeroArray(clickTemp.getSpectrumStdLog().size()));
		
		if (allFFT!=null){
			for (int i=0;i<allFFT.size();i++){
				fftTemplate.add(allFFT.get(i));
			}
		}
		
		if (fftTemplate.size()==0)return;
		
		String header=createHeader( clickTemp);
		
		if (header==null) return;
		
//		System.out.println("fftSize: "+fftTemplate.size());
//		System.out.println("fftLog: "+clickTemp.getSpectrumLog());
//		System.out.println("fftstdLog: "+clickTemp.getSpectrumStdLog());
		
 		writeFile( header, fftTemplate,	 outputFileForAnalysedResults );
 		
	}
	
	public static void writeClickTemptoFile(ClickTemplate clickTemp, String outputFileForAnalysedResults){
		 writeClickTemptoFile( clickTemp,  null,  outputFileForAnalysedResults);
	}
	
	
	private static void writeFile(String header, ArrayList<ArrayList<Double>> fft,	String outputFileForAnalysedResults ){
		String results;
		File resultsFile=new File(outputFileForAnalysedResults);
		try {
		
					 	FileWriter fw = new FileWriter(resultsFile, true);

					 	//writefft data
					 	fw.write(header + "\n");
//					 	System.out.println("Saving click template results....");
					 	for (int i=0; i<fft.size(); i++){
					 		results=Double.toString(fft.get(i).get(0));
					 		for (int j=1;j<fft.get(i).size(); j++){
					 			results=results+","+fft.get(i).get(j);
								//System.out.println(Results);
					 		}
					 		fw.write(results + "\n");
					 	}
					 	fw.flush();
				        fw.close();
//				        System.out.println("Saved....");
		
		} 
		catch (IOException e) {   
			System.out.println("Error in saving click Template");
			e.printStackTrace();
		} 
	}
	
	
	private static String createHeader(ClickTemplate clickTemp){
		
		String species=clickTemp.getSpecies();
		Float sampleRate=clickTemp.getSampleRate();
		Color color=clickTemp.getColour();
		float[] colorComponents = new float[4];
		color.getColorComponents(colorComponents);
		Integer N=clickTemp.getN();
			
		String header;
		
		//write samplerate
 		if (sampleRate ==null)return null;
 		header=sampleRate.toString()+",";
 		//write species
 		if (species ==null){
 			header=header+"unknown"+",";
 		}
 		else{
 			header=header+species+",";
 		}
 		if (N !=null){
 			header=header+N+",";
 		}
 		else{
 			header=header+0+",";
 		}
 		//write colour
 		Float col1=colorComponents[0];
 		header=header+col1.toString()+",";
 		col1=colorComponents[1];
 		header=header+col1.toString()+",";
 		col1=colorComponents[2];
 		header=header+col1.toString()+",";
 		
// 		System.out.println("header: "+header);

		return header;

	}



	/**
	 * Get a click template. 
	 * Format
	 * 0=Samplerate
	 * 1=Possible species
	 * 2=Color1=red
	 * 3=Color2=blue
	 * 4=Color3=green
	 * 5-N=fft results
	 * @param filename
	 * @return ClickTemplate from file
	 */

	public static ClickTemplate getCSVResults(String filename) {
	 
	 ClickTemplate clickTemplate=new ClickTemplate();
	 
		Collection<String> lines = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(
				new File(filename)
			));
			String line;
			while ((line = br.readLine()) != null) {
				lines.add(line);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	
		
		ArrayList<ArrayList<Double>> CSVResults=new ArrayList<ArrayList<Double>>();
		int N=0;
		float sampleRate=0;
		String species=null;
		Float r;
		Float g;
		Float b;
		Color color=null;
		Integer numberClicks=0;
		
		
		for (String line : lines) {
		
			if (N>4) break ;
			
			String[] recordsOnLine = line.split(",");
			
			ArrayList<Double> Data=new ArrayList<Double>();
			String LineBegin=recordsOnLine[0];
			char FirstChar=LineBegin.charAt(0);
			
			if (N==0){
				sampleRate=(float) Double.parseDouble(recordsOnLine[0]);
				if (sampleRate<1) return null;
				species=recordsOnLine[1];
				numberClicks=Integer.parseInt(recordsOnLine[2]);
				r=Float.parseFloat(recordsOnLine[3]);
				g=Float.parseFloat(recordsOnLine[4]);
				b=Float.parseFloat(recordsOnLine[5]);
				if (r<=1 && g<=1 && b<=1 && r>=0 && g>=0 && b>=0){
				color=new Color(r,g,b);
				}
				else{
				color=new Color(0.5f,0.5f,0.5f);
				}
				
			}
		
			
			if (N>0){
			for (int i=0; i<recordsOnLine.length; i++){
				//System.out.println(i);
				double Dat = Double.parseDouble(recordsOnLine[i]);
				
				//System.out.println(Dat);
			Data.add(Dat);
			}
			CSVResults.add(Data);
			}
			N++;
			
		}
		
	
		clickTemplate.setSpectrum(CSVResults.get(0));
		clickTemplate.setSpectrumStd(CSVResults.get(1));
		clickTemplate.setSpectrumLog(CSVResults.get(2));
		clickTemplate.setSpectrumStdLog(CSVResults.get(3));
		clickTemplate.setN(numberClicks);
		clickTemplate.setSampleRate(sampleRate);
		clickTemplate.setSpecies(species);
		clickTemplate.setColor(color);
//		System.out.println("Sample Rate: "+sampleRate+ " fft"+ CSVResults);
		
		return clickTemplate;
	}
	

	
}
