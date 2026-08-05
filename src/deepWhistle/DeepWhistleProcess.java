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
	
	private DeepWhistleControl deepWhistleControl;

	public DeepWhistleProcess(DeepWhistleControl control) {
		super(control);
		this.deepWhistleControl = control;
		this.setMask(deepWhisltleMask = new DeepWhistleMask(this));
	}
	
	
	@Override
	public void prepareProcess() {
		super.prepareProcess();
		// TODO Auto-generated method stub
		//load a deep learning model. 
	}


	public DeepWhistleParamters getDeepWhistleParameters() {
		return (DeepWhistleParamters) deepWhistleControl.getParameters();
	}
	
	

}
