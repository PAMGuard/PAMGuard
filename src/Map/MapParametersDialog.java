*** Begin Patch
*** Update File: src/Map/MapParametersDialog.java
@@
 public class MapParametersDialog extends PamDialog {
@@
     private JTextField trackShowtime, dataKeepTime, dataShowTime;
     private JTextField basemapTemplate;
+    private JComboBox<String> basemapPresets;
+    private JButton clearBasemapButton;
+    private JButton clearTileCacheButton;
+    private JCheckBox showAttributionCheckbox;
+    private String basemapAttributionValue = "";
@@
     class OptionsPanel extends JPanel {
         public OptionsPanel() {
@@
             addComponent(this, showSurface, constraints);
+
+            // Basemap URL template
+            constraints.gridy++;
+            constraints.gridx = 0;
+            constraints.gridwidth = 1;
+            addComponent(this, new JLabel("Basemap (XYZ template): ", SwingConstants.RIGHT), constraints);
+            constraints.gridx++;
+            constraints.gridwidth = 2;
+            addComponent(this, basemapTemplate = new JTextField(40), constraints);
+
+            // presets and controls
+            final String[] presetNames = new String[] { "None", "OpenStreetMap", "Carto Positron", "Stamen Toner" };
+            final String[] presetTemplates = new String[] {
+                    "",
+                    "https://a.tile.openstreetmap.org/{z}/{x}/{y}.png",
+                    "https://a.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png",
+                    "http://tile.stamen.com/toner/{z}/{x}/{y}.png"
+            };
+            final String[] presetAttributions = new String[] {
+                    "",
+                    "© OpenStreetMap contributors",
+                    "© CARTO",
+                    "© Stamen Design"
+            };
+
+            constraints.gridy++;
+            constraints.gridx = 0;
+            addComponent(this, new JLabel("Presets", SwingConstants.RIGHT), constraints);
+            constraints.gridx++;
+            addComponent(this, basemapPresets = new JComboBox<String>(presetNames), constraints);
+            constraints.gridx += 2;
+            addComponent(this, clearBasemapButton = new JButton("Disable basemap"), constraints);
+
+            constraints.gridy++;
+            constraints.gridx = 1;
+            addComponent(this, showAttributionCheckbox = new JCheckBox("Show attribution"), constraints);
+            constraints.gridx++;
+            addComponent(this, clearTileCacheButton = new JButton("Clear tile cache"), constraints);
+
+            basemapPresets.addActionListener(new ActionListener() {
+                @Override
+                public void actionPerformed(ActionEvent e) {
+                    int idx = basemapPresets.getSelectedIndex();
+                    if (idx >= 0 && idx < presetTemplates.length) {
+                        basemapTemplate.setText(presetTemplates[idx]);
+                        basemapAttributionValue = presetAttributions[idx];
+                        showAttributionCheckbox.setSelected(basemapAttributionValue != null && basemapAttributionValue.length() > 0);
+                    }
+                }
+            });
+
+            clearBasemapButton.addActionListener(new ActionListener() {
+                @Override
+                public void actionPerformed(ActionEvent e) {
+                    basemapTemplate.setText("");
+                    basemapAttributionValue = "";
+                    showAttributionCheckbox.setSelected(false);
+                    basemapPresets.setSelectedIndex(0);
+                }
+            });
+
+            clearTileCacheButton.addActionListener(new ActionListener() {
+                @Override
+                public void actionPerformed(ActionEvent e) {
+                    // clear cache
+                    try {
+                        Map.gridbaselayer.XyzTileLayer.clearCache();
+                        showWarning("Tile cache cleared");
+                    } catch (Exception ex) {
+                        showWarning("Failed to clear tile cache: " + ex.getMessage());
+                    }
+                }
+            });
*** End Patch
