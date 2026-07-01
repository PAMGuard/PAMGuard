package loggerForms.cameragrabber.source;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import loggerForms.cameragrabber.CameraParams;
import loggerForms.cameragrabber.GrabberProcess;
import loggerForms.network.LoggerNetworkManager;
import loggerForms.network.LoggerNetworkMessage;
import loggerForms.network.LoggerNetworkReceiver;

public class NetcamSouce extends CameraSource implements LoggerNetworkReceiver {

	private String netName;
	
	public NetcamSouce(GrabberProcess grabberProcess, int cameraIndex) {
		super(grabberProcess, cameraIndex);
	}

	@Override
	public boolean prepare(CameraParams cameraParams) {
		LoggerNetworkManager netManager = LoggerNetworkManager.getInstance();
		netName = "CameraFrame" + "/" + cameraParams.cameraName;
		netManager.subsribeTopic(netName, this);
		return false;
	}

	@Override
	public boolean shutdown() {
		LoggerNetworkManager netManager = LoggerNetworkManager.getInstance();
		netManager.unsubscribeTopic(netName, this);
		return true;
	}

	@Override
	public boolean newMessage(LoggerNetworkMessage message) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
//				unpackCameraMessage(message);
			}
		});
		unpackCameraMessage(message);
		return true;
	}

	protected void unpackCameraMessage(LoggerNetworkMessage message) {
		byte[] data = message.getData();
		// should be in the form of a png image. 
		BufferedImage image = null;
		try {
			image = ImageIO.read(new ByteArrayInputStream(data));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		if (image != null) {
//			System.out.printf("Received image %dx%d\n", image.getWidth(), image.getHeight());
//		}
		final BufferedImage toSend = image;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				grabberProcess.takeFrame(cameraIndex, NetcamSouce.this, toSend);
			}
		});
	}
}
