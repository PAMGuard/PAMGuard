Modify-PrepareBaseImage: Add call to XyzTileLayer
---
*** Begin Patch
*** Update File: src/Map/MapPanel.java
@@
 	private void prepareBaseImage() {
@@
 		GridbaseControl gridbaseControl = simpleMapRef.getGridBaseControl();
 		if (gridbaseControl != null) {
@@
 		}
+
+		// Draw XYZ tile baselayer if configured (OGIS branch)
+		try {
+			if (simpleMapRef.mapParameters != null && simpleMapRef.mapParameters.basemapUrlTemplate != null
+					&& simpleMapRef.mapParameters.basemapUrlTemplate.length() > 0) {
+				XyzTileLayer tileLayer = new XyzTileLayer(simpleMapRef.mapParameters.basemapUrlTemplate);
+				tileLayer.drawTiles(g, rectProj, this.getWidth(), this.getHeight());
+			}
+		} catch (Exception e) {
+			// don't let basemap failures break the map
+		}
*** End Patch
