package pamViewFX.fxNodes.pamDialogFX;

import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import pamViewFX.fxStyles.PamStylesManagerFX;

/**
 * Daft panel that takes it's content out of a scene and then puts it pack in again which 
 * somehow convinces the scene to resize. This is used in the Swing dialog that 
 * wraps FX settings panes. 
 * @author Doug Gillespie
 *
 */
public class PamJFXPanel extends JFXPanel {

	private Pane rootPane;
	private Scene scene;
	private BorderPane midBorderPane;

	public PamJFXPanel() {
		super();
	}

	public Scene setRoot(Pane root) {
		this.rootPane = root;
		
		midBorderPane = new BorderPane(root);
		
		this.scene = new Scene(midBorderPane);

		scene.getStylesheets().addAll(PamStylesManagerFX.getPamStylesManagerFX().getCurStyle().getDialogCSS());  

		setScene(scene);
		
		return scene;
	}

	/**
	 * Repack the root panel into a new scene inside the JFXPanel in order 
	 * to get it to resize correctly. 
	 */
	public void prePackFX() {
		if (rootPane != null && midBorderPane != null) {
			midBorderPane.getChildren().remove(rootPane);
			rootPane.autosize();
			setRoot(rootPane);
		}
	}

//	/* (non-Javadoc)
//	 * @see javafx.embed.swing.JFXPanel#getPreferredSize()
//	 */
//	@Override
//	public Dimension getPreferredSize() {
////		if (rootPane == null || scene == null) {
////			return super.getPreferredSize();
////		}
////		System.out.println("In PamJFXPanel getPreferred Size");
//////		rootPane.autosize();
////		System.out.printf("Root min size = %3.1fx%3.1f or %3.1fx%3.1f\n", rootPane.getPrefWidth(), rootPane.getPrefHeight(), rootPane.prefWidth(0), rootPane.prefHeight(0));
////		System.out.printf("Scene size = %3.1fx%3.1f\n", scene.getWidth(), scene.getHeight());
////		
////		Dimension d = super.getPreferredSize();
////		System.out.printf("Swing size = %dx%d\n", d.width, d.height);
////		sayAllSizes(rootPane);
//		return super.getPreferredSize();
//	}
//
//	private void sayAllSizes(Pane pane) {
//		if (pane.getClass().getName().contains("Silly"))
//			System.out.printf("%s size = %3.1fx%3.1f\n", pane.getClass().getName(), pane.prefWidth(0), pane.prefHeight(0));
//		ObservableList<Node> kids = pane.getChildren();
//		for (int i = 0; i < kids.size(); i++) {
//			Node kid = kids.get(i);
//			if (kid instanceof Pane) {
//				sayAllSizes((Pane) kid);
//			}
//			else if (kid instanceof TabPane) {
//				TabPane tabPane = (TabPane) kid;
//				tabPane.autosize();
////				tabPane.
//				for (int t = 0; t < tabPane.getTabs().size(); t++) {
//					Tab tab = tabPane.getTabs().get(t);
//					if (tab.getContent() instanceof Pane) {
//						sayAllSizes((Pane) tab.getContent());
//					}
//				}
//			}
//			else {
////				System.out.printf("Cant get size of %s\n", kid.getClass().getName());
//			}
//		}
//	}

}
