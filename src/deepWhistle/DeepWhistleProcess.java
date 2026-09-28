package deepWhistle;

/**
 * Process class for DeepWhistle which inludes the ability to use different types of deep learning based masks
 * 
 * @author Jamie Macaulay
 */
public class DeepWhistleProcess extends MaskedFFTProcess {
	
	
	private DeepWhistleControl deepWhistleControl;

	public DeepWhistleProcess(DeepWhistleControl control) {
		super(control);
		this.deepWhistleControl = control;
		// the mask is created in prepareProcess() based on the selected mask type.
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
