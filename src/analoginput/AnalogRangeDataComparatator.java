package analoginput;

import java.util.Comparator;

public class AnalogRangeDataComparatator implements Comparator<AnalogRangeData>{

	@Override
	public int compare(AnalogRangeData o1, AnalogRangeData o2) {
		return o1.compareTo(o2);
	}

}
