package userDisplayFX;

import java.util.ArrayList;

import javafx.collections.ListChangeListener.Change;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;

public class UserDisplayProcess extends PamProcess {
	
	/**
	 * Holds compatible data units. Note- do not touch this as it changes with PamControlledUnit
	 */
	ArrayList<Class<? extends PamDataUnit>> compatibleDataUnits=new ArrayList<Class<? extends PamDataUnit>>();

	public UserDisplayProcess(UserDisplayControlFX pamControlledUnit,
			PamDataBlock parentDataBlock) {
		super(pamControlledUnit, parentDataBlock);
//		//make sure compatible units array changes as units are removed and added. 
//		pamControlledUnit.getCompatibleDataUnits().addListener( (Change<? extends Class<? extends PamDataUnit>> c)->{
//			compatibleDataUnits=new ArrayList<Class<? extends PamDataUnit>>(compatibleDataUnits);
//		});
		//allow multiplex data blocks.
		this.setMultiplex(true);
	}

	@Override
	public ArrayList getCompatibleDataUnits() {
		return compatibleDataUnits;
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
