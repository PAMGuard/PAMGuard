package pamViewFX.fxNodes.utilityPanes;

import PamController.SettingsPane;
import PamUtils.PamUtils;
import PamguardMVC.PamConstants;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;
import pamViewFX.fxNodes.PamGridPane;

public class ChannelPanelFX extends SettingsPane<Integer> {

	private boolean multiColumn = false;

	private CheckBox channelBoxes[];
	
	private CheckBox allButton;
	
	private int availableChannels;
	
	private GridPane channelPane;
	
//	private PamVBox ctrlPane;
	
	private GridPane mainPane;
	
	public ChannelPanelFX(Object ownerWindow, boolean multiRow) {
		super(ownerWindow);
		this.multiColumn = multiRow;
		createPanel();
	}

	public ChannelPanelFX(Object ownerWindow) {
		super(ownerWindow);
		createPanel();
	}

	private void createPanel() {
		channelPane = new PamGridPane();
		channelBoxes = new CheckBox[PamConstants.MAX_CHANNELS];
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			channelBoxes[i] = new CheckBox("Channel " + i);
			channelBoxes[i].setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					checkAllStatus();
				}
			});
		}
		allButton = new CheckBox("All");
//		ctrlPane= new PamVBox();
//		ctrlPane.setFillWidth(false);
//		Button showAll = new Button("Select All");
//		Button showNone = new Button("Select None");
//		showAll.setMaxWidth(Double.MAX_VALUE);
//		showNone.setMaxWidth(Double.MAX_VALUE);
//		ctrlPane.setSpacing(10);
//		ctrlPane.setPadding(new Insets(10, 20, 10, 5)); 
//		ctrlPane.getChildren().addAll(showAll, showNone);
		
		mainPane = new PamGridPane();
//		mainPane.add(ctrlPane, 0, 0);
		mainPane.add(channelPane, 1, 0, 1, 2);
		
//		showAll.setOnAction(new EventHandler<ActionEvent>() {
//			@Override
//			public void handle(ActionEvent event) {
//				selectAll();
//			}
//		});
//		
//		showNone.setOnAction(new EventHandler<ActionEvent>() {
//			@Override
//			public void handle(ActionEvent event) {
//				selectNone();
//			}
//		});
		allButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				selectAll();
			}
		});
		
		showChannels();
		
	}
	
	protected void checkAllStatus() {
		int channelMap = getChannelMap();
		if ((channelMap & availableChannels) == availableChannels) {
			allButton.setSelected(true);
			allButton.setIndeterminate(false);
		}
		else if (channelMap == 0) {
			allButton.setSelected(false);
			allButton.setIndeterminate(false);
		}
		else {
			allButton.setIndeterminate(true);
		}
	}

	protected void selectAll() {
		if (allButton.isSelected()) {
			setChannelMap(getAvailableChannels());
		}
		else {
			setChannelMap(0);
		}
	}

	/**
	 * show the available channels. 
	 */
	private void showChannels() {
		int nChan = PamUtils.getNumChannels(availableChannels);
		int nCol = getNumColumns(nChan);
		int iCol = 0;
		int iRow = 1;
		channelPane.getChildren().clear();
		channelPane.add(allButton, 0, 0);
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			if ((availableChannels & 1<<i) != 0) {
				channelPane.add(channelBoxes[i], iCol, iRow);
				if (++iCol < nCol) {
//					iRow++;
				}
				else {
					iCol = 0;
					iRow ++;
				}
			}
		}
	}
	
	/**
	 * Set the selected channels
	 * @param channelMap bitmap of channels to select
	 */
	public void setChannelMap(int channelMap) {
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			channelBoxes[i].setSelected((channelMap & 1<<i) != 0);
		}
		allButton.setSelected((channelMap & availableChannels) == availableChannels);
	}
	
	/**
	 * Get the selected channels
	 * @return bitmap o selected channels
	 */
	public int getChannelMap() {
		int channelMap = 0;
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			if ((availableChannels & 1<<i) == 0) {
				continue;
			}
			if (channelBoxes[i].isSelected()) {
				channelMap |= 1<<i;
			}
		}
		return channelMap;
	}

	/**
	 * Get a sensible number of columns for the layout
	 * @param numChannels total number of channels to show
	 * @return sensible number of columns. 
	 */
	private int getNumColumns(int numChannels) {
		if (multiColumn == false) return 1;
		return numChannels > 6 ? 2 : 1;
	}
	
	/**
	 * @return the bitmap of availableChannels
	 */
	public int getAvailableChannels() {
		return availableChannels;
	}

	/**
	 * @param availableChannels the bitmap of availableChannels
	 */
	public void setAvailableChannels(int availableChannels) {
		this.availableChannels = availableChannels;
		showChannels();
	}

	@Override
	public Integer getParams(Integer currParams) {
		return getChannelMap();
	}

	@Override
	public void setParams(Integer input) {
		setChannelMap(input == null ? 0 : input);
	}

	@Override
	public String getName() {
		return "Channel Selection";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @return the multiColumn layout option
	 */
	public boolean isMultiColumn() {
		return multiColumn;
	}

	/**
	 * @param multiColumn set optional multicolumn layout
	 */
	public void setMultiColumn(boolean multiColumn) {
		this.multiColumn = multiColumn;
	}


	
}
