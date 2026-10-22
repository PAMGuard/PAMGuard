package difar.dataSelector;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import PamUtils.PamUtils;
import PamView.PamColors;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;

public class DifarSelectPanel implements PamDialogPanel {

	/**
	 * The most classes listed in one column. More classes go into further
	 * columns, so a long list does not push the dialog off the screen.
	 */
	private static final int MAX_CLASS_ROWS = 10;

	private DifarDataSelector difarDataSelector;

	private JPanel mainPanel;

	private JTextField minFreq;

	private JTextField maxFreq;

	private JTextField minAmplitude;

	private JTextField minLength;

	private JCheckBox[] species;

	private JCheckBox[] channel = new JCheckBox[0];

	/** The channel number each channel checkbox stands for. */
	private int[] channelNumbers = new int[0];

	private JPanel channelPanel;

	private JPanel speciesPanel;

	private JButton selectAll, selectNone;

	private JRadioButton allBearings, triangulatedBearings;

	private JCheckBox hideOnBuoy;

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
		tfPanel.add(new JLabel("Min length ", JLabel.RIGHT), c);
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

		updateSpeciesPanel();
		DifarSelectParameters difarSelectParameters =  difarDataSelector.getDifarSelectParameters();

		// channels are listed when the dialog opens, from the channels in use
		channelPanel = new JPanel();
		channelPanel.setBorder(new TitledBorder("Channel filters"));
		channelPanel.setLayout(new GridBagLayout());
		updateChannelPanel(PamUtils.makeChannelMap(difarSelectParameters.channelEnabled.length));

		// channels and triangulations side by side, to keep the dialog short
		JPanel southPanel = new JPanel(new BorderLayout());
		southPanel.add(channelPanel, BorderLayout.WEST);
		southPanel.add(makeTriangulationPanel(), BorderLayout.CENTER);
		mainPanel.add(southPanel,BorderLayout.SOUTH);

	}

	/**
	 * The choice of which bearings to show, by whether they are part of a
	 * triangulation.
	 */
	private JPanel makeTriangulationPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(new TitledBorder("Triangulations"));
		GridBagConstraints c = new PamGridBagContraints();
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		panel.add(new JLabel("Which bearings to show:"), c);
		c.gridy++;
		panel.add(allBearings = new JRadioButton("All bearings, triangulated or not"), c);
		c.gridy++;
		panel.add(triangulatedBearings = new JRadioButton("Only bearings in a triangulation"), c);
		c.gridy++;
		panel.add(hideOnBuoy = new JCheckBox("Hide bearings in a triangulation on one of its own sonobuoys"), c);
		ButtonGroup group = new ButtonGroup();
		group.add(allBearings);
		group.add(triangulatedBearings);
		allBearings.setToolTipText("Show every bearing that passes the filters above");
		triangulatedBearings.setToolTipText("Hide bearings that are not part of a triangulation");
		hideOnBuoy.setToolTipText("<html>A triangulation is on a sonobuoy when it lies within the on-buoy radius<br>"
				+ "of one of its own sonobuoys, often because one bearing points at another sonobuoy.<br>"
				+ "Its location cannot be trusted. See Triangulation quality in the DIFAR help,<br>"
				+ "under Advanced localisation.</html>");
		return panel;
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
			int number = channelNumbers[i];
			channel[i].setSelected(number >= difarSelectParameters.channelEnabled.length
					|| difarSelectParameters.channelEnabled[number]);
		}
		if (difarSelectParameters.bearingsShown == DifarSelectParameters.BEARINGS_TRIANGULATED) {
			triangulatedBearings.setSelected(true);
		}
		else {
			allBearings.setSelected(true);
		}
		hideOnBuoy.setSelected(difarSelectParameters.hideOnBuoyBearings);
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

			difarSelectParameters.channelEnabled = difarSelectParameters.channelEnabled.clone();
			for (int i = 0; i < channel.length; i++){
				int number = channelNumbers[i];
				if (number < difarSelectParameters.channelEnabled.length) {
					difarSelectParameters.channelEnabled[number] = channel[i].isSelected();
				}
			}
			difarSelectParameters.bearingsShown = triangulatedBearings.isSelected()
					? DifarSelectParameters.BEARINGS_TRIANGULATED : DifarSelectParameters.BEARINGS_ALL;
			difarSelectParameters.hideOnBuoyBearings = hideOnBuoy.isSelected();
		}
		catch (NumberFormatException e) {
			return false;
		}
		difarDataSelector.setDifarSelectParameters(difarSelectParameters);
		return true;
	}

	/**
	 * List a checkbox for each channel in a channel map, labelled with its
	 * channel number.
	 * @param channelMap the channels to list.
	 */
	public void updateChannelPanel(int channelMap) {
		channelPanel.removeAll();
		GridBagConstraints c = new PamGridBagContraints();
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		int n = PamUtils.getNumChannels(channelMap);
		channel = new JCheckBox[n];
		channelNumbers = new int[n];
		for (int i = 0; i < n; i++) {
			int number = PamUtils.getNthChannel(i, channelMap);
			channelNumbers[i] = number;
			c.gridx = 0;
			channel[i] = new JCheckBox();
			channel[i].setHorizontalAlignment(SwingConstants.LEFT);
			channelPanel.add(channel[i], c);
			c.gridx++;
			JLabel channelLabel = new JLabel("Channel " + number, JLabel.LEFT);
			channelLabel.setForeground(PamColors.getInstance().getChannelColor(number));
			channelPanel.add(channelLabel, c);
			c.gridy++;
		}
		channelPanel.revalidate();
		channelPanel.repaint();
	}

	/**
	 * List a checkbox for each class, in columns of at most
	 * {@link #MAX_CLASS_ROWS}, filled top to bottom then left to right.
	 */
	public void updateSpeciesPanel(){
		speciesPanel.removeAll();
		DifarSelectParameters difarSelectParameters =  difarDataSelector.getDifarSelectParameters();
		int n = difarSelectParameters.speciesEnabled.length;
		int nColumns = Math.max(1, (n + MAX_CLASS_ROWS - 1) / MAX_CLASS_ROWS);
		int nRows = Math.max(1, (n + nColumns - 1) / nColumns);

		GridBagConstraints c = new PamGridBagContraints();
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		species = new JCheckBox[n];
		for (int i = 0; i < n; i++){
			int column = i / nRows;
			c.gridy = i % nRows;
			c.gridx = column * 3;
			c.weightx = 0;
			species[i] = new JCheckBox();
			species[i].setHorizontalAlignment(SwingConstants.LEFT);
			speciesPanel.add(species[i], c);
			c.gridx++;
			speciesPanel.add(new JLabel(difarSelectParameters.speciesList.getLookupItem(i).getSymbol(), JLabel.LEFT), c);
			c.gridx++;
			c.weightx = 1;
			speciesPanel.add(new JLabel(difarSelectParameters.speciesList.getLookupItem(i).getText(), JLabel.LEFT), c);
		}

		// the buttons below the list, the first under the first column
		JPanel buttonPanel = new JPanel(new GridBagLayout());
		GridBagConstraints b = new PamGridBagContraints();
		buttonPanel.add(selectAll = new JButton("Select All"), b);
		b.gridx++;
		buttonPanel.add(selectNone = new JButton("Select None"), b);
		selectAll.addActionListener(activateAll);
		selectNone.addActionListener(deactivateAll);
		c.gridx = 0;
		c.gridy = nRows;
		c.gridwidth = nColumns * 3;
		c.weightx = 1;
		c.fill = GridBagConstraints.NONE;
		speciesPanel.add(buttonPanel, c);
		speciesPanel.revalidate();
		speciesPanel.repaint();
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
