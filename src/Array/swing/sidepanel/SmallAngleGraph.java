package Array.swing.sidepanel;

import javax.swing.border.TitledBorder;

import PamView.panel.PamPanel;

public class SmallAngleGraph {

	private String paramName;
	
	private double[] range;
	
	private boolean autoRange = false;
	
	private PamPanel mainPanel;

	public SmallAngleGraph(String paramName, double[] range) {
		super();
		this.paramName = paramName;
		this.range = range;
		mainPanel = new PamPanel();
		mainPanel.setBorder(new TitledBorder(paramName));
	}

	/**
	 * @return the mainPanel
	 */
	public PamPanel getMainPanel() {
		return mainPanel;
	}

	/**
	 * @return the autoRange
	 */
	public boolean isAutoRange() {
		return autoRange;
	}

	/**
	 * @param autoRange the autoRange to set
	 */
	public void setAutoRange(boolean autoRange) {
		this.autoRange = autoRange;
	}
}
