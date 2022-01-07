package PamView.dialog;

import javax.swing.Icon;

import PamView.ColorManaged;

public class VerticalLabel extends PamLabel implements ColorManaged{

    private static final long serialVersionUID = 1L;
    
	static VerticalLabelUI labelUI;
	static {
		labelUI = new VerticalLabelUI(false);
	}
	
	{
		setUI(labelUI);
		setDefaultColor(null);
	}
	
	public VerticalLabel() {
		super();
	}

	/**
	 * @param image
	 * @param horizontalAlignment
	 */
	public VerticalLabel(Icon image, int horizontalAlignment) {
		super(image, horizontalAlignment);
	}

	/**
	 * @param image
	 */
	public VerticalLabel(Icon image) {
		super(image);
	}

	/**
	 * @param txt
	 * @param image
	 * @param horizontalAlignment
	 */
	public VerticalLabel(String txt, Icon image, int horizontalAlignment) {
		super(txt, image, horizontalAlignment);
	}

	/**
	 * @param txt
	 * @param horizontalAlignment
	 */
	public VerticalLabel(String txt, int horizontalAlignment) {
		super(txt, horizontalAlignment);
	}

	/**
	 * @param txt
	 */
	public VerticalLabel(String txt) {
		super(txt);
	}

	/**
	 * 
	 * @param colorManaged Manage colours, using night layout when selected
	 */
	public VerticalLabel(boolean colorManaged) {
		super();
		if (colorManaged) {
			setDefaultColor(defaultColor);
		}
	}

	/**
	 * @param image
	 * @param horizontalAlignment
	 */
	public VerticalLabel(Icon image, int horizontalAlignment, boolean colorManaged) {
		super(image, horizontalAlignment);
		if (colorManaged) {
			setDefaultColor(defaultColor);
		}
	}

	/**
	 * @param image
	 * @param colorManaged Manage colours, using night layout when selected
	 */
	public VerticalLabel(Icon image, boolean colorManaged) {
		super(image);
		if (colorManaged) {
			setDefaultColor(defaultColor);
		}
	}

	/**
	 * @param txt
	 * @param image
	 * @param horizontalAlignment
	 * @param colorManaged Manage colours, using night layout when selected
	 */
	public VerticalLabel(String txt, Icon image, int horizontalAlignment, boolean colorManaged) {
		super(txt, image, horizontalAlignment);
		if (colorManaged) {
			setDefaultColor(defaultColor);
		}
	}

	/**
	 * @param txt
	 * @param horizontalAlignment
	 * @param colorManaged Manage colours, using night layout when selected
	 */
	public VerticalLabel(String txt, int horizontalAlignment, boolean colorManaged) {
		super(txt, horizontalAlignment);
		if (colorManaged) {
			setDefaultColor(defaultColor);
		}
	}

	/**
	 * @param txt
	 * @param colorManaged Manage colours, using night layout when selected
	 */
	public VerticalLabel(String txt, boolean colorManaged) {
		super(txt);
		if (colorManaged) {
			setDefaultColor(defaultColor);
		}
	}
	
}
