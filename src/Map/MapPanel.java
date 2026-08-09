*** Begin Patch
*** Update File: src/Map/MapPanel.java
@@
 		drawConstantOverlays(g);
+
+		// Draw basemap attribution if requested
+		try {
+			if (simpleMapRef != null && simpleMapRef.mapParameters != null && simpleMapRef.mapParameters.showBasemapAttribution
+					&& simpleMapRef.mapParameters.basemapAttribution != null
+					&& simpleMapRef.mapParameters.basemapAttribution.length() > 0) {
+				String attr = simpleMapRef.mapParameters.basemapAttribution;
+				Graphics2D g2 = (Graphics2D) g;
+				int padding = 6;
+				int fmHeight = g2.getFontMetrics().getHeight();
+				int textWidth = g2.getFontMetrics().stringWidth(attr);
+				int x = this.getWidth() - textWidth - padding * 2 - 4;
+				int y = this.getHeight() - fmHeight - padding;
+				// semi-transparent background
+				java.awt.Color bg = new java.awt.Color(0, 0, 0, 100);
+				java.awt.Color fg = new java.awt.Color(255, 255, 255, 220);
+				g2.setColor(bg);
+				g2.fillRect(x, y - 2, textWidth + padding * 2, fmHeight + padding);
+				g2.setColor(fg);
+				g2.drawString(attr, x + padding, y + fmHeight - 6);
+			}
+		} catch (Exception e) {
+			// never let attribution drawing crash the map
+		}
*** End Patch
