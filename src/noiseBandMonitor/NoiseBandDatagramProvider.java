package noiseBandMonitor;

import PamguardMVC.PamDataUnit;
import dataGram.DatagramProvider;
import dataGram.DatagramScaleInformation;
import noiseMonitor.NoiseDataUnit;

public class NoiseBandDatagramProvider implements DatagramProvider {

	private NoiseBandControl noiseBandControl;
	private NoiseBandProcess noiseBandProcess;
	private DatagramScaleInformation scaleInfo;
	
	public NoiseBandDatagramProvider(NoiseBandControl noiseBandControl, NoiseBandProcess noiseBandProcess) {
		super();
		this.noiseBandControl = noiseBandControl;
		this.noiseBandProcess = noiseBandProcess;
	}

	@Override
	public int addDatagramData(PamDataUnit dataUnit, float[] dataGramLine) {
		int nPoints = getNumDataGramPoints();
		int lineLength = dataGramLine.length;
		NoiseDataUnit noiseDataUnit = (NoiseDataUnit) dataUnit;
		double[][] data = noiseDataUnit.getNoiseBandData();
		int nData = data.length;
		int n = Math.min(nData, lineLength);
		for (int i = 0; i < n; i++) {
			dataGramLine[i] += Math.pow(10., data[i][0]/10.);
		}
		return n;
	}

	@Override
	public int getNumDataGramPoints() {
		double[] edges = noiseBandProcess.getNoiseDataBlock().getBandHiEdges();
		if (edges != null) {
			return edges.length;
		}
		return 0;
	}

	@Override
	public DatagramScaleInformation getScaleInformation() {
		if (scaleInfo == null) {
			double[] hiedges = noiseBandProcess.getNoiseDataBlock().getBandHiEdges();
			double[] loedges = noiseBandProcess.getNoiseDataBlock().getBandLoEdges();
			if (loedges == null) return null;
			int n = loedges.length;
			double minValue = Math.sqrt(hiedges[0]*loedges[0]);
			double maxValue = Math.sqrt(loedges[n-1]*hiedges[n-1]);
			scaleInfo = new DatagramScaleInformation(minValue, maxValue, "Hz");
			scaleInfo.setLogScale(true);
		}
		return scaleInfo;
	}

}
