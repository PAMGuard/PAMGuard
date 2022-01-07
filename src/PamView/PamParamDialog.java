///**
// * 
// */
//package PamView;
//
//import java.awt.BorderLayout;
//import java.awt.Frame;
//import java.awt.GridBagConstraints;
//
//import javax.swing.JButton;
//import javax.swing.JPanel;
//import javax.swing.JTabbedPane;
//import javax.swing.border.EmptyBorder;
//
//import PamController.PamControlledUnit;
//
//
///**
// * @author gw
// *
// */
//public class PamParamDialog  extends PamDialog {
//
//		private PamControlledUnit control;
//		private static PamParamDialog singleInstance;
//		
//		private PamParameters params;
//		
//		private JButton okButton, cancelButton;
//		
////		public static void main(String[] args){
////			showDialog(null, null, new DifarParameters());
////			
////		}
//
//		private PamParamDialog(PamControlledUnit control) {
//			
//			super(control.getGuiFrame(),"  ", true);
//
////			this.difarControl = difarControl;
//
//			JPanel panel = new JPanel();
//			panel.setBorder(new EmptyBorder(5,5,5,5));
//			JTabbedPane tabbedPane = new JTabbedPane();
//			panel.setLayout(new BorderLayout());
//			panel.add(BorderLayout.CENTER, tabbedPane);
//
////			TODO add stuff here
//			tabbedPane.add(exeDemuxPanel = new ExeDemuxPanel());
//			
//			GridBagConstraints gc = new GridBagConstraints();
//			JPanel generalPanel = new PamPanel();
//			generalPanel.setName("General");
//
//			addComponent(generalPanel, sourcePanel, gc);
//			addComponent(generalPanel, vesselPanel, gc);
//			
//			tabbedPane.add(generalPanel);
//
//			setDialogComponent(panel);
//			setResizable(true);
//			pack();
//
//			//*************************************************************************
//			//Add these lines to enable context sensitive help at the specified target
//			
////			TODO set correct help point
//			
//			this.setHelpPoint("detectors.clickDetectorHelp.docs.ClickDetector_clickDetector");
////			this.enableHelpButton(true);
//			//*************************************************************************
//
//		}
//		
//
//		public void setParams(PamParameters params) {
//			params
//		}
//		
//
//		@Override
//		public boolean getParams() {
//			
//			return false;
//		}
//
//		public static PamParameters showDialog(Frame parentFrame, PamControlledUnit control, PamParameters params) {
//
//			if (singleInstance == null || singleInstance.getOwner() != parentFrame) {
//				singleInstance = new PamParamDialog(parentFrame, difarControl);
//			}
//			singleInstance.params = params.clone();
//			singleInstance.setParams(params);
//			singleInstance.setVisible(true);
//			return singleInstance.params;
//		}
//
//		@Override
//		public void cancelButtonPressed() {
//			params = null;
//		}
//		
//		@Override
//		public void restoreDefaultSettings() {
//			DifarParameters defaltParameters = new DifarParameters();
//			setParams(defaltParameters);		
//		}
//}
