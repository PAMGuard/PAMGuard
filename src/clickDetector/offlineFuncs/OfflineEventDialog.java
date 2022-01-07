package clickDetector.offlineFuncs;

import generalDatabase.lookupTables.LookupComponent;
import generalDatabase.lookupTables.LookupItem;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import clickDetector.ClickControl;
import PamView.ColorSettings;
import PamView.DBTextArea;
import PamView.PamColors;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class OfflineEventDialog extends PamDialog {

	private OfflineEventDataUnit offlineEventDataUnit;

	private static OfflineEventDialog singleInstance;

	private ClickControl clickControl;

	private JTextField eventNumber;

	//	private JComboBox eventType, eventColour;
	private LookupComponent speciesList;

	private JTextField minNum, bestNum, maxNum;

	private DBTextArea commentText;

	private JButton colourButton;

	private static final int colourSize = 14;


	public OfflineEventDialog(Window parentFrame, ClickControl clickControl) {
		super(parentFrame, "Click event", false);
		this.clickControl = clickControl;


		JPanel mainPanel = new JPanel();
//		mainPanel.setBorder(new EmptyBorder(new Insets(5 ,5, 5, 5)));
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		JPanel northPanel = new JPanel();
		northPanel.setLayout(new GridBagLayout());
		mainPanel.setBorder(new TitledBorder("Event information"));
		GridBagConstraints c = new PamGridBagContraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = c.gridy = 0;
//		c.fill = GridBagConstraints.NONE;
		c.gridwidth = 2;
		northPanel.add(new JLabel("Event Number", SwingConstants.RIGHT), c);
		c.gridx += c.gridwidth;
		c.gridwidth = 1;
		northPanel.add(eventNumber = new JTextField(3), c);
		eventNumber.setEditable(false);
		c.gridx+=2;
		c.gridwidth = 1;
		northPanel.add(new JLabel("   Colour ", JLabel.RIGHT), c);
		c.gridx++;
		northPanel.add(colourButton = new JButton(), c);
		colourButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectColour();
			}
		});


		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 6;
//		c.fill = GridBagConstraints.HORIZONTAL;
		//TODO FIXME! check this!!!
		speciesList = new LookupComponent(ClicksOffline.ClickTypeLookupName, null);
		speciesList.setNorthTitle("Event type / species");
		northPanel.add(speciesList.getComponent(), c);
		
//		mainPanel.add(northPanel)/;
		//		addComp
		//		c.gridx++;
		//		addComponent(mainPanel, new JLabel(" Colour"), c);
		//		c.gridx++;
		//		c.gridwidth = 2;
//				addComponent(mainPanel, eventColour = new JComboBox(), c);
//		JPanel southPanel = new JPanel(new GridBagLayout());
//		c = new PamGridBagContraints();
		JPanel southPanel = northPanel;
		c.fill = GridBagConstraints.NONE;
		c.gridx = 0;
		c.gridwidth = 6;
		c.gridy++;
//		c.fill = GridBagConstraints.BOTH;
		southPanel.add(new JLabel(" "), c);
		c.gridy++;
		southPanel.add(new JLabel("Estimated number of animals"), c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		southPanel.add(new JLabel("Min ", SwingConstants.RIGHT), c);
		c.gridx++;
		southPanel.add(minNum = new JTextField(3), c);
		c.gridx++;
		southPanel.add(new JLabel(" Best ", SwingConstants.RIGHT), c);
		c.gridx++;
		southPanel.add(bestNum = new JTextField(3), c);
		c.gridx++;
		southPanel.add(new JLabel(" Max ", SwingConstants.RIGHT), c);
		c.gridx++;
		southPanel.add(maxNum = new JTextField(3), c);
		c.gridx = 0;
		c.gridwidth = 6;
		c.gridy++;
		southPanel.add(new JLabel("Comment "), c);
		c.gridy++;
		commentText = new DBTextArea(1, 2, OfflineEventLogging.COMMENT_LENGTH);
		JScrollPane scrollPane = new JScrollPane(commentText.getComponent());
		scrollPane.setPreferredSize(new Dimension(350, 150));
//		southPanel.add(scrollPane, c);
//		southPanel.add(commentText.getComponent(), c);

		JPanel nwPanel = new JPanel(new BorderLayout());
		nwPanel.add(BorderLayout.WEST, northPanel);
		mainPanel.add(nwPanel);
		mainPanel.add(scrollPane);
//		mainPanel.add(southPanel);
		setDialogComponent(mainPanel);
		setResizable(true);
	}

	private JPopupMenu createColourMenu() {
		JPopupMenu popMenu = new JPopupMenu();
		PamColors pamColours = PamColors.getInstance();
		for (int i = 1; i < pamColours.getNWhaleColours(); i++) {
			Color col = pamColours.getWhaleColor(i);
			JMenuItem mi = new JMenuItem(new Integer(i).toString(), new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, colourSize, colourSize, true, col, col));
			mi.addActionListener(new SelectColour(i));
			popMenu.add(mi);
		}
		return popMenu;
	}

	private void selectColour() {
		JPopupMenu colourMenu = createColourMenu();
		colourMenu.show(colourButton, colourButton.getWidth()/2, colourButton.getHeight()/2);
	}

	private class SelectColour implements ActionListener {

		int colourIndex;

		public SelectColour(int colourIndex) {
			super();
			this.colourIndex = colourIndex;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			setWhaleColour(colourIndex);
		}


	}

	private void setWhaleColour(int colourIndex) {
		if (offlineEventDataUnit != null) {
			offlineEventDataUnit.setColourIndex(colourIndex);
		}
		Color col = PamColors.getInstance().getWhaleColor(colourIndex);
		colourButton.setIcon(new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, colourSize, colourSize, true, col, col));
	}
	public static OfflineEventDataUnit showDialog(Window parentFrame, ClickControl clickControl, OfflineEventDataUnit offlineEventDataUnit) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame || 
				singleInstance.clickControl != clickControl) {
			singleInstance = new OfflineEventDialog(parentFrame, clickControl);
		}
		singleInstance.offlineEventDataUnit = offlineEventDataUnit;
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.offlineEventDataUnit;
	}

	@Override
	public void cancelButtonPressed() {
		offlineEventDataUnit = null;
	}  

	private void setParams() {
		if (offlineEventDataUnit == null) {
			eventNumber.setText(null);
			minNum.setText(null);
			bestNum.setText(null);
			maxNum.setText(null);
			commentText.setText(null);
			speciesList.setSelectedCode(null);
		}
		else {
			setIntegerText(eventNumber, guessEventId(offlineEventDataUnit));
			setShortText(minNum, offlineEventDataUnit.getMinNumber());
			setShortText(bestNum, offlineEventDataUnit.getBestNumber());
			setShortText(maxNum, offlineEventDataUnit.getMaxNumber());
			commentText.setText(offlineEventDataUnit.getComment());
			speciesList.setSelectedCode(offlineEventDataUnit.getEventType());
			int symCol = offlineEventDataUnit.getColourIndex();
			setWhaleColour(symCol);
		}
	}
	
	private int guessEventId(OfflineEventDataUnit offlineEventDataUnit) {
		if (offlineEventDataUnit == null) {
			return 0;
		}
		if (offlineEventDataUnit.getDatabaseIndex() > 0) {
			return offlineEventDataUnit.getDatabaseIndex();
		}
		else {
			return offlineEventDataUnit.getEventId();
		}
	}

	private void setIntegerText(JTextField tf, Integer intNum) {
		if (intNum == null) {
			tf.setText(null);
		}
		else {
			tf.setText(String.format("%d", intNum));
		}
	}
	private void setShortText(JTextField tf, Short intNum) {
		if (intNum == null) {
			tf.setText(null);
		}
		else {
			tf.setText(String.format("%d", intNum));
		}
	}

	private Integer getIntegerValue(JTextField tf) {
		try {
			return Integer.valueOf(tf.getText());
		}
		catch (NumberFormatException e) {
			return null;
		}
	}
	private Short getShortValue(JTextField tf) {
		try {
			return Short.valueOf(tf.getText());
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	public boolean getParams() {
		if (offlineEventDataUnit == null) {
			return false;
		}
		LookupItem selItem = speciesList.getSelectedItem();
		if (selItem == null) {
			return showWarning("You must select a species / event type");
		}
		offlineEventDataUnit.setEventType(selItem.getCode());
		offlineEventDataUnit.setMinNumber(getShortValue(minNum));
		offlineEventDataUnit.setBestNumber(getShortValue(bestNum));
		offlineEventDataUnit.setMaxNumber(getShortValue(maxNum));
		offlineEventDataUnit.setComment(commentText.getText());
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
