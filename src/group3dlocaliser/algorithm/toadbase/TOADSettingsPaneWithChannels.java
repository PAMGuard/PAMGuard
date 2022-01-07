package group3dlocaliser.algorithm.toadbase;

import PamController.SettingsPane;
import PamUtils.PamUtils;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.BorderPane;
import pamViewFX.fxNodes.PamTitledBorderPane;
import pamViewFX.fxNodes.pamDialogFX.ManagedSettingsPane;
import pamViewFX.fxNodes.pamDialogFX.SwingFXDialogWarning;
import pamViewFX.fxNodes.utilityPanes.ChannelPanelFX;

public class TOADSettingsPaneWithChannels<T> extends ManagedSettingsPane<T> {

	private ManagedSettingsPane<T> toadSettingsPane;
	private ChSettingsPane tabbedPane;
	private TabPane tabPane;
	private ChannelPanelFX channelPanel;
	private TOADBaseAlgorithm toadBaseAlgorithm;
	private Object parent;
	private TOADOptionsPane toadOptionsPane;
	

	public TOADSettingsPaneWithChannels(Object parent, TOADBaseAlgorithm toadBaseAlgorithm, ManagedSettingsPane<T> toadSettingsPane) {
		this.parent = parent;
		this.toadBaseAlgorithm = toadBaseAlgorithm;
		this.toadSettingsPane = toadSettingsPane;
		channelPanel = new ChannelPanelFX(parent);
		toadOptionsPane = new TOADOptionsPane(parent);
		tabbedPane = new ChSettingsPane(parent);
	}
	
	@Override
	public SettingsPane<T> getSettingsPane() {
		return tabbedPane;
	}

	@Override
	public boolean useParams(T newParams) {
		int chMap = channelPanel.getChannelMap();
		toadBaseAlgorithm.getToadBaseParams().setChannelBitmap(chMap);
		int nSelChannels = PamUtils.getNumChannels(chMap);
		if (nSelChannels < 3) {
			return SwingFXDialogWarning.showWarning(parent, "Channel selection", "Not enough channels selected");
		}
		TOADBaseParams ans = toadOptionsPane.getParams(toadBaseAlgorithm.getToadBaseParams());
		return ans != null;
	}

	@Override
	public T findParams() {
		channelPanel.setChannelMap(toadBaseAlgorithm.getToadBaseParams().getChannelBitmap());
		toadOptionsPane.setParams(toadBaseAlgorithm.getToadBaseParams());
		return null;
	}

	private class ChSettingsPane extends SettingsPane {
		
		/*
		 * Getting a bit messy with some standard params mixed in with detector specific ones. 
		 */
		

		public ChSettingsPane(Object ownerWindow) {
			super(ownerWindow);
			tabPane = new TabPane();
			tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
			BorderPane boderPane = new BorderPane();
			boderPane.setCenter(new PamTitledBorderPane("Timing options", toadOptionsPane.getContentNode()));
			boderPane.setBottom(toadSettingsPane.getSettingsPane().getContentNode());
			tabPane.getTabs().add(new Tab("Timing", boderPane));
			tabPane.getTabs().add(new Tab("Channels", new PamTitledBorderPane("Channel Selection", channelPanel.getContentNode())));
		}

		@Override
		public Object getParams(Object currParams) {
			return toadSettingsPane.getParams();
		}

		@Override
		public void setParams(Object input) {
			toadSettingsPane.setParams();
		}

		@Override
		public String getName() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Node getContentNode() {
			return tabPane;
		}

		@Override
		public void paneInitialized() {
			// TODO Auto-generated method stub
			
		}
		
	}

	/**
	 * @return the toadSettingsPane
	 */
	public ManagedSettingsPane<T> getToadSettingsPane() {
		return toadSettingsPane;
	}

	/**
	 * @return the channelPanel
	 */
	public ChannelPanelFX getChannelPanel() {
		return channelPanel;
	}
}
