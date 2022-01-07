package qa.swing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;

import PamUtils.PamCalendar;
import PamView.PamColors.PamColor;
import PamView.PamSidePanel;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.PamLabel;
import PamView.hidingpanel.HidingPanel;
import PamView.panel.PamPanel;
import PamView.panel.WestAlignedPanel;
import qa.QAControl;
import qa.QANotifyable;
import qa.QASequenceDataUnit;
import qa.generator.clusters.QACluster;

public class QASidePanel implements PamSidePanel, QANotifyable {

	private PamPanel mainPanel;
	private QAControl qaControl;
	
	private HidingPanel testsHidingPanel;
	
	private ArrayList<ClusterSet> clusterSets;
	private JLabel currSeqs;
	
	public QASidePanel(QAControl qaControl) {
		this.qaControl = qaControl;
		qaControl.addNotifyable(this);
		mainPanel = new PamPanel(PamColor.BORDER);
		mainPanel.setBorder(new TitledBorder("SIDE Module"));
		mainPanel.setLayout(new BorderLayout());
		QAOperationsDisplays qaOpsDisplays = qaControl.getQaOperationsStatus().getQaOpsDisplays();
		mainPanel.add(BorderLayout.NORTH, qaOpsDisplays.getOpsStatusLookup(false));

		PamPanel testsPanel = new PamPanel(PamColor.BORDER);
		
		testsHidingPanel = new HidingPanel(mainPanel, new WestAlignedPanel(testsPanel), HidingPanel.VERTICAL, false);
		testsHidingPanel.setBorder(new TitledBorder("Last Tests"));
		mainPanel.add(BorderLayout.CENTER, testsHidingPanel);
		testsPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		ArrayList<QACluster> clusters = qaControl.getAvailableClusters();
		clusterSets = new ArrayList(clusters.size());
//		testsPanel.add()
		testsPanel.add(new PamLabel("Species Cluster ", JLabel.RIGHT), c);
		c.gridx ++;
		testsPanel.add(new PamLabel("Last seq'", JLabel.LEFT), c);
		for (int i = 0; i < clusters.size(); i++) {
			c.gridy++;
			c.gridx = 0;
			ClusterSet clusterSet;
			clusterSets.add(clusterSet = new ClusterSet(clusters.get(i)));
			testsPanel.add(clusterSet.nameLabel, c);
			c.gridx++;
			testsPanel.add(clusterSet.lastSeqLabel, c);
		}
		c.gridx = 0;
		c.gridy++;
		testsPanel.add(new JLabel("Active Sequences", JLabel.RIGHT), c);
		c.gridx++;
		testsPanel.add(currSeqs = new JLabel(""), c);
		
		Timer updateTimer = new Timer(2000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateAll();
			}
		});
		updateTimer.start();
	}
	
	private class ClusterSet {
		private QACluster cluster;
		
		private PamLabel nameLabel;
		private PamLabel lastSeqLabel;
		
		private QASequenceDataUnit lastestStarted, latestEnded;

		/**
		 * @param cluster
		 */
		private ClusterSet(QACluster cluster) {
			super();
			this.cluster = cluster;
			nameLabel = new PamLabel(cluster.getName() + " ", JLabel.RIGHT);
			lastSeqLabel = new PamLabel("unknown");
		}

		/**
		 * @param lastestStarted the lastestStarted to set
		 */
		private void setLastestStarted(QASequenceDataUnit lastestStarted) {
			this.lastestStarted = lastestStarted;
			updateFields();
		}

		/**
		 * @param latestEnded the latestEnded to set
		 */
		private void setLatestEnded(QASequenceDataUnit latestEnded) {
			this.latestEnded = latestEnded;
			updateFields();
		}
		
		private void updateFields() {
			long now = PamCalendar.getTimeInMillis();
			if (latestEnded != null) {
				long timediff = now - latestEnded.getLastUpdateTime();
				lastSeqLabel.setText(formatTimeDiff(timediff));
			}			
			else {
				lastSeqLabel.setText("unknown");
			}
		}
	}
	
	private String formatTimeDiff(long diffMillis) {
		long timeMins = diffMillis / 60000;
		if (timeMins <= 0) {
			return "Just Now";
		}
		if (timeMins < 90) {
			return String.format(">%d mins ago", timeMins);
		}
		else {
			return String.format(">%d hours ago", timeMins/60);
		}
	}

	@Override
	public JComponent getPanel() {
		return mainPanel;
	}

	@Override
	public void rename(String newName) {
	}

	protected void updateAll() {
		for (ClusterSet cs:clusterSets) {
			cs.updateFields();
		}
		int nCurrSeq = qaControl.getQaGeneratorProcess().getNumCurrentSequences();
		currSeqs.setText(String.format("%d", nCurrSeq));
	}

	@Override
	public void qaNotify(int noteCode, Object noteObject) {
		switch(noteCode) {
		case QANotifyable.SEQUENCE_START:
		case QANotifyable.SEQUENCE_END:
			updateSequence((QASequenceDataUnit) noteObject, noteCode);
		}
	}

	private void updateSequence(QASequenceDataUnit seqDataunit, int noteCode) {
		QACluster cluster = seqDataunit.getSoundSequence().getQaTestSet().getQaCluster();
		if (cluster == null) return;
		ClusterSet clusterSet = findClusterSet(cluster);
		if (clusterSet == null) {
			return;
		}
		if (noteCode == QANotifyable.SEQUENCE_START) {
			clusterSet.setLastestStarted(seqDataunit);
		}
		else {
			clusterSet.setLatestEnded(seqDataunit);
		}
	}
	
	private ClusterSet findClusterSet(QACluster cluster) {
		for (ClusterSet cs:clusterSets) {
			if (cs.cluster == cluster) {
				return cs;
			}
		}
		return null;
	}

}
