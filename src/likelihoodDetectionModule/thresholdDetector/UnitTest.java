package likelihoodDetectionModule.thresholdDetector;

import likelihoodDetectionModule.SignalBand;
import likelihoodDetectionModule.GuardBand;
import likelihoodDetectionModule.normalizer.NormalizedDataUnit;
import likelihoodDetectionModule.normalizer.NormalizedData;
import likelihoodDetectionModule.AcquisitionSettings;
import likelihoodDetectionModule.LikelihoodFFTParameters;

public class UnitTest {
	
	public static void main( String[] args ) {
		
		boolean enabled = false;
		assert enabled = true;
		if( ! enabled ) {
			System.out.println( "Asserts are disabled, please run this unit test with -ea option" );
			System.out.println( "FAIL" );
			throw new java.lang.RuntimeException();
		}
		
		
		{
		
			System.out.println( "Testing DetectionKey class" );
			final int band_1_index = 0;
			final int band_2_index = 2;
			DetectionKey k1 = new DetectionKey( 0x1, band_1_index );
			DetectionKey k2 = new DetectionKey( 0x2, band_1_index );
			DetectionKey k3 = new DetectionKey( 0x1, band_1_index );
			DetectionKey k4 = new DetectionKey( 0x1, band_2_index );
			
			assert( false == k1.equals( k2 ));
			assert( true == k1.equals( k3 ));
			assert( false == k1.equals( k4 ));
			
			assert( 1 == k1.channelMask() );
			assert( band_1_index == k1.bandIndex() );
		}
		
		{
			System.out.println( "Testing Detection Filter" );
			TestConsumer tc = new TestConsumer();
			DetectionFilter d = new DetectionFilter( tc, 1000 );
			final int channel_one_mask = 0x1;
			final int band_1_index = 1;
			DetectionKey k1 = new DetectionKey( channel_one_mask, band_1_index );
			
			long channel_one_time = 0;
			long channel_one_start_sample = 0;
			final long channel_one_duration = 100;
			final long channel_one_delta_time = 10;
			NormalizedDataUnit ndu1 = new NormalizedDataUnit( channel_one_time,
															 channel_one_mask,
															 channel_one_start_sample,
															 channel_one_duration );
			
			assert( false == d.updateDetection( k1, ndu1 ));
			assert( tc.events().isEmpty() );
			
			d.startDetection( k1, ndu1.getTimeMilliseconds() );
			assert( true == d.updateDetection( k1, ndu1 ) );
			assert( tc.events().size() == 1 );
			assert( d.activeDetections() == 1 );
			assert( d.activeDetections( channel_one_mask ) == 1 );
			assert( d.activeDetections( channel_one_mask + 1 ) == 0 );
			
			final int band_2_index = 3;
			DetectionKey k2 = new DetectionKey( channel_one_mask, band_2_index );
			
			channel_one_time += channel_one_delta_time;
			channel_one_start_sample += channel_one_duration;
			
			NormalizedDataUnit ndu2 = new NormalizedDataUnit( channel_one_time,
															  channel_one_mask,
															  channel_one_start_sample,
															  channel_one_duration );
			d.startDetection( k2, ndu2.getTimeMilliseconds() );
			assert( false == d.updateDetection( k2, ndu2 ));
			assert( tc.events().size() == 1 );
			
			d.endDetection( k1 );
			d.endDetection( k2 );
			
			
			// Now try on two different channels
			d.reset();
			final int channel_two_mask = 0x2;
			DetectionKey k3 = new DetectionKey( channel_two_mask, band_2_index );
			
			final long channel_two_duration = 100;
			long channel_two_time = 10;
			long channel_two_start_sample = 100;
			
			NormalizedDataUnit ndu3 = new NormalizedDataUnit( channel_two_time,
															  channel_two_mask,
															  channel_two_start_sample,
															  channel_two_duration );
			
			d.startDetection( k1, ndu1.getTimeMilliseconds() );
			d.startDetection( k3, ndu2.getTimeMilliseconds() );
			assert( true == d.updateDetection( k1, ndu1 ));
			assert( true == d.updateDetection( k3, ndu3 ));
			assert( d.activeDetections() == 2 );
			assert( d.activeDetections( channel_one_mask ) == 1 );
			assert( d.activeDetections( channel_two_mask ) == 1 );
			
			final int band_3_index = 5;
			final int band_4_index = 10;
			DetectionKey k4 = new DetectionKey( channel_one_mask, band_4_index );
			DetectionKey k5 = new DetectionKey( channel_two_mask, band_3_index );
			
			channel_one_time = 1100;
			channel_one_start_sample = 11000;
			NormalizedDataUnit ndu4 = new NormalizedDataUnit( channel_one_time,
															  channel_one_mask,
															  channel_one_start_sample,
															  channel_one_duration );
			
			channel_two_time = 800;
			channel_two_start_sample = 8000;
			
			NormalizedDataUnit ndu5 = new NormalizedDataUnit( channel_two_time,
															  channel_two_mask,
															  channel_two_start_sample,
															  channel_two_duration );
			
			d.startDetection( k4, ndu4.getTimeMilliseconds() );
			d.startDetection( k5, ndu5.getTimeMilliseconds() );
			assert( true == d.updateDetection( k4, ndu4 ) );
			assert( false == d.updateDetection( k5, ndu5) );
			assert( d.activeDetections() == 3 );
			assert( d.activeDetections( 0x1 ) == 2 );
			assert( d.activeDetections( 0x2 ) == 1 );
			
		}
		
		
		{
			System.out.println( "Testing Threshold Class Test #1" );
			
			// Create a fake AcquisitionSettings and fft_params.  These are typically used to validate
			// the signal/guard bands in the UI, but here we are just hard coding some pertinent values.
			AcquisitionSettings acq = new AcquisitionSettings();
			final double freq_res = 1.d;
			final double time_res = 2.d;
			final int channel_map = 0x1;
			LikelihoodFFTParameters fft_params = new LikelihoodFFTParameters( acq, channel_map, freq_res, time_res );
			
			SignalBand sb1 = new SignalBand( acq, fft_params );
			sb1.identifier = "band1";
			sb1.inBandThresholdDb = 10.;
			sb1.guardBandThresholdDb = 10.;
			
			SignalBand sb2 = new SignalBand( acq, fft_params );
			sb2.identifier = "band2";
			sb2.inBandThresholdDb = 10.;
			sb2.guardBandThresholdDb = 10.;
			
			GuardBand gb1 = new GuardBand( acq, fft_params );
			GuardBand gb2 = new GuardBand( acq, fft_params );
			java.util.HashMap< Integer, GuardBand > guardMap = new java.util.HashMap< Integer, GuardBand >();
			
			guardMap.put( 1, gb1 );
			guardMap.put( 2, gb2 );
			
			

			TestConsumer tc = new TestConsumer();
			DetectionFilter d = new DetectionFilter( tc, 1000 );
			final int sb1_index = 0;
			final int sb2_index = 3;
			Threshold t1 = new Threshold( d, sb1_index, sb1, guardMap );
			Threshold t2 = new Threshold( d, sb2_index, sb2, new java.util.HashMap< Integer, GuardBand >() );
			
			
			// Create two valid detections
			NormalizedDataUnit ndu = new NormalizedDataUnit( 0, 0, 0, 0 );
			NormalizedData[] data = new NormalizedData[ 4 ];
			data[ 0 ] = new NormalizedData( 150, 10 );
			data[ 1 ] = new NormalizedData( 16, 1 );
			data[ 2 ] = new NormalizedData( 10, 1 );
			data[ 3 ] = new NormalizedData( 10, 0.5 );
			ndu.setData(data);
			ndu.setChannelBitmap( 0x1 );
			long timeStamp = 0;
			ndu.setTimeMilliseconds( timeStamp );
			
			t1.process( ndu );
			t2.process( ndu );
			// There should only be one, the second is filtered
			assert( d.activeDetections() == 1 );
			assert( tc.events().peek() != null );
			TestConsumer.EventInfo detEv = tc.events().poll();
			assert( detEv.bandIndex() == sb1_index );
			assert( detEv.timeStamp()  == timeStamp  );
			assert( detEv.channelMask() == 0x1 );
			
			// Set the band2 data to stop detecting (falling edge)
			data[3] = new NormalizedData( 1, 1 );
			ndu.setData( data );
			timeStamp += 550;
			ndu.setTimeMilliseconds( timeStamp );
			
			t1.process(ndu);
			t2.process(ndu);
			// There should still only be one
			assert( d.activeDetections() == 1 );
			assert( tc.events().peek() != null );
			detEv = tc.events().poll();
			assert( detEv.bandIndex() == sb1_index );
			assert( detEv.timeStamp() == timeStamp );
			assert( detEv.channelMask() == 0x1 );
			
			
			// Pass the original data through, 2 seconds later. Now we should get a second detection
			data[ 3 ] = new NormalizedData( 10, 0.5 );
			ndu.setData( data );
			timeStamp += 550;
			ndu.setTimeMilliseconds( timeStamp );
			
			t1.process(ndu);
			t2.process(ndu);
			assert( d.activeDetections() == 2 );
			assert( tc.events().peek() != null );
			detEv = tc.events().poll();
			assert( detEv.bandIndex() == sb1_index );
			assert( detEv.channelMask() == 0x1  );
			assert( detEv.timeStamp() == timeStamp );
			
			detEv = tc.events().poll();
			assert( detEv.bandIndex() == sb2_index );
			assert( detEv.channelMask() == 0x1 );
			assert( detEv.timeStamp() == timeStamp );
		
			// Now try the same data on channelMask 2
			ndu.setChannelBitmap( 0x02 );
			t1.process(ndu);
			t2.process(ndu);
			assert( d.activeDetections() == 3 );
			assert( d.activeDetections( 0x1 ) == 2 );
			assert( d.activeDetections( 0x2 ) == 1 );
			assert( tc.events().size() == 1 );
			assert( tc.events().peek() != null );
			detEv = tc.events().poll();
			assert( detEv.bandIndex() == sb1_index );
			assert( detEv.channelMask() == 0x2 );
			assert( detEv.timeStamp() == timeStamp );
			
			// Now end the detections on all bands/channels 200 ms later
			data[ 0 ] = new NormalizedData( 1, 1 );
			data[ 1 ] = new NormalizedData( 1, 1 );
			data[ 2 ] = new NormalizedData( 1, 1 );
			data[ 3 ] = new NormalizedData( 1, 1 );
			ndu.setData(data);
			timeStamp += 200;
			ndu.setTimeMilliseconds(timeStamp);
			ndu.setChannelBitmap( 0x1 );
			
			t1.process(ndu);
			t2.process(ndu);
			
			ndu.setChannelBitmap( 0x2 );
			t1.process(ndu);
			t2.process(ndu);
			
			assert( d.activeDetections() == 0 );
			assert( tc.events().isEmpty() );
			
			// Check that new detections 200ms later are filtered on all channels/bands
			data[ 0 ] = new NormalizedData( 150, 10 );
			data[ 1 ] = new NormalizedData( 16, 1 );
			data[ 2 ] = new NormalizedData( 10, 1 );
			data[ 3 ] = new NormalizedData( 10, 0.5 );
			timeStamp += 200;
			ndu.setData(data);
			ndu.setTimeMilliseconds(timeStamp);
			ndu.setChannelBitmap( 0x1 );
			
			t1.process(ndu);
			t2.process(ndu);
			
			ndu.setChannelBitmap( 0x2 );
			t1.process(ndu);
			t2.process(ndu);
			
			assert( d.activeDetections() == 0 );
			assert( tc.events().isEmpty() );
			
		
		}
		
		{
			System.out.println( "Testing Threshold Class Test #2" );
			// Test a Threshold object with null guardBands
			
			// Create a fake AcquisitionSettings and fft_params.  These are typically used to validate
			// the signal/guard bands in the UI, but here we are just hard coding some pertinent values.
			AcquisitionSettings acq = new AcquisitionSettings();
			final double freq_res = 1.d;
			final double time_res = 2.d;
			final int channel_map = 0x1;
			LikelihoodFFTParameters fft_params = new LikelihoodFFTParameters( acq, channel_map, freq_res, time_res );
			
			SignalBand sb1 = new SignalBand( acq, fft_params );
			sb1.identifier = "band1";
			sb1.inBandThresholdDb = 10.;
			
			NormalizedDataUnit ndu = new NormalizedDataUnit( 0, 0, 0, 0 );
			NormalizedData[] data = new NormalizedData[ 1 ];
			data[ 0 ] = new NormalizedData( 150, 10 );
			
			ndu.setData(data);
			ndu.setChannelBitmap( 0x2 );
			long timeStamp = 0;
			ndu.setTimeMilliseconds( timeStamp );
			
			TestConsumer tc = new TestConsumer();
			DetectionFilter d = new DetectionFilter( tc, 1000 );
			final int sb1_index = 0;
			Threshold t = new Threshold( d, sb1_index, sb1, null );
			t.process(ndu);
			assert( d.activeDetections() == 1 );
			assert( tc.events().size() == 1 );
			
			TestConsumer.EventInfo ev = tc.events().poll();
			assert( ev != null );
			assert( ev.timeStamp() == 0 );
			assert( ev.bandIndex() == sb1_index );
			assert( ev.channelMask() == 0x2 );
			
		}
		
		
		{
			System.out.println( "Testing the CurvePeakTracker" );
			long timeMs = 0;
			long sampleNo = 0;
			double value = 10.;
			// Construct
			CurvePeakTracker cpt = new CurvePeakTracker( timeMs, sampleNo, value );
			assert( cpt.peakSample() == 0 );
			assert( cpt.peakTime() == 0 );
			assert( cpt.peakValue() == 10. );
			
			timeMs += 10;
			sampleNo += 100;
			value = 9;
			// Peak should not update
			cpt.update( timeMs, sampleNo, value );
			assert( cpt.peakSample() == 0 );
			assert( cpt.peakTime() == 0 );
			assert( cpt.peakValue() == 10. );
			
			timeMs += 10;
			sampleNo += 100;
			value = 12.;
			// New peak
			cpt.update( timeMs, sampleNo, value );
			assert( cpt.peakSample() == 200 );
			assert( cpt.peakTime() == 20 );
			assert( cpt.peakValue() == 12. );
			

			timeMs += 10;
			sampleNo += 100;
			value = 10.;
			// Peak should not update
			cpt.update( timeMs, sampleNo, value );
			assert( cpt.peakSample() == 200 );
			assert( cpt.peakTime() == 20 );
			assert( cpt.peakValue() == 12. );
			
		}
		
		{
			System.out.println( "Testing DetectionKey.hashCode() functionality" );

			DetectionKey k1 = new DetectionKey( 1, 2 );
			DetectionKey k2 = new DetectionKey( 2, 1 );
			DetectionKey k3 = new DetectionKey( 1, 2 );
			
			assert( k1.hashCode() == k3.hashCode() );
			assert( k1.hashCode() != k2.hashCode() );
			
			
		}
		
		{
			System.out.println( "Testing DetectionTracker functionality" );
			
			
			// Start with some data.  It doesn't matter, the peak is always seeded with the first data point
			final long start_time = 100000;
			long time = start_time; 
			final long start_sample = 0;
			long sample = start_sample;
			final long duration = 100;
			final long delta_time = 10;
			final int channel_one_mask = 0x1;
			
			NormalizedDataUnit ndu = new NormalizedDataUnit( time,
															 channel_one_mask,
															 sample,
															 duration );
			
			NormalizedData[] data = new NormalizedData[ 1 ];
			double signal = 10.;
			double noise = 10.;
			data[0] = new NormalizedData( signal, noise ); 
			ndu.setData( data );
			
			final int index = 0;
			DetectionTracker tracker = new DetectionTracker( index, ndu );
			assert( tracker.duration() == duration );
			assert( tracker.startSample() == start_sample );
			assert( tracker.startTime() == start_time );
			
			assert( tracker.ratioPeakTracker().peakSample() == sample );
			assert( tracker.ratioPeakTracker().peakTime() == time );
			assert( tracker.ratioPeakTracker().peakValue() == 1.0 );
			assert( tracker.rawEnergyPeakTracker().peakSample() == sample );
			assert( tracker.rawEnergyPeakTracker().peakTime() == time );
			assert( tracker.rawEnergyPeakTracker().peakValue() == 10. );
			
			
			
			// Now halve the signal.  This should not update the peak estimates.  But the duration should increase
			signal = 5.;
			time += delta_time;
			sample += duration;
			
			data[0] = new NormalizedData( signal, noise );
			ndu.setData( data );
			ndu.setTimeMilliseconds( time );
			ndu.setStartSample( sample );
			
			
			tracker.update( ndu );
			assert( tracker.duration() == duration * 2 );
			assert( tracker.startSample() == start_sample );
			assert( tracker.startTime() == start_time );
			
			assert( tracker.ratioPeakTracker().peakSample() == 0 );
			assert( tracker.ratioPeakTracker().peakTime() == start_time );
			assert( tracker.ratioPeakTracker().peakValue() == 1.0 );
			assert( tracker.rawEnergyPeakTracker().peakSample() == 0 );
			assert( tracker.rawEnergyPeakTracker().peakTime() == start_time );
			assert( tracker.rawEnergyPeakTracker().peakValue() == 10. );
			 
			
			// Now double the original signal.  This should cause an update in the peak estimates.
			signal = 20.;
			time += delta_time;
			sample += duration;
			
			data[0] = new NormalizedData( signal, noise );
			ndu.setData( data );
			ndu.setTimeMilliseconds( time );
			ndu.setStartSample( sample );
			tracker.update( ndu );
			
			
			assert( tracker.duration() == duration * 3 );
			assert( tracker.startSample() == start_sample );
			assert( tracker.startTime() == start_time );
			
			assert( tracker.ratioPeakTracker().peakSample() == sample );
			assert( tracker.ratioPeakTracker().peakTime() == time );
			assert( tracker.ratioPeakTracker().peakValue() == 2.0 );
			assert( tracker.rawEnergyPeakTracker().peakSample() == sample );
			assert( tracker.rawEnergyPeakTracker().peakTime() == time );
			assert( tracker.rawEnergyPeakTracker().peakValue() == 20. );

			
			// Set the signal back to a low value, and test a couple tries at this.  It should not update
			// the peak estimates
			signal = 1.;
			time += delta_time;
			sample += duration;
			
			data[0] = new NormalizedData( signal, noise );
			ndu.setData( data );
			ndu.setTimeMilliseconds( time );
			ndu.setStartSample( sample );
			tracker.update( ndu );
			
			
			assert( tracker.duration() == duration * 4 );
			assert( tracker.startSample() == start_sample );
			assert( tracker.startTime() == start_time );
			
			assert( tracker.ratioPeakTracker().peakSample() == sample - duration );
			assert( tracker.ratioPeakTracker().peakTime() == time - delta_time );
			assert( tracker.ratioPeakTracker().peakValue() == 2.0 );
			assert( tracker.rawEnergyPeakTracker().peakSample() == sample - duration );
			assert( tracker.rawEnergyPeakTracker().peakTime() == time - delta_time );
			assert( tracker.rawEnergyPeakTracker().peakValue() == 20. );			
			
			
			signal = 1.;
			time += delta_time;
			sample += duration;
			
			data[0] = new NormalizedData( signal, noise );
			ndu.setData( data );
			ndu.setTimeMilliseconds( time );
			ndu.setStartSample( sample );
			tracker.update( ndu );
			
			
			assert( tracker.duration() == duration * 5 );
			assert( tracker.startSample() == start_sample );
			assert( tracker.startTime() == start_time );
			
			assert( tracker.ratioPeakTracker().peakSample() == sample - 2*duration );
			assert( tracker.ratioPeakTracker().peakTime() == time - 2*delta_time );
			assert( tracker.ratioPeakTracker().peakValue() == 2.0 );
			assert( tracker.rawEnergyPeakTracker().peakSample() == sample - 2*duration );
			assert( tracker.rawEnergyPeakTracker().peakTime() == time - 2*delta_time );
			assert( tracker.rawEnergyPeakTracker().peakValue() == 20. );			
		}
		
		System.out.println( "PASSED" );
		
	}
	
}
