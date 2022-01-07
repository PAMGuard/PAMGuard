package dataPlots.layout;

import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.BoxLayout;

import dataPlots.TDControl;
import pamScrollSystem.PamScroller;
import PamView.PamColors.PamColor;
import PamView.panel.PamPanel;

/**
 * Contains a set of split panes or 
 * a box layout for all the actual graphs. 
 * <br>
 * New one gets created each time the layut changes. 
 * @author doug
 *
 */
public class TDGraphContainer {

	private TDControl tdControl;
	
	private PamPanel graphContainer;

	public TDGraphContainer(TDControl tdControl) {
		super();
		this.tdControl = tdControl;
		graphContainer = new PamPanel(PamColor.MAP);
		if (tdControl.getTdParameters().orientation == PamScroller.HORIZONTAL) {
//			graphContainer.setLayout(new BoxLayout(graphContainer, BoxLayout.Y_AXIS));
			graphContainer.setLayout(new GridLayout(0, 1));
		}
		else {
//			graphContainer.setLayout(new GridLayout(tdControl.getTdParameters().nGraphs, 1));
//			graphContainer.setLayout(new BoxLayout(graphContainer, BoxLayout.X_AXIS));
			graphContainer.setLayout(new GridLayout(1,0));
		}
	}
	
	public void addGraph(Component component) {
		graphContainer.add(component);
	}

	/**
	 * @return the graphContainer
	 */
	public PamPanel getGraphContainer() {
		return graphContainer;
	}
	
}
