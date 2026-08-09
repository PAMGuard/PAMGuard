*** Begin Patch
*** Update File: src/Map/MapParametersDialog.java
@@
     private JTextField trackShowtime, dataKeepTime, dataShowTime;
+    private JTextField basemapTemplate;
@@
             constraints.gridx = 0;
             constraints.gridy ++;
             constraints.anchor = GridBagConstraints.LINE_START;
             addComponent(this,keepShipCentred, constraints);
             constraints.gridx += constraints.gridwidth;    
             addComponent(this,headingUp, constraints);
             constraints.gridy ++;
             constraints.gridx = 0;
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
@@
     private void setParams() {
@@
         filePanel.setMapFile(mapParameters.mapFile);
+        basemapTemplate.setText(mapParameters.basemapUrlTemplate == null ? "" : mapParameters.basemapUrlTemplate);
@@
         enableControls();
     }
@@
         try {
@@
             mapParameters.lockMap = this.staticMapOptionsPanel.isStaticToggledOn();
@@
         }
         catch (Exception Ex) {
             System.out.println("Error setting new map params. Error: "+Ex.getMessage());
             return false;
         }
+        // basemap template
+        mapParameters.basemapUrlTemplate = basemapTemplate.getText().trim();
         mapParameters.mapFile = filePanel.getMapFile();
*** End Patch
