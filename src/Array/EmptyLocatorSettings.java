package Array;

public class EmptyLocatorSettings extends LocatorSettings {

	public EmptyLocatorSettings(Class locatorClass) {
		super(locatorClass);
	}

	private static final long serialVersionUID = 1L;
	
	@Override
	public EmptyLocatorSettings clone() {
		return (EmptyLocatorSettings) super.clone();
	}

}
