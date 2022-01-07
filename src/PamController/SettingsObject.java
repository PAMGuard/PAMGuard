package PamController;

import java.io.Serializable;

/**
 * All settings objects must implements both Cloneable and Serializable
 * to make an interface that has both and use for settings (a bit after the 
 * horse has bolted this, but may be able to apply retrospectively). 
 * @author Doug Gillespie
 *
 */
public interface SettingsObject extends Cloneable, Serializable {

}
