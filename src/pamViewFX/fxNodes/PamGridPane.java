package pamViewFX.fxNodes;

import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

public class PamGridPane extends GridPane {
	
	private boolean vFill, hFill;

	public PamGridPane() {
		super();
		vFill = false;
		hFill = true;
		setVgap(3);
		setHgap(3);
	}

	/* (non-Javadoc)
	 * @see javafx.scene.layout.GridPane#add(javafx.scene.Node, int, int)
	 */
	@Override
	public void add(Node child, int columnIndex, int rowIndex) {
		add(child, columnIndex, rowIndex, 1, 1);
	}

	/* (non-Javadoc)
	 * @see javafx.scene.layout.GridPane#add(javafx.scene.Node, int, int, int, int)
	 */
	@Override
	public void add(Node child, int columnIndex, int rowIndex, int colspan, int rowspan) {
		try {
		super.add(child, columnIndex, rowIndex, colspan, rowspan);
		if (child instanceof Region) {
			Region r = (Region) child;
			if (vFill) {
				r.setMaxHeight(Double.MAX_VALUE);
				GridPane.setFillHeight(r, vFill);
			}
			if (hFill) {
				r.setMaxWidth(Double.MAX_VALUE);
				GridPane.setFillWidth(r, hFill);
			}
		}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the vFill
	 */
	public boolean isvFill() {
		return vFill;
	}

	/**
	 * @param vFill the vFill to set
	 */
	public void setvFill(boolean vFill) {
		this.vFill = vFill;
	}

	/**
	 * @return the hFill
	 */
	public boolean ishFill() {
		return hFill;
	}

	/**
	 * @param hFill the hFill to set
	 */
	public void sethFill(boolean hFill) {
		this.hFill = hFill;
	}

}
