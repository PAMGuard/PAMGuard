package mcc.mccacquisition;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.NativeLongByReference;

import Acquisition.AcquisitionDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.warn.WarnOnce;
import analoginput.AnalogRangeData;
import mcc.MccJniInterface;
import mcc.mccjna.MCCBoardInfo;
import mcc.mccjna.MCCConstants;
import mcc.mccjna.MCCJNA;
import mcc.mccjna.MCCJNA.MCCLibrary;
import mcc.mccjna.MCCUtils;

public class MCCAcquisitionDialogPanel {
	
	private JPanel mainPanel;
	private MCCDaqSystem mccDaqSystem;
	private JLabel revLabel;
	private JComboBox<String> boardList;
	private JComboBox<String> terminalType;
	private JComboBox<AnalogRangeData> adcRange;

	private MCCLibrary mccLibrary;
	private ArrayList<MCCBoardInfo> boardInfos;
	private MCCDaqParams daqParams;
	private AcquisitionDialog acquisitionDialog;
	
	private static final String warnTitle = "Measurement Computing Acquisition";
	
//	private int[] boardRanges = MccJniInterface.bipolarRanges;
	
	public MCCAcquisitionDialogPanel(AcquisitionDialog acquisitionDialog, MCCDaqSystem mccDaqSystem) {
		this.acquisitionDialog = acquisitionDialog;
		this.mccDaqSystem = mccDaqSystem;
		mccLibrary =MCCJNA.getMccLibrary();
		if (mccLibrary != null) {
			boardInfos = MCCJNA.getBoardInformation();
		}
		mainPanel = new JPanel();
		mainPanel.setBorder(new TitledBorder("MCC Device"));
		boardList = new JComboBox<>();
		GridBagConstraints c = new PamGridBagContraints();
		mainPanel.setLayout(new GridBagLayout());

		mainPanel.add(new JLabel(" MCC Software ", JLabel.RIGHT), c);
		c.gridx++;
		c.gridwidth = 2;
		mainPanel.add(revLabel = new JLabel(""),c); 
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		mainPanel.add(new JLabel(" MCC Board' ", JLabel.RIGHT), c);
		c.gridx++;
		c.gridwidth = 2;
		mainPanel.add(boardList, c);
		c.gridx+=c.gridwidth;
		c.gridwidth = 1;
		JButton flashButton = new JButton("Flash");
		mainPanel.add(flashButton,c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		mainPanel.add(new JLabel(" Terminal Config' ", JLabel.RIGHT), c);
		c.gridx++;
		c.gridwidth = 2;
		mainPanel.add(terminalType = new JComboBox<String>(), c);
		terminalType.addItem("Single Ended");
		terminalType.addItem("Differential");
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		mainPanel.add(new JLabel(" Range ", JLabel.RIGHT), c);
		c.gridx++;
		c.gridwidth = 2;
		mainPanel.add(adcRange = new JComboBox<AnalogRangeData>(), c);
		
		boardList.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boardSelection();
			}
		} );
		adcRange.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				adcRangeSelection();
			}
		});
		terminalType.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				terminalAction();
			}
		});
		flashButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				flashBoardButton();
			}
		});
		revLabel.setToolTipText("Software version should be V5.0 or above");
		boardList.setToolTipText("Select device. If none are showing, you need to run the Measurementcomputing Instacal utility then restart PAMGuard");
		terminalType.setToolTipText("Select input type. Use differential wherever possible");
		adcRange.setToolTipText("Select the voltage range of the board");
		flashButton.setToolTipText("Flash the LED on the device");
	}

	protected void flashBoardButton() {
		MCCBoardInfo board = getSelectedBoard();
		if (board != null && mccLibrary != null) {
			mccLibrary.cbFlashLED(board.getBoardNumber());
		}
	}

	public JPanel getComponent() {
		return mainPanel;
	}

	public void setParams() {
		daqParams = mccDaqSystem.getMccDaqParams().clone();
		boardList.removeAllItems();
		if (mccLibrary == null) {
			boardList.addItem("--No MCC Library--!");
			boardList.setEnabled(false);
			revLabel.setText("MCC drivers not installed");
		}
		else {
			boardInfos = MCCJNA.getBoardInformation();
			FloatByReference r1 = new FloatByReference();
			FloatByReference r2 = new FloatByReference();
			mccLibrary.cbGetRevision(r1, r2);
			revLabel.setText(String.format(" Rev %3.2f", r1.getValue()));
		}
		if (boardInfos.size() == 0) {
			boardList.addItem("--No MCC Boards--!");
			boardList.setEnabled(false);
		}
		else {
			for (int i = 0; i < boardInfos.size(); i++) {
				boardList.addItem(boardInfos.get(i).toString());
			}
			boardList.setEnabled(true);
		}
		if (daqParams.boardIndex < boardInfos.size()) {
			boardList.setSelectedIndex(daqParams.boardIndex);
		}
		terminalType.setSelectedIndex(daqParams.differential ? 1 : 0);
		
//		boardSelection();
	}

	public boolean getParams() {
		if (boardList.isEnabled() == false) {
			return false;
		}
		daqParams.boardIndex = boardList.getSelectedIndex();
		daqParams.differential = terminalType.getSelectedIndex() == 1;
		daqParams.setRangeData((AnalogRangeData) adcRange.getSelectedItem());
		mccDaqSystem.setMccDaqParams(daqParams);
		return checkSampleRate();
	}
	
	private boolean checkSampleRate() {
		if (mccLibrary == null) {
			return showWarning("No Measurement Computing software is installed on this system");
		}
		MCCBoardInfo selBoard = getSelectedBoard();
		Double fs = acquisitionDialog.getSampleRate();
		if (fs == null || selBoard == null) {
			return showWarning("Invalid board selection");
		}
		Integer nChan = acquisitionDialog.getChannels();
		if (nChan == null) {
			return showWarning("Invalid number of channels");
		}
		AnalogRangeData ard = (AnalogRangeData) adcRange.getSelectedItem();
		Integer selRange = MCCUtils.findRangeCode(ard);
		if (selRange == null) {
			return showWarning("No input range selected");
		}
		// try to start the adc for a quick scan at this sample rate and
		// see what happens. 
		NativeLongByReference rate = new NativeLongByReference();
		double dfs = fs;
		rate.setValue(new NativeLong((int) dfs));
		
		int nSamp = nChan*256;
		
		Pointer memHandle = mccLibrary.cbWinBufAlloc(new NativeLong(nSamp));	
		int options = MCCConstants.CONTINUOUS | MCCConstants.BACKGROUND;
		int ans = mccLibrary.cbAInScan(selBoard.getBoardNumber(), 0, nChan-1, new NativeLong(nSamp), rate, selRange, memHandle, options);
		if (ans != MCCConstants.NOERRORS) {
			String errStr = MCCJNA.getErrorMessage(ans);
			return acquisitionDialog.showWarning(warnTitle, errStr);
		}
		ans = mccLibrary.cbStopIOBackground(selBoard.getBoardNumber(), MCCConstants.AIFUNCTION);
//		System.out.printf("FS Check and %d new FS %d\n", ans, rate.getValue());
		if ((int) dfs != rate.getValue().longValue()) {
			String msg = String.format("The selected device cannot sample at exactly %dHz. \n" + 
					"The closest available sample rate is %dHz. "
					+ "Do you want to switch to this sample rate ?", (int) dfs, rate.getValue().intValue());
			int wa = WarnOnce.showWarning(acquisitionDialog, warnTitle, msg, WarnOnce.OK_CANCEL_OPTION);
			if (wa == WarnOnce.OK_OPTION) {
				acquisitionDialog.setSampleRate(rate.getValue().floatValue());
			}
			return false; // false anyway, so have to press OK again - should be fine next time. 
		}
		return true;
	}
	
	private boolean showWarning(String msg) {
		return acquisitionDialog.showWarning(warnTitle, msg);
	}
	
	private MCCBoardInfo getSelectedBoard() {
		if (mccLibrary == null) {
			return null;
		}
		int selBoard = boardList.getSelectedIndex();
		if (selBoard >= 0 && selBoard < boardInfos.size()) {
			return boardInfos.get(selBoard);
		}
		else {
			return null;
		}
	}

	protected void adcRangeSelection() {
		AnalogRangeData ard = (AnalogRangeData) adcRange.getSelectedItem();
		if (ard != null) {
			double pp = ard.getRange()[1]-ard.getRange()[0];
			acquisitionDialog.setVPeak2Peak(pp);
		}
	}
	
	protected void terminalAction() {
		fillADCRanges();
	}

	protected void boardSelection() {
		fillADCRanges();
	}
	
	MCCBoardInfo lastBoard = null;
	int lastTerm = -1;
	List<AnalogRangeData> rangeData;
	private void fillADCRanges() {

		adcRange.removeAllItems();
		MCCBoardInfo selInfo = getSelectedBoard();
		int terminalConfig = terminalType.getSelectedIndex() == 0 ? MCCConstants.SINGLE_ENDED : MCCConstants.DIFFERENTIAL;
		if (rangeData == null || selInfo != lastBoard || terminalConfig != lastTerm) {
			rangeData = getAvailableRanges(selInfo, terminalConfig);
			lastBoard = selInfo;
			lastTerm = terminalConfig;
		}
		if (rangeData == null) return;
		int currSel = -1;
		for (int i = 0; i < rangeData.size(); i++) {
			if (rangeData.get(i).equals(daqParams.getRangeData())) {
				currSel = i;
			}
			adcRange.addItem(rangeData.get(i));
		}
		if (currSel >=0) {
			adcRange.setSelectedIndex(currSel);
		}
//		checkSampleRate();
	}

	private List<AnalogRangeData> getAvailableRanges(MCCBoardInfo selInfo, int terminalConfig) {
		if (selInfo == null) {
			rangeData = MCCUtils.getAllRanges(true, true);
		}
		else {
			int[] rangeCodes = selInfo.getAllowableRanges(terminalConfig, true, MCCConstants.BIPOLAR);
			rangeData = MCCUtils.getAnalogRangeData(rangeCodes);
		}
		if (rangeData == null || rangeData.size() == 0) {
			rangeData = MCCUtils.getAllRanges(true, false);
		}
		Collections.sort(rangeData);
		return rangeData;
	}
	

}
