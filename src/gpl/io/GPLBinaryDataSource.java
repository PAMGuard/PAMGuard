package gpl.io;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import binaryFileStorage.BinaryDataSource;
import binaryFileStorage.BinaryHeader;
import binaryFileStorage.BinaryObjectData;
import binaryFileStorage.ModuleFooter;
import binaryFileStorage.ModuleHeader;
import gpl.GPLDetection;
import gpl.GPLDetectionBlock;
import gpl.GPLProcess;
import gpl.contour.GPLContour;
import gpl.contour.GPLContourPoint;

public class GPLBinaryDataSource extends BinaryDataSource {

	private GPLDetectionBlock gplDetectionBlock;
	private GPLProcess gplProcess;

	private ByteArrayOutputStream bos;
	private DataOutputStream dos;
	
	public GPLBinaryDataSource(GPLProcess gplProcess, GPLDetectionBlock gplDetectionBlock) {
		super(gplDetectionBlock);
		this.gplProcess = gplProcess;
		this.gplDetectionBlock = gplDetectionBlock;
	}

	@Override
	public String getStreamName() {
		return "GPL Detections";
	}

	@Override
	public int getStreamVersion() {
		return 1;
	}

	@Override
	public int getModuleVersion() {
		return 2;
	}

	@Override
	public byte[] getModuleHeaderData() {
		return null;
	}

	@Override
	public BinaryObjectData getPackedData(PamDataUnit pamDataUnit) {
		GPLDetection gplDetection = (GPLDetection) pamDataUnit;
		GPLContour contour = gplDetection.getContour();
		if (contour == null) {
			return null;
		}
		contour.getContourPoints();
		ArrayList<GPLContourPoint> points = contour.getContourPoints();
		// almost certain that we can write the contour as int8, but check and if we have to
		// write it as int16. Normally, patches are a dozen bins in t and f at most. 
		int sz = getBiggestBin(contour);
		boolean bits16 = sz > 255;
		if (dos == null || bos == null) {
			dos = new DataOutputStream(bos = new ByteArrayOutputStream());
		}
		else {
			bos.reset();
		}
		try {
			/**
			 * Write the time and freq resolutions, the number of bits, the size of the points (8 or 16) and the points. 
			 */
			dos.writeFloat((float) gplDetection.getTimeRes());
			dos.writeFloat((float) gplDetection.getFreqRes());
			if (points == null) {
				dos.writeShort(0);
				dos.writeByte(8);
			}
			else {
				dos.writeShort(points.size());
				dos.writeByte(bits16 ? 16 : 8);
				for (GPLContourPoint p : points) {
					if (bits16) {
						dos.writeShort(p.x);
						dos.writeShort(p.y);
					}
					else {
						dos.writeByte(p.x);
						dos.writeByte(p.y);
					}
					dos.writeFloat((float) p.signalExcess);
					dos.writeFloat((float) p.totalEnergy);
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		BinaryObjectData bod = new BinaryObjectData(1, bos.toByteArray());
		
		return bod;
	}

	@Override
	public PamDataUnit sinkData(BinaryObjectData binaryObjectData, BinaryHeader bh, int moduleVersion) {
		DataInputStream dis = binaryObjectData.getDataInputStream();
		GPLContour contour = null;
		GPLDetection gplDetection = null;
		int x, y;
		double excess, energy=0;
		try {
			float tRes = dis.readFloat();
			float fRes = dis.readFloat();
			int nP = dis.readShort();
			int nBit = dis.readByte();
			boolean bits16 = (nBit == 16);
			for (int i = 0; i < nP; i++) {
				if (bits16) {
					x = dis.readUnsignedShort();
					y = dis.readUnsignedShort();
				}
				else {
					x = dis.readUnsignedByte();
					y = dis.readUnsignedByte();
				}
				excess = dis.readFloat();
				if (moduleVersion >= 2) {
					energy = dis.readFloat();
				}
				
				if (contour == null) {
					contour = new GPLContour(x,  y,  excess, energy);
				}
				else {
					contour.addContourPoint(x,  y,  excess, energy);
				}
			}
			gplDetection = new GPLDetection(binaryObjectData.getDataUnitBaseData(), tRes, fRes, contour);
			
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
		
		return gplDetection;
	}

	@Override
	public ModuleHeader sinkModuleHeader(BinaryObjectData binaryObjectData, BinaryHeader bh) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ModuleFooter sinkModuleFooter(BinaryObjectData binaryObjectData, BinaryHeader bh,
			ModuleHeader moduleHeader) {
		// TODO Auto-generated method stub
		return null;
	}

	private int getBiggestBin(GPLContour contour) {
		ArrayList<GPLContourPoint> points = contour.getContourPoints();
		if (points == null) {
			return 0;
		}
		int b = 0;
		for (GPLContourPoint p : points) {
			b = Math.max(b, p.x);
			b = Math.max(b, p.y);
		}
		return b;
	}

	@Override
	public void newFileOpened(File outputFile) {
		// TODO Auto-generated method stub

	}

}
