package PamController.pamWizard;

import PamController.soundMedium.GlobalMedium.SoundMedium;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Dialog to let users pick a PamAutoConfig, see its description and species,
 * and filter by species and medium (air/water/both).
 */
public class PamAutoConfigDialog extends JDialog {

    private List<PamAutoConfig> allConfigs;
    private DefaultListModel<PamAutoConfig> listModel;
    private JList<PamAutoConfig> configJList;
    private JTextArea descriptionArea;
    private JList<String> speciesJList;
    private JComboBox<String> speciesFilter;
    private JComboBox<String> mediumFilter;
    private PamAutoConfig selectedConfig;

    public PamAutoConfigDialog(Frame owner, List<PamAutoConfig> configs) {
        super(owner, "Select automatic configuration", true);
        this.allConfigs = configs == null ? Collections.emptyList() : new ArrayList<>(configs);
        initComponents();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(8, 8));

        // Top panel: filters
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Species:"));
        speciesFilter = new JComboBox<>();
        filterPanel.add(speciesFilter);
        filterPanel.add(new JLabel("Medium:"));
        mediumFilter = new JComboBox<>(new String[]{"All", "Air", "Water", "Both"});
        filterPanel.add(mediumFilter);
        add(filterPanel, BorderLayout.NORTH);

        // Center: split pane with list on left and details on right
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        listModel = new DefaultListModel<>();
        configJList = new JList<>(listModel);
        configJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        configJList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel lbl = new JLabel(value == null ? "" : value.getConfigName());
            if (isSelected) {
                lbl.setOpaque(true);
                lbl.setBackground(list.getSelectionBackground());
                lbl.setForeground(list.getSelectionForeground());
            }
            return lbl;
        });

        JScrollPane listScroll = new JScrollPane(configJList);
        listScroll.setPreferredSize(new Dimension(260, 300));
        split.setLeftComponent(listScroll);

        // Details
        JPanel details = new JPanel(new BorderLayout(6, 6));
        descriptionArea = new JTextArea();
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setPreferredSize(new Dimension(320, 160));
        details.add(descScroll, BorderLayout.NORTH);

        speciesJList = new JList<>();
        JScrollPane speciesScroll = new JScrollPane(speciesJList);
        speciesScroll.setPreferredSize(new Dimension(320, 120));
        details.add(speciesScroll, BorderLayout.CENTER);

        split.setRightComponent(details);
        split.setDividerLocation(260);
        add(split, BorderLayout.CENTER);

        // Bottom buttons
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okBtn = new JButton("OK");
        JButton cancelBtn = new JButton("Cancel");
        bottom.add(okBtn);
        bottom.add(cancelBtn);
        add(bottom, BorderLayout.SOUTH);

        // Populate filters and list
        populateSpeciesFilter();
        applyFilters();

        // Listeners
        configJList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    PamAutoConfig c = configJList.getSelectedValue();
                    updateDetails(c);
                }
            }
        });

        ActionListener filterAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyFilters();
            }
        };
        speciesFilter.addActionListener(filterAction);
        mediumFilter.addActionListener(filterAction);

        okBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedConfig = configJList.getSelectedValue();
                dispose();
            }
        });

        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedConfig = null;
                dispose();
            }
        });
    }

    private void populateSpeciesFilter() {
        Set<String> speciesSet = new HashSet<>();
        for (PamAutoConfig c : allConfigs) {
            String[] sl = c.getSpeciesList();
            if (sl != null) {
                for (String s : sl) {
                    if (s != null && !s.trim().isEmpty()) speciesSet.add(s);
                }
            }
        }
        List<String> species = new ArrayList<>(speciesSet);
        Collections.sort(species, Comparator.nullsLast(String::compareTo));
        speciesFilter.removeAllItems();
        speciesFilter.addItem("All");
        for (String s : species) speciesFilter.addItem(s);
    }

    private void applyFilters() {
        String speciesSelected = (String) speciesFilter.getSelectedItem();
        String mediumSelected = (String) mediumFilter.getSelectedItem();

        listModel.clear();
        for (PamAutoConfig c : allConfigs) {
            if (!matchesSpeciesFilter(c, speciesSelected)) continue;
            if (!matchesMediumFilter(c, mediumSelected)) continue;
            listModel.addElement(c);
        }
        if (!listModel.isEmpty()) {
            configJList.setSelectedIndex(0);
        } else {
            updateDetails(null);
        }
    }

    private boolean matchesSpeciesFilter(PamAutoConfig c, String speciesSelected) {
        if (speciesSelected == null || "All".equals(speciesSelected)) return true;
        String[] sl = c.getSpeciesList();
        if (sl == null) return false;
        for (String s : sl) {
            if (speciesSelected.equals(s)) return true;
        }
        return false;
    }

    private boolean matchesMediumFilter(PamAutoConfig c, String mediumSelected) {
        if (mediumSelected == null || "All".equals(mediumSelected)) return true;
        SoundMedium gm = c.getGlobalMediumSettings();
        if ("Both".equals(mediumSelected)) {
            return gm == null;
        } else if ("Air".equals(mediumSelected)) {
            return gm == SoundMedium.Air || gm == null;
        } else if ("Water".equals(mediumSelected)) {
            return gm == SoundMedium.Water || gm == null;
        }
        return true;
    }

    private void updateDetails(PamAutoConfig c) {
        if (c == null) {
            descriptionArea.setText("");
            speciesJList.setListData(new String[0]);
            return;
        }
        descriptionArea.setText(c.getConfigDescription() == null ? "" : c.getConfigDescription());
        String[] sl = c.getSpeciesList();
        if (sl == null) sl = new String[0];
        speciesJList.setListData(sl);
    }

    /**
     * Show the dialog (modal) and return the selected config or null.
     */
    public PamAutoConfig showDialog() {
        setVisible(true);
        return selectedConfig;
    }

    // Small demo main to run the dialog standalone for quick testing.
    public static void main(String[] args) {
        // Create a few dummy PamAutoConfig implementations for demo
        List<PamAutoConfig> demo = new ArrayList<>();
        demo.add(new PamAutoConfig() {
            public boolean isConfigValid(PamFileImport importHandler) { return true; }
            public String getConfigDescription() { return "Air config for species A"; }
            public String[] getSpeciesList() { return new String[]{"Species A", "Species B"}; }
            public String getConfigName() { return "Air Config 1"; }
            public SoundMedium getGlobalMediumSettings() { return SoundMedium.Air; }
        });
        demo.add(new PamAutoConfig() {
            public boolean isConfigValid(PamFileImport importHandler) { return true; }
            public String getConfigDescription() { return "Water config only"; }
            public String[] getSpeciesList() { return new String[]{"Species C"}; }
            public String getConfigName() { return "Water Config"; }
            public SoundMedium getGlobalMediumSettings() { return SoundMedium.Water; }
        });
        demo.add(new PamAutoConfig() {
            public boolean isConfigValid(PamFileImport importHandler) { return true; }
            public String getConfigDescription() { return "Both media, many species"; }
            public String[] getSpeciesList() { return new String[]{"Species A", "Species C"}; }
            public String getConfigName() { return "Both Config"; }
            public SoundMedium getGlobalMediumSettings() { return null; }
        });

        SwingUtilities.invokeLater(() -> {
            PamAutoConfigDialog dlg = new PamAutoConfigDialog(null, demo);
            PamAutoConfig sel = dlg.showDialog();
            System.out.println("Selected: " + (sel == null ? "<none>" : sel.getConfigName()));
            System.exit(0);
        });
    }
}