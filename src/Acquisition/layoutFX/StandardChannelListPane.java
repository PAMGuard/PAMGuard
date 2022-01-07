package Acquisition.layoutFX;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import pamViewFX.fxNodes.PamComboBox;
import pamViewFX.fxNodes.PamTilePane;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;

import java.awt.Component;

import Acquisition.ChannelListPanel;
import PamguardMVC.PamConstants;

/**
 * Allows users to map hardware channels to software channels in PAMGUARD. 
 * @author Doug Gillespie. Converted JavaFX by Jamie Macaulay.
 *
 */
public class StandardChannelListPane implements ChannelListPanel {

//	public int channelList[] = new int[PamConstants.MAX_CHANNELS];
	private Label panelChannelLabel[] = new Label[PamConstants.MAX_CHANNELS];
	private PamComboBox<Integer> panelChannelList[] = new PamComboBox[PamConstants.MAX_CHANNELS];
	
	private Pane pane;
	int nChannels = 0;
	
	public StandardChannelListPane() {
		super();
		pane = createStandardChannelListPane();
	}
	
	@Override
	public int[] getChannelList() {
		if (nChannels == 0) {
			return null;
		}
		int[] list = new int[nChannels];
		for (int i = 0; i < nChannels; i++) {
			list[i] = panelChannelList[i].getSelectionModel().getSelectedItem();
//			System.out.println(String.format("%s get list item %d = %d", this.toString(), i, list[i]));
		}
		return list;
	}

	@Override
	public void setNumChannels(int nChannels) {
		
		nChannels = Math.min(nChannels, panelChannelList.length);
		
		this.nChannels = nChannels; 
		
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			panelChannelLabel[i].setVisible(i < nChannels);
			panelChannelList[i].setVisible(i < nChannels);
		}

//		
//		for (int iL = 0; iL < nChannels; iL++) {
//			panelChannelList[iL].removeAllItems();
//			for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
//				panelChannelList[iL].addItem(i);
//			}
//			
//		}
	}

	@Override
	public void setParams(int[] channelList) {

		if (channelList == null) {
			return;
		}
		for (int i = 0; i < Math.min(panelChannelList.length, channelList.length); i++){
			panelChannelList[i].getSelectionModel().select(channelList[i]);
//			System.out.println(String.format("%s Set box %d to item %d and get %d", 
//					this.toString(), i, channelList[i], panelChannelList[i].getSelectedItem()));
		}
	}
	@Override
	public boolean isDataOk() {
		
		// now check for repeat combinations. 
		for (int i = 0; i < nChannels-1; i++) {
			for (int j = i+1; j < nChannels; j++) {
				if (panelChannelList[i].getSelectionModel().getSelectedIndex() == panelChannelList[j].getSelectionModel().getSelectedIndex()) {
					String w = String.format("Channel %d is used twice\n"+
							"only use each channel once on each device",
							panelChannelList[i].getSelectionModel().getSelectedIndex());
					PamDialogFX.showWarning(null, "Error", w);
					return false;			
				}
			}
		}
		return true;
	}


	private Pane createStandardChannelListPane() {

		/* code for select channel */
		/*
		 * put this in a separate panel so it can be hidden if 
		 * it's not possible to change these parameters. 
		 * 
		 * Text information updated DG & JG 12/8/08
		 */
		PamVBox cP;
		cP = new PamVBox();
		cP.setSpacing(5);
//		addComponent(channelListPanel, new JLabel("Select Hardware (HW) Channels"), c2);
		cP.getChildren().add(new Label("Map Hardware (HW) to Software (SW) Channels"));
		
		String s = "<html>PAMGUARD channel numbering starts at 0.<br>Your hardware channel numbering may start at 1.";
		s += "<br>So be aware. If you've put a plug into socket 1, <br>you probably want to select channel 0, etc.</html>";
		
		PamTilePane tP=new PamTilePane();
		tP.setPrefColumns(4);
		
		
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++){ //Xiao Yan Deng
		//for (int i = 0; i < getNumChannels(); i++){
			
			tP.getChildren().add(panelChannelLabel[i] = 
				new Label("   SW Ch " + i + " = HW Ch "));
			//constraints.gridwidth = 2;
			tP.getChildren().add(panelChannelList[i] = new PamComboBox<Integer>());
			panelChannelLabel[i].setTooltip(new Tooltip(s));
			panelChannelList[i].setTooltip(new Tooltip(s));
			
//			System.out.println(String.format("Fill channel list %d", i));
			for (int iC = 0; iC < PamConstants.MAX_CHANNELS; iC++) {
				panelChannelList[i].getItems().add(iC);
			}
			
		}
		return cP;
	}

	@Override
	public Node getNode() {
		return pane;
	}

	@Override
	public Component getComponent() {
		// TODO Auto-generated method stub
		return null;
	}
	

}
