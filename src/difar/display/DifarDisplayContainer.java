package difar.display;

import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import difar.DifarControl;
import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayComponentAdapter;
import PamController.PamControllerInterface;
import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamView.panel.PamPanel;

/**
 * This is a main panel which will hold as many other panels as 
 * we need for the DIFAR display system. In principle, this can be included 
 * in either a panel for a user display or could very quickly adapt to go in 
 * it's own tab panel. 
 * Depending on the state of DifarControl.SPLITDISPLAYS it may contain everything, 
 * or just the difargram part.
 * @author dg50
 *
 */
public class DifarDisplayContainer extends UserDisplayComponentAdapter {



	private PamPanel outerDisplayPanel;

	private DifarControl difarControl;

	private JSplitPane horizSplitPane;

	/**
	 * 
	 * @param difarControl
	 */
	public DifarDisplayContainer(DifarControl difarControl) {
		super();
		this.difarControl = difarControl;

		outerDisplayPanel = new PamPanel(PamColor.BORDER);
		outerDisplayPanel.setLayout(new BorderLayout());

		boolean split = DifarControl.SPLITDISPLAYS;
		if (split)  {
			outerDisplayPanel.add(BorderLayout.CENTER, new DisplaySouthPanel(difarControl));
		}
		else{
			horizSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			horizSplitPane.addPropertyChangeListener(new SplitPaneListener());
			Integer pos = difarControl.getDifarParameters().horizontalDividerPos;
			if (pos != null) {
				horizSplitPane.setDividerLocation(pos);
			}
			else {
				horizSplitPane.setResizeWeight(0.5);
			}
			horizSplitPane.add(new DisplayNorthPanel(difarControl));
			horizSplitPane.add(new DisplaySouthPanel(difarControl));

			outerDisplayPanel.add(BorderLayout.CENTER, horizSplitPane);
		}

		//		if (difarControl.isViewer() == false) {
		//			outerDisplayPanel.add(BorderLayout.NORTH, difarControl.getInternalActionsPanel().getComponent());
		//		}
		//		
		//		horizSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		//		horizSplitPane.addPropertyChangeListener(new SplitPaneListener());
		//		horizSplitPane.add(difarControl.getDifarQueue().getComponent());
		//		Integer pos = difarControl.getDifarParameters().horizontalDividerPos;
		//		if (pos != null) {
		//			horizSplitPane.setDividerLocation(pos);
		//		}
		//		else {
		//			horizSplitPane.setResizeWeight(0.5);
		//		}
		//		
		//		JPanel lowerPanel = new JPanel(new BorderLayout());
		//		lowerPanel.add(BorderLayout.NORTH, difarControl.getDemuxProgressDisplay().getComponent());
		//		lowerPanel.add(BorderLayout.CENTER, difarControl.getDifarGram().getComponent());
		//		horizSplitPane.add(lowerPanel);
		//		
		//		
		//		outerDisplayPanel.add(BorderLayout.CENTER, horizSplitPane);



		//		outerDisplayPanel.add(BorderLayout.CENTER, difarControl.getDifarGram().getComponent());
	}

	@Override
	public Component getComponent() {
		return outerDisplayPanel;
	}

	@Override
	public void openComponent() {
		// TODO Auto-generated method stub

	}

	@Override
	public void closeComponent() {
		// TODO Auto-generated method stub

	}

	class SplitPaneListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent arg0) {
			difarControl.getDifarParameters().horizontalDividerPos = horizSplitPane.getDividerLocation();
		}
	}

	@Override
	public void notifyModelChanged(int changeType) {
		difarControl.getDifarQueue().getClipDisplayPanel().notifyModelChanged(changeType);
	}

	@Override
	public String getFrameTitle() {
		return difarControl.getUnitName();
	}

}
