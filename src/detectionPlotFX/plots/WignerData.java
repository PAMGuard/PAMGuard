package detectionPlotFX.plots;

import PamUtils.complex.ComplexArray;
import fftManager.FastFFT;

/*
 *Stores and handles Wigner Data. Includes a compression algorithm so that extremely large wigner images can still be displayed  and memory
 *usage can be managed effectively. 
 */
public class WignerData {

	/**
	 * The data to draw a Wigner plot. 
	 */
	private double[][] wignerData; 

	/**
	 * Temporary data 
	 */
	private double[][] temporaryDataBin;

	/**
	 * The max image width can be 2048. Note that due to rounding to a discrete number of bins this 
	 * the actual size of the array may be a few bins below or above this maximum values. 
	 */
	private int maxDataWidth=2048; 

	/**
	 * The max image width can be 2048. Note that due to rounding to a discrete number of bins this 
	 * the actual size of the array may be a few bins below or above this maximum values. 
	 */
	private int maxDataHeight=2048;

	/**
	 * The number of raw bins in height that correspond to a single data point in {@link wignerData}
	 */
	private int binHeight;

	/**
	 * The number of raw bins in width that correspond to a single data point in {@link wignerData}
	 */
	private int binWidth;

	/**
	 * The total number of bins expected for the wigner plot
	 */
	private int rawBins;

	/**
	 * True if needs compression
	 */
	private boolean needsCompression; 



	/**
	 * Reset the curretn data. 
	 * @param bins - the number of bins for the new data. 
	 */
	public void resetData(int bins) {
		this.rawBins=FastFFT.nextBinaryExp(bins); 

//		System.out.println("No. raw bins bins: " + bins + " next binary exponential: " + rawBins);

		if (rawBins<=maxDataWidth && rawBins<=maxDataHeight) {
			needsCompression=false; 
			wignerData=new double[rawBins][rawBins]; 
			binWidth=1;
			binHeight=1; 
			temporaryDataBin = new double[1][rawBins]; 
		}
		else {
			needsCompression=true; 

			//work out the compression level required. 
			binWidth = (int) Math.ceil(rawBins /(double) maxDataWidth); 

			binHeight = (int) Math.ceil(rawBins /(double) maxDataHeight); 		

			//now work out the dimensions of the new data. Should be close to 
			int dataWidth=(int) Math.ceil(rawBins/ binWidth); 
			int dataHeight=(int) Math.ceil(rawBins/binHeight); 

			//wigner data.
			wignerData=new double[dataWidth][dataHeight];

//			System.out.println("Array: dataWidth: " + dataWidth +  " dataHeight: " + dataHeight + 
//					"vbinWidth " + binWidth + " binHeight " + binHeight + " rawBins: " + rawBins);

			//temporary array which holds line. 
			temporaryDataBin = new double[binWidth][dataHeight]; 
		}

		lineCount=0; 

	}


	/**
	 * The current line cound
	 */
	int lineCount = 0;

	/**
	 * Add a date line. Nearest neighbour averaging.
	 * @param complexArray - the complex array for a line of the raw wigner data
	 * @param imageBin - the image bin index
	 */
	public void addDataLine(ComplexArray complexArray, int imageBin) {
		//calculate real data
		double[] realData = new double[complexArray.length()];
		for (int j = 0; j < complexArray.length(); j++) {
			realData[j] = complexArray.getReal(j);
		}
		
//		if (!needsCompression) {
//			wignerData[imageBin]=realData; 
//			return; 
//		}

		//need to compress the image compress in y direction for each line. 
		int n=0; 
		double meanVal =0.;
		for (int j = 0; j < realData.length; j++) {
			meanVal+=realData[j]; 
			if (j%binHeight==0) {
				//System.out.println(" j " + j + " n: " +  n + " length: " + realData.length + " rawBins: " + rawBins); 
				meanVal=meanVal/binHeight;
				temporaryDataBin[imageBin%binWidth][n]=meanVal; 
				meanVal=0; 
				n++;
			}
		}


		//once we havea total width bin then compress width wise. 
		if (imageBin%binWidth==0) {
			//now compress array laterally and add to main wignerData
			meanVal=0; 

//			System.out.println(" Temporary data bin: " + temporaryDataBin[0].length + " "
//					+ temporaryDataBin.length + " line count: " + lineCount+ " Example data: ");
//			for (int m=0; m<temporaryDataBin[0].length ; m++) {
//				System.out.print(" " + temporaryDataBin[0][m]);
//			}
//			System.out.println("");


			for (int i=0; i<temporaryDataBin[0].length; i++) {
				for (int j=0; j<temporaryDataBin.length; j++) {
					meanVal+=temporaryDataBin[j][i];
				}
				meanVal=meanVal/temporaryDataBin.length;

				//add to the final data array. 
				wignerData[lineCount][i]=meanVal; 
//				System.out.print(" " + meanVal);
				meanVal=0; 
			}
			
//			System.out.println("");

			lineCount++;

			//now need a new array. 
			temporaryDataBin = new double[binWidth][wignerData[0].length]; 
		}

	}


	/**
	 * Set the Wigner data
	 * @param calcWignerData - an N x M array representing a wigner surface 
	 */
	public void setWignerData(double[][] calcWignerData) {
		this.wignerData=calcWignerData; 

	}

	/**
	 * Get the Wigner data. 
	 * @return an array representing a wigner surface
	 */
	public double[][] getWignerData() {
		return wignerData;
	}



}
