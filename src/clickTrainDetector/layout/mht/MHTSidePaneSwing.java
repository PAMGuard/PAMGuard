package clickTrainDetector.layout.mht;

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
import clickTrainDetector.clickTrainAlgorithms.mht.MHTClickTrainAlgorithm.MHTAlgorithm;
import clickTrainDetector.clickTrainAlgorithms.mht.MHTGarbageBot;

/**
 * Swing version of the side pane. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class MHTSidePaneSwing {
	
	/**
	 * The main panel. 
	 */
	private PamPanel mainPanel;
	
	/**
	 * Reference to the MHT algorithm 
	 */
	private MHTClickTrainAlgorithm mhtClickTrainAlgoirthm;

	private PamPanel bufferBarPanel;

	private ArrayList<BufferPanel> bufferPanels;


	public MHTSidePaneSwing(MHTClickTrainAlgorithm mhtClickTrainAlgoirthm) {
		this.mhtClickTrainAlgoirthm=mhtClickTrainAlgoirthm; 
		createSidePane(); 
		updateNewParams(); 
	}

	
	private void createSidePane() {
		mainPanel = new PamPanel(new BorderLayout()); 
		 
		mainPanel.setBorder(new TitledBorder("Click Train  Detector")); 
		
		bufferBarPanel = new PamPanel(new GridBagLayout()); 
	
		mainPanel.add(bufferBarPanel, BorderLayout.WEST); 
	}
	
	/**
	 * Update when there are new params
	 */
	public void updateNewParams() {
		createBufferPanels(); 
	}
	
	
	private void createBufferPanels() {
		bufferBarPanel.removeAll();
		
		ArrayList<MHTAlgorithm> mhtAlgorithms = mhtClickTrainAlgoirthm.getMHTAlgorithms(); 
		GridBagConstraints c = new GridBagConstraints(); 
		BufferPanel bufferPanel;
		bufferPanels = new 	ArrayList<BufferPanel>(); 
		for (int i=0; i<mhtAlgorithms.size();  i++) {
			PamPanel.addComponent(bufferBarPanel, bufferPanel = new BufferPanel(mhtAlgorithms.get(i).getChannelBitMap()),  c);
			bufferPanels.add(bufferPanel); 
			c.gridy++;
		}
		bufferBarPanel.validate();
	}


	public Component getComponent() {
		return mainPanel;
	}

	/**
	 * Update the buffer panes. 
	 */
	public void updateBuffer() {
		ArrayList<MHTAlgorithm> mhtAlgorithms = mhtClickTrainAlgoirthm.getMHTAlgorithms(); 	
		for (int i=0; i<mhtAlgorithms.size(); i++) {
			//find the correct buffer panel - should be in same order; 
			bufferPanels.get(i).updatePanel(mhtAlgorithms.get(i).getMHTKernal().getKCount(), 
					MHTGarbageBot.DETECTION_HARD_LIMIT, mhtAlgorithms.get(i).getMHTKernal().getPossibilityCount()); 
		}
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
		 * The channel a label
		 */
		private JLabel channelLabel;
		
		/**
		 * A progress bar showing buffer
		 */
		private JProgressBar bufferBar;

		/**
		 * The channel bitmap this represents. 
		 */
		private int channels;
		
		/**
		 * Label showing % full. 
		 */
		private JLabel bufferLabel;

		private JLabel posLabel;

		public BufferPanel(int channels) {
			this.channels=channels; 
			
			this.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints(); 
			String chnnlprefix= "Channel "; 
			if (PamUtils.getNumChannels(channels)>1) {
				chnnlprefix= "Channels "; 
			}
					
			c.gridx=0;
			c.gridy=0; 
			PamPanel.addComponent(this, channelLabel = new JLabel(chnnlprefix + PamUtils.getChannelList(channels)+ " Buffer"), c);
			
			c.gridwidth=2; 
			c.gridy++;
			PamPanel.addComponent(this, bufferBar = new JProgressBar(), c);
			c.gridx=2; 
			PamPanel.addComponent(this, bufferLabel = new JLabel(), c);
			
			c.gridy++;
			c.gridx=0; 
			PamPanel.addComponent(this, posLabel = new JLabel(), c);

		}
		
		/**
		 * Update the panel. 
		 * @param kcount - the kcount
		 * @param buffermax - the maximum allowed value of the buffer. 
		 * @param nPossibilities - the current number of possibilities.  
		 */
		public void updatePanel(int kcount, int buffermax, int nPossibilities) {
			//System.out.println("Update buffer: " + kcount + " buffermax: " +buffermax +  "   " + (kcount/(double) buffermax) + "%" ); 
			SwingUtilities.invokeLater(()->{
				bufferBar.setValue(kcount);
				bufferBar.setMaximum(buffermax);
				bufferLabel.setText(String.format("%.1f%%", 100*kcount/(double) buffermax));
				posLabel.setText(String.format("Possibility Size: %d ",nPossibilities));
			});
		}
		
	}


}