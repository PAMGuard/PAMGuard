@@
 public class MapParameters implements Serializable, Cloneable, ManagedParameters {
@@
     public int symbolSize = Hydrophone.DefaultSymbolSize;
@@
     public String effortDataSource;
+
+    /**
+     * Basemap URL template for XYZ tiles. Example:
+     * https://a.tile.openstreetmap.org/{z}/{x}/{y}.png
+     * If empty, no XYZ basemap will be requested.
+     */
+    public String basemapUrlTemplate = "";
*** End Patch
