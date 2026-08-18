package PamController.pamWizard.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
 * Every species group is shown as an icon beneath the description, with the ones
 * the selected configuration targets picked out in black and the rest greyed, so
 * that what a configuration is <i>not</i> for is as clear as what it is for.
 *
 * @author Jamie Macaulay
 */
public class ConfigSelectionCard extends PamWizardCard<ConfigWizardData> {

	private static final long serialVersionUID = 1L;

	private static final int ICON_SIZE = 32;

	private static final String ALL_GROUPS = "All species";

	/** Colour of a species group the selected configuration targets. */
	private static final Color ACTIVE_TINT = Color.BLACK;

	/** Colour of a species group the selected configuration does not target. */
	private static final Color MUTED_TINT = new Color(0xB4, 0xB4, 0xB4);

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
	 * what it is not.
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

		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScroller, createDetailPanel());
		split.setDividerLocation(300);
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
	 */
	private JPanel createSpeciesIconPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
		for (ConfigSpeciesGroup group : ConfigSpeciesGroup.values()) {
			JLabel label = new JLabel(SpeciesIconFactory.getInstance()
					.getSwingIcon(group, ICON_SIZE, MUTED_TINT));
			label.setToolTipText(group.getGroupName());
			speciesIcons.put(group, label);
			panel.add(label);
		}
		return panel;
	}

	/**
	 * The air / water chooser shown under the icons.
	 */
	private JPanel createMediumPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
		panel.add(mediumLabel);
		panel.add(mediumChooser);
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
		showSpeciesIcons(config);
		showMediumChoice(config);

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
	 * Pick out the species groups the configuration targets and grey the rest.
	 *
	 * @param config the selected configuration, or null if none is selected.
	 */
	private void showSpeciesIcons(PamAutoConfig config) {
		Set<ConfigSpeciesGroup> targeted = new LinkedHashSet<>();
		if (config instanceof FileConfigAutoConfig) {
			targeted.addAll(((FileConfigAutoConfig) config).getDescription().getGroups());
		}
		for (Map.Entry<ConfigSpeciesGroup, JLabel> entry : speciesIcons.entrySet()) {
			ConfigSpeciesGroup group = entry.getKey();
			boolean active = targeted.contains(group);
			entry.getValue().setIcon(SpeciesIconFactory.getInstance()
					.getSwingIcon(group, ICON_SIZE, active ? ACTIVE_TINT : MUTED_TINT));
			entry.getValue().setToolTipText(active
					? group.getGroupName() + " - targeted by this configuration"
					: group.getGroupName() + " - not targeted by this configuration");
		}
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
