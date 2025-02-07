package export.RExport;

import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.Vector;

import PamguardMVC.PamDataUnit;
import PamguardMVC.RawDataHolder;

import org.renjin.sexp.ListVector.NamedBuilder;

import clickDetector.ClickDetection;
import export.MLExport.MLDataUnitExport;
import pamMaths.PamVector;

public class RRawExport extends RDataUnitExport<PamDataUnit<?,?>> {

	@Override
	public NamedBuilder addDetectionSpecificFields(NamedBuilder rData, PamDataUnit dataUnit, int index) {
		// add click detection specific fields to rData units. 
		//the waveform
//		DoubleArrayVector[] wavefrom = new DoubleArrayVector[dataUnit.getWaveLength()]; 
//		for (int i = 0; i<dataUnit.getWaveLength();i++) {
//			wavefrom[i] = new DoubleArrayVector(dataUnit.getWaveData()[i]);
//		}
		
		
		RawDataHolder dataholder = (RawDataHolder) dataUnit;
		
		if (dataholder==null) return null; 


		//add the raw wave data 
		if (dataholder.getWaveData()!=null && dataholder.getWaveData().length>0) {
			Vector newMatrix = doubleArr2R(dataholder.getWaveData()); 
			rData.add("wave", newMatrix); 
		}
		
		
		rData.add("duration", dataUnit.getSampleDuration()); 
		
		//time delay stuff.
		if (dataUnit.getLocalisation()!=null) {
			//bearing angles 
			if (dataUnit.getLocalisation().getRealWorldVectors() != null) {
				
				double[] angles = MLDataUnitExport.realWordlVec2Angles(dataUnit);
							
				rData.add("angles", new DoubleArrayVector(angles));
				
			}
			else {
				rData.add("angles", new DoubleArrayVector(0.));
			}
			//bearing angles 
			rData.add("angles", dataUnit.getLocalisation().getRealWorldVectors() == null ? new DoubleArrayVector(0.) :  new DoubleArrayVector(dataUnit.getLocalisation().getAngles())); 
			//angle errors 
			rData.add("angleErrors", dataUnit.getLocalisation().getAngleErrors() == null? new DoubleArrayVector(0.) : new DoubleArrayVector(dataUnit.getLocalisation().getAngleErrors())); 
		}
		else {
			//bearing angles 
			rData.add("angles",0);
			//angle errors 
			rData.add("angleErrors",0);
		}
		
		return rData;
	}



	@Override
	public Class<?> getUnitClass() {
		return RawDataHolder.class;
	}

	@Override
	public String getName() {
		return "raw_data_units";
	}
	
	

}
