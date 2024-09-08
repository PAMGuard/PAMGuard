package difar;


import PamUtils.MatrixOps;
import Spectrogram.WindowFunction;
import difar.DifarParameters.DifarOutputTypes;
import difar.DifarProcess.DifarDemuxWorker;
import fftManager.Complex;
import fftManager.FastFFT;

/**
 * DIFAR Calculations, based on the Matlab bScanNew functions
 *
 */
public class BScan {
	
	private double[][] surfaceData;
	private double difarGain;

	public BScan(DifarControl difarControl, double[][] demuxedData,
			int fftLength,int fftHop,float sampleRate,double[] selFrequency, int nAngleSections, DifarOutputTypes difarOutputType,
			DifarDemuxWorker demuxWorker) {
		this.surfaceData = bScan(difarControl, demuxedData,
				fftLength, fftHop, sampleRate, selFrequency, nAngleSections, difarOutputType,
				demuxWorker);
	}
		/**
		 * Calculate the ambiguity surface for demultiplexed signals from a DIFAR sonobuoy.
		 * The ambiguity surface is the power as a function of magnetic bearing and frequency.
		 * This function is based on the manuscript: "Relationship of Underwater Acoustic Intensity 
		 * Measurements to Beamforming", by Gerald D'Spain, Canadian Acoustics, Proceedings Issue, 
		 * September 1994, pp. 157-158
		 *   
		 * @param difarControl 
		 * @param demuxedData
		 * @param lockData
		 * @param fftLength
		 * @param sampleRate
		 * @param selFrequency
		 * @param demuxWorker 
		 * @return
		 */
		private double[][] bScan(DifarControl difarControl, double[][] demuxedData, 
				int fftLength,int fftHop, float sampleRate,double[] selFrequency, int nAngleSections, DifarOutputTypes difarOutputType,
				DifarDemuxWorker demuxWorker){
			
			double[] freqRange = selFrequency.clone();
			double delt = freqRange[1]-freqRange[0];
			freqRange[1] = freqRange[1] + delt/4;
			freqRange[0] = freqRange[0] - delt/4;
			
			if (freqRange==null||freqRange.length!=2) {
				freqRange=new double[2];
				freqRange[0]=Double.NEGATIVE_INFINITY;
				freqRange[1]=Double.POSITIVE_INFINITY;
			}else if (freqRange[0]>freqRange[1]) {
				double tfr=freqRange[0];
				freqRange[0]=freqRange[1];
				freqRange[1] = tfr;
			}
			
			
			double[] Om = demuxedData[0];
			double[] EW = demuxedData[1];
			double[] NS = demuxedData[2];

			
			/**
			 * Removed code regarding locks and trimming data since it is not used here.
			 * It is only used during demultiplexing, which must have happened in 
			 * order to extract the omni, EW and NS channels from the DIFAR signal
			 * BSM 2014-03-06   
			 */

			if (demuxWorker != null) {
				demuxWorker.setBScanProgress(DemuxWorkerMessage.STATUS_STARTDIFARCALC, 0); // guess it's used 20% up to here. 
			}
			
			int windowType = WindowFunction.HANNING; 
			
			Complex[] OmSc = Complex.createComplexArray(spectralPower(Om, fftLength, windowType, 0, fftHop));
			Complex[] EWSc = Complex.createComplexArray(spectralPower(EW, fftLength, windowType, 0, fftHop));
			Complex[] NSSc = Complex.createComplexArray(spectralPower(NS, fftLength, windowType, 0, fftHop));

			if (demuxWorker != null) {
				demuxWorker.setBScanProgress(DemuxWorkerMessage.STATUS_DIFAR_DONE_POWER, .20); // guess it's used 20% up to here. 
			}
			
			Complex[] OmEWx = spectralCross("OmEWx: ", Om, EW, fftLength, windowType, 0, fftHop);
			Complex[] OmNSx = spectralCross("OmNSx: ", Om, NS, fftLength, windowType, 0, fftHop);
			Complex[] EWNSx = spectralCross("EWNSx: ", EW, NS, fftLength, windowType, 0, fftHop);
			
			if (demuxWorker != null) {
				demuxWorker.setBScanProgress(DemuxWorkerMessage.STATUS_DIFAR_DONE_CROSS, .40); // guess it's used 20% up to here. 
			}
			
			/*
			 * Frequency bins from DC to Nyquest should be 1+ fftLength/2?;
			 * Seems we are ignoring the nyquist frequency, which is ok by me.
			 */
			int nfbins=fix(fftLength/2);
			double fstep=(sampleRate/2)/nfbins;
			
	//		freqBins=((1:nfbins)*fstep)-(fstep/2); % the center frequencies of each bin
			double[] freqBins=new double[nfbins];
			for (int fBin=0;fBin<nfbins;fBin++){
				freqBins[fBin]=fBin*fstep;
			}
			
	//		%the frequency  bins below 15 Hz are discarded
			int fbinmin=(int) Math.ceil(15/fstep);
			
			fbinmin = Math.max(fbinmin ,fix(freqRange[0]/fstep));
	
	//		% the top 10 percent of the bins are discarded because they are in the
	//		% rollof of the resampling filter
			int fbinmax = fix(nfbins-(0.1*nfbins));
			fbinmax = Math.min(fbinmax ,fix(freqRange[1]/fstep));
	
	//		%the steering vectors are a 1X3 matrix for each step in azimuth (theta)
			double radstep=2*Math.PI/nAngleSections; // % step in radians
			int nazsteps=nAngleSections;  // % number of steps in azimuth
			
			Complex[][] OutB = Complex.allocateComplexArray(nazsteps, nfbins);
			
	//		System.out.println("OmSc length ; "+OmSc.length);
			
			for (int az=0;az<nazsteps;az++){
				double theta = (az)*radstep;
	//			System.out.println("theta:"+theta);
	//			svec=[0.5, 0.5*Math.sin(theta), 0.5*Math.cos(theta)];
	//			double[] svec = new double[3];
	//			svec[0]=0.5;
	//			svec[1]=0.5*Math.sin(theta);
	//			svec[2]=0.5*Math.cos(theta);
				
				Complex[][] sVec=new Complex[1][3];
				sVec[0][0]=new Complex(0.5,0);
				sVec[0][1]=new Complex(0.5*Math.sin(theta),0);
				sVec[0][2]=new Complex(0.5*Math.cos(theta),0);
				
				//TODO make this only be done for frequencies interested in
	//			for f = fbinmin:fbinmax, % for each frequency bin 
				for (int fB=fbinmin;fB<fbinmax;fB++){
				
	//				% form the 3X3 Data Cross Spectral Matrix, ignoring the conversion factors rho-c
	//				Sxm = [OmS(f),  OmEWx(f), OmNSx(f);...
	//					  OmEWx(f), EWS(f),   EWNSx(f);...
	//					  OmNSx(f), EWNSx(f), NSS(f) ];
					
					Complex[][] Sxm = new Complex[3][3];
	//				try{
						Sxm[0][0]=	OmSc[fB];
						Sxm[0][1]=	OmEWx[fB];
						Sxm[0][2]=	OmNSx[fB];
						
						Sxm[1][0]=	OmEWx[fB];
						Sxm[1][1]=	EWSc[fB];
						Sxm[1][2]=	EWNSx[fB];
						
						Sxm[2][0]=	OmNSx[fB];
						Sxm[2][1]=	EWNSx[fB];
						Sxm[2][2]=	NSSc[fB];
					
	//				}catch(Exception e){
	//					System.out.println("fB: "+fB+ "  az;"+az);
	//					e.getMessage();
	//				}
					switch (difarOutputType) {
					case BARTLETT:
						
						Complex[][] p1 = MatrixOps.transposeMatrix(sVec);
						Complex[][] p2 = MatrixOps.complexMatrixCross(Sxm,p1);
						
						Complex[][] ent = MatrixOps.complexMatrixCross(sVec,p2 );
						if (ent!=null&&ent.length==1&&ent[0].length==1){
							if (ent[0][0]==null){
								System.out.println("fB: "+fB+ "  az;"+az);
							}
							
							
							OutB[az][fB]=ent[0][0];
						}else{
							return null;
					}
						break;
					case MVDR:
						
						Complex[][] ent2 = MatrixOps.complexMatrixCross(sVec, MatrixOps.complexMatrixCross(MatrixOps.inverse33Matrix(Sxm),MatrixOps.transposeMatrix(sVec)));
						
						
						if (ent2!=null&&ent2.length==1&&ent2[0].length==1){
							OutB[az][fB]=MatrixOps.recipComp(ent2[0][0]);
						}else{
							
							return null;
						}
						break;
					default:
						break;
					}
					
	//				switch outputType  
	//					case 'Bartlett' %(McDonald version)
	//						OutB(az,f)=svec*Sxm*svec'; 
	//					case 'MVDR'	 % MVDR beamformer (Thode modification)
	//						OutB(az,f)=1./(svec*inv(Sxm)*svec');
	//				end
	//			 end
				}
				if (demuxWorker != null && (az+1)%10 == 0) {
					double percComplete = (double) (az+1)/nazsteps;
					percComplete = percComplete * .6 + .40;
					demuxWorker.setBScanProgress(DemuxWorkerMessage.STATUS_DIFAR_ANGLES, percComplete); // guess it's used 20% up to here. 
				}
			}
			
			
			double[][] OutBR = MatrixOps.getRealMatrix(OutB);

			return OutBR;
		}

	private int fix(double dub){
		return new Double(Math.floor((dub))).intValue();
	}
	
	private double getWindowTSV(double[] windowFunction){
		double tsv=0;
		for (double val:windowFunction){
			tsv+=val*val;
		}
		return tsv;
		
	}
	
	
	/**
	 * This is the cross-spectral density of signals x, and y. It is similar 
	 * to the power-spectral density of a single signal, but is applied to
	 * two different signals. Signals x, and y must be of the same length
	 * Consider consolidating this function into a general purpose signal
	 * processing package such as PamMaths.
	 * From "Signal Processing Algorithms in Matlab" by S.D. Stearns and
	 * R. A. David, Prentice Hall 1996
	 * @param x
	 * @param y
	 * @param fftLength
	 * @param windowType - hann etc - see Spectrogram.WindowFucntion
	 * @param windowParam - used if need tukey windowWunction
	 * @param fftHop
	 * @return
	 */
	private Complex[] spectralCross(String prefix,double[] x,double [] y, int fftLength ,int windowType, double windowParam, int fftHop ){
		
		
		int N=x.length;
		
		if (N!=y.length){
			System.out.println("SPCROS: Lengths of x and y are unequal. - ");//   +this.getClass().getName());
			return null;
		}else if(N<fftLength) {
			System.out.println("SPCROS: DFT size exceeds data length. - "+N+" < "+fftLength);//   +this.getClass().getName());
			return null;
			
		}else if(fftLength<8||fftLength%2==1) {
			System.out.println("SPCROS: DFT size (NDFT) is too small or is odd. - ");//   +this.getClass().getName());
			return null;
		}
		
		int Ny = fftLength/2;
		
		//empty array 
		Complex [] z=Complex.allocateComplexArray(Ny);
		
		double[] windowFunction = null;
		if (windowType>=WindowFunction.NWINDOWS){
			windowFunction=WindowFunction.tukey(fftLength, windowParam);
		}else{
			windowFunction=WindowFunction.getWindowFunc(windowType, fftLength);
		}
		
		double tsv=getWindowTSV(windowFunction);
		fftHop = Math.max(1,fftHop);
		int nshift = Math.min(fftLength,fftHop);
		FastFFT fFFT = new FastFFT();
		
		int nsgmts=fix((N-fftLength)/nshift);
		for (int isegmt = 0; isegmt < nsgmts; isegmt++) {
			
			int index = nshift * isegmt;

			Complex[] temp = new Complex[fftLength];

			for (int t = 0; t <  fftLength; t++) {
				temp[t] = new Complex(x[t+index], y[t+index]);
			}
			
			if (!MatrixOps.applyMask(temp, windowFunction)) {
				System.out.println("apply window function failed! isegmt; "+isegmt);
				return null;
			}

			// FFT(X,N) is the N-point FFT, padded with zeros if X has less
			// than N points and truncated if it has more.

			// fft of complex ary x
			fFFT.fft(temp);

			z[0] = z[0].plus(temp[0].real / (tsv * nsgmts) * temp[0].imag / (tsv * nsgmts));
	
			for (int m = 1; m < Ny; m++) {
				Complex p = temp[m].plus(temp[fftLength - m]);
				Complex q = temp[m].minus(temp[fftLength - m]);

				Complex u = new Complex(p.real / 2, q.imag / 2);
				Complex v = new Complex(p.imag / 2, -q.real / 2);

				z[m] = z[m].plus(u.conj().times(v.times(1./(tsv * nsgmts))));
			}
		}
		
		return z;
		
	}
	
	
	
	/**
	 * This returns the power spectral density of a signal x using Welch's method 
	 * Maybe consider moving this to PamMaths, or some other package that 
	 * consolidates signal processing functions.
	 * Adapted from "Signal Processing Algorithms in Matlab" by S.D. Stearns and
	 * R. A. David, Prentice Hall 1996
	 * @param x
	 * @param y
	 * @param fftLength
	 * @param windowType - hann etc - see Spectrogram.WindowFucntion
	 * @param windowParam - used if need tukey windowWunction
	 * @param fftHop
	 * @return array can be null if broken!! TODO fix this
	 */
	private double[] spectralPower(double[] x, int fftLength ,int windowType, double windowParam, int fftHop ){
		int M=fix(fftLength/2);
		if (fftLength<8){
			System.out.println("fft < 8");
			return null;
		}else if(x.length<fftLength) {
			System.out.println("x len < fft");
			return null;
			
		}
		
		double[] windowFunction = null;
		if (windowType>=WindowFunction.NWINDOWS){
			
			windowFunction=WindowFunction.tukey(fftLength, windowParam);
		}else{
			windowFunction=WindowFunction.getWindowFunc(windowType, fftLength);
		}
		
		double tsv = getWindowTSV(windowFunction);
		
		double[] pds = new double[M];
		int nshift = fix(Math.min(fftLength,Math.max(1,fix(fftHop+.5))));
		int nsgmts = fix(1+(x.length-fftLength)/nshift);
		FastFFT fFFT = new FastFFT();
		for (int isegmt=0;isegmt<nsgmts;isegmt++){
			double[] temp = new double[fftLength];

			for (int t = 0; t <  fftLength; t++) {
				temp[t] = x[isegmt*nshift+t];
			}
			if (!MatrixOps.applyMask(temp, windowFunction)) {
				System.out.println("apply window function failed! isegmt; "+isegmt);
				return null;
			}
			Complex[] ytemp;//=Complex.allocateComplexArray(temp.length);
			ytemp=fFFT.rfft(temp, null, FastFFT.log2(fftLength));
			/*
			 * This next line is a bit tricky, and perhaps a bit pedantic. The
			 * Matlab code is
			 * y(1)=y(1)+real(TEMP(1))*imag(TEMP(1))/(tsv*nsgmts); TEMP(1) is
			 * the DC component of the signal, and for real-valued signals there
			 * should be no imaginary component at 0 Hz.
			 * 
			 * However, the FastFFT class uses JTransforms, and some custome
			 * code that packs the output of JTransforms into a Complex number.
			 * When the length of the signal/FFT is an even number this process
			 * results in temp[0].imag having the amplitude of the nyquist
			 * frequency, rather than 0. This can lead to the following code
			 * giving erroneous results: z[0] = z[0].plus(temp[0].real / (tsv *
			 * nsgmts) * temp[0].imag / (tsv * nsgmts));
			 * 
			 * Since we know that the FFTLength will always be a power of two,
			 * we will simply hard-code the imaginary value to be zero, which
			 * will always make z[0] = 0;. This means continuing to ignore the
			 * nyquist frequecny. I'm not sure whether this makes a difference
			 * in the results or not since we aren't usually interested in 0 Hz
			 * or the nyquist frequency.
			 */
			ytemp[0].assign(ytemp[0].real, 0);
			for (int m = 0; m < M; m++) {
				pds[m] += (ytemp[m].real*ytemp[m].real+ytemp[m].imag*ytemp[m].imag)/(tsv*nsgmts);
			}
			
			
//		end
		}
		
		return pds;
	}
	
	
	/**
	 * @return the surfaceData
	 */
	public double[][] getSurfaceData() {
		return surfaceData;
	}
	/**
	 * @return the difarGain
	 */
	public double getDifarGain() {
		return difarGain;
	}
	
		
		
		
		
		
		
}
