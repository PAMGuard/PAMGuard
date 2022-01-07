/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation 
 * of marine mammals (cetaceans).
 *  
 * Copyright (C) 2006 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package clickDetector.ClickClassifiers.basic;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import clickDetector.BasicClickIdParameters;
import clickDetector.ClickTypeParams;
import PamView.PamSymbol;
import PamView.PamSymbolDialog;
import PamView.dialog.PamDialog;
import clickDetector.ClickAlarm;
import clickDetector.ClickControl;
import java.awt.Insets;
import javax.swing.JComboBox;

/**
 * 
 * @author Doug Gillespie
 *         <p>
 *         Dialog for definition of a single click type based on Marjolainie
 *         Caillat's MRes work, 2005.
 */
public class ClickTypeDialog extends PamDialog implements ActionListener {

	static private ClickTypeDialog clickTypeDialog;

    private ClickControl clickControl = null;

	ClickTypeParams clickTypeParams;
	
	BasicClickIdParameters basicClickIdParameters;

	static private int FREQ_FIELD_WIDTH = 8;

	private JTextField name, code;
	
	private JCheckBox[] enableBoxes = new JCheckBox[5];

	private JTextField[] band1Freq = new JTextField[2]; // frequency range for test band

	private JTextField[] band2Freq = new JTextField[2]; // frequency range for control
												// band

	private JTextField[] band1Energy = new JTextField[2]; // energy range for test
													// band

	private JTextField[] band2Energy = new JTextField[2]; // energy range for control
													// band

	private JTextField bandEnergyDifference; // minimum difference in ban energiesin
										// dB

	private JTextField[] peakFrequencySearch = new JTextField[2]; // search range for
															// peak frequency
															// (Hz)

	private JTextField[] peakFrequencyRange = new JTextField[2]; // allowable range
															// for peak
															// frequency (Hz)

	private JTextField[] peakWidth = new JTextField[2]; // max width of frequency peak
												// (Hz)

	private JTextField widthEnergyFraction; // energy fraction to use in width
									// calculation

	private JTextField[] clickLength = new JTextField[2]; // allowable lengh of clicks
													// in ms.
	
	private JTextField[] meanSumRange = new JTextField[2];
	private JTextField[] meanSelRange = new JTextField[2];

	private JTextField lengthEnergyFraction;

    /**
     * Maximum amount of elapsed time between detections to ring the alarm
     */
    private JTextField maxTime;

	private JButton symbolTypeButton, filtersButton;

	private JPopupMenu typeMenu;
	
    /**
     * ComboBox to list available alarms
     */
    private JComboBox alarmChooser;

	private SymbolPanel symbolPanel;
	private EnergyPanel energyPanel;
	private PeakPanel peakPanel;
	private MeanFreqPanel meanFreqPanel;
	private LengthPanel lengthPanel;
    private AlarmPanel alarmPanel;

	private ClickTypeDialog(Frame parentFrame) {

		super(parentFrame, "Individual Click Classification", true);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setOpaque(true);

		JPanel c = new JPanel();
		c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
		c.add(new TypePanel());
		c.add(energyPanel  = new EnergyPanel());
		c.add(peakPanel = new PeakPanel());
		c.add(meanFreqPanel = new MeanFreqPanel());
		c.add(lengthPanel = new LengthPanel());
        c.add(alarmPanel = new AlarmPanel());

		symbolTypeButton.addActionListener(this);
		// symbolColorButton.addActionListener(this);
		
		getDefaultButton().setText("Species Defaults");

		setDialogComponent(c);

		setHelpPoint("detectors.clickDetectorHelp.docs.ClickDetector_clickClassification");
	}

    /**
     * New constructor required in order to pass clickControl object, so that
     * we can access the alarm list through clickParameters
     * @param parentFrame
     * @param clickControl
     * @param basicClickIdParameters
     * @param clickTypeParams
     * @return
     */
    public static ClickTypeParams showDialog(Frame parentFrame,
            ClickControl clickControl,
            BasicClickIdParameters basicClickIdParameters,
            ClickTypeParams clickTypeParams) {

        /* check to see if this class has been instantiated.  If not, do that
         * first or else we can't access the clickControl parameter
         */
		if (clickTypeDialog == null || clickTypeDialog.getOwner() != parentFrame) {
			clickTypeDialog = new ClickTypeDialog(parentFrame);
		}

        clickTypeDialog.clickControl = clickControl;
        showDialog(parentFrame, basicClickIdParameters, clickTypeParams);
		return clickTypeDialog.clickTypeParams;
    }

	public static ClickTypeParams showDialog(Frame parentFrame, BasicClickIdParameters basicClickIdParameters, ClickTypeParams clickTypeParams) {

		if (clickTypeDialog == null || clickTypeDialog.getOwner() != parentFrame) {
			clickTypeDialog = new ClickTypeDialog(parentFrame);
		}
		
		clickTypeDialog.basicClickIdParameters = basicClickIdParameters;

		clickTypeDialog.clickTypeParams = clickTypeParams.clone();
		
		clickTypeDialog.setParams(clickTypeParams);

		clickTypeDialog.setVisible(true);

		return clickTypeDialog.clickTypeParams;

	}

    /**
     * add the list of available alarms to the ComboBox
     */
    private void addAlarmList() {
    	if (clickControl.getClickParameters().clickAlarmList==null)return;
        alarmChooser.removeAllItems();
        for (int i=0 ; i<clickControl.getClickParameters().clickAlarmList.size() ; i++ ) {
            alarmChooser.addItem(clickControl.getClickParameters().clickAlarmList.get(i));
        }
    }

    /**
     * Loads the dialog fields with the current values in {@link ClickTypeParams}
     *
     * @param clickTypeParams
     */
	private void setParams(ClickTypeParams clickTypeParams) {

		// fill in dialog entries
		name.setText(clickTypeParams.getName());
		code.setText(String.format("%d", clickTypeParams.getSpeciesCode()));
		enableBoxes[0].setSelected((clickTypeParams.whichSelections & ClickTypeParams.ENABLE_ENERGYBAND) != 0);
		enableBoxes[1].setSelected((clickTypeParams.whichSelections & ClickTypeParams.ENABLE_PEAKFREQPOS) != 0);
		enableBoxes[2].setSelected((clickTypeParams.whichSelections & ClickTypeParams.ENABLE_PEAKFREQWIDTH) != 0);
		enableBoxes[3].setSelected((clickTypeParams.whichSelections & ClickTypeParams.ENABLE_MEANFREQUENCY) != 0);
		enableBoxes[4].setSelected((clickTypeParams.whichSelections & ClickTypeParams.ENABLE_CLICKLENGTH) != 0);
		for (int i = 0; i < 2; i++) {
			band1Freq[i].setText(String.format("%.0f",
					clickTypeParams.band1Freq[i]));
			band2Freq[i].setText(String.format("%.0f",
					clickTypeParams.band2Freq[i]));
			band1Energy[i].setText(String.format("%.0f",
					clickTypeParams.band1Energy[i]));
			band2Energy[i].setText(String.format("%.0f",
					clickTypeParams.band2Energy[i]));
			peakFrequencySearch[i].setText(String.format("%.0f",
					clickTypeParams.peakFrequencySearch[i]));
			peakFrequencyRange[i].setText(String.format("%.0f",
					clickTypeParams.peakFrequencyRange[i]));
			peakWidth[i].setText(String.format("%.0f",
					clickTypeParams.peakWidth[i]));
			clickLength[i].setText(String.format("%.2f",
					clickTypeParams.clickLength[i]));
			meanSelRange[i].setText(String.format("%.0f",clickTypeParams.meanSelRange[i]));
			meanSumRange[i].setText(String.format("%.0f",clickTypeParams.meanSumRange[i]));
		}
		bandEnergyDifference.setText(String.format("%.1f",
				clickTypeParams.bandEnergyDifference));
		widthEnergyFraction.setText(String.format("%.1f",
				clickTypeParams.widthEnergyFraction));
		lengthEnergyFraction.setText(String.format("%.1f",
				clickTypeParams.lengthEnergyFraction));
//		symbolTypeButton.setIcon(clickTypeParams.symbol);
		symbolPanel.setSymbol(clickTypeParams.symbol);
		
        /* add the alarm list - only do this here, when the dialog is
         * first created
         */
        clickTypeDialog.addAlarmList();
        alarmChooser.setSelectedItem(clickTypeParams.getAlarm());
        maxTime.setText(String.format("%d", clickTypeParams.getMaxTime()));

		energyPanel.enableControls();
		peakPanel.enableControls();
		meanFreqPanel.enableControls();
		lengthPanel.enableControls();
	}

    /**
     * Load the {@link ClickTypeParams} object with the current values in the dialog
     *
     * @return success or failure of the method
     */
	@Override
	public boolean getParams() {

		clickTypeParams.whichSelections = 0;
		if (enableBoxes[0].isSelected()) clickTypeParams.whichSelections |= ClickTypeParams.ENABLE_ENERGYBAND;
		if (enableBoxes[1].isSelected()) clickTypeParams.whichSelections |= ClickTypeParams.ENABLE_PEAKFREQPOS;
		if (enableBoxes[2].isSelected()) clickTypeParams.whichSelections |= ClickTypeParams.ENABLE_PEAKFREQWIDTH;
		if (enableBoxes[3].isSelected()) clickTypeParams.whichSelections |= ClickTypeParams.ENABLE_MEANFREQUENCY;
		if (enableBoxes[4].isSelected()) clickTypeParams.whichSelections |= ClickTypeParams.ENABLE_CLICKLENGTH;
//		enableBoxes[0].setSelected((clickTypeParams.whichSelections & ClickTypeParams.ENABLE_ENERGYBAND) != 0);
//		enableBoxes[1].setSelected((clickTypeParams.whichSelections & ClickTypeParams.ENABLE_PEAKFREQPOS) != 0);
//		enableBoxes[2].setSelected((clickTypeParams.whichSelections & ClickTypeParams.ENABLE_PEAKFREQWIDTH) != 0);
//		enableBoxes[3].setSelected((clickTypeParams.whichSelections & ClickTypeParams.ENABLE_MEANFREQUENCY) != 0);
//		enableBoxes[4].setSelected((clickTypeParams.whichSelections & ClickTypeParams.ENABLE_CLICKLENGTH) != 0);
		
		try {
			clickTypeParams.setName(name.getText());
			clickTypeParams.setSpeciesCode(Integer.valueOf(code.getText()));
			for (int i = 0; i < 2; i++) {
				clickTypeParams.band1Freq[i] = Double.valueOf(band1Freq[i]
						.getText());
				clickTypeParams.band2Freq[i] = Double.valueOf(band2Freq[i]
						.getText());
				clickTypeParams.band1Energy[i] = Double.valueOf(band1Energy[i]
						.getText());
				clickTypeParams.band2Energy[i] = Double.valueOf(band2Energy[i]
						.getText());
				clickTypeParams.peakFrequencySearch[i] = Double
						.valueOf(peakFrequencySearch[i].getText());
				clickTypeParams.peakFrequencyRange[i] = Double
						.valueOf(peakFrequencyRange[i].getText());
				clickTypeParams.peakWidth[i] = Double.valueOf(peakWidth[i]
						.getText());
				clickTypeParams.clickLength[i] = Double.valueOf(clickLength[i]
						.getText());
				clickTypeParams.meanSelRange[i] = Double.valueOf(meanSelRange[i].getText());
				clickTypeParams.meanSumRange[i] = Double.valueOf(meanSumRange[i].getText());
			}
			clickTypeParams.bandEnergyDifference = Double
					.valueOf(bandEnergyDifference.getText());
			clickTypeParams.widthEnergyFraction = Double
					.valueOf(widthEnergyFraction.getText());
			clickTypeParams.lengthEnergyFraction = Double
					.valueOf(lengthEnergyFraction.getText());
//            int alarmIdx = alarmChooser.getSelectedIndex();
//            clickTypeParams.setAlarm(
//                    clickControl.getClickParameters().clickAlarmList.get(alarmIdx));
            clickTypeParams.setAlarm((ClickAlarm) alarmChooser.getSelectedItem());
            clickTypeParams.setMaxTime(Long.parseLong(maxTime.getText()));
		} catch (Exception ex) {
			return false;
		}
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		clickTypeParams = null;
	}

	@Override
	public void restoreDefaultSettings() {
		setParams(new ClickTypeParams(clickTypeParams.getSpeciesCode()));
	}

	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == symbolTypeButton) {
			Point pt = symbolTypeButton.getLocationOnScreen();
			PamSymbol newSymbol = PamSymbolDialog.show(this, clickTypeParams.symbol,
					pt.x, pt.y);
			if (newSymbol != null) {
				clickTypeParams.symbol = newSymbol;
//				symbolTypeButton.setIcon(clickTypeParams.symbol);
				symbolPanel.setSymbol(clickTypeParams.symbol);
			}
			// NewSymbolType((JButton) e.getSource());
		}
		// else if (e.getSource() == symbolColorButton) {
		// returnedParams = null;
		// }
	}

	@Override
	public void restoreDefaultSettingsQ() {
		// need to make a menu - need to xknow the position of the defaults button !
		JPopupMenu menu = new JPopupMenu("Select a Default Species Configuration");
		JMenuItem menuItem;
		for (int i = 0; i < ClickTypeParams.getNUM_STANDARDS(); i++) {
			menuItem = new JMenuItem(ClickTypeParams.getStandardName(i));
			menuItem.addActionListener(new DefaultSpeciesAction(i));
			menu.add(menuItem);
		}
		menu.show(getDefaultButton(), getDefaultButton().getWidth()/2, getDefaultButton().getHeight()/2);
	}
	
	/**
	 * Setup a default species from the list. 
	 * @author Douglas Gillespie
	 *
	 */
	class DefaultSpeciesAction implements ActionListener {

		int iSpecies;
		
		public DefaultSpeciesAction(int species) {
			super();
			iSpecies = species;
		}

		public void actionPerformed(ActionEvent e) {

			ClickTypeParams newParams = basicClickIdParameters.createStandard(iSpecies);
			if (newParams == null) {
				return;
			}
			clickTypeParams = newParams;
			setParams(clickTypeParams);
		}
		
	}

	class TypePanel extends JPanel {

		TypePanel() {
			super();
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			setBorder(new TitledBorder("Click Type"));
			add(new JLabel("Name"));
			add(name = new JTextField(20));
			add(new JLabel("   Code"));
			add(code = new JTextField(3));
			add(symbolPanel = new SymbolPanel());
			add(symbolTypeButton = new JButton("Set Symbol"));
			// add(symbolColorButton = new JButton("Color"));
		}

	}

	class SymbolPanel extends JPanel {
		private PamSymbol symbol;
		SymbolPanel() {
			setPreferredSize(new Dimension(23, 17));
		}
		public void setSymbol(PamSymbol symbol) {
			this.symbol = symbol;
			repaint();
		}
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (symbol != null) {
				symbol.draw(g, new Point(11, 9), 16, 16);
			}
		}
	}
	class EnergyPanel extends JPanel {
		EnergyPanel() {
			super();
			GridBagLayout gb;
			setBorder(new TitledBorder("Energy Bands"));
//			setLayout(new BorderLayout());

			JPanel c = this;
			c.setLayout(gb = new GridBagLayout());
			GridBagConstraints gc = new GridBagConstraints();

			gc.gridy = 0;
			gc.gridx = 0;
			gc.anchor = GridBagConstraints.WEST;
			gc.fill = GridBagConstraints.HORIZONTAL;
			c.add(enableBoxes[0] = new JCheckBox(""), gc);
			enableBoxes[0].addActionListener(new EnergyEnabler());
//			gc.gridy ++;
			gc.gridx = 1;
			c.add(new JLabel("Test Band"), gc);
			gc.gridx = 5;
			c.add(new JLabel("Control Band"), gc);
			// c.add(new J)

			gc.gridy ++;
			gc.gridx = 0;
			c.add(new JLabel("Frequency Range (Hz) "), gc);
			gc.gridx = 1;
			c.add(band1Freq[0] = new JTextField(FREQ_FIELD_WIDTH), gc);
			gc.gridx = 2;
			c.add(new JLabel(" To "), gc);
			gc.gridx = 3;
			c.add(band1Freq[1] = new JTextField(FREQ_FIELD_WIDTH), gc);
			gc.gridx = 5;
			c.add(band2Freq[0] = new JTextField(FREQ_FIELD_WIDTH), gc);
			gc.gridx = 6;
			c.add(new JLabel(" To "), gc);
			gc.gridx = 7;
			c.add(band2Freq[1] = new JTextField(FREQ_FIELD_WIDTH), gc);

			gc.gridy ++;
			gc.gridx = 0;
			c.add(new JLabel("Energy Range \n(dB re 1\u03BCPa) "), gc);
			gc.gridx = 1;
			c.add(band1Energy[0] = new JTextField(FREQ_FIELD_WIDTH), gc);
			gc.gridx = 2;
			c.add(new JLabel(" To "), gc);
			gc.gridx = 3;
			c.add(band1Energy[1] = new JTextField(FREQ_FIELD_WIDTH), gc);
			gc.gridx = 5;
			c.add(band2Energy[0] = new JTextField(FREQ_FIELD_WIDTH), gc);
			gc.gridx = 6;
			c.add(new JLabel(" To "), gc);
			gc.gridx = 7;
			c.add(band2Energy[1] = new JTextField(FREQ_FIELD_WIDTH), gc);
			
			gc.gridy++;
			gc.gridx = 0;
			gc.gridwidth = 5;
			c.add(new JLabel("Minimum energy difference between test and control bands "), gc);
			gc.gridwidth = 1;
			gc.gridx = 5;
			c.add(bandEnergyDifference = new JTextField(5), gc);
			gc.gridwidth = 2;
			gc.gridx = 6;
			c.add(new JLabel(" dB"), gc);
			

//			this.add(BorderLayout.CENTER, c);

//			JPanel s = new JPanel();
//			s.setLayout(new BoxLayout(s, BoxLayout.X_AXIS));
//			s.setBorder(new EmptyBorder(10, 0, 0, 0));
//			s.add(new JLabel(
//							"Minimum energy difference between test and control bands "));
//			s.add(bandEnergyDifference = new JTextField(5));
//			s.add(new JLabel(" dB"));
//			this.add(BorderLayout.SOUTH, s);

		}
		class EnergyEnabler implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				enableControls();
			}
		}
		
		void enableControls() {
			for (int i = 0; i < this.getComponentCount(); i++) {
				if (getComponent(i) != enableBoxes[0]) {
					if (getComponent(i).getClass() == JLabel.class) continue;
					getComponent(i).setEnabled(enableBoxes[0].isSelected());
				}
			}
		}
	}
	
	class MeanFreqPanel extends JPanel {
		MeanFreqPanel() {
			setBorder(new TitledBorder("Mean Frequency"));
			setLayout(new GridBagLayout());
			GridBagConstraints gc = new GridBagConstraints();
			
			gc.gridx = gc.gridy = 0;
			gc.anchor = GridBagConstraints.WEST;
			add(enableBoxes[3] = new JCheckBox(""), gc);
			enableBoxes[3].addActionListener(new MeanFreqListener() );
			gc.gridx = 1;
			gc.gridwidth = 3;
			gc.anchor = GridBagConstraints.CENTER;
			add(new JLabel("Summation Range (Hz)"), gc);
			gc.gridx = 5;
			gc.gridwidth = 3;
			add(new JLabel("Selection Range (Hz)"), gc);
			
			gc.gridy++;
			gc.gridx = 1;
			gc.gridwidth = 1;
			add(meanSumRange[0] = new JTextField(FREQ_FIELD_WIDTH), gc);
			gc.gridx++;
			add(new JLabel(" To "), gc);
			gc.gridx++;
			add(meanSumRange[1] = new JTextField(FREQ_FIELD_WIDTH), gc);
			gc.gridx++;
			add(new JLabel("                  "), gc);

			gc.gridx = 5;
			add(meanSelRange[0] = new JTextField(FREQ_FIELD_WIDTH), gc);
			gc.gridx++;
			add(new JLabel(" To "), gc);
			gc.gridx++;
			add(meanSelRange[1] = new JTextField(FREQ_FIELD_WIDTH), gc);
			
			
			
			
		}
		class MeanFreqListener implements ActionListener {

			public void actionPerformed(ActionEvent e) {
				enableControls();
			}
			
		}
		
		void enableControls() {
			for (int i = 0; i < 2; i++) {
				meanSumRange[i].setEnabled(enableBoxes[3].isSelected());
				meanSelRange[i].setEnabled(enableBoxes[3].isSelected());
			}
		}
	}

	class PeakPanel extends JPanel {

		PeakPanel() {
			super();
			GridBagLayout gb;
			setBorder(new TitledBorder("Peak Frequency"));
			setLayout(gb = new GridBagLayout());
			GridBagConstraints gc = new GridBagConstraints();


			gc.gridy = 0;
			gc.gridx = 0;
			gc.anchor = GridBagConstraints.WEST;
//			gc.fill = GridBagConstraints.HORIZONTAL;
			add(enableBoxes[1] = new JCheckBox(""), gc);
			gc.gridy = 0;
			gc.gridx = 1;
			gc.gridwidth = 4;
			gc.anchor = GridBagConstraints.CENTER;
			add(new JLabel("Search Range (Hz)"), gc);
			gc.gridx = 6;
			gc.gridwidth = 5;
			add(new JLabel("Peak Frequency Range (Hz)"), gc);

			gc.gridy ++;
			gc.gridx = 1;
			gc.gridwidth = 1;
			add(peakFrequencySearch[0] = new JTextField(FREQ_FIELD_WIDTH), gc);
			gc.gridx = 2;
			add(new JLabel(" To "), gc);
			gc.gridx = 3;
			gc.gridwidth = 2;
			add(peakFrequencySearch[1] = new JTextField(FREQ_FIELD_WIDTH), gc);
			gc.gridx = 4;
			gc.gridwidth = 1;
			add(new JLabel("            "), gc);
			gc.gridx = 6;
//			gc.gridwidth = 2;
			add(peakFrequencyRange[0] = new JTextField(FREQ_FIELD_WIDTH), gc);
			gc.gridx = 7;
			gc.gridwidth = 1;
			add(new JLabel(" To "), gc);
			gc.gridx = 8;
			add(peakFrequencyRange[1] = new JTextField(FREQ_FIELD_WIDTH), gc);

			gc.gridy = 2;
			add(new JLabel(" "), gc);

			gc.gridy = 3;
			gc.gridx = 0;
			gc.anchor = GridBagConstraints.WEST;
			add(enableBoxes[2] = new JCheckBox(""), gc);
			gc.gridx = 6;
			gc.gridwidth = 3;
			add(new JLabel("Peak Width Range (Hz)"), gc);

			gc.gridy = 4;
			gc.gridx = 0;
			gc.gridwidth = 3;
			add(new JLabel("Measure width over "), gc);
			gc.gridx = 3;
			gc.gridwidth = 1;
			add(widthEnergyFraction = new JTextField(4), gc);
			gc.gridx = 4;
			gc.gridwidth = 2;
			gc.fill = GridBagConstraints.HORIZONTAL;
			add(new JLabel(" % total energy    "), gc);
			gc.gridx = 6;
			gc.gridwidth = 1;
			add(peakWidth[0] = new JTextField(FREQ_FIELD_WIDTH), gc);
			gc.gridx = 7;
			add(new JLabel(" To "), gc);
			gc.gridx = 8;
			add(peakWidth[1] = new JTextField(FREQ_FIELD_WIDTH), gc);

			enableBoxes[1].addActionListener(new PeakEnable());
			enableBoxes[2].addActionListener(new PeakEnable());
//			gc.gridy++;
//			for (int i = 0; i <= 8; i++) {
//				gc.gridx = i;
//				add(new JLabel("Col " + i), gc);
//			}
		}
		class PeakEnable implements ActionListener {

			public void actionPerformed(ActionEvent e) {
				enableControls();
			}
			
		}
		void enableControls() {
			peakFrequencySearch[0].setEnabled(enableBoxes[1].isSelected());
			peakFrequencySearch[1].setEnabled(enableBoxes[1].isSelected());
			peakFrequencyRange[0].setEnabled(enableBoxes[1].isSelected());
			peakFrequencyRange[1].setEnabled(enableBoxes[1].isSelected());
			widthEnergyFraction.setEnabled(enableBoxes[2].isSelected());
			peakWidth[0].setEnabled(enableBoxes[2].isSelected());
			peakWidth[1].setEnabled(enableBoxes[2].isSelected());
		}
	}

	class LengthPanel extends JPanel {

		LengthPanel() {
			super();
			// GridBagLayout gb;
			setBorder(new TitledBorder("Click Length"));
			setLayout(new GridBagLayout());
			GridBagConstraints gc = new GridBagConstraints();

			gc.gridy = 0;
			gc.gridy = 0;
			gc.anchor = GridBagConstraints.WEST;
			add(enableBoxes[4] = new JCheckBox(""), gc);
			enableBoxes[4].addActionListener(new EnableLength());
			gc.gridx = 4;
			gc.gridwidth = 3;
			gc.anchor = GridBagConstraints.CENTER;
			add(new JLabel(" Click Length Range (ms) "), gc);

			gc.gridy = 1;
			gc.gridx = 0;
			gc.gridwidth = 1;
			add(new JLabel("Measure length over "), gc);
			gc.gridx = 1;
			add(lengthEnergyFraction = new JTextField(4), gc);
			gc.gridx = 2;
			add(new JLabel(" % total energy "), gc);
			gc.gridx = 3;
			add(new JLabel("            "), gc);
			gc.gridx = 4;
			add(clickLength[0] = new JTextField(FREQ_FIELD_WIDTH), gc);
			gc.gridx = 5;
			add(new JLabel(" to "), gc);
			gc.gridx = 6;
			add(clickLength[1] = new JTextField(FREQ_FIELD_WIDTH), gc);
		}
		class EnableLength implements ActionListener {

			public void actionPerformed(ActionEvent e) {
				enableControls();				
			}
			
		}
		
		void enableControls() {
			lengthEnergyFraction.setEnabled(enableBoxes[4].isSelected());
			clickLength[0].setEnabled(enableBoxes[4].isSelected());
			clickLength[1].setEnabled(enableBoxes[4].isSelected());
		}

	}

    /**
     * Inner class to display the alarm panel
     */
    class AlarmPanel extends JPanel {
        String[] alarmList;

		AlarmPanel() {
			super();
			// GridBagLayout gb;
			setBorder(new TitledBorder("Select Alarm"));
			setLayout(new GridBagLayout());
			GridBagConstraints gc = new GridBagConstraints();

			gc.gridx = 0;
			gc.gridy = 0;
            gc.insets = new Insets(5, 20, 0, 0);
			gc.anchor = GridBagConstraints.NORTHWEST;
//            int alarmIdx = createComboBoxList();
//            alarmChooser = new JComboBox(alarmList);
//            alarmChooser.setSelectedIndex(alarmIdx);
            alarmChooser = new JComboBox();
            add(alarmChooser,gc);
            gc.gridy++;
			add(new JLabel("Max amount of time between detections "), gc);
			gc.gridx++;
            gc.insets.left = 0;
			add(maxTime = new JTextField(6), gc);
			gc.gridx++;
			add(new JLabel(" ms"), gc);
            gc.gridx++;
            gc.weightx = 1.0;
            add(new JLabel("   "), gc);
            gc.gridx = 0;
            gc.insets.left = 20;
            gc.insets.top = 20;
            gc.gridy++;
            gc.gridwidth=4;
            gc.fill = GridBagConstraints.HORIZONTAL;
			add(new JLabel("Note: alarm is enabled/disabled on previous screen"), gc);
        }
    }
}
