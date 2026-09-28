package soundtrapSensor;

import PamguardMVC.PamProcess;
import pamScrollSystem.AbstractScrollManager;

/**
 * The PAMGuard process for the SudSensor module. Creates and owns the
 * {@link SudSensorDataBlock} and drives the sensor processing.
 *
 * @author Jamie Macaulay
 */
public class SudSensorProcess extends PamProcess {

    private final SudSensorControl sensorControl;
    private final SudSensorDataBlock sensorDataBlock;

    public SudSensorProcess(SudSensorControl sensorControl) {
        super(sensorControl, null);
        this.sensorControl = sensorControl;

        sensorDataBlock = new SudSensorDataBlock(this);
        sensorDataBlock.setDatagramProvider(new SudSensorDatagramProvider(sensorControl));
        //AbstractScrollManager.getScrollManager().addToSpecialDatablock(sensorDataBlock);
        addOutputDataBlock(sensorDataBlock);
    }

    public SudSensorDataBlock getSensorDataBlock() {
        return sensorDataBlock;
    }
    
    @Override
	public void prepareProcess() {
        sensorControl.getSWVHandler().prepare();
    }

    @Override
    public String getProcessName() {
        return "SUD Sensor Process";
    }

    @Override
    public void pamStart() {
        sensorControl.getSWVHandler().pamStart();
    }



    @Override
    public void destroyProcess() {
        //AbstractScrollManager.getScrollManager().removeFromSpecialDatablock(sensorDataBlock);
        super.destroyProcess();
    }

	@Override
	public void pamStop() {
		// TODO Auto-generated method stub
		
	}
}
