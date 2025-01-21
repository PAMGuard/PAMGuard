package export.RExport;

import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.Vector;
import org.renjin.sexp.ListVector.NamedBuilder;

import PamguardMVC.PamDataUnit;
import PamguardMVC.RawDataHolder;
import cpod.CPODClick;
import export.MLExport.MLCPODExport;


public class RCPODExport extends RDataUnitExport<PamDataUnit<?, ?>> {

	@Override
	public NamedBuilder addDetectionSpecificFields(NamedBuilder rData, PamDataUnit dataUnit, int index) {
		// add click detection specific fields to rData units. 
		//the waveform
//		DoubleArrayVector[] wavefrom = new DoubleArrayVector[dataUnit.getWaveLength()]; 
//		for (int i = 0; i<dataUnit.getWaveLength();i++) {
//			wavefrom[i] = new DoubleArrayVector(dataUnit.getWaveData()[i]);
//		}
		
		
		CPODClick fpodClick = (CPODClick) dataUnit;
		
		if (fpodClick==null) return null; 


		//add the raw wave data 
		if (fpodClick.getWaveData()!=null && fpodClick.getWaveData().length>0) {
			int nbins =fpodClick.getWaveData().length*fpodClick.getWaveData()[0].length;
			int n=0;
			double[] concatWaveform  = new double[nbins];
			//System.out.println("Number of bins: " + nbins);
			for (int i=0; i<fpodClick.getWaveData().length; i++) {
				for (int j=0; j<fpodClick.getWaveData()[i].length; j++) {
//					System.out.println("Current: " + i + " "+ j 
//							+ " nchan: " + dataUnit.getNChan() + "  wave size: " 
//							+ dataUnit.getWaveLength() +"len concat: " + concatWaveform.length);
					concatWaveform[n++] = fpodClick.getWaveData()[i][j];
				}
			}

			Vector newMatrix = DoubleArrayVector.newMatrix(concatWaveform, fpodClick.getWaveData()[0].length, fpodClick.getWaveData().length); 
			rData.add("wave", newMatrix); 
		}
		else {
			rData.add("wave", DoubleArrayVector.newMatrix(new double[] {}, 0, 0)); 
		}
		
		
		rData.add("durationmillis", dataUnit.getDurationInMilliseconds()); 

//		Matrix duration = Mat5.newScalar(fpodClick.getDurationInMilliseconds()); 
		
		rData.add("numcycles", fpodClick.getnCyc()); 
		rData.add("bandwidth", fpodClick.getBw()*1000.); 
		rData.add("peakfreq", fpodClick.getkHz()*1000.); 
		rData.add("endfreq", fpodClick.getEndF()*1000); 
		rData.add("SPL", fpodClick.getSpl()); 
		rData.add("slope", fpodClick.getSlope()); 
		
		short species = -1;
		long clicktrainID = -1;

		if (fpodClick.getCPODClickTrain()!=null) {
			species =  MLCPODExport.getCPODSpecies(fpodClick.getCPODClickTrain().getSpecies());
			clicktrainID = fpodClick.getCPODClickTrain().getUID();
		}
		
		rData.add("species", species); 
		rData.add("clicktrainID", clicktrainID); 

		
		return rData;
	}

	@Override
	public Class<?> getUnitClass() {
		return CPODClick.class;
	}

	@Override
	public String getName() {
		return "cpod_clicks";
	}

}
