/**
 * 
 */
package IshmaelLocator;

/**
 * @author Dave Mellinger
 *
 */
import java.io.Serializable;

public class IshLocParams implements Serializable, Cloneable {
	static public final long serialVersionUID = 0;
	String name = "";				//copied from FFTParams; not sure how used
	public String inputDataSource;
	public int channelList = 1;
	double tBefore = 0.2;
	double tAfter = 0.2;
	boolean useDetector;

	public String getName() { return name; }

	public void setName(String name) { this.name = name; }

	@Override
	protected IshLocParams clone() {
		try {
			return (IshLocParams) super.clone();
		}
		catch (CloneNotSupportedException Ex) {
			return null;
		}
	}
}
