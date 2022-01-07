package cpod;

import java.io.File;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import dataGram.Datagram;
import dataGram.DatagramPoint;
import fileOfflineData.OfflineFileMapPoint;

public class CPODDataMapPoint extends OfflineFileMapPoint implements DatagramPoint, ManagedParameters {

	private Datagram dataGram;
	private long filePos;
	
	public CPODDataMapPoint(long pointStart, long pointEnd, int nDatas, File cp1File, long filePos) {
		super(pointStart, pointEnd, nDatas, cp1File);
		this.filePos = filePos;
	}

	@Override
	public Datagram getDatagram() {
		return dataGram;
	}

	@Override
	public void setDatagram(Datagram datagram) {
		this.dataGram = datagram;
	}

	/**
	 * @return the filePos
	 */
	public long getFilePos() {
		return filePos;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = super.getParameterSet();
		return ps;
	}


}
