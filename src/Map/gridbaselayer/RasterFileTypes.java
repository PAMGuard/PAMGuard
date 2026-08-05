package Map.gridbaselayer;

import java.io.File;

public class RasterFileTypes {
	
	/**
	 * Available file extensions. 
	 */
	public static final String[] fileExtensions = {".nc", ".tif", ".tiff"};
	
	// must match above list of file extensions
	private static final RASTERTYPES[] fileTypes = {RASTERTYPES.NETCDF, RASTERTYPES.GEOTIFF, RASTERTYPES.GEOTIFF};

	/**
	 * Available file types. 
	 */
	public enum RASTERTYPES {NETCDF, GEOTIFF};

	/**
	 * Get the enum file type (NETCDF or GEOTIFF)
	 * @param file
	 * @return
	 */
	public static RASTERTYPES getFileType(File file) {
		if (file == null) {
			return null;
		}
		return getFileType(file.getName());
	}
	
	/**
	 * Get the enum file type (NETCDF or GEOTIFF)
	 * @param file
	 * @return
	 */
	public static RASTERTYPES getFileType(String file) {
		if (file == null) {
			return null;
		}
		file = file.toLowerCase();
		for (int i = 0; i < fileTypes.length; i++) {
			if (file.endsWith(fileExtensions[i])) {
				return fileTypes[i];
			}
		}
		return null;
	}
}
