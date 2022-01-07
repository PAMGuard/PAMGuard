package PamView.symbol;

import java.io.Serializable;

/**
 * Symbol options for annotations. For now part of standard symbol options, 
 * but might grow and might also end up incorporated more into bespoke annotation options. 
 * @author dg50
 *
 */
public class AnnotationSymbolOptions implements Serializable, Cloneable {

	public static final long serialVersionUID = 1L;
	
	public static final int CHANGE_LINE_COLOUR = 0x1;
	public static final int CHANGE_FILL_COLOUR = 0x2;
	public static final int CHANGE_SYMBOL = 0x4;
	
	public int changeChoice = 0x7;

}
