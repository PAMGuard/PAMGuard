package clickDetector.layoutFX.clickClassifiers;

import javafx.scene.Node;

public interface ClassifyPaneFX {

	public Node getNode();

	public void setParams();

	public boolean getParams();

	public String getHelpPoint();

	public void setActive(boolean b);

}
