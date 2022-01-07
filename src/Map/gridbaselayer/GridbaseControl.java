package Map.gridbaselayer;


import java.io.Serializable;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;

public class GridbaseControl extends PamControlledUnit implements PamSettings {

	public static final String unitType = "Gridded Bathymetry";
//	private GridDataBlock gridDataBlock;
	
	private GridbaseParameters gridbaseParameters = new GridbaseParameters();
//	private BufferedImage totalImage;
//	private double[] latArray, lonArray;
//	private double maxHeight, maxDepth;
//	
//	private GridDataUnit gridDataUnit;
//	
//	private ColourArray depthArray = ColourArray.createMultiColouredArray(100, Color.WHITE, Color.BLUE, new Color(0,0,64));
//	private ColourArray heightArray = ColourArray.createMultiColouredArray(100, Color.BLACK, Color.GRAY);
	private GebcoNETCDF netCDFFile;
	
	private GridSwingPainter swingPainter;
	
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
//		GriddedProcess gridProcess = new GriddedProcess(this);
//		addPamProcess(gridProcess);
//		gridDataBlock = new GridDataBlock(gridProcess);
////		gridProcess.addOutputDataBlock(gridDataBlock);
//		gridDataBlock.setOverlayDraw(new GridOverlayDraw(null));
		PamSettingManager.getInstance().registerSettings(this);
		loadMapData();
	}

	public void loadMapData() {
//		long t1 = System.currentTimeMillis();
		netCDFFile = GebcoNETCDF.makeGebcoNCDFFile(gridbaseParameters.netCDFFile);
	}
	
	
//		try {
//			loadCDFMap();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (Exception e) {
//			System.out.println("Error loading bathymetry data from " + gridbaseParameters.netCDFFile);
//		}
//		t1 = System.currentTimeMillis()-t1;
//		Debug.out.printf("Grid map load took %d milliseconds\n", t1);
//		
//	}

//	private void loadCDFMap() throws IOException {
//
//		gridDataBlock.clearAll();
//		
//		if (gridbaseParameters.netCDFFile == null || gridbaseParameters.netCDFFile.length() == 0) {
//			return;
//		}
//		
//		java.io.File file = new File(gridbaseParameters.netCDFFile);
//		if (file.exists() == false) {
//			System.out.println("Bathymetry file " + gridbaseParameters.netCDFFile + " cannot be found");
//		}
//		
//		Color mapCol = PamColors.getInstance().getColor(PamColor.MAP);
//		depthArray = ColourArray.createMultiColouredArray(100, Color.WHITE, mapCol);
//		
//		NetcdfFile ncf = new NetcdfFile(file, true);
//		DimensionSet dim = ncf.getDimensions();
//		VariableIterator varIter = ncf.iterator();
////		MultiArray elevation=null; 
//				MultiArray lat=null, lon=null;
//		maxHeight = maxDepth = 0;
//		while (varIter.hasNext()) {
//			Variable var = varIter.next();
//			int[] lengths = var.getLengths();
//			switch (var.getName()) {
//			case "elevation":
//				int[] o = {0,0};
//				Debug.out.printf("Map %s size is %dx%d\n", var.getName(), lengths[0], lengths[1]);
////				elevation = var.copyout(o, lengths);
////				elevation.
////				for (int i = 0; i < lengths[0]; i++) {
////					for (int j = 0; j < lengths[1]; j++) {
////						double val = elevation[i][j];
////					}
////					}
////				Debug.out.println(elevation);
//				totalImage = new BufferedImage(lengths[1], lengths[0], BufferedImage.TYPE_USHORT_555_RGB);
//				int[] pos = new int[2];
//				for (int i = 0; i < lengths[0]; i++) {
//					pos[0] = i;
//					for (int j = 0; j < lengths[1]; j++) {
//						pos[1] = j;
//						double val = var.getDouble(pos);
//						if (val > maxHeight) {
//							maxHeight = val;
//						}
//						if (-val > maxDepth) {
//							maxDepth = -val;
//						}
//					}
//				}
//				double hRat = 300;
//				double ah = 99/Math.log(hRat);
//				double bh = 1.-ah*Math.log(maxHeight/hRat);
//				double ad = ah;
//				double bd = 1.-ad*Math.log(maxDepth/hRat);
//				for (int i = 0; i < lengths[0]; i++) {
//					pos[0] = i;
//					for (int j = 0; j < lengths[1]; j++) {
//						pos[1] = j;
//						double val = var.getDouble(pos);
//						int iVal;
//						Color col;
//						ColourArray colours;
//						if (val >= 0) {
////							iVal = (int) (99*Math.log10(val+1)/Math.log10(maxHeight));
//							iVal = (int) (ah*Math.log(val+1) + bh);
//							colours = heightArray;
//						}
//						else {
////							iVal = (int) (99*Math.log10(-val)/Math.log10(maxDepth));
//							iVal = (int) (ad*Math.log(-val) + bd);
//							colours = depthArray;
//						}
//						iVal = Math.max(0, Math.min(colours.getNumbColours()-1, iVal));
//						col = colours.getColour(iVal);
//						totalImage.setRGB(j, lengths[0]-i-1, col.getRGB());
//					}
//				}
//				break;
//			case "lat":
//				int[] or = {0};
//				lat = var.copyout(or, lengths);
////				Debug.out.println(lat);
//				latArray = (double[]) lat.toArray();
//				break;
//			case "lon":
//				int[] or2 = {0};
//				lon = var.copyout(or2, lengths);
////				Debug.out.println(lon);
//				lonArray = (double[]) lon.toArray();
//				break;
//			}
//		}
//		gridDataUnit = new GridDataUnit(latArray, lonArray, totalImage, null);
//		gridDataBlock.addPamData(gridDataUnit);
//		
////		
////		Debug.out.println(ncf.toString());
////		AttributeSet attributes = ncf.getAttributes();
////		AttributeIterator atiter = attributes.iterator();
////		Attribute att;
////		while (atiter.hasNext()) {
////			att = atiter.next();
////			Debug.out.println(att.toString());
////		}
//		
//	}

//	private class GriddedProcess extends PamProcess {
//
//		public GriddedProcess(PamControlledUnit pamControlledUnit) {
//			super(pamControlledUnit, null);
//			// TODO Auto-generated constructor stub
//		}
//
//		@Override
//		public void pamStart() {
//			// TODO Auto-generated method stub
//			
//		}
//
//		@Override
//		public void pamStop() {
//			// TODO Auto-generated method stub
//			
//		}
//		
//	}
//
//	/**
//	 * @return the gridDataBlock
//	 */
//	public GridDataBlock getGridDataBlock() {
//		return gridDataBlock;
//	}
//
//	@Override
//	public JMenuItem createDetectionMenu(Frame parentFrame) {
//		JMenuItem menuItem = new JMenuItem(getUnitName() + "file selection ...");
//		menuItem.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				choseFile();
//			}
//		});
//		return menuItem;
//	}
//
//	protected void choseFile() {
//		boolean ans = GridbaseDialog.showDialog(getGuiFrame(), this);
//		if (ans) {
//			loadMapData();
//		}
//	}

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
		if (netCDFFile == null) {
			return null;
		}
		return netCDFFile.getImage(latRange, lonRange, forceRecreate);
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
