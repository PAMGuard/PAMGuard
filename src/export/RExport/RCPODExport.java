package export.RExport;

import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.Vector;
import org.renjin.sexp.ListVector.NamedBuilder;

import PamguardMVC.PamDataUnit;
import PamguardMVC.RawDataHolder;
import cpod.CPODClick;


public class RCPODExport extends RDataUnitExport<PamDataUnit> {

	@Override
	public NamedBuilder addDetectionSpecificFields(NamedBuilder rData, PamDataUnit dataUnit, int index) {
		// add click detection specific fields to rData units. 
		//the waveform
//		DoubleArrayVector[] wavefrom = new DoubleArrayVector[dataUnit.getWaveLength()]; 
//		for (int i = 0; i<dataUnit.getWaveLength();i++) {
//			wavefrom[i] = new DoubleArrayVector(dataUnit.getWaveData()[i]);
//		}
		
		
		CPODClick dataholder = (CPODClick) dataUnit;
		
		if (dataholder==null) return null; 


		//add the raw wave data 
		if (dataholder.getWaveData()!=null && dataholder.getWaveData().length>0) {
			int nbins =dataholder.getWaveData().length*dataholder.getWaveData()[0].length;
			int n=0;
			double[] concatWaveform  = new double[nbins];
			//System.out.println("Number of bins: " + nbins);
			for (int i=0; i<dataholder.getWaveData().length; i++) {
				for (int j=0; j<dataholder.getWaveData()[i].length; j++) {
//					System.out.println("Current: " + i + " "+ j 
//							+ " nchan: " + dataUnit.getNChan() + "  wave size: " 
//							+ dataUnit.getWaveLength() +"len concat: " + concatWaveform.length);
					concatWaveform[n++] = dataholder.getWaveData()[i][j];
				}
			}

			Vector newMatrix = DoubleArrayVector.newMatrix(concatWaveform, dataholder.getWaveData()[0].length, dataholder.getWaveData().length); 
			rData.add("wave", newMatrix); 
		}
		else {
			rData.add("wave", DoubleArrayVector.newMatrix(new double[] {}, 0, 0)); 
		}
		
		
		rData.add("durationmillis", dataUnit.getDurationInMilliseconds()); 

//		Matrix duration = Mat5.newScalar(fpodClick.getDurationInMilliseconds()); 
		
		rData.add("numcycles", dataholder.getnCyc()); 
		rData.add("bandwidth", dataholder.getBw()*1000.); 
		rData.add("peakfreq", dataholder.getkHz()*1000.); 
		rData.add("endfreq", dataholder.getEndF()); 
		rData.add("SPL", dataholder.getSpl()); 
		rData.add("slope", dataholder.getSlope()); 
		
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
