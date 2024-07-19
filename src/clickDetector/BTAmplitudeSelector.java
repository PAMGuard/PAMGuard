package clickDetector;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ListIterator;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.dialog.PamDialog;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;
import clickDetector.dataSelector.ClickDataSelector;
import pamMaths.HistogramDisplay;
import pamMaths.HistogramGraphicsLayer;
import pamMaths.PamHistogram;

/*
 * Some kind of a histogram type thing to show a histogram of click amplitudes and 
 * allow the user to drag a selector bar which will set a minimum level on the 
 * BT display below which clicks will not be shown. 
 * <p>
 * The basic selector is in the form of a histogram panel which can be inserted into 
 * a variety of frames. 
 * <p>
 * Will histogram all clicks and also by species type. 
 */
public class BTAmplitudeSelector implements PamDialogPanel {

	private static final int NBINS = 80;
	private PamHistogram allHistogram;
	private PamHistogram[] speciesHistograms;
	private ClickBTDisplay btDisplay;
	private ClickControl clickControl;
	private ClickDataBlock clickDataBlock;
	private HistogramDisplay histoPlot;
	private AmpCtrlPanel ampCtrlPanel;
	private HistoOverLayer histoOverLayer;
	private JPanel plotPanel;

	private JCheckBox amplitudeSelect;
	private JTextField minAmplitude;
	private JFrame ownerFrame;
	private ClickDataSelector dataSelector;
	/**
	 * @param btDisplay
	 */
	public BTAmplitudeSelector(ClickControl clickControl, ClickBTDisplay btDisplay) {
		super();
		this.clickControl = clickControl;
		this.btDisplay = btDisplay;
		dataSelector = btDisplay.getDataSelector();
		clickDataBlock = clickControl.getClickDataBlock();
		histoPlot = new HistogramDisplay();
		histoPlot.setGraphicsOverLayer(histoOverLayer = new HistoOverLayer());
		HistogramMouse hm = new HistogramMouse();
		histoPlot.getHistoPlotPanel().addMouseMotionListener(hm);
		histoPlot.getHistoPlotPanel().addMouseListener(hm);
		createHistograms();
		ampCtrlPanel = new AmpCtrlPanel();
		plotPanel = new JPanel(new BorderLayout());
		JPanel c = new JPanel(new BorderLayout());
		c.setBorder(new TitledBorder("Amplitude histogram"));
		c.add(BorderLayout.CENTER, histoPlot.getGraphicComponent());
		plotPanel.add(BorderLayout.CENTER, c);
		plotPanel.add(BorderLayout.SOUTH, ampCtrlPanel.getPanel());
		BTDisplayParameters btDisplayParameters = btDisplay.getBtDisplayParameters();
		ampCtrlPanel.setParams(btDisplayParameters);
	}
	
	/**
	 * force the histogram to repaint. 
	 */
	void redrawHisto() {
		histoPlot.update(allHistogram, null);
	}
	
	public void createHistograms() {
		BTDisplayParameters btDisplayParameters = btDisplay.getBtDisplayParameters();
		double[] ampRange = btDisplayParameters.amplitudeRange;
		if (allHistogram == null) {
			allHistogram = new PamHistogram(ampRange[0], ampRange[1], NBINS);
			histoPlot.addHistogram(allHistogram);
		}
		allHistogram.clear();
		allHistogram.setRange(ampRange[0], ampRange[1], NBINS);
		ListIterator<ClickDetection> li = clickDataBlock.getListIterator(0);
		ClickDetection click;
		while (li.hasNext()) {
			 click = li.next();
			 allHistogram.addData(click.getAmplitudeDB());
		}
		histoPlot.update(allHistogram, null);
	}
	
	class HistoOverLayer implements HistogramGraphicsLayer {
		private static final int SYMSIZE = 9;
		PamSymbol upT = new PamSymbol(PamSymbolType.SYMBOL_TRIANGLEU, SYMSIZE, SYMSIZE, true, Color.RED, Color.RED);
		PamSymbol dnT = new PamSymbol(PamSymbolType.SYMBOL_TRIANGLED, SYMSIZE, SYMSIZE, true, Color.RED, Color.RED);
		private double axMin;
		private double axMax;
		private Rectangle plotRectangle;
		@Override
		public void paintLayer(Graphics g) {
			BTDisplayParameters btDisplayParameters = btDisplay.getBtDisplayParameters();
			if (btDisplayParameters.amplitudeSelect == false) {
				return;
			}
			axMin = allHistogram.getScaleMinVal();
			axMax = allHistogram.getScaleMaxVal();
			plotRectangle = g.getClipBounds();
//			double dx = (btDisplayParameters.minAmplitude - axMin) / (axMax-axMin) * plotRectangle.width;
			double dx = (dataSelector.getParams().minimumAmplitude - axMin) / (axMax-axMin) * plotRectangle.width;
			int x = (int) Math.round(dx);
			g.setColor(Color.RED);
			g.drawLine(x, 0, x, plotRectangle.height);
			int oSet = SYMSIZE/2;
			dnT.draw(g,new Point(x, oSet));
			upT.draw(g,new Point(x, plotRectangle.height-oSet));
		}
	}
	
//	public class HistoPlot extends HistogramDisplay {
//		
//	}
	
	class HistogramMouse extends MouseAdapter {

		private boolean canDrag = false;
		@Override
		public void mouseDragged(MouseEvent mouseEvent) {
			if (canDrag == false) {
				return;
			}
			// work out a new value based on the current mouse position
			double newAmp = (double) mouseEvent.getX() / histoOverLayer.plotRectangle.width * 
			(histoOverLayer.axMax-histoOverLayer.axMin) + histoOverLayer.axMin;
			BTDisplayParameters btDisplayParameters = btDisplay.getBtDisplayParameters();
			dataSelector.getParams().minimumAmplitude = newAmp;
			ampCtrlPanel.setParams(btDisplayParameters);
			redrawHisto();
			//
//			System.out.println("Amplitude Selector");
			btDisplay.amplitudeSelectChanged();
			
		}

		@Override
		public void mousePressed(MouseEvent e) {
			canDrag = isCanDrag(e);
		}

		private boolean isCanDrag(MouseEvent e) {
			if (histoOverLayer == null || histoOverLayer.plotRectangle == null) {
				return false;
			}
			BTDisplayParameters btDisplayParameters = btDisplay.getBtDisplayParameters();
			if (btDisplayParameters.amplitudeSelect == false) {
				return false;
			}
			double dx = (dataSelector.getParams().minimumAmplitude - histoOverLayer.axMin) / 
			(histoOverLayer.axMax-histoOverLayer.axMin) * histoOverLayer.plotRectangle.width;
			int x = (int) Math.round(dx);
			if (Math.abs(e.getX()-x) > 10) {
				return false;
			}
			
			return true;
		}
	}
	
	class AmpCtrlPanel {
		private JPanel panel;
		public AmpCtrlPanel() {
			panel = new JPanel();
			panel.setBorder(new TitledBorder("Amplitude options"));
			panel.setLayout(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();
			c.gridwidth = 3;
			PamDialog.addComponent(panel, amplitudeSelect = new JCheckBox("Display only clicks over set amplitude"), c);
			c.gridx = 0;
			c.gridy++;
			c.gridwidth = 1;
			PamDialog.addComponent(panel, new JLabel("Min amplitude ", SwingConstants.RIGHT), c);
			c.gridx++;
			PamDialog.addComponent(panel, minAmplitude = new JTextField(7), c);
			c.gridx++;
			PamDialog.addComponent(panel, new JLabel(" dB"), c);
			
			amplitudeSelect.addActionListener(new AmplitudeSelect());
			minAmplitude.addActionListener(new AmplitudeListener());
		}
		/**
		 * @return the panel
		 */
		public JPanel getPanel() {
			return panel;
		}
		private void setParams(BTDisplayParameters btParams) {
			amplitudeSelect.setSelected(btParams.amplitudeSelect);
			minAmplitude.setText(String.format("%3.1f", dataSelector.getParams().minimumAmplitude));

			enableControls();
		}
		private boolean getParams(BTDisplayParameters btParams) {
			btParams.amplitudeSelect = amplitudeSelect.isSelected();
			try {
				dataSelector.getParams().minimumAmplitude = Double.valueOf(minAmplitude.getText());
			}
			catch (NumberFormatException e) {
				return false;
			}
			return true;
		}
	}
	
	class AmplitudeSelect implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			updateBTParams();
			enableControls();
			redrawHisto();
		}
	}
	class AmplitudeListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
//			System.out.println("Amplitude Listener activated: " + arg0.toString());
			updateBTParams();
			redrawHisto();
		}
	}
	
	private void enableControls() {
		minAmplitude.setEnabled(amplitudeSelect.isSelected());
	}
	
	/**
	 * update btdisplay params from amplitude bt params
	 */
	public void updateBTParams() {
		BTDisplayParameters btDisplayParameters = btDisplay.getBtDisplayParameters();
		if (ampCtrlPanel.getParams(btDisplayParameters)) {
			redrawHisto();
			btDisplay.amplitudeSelectChanged();
		}
	}

	/**
	 * Get the bit of the histogram panel to plot. 
	 * @return
	 */
	public JComponent getPlotPanel() {
		return plotPanel;
	}
	
	public static BTAmplitudeSelector showAmplitudeFrame(ClickControl clickControl, ClickBTDisplay btDisplay) {
		BTAmplitudeSelector btas = new BTAmplitudeSelector(clickControl, btDisplay);
		JFrame frame = new JFrame("BT Amplitude Selector");
		frame.setLayout(new BorderLayout());
		frame.add(BorderLayout.CENTER, btas.getPlotPanel());
		frame.setMinimumSize(new Dimension(300,400));
		btas.setFrame(frame);
		frame.setVisible(true);
//		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		return btas;
	}

	private void setFrame(JFrame frame) {
		this.ownerFrame = frame;
	}
	
	public JFrame getFrame() {
		return ownerFrame;
	}

	@Override
	public JComponent getDialogComponent() {
		return getPlotPanel();
	}

	@Override
	public boolean getParams() {
		return true;
	}

	@Override
	public void setParams() {
		
	}
}
