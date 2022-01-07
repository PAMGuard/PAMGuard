package PamguardMVC;

import java.util.Comparator;

/**
 * Compare two datablocks by name so that they can be easily sorted. 
 * @author dg50
 *
 */
public class DataBlockNameComparator implements Comparator<PamDataBlock> {

	@Override
	public int compare(PamDataBlock block0, PamDataBlock block1) {
		return block0.getDataName().compareTo(block1.getDataName());
	}

}
