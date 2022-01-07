package PamView;

import javax.swing.JToolBar;

import PamView.PamColors.PamColor;

public class PamToolBar extends JToolBar implements ColorManaged {


	private static final long serialVersionUID = 1L;

	@Override
	public PamColor getColorId() {
		return PamColor.BORDER;
	}

	/**
	 * 
	 */
	public PamToolBar() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param orientation
	 */
	public PamToolBar(int orientation) {
		super(orientation);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param name
	 * @param orientation
	 */
	public PamToolBar(String name, int orientation) {
		super(name, orientation);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param name
	 */
	public PamToolBar(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

}
