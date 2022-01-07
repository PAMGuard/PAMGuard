package cpod.tdPlots;

import cpod.CPODClick;
import cpod.CPODControl;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import dataPlots.data.DataLineInfo;
import dataPlots.data.TDDataInfo;
import dataPlots.data.TDDataProvider;
import dataPlots.data.TDSymbolChooser;
import dataPlots.layout.TDGraph;
import dataPlotsFX.data.TDScaleInfo;

public class CPODPlotinfo extends TDDataInfo {

	CPODSymbolChooser cpodSymbolChooser;
	
	/*
	 * 
	private short nCyc;
	private short bw;
	private short kHz;
	private short endF;
	private short spl;
	private short slope;
	 */
	DataLineInfo kHzDataLineInfo;
	DataLineInfo nCycDataLineInfo;
	DataLineInfo bwDataLineInfo;
	DataLineInfo endFreqDataLineInfo;
	DataLineInfo splDataLineInfo;
	DataLineInfo slopeDataLineInfo;
	DataLineInfo iciDataLineInfo;
	
	public CPODPlotinfo(CPODControl cpodControl, TDDataProvider tdDataProvider, TDGraph tdGraph,
			PamDataBlock pamDataBlock) {
		super(tdDataProvider, tdGraph, pamDataBlock);
		cpodSymbolChooser = new CPODSymbolChooser(this);
		addDataUnits(kHzDataLineInfo = new DataLineInfo("Frequency", "kHz"));
		addDataUnits(nCycDataLineInfo = new DataLineInfo("nCycles", "n"));
		addDataUnits(bwDataLineInfo = new DataLineInfo("BandWidth", "kHz"));
		addDataUnits(endFreqDataLineInfo = new DataLineInfo("End Frequency", "kHz"));
		addDataUnits(splDataLineInfo = new DataLineInfo("SPL", "Units"));
		addDataUnits(slopeDataLineInfo = new DataLineInfo("Slope", "Units"));
		addDataUnits(iciDataLineInfo = new DataLineInfo("ICI", "ms"));
		setFixedScaleInformation(new TDScaleInfo(0, 250, ParameterType.FREQUENCY, ParameterUnits.HZ));
	}

	@Override
	public Double getDataValue(PamDataUnit pamDataUnit) {
		CPODClick cpodClick = (CPODClick) pamDataUnit;
		int iLine = getCurrentDataLineIndex();
		
		switch (iLine) {
		case 0:
			return (double) cpodClick.getkHz();
		case 1:
			return (double) cpodClick.getnCyc();
		case 2:
			return (double) cpodClick.getBw();
		case 3:
			return (double) cpodClick.getEndF();
		case 4:
			return (double) cpodClick.getSpl();
		case 5:
			return (double) cpodClick.getSlope();
		case 6:
			return (double) cpodClick.getICISamples()/200.;
		}
		return null;
	}

	@Override
	public TDSymbolChooser getSymbolChooser() {
		return cpodSymbolChooser;
	}


}
