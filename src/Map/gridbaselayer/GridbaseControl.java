package Map.gridbaselayer;


import java.io.File;
import java.io.Serializable;

import Map.gridbaselayer.RasterFileTypes.RASTERTYPES;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;

public class GridbaseControl extends PamControlledUnit implements PamSettings {

	public static final String unitType = "Gridded Bathymetry";
	
	private GridbaseParameters gridbaseParameters = new GridbaseParameters();	private GebcoNETCDF netCDFFile;
	
	private GridSwingPainter swingPainter;
	private GeoTiffFile geoTiffFile;
	
	public final static String bodcURL = "https://www.bodc.ac.uk/data/hosted_data_systems/gebco_gridded_bathymetry_data/";
	
/*
 * Data also available at:
 *  Here�s the link to the bathymetry portal shown below http://portal.emodnet-bathymetry.eu/ . 
 *  From our chat the NetCDF version of the tiles likely to be easiest to get into PAMGuard. 
 *  The Mean Sea Level  ESRI ASCII depths also look like an interesting newish addition (the rest all 
 *  likely to be relative to Chart Datum / Lowest Astronomic Tide). 
 */
	/*
	 * Data from
	 * https://www.bodc.ac.uk/data/hosted_data_systems/gebco_gridded_bathymetry_data/
	 */
	
	/**
	 * 
	 * @param unitName
	 */
	public GridbaseControl(String unitName) {
		super(unitType, unitName);

		PamSettingManager.getInstance().registerSettings(this);
		loadMapData();
	}

	public void loadMapData() {
//		long t1 = System.currentTimeMillis();
		RASTERTYPES mapType = RasterFileTypes.getFileType(gridbaseParameters.netCDFFile);
		netCDFFile = null;
		geoTiffFile = null;
		if (mapType == RASTERTYPES.NETCDF) {
			netCDFFile = GebcoNETCDF.makeGebcoNCDFFile(gridbaseParameters.netCDFFile);
		}
		if (mapType == RASTERTYPES.GEOTIFF) {
			geoTiffFile = GeoTiffFile.makeFile(new File(gridbaseParameters.netCDFFile));
		}
	}
	

	@Override
	public Serializable getSettingsReference() {
		return gridbaseParameters;
	}

	@Override
	public long getSettingsVersion() {
		return GridbaseParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		gridbaseParameters = (GridbaseParameters) pamControlledUnitSettings.getSettings();
		return gridbaseParameters != null;
	}

	/**
	 * @return the gridbaseParameters
	 */
	protected GridbaseParameters getGridbaseParameters() {
		return gridbaseParameters;
	}

	/**
	 * @param gridbaseParameters the gridbaseParameters to set
	 */
	protected void setGridbaseParameters(GridbaseParameters gridbaseParameters) {
		this.gridbaseParameters = gridbaseParameters;
		loadMapData();
	}

	public MapRasterImage getImage(double[] latRange, double[] lonRange, boolean forceRecreate) {
		if (netCDFFile != null) {
			return netCDFFile.getImage(latRange, lonRange, forceRecreate);
		}
		if (geoTiffFile != null) {
			return geoTiffFile.getImage();
		}
		return null;
	}
	/**
	 * @return the swingPainter
	 */
	public GridSwingPainter getSwingPainter() {
		if (swingPainter == null) {
			swingPainter = new GridSwingPainter();
		}
		return swingPainter;
	}
}
