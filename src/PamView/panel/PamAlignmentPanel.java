package PamView.panel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.LayoutManager;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.Border;

import PamView.ColorManaged;
import PamView.PamColors.PamColor;

public class PamAlignmentPanel extends JPanel implements ColorManaged {

	private static final long serialVersionUID = 1L;
	
	private String alignment = BorderLayout.NORTH;

	private AlignedPanel alignedPanel;
	
	private Component alignedComponent;


	public PamAlignmentPanel(String alignment) {
		this(null, alignment, false);
	}

	public PamAlignmentPanel(JComponent component, String alignment) {
		this(component, alignment, false);
	}
	
	public PamAlignmentPanel(JComponent component, String alignment, boolean stealBorder) {
		super();
		super.setLayout(new BorderLayout());
		alignedPanel = new AlignedPanel();
		super.add(alignment, alignedPanel);
		if (component != null) {
			alignedPanel.add(alignedComponent = component);
		}
		if (stealBorder) {
			this.stealBorder();
		}
	}
	
	public PamAlignmentPanel(LayoutManager innerLayout, String alignment) {
		this(null, alignment, false);
		alignedPanel.setLayout(innerLayout);
	}
	
	/**
	 * Steal the border from the west component and put it on the main window
	 */
	private void stealBorder() {
		if (alignedComponent == null) {
			return;
		}
		if (alignedComponent instanceof JComponent) {
			JComponent jWest = (JComponent) alignedComponent;
			Border border = jWest.getBorder();
			if (border != null) {
				jWest.setBorder(null);
				this.setBorder(border);
			}
		}
	}

	/**
	 * @param isDoubleBuffered
	 */
	public PamAlignmentPanel(boolean isDoubleBuffered, String alignment) {
		super(isDoubleBuffered);
	}

	/* (non-Javadoc)
	 * @see java.awt.Container#add(java.awt.Component, int)
	 */
	@Override
	public Component add(Component arg0, int arg1) {
		alignedComponent = arg0;
		return alignedPanel.add(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see java.awt.Container#add(java.awt.Component, java.lang.Object, int)
	 */
	@Override
	public void add(Component arg0, Object arg1, int arg2) {
		alignedComponent = arg0;
		alignedPanel.add(arg0, arg1, arg2);
	}

	/* (non-Javadoc)
	 * @see java.awt.Container#add(java.awt.Component, java.lang.Object)
	 */
	@Override
	public void add(Component arg0, Object arg1) {
		alignedComponent = arg0;
		alignedPanel.add(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see java.awt.Container#add(java.awt.Component)
	 */
	@Override
	public Component add(Component arg0) {
		alignedComponent = arg0;
		return alignedPanel.add(arg0);
	}

	/* (non-Javadoc)
	 * @see java.awt.Container#add(java.lang.String, java.awt.Component)
	 */
	@Override
	public Component add(String arg0, Component arg1) {
		alignedComponent = arg1;
		return alignedPanel.add(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see java.awt.Container#getLayout()
	 */
	@Override
	public LayoutManager getLayout() {
		return alignedPanel.getLayout();
	}

	/* (non-Javadoc)
	 * @see java.awt.Container#setLayout(java.awt.LayoutManager)
	 */
	@Override
	public void setLayout(LayoutManager mgr) {
		if (alignedPanel == null) {
			super.setLayout(mgr);
		}
		else {
			alignedPanel.setLayout(mgr);
		}
	}
	
	private class AlignedPanel extends PamPanel {

		private static final long serialVersionUID = 1L;

		/**
		 * 
		 */
		public AlignedPanel() {
			super();
		}

		@Override
		public PamColor getColorId() {
			return PamAlignmentPanel.this.getColorId();
		}
		
	}

	/* (non-Javadoc)
	 * @see java.awt.Container#remove(int)
	 */
	@Override
	public void remove(int arg0) {
		super.remove(arg0);
	}

	/* (non-Javadoc)
	 * @see java.awt.Container#removeAll()
	 */
	@Override
	public void removeAll() {
		super.removeAll();
	}

	@Override
	public PamColor getColorId() {
		if (alignedComponent instanceof ColorManaged) {
			return ((ColorManaged) alignedComponent).getColorId();
		}
		else {
			return null;
		}
	}
	
//	/**
//	 * Getter for inner component. Needed for box layouts. 
//	 * @return
//	 */
//	public Component getLayoutComponent() {
//		return alignedComponent;
//	}


}
