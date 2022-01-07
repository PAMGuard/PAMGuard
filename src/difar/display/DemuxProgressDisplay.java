package difar.display;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import PamUtils.FrequencyFormat;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.PamColors.PamColor;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.PamLabel;
import PamView.dialog.PamTextArea;
import PamView.dialog.PamTextDisplay;
import PamView.panel.PamPanel;
import difar.DIFARMessage;
import difar.DemuxWorkerMessage;
import difar.DifarControl;

public class DemuxProgressDisplay implements DIFARDisplayUnit {

	private PamPanel mainPanel;
	
	private PamLabel speciesIcon;
	
	private PamTextDisplay messageArea, cursorPosText;
	
	private DifarControl difarControl;
	
	private JProgressBar progressBar;

	private Color normalColour;
	
	
	
	public DemuxProgressDisplay(DifarControl difarControl) {
		super();
		this.difarControl = difarControl;
		mainPanel = new PamPanel(new BorderLayout());
		PamPanel leftPanel = new PamPanel(new FlowLayout(FlowLayout.LEFT));
		leftPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		leftPanel.setFocusable(false);;
		leftPanel.add(new PamLabel(" Status: "));
		speciesIcon = new PamLabel();
		speciesIcon.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		leftPanel.add(speciesIcon);
		mainPanel.add(BorderLayout.WEST, leftPanel);
		mainPanel.add(BorderLayout.CENTER, messageArea = new PamTextDisplay());
		messageArea.setEditable(false);
		
		PamPanel infoPanel = new PamPanel(PamColor.BORDER);
		infoPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		int textLen = 12;
//		PamDialog.addComponent(infoPanel, new PamLabel("Selection ", JLabel.RIGHT), c);
//		c.gridx++;
//		PamDialog.addComponent(infoPanel, selection = new PamTextDisplay(textLen), c);
		c.gridx++;
		PamDialog.addComponent(infoPanel, new PamLabel("Cursor ", JLabel.RIGHT), c);
		c.gridx++;
		PamDialog.addComponent(infoPanel, cursorPosText = new PamTextDisplay(textLen), c);
		c.gridx++;
		infoPanel.add(progressBar = new JProgressBar(SwingConstants.HORIZONTAL), c);
		mainPanel.add(BorderLayout.EAST, infoPanel);


		
		progressBar.setMaximum(100);
		progressBar.setMinimum(0);
		progressBar.setStringPainted(true);
		this.normalColour = progressBar.getForeground();
//		progressBar.s
		
	}

	public void newMessage(DemuxWorkerMessage workerMessage) {
		String msg = workerMessage.getMessageString();
		String progressMsg = (String) workerMessage.getStatusString();
		if (workerMessage.difarDataUnit.isVessel()){
			PamSymbol symbol = new PamSymbol(PamSymbolType.SYMBOL_CIRCLE,10,10,false, Color.BLACK, Color.BLACK);
			String classification = "Vessel Calibration";
		} else {
			PamSymbol symbol = workerMessage.difarDataUnit.getLutSpeciesItem().getSymbol();
			symbol.setHeight(10);
			symbol.setWidth(10);
			speciesIcon.setIcon(symbol);
			String classification = workerMessage.difarDataUnit.getLutSpeciesItem().getText();
			
		}
		
		if (msg != null) {
			messageArea.setText(msg);
		}
		if (progressMsg != null) {
			progressBar.setString(progressMsg);
		}
		
		double progress = workerMessage.getTotalProgress();
		if (progress > 0) {
			progressBar.setValue((int) progress);
		}
		Boolean lock75, lock15;
		lock75 = workerMessage.isLock75();
		lock15 = workerMessage.isLock15();
		if (workerMessage.status == DemuxWorkerMessage.STATUS_INDEMUXCALC && lock15 != null && lock75 != null) {
			boolean locked = lock15 && lock75;
			progressBar.setString(locked ? "Locked": "No lock");
			progressBar.setForeground(locked ? normalColour : Color.RED); 
		}
	}
	
	@Override
	public String getName() {
		return "Demux Progress";
	}

	@Override
	public Component getComponent() {
		return mainPanel;
	}

	@Override
	public int difarNotification(DIFARMessage difarMessage) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void setDataGramCursorInfo(Double freq, Double angle) {
		
		String cursorText = "";
		
		if (freq == null) {
			cursorText = "-";
		}
		else {
			cursorText = FrequencyFormat.formatFrequency(freq, true);
		}
		cursorText += ", ";
		
		if (angle == null) {
			cursorText += "-";
		}
		else {
			cursorText += String.format("%3.1f", angle);
		}
		cursorText += "\u00B0";
		cursorPosText.setText(cursorText);

	}

}
