*** Begin Patch
*** Update File: src/Map/gridbaselayer/XyzTileLayer.java
@@
     }
+
+    /**
+     * Clear the on-disk tile cache. This deletes all files under ~/.pamguard/tilecache
+     */
+    public static void clearCache() {
+        File cache = new File(System.getProperty("user.home"), ".pamguard/tilecache");
+        if (!cache.exists()) return;
+        deleteRecursive(cache);
+    }
+
+    private static void deleteRecursive(File f) {
+        if (f.isDirectory()) {
+            File[] children = f.listFiles();
+            if (children != null) {
+                for (File c : children) {
+                    deleteRecursive(c);
+                }
+            }
+        }
+        f.delete();
+    }
*** End Patch
