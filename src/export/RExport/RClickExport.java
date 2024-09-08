package export.RExport;

import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.ListVector.NamedBuilder;

import org.renjin.sexp.Vector;

import PamguardMVC.PamDataUnit;
import clickDetector.ClickDetection;

public class RClickExport extends RRawExport{

	@Override
	public NamedBuilder addDetectionSpecificFields(NamedBuilder rData, PamDataUnit dataUnit, int index) {
		// add click detection specific fields to rData units. 
		//the waveform
		
		ClickDetection clickDetection = (ClickDetection) dataUnit; 
  		
		super.addDetectionSpecificFields(rData, dataUnit, index); 

		//add some basic click measurements
		rData.add("triggerMap", new DoubleArrayVector(clickDetection.getTriggerList())); 
		rData.add("type", clickDetection.getClickType()); 
		rData.add("flag", clickDetection.getClickFlags()); 
		rData.add("nChan", clickDetection.getNChan()); 
		
		return rData;
	}

	@Override
	public Class<?> getUnitClass() {
		return ClickDetection.class;
	}

	@Override
	public String getName() {
		return "clicks";
	}

}
