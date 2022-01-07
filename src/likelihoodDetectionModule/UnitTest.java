package likelihoodDetectionModule;

/**
 * The unit test for Signal and Guard Bands.
 */
public class UnitTest {

	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 */
	static public void main( String[] args ) {
		boolean enabled = false;
		assert enabled = true;
		if( ! enabled ) {
			System.out.println( "Asserts are disabled, please run this unit test with -ea option" );
			System.out.println( "FAIL" );
			throw new java.lang.RuntimeException();
		}
		
		{
			System.out.println( "Testing Signal Band" );
			
			// Create a fake AcquisitionSettings and fft_params.  These are typically used to validate
			// the signal/guard bands in the UI, but here we just need them to construct.  We will set the
			// values we wish to test by hand.
			AcquisitionSettings acq = new AcquisitionSettings();
			final double freq_res = 1.d;
			final double time_res = 2.d;
			final int channel_map = 0x1;
			LikelihoodFFTParameters fft_params = new LikelihoodFFTParameters( acq, channel_map, freq_res, time_res );
			
			SignalBand sb = new SignalBand( acq, fft_params );
			sb.inBandThresholdDb = 10.;
			sb.guardBandThresholdDb = 20.;
			assert( sb.GuardBandAsRatio() == 100. );
			assert( sb.InBandAsRatio() == 10 );
		}
		
		{
			System.out.println( "Testing LikelihoodFFTParameters #1" );
			AcquisitionSettings acq = new AcquisitionSettings();
			acq.samplingRateHz = 48000.0f;
			acq.sourceNumber = 1;
				
			final double freqRes = 100;
			final double timeRes = 0.01;
			final int channelMap = 0x3;
			LikelihoodFFTParameters params = new LikelihoodFFTParameters( acq, channelMap, freqRes, timeRes );
			
			assert( params.getActualFrequencyResolution() == 93.75 );
			// No fuzzy compare, this will do
			assert( params.getActualTimeResolution() > 0.01 );
			assert( params.getActualTimeResolution() < 0.0107 );
			
			assert( acq.sourceNumber == params.getdataSourceNumber() );
			assert( channelMap == params.getChannelMap() );
			assert( 256 == params.getFFTHop() );
			assert( 512 == params.getFFTSize() );
			assert( 0.5 == params.getOverlap() );
			assert( 2 == params.getNumberAverages() );

		}
		
		{
			System.out.println( "Testing LikelihoodFFTParameters #2" );
			AcquisitionSettings acq = new AcquisitionSettings();
			acq.samplingRateHz = 48000.0f;
			acq.sourceNumber = 1;
			
			final double freqRes = 788;
			final double timeRes = 1;
			final int channelMap = 0x5;
			LikelihoodFFTParameters params = new LikelihoodFFTParameters( acq, channelMap, freqRes, timeRes );
			
			
			assert( 750.0 == params.getActualFrequencyResolution() );
			assert( 1.0 == params.getActualTimeResolution() );
			assert( acq.sourceNumber == params.getdataSourceNumber() );
			assert( channelMap == params.getChannelMap() );
			assert( 32 == params.getFFTHop() );
			assert( 64 == params.getFFTSize() );
			assert( 0.5 == params.getOverlap() );
			assert( 1500 == params.getNumberAverages() );			
			
			
		}

		{
			System.out.println( "Testing LikelihoodFFTParameters #3" );
			AcquisitionSettings acq = new AcquisitionSettings();
			acq.samplingRateHz = 96000.0f;
			acq.sourceNumber = 1;
			
			final double freqRes = 10;
			final double timeRes = 0.01;
			final int channelMap = 0x5;
			LikelihoodFFTParameters params = new LikelihoodFFTParameters( acq, channelMap, freqRes, timeRes );
			
			assert( 11.71875 ==  params.getActualFrequencyResolution() );
			assert( 0.01 == params.getActualTimeResolution() );
			assert( acq.sourceNumber == params.getdataSourceNumber() );
			assert( channelMap == params.getChannelMap() );
			assert( 960 == params.getFFTHop() );
			assert( 8192 == params.getFFTSize() );
			assert( 0.8828125 == params.getOverlap() );
			assert( 1 == params.getNumberAverages() );			
			
		}
		
		{
			System.out.println( "Testing LikelihoodFFTParameters #4" );
			AcquisitionSettings acq = new AcquisitionSettings();
			acq.samplingRateHz = 4096;
			acq.sourceNumber = 1;
			
			final double freqRes = 32;
			final double timeRes = 0.00390625;
			final int channelMap = 0x5;
			LikelihoodFFTParameters params = new LikelihoodFFTParameters( acq, channelMap, freqRes, timeRes );
			assert( 128 == params.getFFTSize() );
			assert( 16 == params.getFFTHop() );
			assert( 1 == params.getNumberAverages() );

		}
		
		{
			System.out.println( "Testing LikelihoodFFTParameters #5" );
			AcquisitionSettings acq = new AcquisitionSettings();
			acq.samplingRateHz = 4096;
			acq.sourceNumber = 1;
			
			final double freqRes = 32;
			final double timeRes = 0.0625;
			final int channelMap = 0x5;
			LikelihoodFFTParameters params = new LikelihoodFFTParameters( acq, channelMap, freqRes, timeRes );
			assert( 128 == params.getFFTSize() );
			assert( 64 == params.getFFTHop() );
			assert( 4 == params.getNumberAverages() );
		}
		
		{
			System.out.println( "Testing LikelihoodFFTParameters #6" );
			AcquisitionSettings acq = new AcquisitionSettings();
			acq.samplingRateHz = 4096;
			acq.sourceNumber = 1;
			
			// Ask for a time resolution that would require > 90% overlap, and ensure it
			// clamps at 90
			final double freqRes = 1;
			final double timeRes = 0.085;
			final int channelMap = 0x5;
			LikelihoodFFTParameters params = new LikelihoodFFTParameters( acq, channelMap, freqRes, timeRes );
			assert( 410 == params.getFFTHop() );
			assert( 4096 == params.getFFTSize() );
			assert( 1 == params.getNumberAverages() );
			assert( 0.9 == params.getOverlap() );
			
		}
		
		System.out.println( "PASSED" );
		
	}
	
}
