*** Begin Patch
*** Update File: src/Map/MapParametersDialog.java
@@
     private void setParams() {
@@
-        filePanel.setMapFile(mapParameters.mapFile);
-        basemapTemplate.setText(mapParameters.basemapUrlTemplate == null ? "" : mapParameters.basemapUrlTemplate);
+        filePanel.setMapFile(mapParameters.mapFile);
+        basemapTemplate.setText(mapParameters.basemapUrlTemplate == null ? "" : mapParameters.basemapUrlTemplate);
+        basemapAttributionValue = mapParameters.basemapAttribution == null ? "" : mapParameters.basemapAttribution;
+        showAttributionCheckbox.setSelected(mapParameters.showBasemapAttribution);
+        // if the current template matches one of the presets select it
+        int sel = -1;
+        for (int i = 0; i < presetTemplatesList.size(); i++) {
+            if (presetTemplatesList.get(i).equals(mapParameters.basemapUrlTemplate)) {
+                sel = i;
+                break;
+            }
+        }
+        if (sel >= 0) basemapPresets.setSelectedIndex(sel);
@@
     public boolean getParams() {
@@
-        // basemap template
-        mapParameters.basemapUrlTemplate = basemapTemplate.getText().trim();
+        // basemap template and attribution
+        mapParameters.basemapUrlTemplate = basemapTemplate.getText().trim();
+        mapParameters.basemapAttribution = basemapAttributionValue = (basemapAttributionValue == null ? "" : basemapAttributionValue);
+        // if the user toggled show attribution checkbox, update flag
+        mapParameters.showBasemapAttribution = showAttributionCheckbox.isSelected();
*** End Patch
