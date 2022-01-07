package userDisplay;


/**
 * Adapter class for UserDisplayComponent which manages
 * some of the functions which may not be needed to all 
 * display components. 
 * @author dg50
 *
 */
public abstract class UserDisplayComponentAdapter implements UserDisplayComponent {

	private String uniqueName;
	
	@Override
	public void openComponent() {
	}

	@Override
	public void closeComponent() {

	}

	@Override
	public void notifyModelChanged(int changeType) {

	}

	@Override
	public String getUniqueName() {
		return uniqueName;
	}

	@Override
	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}

	@Override
	public String getFrameTitle() {
		return getUniqueName();
	}

}
