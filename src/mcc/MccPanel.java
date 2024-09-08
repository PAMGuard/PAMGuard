package mcc;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import PamView.dialog.PamGridBagContraints;
import mcc.mccjna.MCCBoardInfo;
import mcc.mccjna.MCCConstants;
import mcc.mccjna.MCCJNA;
import mcc.mccjna.MCCUtils;

/**
 * Dialog panel for MCC boards. 
 * @author Doug Gillespie
 *
 */
public class MccPanel {

	private JPanel mccPanel;
	private JComboBox<Object> boardList;
	private JComboBox<Object> boardRange;

	private MccJniInterface mccJni = new MccJniInterface();
	private ArrayList<MCCBoardInfo> boardInfos;
	private int nBoards;
	
	public MccPanel() {
		mccPanel = new JPanel();
		mccPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		mccPanel.add(new JLabel("Board ", SwingConstants.RIGHT), c);
		c.gridx++;
		c.gridwidth = 2;
		mccPanel.add(boardList = new JComboBox<>(), c);
		boardList.setToolTipText("<html>Select MeasurementComputing acquisition device.<p>"
				+ "(Devices must first be installed using the MCC Instacal utility)</html>");
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 1;
		mccPanel.add(new JLabel("Range ", SwingConstants.RIGHT), c);
		c.gridx++;
		c.gridwidth = 2;
		mccPanel.add(boardRange = new JComboBox<>(), c);
		boardRange.setToolTipText("<html>Select voltage range for the measurement. "
				+ "Using the smallest<p>possible range will maximise the resolution of the measurement."
				+ "<p>Note that only differential measurements are currently supported</html>");
		
		boardInfos = MCCJNA.getBoardInformation();
		nBoards = 0;
		if (boardInfos != null) {
			nBoards = boardInfos.size();
		}
		for (int i = 0; i < nBoards; i++) {
			boardList.addItem(boardInfos.get(i).getBoardName());
		}
		int[] bpRanges = MCCConstants.bipolarRanges;
		for (int i = 0; i < bpRanges.length; i++) {
			boardRange.addItem(MCCUtils.sayBibolarRange(bpRanges[i]));
		}
		
	}
	
	public JPanel getPanel() {
		return mccPanel;
	}

	public void setDeviceIndex(int boardIndex) {
		if (boardIndex >= 0 && boardIndex < nBoards) {
			boardList.setSelectedIndex(boardIndex);
		}
	}
	
	/**
	 * Gets the device index - position in board combo box. 
	 * @return device index (0 to ...)
	 */
	public int getDeviceIndex() {
		return boardList.getSelectedIndex();
	}

	public void setRange(int boardRange) {
		int rangeInd = MCCUtils.getBipolarRangeIndex(boardRange);
		if (rangeInd >= 0) {
			this.boardRange.setSelectedIndex(rangeInd);
		}
	}
	
	public int getRange() {
		int rangeInd = boardRange.getSelectedIndex();
		if (rangeInd >= 0) {
			return MCCConstants.bipolarRanges[rangeInd];
		}
		return -1;
	}

}
