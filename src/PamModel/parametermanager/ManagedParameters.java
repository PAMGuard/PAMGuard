package PamModel.parametermanager;


/**
 * Interface to add to any configuration settings object which 
 * will contain / generate / allow creation of descriptors 
 * of what's within that parameter set. </br></br>
 * In the simplest case, this should be the method overridden in
 * the class:</br>
 * <pre>
 * 	&#64;Override
 *	public PamParameterSet getParameterSet() {
 *		PamParameterSet ps = PamParameterSet.autoGenerate(this);
 *		return ps;
 *	}
 *</pre>
 * Notes:</br>
 * <ul>
 * <li>Any public fields will automatically be included in the list</li>
 * <li>Any private/protected fields will be included IF the have a getter that matches their name exactly,
 * with no extra parameters.  Boolean fields need <em>is...</em>, while the rest need <em>get...</em></li>
 * <ul>
 * <li>for field <em>int channelmap</em>, would need a getter <em>getChannelmap()</em>.
 *   If the getter is <em>getChannelmap(int selectWhichMap)</em> with the extra parameter, it will not work</li>
 * <li>for field <em>boolean hasLatLong</em>, would need a getter <em>isHasLatLong()</em></li>
 * </ul>
 * <li>transient and static fields are not included</li>
 * <li>if a private/protected field does not have a getter and you want to include it, use this code:</li>
 * </ul>
 * <pre>
 * 	&#64;Override
 *	public PamParameterSet getParameterSet() {
 *		PamParameterSet ps = PamParameterSet.autoGenerate(this);
 *		try {
 *			Field field = this.getClass().getDeclaredField("lastTriggerEnd");
 *			ps.put(new PrivatePamParameterData(this, field) {
 *				public Object getData() throws IllegalArgumentException, IllegalAccessException {
 *					return lastTriggerEnd;
 *				}
 *			});
 *		} catch (NoSuchFieldException | SecurityException e) {
 *			e.printStackTrace();
 *		}
 *		return ps;
 *	}
 * </pre>
 * <ul>
 * <li>note that in the above code, you are specifying what gets returned from a getData() call to that class.  You can specify
 * whatever you want.  If the field were <em>LatLong loc</em> but you were only interested in the latitude, you could write
 * <em>return loc.getLatitude();</em> 
 * <li>if the field is a primitive, array, ArrayList, List, Map, Colour or File, PamguardXMLWriter.writeField will
 * write the contents properly (including iterating through an array/List).  Other types of classes will just give the class name</li>
 * <li>if you want more information from a class, it needs to implement ManagedParameters as well.  For example,
 * the ClipSettings class has this field:</li>
 * </ul>
 * <pre>
 * private ArrayList{@code <ClipGenSetting>} clipGenSettings;
 * </pre>
 * <ul>
 * <li>In order for all of the fields in ClipGenSettings to be included in the export, it will also need to extend ManagedParameters</li>
 * </ul>
 * <li>Finally, if the class is a superclass for something else, then it's fields will be skipped if the subclass makes the call.  See
 * the notes for classes RecorderTriggerData and WMRecorderTriggerData for an example of this.</li>
 * </ul>
 * 
 * 
 * 
 * @author dg50
 * 
 * 
 *
 */
public interface ManagedParameters {

	/**
	 * Get a set of data that describes all of the parameters in a class
	 * @return description of the parameters in a class. 
	 */
	public PamParameterSet getParameterSet();
	
}
