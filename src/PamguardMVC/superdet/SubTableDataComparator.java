package PamguardMVC.superdet;

import java.util.Comparator;

import generalDatabase.PamSubtableData;

public class SubTableDataComparator implements Comparator<PamSubtableData> {

	@Override
	public int compare(PamSubtableData o1, PamSubtableData o2) {
		/*
		 * First compare their parent data unit so that all data from the same unit are together, 
		 * then compare based on their data name, then on time. This is not quite the same as the 
		 * comparison in PamDataunit since that also uses sample number, so there is a very small 
		 * chance that if two units have the same ms time, they will be in a different order to 
		 * those of the datablocks. 
		 */
		// try the parent detection UID
		long comp = o1.getParentUID() - o2.getParentUID();
		if (comp != 0) {
			return Long.signum(comp);
		}
		// then the data name of the sub detection
		if (o1.getLongName() != null && o2.getLongName() != null) {
			comp = o1.getLongName().compareTo(o2.getLongName());
			if (comp != 0) {
				return Long.signum(comp);
			}
		}
		// next try the child UTC
		comp = o1.getChildUTC() - o2.getChildUTC();
		if (comp != 0) {
			return Long.signum(comp);
		}
		// then the child UID
		comp = o1.getChildUID()-o2.getChildUID();
		return Long.signum(comp);
	}
	

}
