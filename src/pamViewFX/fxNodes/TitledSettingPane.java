package pamViewFX.fxNodes;

import PamController.SettingsPane;
import javafx.scene.Node;

/**
 * Simple decorator class to add a titled border to any settings pane. 
 * @author Douglas Gillespie
 *
 * @param <T> Parameter object type
 */
public class TitledSettingPane<T> extends SettingsPane<T> {

	private SettingsPane<T> settingsPane;
	private PamTitledBorderPane titledPane;

	/**
	 * Construct a titled settings pane based on an existing pane with the given title. 
	 * @param settingsPane Settings pane
	 * @param borderTitle border title
	 */
	public TitledSettingPane(SettingsPane<T> settingsPane, String borderTitle) {
		super(settingsPane.getOwnerWindow());
		this.settingsPane = settingsPane;
		this.titledPane = new PamTitledBorderPane(borderTitle, settingsPane.getContentNode());
	}

	/**
	 * Construct a titled settings pane based on an existing pane using the panes name as the title. 
	 * @param settingsPane Settings pane
	 */
	public TitledSettingPane(SettingsPane<T> settingsPane) {
		super(settingsPane.getOwnerWindow());
		this.settingsPane = settingsPane;
		this.titledPane = new PamTitledBorderPane(settingsPane.getName(), settingsPane.getContentNode());
	}

	@Override
	public T getParams(T currParams) {
		return settingsPane.getParams(currParams);
	}

	@Override
	public void setParams(T input) {
		settingsPane.setParams(input);
	}

	@Override
	public String getName() {
		return settingsPane.getName();
	}

	@Override
	public Node getContentNode() {
		return titledPane;
	}

	@Override
	public void paneInitialized() {
		settingsPane.paneInitialized();
	}

}
