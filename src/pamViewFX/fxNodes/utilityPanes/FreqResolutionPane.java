package pamViewFX.fxNodes.utilityPanes;

import javafx.scene.control.Label;

import pamViewFX.fxNodes.PamGridPane;
import fftManager.FFTDataBlock;

/**
 * Creates a standard panel to display frequency resolution information
 * @author Doug Gillespie and Jamie Macaulay
 *
 */
public class FreqResolutionPane extends PamGridPane {

	
	private Label sampleRate, freqRes, fftLength, timeRes, timeStep;
	
	public FreqResolutionPane() {
		
		this.setHgap(5);
		this.setVgap(5);

		this.add(new Label("Sample Rate "), 0, 0);
		this.add(sampleRate = new Label(), 1, 0);
		this.add(new Label(" Hz"), 2, 0);
		

		this.add(new Label("FFT Length "), 0, 1);
		this.add(fftLength = new Label(), 1, 1);
		this.add(new Label(" bins"), 2, 1);
		
		this.add(new Label("Frequency Resolution "), 0, 2);
		this.add(freqRes = new Label(), 1, 2);
		this.add(new Label(" Hz"), 2, 2);
		
		this.add(new Label("Time Resolution "), 0, 3);
		this.add(timeRes = new Label(), 1, 3);
		this.add(new Label(" ms"), 2, 3);
		
		this.add(new Label("Time Step Size "), 0, 4);
		this.add(timeStep = new Label(), 1, 4);
		this.add(new Label(" ms"), 2, 4);
		

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
}
