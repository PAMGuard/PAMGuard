package clickDetector.echoDetection;

import clickDetector.ClickControl;
import clickDetector.ClickDetection;
import clickDetector.ClickDetector.ChannelGroupDetector;

public class JamieEchoDetector implements EchoDetector {


	private JamieEchoDetectionSystem jamieEchoDetectionSystem;

	private int channelBitmap;

	private ClickControl clickControl;

	private ChannelGroupDetector channelGroupDetector;

	// a click detection is a click object, containing click information
	//need to use three preceding clicks for this echo detection system 

	private ClickDetection prevDetection1;

	private ClickDetection prevDetection2;

	private ClickDetection prevDetection3;

	private double meanAmp;

	private double meanICI;

	private double currentICI;

	private double currentAmp;

	private double sampleRate;

	private double N=0;

	/*Echo Detection params*/
	private double maxIntervalSeconds;

	public double maxAmpDifference;

	public double maxICIDifference;




	/**
	 * Construct an Echo detector
	 * @param simpleEchoDetectionSystem 
	 * @param clickControl
	 * @param channelGroupDetector
	 * @param channelBitmap
	 */
	public JamieEchoDetector(JamieEchoDetectionSystem jamieEchoDetectionSystem, ClickControl clickControl,
			ChannelGroupDetector channelGroupDetector, int channelBitmap) {
		super();
		this.jamieEchoDetectionSystem = jamieEchoDetectionSystem;
		this.clickControl = clickControl;
		this.channelGroupDetector = channelGroupDetector;
		this.channelBitmap = channelBitmap;
	}





	@Override
	public void initialise() {
		sampleRate = clickControl.getClickDetector().getSampleRate();
	}



	@Override
	/**
	 * Determine whether a click is an echo or not based on the mean ICI and Amplitude of the three preceding non echo clicks. Credit for this type of Echo detector goes to Dr Stacey Deruiter. 
	 */
	public boolean isEcho(ClickDetection clickDetection) {

		maxIntervalSeconds=jamieEchoDetectionSystem.jamieEchoParams.maxIntervalSeconds;
		maxAmpDifference=jamieEchoDetectionSystem.jamieEchoParams.maxAmpDifference;
		maxICIDifference=jamieEchoDetectionSystem.jamieEchoParams.maxICIDifference;

//		System.out.println("Begin Echo Detect");

		// the first three clicks are ignored and added to our list but only if they are seperated by at least the time interval 
		//for a porpoise. 

		if (sampleRate == 0) {
			initialise();
		}


		if (prevDetection1 == null || prevDetection2 == null || prevDetection3== null || prevDetection3.getStartSample()>clickDetection.getStartSample()) {

			//System.out.println("/////////////////Creating first three non echo clicks/////////////");

			if (prevDetection1== null){
				prevDetection1=clickDetection;
				return false;
			}

			else if (prevDetection2== null) {
				if ((clickDetection.getStartSample()-prevDetection1.getStartSample())>(maxIntervalSeconds*sampleRate)){
					prevDetection2=clickDetection;
					return false;
				}
				return true;
			}

			else if (prevDetection3== null){
				if ((clickDetection.getStartSample()-prevDetection2.getStartSample())>maxIntervalSeconds*sampleRate){
					prevDetection3=clickDetection;
					return false;}
				return true;	
			}

			// begins again when a new file starts
			else if (prevDetection3.getStartSample()>clickDetection.getStartSample()){
				prevDetection1=null;
				prevDetection2=null;
				prevDetection3=null;
				return false;
			}			
			return false;
		}

		N=N+1;


		if (prevDetection3==null){
			return false;
		}	
		/*Make sure the click separation is at least the time interval before regarding as a candidate click or not*/
		if (clickDetection.getStartSample()-prevDetection3.getStartSample()>maxIntervalSeconds*sampleRate){

//			System.out.println("N: "+N);

			//System.out.println("ClickStartSample: "+clickDetection.getStartSample());
			//System.out.println("PreviousDetection1: "+ prevDetection1.getStartSample());
			//System.out.println("PreviousDetection2: "+ prevDetection2.getStartSample());
			//System.out.println("PreviousDetection3: "+ prevDetection3.getStartSample());**/

			/*Work out mean amplitude and mean ICI of three preceding clicks.*/
			meanAmp=(prevDetection1.getWaveAmplitude()+prevDetection2.getWaveAmplitude()+prevDetection3.getWaveAmplitude())/3;
			meanICI=((prevDetection2.getStartSample()-prevDetection1.getStartSample()) +(prevDetection3.getStartSample()-prevDetection2.getStartSample()))/3;

			currentICI=clickDetection.getStartSample()-prevDetection3.getStartSample();
			currentAmp=clickDetection.getWaveAmplitude();

			/*Accept the click if the amplitude is greater than 0.5*mean amplitude and the ICI is greater than 0.2*meanICI*/ 
			if (currentAmp>=(maxAmpDifference)*meanAmp &&  currentICI>=(maxICIDifference)*meanICI){
				prevDetection1=prevDetection2;
				prevDetection2=prevDetection3;
				prevDetection3=clickDetection;

				//System.out.println("1ClickStartSample: "+clickDetection.getStartSample());
				//System.out.println("1PreviousDetection1: "+ prevDetection1.getStartSample());
				//System.out.println("1PreviousDetection2: "+ prevDetection2.getStartSample());
				//System.out.println("1PreviousDetection3: "+ prevDetection3.getStartSample());**/

				return false;

			}
			/*Also accept, even the ICI criterea is false if amplitude is greater than 3*average amplitude*/
			else if(currentAmp>=3*meanAmp){
				prevDetection1=prevDetection2;
				prevDetection2=prevDetection3;
				prevDetection3=clickDetection;

				//System.out.println("1ClickStartSample: "+clickDetection.getStartSample());
				//System.out.println("1PreviousDetection1: "+ prevDetection1.getStartSample());
				//System.out.println("1PreviousDetection2: "+ prevDetection2.getStartSample());
				//System.out.println("1PreviousDetection3: "+ prevDetection3.getStartSample());

				return false;

			}
			/*Also accept, if the current ICI is 3*the pervious mean ICI. Essentially a reset...allows for quieter click trains to be considered */
			else if(currentICI>=3*meanICI ){
				this.prevDetection1=clickDetection;
				this.prevDetection2=null;
				this.prevDetection3=null;

				return false;

			}

			else{
				//System.out.println("Echo!");
				return true;
			}

		}

		else {
			//System.out.println("Echo!");
			return true;
		}
	}








}
