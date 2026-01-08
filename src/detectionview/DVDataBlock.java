package detectionview;

import java.util.ListIterator;

import PamguardMVC.PamDataUnit;
import clipgenerator.ClipDisplayDataBlock;

/**
 * Datablock to hold generated clips to be displayed. 
 * This does not actually load anything at all but will be filled
 * when the main datablock for the dvdisplay has loaded it's data. 
 * This means that it's quite hard for the detection datablock to know how 
 * long to hold data for, unless we manage to frig the scroller in the 
 * clip display control panel!
 * @author dg50
 *
 */
public class DVDataBlock extends ClipDisplayDataBlock<DVDataUnit> {

	public DVDataBlock(DVControl dvControl, DVProcess dvProcess, int channelMap) {
		super(DVDataUnit.class, dvControl.getUnitName() + " clips", dvProcess, channelMap);
	}

	
}
