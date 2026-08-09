@@
     public String effortDataSource;
     
     private static final int defaultMapRange = 10000;
     /**
      * Value to store persistently between runs. 
      */
+    
+    /**
+     * Basemap attribution string to render on the map when a basemap is enabled.
+     */
+    public String basemapAttribution = "";
+
+    /**
+     * Whether to draw the basemap attribution on the map.
+     */
+    public boolean showBasemapAttribution = true;
+
*** End Patch
