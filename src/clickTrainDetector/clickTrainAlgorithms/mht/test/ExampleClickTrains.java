package clickTrainDetector.clickTrainAlgorithms.mht.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import PamUtils.PamArrayUtils;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtMAT.SimpleClick;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.format.Mat5File;
import us.hebi.matlab.mat.types.Cell;

/**
 * Simulates some clicks to test the MHT algorithm on. 
 * @author Jamie Macaulay 
 *
 */
public class ExampleClickTrains {
	


	public enum SimClickImport {
		
		/**
		 * 3 interleaved clicks trains at constant amplitudes and ICIs but with a little varience. 
		 * Should be a fairly simple problem for the click train algorithm to solve. 
		 */
		SIMCLICKS_1, 
		
		/**
		 * A single click train with gaps to test coasting. 
		 */
		SIMCLICKS_2,
		
		/**
		 * Two closesly interleved click trains which have different waveforms. 
		 */
		SIMCLICKS_3,
		
		
		/**
		 * Example of dolphins on a single channel soundtrap
		 */
		DOLPHINS_ST
		
		
	};

	/**
	 * Default path to the simulated clicks. 
	 */
	public static String masterPath =  "E:\\Google Drive\\Programming\\MATLAB\\click_train_detector\\example_data\\"; 

	/**
	 * 
	 */
	private SimpleClickDataBlock simpleClicks;


	public ExampleClickTrains() {
		//create simple clicks. 
		//simClicks1() ;
	}
	
	/**
	 * The simulated clicks to load.
	 * @param type
	 */
	public ExampleClickTrains(int type) {
		//create simple clicks. 
		//simClicks1() ;
	}

	
	/**
	 * Get a .mat file containing simulated clicks. 
	 * @param type - the file type 
	 * @return the file. 
	 */
	public File getSimClicksFile(SimClickImport type) {
		String path = null; 
		switch (type) {
		case SIMCLICKS_1:
			 path = masterPath + File.separator + "simtrains_1.mat";
			 break;
		case SIMCLICKS_2:
			 path = masterPath + File.separator + "simtrains_2.mat";
			 break;
		case DOLPHINS_ST:
			 path = masterPath + File.separator + "20190801_212731_clicks_dolphins_ST.mat";
			 break;
		case SIMCLICKS_3:
			 path = masterPath + File.separator + "simtrains_3_corr.mat";
			 break;
		}
		File file = new File(path); 
		
		return file; 
	}
	
	/**
	 * Import clicks from an MHT file. 
	 * @param file - the .mat file to import. 
	 * @return the datablock containg simple clicks. 
	 */
	public SimpleClickDataBlock importSimClicks(File file) {
	
		ArrayList<SimpleClick> simpleClicks = importSimpleClickMAT(file);  
		//System.out.println("No. clicks: " + simpleClicks); 

		SimpleClickDataBlock simpleClickDataBlock = new SimpleClickDataBlock();
		simpleClickDataBlock.addPamData(simpleClicks);

		this.simpleClicks=simpleClickDataBlock;
		
		return this.simpleClicks; 
	}
	
	/**
	 * Convenience function to import simulated click trains. 
	 * @param type - flag for the simulated clicks to import. 
	 * @return a data block containing the simulated clicks. 
	 * 
	 * 
	 */
	public SimpleClickDataBlock importSimClicks(SimClickImport type) {
		File file  = getSimClicksFile(type);
		return importSimClicks(file);
	}

	/**
	 * Import a list of time, amplitude clicks from a .mat file. 
	 * @param file - the file. 
	 */
	private ArrayList<SimpleClick> importSimpleClickMAT(File file) {
		Mat5File mfr = null; 
		try {
			if (file ==null) {
				System.out.println("The imported file is null");
				return null;
			}
			
			
			mfr = Mat5.readFromFile(file);
			
			
				mfr.getCell("clicksMHT");

			//get array of a name "my_array" from file
			Cell mlArrayRetrived = mfr.getCell("clicksMHT");
			Double mlSampleRate = mfr.getMatrix( "sR" ).getDouble(0);
			
			
			ArrayList<SimpleClick> simpleClicks= new ArrayList<SimpleClick>();
			
			//now the cell array can be anywhere from 2- 6 columns depending on the data contained. 
			int nColumns = mlArrayRetrived.getNumRows();
			int nClks = mlArrayRetrived.getNumCols();

			SimpleClick simpleClick;
			
			Integer UID = null; 
			Double timeSeconds = null;
			Double amplitude = null; 
			Double bearing = null;
			double[][] waveform =null;
			
			float sR =(float) mlSampleRate.doubleValue();

			
			for (int i=0; i<nClks; i++) {
				
			
				
				//first column is UID (int32)
				UID =  mlArrayRetrived.getCell(i,0).get(0);
				
				//second column is time in seconds (double)
				timeSeconds =  mlArrayRetrived.getCell(i,1).get(0);

				//third column is amplitude in dB (double)
				if (nColumns>2) amplitude = mlArrayRetrived.getCell(i,2).get(0);

				//fourth column is bearing in degrees (double)
				if (nColumns<3) bearing = mlArrayRetrived.getCell(i,3).get(0);

				//fifth column is a waveform array (double[][])
				if (nColumns>4) waveform = PamArrayUtils.matrix2array(mlArrayRetrived.getCell(i,4).getMatrix(0));
				
				//tranpose the waveform
				waveform = PamUtils.PamArrayUtils.transposeMatrix(waveform); 
	
//				System.out.println("Waveform size is: " + waveform[0].length);

				simpleClick = new SimpleClick( UID,  timeSeconds,  amplitude,  bearing,  waveform[0],  sR); 
				
				simpleClicks.add(simpleClick); 
			}
			
			return simpleClicks; 
		}
		catch (Exception e) {
			e.printStackTrace();
			return null; 
		}
	}

	/**
	 * Simulate a set of clicks without importing any clicks.  
	 */
	public void simClicks1() {
		
		ArrayList<SimpleClick> simpleClicks = new ArrayList<SimpleClick>(); 

		//ICI = 0.32, amplitude =160; 
		simpleClicks.addAll(generateClickSet(0.32, 160, 0,
				5, 0.01, 10));

		//ICI = 0.01, amplitude =110; 
		simpleClicks.addAll(generateClickSet(0.2, 110, 0.01,
				10, 0.00001, 5));

		//ICI = 0.32, amplitude =160; 
		simpleClicks.addAll(generateClickSet(0.01, 130, 0.6,
				2, 0.00001, 2));

		//sort into chronological order
		Collections.sort(simpleClicks, (a, b) -> a.compareTo(b));

		System.out.println("Number of clicks: " + simpleClicks.size()); 

		SimpleClickDataBlock simpleClickDataBlock = new SimpleClickDataBlock();
		simpleClickDataBlock.addPamData(simpleClicks);

		this.simpleClicks=simpleClickDataBlock;
	}



	/**
	 * Simulates a single tracks and adds to the data block after clearing previous data. 
	 * @param ICI - the inter click interval
	 * @param amplitude - the amplitude in dB
	 * @param start - the start in seconds
	 * @param end - the end in seconds
	 * @param randTimeFactor - the random error to add to the time 
	 * @param randAmpFactor - the random error to add to the amplitude. 
	 * @return the time series of simple clicks. 
	 */
	public void simClickTrack(double ICI, double amplitude, double start,
			double end, double randTimeFactor, double randAmpFactor) {
		ArrayList<SimpleClick> simpleClicks = new ArrayList<SimpleClick>(); 

		//ICI = 0.32, amplitude =160; 
		simpleClicks.addAll(generateClickSet(ICI, amplitude, start,
				end, randTimeFactor, randAmpFactor));

		SimpleClickDataBlock simpleClickDataBlock = new SimpleClickDataBlock();
		simpleClickDataBlock.addPamData(simpleClicks);

		this.simpleClicks=simpleClickDataBlock;
	}

	/**
	 * Generate a set of clicks. 
	 * @param ICI - the inter click interval
	 * @param amplitude - the amplitude in dB
	 * @param start - the start in seconds
	 * @param end - the end in seconds
	 * @param randTimeFactor - the random error to add to the time 
	 * @param randAmpFactor - the random error to add to the amplitude. 
	 * @return the time series of simple clicks. 
	 */
	public ArrayList<SimpleClick> generateClickSet(double ICI, double amplitude, double start,
			double end, double randTimeFactor, double randAmpFactor){
		ArrayList<SimpleClick> simpleClicks= new ArrayList<SimpleClick>();

		double time=start; 
		int UID =0;
		while (time<end) {
			simpleClicks.add(new SimpleClick(UID, time+ randTimeFactor*(Math.random()-0.5), amplitude+ randAmpFactor*(Math.random()-0.5), SimpleClickDataBlock.defaultSR));
			time=time+ICI; 
			UID++;
		}

		return simpleClicks; 
	}

	/**
	 * Get the last laoded set of simulated clicks. 
	 * @return data block containing the simulated clicks. 
	 */
	public SimpleClickDataBlock getSimClicks(){
		return simpleClicks; 
	}


}
