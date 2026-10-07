package difar.display;

import java.awt.BorderLayout;
import java.awt.Component;

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
			/*
			 * Three panels: the clip strip, the actions and grams, and the match
			 * selector, by default 40%, 32% and 28% of the height. The selector
			 * sits below the grams so that it is clear the Save button applies to
			 * the match shown there. Where the operator leaves each divider is
			 * remembered, as a fraction, from run to run.
			 */
			JSplitPane lowerSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			lowerSplitPane.add(new DisplaySouthPanel(difarControl));
			lowerSplitPane.add(difarControl.getMatchContainer().getMatchPanel());
			SplitPaneMemory.apply(lowerSplitPane, DEFAULT_GRAM_SHARE / (DEFAULT_GRAM_SHARE + DEFAULT_MATCH_SHARE),
					() -> difarControl.getDifarParameters().gramDividerFraction,
					f -> difarControl.getDifarParameters().gramDividerFraction = f);

			horizSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			SplitPaneMemory.apply(horizSplitPane, DEFAULT_STRIP_SHARE,
					() -> difarControl.getDifarParameters().queueDividerFraction,
					f -> difarControl.getDifarParameters().queueDividerFraction = f);
			horizSplitPane.add(new DisplayNorthPanel(difarControl));
			horizSplitPane.add(lowerSplitPane);

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

	/** Default share of the display's height for the clip strip. */
	private static final double DEFAULT_STRIP_SHARE = 0.40;

	/** Default share for the actions and grams. */
	private static final double DEFAULT_GRAM_SHARE = 0.32;

	/** Default share for the match selector. */
	private static final double DEFAULT_MATCH_SHARE = 0.28;

	@Override
	public void notifyModelChanged(int changeType) {
		difarControl.getDifarQueue().getClipDisplayPanel().notifyModelChanged(changeType);
	}

	@Override
	public String getFrameTitle() {
		return difarControl.getUnitName();
	}

}
