package userDisplay;

import java.awt.Dimension;
import java.awt.Point;
import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

/**
 * Data stored in a list for each component of a User Display Panel. 
 * This contains enough information to recreate, name and position each 
 * display component.   
 * @author dg50
 *
 */
public class DisplayProviderParameters  implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	private String displayName;
	
	/*
	 * changes this from Class to String since class would fail in 
	 * serialisation if the object changes significantly even 
	 * with a fixed serial id if it incorporated information 
	 * from sub classes
	 */
	private String providerClassName; // need to ensure a fixed serialVersionUID in any class here !
	
	/**
	 * Need to add in another name to identify modules. 
	 * Originally, providers were 'built' in, so there was only one of them and always
	 * did the same thing. Now we have display providers in modules like the alarm
	 * or the bearinglocaliser, so need to know which alarm or bearing localiser
	 * they come from - so need an additional identifier. 
	 */
	private String displayProviderName;
	
	public Point location;
	 
	public Dimension size;


	public DisplayProviderParameters(String providerClassName, String displayProviderName, String displayName) {
		super();
		this.providerClassName = providerClassName;
		this.displayProviderName = displayProviderName;
		this.displayName = displayName;
	}

	@Override
	protected DisplayProviderParameters clone() {
		try {
			return (DisplayProviderParameters) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 
	 * @return The name to use when searching for the right display provider. 
	 */
	public String getProviderName() {
		return displayProviderName;
	}

	/**
	 * @return the displayName
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * @param displayName the displayName to set
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * @return the providerClassName
	 */
	public String getProviderClassName() {
		return providerClassName;
	}

	/**
	 * @return the displayProviderName
	 */
	public String getDisplayProviderName() {
		return displayProviderName;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}

}
