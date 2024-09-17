package angleVetoes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import Layout.PamAxis;
import PamUtils.PamCalendar;
import PamView.dialog.PamLabel;
import PamView.panel.PamBorderPanel;
import pamMaths.HistogramDisplay;
import pamMaths.HistogramGraphicsLayer;
import pamMaths.PamHistogram;

/**
 * Dispaly for angle veto. Shows a histogram of angles
 * and a pass rate. Plot has a default half life of 10s. 
 * Vetoed areas are shown in pink. 
 * 
 * @author Douglas Gillespie
 * @see AngleVetoes
 *
 */
public class AngleVetoDisplay {
	
	private PamHistogram angleHistogram;
	
	private HistogramDisplay histogramDisplay;
	
	private PamHistogram passHistogram;
	
	private JProgressBar passBar;
	
	private AngleVetoFrame angleVetoFrame;
	
	private AngleVetoes angleVetoes;
	
	private double halfLife = 10;
	
	Timer halfLifeTimer;

	public AngleVetoDisplay(AngleVetoes angleVetoes) {
		super();
		this.angleVetoes = angleVetoes;
		angleHistogram = new PamHistogram(0, 180, 90);
		angleHistogram.setName("Angle Data");
		passHistogram = new PamHistogram(0, 1, 2, true);
		histogramDisplay = new HistogramDisplay(angleHistogram);
		histogramDisplay.getSouthAxis().setAngleScales(true);
		histogramDisplay.setShowStats(false);
		histogramDisplay.setGraphicsUnderLayer(new GraphicsUnderlayer());
		angleVetoFrame = new AngleVetoFrame();
		halfLifeTimer = new Timer(1000, new HalfLifeTimer());
		halfLifeTimer.start();
	}
	
	public void repaint() {
		histogramDisplay.repaint();
	}
	
	public void setVisible(boolean visible) {
		angleVetoFrame.setVisible(visible);
		if (visible) {
			int state = angleVetoFrame.getExtendedState();
			if ((state & Frame.ICONIFIED) != 0) {
				angleVetoFrame.setState(Frame.NORMAL);
			}
			else if ((state & Frame.MAXIMIZED_BOTH) != 0) {
				angleVetoFrame.setState(Frame.NORMAL);				
			}
		}
	}

	class AngleVetoFrame extends JFrame {
		public AngleVetoFrame() {
			AngleVetoPanel angleVetoPanel = new AngleVetoPanel();
			setTitle(angleVetoes.getUnitName() + " Angle Veto");
			setIconImage(null);
			setContentPane(angleVetoPanel);
			pack();
		}
	}
	
	class AngleVetoPanel extends PamBorderPanel {

		public AngleVetoPanel() {
			super();
			setLayout(new BorderLayout());
			JPanel passPanel = new JPanel();
			passPanel.setLayout(new BorderLayout());
			passPanel.add(BorderLayout.WEST, new PamLabel("  Pass rate  "));
			passPanel.add(BorderLayout.CENTER, passBar = new JProgressBar(SwingConstants.HORIZONTAL, 0, 100));
			passBar.setStringPainted(true);
			add(BorderLayout.CENTER, histogramDisplay.getGraphicComponent());
			add(BorderLayout.SOUTH, passPanel);
			setPreferredSize(new Dimension(500,300));
		}
		
	}
	
	/**
	 * Draw the vetoed regions uner the histogram. 
	 * @author Douglas Gillespie
	 *
	 */
	class GraphicsUnderlayer implements HistogramGraphicsLayer {

		Color vetoColor = new Color(255, 215, 215);
		@Override
		public void paintLayer(Graphics g) {
			Rectangle r = g.getClipBounds();
			PamAxis southAxis = histogramDisplay.getSouthAxis();
			AngleVetoParameters params = angleVetoes.getAngleVetoParameters();
			int n = params.getVetoCount();
			AngleVeto angleVeto;
			for (int i = 0; i < n; i++) {
				angleVeto = params.getVeto(i);
				r.x = (int) southAxis.getPosition(angleVeto.startAngle);
				r.width = (int)  southAxis.getPosition(angleVeto.endAngle) - r.x;
				g.setColor(vetoColor);
//				g.setPaintMode();
				g.fillRect(r.x, r.y, r.width, r.height);
			}
		}
		
	}
	
	public void newAngle(double angle) {
		angleHistogram.addData(angle, true);
	}
	
	public void newPassData(boolean pass) {
		passHistogram.addData(pass ? 1 : 0, true);
	}
	
	class HalfLifeTimer implements ActionListener {
		private boolean first = true;
		private long lastTime = 0;
		@Override
		public void actionPerformed(ActionEvent e) {
			long now = PamCalendar.getTimeInMillis();
			long delay = now - lastTime;
			if (first || delay < 0) {
				delay = 1000;
				first = true;
			}
			lastTime = now;
				
			double f = Math.pow(0.5, delay / 1000. / halfLife);
			angleHistogram.scaleData(f);
			passHistogram.scaleData(f);
			sayPassData();
		}
	}
	
	private void sayPassData() {
		double nTot = passHistogram.getTotalContent();
		double nPass = passHistogram.getData()[1];
		if (nTot > 0) {
			passBar.setValue((int) (100 * nPass / nTot));
		}
	}

	public double getHalfLife() {
		return halfLife;
	}

	public void setHalfLife(double halfLife) {
		this.halfLife = halfLife;
	}
	
	
//	class AngleVetoHistogramDisplay extends HistogramDisplay {
//		
//	}
}
