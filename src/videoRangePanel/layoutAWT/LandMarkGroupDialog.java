package videoRangePanel.layoutAWT;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import videoRangePanel.VRControl;
import videoRangePanel.vrmethods.landMarkMethod.LandMarkGroup;
import PamView.dialog.PamDialog;
import PamView.panel.PamPanel;

public class LandMarkGroupDialog extends PamDialog {
	
	private static LandMarkGroupDialog singleInstance;
	private Frame frame;
	private VRControl vrControl;
	private LandMarkPanel landMarkManagerPanel;
	private JTextField name;

	public LandMarkGroupDialog(Frame parentFrame, VRControl vrControl) {
		
		super(parentFrame, "Landmark Manager", false);
		this.frame=parentFrame;
		this.vrControl=vrControl;
		this.landMarkManagerPanel=new LandMarkPanel(vrControl);
		
		PamPanel namePanel=new PamPanel(new BorderLayout());
		namePanel.setBorder(new TitledBorder("Land Mark Group Name"));
		namePanel.add(BorderLayout.CENTER, name=new JTextField());
		
		PamPanel mainPanel=new PamPanel(new BorderLayout());
		mainPanel.add(BorderLayout.NORTH, namePanel);
		mainPanel.add(BorderLayout.CENTER, landMarkManagerPanel);

		setDialogComponent(mainPanel);
		
		super.setSize(700, 500);
		setResizable(true);	
		
//		pack();
	}
	

		
		
		@Override
		public boolean getParams() {
			try{
				landMarkManagerPanel.getLandMarkList();
				if (landMarkManagerPanel.getLandMarkList()!=null) landMarkManagerPanel.getLandMarkList().setGroupName(name.getText());
			}
			catch(Exception e){
				return false; 
			}
			return true;
		}
		
		public boolean setParams(){
			if (landMarkManagerPanel.getLandMarkList().getName()!=null) name.setText(landMarkManagerPanel.getLandMarkList().getName());
			return true;
		}
	
		@Override
		public void cancelButtonPressed() {
			setLandMarkList(null);
			
		}
		
	
		@Override
		public void restoreDefaultSettings() {
			// TODO Auto-generated method stub
			
		}
		
		private void setLandMarkList(LandMarkGroup landMrkGrp) {
			landMarkManagerPanel.setLandMarkList(landMrkGrp);
		}
		
		private LandMarkGroup getLandMarkList() {
			return landMarkManagerPanel.getLandMarkList();
		}

		
		/**
		 * Show the LandMark dialog. Allows users to add landmark groups etc. 
		 * @param frame- the parent window
		 * @param vrControl - reference to the VRControl
		 * @param existingGroup - an existing landmark group. This can be null if a new group. 
		 * @return a landmark group
		 */
		public static LandMarkGroup showDialog(Frame frame, VRControl vrControl, LandMarkGroup existingGroup) {
//			if (singleInstance == null || frame != singleInstance.getOwner()) {
				singleInstance = new LandMarkGroupDialog(frame, vrControl);
//			}
				
			if (existingGroup != null) {
				singleInstance.setLandMarkList(existingGroup.clone());
			}
			else{
				singleInstance.setLandMarkList(new LandMarkGroup());
			}
			singleInstance.vrControl = vrControl;
			singleInstance.setParams();
//			singleInstance.pack();
			singleInstance.setVisible(true);
			
			return singleInstance.getLandMarkList();
		}

	}
