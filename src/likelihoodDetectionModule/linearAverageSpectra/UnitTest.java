package likelihoodDetectionModule.linearAverageSpectra;

import PamUtils.complex.ComplexArray;

public class UnitTest {

	static public void main( String[] args ) {

        boolean enabled = false;
        assert enabled = true;
        if( ! enabled ) {
        	System.out.println( "Asserts are disabled, please run this unit test with -ea option" );
        	System.out.println( "FAIL" );
        	throw new java.lang.RuntimeException();
        }
             
//		fftManager.Complex[] d = new fftManager.Complex[ 2 ];
//		
//		d[0] = new fftManager.Complex();
//		d[0].real = 10;
//		d[0].imag = 15;
//		
//		d[1] = new fftManager.Complex();
//		d[1].real = 23;
//		d[1].imag = 45;
        double[] testData = {10, 15, 23, 45};
        ComplexArray d = new ComplexArray(testData);
		
		long timestamp = 0;
		long startSample = 100;

		final int recordsToAverage = 5;
		System.out.println( "Test 1 with constant data" );
		RealBlockAverage rba = new RealBlockAverage( recordsToAverage );
		System.out.println( "Created the RealBlockAverage object with nAvg == " + recordsToAverage );
		assert( rba.NAvg() == recordsToAverage );
		
		System.out.println( "Checking no output is ready yet" );
		assert( rba.averageReady() == false );
		
		rba.processData( timestamp++, startSample, d);
		startSample += 100;
		rba.processData( timestamp++, startSample, d);
		startSample += 100;
		
		System.out.println( "Checking that nothing is ready after 2 data points" );
		assert( rba.averageReady() == false );
		
		
		rba.processData( timestamp++, startSample, d);
		startSample += 100;
		rba.processData( timestamp++, startSample, d);
		startSample += 100;
		
		System.out.println( "Checking that nothing is ready after 4 data points" );
		assert( rba.averageReady() == false );
		
		
		// Do the 5th input, this should produce an average
		rba.processData( timestamp++, startSample, d);
		startSample += 100;
		
		System.out.println( "Checking after 5 data points, should have an average" );
		assert( rba.averageReady() == true );
		
		double[] result = rba.average();
		System.out.println( "Making sure the output record is the same width as the input records" );
		assert( result.length == 2 );
		
		System.out.println( "Checking timestamp of average" );
		assert( 0 == rba.averageTimestamp() );
		assert( 100 == rba.averageStartSample() );
		
		System.out.println( "Checking output" );
		for( double x : result ) {
			System.out.println( x );
		}
		assert( result[ 0 ] == 325. );
		assert( result[ 1 ] == 2554. );
		

		System.out.println( "Making sure another average is not ready" );
		assert( rba.averageReady() == false);
		

		System.out.println();
		System.out.println( "Test 2 with changing data" );
		testData[0] = 2;
		testData[1] = 3;
		testData[2] = 5;
		testData[3] = 6;
		for( int index = 0; index < rba.NAvg(); ++index ) {
			rba.processData( timestamp++, startSample, d );
			startSample += 100;
			testData[0] += testData[0];
			testData[1] += testData[1];
			testData[2] += testData[2];
			testData[3] += testData[3];
//			d[0].imag += d[0].imag;
//			d[1].real += d[1].real;
//			d[1].imag += d[1].imag;
		}
		System.out.println( "Checking after 5 data points, should have an average" );
		assert( rba.averageReady() == true );

		
		result = rba.average();
		System.out.println( "Making sure the output record is the same width as the input records" );
		assert( result.length == 2 );
		
		System.out.println( "Checking timestamp of average" );
		assert( 5 == rba.averageTimestamp() );
		assert( 600 == rba.averageStartSample() );
		
		System.out.println( "Checking output" );
		for( double x : result ) {
			System.out.println( x );
		}
		assert( result[ 0 ] == 886.6 );
		assert( result[ 1 ] == 4160.2 );
		

		System.out.println( "Making sure another average is not ready" );
		assert( rba.averageReady() == false );

		
		System.out.println();
		System.out.println( "PASSED" );
	}
	
}
