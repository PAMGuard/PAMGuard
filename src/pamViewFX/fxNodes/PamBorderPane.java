package pamViewFX.fxNodes;

import clickTrainDetector.ClickTrainParams;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class PamBorderPane extends BorderPane {

	public PamBorderPane() {
		super();
	}

	public PamBorderPane(Node arg0, Node arg1, Node arg2, Node arg3, Node arg4) {
		super(arg0, arg1, arg2, arg3, arg4);
		// TODO Auto-generated constructor stub
	}

	public PamBorderPane(Node arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}
	
	public void setRightSpace(double space) {
		this.setRight(new EmptyPane(space, 0));
	}
	
	public void setLeftSpace(double space) {
		this.setLeft(new EmptyPane(space, 0));
	}
	
	public void setTopSpace(double space) {
		this.setTop(new EmptyPane(0, space));
	}
	
	public void setBottomSpace(double space) {
		this.setBottom(new EmptyPane(0, space));
	}
	
	private class EmptyPane extends Pane {
		public EmptyPane(double minWidth, double minHeight) {
			super();
			if (minWidth > 0) {
				this.setMinWidth(minWidth);
			}
			if (minHeight > 0) {
				this.setMinHeight(minHeight);
			}
		}
	}

}
