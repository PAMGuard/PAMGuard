package PamView;

import java.awt.Component;
import java.awt.Frame;

import javax.swing.JFrame;
import javax.swing.JMenuItem;

import PamController.PamControlledUnit;

/**
 * Wrapper class which returns  Swing GUI functions from the soon to be 
 * deprecated functions within the PamControlledUnit class. 
 * <p>
 * This is used to allow developers to call GUI functions from the PAMControlled
 * unit using getPamGui(Swing) without requiring any alterations to the 
 * PamControlledUnit. 
 * 
 * @author Jamie Macaulay
 *
 */
public class WrapperControlledGUISwing extends PamControlledGUISwing {

	/**
	 * Reference to the PamControlledUnit. 
	 */
	private PamControlledUnit pamControlledUnit;

	public WrapperControlledGUISwing(PamControlledUnit pamControlledUnit) {
		this.pamControlledUnit=pamControlledUnit; 

	}
	@Override
	public PamTabPanel getTabPanel() {
		return pamControlledUnit.getTabPanel();
	}

	@Override

	public void setSidePanel(PamSidePanel sidePanel) {
		pamControlledUnit.setSidePanel(sidePanel);
	}

	@Override

	public void setTabPanel(PamTabPanel tabPanel) {
		pamControlledUnit.setTabPanel(tabPanel);
	}

	@Override

	public int getFrameNumber() {
		return pamControlledUnit.getFrameNumber();
	}

	@Override
	public void setFrameNumber(int frameNumber) {
		pamControlledUnit.setFrameNumber(frameNumber);
	}

	@Override

	public PamSidePanel getSidePanel() {
		return pamControlledUnit.getSidePanel();
	}

	@Override
	public Component getToolbarComponent() {
		return pamControlledUnit.getToolbarComponent();
	}

	@Override
	protected void setToolbarComponent(Component toolbarComponent) {
		pamControlledUnit.setToolbarComponent(toolbarComponent);
	}

	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		return pamControlledUnit.createDetectionMenu(parentFrame);
	}

	@Override
	public JMenuItem createDisplayMenu(Frame parentFrame) {
		return pamControlledUnit.createDisplayMenu(parentFrame);
	}

	@Override
	public JMenuItem createHelpMenu(Frame parentFrame) {
		return pamControlledUnit.createHelpMenu(parentFrame);
	}

	@Override
	public JMenuItem createFileMenu(JFrame parentFrame) {
		return pamControlledUnit.createFileMenu(parentFrame);
	}

	@Override
	public PamView getPamView() {
		return pamControlledUnit.getPamView();
	}

	@Override
	public ClipboardCopier getTabClipCopier() {
		return pamControlledUnit.getTabClipCopier();
	}

	@Override
	public Frame getGuiFrame() {
		return pamControlledUnit.getGuiFrame();
	}


}
