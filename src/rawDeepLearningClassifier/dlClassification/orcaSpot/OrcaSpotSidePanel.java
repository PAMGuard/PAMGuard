package rawDeepLearningClassifier.dlClassification.orcaSpot;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import PamUtils.PamUtils;
import PamView.panel.PamPanel;
import clickTrainDetector.clickTrainAlgorithms.mht.MHTClickTrainAlgorithm;
import clickTrainDetector.clickTrainAlgorithms.mht.MHTGarbageBot;
import clickTrainDetector.clickTrainAlgorithms.mht.MHTClickTrainAlgorithm.MHTAlgorithm;
import clickTrainDetector.layout.mht.MHTSidePaneSwing.BufferPanel;

/**
 * Side panel showing how many OrcaSpot detections are in the buffer and the last values of the detections. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class OrcaSpotSidePanel extends PamPanel {
	
	/**
	 * The main panel. 
	 */
	private PamPanel mainPanel;
	
	/**
	 * Reference to the MHT algorithm 
	 */
	private OrcaSpotClassifier orcaSpotClassifier;

	private BufferPanel bufferBarPanel;

	public OrcaSpotSidePanel(OrcaSpotClassifier orcaSpotClassifier) {
		this.orcaSpotClassifier=orcaSpotClassifier; 
		createSidePane(); 
		updateNewParams(); 
		this.setLayout(new BorderLayout());
		this.add(mainPanel, BorderLayout.CENTER);
	}

	/**
	 * Update the parameters on the side panel. 
	 */
	public void updateNewParams() {
		double lastPrediction = -1;
		String lastPredString = ""; 
		if (orcaSpotClassifier.getLastPrediction()!=null) {
			lastPrediction = orcaSpotClassifier.getLastPrediction().getPrediction()[0];
			lastPredString = orcaSpotClassifier.getLastPrediction().getResultString(); 
		}
		bufferBarPanel.updatePanel(orcaSpotClassifier.getRawDataQueue(), orcaSpotClassifier.MAX_QUEUE_SIZE, 
				lastPrediction, lastPredString);
	}

	private void createSidePane() {
		mainPanel = new PamPanel(new BorderLayout()); 
		 
		mainPanel.setBorder(new TitledBorder("OrcaSpot Classifier")); 
		
		bufferBarPanel =  new BufferPanel(); 
		
		mainPanel.add(bufferBarPanel, BorderLayout.WEST); 
	}


	/**
	 * Buffer panel. 
	 * 
	 * @author Jamie Macaulay
	 *
	 */
	public class BufferPanel extends PamPanel {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;


		/**
		 * A progress bar showing buffer
		 */
		private JProgressBar bufferBar;

		/**
		 * Label showing % full. 
		 */
		private JLabel bufferLabel;

		private JLabel posLabel;

		/**
		 * Shows results from results label. 
		 */
		private JLabel resultLabel;

		public BufferPanel() {
			
			this.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints(); 
					
			c.gridx=0;
			c.gridy=0; 
			
			c.gridy++;
			PamPanel.addComponent(this, new JLabel("Buffer"), c);
			c.gridx=1; 
			PamPanel.addComponent(this, bufferLabel = new JLabel(), c);
			
			c.gridx=0;
			c.gridwidth=2; 
			c.gridy++;
			PamPanel.addComponent(this, bufferBar = new JProgressBar(), c);

			
			c.gridy++;
			c.gridx=0; 
			PamPanel.addComponent(this, posLabel = new JLabel(), c);
			
			c.gridy++;
			c.gridx=0; 
			PamPanel.addComponent(this, resultLabel = new JLabel(), c);


		}
		
		/**
		 * Update the panel. 
		 * @param numinbuffer - the kcount
		 * @param buffermax - the maximum allowed value of the buffer. 
		 * @param lastPrediciton - the current number of possibilities.  
		 */
		public void updatePanel(int numinbuffer, int buffermax, double lastPrediciton, String predicitonString) {
			//System.out.println("Update buffer: " + kcount + " buffermax: " +buffermax +  "   " + (kcount/(double) buffermax) + "%" ); 
			SwingUtilities.invokeLater(()->{
				bufferBar.setValue(numinbuffer);
				bufferBar.setMaximum(buffermax);
				bufferLabel.setText(String.format("%.1f%%", 100*numinbuffer/(double) buffermax));
				posLabel.setText(String.format("Last prediction: %.1f ",lastPrediciton));
				
				resultLabel.setText(	"<html><pre>" + 	predicitonString + "</pre></html>");
			});
		}
		
	}




	
	
}
