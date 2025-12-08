package deepWhistle;

/**
 * Process class for DeepWhistle which inludes the ability to use different types of deep learning based masks
 * 
 * @author Jamie Macaulay
 */
public class DeepWhistleProcess extends MaskedFFTProcess {
	
	
	/**
	 * Reference to the DeepWhistleMask used in this process.
	 */
	private DeepWhistleMask deepWhisltleMask;


	public DeepWhistleProcess(DeepWhistleControl control) {
		super(control);
		this.setMask(deepWhisltleMask = new DeepWhistleMask(this));
	}
	
	
	@Override
	public void prepareProcess() {
		super.prepareProcess();
		// TODO Auto-generated method stub
		//load a deep learning model. 
		
		
	}



	
	

}
