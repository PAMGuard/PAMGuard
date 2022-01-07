package targetMotionModule.panels;

import java.awt.BorderLayout;

import javax.swing.JLayer;
import javax.swing.JPanel;

import PamView.PamWaitAnimation;
import PamView.panel.PamPanel;
//import staticLocaliser.panels.AbstractLocaliserControl;
import targetMotionModule.TargetMotionControl;

public abstract class AbstractControlPanel implements TMDialogComponent, TargetMotionControlPanel {
	
	TargetMotionControl targetMotionControl;
	TargetMotionMainPanel<?> targetMotionMainPanel;
	JLayer<JPanel> jlayer;
	PamWaitAnimation layerUI;
	
	/**
	 * Panel which contains the control panel and a hidden load bar. 
	 */
	private PamPanel mainPanelHolder;

	
	public AbstractControlPanel(TargetMotionControl targetMotionControl){
		this.targetMotionControl=targetMotionControl;
		this.targetMotionMainPanel=targetMotionControl.getTargetMotionMainPanel();
	}
	
	
	public PamPanel createMainPanel(){
		PamPanel panel= new PamPanel(new BorderLayout());
		panel.add(BorderLayout.CENTER,getLayerPanel());
		return panel;
	}
	
	public PamPanel getMainPanel(){
		if (mainPanelHolder==null) mainPanelHolder=createMainPanel();
		return mainPanelHolder;
	}
	
	
	/**
	 * This return the panel with a JLayer overlay. This overlay is used for disabling the panel during batch run. Note that this is a new feature of Java 7. 
	 * @return
	 */
	public JLayer<JPanel> getLayerPanel(){
		
		layerUI = new PamWaitAnimation();
		jlayer = new JLayer<JPanel>(getPanel(), layerUI);

		return jlayer;
		
	}
	
	/**
	 * Enable or disable the panel. Panel enabled shows the normal panel with all components enabled. Disabling the panel adds an animation over the top of the panel, greys out the rest of the panel and disables all components. 
	 * @param enable
	 */
	public void setlayerPanelEnabled(boolean enable){
//		System.out.println(enable);
//		if (enable){
//			((PamWaitAnimation) layerUI).stop();
//		}
//		if (!enable){
//			((PamWaitAnimation) layerUI).start();
//		}
		
//		AbstractLocaliserControl.enableComponents(enable, getPanel());

	}
	
	/**
	 * Notify tmControl that an update has occured. 
	 * @param flag
	 */
	@SuppressWarnings("unused")
	private void notifyUpdate(int flag) {
		targetMotionControl.update(flag);
	}

	@Override
	public void update(int flag) {
		
//	switch (flag){
//		
//		case TargetMotionControl.DETECTION_INFO_CALC_START:
//			setlayerPanelEnabled(false);
//			break ;
//		case TargetMotionControl.DETECTION_INFO_CALC_END:
//			setlayerPanelEnabled(true);
//		}
//		
	}
	

}
