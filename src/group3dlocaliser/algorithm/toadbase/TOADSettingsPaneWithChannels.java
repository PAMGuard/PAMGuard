package group3dlocaliser.algorithm.toadbase;

import PamController.SettingsPane;
import PamUtils.PamUtils;
import group3dlocaliser.ToadManagedSettingsPane;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.BorderPane;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamTitledBorderPane;
import pamViewFX.fxNodes.pamDialogFX.ManagedSettingsPane;
import pamViewFX.fxNodes.pamDialogFX.SwingFXDialogWarning;
import pamViewFX.fxNodes.utilityPanes.ChannelPanelFX;

public class TOADSettingsPaneWithChannels<T> extends ToadManagedSettingsPane<T> {

	private ManagedSettingsPane<T> toadSettingsPane;
	private ChSettingsPane tabbedPane;
	private TabPane tabPane;
	
	/**
	 * Get the tab pane for the settings. 
	 * @return the tab pane for the different settings. 
	 */
	@Override
	public TabPane getTabPane() {
		return tabPane;
	}

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
			
			Tab timingTab = new Tab("Timing", boderPane);
			timingTab.setGraphic(PamGlyphDude.createPamIcon("mdi2w-waveform"));

			tabPane.getTabs().add(timingTab );
			
	
			Tab channelsTab = new Tab("Channels", new PamTitledBorderPane("Channel Selection", channelPanel.getContentNode()));
					channelsTab.setGraphic(PamGlyphDude.createPamIcon("mdi2f-format-list-numbered-rtl"));
			
			tabPane.getTabs().add(channelsTab);
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
