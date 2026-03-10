package export.RExport;

import org.jamdev.jdl4pam.utils.DLMatFile;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.Vector;
import org.renjin.sexp.ListVector.NamedBuilder;
import org.renjin.sexp.StringArrayVector;

import PamguardMVC.PamDataBlock;
import export.MLExport.MLNoiseExport;
import noiseMonitor.NoiseDataBlock;
import noiseMonitor.NoiseDataUnit;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.types.Cell;
import us.hebi.matlab.mat.types.Matrix;
import us.hebi.matlab.mat.types.Struct;


/**
 * Export noise data to RData
 * @author Jamie Macaulay
 *
 */
public class RNoiseExport  extends RDataUnitExport<NoiseDataUnit> {

	@Override
	public NamedBuilder addDetectionSpecificFields(NamedBuilder rData, NoiseDataUnit noiseBandData, int index) {
		
		Vector newMatrix = doubleArr2R(noiseBandData.getNoiseBandData()); 
		rData.add("noise", newMatrix); 
	
		rData.add("nMeasures", noiseBandData.getNoiseBandData()[0].length); 
		rData.add("nBand",noiseBandData.getNoiseBandData().length); 

		return rData;
	}
	
	@Override
	protected NamedBuilder detectionHeader(PamDataBlock pamDataBlock) {
		ListVector.NamedBuilder rData = new ListVector.NamedBuilder();
		
		//now add some data specific to the noise data block.
		
		NoiseDataBlock noiseBandDataBlock = (NoiseDataBlock) pamDataBlock; 
		
		Struct struct = Mat5.newStruct(1, 1);
		
		Vector hiEdgesR =  new DoubleArrayVector(noiseBandDataBlock.getBandHiEdges()); 
		Vector loEdgesR =  new DoubleArrayVector(noiseBandDataBlock.getBandLoEdges()); 

		rData.add("hiEdges",hiEdgesR);
		rData.add("loEdges",loEdgesR);
		
		// Get the name of the measures
		String[] strings = MLNoiseExport.getMeasureNames( noiseBandDataBlock);
		
		// Create a StringArrayVector
		StringArrayVector stringVector = new StringArrayVector(strings);

		// Add to rData
		rData.add("noise_descriptors", stringVector);
		
		return rData; 
	}
	
	@Override
	public Class<?> getUnitClass() {
		return NoiseDataUnit.class;
	}
	

	

	@Override
	public String getName() {
		return "noise_band_data";
	}
}
