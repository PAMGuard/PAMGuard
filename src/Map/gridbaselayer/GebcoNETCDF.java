package Map.gridbaselayer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import PamView.ColourArray;
import PamView.PamColors;
import PamView.PamColors.PamColor;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class GebcoNETCDF {

	private String latName = "lat";
	private String lonName = "lon";
	private String eleName = "elevation";

	private NetcdfFile netcdfFile;
	private int nLat, nLon, nEle;
	private double[] latRange, lonRange;
	private double[] elevationRange;
	private MapRasterImage mapImage;

	private GebcoNETCDF(NetcdfFile ncf) {
		this.netcdfFile = ncf;

		boolean ok = true;
		ok &= hasField(latName);
		ok &= hasField(lonName);
		ok &= hasField(eleName);

		if (ok == false) {
			return;
		}


		ncf.findDimension(eleName);
		Dimension latDim = ncf.findDimension(latName);
		nLat = latDim.getLength();
		Dimension lonDim = ncf.findDimension(lonName);
		nLon = lonDim.getLength();

		latRange = getRange(latName);
		lonRange = getRange(lonName);
		elevationRange = extractElevationRange();

	}

	public MapRasterImage getImage(double[] latRange, double[] lonRange, boolean forceRecreate) {
		if (mapImage == null || forceRecreate) {
			int[] latRangeBins = {0, nLat};
			int[] lonRangeBins = {0, nLon};
			/*
			 * Scale the resolution so that the map never gets > 5k*5k
			 */
			int maxDim = Math.max(nLat, nLon);
			int scale = maxDim / 5000 + 1;
			mapImage = createImage(latRangeBins, lonRangeBins, scale);
		}
		return mapImage;
	}

	private double[] getRange(String varName) {
		Variable var = findVariable(varName);
		Dimension dim = netcdfFile.findDimension(varName);
		int n = dim.getLength();
		if (var == null) {
			return null;
		}
		

		double[] range = new double[2];
		
		int[] pos = new int[2];
		try {
			Array varA = var.read();
			Index index = varA.getIndex();
			
			Index aa = index.set(pos[0]);
			range[0] = varA.getDouble(aa);
			pos[0] = n-1;
			range[1] = varA.getDouble(index.set(pos[0]));
		} catch (IOException e) {
			e.printStackTrace();
		}
		catch (Exception e2) {
			e2.printStackTrace();
		}
		return range;
	}

	private double[] extractElevationRange() {
		Variable eleV  = findVariable(eleName);

		try {
			Array ele = eleV.read();
			double[] range = new double[2];
			double val;
//			int iVal;
//			int[] pos = new int[2];

			Index index = ele.getIndex();
			for (int i = 0; i < nLat; i++) {
//				pos[0] = i;
				for (int j = 0; j < nLon; j++) {
//					pos[1] = j;
					val = ele.getDouble(index.set(i,j));
					range[0] = Math.min(range[0], val);
					range[1] = Math.max(range[1], val);
				}
			}
			return range;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * work out a value from an evenly spaced range based on a bin number
	 * @param valueRange
	 * @param bin
	 * @return
	 */
	double binToValue(double[] valueRange, int nBin, double bin) {
		return valueRange[0] + (valueRange[1]-valueRange[0])*(double) bin / (double) (nBin);
	}

	double latBinToValue(double latBin) {
		return binToValue(latRange, nLat, latBin);
	}

	double lonBinToValue(double lonBin) {
		return binToValue(lonRange, nLon, lonBin);
	}

	public MapRasterImage createImage(int[] latRangeBins, int[] lonRangeBins, int hop) {
		ColourArray heightArray = ColourArray.createMultiColouredArray(100, Color.BLACK, Color.lightGray);
		ColourArray depthArray = ColourArray.createMultiColouredArray(100, Color.WHITE, PamColors.getInstance().getColor(PamColor.MAP));
		return createImage(latRangeBins, lonRangeBins, heightArray, depthArray, hop);
	}

	public MapRasterImage createImage(int[] latRangeBins, int[] lonRangeBins, ColourArray heightColours, ColourArray depthColours, int hop) {
		latRangeBins[0] = Math.max(latRangeBins[0], 0);
		latRangeBins[1] = Math.min(latRangeBins[1], nLat);
		lonRangeBins[0] = Math.max(lonRangeBins[0], 0);
		lonRangeBins[1] = Math.min(lonRangeBins[1], nLon);
		hop = Math.max(1, hop);
		int nLatBins = (latRangeBins[1]-latRangeBins[0])/hop;
		int nLonBins = (lonRangeBins[1]-lonRangeBins[0])/hop;
		if (nLatBins <= 0 || nLonBins < 0) {
			return null;
		}
		BufferedImage image = new BufferedImage(nLonBins, nLatBins, BufferedImage.TYPE_3BYTE_BGR);
		/*
		 *  need to get the maxima and minima of the elevation data for scaling.  
		 */
		Variable eleV = findVariable(eleName);
		double maxDepth = -elevationRange[0], maxHeight = elevationRange[1];
		double val;
		int iVal;
		int[] pos = new int[2];
		try {
			Array ele = eleV.read();
			Index index = ele.getIndex();
			double hRat = 300;
			int nH = heightColours.getNumbColours();
			int nD = depthColours.getNumbColours();
			double ah = (nH-1)/Math.log(hRat);
			double bh = 1.-ah*Math.log(maxHeight/hRat);
			double ad = (nD-1)/Math.log(hRat);
			double bd = 1.-ad*Math.log(maxDepth/hRat);
			for (int i = latRangeBins[0], ii = 0; i < latRangeBins[1] && ii < image.getHeight(); i+= hop, ii++) {
				pos[0] = i;
				for (int j = lonRangeBins[0], ji = 0; j < lonRangeBins[1] && ji < image.getWidth(); j+= hop, ji++) {
					pos[1] = j;
					val = ele.getDouble(index.set(i,j));
					ColourArray colours;
					if (val >= 0) {
						//						iVal = (int) (99*Math.log10(val+1)/Math.log10(maxHeight));
						iVal = (int) (ah*Math.log(val+1) + bh);
						colours = heightColours;
					}
					else {
						//						iVal = (int) (99*Math.log10(-val)/Math.log10(maxDepth));
						iVal = (int) (ad*Math.log(-val) + bd);
						colours = depthColours;
					}
					iVal = Math.max(0, Math.min(colours.getNumbColours()-1, iVal));
					Color col = colours.getColour(iVal);
					image.setRGB(ji, image.getHeight()-ii-1, col.getRGB());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		double[] latRange = new double[2];
		double[] lonRange = new double[2];
		double[] edges = {-.5, +.5};
		for (int i = 0; i < 2; i++) {
			latRange[i] = latBinToValue(latRangeBins[i]+edges[i]);
			lonRange[i] = lonBinToValue(lonRangeBins[i]+edges[i]);
		}

		return new MapRasterImage(latRange, lonRange, image);
	}

	/**
	 * Find variable with the given name. 
	 * @param varName Variable name
	 * @return Variable or null
	 */
	private Variable findVariable(String varName) {
		return  netcdfFile.findVariable(varName);
		//		VariableIterator varIter = netcdfFile.findVariable(varName);
		//		while (varIter.hasNext() ) {
		//			Variable var = varIter.next();
		//			if (var.getName().equalsIgnoreCase(varName)) {
		//				return var;
		//			}
		//		}
		//		return null;
	}

	private boolean hasField(String fieldName) {

		if (netcdfFile.findVariable(fieldName)!=null) {
			return true;
		}
		System.out.printf("Map file %s has no field \"%s\"\n", netcdfFile.getTitle(), fieldName);
		return false;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		netcdfFile.close();
	}

	public static GebcoNETCDF makeGebcoNCDFFile(String fileName) {
		if (fileName == null || fileName.length() == 0) {
			return null;
		}
		File file = new File(fileName);
		return makeGebcoNCDFFile(file);
	}

	public static GebcoNETCDF makeGebcoNCDFFile(File ncFile) {
		if (ncFile.exists() == false) {
			System.out.println("Bathymetry file " + ncFile.getAbsolutePath() + " cannot be found");
			return null;
		}
		NetcdfFile ncf;
		try {
			ncf = NetcdfFile.open(ncFile.getPath());
			
			GebcoNETCDF gebcoRaster = new GebcoNETCDF(ncf);
			//			ncf.close();
			return gebcoRaster;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * @return the nLat
	 */
	protected int getnLat() {
		return nLat;
	}

	/**
	 * @return the nLon
	 */
	protected int getnLon() {
		return nLon;
	}

	/**
	 * @return the latRange
	 */
	protected double[] getLatRange() {
		return latRange;
	}

	/**
	 * @return the lonRange
	 */
	protected double[] getLonRange() {
		return lonRange;
	}

	/**
	 * @return the elevationRange
	 */
	protected double[] getElevationRange() {
		return elevationRange;
	}


}
