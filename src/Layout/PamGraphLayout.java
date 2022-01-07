package Layout;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import PamView.panel.PamBorder;

/**
 * Class to layout PamFramePlots 
 * <p>
 * Theses are sets of components with various axis, etc, all 
 * of which need to be put into a very common layout so that they all look 
 * more or less the same. <p>
 * The functionality in here was all encapsulated within PamInternalFrame
 * but this class allows the same layout within a simple component so that 
 * it can be used within other displays which are not necessarily part of 
 * a JInternalFrame display item.  
 * @author dg50
 *
 */
public class PamGraphLayout {

	private PamFramePlots framePlots;
	
	private JPanel mainComponent;

	/**
	 * axisPanel is the main (outer) panel that fills the entire 
	 * JInternalFrame centre
	 */
	private JPanel axisPanel;

	/**
	 * plotPanel is the inner panel containing various the actual plot,
	 * e.g. a spectrogram, bearing time display, waveform, etc. 
	 */
	private JPanel plotPanel;

	/**
	 * in the case of a dual display, where there are two plot panels beside each
	 * other sharing a common vertical axis, define left and right panel objects
	 * UNTESTED - use at your own risk
	 */
	private JPanel leftPlotPanel, rightPlotPanel;


	protected EmptyBorder emptyBorder;

	private Dimension lastSize = new Dimension();

	/**
	 * @param pamFramePlots
	 */
	public PamGraphLayout(PamFramePlots pamFramePlots) {
		super();
		this.framePlots = pamFramePlots;
		mainComponent = new JPanel(new BorderLayout());

		boolean dualPlots = pamFramePlots.checkDualDisplay();

		if (dualPlots) {
			makeFrame (pamFramePlots.getName(), pamFramePlots.getAxisPanel(),
					pamFramePlots.getLeftPlotPanel(), pamFramePlots.getRightPlotPanel());
		} else {
			makeFrame(pamFramePlots.getName(), pamFramePlots.getAxisPanel(),
					pamFramePlots.getPlotPanel());
		}

//		mainComponent.setSize(900, 400);
//
//		mainComponent.setVisible(true);

//		mainComponent.addComponentListener(this);
	}

	private void makeFrame(String name, Component axisPanel, Component plotPanel) {

		this.axisPanel = (JPanel) axisPanel;

		this.plotPanel = (JPanel) plotPanel;

		emptyBorder = new EmptyBorder(20, 20, 20, 20);

		Component panel;

		if (axisPanel != null) {

			this.axisPanel.setBorder(emptyBorder);

			if (plotPanel != null) {
				this.axisPanel.setLayout(new GridLayout(1, 0));
				//				PamColors.getInstance().registerComponent(plotPanel,
				//						PamColors.PamColor.PlOTWINDOW);
				plotPanel.setSize(100, 100);
				plotPanel.setVisible(true);
				this.plotPanel.setBorder(PamBorder.createInnerBorder());
				this.axisPanel.add(plotPanel);
			}

			if ((panel = framePlots.getNorthPanel()) != null) {
				mainComponent.add(BorderLayout.NORTH, panel);
			}

			if ((panel = framePlots.getSouthPanel()) != null) {
				mainComponent.add(BorderLayout.SOUTH, panel);
			}

			if ((panel = framePlots.getEastPanel()) != null) {
				mainComponent.add(BorderLayout.EAST, panel);
			}

			if ((panel = framePlots.getWestPanel()) != null) {
				mainComponent.add(BorderLayout.WEST, panel);
			}

			//			PamColors.getInstance().registerComponent(axisPanel,
			//					PamColors.PamColor.BORDER);
			axisPanel.setSize(10, 10);
			axisPanel.setVisible(true);

			mainComponent.add(BorderLayout.CENTER, axisPanel);
		}

	}

	/**
	 * Overloaded makeFrame method, to handle dual display (two displays with
	 * a common vertical axis)
	 * UNTESTED - use at your own risk
	 *
	 * @param name
	 * @param axisPanel
	 * @param plotPanel
	 */
	private void makeFrame(String name, Component axisPanel,
			Component leftPlotPanel, Component rightPlotPanel) {

		this.axisPanel = (JPanel) axisPanel;
		this.leftPlotPanel = (JPanel) leftPlotPanel;
		this.rightPlotPanel = (JPanel) rightPlotPanel;

		emptyBorder = new EmptyBorder(20, 20, 20, 20);

		Component panel;

		if (axisPanel != null) {

			this.axisPanel.setBorder(emptyBorder);

			if (leftPlotPanel != null) {
				this.axisPanel.setLayout(new GridLayout(1, 0));
				leftPlotPanel.setSize(100, 100);
				leftPlotPanel.setVisible(true);
				this.leftPlotPanel.setBorder(PamBorder.createInnerBorder());
				this.axisPanel.add(leftPlotPanel);
			}

			if (rightPlotPanel != null) {
				rightPlotPanel.setSize(100, 100);
				rightPlotPanel.setVisible(true);
				this.rightPlotPanel.setBorder(PamBorder.createInnerBorder());
				this.rightPlotPanel.add(rightPlotPanel);
			}
			if ((panel = framePlots.getNorthPanel()) != null) {
				mainComponent.add(BorderLayout.NORTH, panel);
			}

			if ((panel = framePlots.getSouthPanel()) != null) {
				mainComponent.add(BorderLayout.SOUTH, panel);
			}

			if ((panel = framePlots.getEastPanel()) != null) {
				mainComponent.add(BorderLayout.EAST, panel);
			}

			if ((panel = framePlots.getWestPanel()) != null) {
				mainComponent.add(BorderLayout.WEST, panel);
			}

			//			PamColors.getInstance().registerComponent(axisPanel,
			//					PamColors.PamColor.BORDER);
			axisPanel.setSize(10, 10);
			axisPanel.setVisible(true);

			mainComponent.add(BorderLayout.CENTER, axisPanel);
		}

	}


	/**
	 * @return the pamFramePlots
	 */
	public PamFramePlots getPamFramePlots() {
		return framePlots;
	}

	/**
	 * @return the mainComponent
	 */
	public JPanel getMainComponent() {
		return mainComponent;
	}
}
