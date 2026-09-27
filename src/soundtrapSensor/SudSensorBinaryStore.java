package soundtrapSensor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import PamguardMVC.PamDataUnit;
import binaryFileStorage.BinaryDataSource;
import binaryFileStorage.BinaryHeader;
import binaryFileStorage.BinaryObjectData;
import binaryFileStorage.ModuleFooter;
import binaryFileStorage.ModuleHeader;

/**
 * Binary storage handler for {@link SudSensorDataUnit} objects. Each data
 * unit is serialised as:
 * <pre>
 *   double magX
 *   double magY
 *   double magZ
 *   double accelX
 *   double accelY
 *   double accelZ
 *   double heading
 *   double pitch
 *   double roll
 * </pre>
 * Total: 9 × 8 = 72 bytes per record.
 *
 * @author Jamie Macaulay
 */
public class SudSensorBinaryStore extends BinaryDataSource {

    private static final int MODULE_VERSION = 1;

    private final SudSensorDataBlock dataBlock;

    private ByteArrayOutputStream bos;
    private DataOutputStream dos;

    public SudSensorBinaryStore(SudSensorDataBlock dataBlock) {
        super(dataBlock);
        this.dataBlock = dataBlock;
    }

    // -----------------------------------------------------------------
    // BinaryDataSource implementation
    // -----------------------------------------------------------------

    @Override
    public String getStreamName() {
        return "SudSensorData";
    }

    @Override
    public int getStreamVersion() {
        return 0;
    }

    @Override
    public int getModuleVersion() {
        return MODULE_VERSION;
    }

    @Override
    public byte[] getModuleHeaderData() {
        return null;
    }

    @Override
    public void newFileOpened(File outputFile) {
        // nothing needed
    }

    @Override
    public BinaryObjectData getPackedData(PamDataUnit pamDataUnit) {
        SudSensorDataUnit su = (SudSensorDataUnit) pamDataUnit;

        if (dos == null || bos == null) {
            dos = new DataOutputStream(bos = new ByteArrayOutputStream());
        } else {
            bos.reset();
        }

        try {
            dos.writeDouble(su.getMagX());
            dos.writeDouble(su.getMagY());
            dos.writeDouble(su.getMagZ());
            dos.writeDouble(su.getAccelX());
            dos.writeDouble(su.getAccelY());
            dos.writeDouble(su.getAccelZ());
            dos.writeDouble(su.getHeading());
            dos.writeDouble(su.getPitch());
            dos.writeDouble(su.getRoll());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return new BinaryObjectData(1, bos.toByteArray());
    }

    @Override
    public PamDataUnit sinkData(BinaryObjectData binaryObjectData,
                                BinaryHeader bh, int moduleVersion) {
        DataInputStream dis = new DataInputStream(
                new ByteArrayInputStream(binaryObjectData.getData()));
        try {
            double magX    = dis.readDouble();
            double magY    = dis.readDouble();
            double magZ    = dis.readDouble();
            double accelX  = dis.readDouble();
            double accelY  = dis.readDouble();
            double accelZ  = dis.readDouble();
            double heading = dis.readDouble();
            double pitch   = dis.readDouble();
            double roll    = dis.readDouble();

            return new SudSensorDataUnit(binaryObjectData.getTimeMilliseconds(),
                    magX, magY, magZ,
                    accelX, accelY, accelZ,
                    heading, pitch, roll);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ModuleHeader sinkModuleHeader(BinaryObjectData binaryObjectData,
                                         BinaryHeader bh) {
        return null;
    }

    @Override
    public ModuleFooter sinkModuleFooter(BinaryObjectData binaryObjectData,
                                          BinaryHeader bh,
                                          ModuleHeader moduleHeader) {
        return null;
    }
}
