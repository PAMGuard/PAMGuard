package GPS;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;

import PamguardMVC.PamDataUnit;
import binaryFileStorage.BinaryDataSource;
import binaryFileStorage.BinaryHeader;
import binaryFileStorage.BinaryObjectData;
import binaryFileStorage.ModuleFooter;
import binaryFileStorage.ModuleHeader;
import binaryFileStorage.PackedBinaryObject;

public class GPSBinaryDataSource extends BinaryDataSource {
	

	// different types of optional GPS information.
	public static final int HAVE_SPEED    = 0x1;
	public static final int HAVE_COG      = 0x2;
	public static final int HAVE_MAGNETIC = 0x4;
	public static final int HAVE_TRUE     = 0x8;

	public GPSBinaryDataSource(GPSDataBlock gpsDataBlock) {
		super(gpsDataBlock);
		// TODO Auto-generated constructor stub
	}

	@Override
	public byte[] getModuleHeaderData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getModuleVersion() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public String getStreamName() {
		// TODO Auto-generated method stub
		return "GPS";
	}

	@Override
	public int getStreamVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void newFileOpened(File outputFile) {
		// TODO Auto-generated method stub

	}

	@Override

	public PackedBinaryObject getPackedData(PamDataUnit pamDataUnit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PamDataUnit sinkData(BinaryObjectData binaryObjectData,
			BinaryHeader bh, int moduleVersion) {
		ByteArrayInputStream bis = new ByteArrayInputStream(binaryObjectData.getData(), 
				0, binaryObjectData.getDataLength());
		DataInputStream dis = new DataInputStream(bis);
		float latitude = 0; 
		float longitude = 0;
/*
 * 
	if (getModuleVersion() >= 2) {
		eBuffer->writeInt64(gpsDataUnit->getGpsTime());
		eBuffer->writeInt8(dataTypes);
		if (gpsDataUnit->hasHeadType(HAVE_SPEED)) {
			eBuffer->writeFloat(gpsDataUnit->getSpeed());
		}
		if (gpsDataUnit->hasHeadType(HAVE_COG)) {
			eBuffer->writeFloat(gpsDataUnit->getCOG());
		}
		if (gpsDataUnit->hasHeadType(HAVE_MAGNETIC)) {
			eBuffer->writeFloat(gpsDataUnit->getMagHead());
		}
		if (gpsDataUnit->hasHeadType(HAVE_TRUE)) {
			eBuffer->writeFloat(gpsDataUnit->getTrueHead());
		}
	}
	
 */
		GpsData gpsData = null;
		try {
			latitude = dis.readFloat();
			longitude = dis.readFloat();
			gpsData = new GpsData(latitude, longitude, 0, binaryObjectData.getTimeMilliseconds());
			if (moduleVersion >= 2) {
				long gpsUTC = dis.readLong();
				gpsData.setTimeInMillis(gpsUTC);
				byte headTypes = dis.readByte();
				if ((headTypes & HAVE_SPEED) != 0) {
					gpsData.setSpeed(dis.readFloat());
				}
				if ((headTypes & HAVE_COG) != 0) {
					gpsData.setCourseOverGround(dis.readFloat());
				}
				if ((headTypes & HAVE_MAGNETIC) != 0) {
					gpsData.setMagneticHeading((double) dis.readFloat());
				}
				if ((headTypes & HAVE_TRUE) != 0) {
					gpsData.setTrueHeading((double) dis.readFloat());
				}
			}
			dis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		System.out.println(String.format("GPS data received at %s", PamCalendar.formatDateTime(binaryObjectData.getTimeMillis())));
		
		GpsDataUnit du = new GpsDataUnit(binaryObjectData.getTimeMilliseconds(), gpsData);
		
		return du;
	}

	@Override
	public ModuleFooter sinkModuleFooter(BinaryObjectData binaryObjectData,
			BinaryHeader bh, ModuleHeader moduleHeader) {
		return null;
	}

	@Override
	public ModuleHeader sinkModuleHeader(BinaryObjectData binaryObjectData,
			BinaryHeader bh) {
		// TODO Auto-generated method stub
		return null;
	}

}
