package Map;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

/**
 * PArameters for MapDetectionsManager which 
 * can be easily serialised and stored in pamsettings
 * 
 * @author Douglas Gillespie
 * @see MapDetectionsManager
 *
 */
public class MapDetectionsParameters implements Serializable, Cloneable, ManagedParameters {
	
	static public final long serialVersionUID = 0;
	
	protected ArrayList<MapDetectionData> mapDetectionDatas = new ArrayList<MapDetectionData>();;

	@Override
	public MapDetectionsParameters clone() {

		try {
			clearDudDetectionData();
			return (MapDetectionsParameters) super.clone();
		}
		catch (CloneNotSupportedException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * Somehow a load of nameless datas (thousands ) have got
	 * into th elist. Remove anything without a valid datName. 
	 */
	private void clearDudDetectionData() {
		if (mapDetectionDatas.size() == 0) return;
		ListIterator<MapDetectionData> it = mapDetectionDatas.listIterator(mapDetectionDatas.size()-1);
		int n = 0;
		while (it.hasPrevious()) {
			MapDetectionData dd = it.previous();
			if (dd.dataName == null) {
				it.remove();
				n++;
			}
		}
		if (n > 0) {
			System.out.printf("%d bad record(s) removed from MapDataParmeters\n", n);
		}
	}

	public ArrayList<MapDetectionData> getMapDetectionDatas() {
		return mapDetectionDatas;
	}

	public void setMapDetectionDatas(ArrayList<MapDetectionData> mapDetectionDatas) {
		this.mapDetectionDatas = mapDetectionDatas;
	}
		
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}

}
