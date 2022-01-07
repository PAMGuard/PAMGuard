package likelihoodDetectionModule.spectralEti;

public class UnitTest {
	
	public static void main( String[] args ) {
		boolean enabled = false;
		assert enabled = true;
		if( ! enabled ) {
			System.out.println( "Asserts are disabled, please run this unit test with -ea option" );
			System.out.println( "FAIL" );
			throw new java.lang.RuntimeException();
		}
		
		System.out.println( "Beginning SpectralBand Unit test" );
		
		
		System.out.println( "Instantiated SpectralBand object" );
		
		double[] data = new double[ 10 ];
		for( int i = 0 ; i < 10; ++i ) { 
			data[i] = i+1;
		}
		
		System.out.println( "Input spectra block" );
		for( double d : data ) {
			System.out.println( d );
		}
		
		{
			System.out.println();
			System.out.println( "Test #1 with bands inside of the spectral range" );
			SpectralBand band = new SpectralBand( 1., 2., 10. );
			double result = band.calculate( data );
			assert( result == 4. );
		
			System.out.println( result );
		}
		
		{
			System.out.println();
			System.out.println( "Test #2 with bands outside of the spectral range to test clipping" );
			SpectralBand band = new SpectralBand( -1, 6, 10. );
			double result = band.calculate( data );
			assert( result == 5.5 );
			System.out.println( result );
		
			System.out.println();
			System.out.println( "PASSED" );
		}
	}
}
