package rocca.tethys;

import java.util.EnumMap;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import nilus.Detection;
import nilus.GranularityEnumType;
import nilus.Helper;
import rocca.RoccaContourStats.ParamIndx;
import rocca.RoccaControl;
import rocca.RoccaLoggingDataUnit;
import tethys.TethysControl;
import tethys.output.StreamExportParams;
import tethys.output.TethysExportParams;
import tethys.pamdata.AutoTethysProvider;

public class RoccaTethysProvider extends AutoTethysProvider{

	private RoccaControl roccaControl;
	
	private Helper helper;

	public RoccaTethysProvider(TethysControl tethysControl, RoccaControl roccaControl, PamDataBlock pamDataBlock) {
		super(tethysControl, pamDataBlock);
		this.roccaControl = roccaControl;
		try {
			helper = new Helper();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Detection createDetection(PamDataUnit dataUnit, TethysExportParams tethysExportParams,
			StreamExportParams streamExportParams) {
		Detection det = super.createDetection(dataUnit, tethysExportParams, streamExportParams);
		RoccaLoggingDataUnit rlDU = (RoccaLoggingDataUnit) dataUnit;
//		EnumMap<ParamIndx, Double> contourStats = rlDU.getContourStats();
//		writeContourStats(det, contourStats);
		return det;
	}
	
	private boolean writeContourStats(Detection det, EnumMap<ParamIndx, Double> contourStats) {
		if (contourStats == null || helper == null) {
			return false;
		}
		Set<ParamIndx> keys = contourStats.keySet();
		List<Element> els = det.getParameters().getUserDefined().getAny();
		for (ParamIndx pI : keys) {
			Double val = contourStats.get(pI);
			if (val == null) {
				continue;
			}
			try {
				helper.AddAnyElement(els, pI.toString(), val.toString());
			} catch (JAXBException | ParserConfigurationException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	@Override
	public GranularityEnumType[] getAllowedGranularities() {
		return super.getAllowedGranularities();
	}



}
