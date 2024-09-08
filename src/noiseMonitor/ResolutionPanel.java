package noiseMonitor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import fftManager.FFTDataBlock;

/**
 * Creates a standard panel to display frequency resolution information
 * @author Doug Gillespie
 *
 */
public class ResolutionPanel {

	private JPanel res;
	
	private JLabel sampleRate, freqRes, fftLength, timeRes, timeStep;
	
	public ResolutionPanel() {

		res = new JPanel();
		res.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		PamDialog.addComponent(res, new JLabel("Sample Rate "), c);
		c.gridx++;
		PamDialog.addComponent(res, sampleRate = new JLabel(), c);
		c.gridx++;
		PamDialog.addComponent(res, new JLabel(" Hz"), c);
		c.gridy++;
		c.gridx = 0;
		PamDialog.addComponent(res, new JLabel("FFT Length "), c);
		c.gridx++;
		PamDialog.addComponent(res, fftLength = new JLabel(), c);
		c.gridx++;
		PamDialog.addComponent(res, new JLabel(" bins"), c);
		c.gridy++;
		c.gridx = 0;
		PamDialog.addComponent(res, new JLabel("Frequency Resolution "), c);
		c.gridx++;
		PamDialog.addComponent(res, freqRes = new JLabel(), c);
		c.gridx++;
		PamDialog.addComponent(res, new JLabel(" Hz"), c);
		c.gridy++;
		c.gridx = 0;
		PamDialog.addComponent(res, new JLabel("Time Resolution "), c);
		c.gridx++;
		PamDialog.addComponent(res, timeRes = new JLabel(), c);
		c.gridx++;
		PamDialog.addComponent(res, new JLabel(" ms"), c);
		c.gridy++;
		c.gridx = 0;
		PamDialog.addComponent(res, new JLabel("Time Step Size "), c);
		c.gridx++;
		PamDialog.addComponent(res, timeStep = new JLabel(), c);
		c.gridx++;
		PamDialog.addComponent(res, new JLabel(" ms"), c);

	}
	
	public JPanel getPanel() {
		return res;
	}
	
	public void setParams(FFTDataBlock sourceData) {

		if (sourceData == null) {
			freqRes.setText(" N/A ");
			timeRes.setText(" N/A ");
			timeStep.setText(" N/A ");
			fftLength.setText(" N/A ");
			sampleRate.setText(" N/A ");
		}
		else {
			setParams(sourceData.getSampleRate(), sourceData.getFftLength(),
					sourceData.getFftHop());
//			double fr = sourceData.getSampleRate() / sourceData.getFftLength();
//			freqRes.setText(String.format("%.2f ", fr));
//			double tr = sourceData.getFftLength() / sourceData.getSampleRate() * 1000;
//			timeRes.setText(String.format("%.1f ", tr));
//			fftLength.setText(String.format("%d", sourceData.getFftLength()));
//			sampleRate.setText(String.format("%d", (int) sourceData.getSampleRate()));
		}
	}
	
	public void setParams(float sampleRate, int fftLength, int fftHop) {
		double fr = sampleRate / (double) fftLength;
		freqRes.setText(String.format("%.2f", fr));
		double tr = fftLength / sampleRate * 1000;
		timeRes.setText(String.format("%.2f ", tr));
		double ts = fftHop / sampleRate * 1000;
		timeStep.setText(String.format("%.2f ", ts));
		this.fftLength.setText(String.format("%d", fftLength));
		this.sampleRate.setText(String.format("%d", (int) sampleRate));
	}

	public double getFreqRes() {
		return Double.valueOf(freqRes.getText());	}
}
