package clipgenerator.clipDisplay;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

public class ClipLayout extends FlowLayout {

	private static final long serialVersionUID = 1L;
	
	private int panelWidth;

	public ClipLayout() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ClipLayout(int align, int hgap, int vgap) {
		super(align, hgap, vgap);
		// TODO Auto-generated constructor stub
	}

	public ClipLayout(int align) {
		super(align);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see java.awt.FlowLayout#layoutContainer(java.awt.Container)
	 */
	@Override
	public void layoutContainer(Container target) {
		super.layoutContainer(target);
		setClipSizes(target);
	}

	public Dimension setClipSizes(Container target) {
		synchronized (target.getTreeLock()) {
			Dimension dim = new Dimension(0, 0);
			int nmembers = target.getComponentCount();
//			boolean firstVisibleComponent = true;
//			boolean useBaseline = getAlignOnBaseline();
//			int maxAscent = 0;
//			int maxDescent = 0;

			int maxWidth = panelWidth;
			if (maxWidth == 0) {
				maxWidth = target.getWidth();
			}
			int currRowHeight = 0;
			int currRowWidth = 0;
			int nInRow = 0;
			int totalHeight = getVgap()*2;

			for (int i = 0 ; i < nmembers ; i++) {
				Component m = target.getComponent(i);
				if (m.isVisible()) {
					Dimension d = m.getPreferredSize();
//					System.out.println(String.format("Layout component %d with height %d", i, d.height));
					if (nInRow > 0 && d.width + currRowWidth > (maxWidth - getHgap())) {
						// start a new row.
						totalHeight += currRowHeight + getVgap();
						currRowHeight = d.height;
						currRowWidth = d.width;
						nInRow = 1;
					}
					else {
						currRowWidth += d.width + getHgap()*2;
						currRowHeight = Math.max(d.height, currRowHeight);
						nInRow++;
					}
					//Check for special case where a single unit is wider than the panelWidth
					maxWidth = Math.max(currRowWidth, maxWidth);
				}
			}
			totalHeight += currRowHeight;
			Insets insets = target.getInsets();
			dim.width = maxWidth;// + insets.left + insets.right + getHgap()*2;
			dim.height = totalHeight;
//			System.out.println(String.format("target %d members w=%d h=%d, output w=%d h=%d", nmembers,
//					target.getWidth(), target.getHeight(), dim.width, dim.height));
			return dim;
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.FlowLayout#preferredLayoutSize(java.awt.Container)
	 */
	@Override
	public Dimension preferredLayoutSize(Container target) {
		return setClipSizes(target);
	}

	/**
	 * @return the panelWidth
	 */
	public int getPanelWidth() {
		return panelWidth;
	}

	/**
	 * @param panelWidth the panelWidth to set
	 */
	public void setPanelWidth(int panelWidth) {
		this.panelWidth = panelWidth;
	}

//	/* (non-Javadoc)
//	 * @see java.awt.FlowLayout#layoutContainer(java.awt.Container)
//	 */
//	@Override
//	public void layoutContainer(Container arg0) {
//		// TODO Auto-generated method stub
//		super.layoutContainer(arg0);
//	}

}
