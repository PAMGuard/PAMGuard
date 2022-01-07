package pamViewFX.fxNodes;

import java.util.HashSet;
import java.util.Set;

import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

/**
 * A draggable tab that can optionally be detached from its tab pane and shown
 * in a separate window. This can be added to any normal PamTabPane, however a
 * PamTabPane with draggable tabs must *only* have DraggableTabs, normal tabs and
 * DrragableTabs mixed will cause issues!
 * <p>
 * @author Michael Berry, Jamie Macaulay
 */
public class PamTabFX extends Tab {
 
    private static final Set<PamTabPane> TabPanes = new HashSet<>();
    private Label nameLabel;
    private Text dragText;
    private static final Stage markerStage;
    private Stage dragStage;
    private boolean detachable;
 
    static {
        markerStage = new Stage();
        markerStage.initStyle(StageStyle.UNDECORATED);
        Rectangle dummy = new Rectangle(3, 10, Color.web("#555555"));
        StackPane markerStack = new StackPane();
        markerStack.getChildren().add(dummy);
        markerStage.setScene(new Scene(markerStack));
    }
 
    /**
     * Create a new draggable tab. This can be added to any normal PamTabPane,
     * however a PamTabPane with draggable tabs must *only* have DraggableTabs,
     * normal tabs and DrragableTabs mixed will cause issues!
     * <p>
     * @param text the text to appear on the tag label.
     */
    public PamTabFX(String text) {
        nameLabel = new Label(text);
        setGraphic(nameLabel);
        detachable = true;
        dragStage = new Stage();
        dragStage.initStyle(StageStyle.UNDECORATED);
        StackPane dragStagePane = new StackPane();
        dragStagePane.setStyle("-fx-background-color:#DDDDDD;");
        dragText = new Text(text);
        StackPane.setAlignment(dragText, Pos.CENTER);
        dragStagePane.getChildren().add(dragText);
        dragStage.setScene(new Scene(dragStagePane));
        nameLabel.setOnMouseDragged(new EventHandler<MouseEvent>() {
 
            @Override
            public void handle(MouseEvent t) {
                dragStage.setWidth(nameLabel.getWidth() + 10);
                dragStage.setHeight(nameLabel.getHeight() + 10);
                dragStage.setX(t.getScreenX());
                dragStage.setY(t.getScreenY());
                dragStage.show();
                Point2D screenPoint = new Point2D(t.getScreenX(), t.getScreenY());
                TabPanes.add((PamTabPane) getTabPane());
                InsertData data = getInsertData(screenPoint);
                if(data == null || data.getInsertPane().getTabs().isEmpty()) {
                    markerStage.hide();
                }
                else {
                    int index = data.getIndex();
                    boolean end = false;
                    if(index == data.getInsertPane().getTabs().size()) {
                        end = true;
                        index--;
                    }
                    Rectangle2D rect = getAbsoluteRect(data.getInsertPane().getTabs().get(index));
                    if(end) {
                        markerStage.setX(rect.getMaxX() + 13);
                    }
                    else {
                        markerStage.setX(rect.getMinX());
                    }
                    markerStage.setY(rect.getMaxY() + 10);
                    markerStage.show();
                }
            }
        });
        nameLabel.setOnMouseReleased(new EventHandler<MouseEvent>() {
 
            @Override
            public void handle(MouseEvent t) {
                markerStage.hide();
                dragStage.hide();
                if(!t.isStillSincePress()) {
                    Point2D screenPoint = new Point2D(t.getScreenX(), t.getScreenY());
                    PamTabPane oldPamTabPane = (PamTabPane) getTabPane();
                    int oldIndex = oldPamTabPane.getTabs().indexOf(PamTabFX.this);
                    TabPanes.add(oldPamTabPane);
                    InsertData insertData = getInsertData(screenPoint);
                    if(insertData != null) {
                        int addIndex = insertData.getIndex();
                        if(oldPamTabPane == insertData.getInsertPane() && oldPamTabPane.getTabs().size() == 1) {
                            return;
                        }
                        oldPamTabPane.getTabs().remove(PamTabFX.this);
                        if(oldIndex < addIndex && oldPamTabPane == insertData.getInsertPane()) {
                            addIndex--;
                        }
                        if(addIndex > insertData.getInsertPane().getTabs().size()) {
                            addIndex = insertData.getInsertPane().getTabs().size();
                        }
                        insertData.getInsertPane().getTabs().add(addIndex, PamTabFX.this);
                        insertData.getInsertPane().selectionModelProperty().get().select(addIndex);
                        return;
                    }
                    if(!detachable) {
                        return;
                    }
                    final Stage newStage = new Stage();
//                    final Pane pane=createNewPane(PamTabFX.this, newStage);
                    final PamTabPane pane = new PamTabPane();
                    TabPanes.add(pane);
                    newStage.setOnHiding(new EventHandler<WindowEvent>() {
 
                        @Override
                        public void handle(WindowEvent t) {
                            TabPanes.remove(pane);
                        }
                    });
                    getTabPane().getTabs().remove(PamTabFX.this);
                    pane.getTabs().add(PamTabFX.this);
                    pane.getTabs().addListener(new ListChangeListener<Tab>() {
 
                        @Override
                        public void onChanged(ListChangeListener.Change<? extends Tab> change) {
                            if(pane.getTabs().isEmpty()) {
                                newStage.hide();
                            }
                        }
                    });
          
                    newStage.setScene(new Scene(createNewPane(PamTabFX.this, pane,  newStage)));
                    newStage.initStyle(StageStyle.UTILITY);
                    newStage.setX(t.getScreenX());
                    newStage.setY(t.getScreenY());
                    newStage.show();
                    pane.requestLayout();
                    pane.requestFocus();
                }
            }
 
        });
    }
    
    	
    /**
     * Called whenever a new stage is being created. Add the new pane to the new stage. By default this is simply a new tab pane 
     * but this function can be overridden for more complex panes. 
     */
    public Pane createNewPane(Tab tab, PamTabPane PamTabPane, Stage newStage){
    	return new PamBorderPane(PamTabPane);
    	
    } 
    
    /**
     * Set whether it's possible to detach the tab from its pane and move it to
     * another pane or another window. Defaults to true.
     * <p>
     * @param detachable true if the tab should be detachable, false otherwise.
     */
    public void setDetachable(boolean detachable) {
        this.detachable = detachable;
    }
 
    /**
     * Set the label text on this draggable tab. This must be used instead of
     * setText() to set the label, otherwise weird side effects will result!
     * <p>
     * @param text the label text for this tab.
     */
    public void setLabelText(String text) {
        nameLabel.setText(text);
        dragText.setText(text);
    }
 
    private InsertData getInsertData(Point2D screenPoint) {
        for(PamTabPane PamTabPane : TabPanes) {
            Rectangle2D tabAbsolute = getAbsoluteRect(PamTabPane);
            if(tabAbsolute.contains(screenPoint)) {
                int tabInsertIndex = 0;
                if(!PamTabPane.getTabs().isEmpty()) {
                    Rectangle2D firstTabRect = getAbsoluteRect(PamTabPane.getTabs().get(0));
                    if(firstTabRect.getMaxY()+60 < screenPoint.getY() || firstTabRect.getMinY() > screenPoint.getY()) {
                        return null;
                    }
                    Rectangle2D lastTabRect = getAbsoluteRect(PamTabPane.getTabs().get(PamTabPane.getTabs().size() - 1));
                    if(screenPoint.getX() < (firstTabRect.getMinX() + firstTabRect.getWidth() / 2)) {
                        tabInsertIndex = 0;
                    }
                    else if(screenPoint.getX() > (lastTabRect.getMaxX() - lastTabRect.getWidth() / 2)) {
                        tabInsertIndex = PamTabPane.getTabs().size();
                    }
                    else {
                        for(int i = 0; i < PamTabPane.getTabs().size() - 1; i++) {
                            Tab leftTab = PamTabPane.getTabs().get(i);
                            Tab rightTab = PamTabPane.getTabs().get(i + 1);
                            if(leftTab instanceof PamTabFX && rightTab instanceof PamTabFX) {
                                Rectangle2D leftTabRect = getAbsoluteRect(leftTab);
                                Rectangle2D rightTabRect = getAbsoluteRect(rightTab);
                                if(betweenX(leftTabRect, rightTabRect, screenPoint.getX())) {
                                    tabInsertIndex = i + 1;
                                    break;
                                }
                            }
                        }
                    }
                }
                return new InsertData(tabInsertIndex, PamTabPane);
            }
        }
        return null;
    }
 
    private Rectangle2D getAbsoluteRect(Control node) {
        return new Rectangle2D(node.localToScene(node.getLayoutBounds().getMinX(), node.getLayoutBounds().getMinY()).getX() + node.getScene().getWindow().getX(),
                node.localToScene(node.getLayoutBounds().getMinX(), node.getLayoutBounds().getMinY()).getY() + node.getScene().getWindow().getY(),
                node.getWidth(),
                node.getHeight());
    }
 
    private Rectangle2D getAbsoluteRect(Tab tab) {
        Control node = ((PamTabFX) tab).getLabel();
        return getAbsoluteRect(node);
    }
 
    public Label getLabel() {
        return nameLabel;
    }
    
 
    private boolean betweenX(Rectangle2D r1, Rectangle2D r2, double xPoint) {
        double lowerBound = r1.getMinX() + r1.getWidth() / 2;
        double upperBound = r2.getMaxX() - r2.getWidth() / 2;
        return xPoint >= lowerBound && xPoint <= upperBound;
    }
 
    private static class InsertData {
 
        private final int index;
        private final PamTabPane insertPane;
 
        public InsertData(int index, PamTabPane insertPane) {
            this.index = index;
            this.insertPane = insertPane;
        }
 
        public int getIndex() {
            return index;
        }
 
        public PamTabPane getInsertPane() {
            return insertPane;
        }
 
    }
}