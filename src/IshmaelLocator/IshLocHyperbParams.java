/**
 * 
 */
package IshmaelLocator;

/**
 * @author Dave Mellinger
 */

import java.io.Serializable;

public class IshLocHyperbParams extends IshLocParams implements Serializable, Cloneable {
	static public final long serialVersionUID = 0;
	int nDimensions = 2;			//2 or 3

	@Override
	protected IshLocHyperbParams clone() {
		return (IshLocHyperbParams)super.clone();
	}
}
