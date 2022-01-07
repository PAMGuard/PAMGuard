package IMU;

import pamScrollSystem.AbstractScrollManager;
import PamguardMVC.PamProcess;

public class IMUProcess extends PamProcess{

	private IMUControl imuControl;
	
	private IMUDataBlock angleDataBlock;

	public IMUProcess(IMUControl imuControl) {
		super(imuControl, null);
		this.imuControl=imuControl;
		angleDataBlock = new IMUDataBlock(imuControl, this.getProcessName(), this);
		//because there a re no displays with scrollers observing the angle data block we need to add it to a special list. 
		AbstractScrollManager.getScrollManager().addToSpecialDatablock(angleDataBlock);
		addOutputDataBlock(angleDataBlock);
	}
	
	/**
	 * Return the IMU datablock. 
	 * @return the IMU datablock;
	 */
	public IMUDataBlock getIMUDataBlock(){
		return angleDataBlock; 
	}
	
	
	@Override
	public void destroyProcess(){
		for (int i=0; i<super.outputDataBlocks.size(); i++){
			AbstractScrollManager.getScrollManager().removeFromSpecialDatablock(outputDataBlocks.get(i));
		}
		super.destroyProcess();
	}
	
	/**
	 * 
	 * @return Name of the PamProcess
	 */
	@Override
	public String getProcessName() {
		return "IMU Measurment";
	}

	@Override
	public void pamStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pamStop() {
		// TODO Auto-generated method stub
		
	}

}
