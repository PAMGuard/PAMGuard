package PamController.pamWizard.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;

import PamController.pamWizard.PamAutoConfig;
import PamController.pamWizard.SoundFileSummary;
import PamController.pamWizard.configurations.ConfigSpeciesGroup;
import PamController.pamWizard.configurations.ConfigWizardData;
import PamController.pamWizard.configurations.FileConfigAutoConfig;
import PamController.pamWizard.configurations.PamConfigDescription;
import PamController.pamWizard.configurations.PamConfigInspection;
import PamController.pamWizard.configurations.SpeciesIconFactory;
import PamController.pamWizard.configurations.SpeciesTooltips;
import PamController.soundMedium.GlobalMedium.SoundMedium;
import PamView.dialog.PamDialog;
import PamView.wizard.PamWizard;
import PamView.wizard.PamWizardCard;

/**
 * Second page of the import wizard: choose a configuration.
 * <p>
 * Only configurations which can actually be used with the imported files are
 * listed - the filtering by sample rate, channel count and file type has already
 * happened. The species filter here is for narrowing a long list down by hand,
 * not for deciding what is possible.
 * <p>
 * Every species group is shown as an icon beneath the description, all on one
 * row, with the ones the selected configuration targets picked out in black and
 * the rest in a light grey, so that what a configuration is <i>not</i> for is as
 * clear as what it is for. A plain spectrogram detects nothing of its own, so
 * for that the groups picked out are the ones which live in the chosen medium and
 * which the imported files are fast enough to show. Each icon carries a tooltip
 * naming its group and saying why it is or is not picked out (see
 * {@link SpeciesTooltips}), for a reader who does not recognise the silhouette.
 *
 * @author Jamie Macaulay
 */
public class ConfigSelectionCard extends PamWizardCard<ConfigWizardData> {

	private static final long serialVersionUID = 1L;

	private static final int ICON_SIZE = 40;

	/** Horizontal gap between the species icons. */
	private static final int ICON_GAP = 6;

	/**
	 * Width of the configuration list, and so of the split pane's left hand side.
	 * Fixed, so that the divider can be put in the same place as the width the list
	 * was packed at and the detail panel is left with exactly the width it asked
	 * for - enough for the species icons to sit in a single row.
	 */
	private static final int LIST_WIDTH = 300;

	private static final String ALL_GROUPS = "All species";

	/** Colour of a species group the selected configuration targets. */
	private static final Color ACTIVE_TINT = Color.BLACK;

	/** Colour of a species group the selected configuration does not target. */
	private static final Color MUTED_TINT = new Color(0xCC, 0xCC, 0xCC);

	private final DefaultListModel<PamAutoConfig> listModel = new DefaultListModel<>();
	private final JList<PamAutoConfig> configList = new JList<>(listModel);

	private final JComboBox<Object> groupFilter = new JComboBox<>();

	/**
	 * Lets the user say whether the recordings are from air or water. Only enabled
	 * for configurations which work in either - one written for a particular medium
	 * shows that medium, fixed.
	 */
	private final JComboBox<SoundMedium> mediumChooser = new JComboBox<>(SoundMedium.values());

	private final JLabel mediumLabel = new JLabel("Medium:");

	private final JTextArea descriptionArea = new JTextArea(6, 30);
	private final JLabel speciesLabel = new JLabel();
	private final JLabel requirementsLabel = new JLabel();
	private final JLabel modulesLabel = new JLabel();

	/**
	 * One icon per species group, all of them always shown. The groups a
	 * configuration targets are drawn in the normal icon colour and the rest are
	 * greyed, so the reader can see at a glance both what a configuration is for and
	 * what it is not. Only the groups worth drawing are here - see
	 * {@link ConfigSpeciesGroup#getDisplayGroups()}.
	 */
	private final Map<ConfigSpeciesGroup, JLabel> speciesIcons = new LinkedHashMap<>();

	/**
	 * Every configuration on offer, before the group and medium filters are applied.
	 */
	private List<PamAutoConfig> allConfigs = new ArrayList<>();

	private ConfigWizardData wizardData;

	public ConfigSelectionCard(PamWizard pamWizard) {
		super(pamWizard, "Choose a configuration");
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		add(BorderLayout.NORTH, createFilterPanel());

		configList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		configList.setCellRenderer(new ConfigCellRenderer());
		configList.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				showDetails(configList.getSelectedValue());
			}
		});

		JScrollPane listScroller = new JScrollPane(configList);
		/*
		 * Never scroll sideways - a horizontal scrollbar under a short list of names
		 * looks broken. Long names are clipped instead, and the tooltip gives the whole
		 * thing.
		 */
		listScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		listScroller.setPreferredSize(new Dimension(LIST_WIDTH, 260));

		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScroller, createDetailPanel());
		split.setDividerLocation(LIST_WIDTH);
		// any extra width the dialog ends up with goes to the details, not to the list.
		split.setResizeWeight(0);
		add(BorderLayout.CENTER, split);
	}

	private JPanel createFilterPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Show:"));
		panel.add(groupFilter);
		groupFilter.addActionListener(e -> applyFilters());
		return panel;
	}

	/**
	 * The row of species group icons shown under the description.
	 * <p>
	 * A grid rather than a flow, so that the groups always read as one row: a flow
	 * silently wraps the last few icons onto a second line as soon as the panel is
	 * a little narrower than the whole row, which reads as two unrelated sets of
	 * animals rather than one list. The row asks for the width it needs and the
	 * detail panel it sits in passes that request on to the dialog.
	 */
	private JPanel createSpeciesIconPanel() {
		List<ConfigSpeciesGroup> groups = ConfigSpeciesGroup.getDisplayGroups();
		JPanel icons = new JPanel(new GridLayout(1, groups.size(), ICON_GAP, 0));
		for (ConfigSpeciesGroup group : groups) {
			JLabel label = new JLabel(SpeciesIconFactory.getInstance()
					.getSwingIcon(group, ICON_SIZE, MUTED_TINT), JLabel.CENTER);
			label.setToolTipText(SpeciesTooltips.getSwingTip(group, null, 0, null));
			speciesIcons.put(group, label);
			icons.add(label);
		}

		// held to the left so that the icons keep their size in a wider dialog.
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(BorderLayout.WEST, icons);
		panel.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
		return panel;
	}

	/**
	 * The air / water chooser shown under the icons.
	 */
	private JPanel createMediumPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
		panel.add(mediumLabel);
		panel.add(mediumChooser);
		/*
		 * A spectrogram of an air recording has no porpoises in it, so the icons have
		 * to follow the medium as the user changes it.
		 */
		mediumChooser.addActionListener(e -> showSpeciesIcons(configList.getSelectedValue()));
		return panel;
	}

	private JPanel createDetailPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

		descriptionArea.setEditable(false);
		descriptionArea.setLineWrap(true);
		descriptionArea.setWrapStyleWord(true);
		descriptionArea.setOpaque(false);
		descriptionArea.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		JPanel facts = new JPanel(new GridLayout(0, 1, 0, 2));
		facts.add(speciesLabel);
		facts.add(requirementsLabel);
		facts.add(modulesLabel);

		JPanel below = new JPanel(new BorderLayout());
		below.add(BorderLayout.NORTH, facts);
		below.add(BorderLayout.CENTER, createSpeciesIconPanel());
		below.add(BorderLayout.SOUTH, createMediumPanel());

		panel.add(BorderLayout.CENTER, new JScrollPane(descriptionArea));
		panel.add(BorderLayout.SOUTH, below);
		return panel;
	}

	@Override
	public void setParams(ConfigWizardData cardParams) {
		this.wizardData = cardParams;
		this.allConfigs = cardParams.getAvailableConfigs();

		buildFilterOptions();
		applyFilters();

		if (cardParams.getSelectedConfig() != null) {
			configList.setSelectedValue(cardParams.getSelectedConfig(), true);
		}
		else if (!listModel.isEmpty()) {
			configList.setSelectedIndex(0);
		}
	}

	@Override
	public boolean getParams(ConfigWizardData cardParams) {
		PamAutoConfig selected = configList.getSelectedValue();
		if (selected == null) {
			return PamDialog.showWarning(getPamWizard(), "No configuration",
					"Please choose a configuration to continue.");
		}
		/*
		 * A file based configuration has to be readable before there is any point going
		 * on to ask where its data should be stored.
		 */
		if (selected instanceof FileConfigAutoConfig) {
			PamConfigInspection inspection = ((FileConfigAutoConfig) selected).getInspection();
			if (inspection == null || !inspection.isValid()) {
				String reason = (inspection == null) ? "no loader is available" : inspection.getError();
				return PamDialog.showWarning(getPamWizard(), "Configuration cannot be used",
						String.format("\"%s\" cannot be loaded: %s", selected.getConfigName(), reason));
			}
		}
		cardParams.setSelectedConfig(selected);
		cardParams.setMedium((SoundMedium) mediumChooser.getSelectedItem());
		return true;
	}

	/**
	 * Fill the filter combos with only the groups and media actually present, so the
	 * user is never offered a filter which would empty the list.
	 */
	private void buildFilterOptions() {
		Set<ConfigSpeciesGroup> groups = new LinkedHashSet<>();
		for (PamAutoConfig config : allConfigs) {
			if (config instanceof FileConfigAutoConfig) {
				groups.addAll(((FileConfigAutoConfig) config).getDescription().getGroups());
			}
		}

		groupFilter.removeAllItems();
		groupFilter.addItem(ALL_GROUPS);
		for (ConfigSpeciesGroup group : groups) {
			groupFilter.addItem(group);
		}
		groupFilter.setEnabled(groupFilter.getItemCount() > 1);
	}

	/**
	 * Rebuild the list from the current filter settings.
	 */
	private void applyFilters() {
		Object group = groupFilter.getSelectedItem();

		PamAutoConfig previous = configList.getSelectedValue();
		listModel.clear();
		for (PamAutoConfig config : allConfigs) {
			if (matchesGroup(config, group)) {
				listModel.addElement(config);
			}
		}

		if (previous != null && listModel.contains(previous)) {
			configList.setSelectedValue(previous, true);
		}
		else if (!listModel.isEmpty()) {
			configList.setSelectedIndex(0);
		}
		else {
			showDetails(null);
		}
	}

	private boolean matchesGroup(PamAutoConfig config, Object group) {
		if (!(group instanceof ConfigSpeciesGroup)) {
			return true;
		}
		if (!(config instanceof FileConfigAutoConfig)) {
			// the built in configurations are not tied to any species.
			return false;
		}
		return ((FileConfigAutoConfig) config).getDescription().getGroups().contains(group);
	}

	/**
	 * Show what a configuration does in the panel beside the list.
	 */
	private void showDetails(PamAutoConfig config) {
		// the medium first: which species groups are picked out depends on it.
		showMediumChoice(config);
		showSpeciesIcons(config);

		if (config == null) {
			descriptionArea.setText("");
			speciesLabel.setText("");
			requirementsLabel.setText("");
			modulesLabel.setText("");
			return;
		}

		descriptionArea.setText(config.getConfigDescription());
		descriptionArea.setCaretPosition(0);

		String[] species = config.getSpeciesList();
		speciesLabel.setText((species == null || species.length == 0)
				? " " : "Targets: " + String.join(", ", species));

		PamConfigDescription description =
				(config instanceof FileConfigAutoConfig) ? ((FileConfigAutoConfig) config).getDescription() : null;
		if (description == null) {
			requirementsLabel.setText(" ");
			modulesLabel.setText(" ");
			return;
		}

		StringBuilder needs = new StringBuilder("Needs ");
		needs.append(SoundFileSummary.formatRate((float) description.getMinSampleRate())).append(" or above");
		if (description.getMinChannels() > 1) {
			needs.append(", ").append(description.getMinChannels()).append(" channels");
		}
		if (wizardData != null && willDecimate(config, description)) {
			needs.append("; data will be decimated to ")
					.append(SoundFileSummary.formatRate(description.getTargetSampleRate().floatValue()));
		}
		requirementsLabel.setText(needs.toString());

		PamConfigInspection inspection = ((FileConfigAutoConfig) config).getInspection();
		modulesLabel.setText(inspection != null && inspection.isValid()
				? "Creates " + inspection.getModules().size() + " modules"
				: " ");
	}

	/**
	 * Whether choosing this configuration would mean decimating the imported data.
	 */
	private boolean willDecimate(PamAutoConfig config, PamConfigDescription description) {
		SoundFileSummary summary = wizardData.getSoundSummary();
		if (summary == null || !summary.isValid() || description.getTargetSampleRate() == null) {
			return false;
		}
		return summary.getMinSampleRate() > description.getTargetSampleRate();
	}

	/**
	 * Pick out the species groups the configuration targets and grey the rest. For
	 * a configuration with no species of its own that means the groups these
	 * recordings could hold, which depends on the medium showing in the chooser -
	 * so this must run after {@link #showMediumChoice(PamAutoConfig)}.
	 *
	 * @param config the selected configuration, or null if none is selected.
	 */
	private void showSpeciesIcons(PamAutoConfig config) {
		double sampleRate = getSampleRate();
		SoundMedium medium = (SoundMedium) mediumChooser.getSelectedItem();
		Set<ConfigSpeciesGroup> targeted = SpeciesTooltips.getTargetedGroups(config, sampleRate, medium);
		for (Map.Entry<ConfigSpeciesGroup, JLabel> entry : speciesIcons.entrySet()) {
			ConfigSpeciesGroup group = entry.getKey();
			boolean active = targeted.contains(group);
			entry.getValue().setIcon(SpeciesIconFactory.getInstance()
					.getSwingIcon(group, ICON_SIZE, active ? ACTIVE_TINT : MUTED_TINT));
			entry.getValue().setToolTipText(SpeciesTooltips.getSwingTip(group, config, sampleRate, medium));
		}
	}

	/**
	 * The sample rate the imported files were recorded at, which is what decides
	 * the species groups for a configuration with none of its own. The lowest rate
	 * found is used, so that a mixed set of files is not credited with more than
	 * all of it can show.
	 *
	 * @return the sample rate in Hz, or zero if it could not be read.
	 */
	private double getSampleRate() {
		SoundFileSummary summary = (wizardData == null) ? null : wizardData.getSoundSummary();
		return (summary == null || !summary.isValid()) ? 0 : summary.getMinSampleRate();
	}

	/**
	 * Offer the air / water choice, but only where the configuration leaves it open.
	 * A configuration written for a particular medium shows that medium, fixed - a
	 * right whale detector is of no use in air.
	 *
	 * @param config the selected configuration, or null if none is selected.
	 */
	private void showMediumChoice(PamAutoConfig config) {
		SoundMedium fixed = (config == null) ? null : config.getGlobalMediumSettings();
		if (fixed != null) {
			mediumChooser.setSelectedItem(fixed);
			mediumChooser.setEnabled(false);
			mediumChooser.setToolTipText("This configuration is only for use in " + fixed.toString().toLowerCase());
		}
		else {
			if (wizardData != null) {
				mediumChooser.setSelectedItem(wizardData.getMedium());
			}
			mediumChooser.setEnabled(config != null);
			mediumChooser.setToolTipText("Whether these recordings were made in air or in water");
		}
		mediumLabel.setEnabled(mediumChooser.isEnabled());
	}

	/**
	 * Draws each configuration by name. The species icons live under the description
	 * rather than in the list, so that the full set can be shown for whichever
	 * configuration is selected.
	 */
	private class ConfigCellRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index,
				boolean isSelected, boolean cellHasFocus) {
			JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value instanceof PamAutoConfig) {
				PamAutoConfig config = (PamAutoConfig) value;
				label.setText(config.getConfigName());
				label.setToolTipText(config.getConfigName());
				label.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 4));
				label.setFont(label.getFont().deriveFont(isSelected ? Font.BOLD : Font.PLAIN));
			}
			return label;
		}
	}
}
