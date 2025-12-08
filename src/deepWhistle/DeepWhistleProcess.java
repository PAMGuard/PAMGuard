package deepWhistle;

public class DeepWhistleProcess extends MaskedFFTProcess {
	
	

	public DeepWhistleProcess(DeepWhistleControl control) {
		super(control);
		// TODO Auto-generated constructor stub
	}
	
	
	@Override
	public void prepareProcess() {
		super.prepareProcess();
		// TODO Auto-generated method stub
		//load a deep learning model. 
		
		
	}
	
	
	@Override
	public double[][] getMask(int n, int m) {
		double[][] mask = new double[m][n];
		for (int i=0; i<m; i++) {
			for (int j=0; j<n; j++) {
				mask[i][j] = 0.1;
			}
		}
		return mask;
	}

}
