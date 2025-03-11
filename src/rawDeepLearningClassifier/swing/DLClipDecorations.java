package rawDeepLearningClassifier.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import PamView.dialog.PamGridBagContraints;
import clipgenerator.clipDisplay.ClipDisplayDecorations;
import clipgenerator.clipDisplay.ClipDisplayUnit;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.dlClassification.DLDetection;
import rawDeepLearningClassifier.dlClassification.PredictionResult;

public class DLClipDecorations extends ClipDisplayDecorations {

	private Color normalGrey;
	
	private DLDetection dlDetection;
	
	private JPanel dataPanel;

	private PredictionResult result;

	public DLClipDecorations(DLControl dlControl, ClipDisplayUnit clipDisplayUnit) {
		super(clipDisplayUnit);
		JPanel panel = new JPanel();
		normalGrey = panel.getBackground();
		this.dlDetection = (DLDetection) clipDisplayUnit.getClipDataUnit();
		

		result = dlControl.getDLClassifyProcess().getBestModelResult(dlDetection);
		if (result != null) {
			dataPanel = new JPanel(new BorderLayout());
//			GridBagConstraints c = new PamGridBagContraints();
//			String res = result.getResultString();
			double best = 0;
			int bestInd = -1;
			float[] prediction = result.getPrediction();
			if (prediction != null) {
				for (int i = 0; i < prediction.length; i++) {
					if (prediction[i] > best) {
						best = prediction[i];
						bestInd = i;
					}
				}
				String p = String.format("Class %d, score %3.2f", bestInd, best);
				dataPanel.add(BorderLayout.WEST, new JLabel(p));
			}
		}
	}

	@Override
	public ClipDisplayUnit getClipDisplayUnit() {
		return super.getClipDisplayUnit();
	}

	@Override
	public Color getClipBackground() {
		return Color.darkGray;
	}

	@Override
	public void drawOnClipAxis(Graphics g) {
		super.drawOnClipAxis(g);
	}

	@Override
	public void drawOnClipBorder(Graphics g) {
		super.drawOnClipBorder(g);
	}

	@Override
	public void decorateDisplay() {
		super.decorateDisplay();
		if (dataPanel != null) {
			getClipDisplayUnit().add(dataPanel, BorderLayout.NORTH);
		}
	}

}
