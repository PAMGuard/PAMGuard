package qa.swing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import PamUtils.FrequencyFormat;
import PamView.dialog.PamCheckBox;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.PamLabel;
import PamView.dialog.VerticalLabel;
import PamView.hidingpanel.HidingPanel;
import PamView.panel.PamPanel;
import qa.QAControl;
import qa.generator.clusters.QACluster;

/**
 * Make a panel which allows you to select a load of clusters. 
 * May need to have multiple selection types ? 
 * @author dg50
 *
 */
public class ClusterSelectPanel {
	
	private JPanel mainPanel; 
	
	private JPanel clusterPanel;

	private String[] colNames;

	private QAControl qaControl;

	private int nColumns;

	private ArrayList<QACluster> currentClusterList;
	
	private JCheckBox[][] clusterSelection;  
	
	private ArrayList<ClusterSelectionListener> selectionListeners = new ArrayList<>();

	protected final int columStartOffset = 2;
	
	protected final int clusterRowOffset = 2;

	public ClusterSelectPanel(QAControl qaControl, String[] colNames) {
		this.qaControl = qaControl;
		this.colNames = colNames;
		if (colNames == null) {
			colNames = new String[1];
		}
		this.nColumns = colNames.length;
		
		mainPanel = new PamPanel(new BorderLayout());
		clusterPanel = new PamPanel();
//		HidingPanel hp = new HidingPanel(mainPanel, clusterPanel, HidingPanel.VERTICAL, true);
		mainPanel.add(BorderLayout.CENTER, clusterPanel);
		setClusterList(qaControl.getAvailableClusters());
	}

	public JPanel getComponent() {
		return mainPanel;
	}
	
	/**
	 * Set whether a particular cluster is selected or not. 
	 * @param qaCluster cluster reference
	 * @param iColumn column in table
	 * @param select select or not
	 */
	public void setSelection(QACluster qaCluster, int iColumn, boolean select) {
		int ind = getClusterIndex(qaCluster);
		if (ind < 0) return; // should never happen
		clusterSelection[ind][iColumn].setSelected(select);
	}
	
	/**
	 * Get whether a particular cluster is selected or not. 
	 * @param qaCluster cluster reference
	 * @param iColumn column in table
	 * @return whether or not selected
	 */
	public boolean getSelection(QACluster qaCluster, int iColumn) {
		int ind = getClusterIndex(qaCluster);
		if (ind < 0) return false; // should never happen
		return clusterSelection[ind][iColumn].isSelected();
	}
	
	/**
	 * Enable a Checkbox in the selection
	 * @param qaCluster cluster reference
	 * @param iColumn column in table
	 * @param enable enable the checkbox
	 */
	public void enableSelection(QACluster qaCluster, int iColumn, boolean enable) {
		int ind = getClusterIndex(qaCluster);
		if (ind < 0) return; // should never happen
		clusterSelection[ind][iColumn].setEnabled(enable);
	}

	/**
	 * Get the index of a cluster in the current list
	 * @param qaCluster
	 * @return index or -1 if it doesn't exist. 
	 */
	private int getClusterIndex(QACluster qaCluster) {
		return currentClusterList.indexOf(qaCluster);
	}
	
	public void setClusterList(ArrayList<QACluster> clusterList) {
		this.currentClusterList = clusterList;
		clusterPanel.removeAll();
		if (clusterList == null) {
			return;
		}
		clusterSelection = new PamCheckBox[clusterList.size()][nColumns];
		clusterPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		c.gridx = columStartOffset;
		c.gridy = clusterRowOffset-1;
		c.fill = GridBagConstraints.VERTICAL;
		c.anchor = GridBagConstraints.BASELINE;
		for (int i = 0; i < nColumns; i++) {
			VerticalLabel vLabel;
			if (colNames[i] != null) {
				clusterPanel.add(vLabel = new VerticalLabel(colNames[i], true), c);
				//			vLabel.setToolTipText("Frequency Range " + FrequencyFormat.formatFrequencyRange(dataBlock.getFrequencyRange(), true));
			}
			c.gridx++;
		}
		c.gridx = columStartOffset-1;
		c.gridy = clusterRowOffset;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		for (QACluster cluster:clusterList) {
//			System.out.printf("Cluster %s has frequency range %s\n", cluster.getName(), cluster.getSoundGenerator().getFrequencyRange());
			JLabel label;
			clusterPanel.add(label = new PamLabel(cluster.getName() + " ", JLabel.RIGHT), c);
			label.setToolTipText("Frequency Range " + FrequencyFormat.formatFrequencyRange(cluster.getSoundGenerator().getFrequencyRange(), true));
			c.gridy++;
		}
		

		c.gridx = columStartOffset;
		c.fill = GridBagConstraints.VERTICAL;
		c.anchor = GridBagConstraints.BASELINE;
		for (int i  = 0; i < nColumns; i++) {
			c.gridy = clusterRowOffset;
			int iClus = 0;
			for (QACluster cluster:clusterList) {
				clusterPanel.add(clusterSelection[iClus][i] = new PamCheckBox(""), c);
				clusterSelection[iClus][i].addActionListener(new ClusterSelectAction(cluster, i));
				c.gridy++;
				iClus++;
			}
			c.gridx++;
		}
	}
	
	/**
	 * Add a selection listener which will receive notifications if any 
	 * of the check boxes are changed. 
	 * @param clusterSelectionListener selection listener
	 */
	public void addSelectionListener(ClusterSelectionListener clusterSelectionListener) {
		selectionListeners.add(clusterSelectionListener);
	}
	
	/**
	 * Remove a selection listener which received notifications if any 
	 * of the check boxes are changed. 
	 * @param clusterSelectionListener
	 * @return true if it existed
	 */
	public boolean removeSelectionListener(ClusterSelectionListener clusterSelectionListener) {
		return selectionListeners.remove(clusterSelectionListener);
	}
	
	/**
	 * action listener for every check box. Can send out notifications 
	 * to other listeners to the goings on in this whole panel. 
	 * @author dg50
	 *
	 */
	private class ClusterSelectAction implements ActionListener {

		private QACluster qaCluster;

		private int column;
		
		/**
		 * @param qaCluster
		 * @param column
		 */
		public ClusterSelectAction(QACluster qaCluster, int column) {
			super();
			this.qaCluster = qaCluster;
			this.column = column;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			boolean isSel = getSelection(qaCluster, column);
			for (ClusterSelectionListener csl:selectionListeners) {
				csl.clusterSelected(qaCluster, column, isSel);
			}
		}

	}

	/**
	 * @return the clusterPanel
	 */
	public JPanel getClusterPanel() {
		return clusterPanel;
	}

	/**
	 * @return the colNames
	 */
	public String[] getColNames() {
		return colNames;
	}

	/**
	 * @return the qaControl
	 */
	public QAControl getQaControl() {
		return qaControl;
	}

	/**
	 * @return the nColumns
	 */
	public int getnColumns() {
		return nColumns;
	}

	/**
	 * @return the currentClusterList
	 */
	public ArrayList<QACluster> getCurrentClusterList() {
		return currentClusterList;
	}
}
