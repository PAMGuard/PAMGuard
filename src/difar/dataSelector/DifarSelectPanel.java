package difar.dataSelector;

import generalDatabase.lookupTables.LookupList;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import PamUtils.PamUtils;
import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;
import PamguardMVC.PamConstants;

public class DifarSelectPanel implements PamDialogPanel {

	private DifarDataSelector difarDataSelector;
	
	private JPanel mainPanel;

	private JTextField minFreq;

	private JTextField maxFreq;

	private JTextField minAmplitude;

	private JTextField minLength;
	
	private JCheckBox[] species;
	
	private JCheckBox[] channel;
	
	private JCheckBox crossBearing;
	
	private JPanel speciesPanel;
	
	private JButton selectAll, selectNone;
	
	
	
	public DifarSelectPanel(DifarDataSelector difarDataSelector) {
		this.difarDataSelector = difarDataSelector;
		
		mainPanel = new JPanel();
		mainPanel.setBorder(new TitledBorder("Show DIFAR data"));
		mainPanel.setLayout(new BorderLayout());
		
		
		// Time-frequency selection options
		JPanel tfPanel = new JPanel();
		tfPanel.setBorder(new TitledBorder("Time-Frequency filters"));
		tfPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		
		tfPanel.add(new JLabel("Min frequency ", JLabel.RIGHT), c);
		c.gridx++;
		tfPanel.add(minFreq = new JTextField(6), c);
		c.gridx++;
		tfPanel.add(new JLabel(" Hz", JLabel.LEFT), c);
		
		c.gridx = 0;
		c.gridy++;
		tfPanel.add(new JLabel("Max frequency ", JLabel.RIGHT), c);
		c.gridx++;
		tfPanel.add(maxFreq = new JTextField(6), c);
		c.gridx++;
		tfPanel.add(new JLabel(" Hz", JLabel.LEFT), c);

		c.gridx = 0;
		c.gridy++;
		tfPanel.add(new JLabel("Min amplitude ", JLabel.RIGHT), c);
		c.gridx++;
		tfPanel.add(minAmplitude = new JTextField(6), c);
		c.gridx++;
		tfPanel.add(new JLabel(" dB", JLabel.LEFT), c);

		c.gridx = 0;
		c.gridy++;
		tfPanel.add(new JLabel("Min	length ", JLabel.RIGHT), c);
		c.gridx++;
		tfPanel.add(minLength = new JTextField(6), c);
		c.gridx++;
		tfPanel.add(new JLabel(" milliseconds", JLabel.LEFT), c);
		c.gridy++;
		mainPanel.add(tfPanel,BorderLayout.NORTH);
		
		// Loop to add all species as options
		speciesPanel = new JPanel();
		speciesPanel.setBorder(new TitledBorder("Classification filters"));
		speciesPanel.setLayout(new GridBagLayout());

		mainPanel.add(speciesPanel,BorderLayout.CENTER);
		c.gridx = 0;
		c.gridy = 0;
		
		updateSpeciesPanel();
		DifarSelectParameters difarSelectParameters =  difarDataSelector.getDifarSelectParameters();
		
		// Loop to add all channels as options
		JPanel channelPanel = new JPanel();
		channelPanel.setBorder(new TitledBorder("Channel filters"));
		channelPanel.setLayout(new GridBagLayout());
		channel = new JCheckBox[difarSelectParameters.numChannels];
		JLabel channelLabel;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		for (int i = 0; i < difarSelectParameters.channelEnabled.length; i++){
			Color chanColor = PamColors.getInstance().getChannelColor(i);
			c.gridx = 0;
			channel[i] = new JCheckBox();
			channel[i].setHorizontalAlignment(SwingConstants.LEFT);
			channelPanel.add(channel[i], c);
			c.gridx++;
			channelLabel = new JLabel("Channel " + i, JLabel.LEFT);
			channelLabel.setForeground(chanColor);
			channelPanel.add(channelLabel, c);
			c.gridx++;
			channelPanel.add(new JLabel(""),c);
			c.gridy++;
		}
		c.gridx = 0;
		crossBearing = new JCheckBox();
		crossBearing.setHorizontalAlignment(SwingConstants.LEFT);
		channelPanel.add(crossBearing,c);
		c.gridx++;
		channelPanel.add(new JLabel("Crosses only", JLabel.LEFT),c);
		c.gridx++;
		channelPanel.add(new JLabel(""),c);
		c.gridy++;
		mainPanel.add(channelPanel,BorderLayout.SOUTH);
		
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		DifarSelectParameters difarSelectParameters = difarDataSelector.getDifarSelectParameters();

		minFreq.setText(String.format("%3.1f", difarSelectParameters.minFreq));
		maxFreq.setText(String.format("%3.1f", difarSelectParameters.maxFreq));
		minAmplitude.setText(String.format("%3.1f", difarSelectParameters.minAmplitude));
		minLength.setText(String.format("%3.1f", difarSelectParameters.minLengthMillis));
		
		for (int i = 0; i < species.length; i++){
			species[i].setSelected(difarSelectParameters.speciesEnabled[i]);
		}
		
		for (int i = 0; i < channel.length; i++){
			channel[i].setSelected(difarSelectParameters.channelEnabled[i]);
		}
		crossBearing.setSelected(difarSelectParameters.showOnlyCrossBearings);
	}

	@Override
	public boolean getParams() {
		DifarSelectParameters difarSelectParameters = difarDataSelector.getDifarSelectParameters().clone();
		try {
			difarSelectParameters.minFreq = Double.valueOf(minFreq.getText());
			difarSelectParameters.maxFreq = Double.valueOf(maxFreq.getText());
			difarSelectParameters.minAmplitude = Double.valueOf(minAmplitude.getText());
			difarSelectParameters.minLengthMillis = Double.valueOf(minLength.getText());
			for (int i = 0; i < species.length; i++){
				difarSelectParameters.speciesEnabled[i] = species[i].isSelected();
			}
		
			for (int i = 0; i < channel.length; i++){
				difarSelectParameters.channelEnabled[i] = channel[i].isSelected();
			}
			difarSelectParameters.showOnlyCrossBearings = crossBearing.isSelected();
		}
		catch (NumberFormatException e) {
			return false;
		}
		difarDataSelector.setDifarSelectParameters(difarSelectParameters);
		return true;
	}

	public void updateSpeciesPanel(){
		GridBagConstraints c = new PamGridBagContraints();

		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;

		speciesPanel.removeAll();
		DifarSelectParameters difarSelectParameters =  difarDataSelector.getDifarSelectParameters();
		species = new JCheckBox[difarSelectParameters.speciesEnabled.length];
		for (int i = 0; i < difarSelectParameters.speciesEnabled.length; i++){
			c.gridx = 0;
			species[i] = new JCheckBox();
			species[i].setHorizontalAlignment(SwingConstants.LEFT);
			speciesPanel.add(species[i], c);
			c.gridx++;
			speciesPanel.add(new JLabel(difarSelectParameters.speciesList.getLookupItem(i).getSymbol(), JLabel.LEFT), c);
			c.gridx++;
			speciesPanel.add(new JLabel(difarSelectParameters.speciesList.getLookupItem(i).getText(), JLabel.LEFT), c);
			c.gridy++;
		}
		c.gridwidth = 2;
		c.weightx = 0.5;
		c.gridx = 0;
		selectAll = new JButton("Select All");
		selectAll.addActionListener(activateAll);
		speciesPanel.add(selectAll, c);
		c.gridx++;
		selectNone = new JButton("Select None");
		selectNone.addActionListener(deactivateAll);
		speciesPanel.add(selectNone, c);
		c.gridx++;
		c.gridy++;
		speciesPanel.invalidate();
	}
	
	ActionListener activateAll = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			for (int i = 0; i < species.length; i++){
				species[i].setSelected(true);
			}
		}
	};

	ActionListener deactivateAll = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			for (int i = 0; i < species.length; i++){
				species[i].setSelected(false);
			}
		}
	};
	
}
