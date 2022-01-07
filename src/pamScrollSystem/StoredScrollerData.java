package pamScrollSystem;

import java.io.Serializable;
import java.util.Vector;

/**
 * Class to hold a whole load of PamScrollerData objects between runs. 
 * @author Doug
 *
 */
public class StoredScrollerData implements Serializable, Cloneable {

	public static final long serialVersionUID = 1L;
	
	boolean coupleAllScrollers = false;
	
	private Vector<PamScrollerData> scrollerData = new Vector<PamScrollerData>();
	
	protected void addScrollerData(AbstractPamScroller scroller) {
		scrollerData.add(scroller.scrollerData);
	}
	
	protected PamScrollerData findScrollerData(AbstractPamScroller scroller) {
		if (scrollerData == null) {
			return null;
		}
		for (int i = 0; i < scrollerData.size(); i++) {
			if (scroller.scrollerData.name.equals(scrollerData.get(i).name)) {
				return scrollerData.get(i);
			}
 		}
		return null;
	}

}
