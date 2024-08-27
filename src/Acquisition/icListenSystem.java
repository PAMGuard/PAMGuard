package Acquisition;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamDetection.RawDataUnit;
import soundPlayback.PlaybackControl;
import soundPlayback.PlaybackSystem;


/**
 * A data acquisition system that uses an Ocean Sonics icListen to acquire the data
 * to be streamed to and used by PAMGUARD.
 * @author John Bowdridge
 *
 */
public class icListenSystem extends DaqSystem implements PamSettings
{
	/******** Defined Constants ********/

	/* Define whether diagnostics should be printed or not */
	private static final boolean DEBUG = false;

	/* ports to be used with the icListens */
	private final int COMMAND_PORT = 50000;
	private final int WAVEFORM_PORT = 51678;

	/* Command Message constants */
	private final int MAX_MESSAGE_SIZE = 6706;
	/* number of bytes in a message before the payload bytes */
	private final int MSG_BEFORE_PAYLOAD = 4;

	private final char SYNC_BYTE = 0x2A;
	private final char ENQUIRE_COMMAND = 0x45;
	private final char JOB_SETUP_COMMAND = 0x44;

	private final char JOB_SETUP_TYPE = 20;
	private final char NUMBER_OF_TAGS = 5;
	private final char TAG_VAL_LEN = 4;

	private final char SAMPLE_RATE_TAG = 14;

	private final char BIT_DEPTH_TAG = 15;
	private final int BIT_DEPTH_VAL = 24;

	private final char GAIN_TAG = 16;
	private final int GAIN_VAL = 0;

	private final char ENDIAN_TAG = 17;
	private final int ENDIAN_VAL = 0;

	private final char LOGGING_MODE_TAG = 18;
	private final int LOGGING_MODE_VAL = 0;

	private final char MIN_TAG_LEN = 4;
	private final char JOB_SETUP_PAYLOAD_LEN = 4 + ((MIN_TAG_LEN + TAG_VAL_LEN) * NUMBER_OF_TAGS);

	private final CommandMessage ENQUIRE_MESSAGE =
			new CommandMessage(SYNC_BYTE, ENQUIRE_COMMAND, new char[]{0,0}, new char[0]);

	/* Stream message constants
	 * Message types */
	private final char TYPE_DATA = 0x31;
	private final char TYPE_HEADER = 0x32;
	private final char TYPE_START = 0x33;
	private final char TYPE_STOP = 0x34;

	/* chunk types */
	private final char EVENT_KEY = 0x41;
	private final char DATA_CHUNK = 0x42;
	private final char STATUS_CHUNK = 0x43;
	private final char DEVICE_CHUNK = 0x44;
	private final char WAVE_SETUP_CHUNK = 0x45;
	private final char SCALING_CHUNK = 0x47;
	private final char TIME_SYNC_CHUNK = 0x50;

	/* Offsets into chunks
	 * Offset into a chunk where the chunk size will be found */
	private final int CHUNK_SIZE_OFFSET = 2;
	private final int CHUNK_PAYLOAD_OFFSET = 4;
	/* this offset is from the start of the chunk payload */
	private final int EVENT_SEQUENCE_NUMBER_OFFSET = 4;
	private final int SCALING_MAX_AMPLITUDE_OFFSET = 16;
	private final int DATA_SAMPLE_NUMBER_OFFSET = 4;
	private final int DATA_SAMPLES_OFFSET = 10;
	private final int DATA_DATA_OFFSET = 12;

	/* Constant stream messages */
	private final StreamMessage START_STREAM_MESSAGE =
			new StreamMessage(TYPE_START, new char[]{0,4}, new char[]{0,0,0,0});

	private final StreamMessage STOP_STREAM_MESSAGE =
			new StreamMessage(TYPE_STOP, new char[]{0,0}, new char[]{});

	/* Bandwidth/Sample rate settings menu constants */
	static final  int NUMBER_OF_BANDWIDTHS = 10;
	static final String[] waveformBandwidthStrings =
			new String[]{"400 Hz", "800 Hz", "1600 Hz", "3200 Hz", "6400 Hz",
						 "12.8 kHz", "25.6 kHz", "51.2 kHz", "102.4 kHz", "204.8 kHz"};

	static final String[] SAMPLE_RATE_STRINGS =
			new String[]{"1000", "2000", "4000", "8000", "16000", "32000",
						 "64000", "128000", "256000", "512000"};
	private static final int[] SAMPLE_RATES = new int[]{1000, 2000, 4000, 8000, 16000, 32000, 64000, 128000, 256000, 512000};

	/**
	 * DOUG.
	 * Set this to whatever the icListens ADC input range is to get correct calibration
	 * in the part of PAMGUaUARd. You'll also need to set the hydrophone sensitivity. That
	 * is also possible to do from here, if a bit more tricky since it's in a very different
	 * part of the software - something for later.
	 */
	private static final double VPEAKTOPEAK = 2.5;

	/* Used to interpret message response */
	private final int SERIAL_NUM_OFFSET = 4;
	private final int DEVICE_TYPE_OFFSET = 32;

	/* Status string */
	private final String NO_CONNECTION_STATUS = "No icListen Connected";

	/* Device types */
	private final int DEV_TYPE_ICTALK_LF = 0x01;
	private final int DEV_TYPE_ICLISTEN_LF = 0x02;
	private final int DEV_TYPE_GUEST_SENSOR = 0x03;
	private final int DEV_TYPE_ICTALK_HF = 0x04;
	private final int DEV_TYPE_ICLISTEN_HF = 0x05;
	private final int DEV_TYPE_ICLISTEN_AF = 0x07;
	private final int DEV_TYPE_ICLISTEN_MF = 0x41;

	//TODO Add max channels constant


	/******** Module Variable Declarations ********/

	/* Data acquisition dialog box variables */
	/* TODO Add variables to support multiple channels in the dialog box
	 * Might be able to just make some of these arrays */
	JPanel daqDialog;
	private JTextField ipAddressInput;
	private JTextField timeOutInput;
	private JTextField icListenStatus;
	private JTextField sampleRate;
	private JButton btnConnect;
	private JButton btnDisconnect;
	private JComboBox waveformBandwidth;
	private int currentWaveBandwidth = 0;
	private int currentSampleRateString = 0;

	//TODO make this an array of strings to
	private String ipAddress = null;

	private int dataUnitSamples;


	//TODO make these sockets arrays to connect to multiple hydrophones at a time
	Socket commandSocket;
	Socket streamSocket;

	AcquisitionControl acquisitionControl;

	int rawBufferSizeInBytes;

	/* data stream variables */
	//TODO make these arrays to support multiple data streams at once
	OutputStream outToServer;
	DataOutputStream outStream;
	InputStream inFromServer;
	DataInputStream inStream;

	/* icListen's current connection status */
	//TODO make an array to show which current indexes are connected
	private boolean connected = false;

	private volatile boolean stopCapture;
	private volatile boolean captureRunning;

	/* Time for connecting before a timeout should occur in ms */
	private int timeOut = 5000;

	private int currentSampleRate = 1000;

	//TODO make these arrays to describe each hydrophone  connected
	private int serialNum;
	private int deviceType;

	// DOUG changed type of this in latest version for some better queue management
	private AudioDataQueue newDataUnits;

	private AcquisitionDialog acquisitionDialog;


	/******** Inner Classes ********/

	/**
	 * Inner class used for command messages from or to the icListen
	 * Byte order is little endian
	 * @author John Bowdridge
	 *
	 */
	class CommandMessage
	{
		/*** Constants ***/

		final int MAX_PAYLOAD_LENGTH = 6700;
		final int PAYLOADLEN_ARRAY_LENGTH = 2;
		final int CRC_ARRAY_LENGTH = 2;
		/* message size with no payload */
		final int MIN_MESSAGE_SIZE = 6;

		/* table used when calculating the crc */
		final char[] crc16_table = new char[]
			{
			0x0000, 0xc0c1, 0xc181, 0x0140, 0xc301, 0x03c0, 0x0280, 0xc241, /*7*/
			0xc601, 0x06c0, 0x0780, 0xc741, 0x0500, 0xc5c1, 0xc481, 0x0440, /*15*/
			0xcc01, 0x0cc0, 0x0d80, 0xcd41, 0x0f00, 0xcfc1, 0xce81, 0x0e40, /*23*/
			0x0a00, 0xcac1, 0xcb81, 0x0b40, 0xc901, 0x09c0, 0x0880, 0xc841, /*31*/
			0xd801, 0x18c0, 0x1980, 0xd941, 0x1b00, 0xdbc1, 0xda81, 0x1a40, /*39*/
			0x1e00, 0xdec1, 0xdf81, 0x1f40, 0xdd01, 0x1dc0, 0x1c80, 0xdc41, /*47*/
			0x1400, 0xd4c1, 0xd581, 0x1540, 0xd701, 0x17c0, 0x1680, 0xd641, /*55*/
			0xd201, 0x12c0, 0x1380, 0xd341, 0x1100, 0xd1c1, 0xd081, 0x1040, /*63*/
			0xf001, 0x30c0, 0x3180, 0xf141, 0x3300, 0xf3c1, 0xf281, 0x3240, /*71*/
			0x3600, 0xf6c1, 0xf781, 0x3740, 0xf501, 0x35c0, 0x3480, 0xf441, /*79*/
			0x3c00, 0xfcc1, 0xfd81, 0x3d40, 0xff01, 0x3fc0, 0x3e80, 0xfe41, /*87*/
			0xfa01, 0x3ac0, 0x3b80, 0xfb41, 0x3900, 0xf9c1, 0xf881, 0x3840, /*95*/
			0x2800, 0xe8c1, 0xe981, 0x2940, 0xeb01, 0x2bc0, 0x2a80, 0xea41, /*103*/
			0xee01, 0x2ec0, 0x2f80, 0xef41, 0x2d00, 0xedc1, 0xec81, 0x2c40, /*111*/
			0xe401, 0x24c0, 0x2580, 0xe541, 0x2700, 0xe7c1, 0xe681, 0x2640, /*119*/
			0x2200, 0xe2c1, 0xe381, 0x2340, 0xe101, 0x21c0, 0x2080, 0xe041, /*127*/
			0xa001, 0x60c0, 0x6180, 0xa141, 0x6300, 0xa3c1, 0xa281, 0x6240, /*135*/
			0x6600, 0xa6c1, 0xa781, 0x6740, 0xa501, 0x65c0, 0x6480, 0xa441, /*143*/
			0x6c00, 0xacc1, 0xad81, 0x6d40, 0xaf01, 0x6fc0, 0x6e80, 0xae41, /*151*/
			0xaa01, 0x6ac0, 0x6b80, 0xab41, 0x6900, 0xa9c1, 0xa881, 0x6840, /*159*/
			0x7800, 0xb8c1, 0xb981, 0x7940, 0xbb01, 0x7bc0, 0x7a80, 0xba41, /*167*/
			0xbe01, 0x7ec0, 0x7f80, 0xbf41, 0x7d00, 0xbdc1, 0xbc81, 0x7c40, /*175*/
			0xb401, 0x74c0, 0x7580, 0xb541, 0x7700, 0xb7c1, 0xb681, 0x7640, /*183*/
			0x7200, 0xb2c1, 0xb381, 0x7340, 0xb101, 0x71c0, 0x7080, 0xb041, /*191*/
			0x5000, 0x90c1, 0x9181, 0x5140, 0x9301, 0x53c0, 0x5280, 0x9241, /*199*/
			0x9601, 0x56c0, 0x5780, 0x9741, 0x5500, 0x95c1, 0x9481, 0x5440, /*207*/
			0x9c01, 0x5cc0, 0x5d80, 0x9d41, 0x5f00, 0x9fc1, 0x9e81, 0x5e40, /*215*/
			0x5a00, 0x9ac1, 0x9b81, 0x5b40, 0x9901, 0x59c0, 0x5880, 0x9841, /*223*/
			0x8801, 0x48c0, 0x4980, 0x8941, 0x4b00, 0x8bc1, 0x8a81, 0x4a40, /*231*/
			0x4e00, 0x8ec1, 0x8f81, 0x4f40, 0x8d01, 0x4dc0, 0x4c80, 0x8c41, /*239*/
			0x4400, 0x84c1, 0x8581, 0x4540, 0x8701, 0x47c0, 0x4680, 0x8641, /*247*/
			0x8201, 0x42c0, 0x4380, 0x8341, 0x4100, 0x81c1, 0x8081, 0x4040, /*255*/
			};


		/*** Fields ***/

		/* Sync character */
		char sync;
		/* Command Character */
		char command;
		/* Length of the data payload */
		char[] payloadLength;
		/* Contains the data for the command (if applicable) */
		char[] payload;
		/* CRC used to verify message validity */
		char[] crc;

		/**
		 * Standard constructor using individual message components
		 *
		 */
		public CommandMessage(char argSync, char argCommand, char[] argPayloadLength,
				char[] argPayload)
		{
			this.sync = argSync;
			this.command = argCommand;
			this.payloadLength = argPayloadLength;
			this.payload = argPayload;
			this.crc = calculateTxCrc(this);
		}

		/**
		 * A constructor using a properly formated byte buffer
		 * Useful with returned messages, CRC will need to be checked
		 * @param buf
		 */
		public CommandMessage(byte[] buf)
		{
			int payloadLength;
			int bufferIndex;

			/* get sync byte */
			this.sync = (char) buf[0];

			/* get command */
			this.command = (char) buf[1];

			/* get payload length */
			this.payloadLength = new char[this.PAYLOADLEN_ARRAY_LENGTH];
			this.payloadLength[0] = (char) buf[2];
			this.payloadLength[1] = (char) buf[3];
			payloadLength = (this.payloadLength[0] & 0xFF)
					| (this.payloadLength[1] & 0xFF << 8);

			/* get payload */
			this.payload = new char[payloadLength];
			for(bufferIndex = MSG_BEFORE_PAYLOAD; bufferIndex < (payloadLength + MSG_BEFORE_PAYLOAD) ; bufferIndex++)
			{
				this.payload[bufferIndex - MSG_BEFORE_PAYLOAD] = (char)buf[bufferIndex];
			}

			/* get crc */
			this.crc = new char[this.CRC_ARRAY_LENGTH];
			this.crc[0] = (char)buf[bufferIndex];
			this.crc[1] = (char)buf[bufferIndex + 1];
		}

		/**
		 * Calculate the CRC for a message
		 * @param msg
		 * @return
		 */
		public char[] calculateTxCrc(CommandMessage msg)
		{
			char[] crc = new char[CRC_ARRAY_LENGTH];
			char checksum = 0;
			int payloadIndex;
			int payloadLength;

			/* calculate a checksum using each byte */
			checksum = crc_update(msg.sync, checksum);
			checksum = crc_update(msg.command, checksum);
			checksum = crc_update(msg.payloadLength[0], checksum);
			checksum = crc_update(msg.payloadLength[1], checksum);
			 /* assemble payload length into one number */
			payloadLength = (msg.payloadLength[0] & 0xFF)
								| (msg.payloadLength[1] & 0xFF << 8);

			for(payloadIndex = 0; payloadIndex < payloadLength ; payloadIndex++)
			{
				checksum = crc_update(msg.payload[payloadIndex], checksum);
			}

			crc[0] = (char) (checksum & 0xFF);
			crc[1] = (char) ((checksum >> 8)& 0xFF);

			return crc;
		}

		/**
		 * Update a running accumulator with the proper number according to a byte
		 * of data sent to create a valid CRC
		 * @param data
		 * @param accum
		 * @return
		 */
		private char crc_update(char data, char accum)
		{
			int combValue;

			/* Get the proper CRC index */
			combValue = (( accum & 0xFF) ^ (data & 0xFF)) & 0xFF;
			/* Use the CRC table and the index found to update the accumulator accordingly */
			accum = (char) ((accum >> 8) ^ crc16_table [(combValue & 0xFF)]);

			return accum;
		}

	} /* end inner class CommandMessage */

	/**
	 * Inner class to be used by the data acquisition to process the captured data
	 * Contains a run where the processing will occur
	 * @author John Bowdridge
	 *
	 */
	class DataProcessThread implements Runnable
	{
		/* a queue to were the data captured will be stored */
		private BlockingQueue<byte[]> msgList;
		//TODO add variable for the current index

		/**
		 * Constructor that receives a queue to use for the data transfer
		 * @param queue
		 */
		public DataProcessThread(BlockingQueue<byte[]> queue)
		{
			this.msgList = queue;
			//TODO add which index this thread is related to
		}

		/**
		 * Function where the data processing will take place
		 * Receives the data from the queue and performs the proper
		 * processing to match the required PAMGUARD RawDataUnit (-1,+1)
		 * and passes it to PAMGUARD using the newDataUnits list
		 */
		@Override
		public void run()
		{
			/* buffer to receive data from message queue */
			byte[] buffer = new byte[MAX_MESSAGE_SIZE];

			/* buffer to put processed samples into to pass to pamguard */
			double[] doubleBuf;

			double maxAmplitude = 1;

			int payloadIndex, doubleIndex, chunkIndex;
			/* doubleBufLen also is the number of samples */
			int chunkLen = 0, payloadLen, doubleBufLen;
			int sample, sampleNumber;

			char[] headerEventKey = null;
			boolean eventKeyMatch = false, sampleTallyInit = false;

			long milliSec, totalSamples = 0;
			long sequenceNumber = 0, sampleTally = 0;

			RawDataUnit newDataUnit = null;
			StreamMessage msg;

			if(DEBUG)
			{
				System.out.println("Data process thread running");
			}

			/* Data capture is running */
			stopCapture = false;
			captureRunning = true;
			totalSamples = 0;

			/* Start capturing data
			 * Loop until requested to stop */

			while(!stopCapture)
			{
				/* Get the next message */

				try
				{
					buffer = msgList.take();
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
					break;
				}

				msg = new StreamMessage(buffer);

				if(DEBUG)
				{
					/* Print the reply */
					System.out.println("Response received");
					printStreamMessageBuffer(buffer);
				}

				payloadLen = ((msg.payloadLength[0] & 0xFF) << 8) | (msg.payloadLength[1] & 0xFF);

				/* Check for the message type */
				if(msg.type == TYPE_HEADER)
				{
					/* Received event header
					 * Go through payload looking for the necessary chunks */
					for(payloadIndex = 0; payloadIndex < payloadLen; payloadIndex += chunkLen + CHUNK_PAYLOAD_OFFSET)
					{
						/* get the chunk length */
						chunkLen = ((msg.payload[payloadIndex + CHUNK_SIZE_OFFSET] & 0xFF) << 8)
										| (msg.payload[payloadIndex + CHUNK_SIZE_OFFSET + 1] & 0xFF);


						if(DEBUG)
						{
							System.out.printf("Chunk Type: 0x%02X Chunk Size: %d\n", (int)msg.payload[payloadIndex], chunkLen);
						}
						/* check the type of chunk */
						switch(msg.payload[payloadIndex])
						{
						case EVENT_KEY:
							/* Save a copy of the event key to ensure that it matches the data message */
							headerEventKey = new char[chunkLen];
							for(chunkIndex=0; chunkIndex < chunkLen; chunkIndex++)
							{
								headerEventKey[chunkIndex] = msg.payload[payloadIndex + CHUNK_PAYLOAD_OFFSET + chunkIndex];
							}

							/* Get the sequence number */
							sequenceNumber = ( ((headerEventKey[EVENT_SEQUENCE_NUMBER_OFFSET]     & 0xFF) << 56)
											 | ((headerEventKey[EVENT_SEQUENCE_NUMBER_OFFSET + 1] & 0xFF) << 48)
											 | ((headerEventKey[EVENT_SEQUENCE_NUMBER_OFFSET + 2] & 0xFF) << 40)
											 | ((headerEventKey[EVENT_SEQUENCE_NUMBER_OFFSET + 3] & 0xFF) << 32)
											 | ((headerEventKey[EVENT_SEQUENCE_NUMBER_OFFSET + 4] & 0xFF) << 24)
											 | ((headerEventKey[EVENT_SEQUENCE_NUMBER_OFFSET + 5] & 0xFF) << 16)
											 | ((headerEventKey[EVENT_SEQUENCE_NUMBER_OFFSET + 6] & 0xFF) << 8)
											 |  (headerEventKey[EVENT_SEQUENCE_NUMBER_OFFSET + 7] & 0xFF)
											 );
							/* check to see if any samples where missed by seeing if
							 * the sequence number is equal to the sample tally */

							if(sequenceNumber != sampleTally)
							{
								/* check to see if the sample tally has been initialize */
								if(sampleTallyInit)
								{
									System.out.println("Missed samples!\nSequence number: " + sequenceNumber
												+ " Sample tally: " + sampleTally + " Missed: " + (sequenceNumber - sampleTally));
									sampleTally = sequenceNumber;

									//TODO add code to handle missed samples
								}
								else
								{
									/* initialize the sample tally */
									sampleTally = sequenceNumber;
									sampleTallyInit = true;
								}
							}

							break;
						case DEVICE_CHUNK:

							break;
						case STATUS_CHUNK:

							break;
						case WAVE_SETUP_CHUNK:

							break;
						case SCALING_CHUNK:
							/* get the max amplitude */
							maxAmplitude = (msg.payload[payloadIndex + SCALING_MAX_AMPLITUDE_OFFSET] << 24)
												 | ((msg.payload[payloadIndex + SCALING_MAX_AMPLITUDE_OFFSET + 1] & 0xFF) << 16)
												 | ((msg.payload[payloadIndex + SCALING_MAX_AMPLITUDE_OFFSET + 2] & 0xFF) << 8 )
												 | ((msg.payload[payloadIndex + SCALING_MAX_AMPLITUDE_OFFSET + 3] & 0xFF));
							if(DEBUG)
							{
								System.out.println("Max Amplitude: " + maxAmplitude);
							}
							break;
						}

					}
				}
				else if(msg.type == TYPE_DATA)
				{
					/* Data message received
					 * Go through payload looking for the necessary chunks */
					for(payloadIndex = 0; payloadIndex < payloadLen; payloadIndex += chunkLen + CHUNK_PAYLOAD_OFFSET)
					{
						/* get the length of the chunk */
						chunkLen = ((msg.payload[payloadIndex + CHUNK_SIZE_OFFSET] & 0xFF) << 8)
										| (msg.payload[payloadIndex + CHUNK_SIZE_OFFSET + 1] & 0xFF);

						if(DEBUG)
						{
							System.out.printf("Chunk Type: 0x%02X Chunk Size: %d\n", (int)msg.payload[payloadIndex], chunkLen);
						}
						/* check the chunk type */
						switch(msg.payload[payloadIndex])
						{
						case EVENT_KEY:

							/* check to see if the event key matches */
							if(headerEventKey == null)
							{
								eventKeyMatch = false;
							}
							else
							{
								eventKeyMatch = true;
								for(chunkIndex = 0; chunkIndex < chunkLen && eventKeyMatch; chunkIndex++)
								{
									if(msg.payload[payloadIndex + CHUNK_PAYLOAD_OFFSET + chunkIndex] != headerEventKey[chunkIndex])
									{
										eventKeyMatch = false;
									}
								}
							}

							if(DEBUG)
							{
								System.out.println("Event Keys Match?: " + eventKeyMatch);
							}

							break;
						case DATA_CHUNK:
							/* make sure the event key matches the header */
							if(eventKeyMatch)
							{
								/* get the number of samples */
								doubleBufLen = ((msg.payload[payloadIndex + DATA_SAMPLES_OFFSET] & 0xFF) << 8)
										| (msg.payload[payloadIndex + DATA_SAMPLES_OFFSET + 1] & 0xFF);

								/* Check if any samples where missed since the header
								 * by comparing the sample number to the tally minus the sequence number */
								sampleNumber =      ( ((msg.payload[payloadIndex + DATA_SAMPLE_NUMBER_OFFSET]     & 0xFF) << 24)
													| ((msg.payload[payloadIndex + DATA_SAMPLE_NUMBER_OFFSET + 1] & 0xFF) << 16)
													| ((msg.payload[payloadIndex + DATA_SAMPLE_NUMBER_OFFSET + 2] & 0xFF) << 8)
												 	|  (msg.payload[payloadIndex + DATA_SAMPLE_NUMBER_OFFSET + 3] & 0xFF)
													);

								/* check if any samples were missed */
								if( sampleNumber != (sampleTally - sequenceNumber))
								{
									System.out.println("Missed samples!\nSample number: " + sampleNumber
											+ " Samples since header: " + (sampleTally - sequenceNumber));
									//TODO add code to handle missed samples
								}

								/* update the sample tally */
								sampleTally = sampleTally + doubleBufLen;

								if(DEBUG)
								{
									System.out.println("double buffer length: " + doubleBufLen);
								}

								doubleBuf = new double[doubleBufLen];
								/* go through the samples and put them into the RawDataUnit list */
								for(doubleIndex = 0, chunkIndex = (payloadIndex + DATA_DATA_OFFSET);
										doubleIndex < doubleBufLen;
										doubleIndex++, chunkIndex += 3)
								{
									/* get the samples */
									sample = ((msg.payload[chunkIndex + 2])& 0xFF)
												 | ((msg.payload[chunkIndex + 1] & 0xFF) << 8)
												 | ((msg.payload[chunkIndex]     & 0xFF) << 16);

									if(DEBUG)
									{
										System.out.printf("Sample pre sign extension: %08X\n", sample);
										System.out.printf("Created from : %X %X %X\n", (int)msg.payload[chunkIndex], (int)msg.payload[chunkIndex + 1], (int)msg.payload[chunkIndex + 2]);
									}

									/* Check the sign (most significant bit is set) and extend the sign if nesc. */
									if((msg.payload[chunkIndex] & 0x0080) == 0x0080)
									{

										/* extend the sign */
										sample = sample | 0xFF000000;
										if(DEBUG)
										{
											System.out.printf("Sign extended: %08X\n", sample);
										}
									}

									if(DEBUG)
									{
										System.out.println("sample before division: " + sample);
									}

									/* Scale the sample to between -1 and 1 */
									doubleBuf[doubleIndex] = sample / maxAmplitude;
									if(DEBUG)
									{
										System.out.println("double buffer: " + doubleBuf[doubleIndex]
												+ "\nindex: " + doubleIndex + " double buffer len: " + doubleBufLen);
									}

								}
								/* get how much time this covers */
								milliSec = acquisitionControl.getAcquisitionProcess().absSamplesToMilliseconds(totalSamples);

								if(DEBUG)
								{
									System.out.println("Milli Seconds: " + milliSec);
								}

								/* add it as a new data unit */
								//TODO add a shift to the channelBitmap that uses the current channel index
								newDataUnit = new RawDataUnit(milliSec, 0x01, totalSamples, doubleBufLen);
								newDataUnit.setRawData(doubleBuf);
								newDataUnits.addNewData(newDataUnit);

								/* tally the total samples */
								totalSamples += doubleBufLen;
							}

							break;
						}

					}
				}
				else
				{
					/* Can ignore since not an expected message type */
					if(DEBUG)
					{
						/* Print the reply */
						System.out.println("Unexpected type: " + (int)msg.type);
					}
				}
			}
			/* reset any necessary components */
			sampleTallyInit = false;

			/* stop capture and return */
			captureRunning = false;
		} /* end run */

	} /* end inner class DataProcessThread */

	/**
	 * Inner class to be used by the data acquisition to capture the data in a separate thread
	 * Contains a run where the data capture will occur
	 * @author John Bowdridge
	 *
	 */
	class CaptureThread implements Runnable
	{
		/* amount of bytes to read */
		private final int CHECK_READ = 4;

		/* queue to put the received messages */
		private BlockingQueue<byte[]> msgList;

		//TODO add a local DataInputStream

		/**
		 * Constructor that receives a queue to use for the data transfer
		 * @param queue
		 */
		public CaptureThread(BlockingQueue<byte[]> queue)
		{
			this.msgList = queue;
			//TODO Pass this an DataInputStream
		}

		/**
		 * Function where a message will be received and put into
		 * the message queue for future processing
		 */
		@Override
		public void run()
		{
			/* Data capture is running */
			stopCapture = false;
			captureRunning = true;

			/* get a message and add it to the queue */
			try
			{
				while(!stopCapture)
				{
					msgList.add(getMessage());
				}
			}
			catch(NullPointerException e)
			{
				/* if a null was added this catch pulls it out of the capture loop */
				stopCapture = true;
			}
			captureRunning = false;
		}

		/**
		 * Function that gets a message from the icListen and returns it
		 * @return
		 */
		public byte[] getMessage()
		{
			/* Buffer to put the message */
			byte[] buffer = new byte[MAX_MESSAGE_SIZE];
			/* temporary buffer used when an unaligned message was found */
			byte[] tmpBuf = new byte[CHECK_READ];
			int readSize = CHECK_READ;
			int offset = 0;

			boolean messageFound = false;

			/* run until capture is requested to stop */
			while(!stopCapture)
			{
				/* read in bytes from the icListen will either be a check
				 * read of 4 bytes or the rest of a message */
				try
				{
					//TODO replace this with the local DataInputStream
					inStream.read(buffer, offset , readSize);
				}
				catch (IOException e)
				{
					System.out.println("IOException when reading start system response");
					e.printStackTrace();
					stopCapture = true;
					return null;
				}
				catch(ArrayIndexOutOfBoundsException e)
				{
					/* A wrong message was found once a possible start was found
					 * message has not been found, ensure it doesn't check it for a message start
					 * will try to re-align */
					System.out.println("Array bounds exceeded: Offset: " + offset + " Size: " + readSize);
					messageFound = false;
					buffer[0] = 0;
					buffer[1] = 0;
					buffer[2] = 0;
					buffer[3] = 0;
				}

				if(messageFound)
				{
					/* the message is found return it */
					return buffer;
				}
				else
				{
					/*
					 * The following is a form of unrolled loop that searches through the read
					 * in message to check for a message start. When one is found it will re-order
					 * bytes in the buffer to be correct. It will then reduce the possibility that
					 * a false positive was found. If it is still determined to be a message
					 * start, set up the read for the rest of the message
					 */

					/* no message is currently found, Check for the alignment */
					if( (buffer[0] == TYPE_HEADER || buffer[0] == TYPE_DATA) && buffer[1] == SYNC_BYTE)
					{
						/* Properly aligned message
						 * Get the message length */
						readSize = ((buffer[2] & 0xFF) << 8) | (buffer[3] & 0xFF);

						/* Make Sure that the read size is valid to reduce the chance that a none message start was found */
						if(readSize > MAX_MESSAGE_SIZE)
						{
							/* not a valid message */
							readSize = CHECK_READ;
							offset = 0;
							messageFound = false;
						}
						else
						{
							/* message is now found and aligned */
							messageFound = true;
							offset = MSG_BEFORE_PAYLOAD;
						}
					}
					else
					{
						/* unaligned message, Check how desynced the message is */
						if((buffer[1] == TYPE_HEADER || buffer[1] == TYPE_DATA) && buffer[2] == SYNC_BYTE)
						{
							/* read in the next couple of bytes to use if the message start is found */
							try
							{
								//TODO replace this with the local DataInputStream
								inStream.read(tmpBuf, 0 , CHECK_READ);
							}
							catch (IOException e)
							{
								System.out.println("IOException when reading start system response");
								e.printStackTrace();
								stopCapture = true;
								captureRunning = false;
								return null;
							}

							/* put the read in bytes into the correct location */
							buffer[0] = buffer[1]; /* Type byte */
							buffer[1] = buffer[2]; /* sync byte */
							buffer[2] = buffer[3]; /* high length byte */
							buffer[3] = tmpBuf[0]; /* low length byte */
							/* payload bytes */
							buffer[4] = tmpBuf[1]; /* low length byte */
							buffer[5] = tmpBuf[2]; /* low length byte */
							buffer[6] = tmpBuf[3]; /* low length byte */

							/*Get the message length*/
							readSize = (((buffer[2] & 0xFF) << 8) | (buffer[3] & 0xFF)) - 3;

							/* Make Sure that the read size is valid to reduce the chance that a none message start was found */
							if(readSize + 3 > MAX_MESSAGE_SIZE)
							{
								/* not a valid message */
								readSize = CHECK_READ;
								offset = 0;
								messageFound = false;
							}
							else
							{
								/* message is now found and aligned */
								messageFound = true;
								offset = MSG_BEFORE_PAYLOAD + 3;
							}
						}
						else if((buffer[2] == TYPE_HEADER || buffer[2] == TYPE_DATA) && buffer[3] == SYNC_BYTE)
						{
							/* read in the next couple of bytes to use if the message start is found */
							try
							{
								//TODO replace this with the local DataInputStream
								inStream.read(tmpBuf, 0 , CHECK_READ);
							}
							catch (IOException e)
							{
								System.out.println("IOException when reading start system response");
								e.printStackTrace();
								stopCapture = true;
								captureRunning = false;
								return null;
							}

							/* put the read in bytes into the correct location */
							buffer[0] = buffer[2]; /* Type byte */
							buffer[1] = buffer[3]; /* sync byte */
							buffer[2] = tmpBuf[0]; /* high length byte */
							buffer[3] = tmpBuf[1]; /* low length byte */
							/* payload bytes */
							buffer[4] = tmpBuf[2];
							buffer[5] = tmpBuf[3];

							/*Get the message length*/
							readSize = (((buffer[2] & 0xFF) << 8) | (buffer[3] & 0xFF)) - 2;

							/* Make Sure that the read size is valid to reduce the chance that a none message start was found */
							if(readSize + 2 > MAX_MESSAGE_SIZE)
							{
								/* not a valid message */
								readSize = CHECK_READ;
								offset = 0;
								messageFound = false;
							}
							else
							{
								/* message is now found and aligned */
								messageFound = true;
								offset = MSG_BEFORE_PAYLOAD + 2;
							}
						}
						else if((buffer[3] == TYPE_HEADER || buffer[3] == TYPE_DATA) && tmpBuf[0] == SYNC_BYTE)
						{
							/* read in the next couple of bytes to use if the message start is found */
							try
							{
								//TODO replace this with the local DataInputStream
								inStream.read(tmpBuf, 0 , CHECK_READ);
							}
							catch (IOException e)
							{
								System.out.println("IOException when reading start system response");
								e.printStackTrace();
								stopCapture = true;
								captureRunning = false;
								return null;
							}

							/* put the read in bytes into the correct location */
							buffer[0] = buffer[3]; /* Type byte */
							buffer[1] = tmpBuf[0]; /* sync byte */
							buffer[2] = tmpBuf[1]; /* high length byte */
							buffer[3] = tmpBuf[2]; /* low length byte */
							/* payload byte */
							buffer[4] = tmpBuf[3]; /* low length byte */

							/*Get the message length*/
							readSize = (((buffer[2] & 0xFF) << 8) | (buffer[3] & 0xFF)) - 1;

							/* Make Sure that the read size is valid to reduce the chance that a none message start was found */
							if(readSize + 1 > MAX_MESSAGE_SIZE)
							{
								/* not a valid message */
								readSize = CHECK_READ;
								offset = 0;
								messageFound = false;
							}
							else
							{
								/* message is now found and aligned */
								messageFound = true;
								offset = MSG_BEFORE_PAYLOAD + 1;
							}
						}
						else
						{
							/* No message start found, make sure the offset and size are still set */
							readSize = CHECK_READ;

							offset = 0;
						}
					}
				}
			}
			return null;
		} /* end run */

	} /* end inner class CaptureThread */

	/**
	 * Inner class used to send and receive icListen stream messages
	 * Byte order is stream order(big endian)
	 * @author John Bowdridge
	 *
	 */
	class StreamMessage
	{
		/*** Constants ***/
		final char SYNC_VAL = 0x2A;

		final int PAYLOADLEN_ARRAY_LENGTH = 2;

		/*** Fields ***/

		/* the type of Character */
		char type;
		/* Sync character */
		char sync;
		/* Length of the data payload */
		char[] payloadLength;
		/* Contains the data (if applicable). It is 32 bit aligned */
		char[] payload;

		/**
		 * Standard constructor using individual message components
		 *
		 */
		public StreamMessage(char argType, char[] argPayloadLength, char[] argPayload)
		{
			this.type = argType;
			this.sync = SYNC_VAL;
			this.payloadLength = argPayloadLength;
			this.payload = argPayload;
		}

		/**
		 * A constructor using a properly formated byte buffer
		 * Useful with returned messages, CRC will need to be checked
		 * @param buf
		 */
		public StreamMessage(byte[] buf)
		{
			int payloadLength;
			int bufferIndex;

			/* get command */
			this.type = (char) buf[0];

			/* get sync byte */
			this.sync = (char) buf[1];

			/* get payload length */
			this.payloadLength = new char[PAYLOADLEN_ARRAY_LENGTH];
			this.payloadLength[0] = (char) buf[2];
			this.payloadLength[1] = (char) buf[3];
			payloadLength = (this.payloadLength[1] & 0xFF)
					| ((this.payloadLength[0] & 0xFF) << 8);

			/* get payload */
			this.payload = new char[payloadLength];
			for(bufferIndex = MSG_BEFORE_PAYLOAD; bufferIndex < (payloadLength + MSG_BEFORE_PAYLOAD) && bufferIndex < MAX_MESSAGE_SIZE ; bufferIndex++)
			{
				this.payload[bufferIndex - MSG_BEFORE_PAYLOAD] = (char)buf[bufferIndex];
			}
		}

	} /* end inner class StreamMessage */


	/******** Methods/Constructors ********/

	/**
	 * Constructor registers settings
	 */
	public icListenSystem()
	{
		PamSettingManager.getInstance().registerSettings(this);
	}

	/**
	 * Returns the type of the system
	 */
	@Override
	public String getSystemType()
	{
		return "icListen";
	}

	/**
	 * Returns the name of the current system
	 */
	@Override
	public String getSystemName()
	{
		/* TODO this will need to be re-thought when using multiple hydrophones
		 * Could do all of them but that would probably be too long, might just
		 * want to reurn how many are connected if more then one is connected */
		String systemName;
		/* Check to see if there is an icListen connected */
		if(connected)
		{
			/* get the device type */
			switch(deviceType)
			{
			case DEV_TYPE_ICTALK_LF:	/* icTalk LF */

				systemName = "icTalk LF";
				break;
			case DEV_TYPE_ICLISTEN_LF:	/* icListen LF */

				systemName = "icListen LF";
				break;
			case DEV_TYPE_GUEST_SENSOR:	/* Standalone Guest Sensor */

				systemName = "Standalone Guest Sensor";
				break;
			case DEV_TYPE_ICTALK_HF:	/* icTalk HF */

				systemName = "icTalk HF";
				break;
			case DEV_TYPE_ICLISTEN_HF:	/* icListen HF */

				systemName = "icListen HF";
				break;
			case DEV_TYPE_ICLISTEN_AF:	/* icListen AF */

				systemName = "icListen AF";
				break;
			case DEV_TYPE_ICLISTEN_MF:	/* icListen MF */

				systemName = "icListen MF";
				break;
			default:

				systemName = "Unknown Type";
				break;
			}

			systemName = systemName + " " + serialNum;
			return systemName;
		}
		else
		{
			return "No icListen";
		}

	}

	/**
	 * Get the data acquisition dialog box component of this system
	 */
	@Override
	public JComponent getDaqSpecificDialogComponent(AcquisitionDialog acquisitionDialog)
	{
		this.acquisitionDialog = acquisitionDialog;
		if (daqDialog == null)
		{
			daqDialog = createDaqDialogPanel();
		}

		/*
		 * DOUG.
		 * while here, set the V p-p
		 */
		acquisitionDialog.setVPeak2Peak(VPEAKTOPEAK);
		return daqDialog;
	}

	/**
	 * Get the data acquisition dialog box component of this system
	 * Must include all the necessary components
	 * @return
	 */
	private JPanel createDaqDialogPanel()
	{
		/* TODO This will need some reworking to implement multiple hydrophones
		 * to be able to get multple ip addresses and other settings from each hydrophone */
		JPanel panel = new JPanel();

		/*Set up the panels layout*/
		panel.setBorder(new TitledBorder(null, "icListen", TitledBorder.LEFT, TitledBorder.TOP, null, null));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gridBagLayout);

		/* Setup the IP address field */
		JLabel lblIpAddress = new JLabel("IP Address:");
		GridBagConstraints gbc_lblIpAddress = new GridBagConstraints();
		gbc_lblIpAddress.anchor = GridBagConstraints.EAST;
		gbc_lblIpAddress.insets = new Insets(0, 0, 5, 5);
		gbc_lblIpAddress.gridx = 0;
		gbc_lblIpAddress.gridy = 0;
		panel.add(lblIpAddress, gbc_lblIpAddress);

		ipAddressInput = new JTextField();
		GridBagConstraints gbc_ipAddressInput = new GridBagConstraints();
		gbc_ipAddressInput.insets = new Insets(0, 0, 5, 5);
		gbc_ipAddressInput.fill = GridBagConstraints.HORIZONTAL;
		gbc_ipAddressInput.gridx = 1;
		gbc_ipAddressInput.gridy = 0;
		panel.add(ipAddressInput, gbc_ipAddressInput);
		ipAddressInput.setColumns(11);

		/* Setup the timeout field */
		JLabel lblConnectionTimeout = new JLabel("  Connection Timeout:");
		GridBagConstraints gbc_lblConnectionTimeout = new GridBagConstraints();
		gbc_lblConnectionTimeout.anchor = GridBagConstraints.EAST;
		gbc_lblConnectionTimeout.insets = new Insets(0, 0, 5, 5);
		gbc_lblConnectionTimeout.gridx = 2;
		gbc_lblConnectionTimeout.gridy = 0;
		panel.add(lblConnectionTimeout, gbc_lblConnectionTimeout);

		timeOutInput = new JTextField();
		GridBagConstraints gbc_timeOutInput = new GridBagConstraints();
		gbc_timeOutInput.insets = new Insets(0, 0, 5, 5);
		gbc_timeOutInput.fill = GridBagConstraints.HORIZONTAL;
		gbc_timeOutInput.gridx = 3;
		gbc_timeOutInput.gridy = 0;
		panel.add(timeOutInput, gbc_timeOutInput);
		timeOutInput.setColumns(6);

		JLabel lblMs = new JLabel("ms");
		GridBagConstraints gbc_lblMs = new GridBagConstraints();
		gbc_lblMs.insets = new Insets(0, 0, 5, 0);
		gbc_lblMs.gridx = 4;
		gbc_lblMs.gridy = 0;
		panel.add(lblMs, gbc_lblMs);

		/* Set up the waveform bandwidth field */
		JLabel lblWaveformBandwidth = new JLabel("Waveform Bandwidth:");
		GridBagConstraints gbc_lblWaveformBandwidth = new GridBagConstraints();
		gbc_lblWaveformBandwidth.anchor = GridBagConstraints.EAST;
		gbc_lblWaveformBandwidth.insets = new Insets(0, 0, 5, 5);
		gbc_lblWaveformBandwidth.gridx = 0;
		gbc_lblWaveformBandwidth.gridy = 1;
		panel.add(lblWaveformBandwidth, gbc_lblWaveformBandwidth);

		waveformBandwidth = new JComboBox();
		waveformBandwidth.addActionListener(
			new ActionListener () {
				@Override
				public void actionPerformed(ActionEvent e) {
					/* update the sampleRate text field*/
					currentSampleRateString = waveformBandwidth.getSelectedIndex();
					/* Make sure the index is valid */
					if(currentSampleRateString >= 0)
					{
						sampleRate.setText(SAMPLE_RATE_STRINGS[currentSampleRateString]);
						/*
						 * DOUG.
						 * Also at this point need to tell the main dialog what sample date you
						 * are using ! Would be better if the SAMPLE_RATE_STRINGS had been entered
						 * as numbers and converted to strings, rather than having to do this
						 * the other way around. Anyway, once you have the sample rate as a
						 * number you can call ...
						 */
						if (acquisitionDialog != null) {
							int sr = SAMPLE_RATES[currentSampleRateString];
							acquisitionDialog.setSampleRate(sr);
						}
					}
				}
			}
		);
		waveformBandwidth.setMaximumRowCount(10);
		GridBagConstraints gbc_waveformBandwidth = new GridBagConstraints();
		gbc_waveformBandwidth.insets = new Insets(0, 0, 5, 5);
		gbc_waveformBandwidth.fill = GridBagConstraints.HORIZONTAL;
		gbc_waveformBandwidth.gridx = 1;
		gbc_waveformBandwidth.gridy = 1;
		panel.add(waveformBandwidth, gbc_waveformBandwidth);

		/* Setup the sample rate field */
		JLabel lblSampleRate = new JLabel("Gives a sample rate of");
		GridBagConstraints gbc_lblSampleRate = new GridBagConstraints();
		gbc_lblSampleRate.anchor = GridBagConstraints.EAST;
		gbc_lblSampleRate.insets = new Insets(0, 0, 5, 5);
		gbc_lblSampleRate.gridx = 2;
		gbc_lblSampleRate.gridy = 1;
		panel.add(lblSampleRate, gbc_lblSampleRate);

		sampleRate = new JTextField();
		sampleRate.setEditable(false);
		GridBagConstraints gbc_sampleRate = new GridBagConstraints();
		gbc_sampleRate.insets = new Insets(0, 0, 5, 5);
		gbc_sampleRate.fill = GridBagConstraints.HORIZONTAL;
		gbc_sampleRate.gridx = 3;
		gbc_sampleRate.gridy = 1;
		panel.add(sampleRate, gbc_sampleRate);
		sampleRate.setColumns(10);

		JLabel lblHz = new JLabel("Hz");
		GridBagConstraints gbc_lblHz = new GridBagConstraints();
		gbc_lblHz.insets = new Insets(0, 0, 5, 0);
		gbc_lblHz.gridx = 4;
		gbc_lblHz.gridy = 1;
		panel.add(lblHz, gbc_lblHz);

		/* Setup the status field */
		icListenStatus = new JTextField();
		icListenStatus.setText(NO_CONNECTION_STATUS);
		icListenStatus.setEditable(false);
		GridBagConstraints gbc_icListenStatus = new GridBagConstraints();
		gbc_icListenStatus.gridwidth = 2;
		gbc_icListenStatus.insets = new Insets(0, 0, 5, 5);
		gbc_icListenStatus.fill = GridBagConstraints.HORIZONTAL;
		gbc_icListenStatus.gridx = 0;
		gbc_icListenStatus.gridy = 2;
		panel.add(icListenStatus, gbc_icListenStatus);
		icListenStatus.setColumns(10);

		/*Create a connect button*/
		btnConnect = new JButton("  Connect  ");
		btnConnect.addMouseListener(
			new MouseAdapter()
			{
				@Override
				public void mouseReleased(MouseEvent e)
				{
					if(!connected)
					{
						/* Call the connect function when the connect button is hit and
						 * it is not already connected */
						/*
						 * TODO connect to all current icListens, Loop through all of the
						 * current ones and pass the proper index to icListenConnect
						 */
						icListenConnect();
					}
				}
			}
		);
		GridBagConstraints gbc_btnConnect = new GridBagConstraints();
		gbc_btnConnect.anchor = GridBagConstraints.EAST;
		gbc_btnConnect.insets = new Insets(0, 0, 5, 5);
		gbc_btnConnect.gridx = 2;
		gbc_btnConnect.gridy = 2;
		panel.add(btnConnect, gbc_btnConnect);

		/* enable the connect button on panel  creation*/
		if(connected)
		{
			btnConnect.setEnabled(false);
		}

		/* Create a Disconnect button */
		btnDisconnect = new JButton("Disconnect");
		btnDisconnect.addMouseListener(
			new MouseAdapter()
			{
				@Override
				public void mouseReleased(MouseEvent e)
				{
					if(connected)
					{
						/* Call the disconnect function when the disconnect button is hit
						 * and a hydrophone is connected */
						/*
						 * TODO disconnect to all current icListens, Loop through all of the
						 * current ones and pass the proper index to icListenDisconnect
						 */
						icListenDisconnect();
					}
				}
			}
		);

		GridBagConstraints gbc_btnDisconnect = new GridBagConstraints();
		gbc_btnDisconnect.anchor = GridBagConstraints.WEST;
		gbc_btnDisconnect.insets = new Insets(0, 0, 5, 5);
		gbc_btnDisconnect.gridx = 3;
		gbc_btnDisconnect.gridy = 2;
		panel.add(btnDisconnect, gbc_btnDisconnect);

		/* disable disconnect button at panel creation */
		if(!connected)
		{
			btnDisconnect.setEnabled(false);
		}

		return panel;
	}

	/**
	 * Sets the fields of the data acquisition when the settings are brought up
	 */
	@Override
	public void dialogSetParams()
	{
		/* TODO This will need to be updated based on the update to the dialog box
		 * when implementing multiple channels */
		/* all the settings fields to the appropriate value */
		ipAddressInput.setText(ipAddress);
		timeOutInput.setText(Integer.toString(timeOut));

		waveformBandwidth.removeAllItems();
		for (int i = 0; i < NUMBER_OF_BANDWIDTHS; i++)
		{
			waveformBandwidth.addItem(waveformBandwidthStrings[i]);
		}

		waveformBandwidth.setSelectedIndex(currentWaveBandwidth);
	}

	/**
	 * Function gets called when the OK of the settings panel is pressed.
	 * It saves all the settings entered by the user
	 * Make sure the IP Address is set to the latest address entered by the user,
	 * Get the Time out specified and get the sample rate using the bandwidth
	 */
	@Override
	public boolean dialogGetParams()
	{
		/* TODO This will need to be updated based on the update to the dialog box
		 * when implementing multiple channels */
		ipAddress = ipAddressInput.getText();

		/* Ensure an IP address was received */
//		if(ipAddress == null || !ipAddress.matches("[0-9.]+"))
		if(ipAddress == null)
		{
			/* inform the user to input a an ip and return false */
			JOptionPane.showMessageDialog(null, "Please enter an IP Address");
			return false;
		}

		/* get time out and ensure it is a number */
		String timeOutString = timeOutInput.getText();
		if(timeOutString == null || !timeOutString.matches("[0-9]+"))
		{
			/* inform the user to input a number and return false */
			JOptionPane.showMessageDialog(null, "Please enter a number into the time out field");
			return false;
		}

		timeOut = Integer.parseInt(timeOutString);

		/* get the bandwidth selected to get the proper sample rate */
		currentWaveBandwidth = waveformBandwidth.getSelectedIndex();
		currentSampleRate = Integer.parseInt(SAMPLE_RATE_STRINGS[currentWaveBandwidth]);

		if(DEBUG)
		{
			System.out.println("IP Address: " + ipAddress + " Time Out: " + timeOut + " ms" +
						" Bandwidth Index: " + currentWaveBandwidth + " Sample rate: " + currentSampleRate);
		}

		return true;
	}

	@Override
	public int getMaxSampleRate()
	{
		/* return PARAMETER_FIXED so that the field isn't active */
		return PARAMETER_FIXED;
	}

	@Override
	public int getMaxChannels()
	{
		/* return PARAMETER_FIXED so that the field isn't active */
		return PARAMETER_FIXED;
	}

	@Override
	public double getPeak2PeakVoltage(int swChannel)
	{
		/* return PARAMETER_FIXED so that the field isn't active */
		return PARAMETER_FIXED;
	}

	/**
	 *  Connect to an icListen using the current IP
	 *  ensure that the connected variable is updated accordingly
	 */
	private void icListenConnect()
	{
		//TODO pass this the index to be connected to
		/* ensure the parameters are up to date */
		if(!dialogGetParams())
		{
			/* if an error occurs, do not connect */
			if(DEBUG)
			{
				System.out.println("An error occured getting the proper parameters");
			}
			return;
		}

		boolean reconnect = false;

		/* Connect to the icListen */
		do
		{
			try
			{
				if(DEBUG)
				{
					System.out.println("Connecting to " + ipAddress
						+ " on port " + COMMAND_PORT);
				}

				/* Connecting to command port */
				SocketAddress socketAddress = new InetSocketAddress(ipAddress, COMMAND_PORT);
				//TODO connect to the socket of the passed index
				commandSocket = new Socket();
				commandSocket.connect(socketAddress, timeOut);

				/* connected so do not attempt to reconnect */
				reconnect = false;
			}
			catch(IOException ex)
			{
				/* no connection found */
				ex.printStackTrace();

				/* See if the user would like to attempt to reconnect using the current setting,
				 * such as in the case of an unplugged cable that can be plugged back in */
				if(JOptionPane.showConfirmDialog(null,
						("Would you like to attempt to reconnect to\nicListen IP: " + ipAddress),
						 "No icListen Found", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
				{
					reconnect = true;
				}
				else
				{
					/* no connection made and no reconnect set so do not start up */
					if(DEBUG)
					{
						System.out.println("Failed to Connect");
					}
					return;
				}
			}
		} while(reconnect);

		/* Connection has been established */
		connected = true;

		if(DEBUG)
		{
			System.out.println("Just connected to "+ commandSocket.getRemoteSocketAddress());
		}

		/* Set up data streams*/
		try
		{
			//TODO create data streams for the passed index
			outToServer = commandSocket.getOutputStream();
			outStream = new DataOutputStream(outToServer);

			inFromServer = commandSocket.getInputStream();
			inStream = new DataInputStream(inFromServer);
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}

		/* create a buffer to hold the reply message */
		byte[] buffer = new byte[MAX_MESSAGE_SIZE];

		/* Set up proper communications with the icListen */
		//TODO pass this the index to send to
		sendCommandMessage(ENQUIRE_MESSAGE);

		if(DEBUG)
		{
			//TODO use the index received to get correct outStream
			System.out.println("Enquire message sent\nBytes sent to date: " + outStream.size());
		}

		/* Read the reply */
		try
		{
			//TODO use the index received to get correct inStream
			inStream.read(buffer);
		}
		catch (IOException e)
		{
			System.out.println("IOException when reading enquire response");
			e.printStackTrace();
			return;
		}

		if(DEBUG)
		{
			/* Print the reply */
			System.out.println("Enquire response received");
			printCommandMessageBuffer(buffer);
		}

		/* Check the crc of the message */
		CommandMessage enquireResponse = new CommandMessage(buffer);
		if(!checkCommandMessageCrc(enquireResponse))
		{
			/* CRC does not match, disconnect and return */
			System.out.println("CRC of the enquire message incorrect error in transmission");

			JOptionPane.showMessageDialog(null,
					"Error with command transmission.\nConnection aborted");

			if(DEBUG)
			{
				//TODO use the index received to get correct socket
				System.out.println("Disconnecting from "+ commandSocket.getRemoteSocketAddress());
			}
			try
			{
				//TODO use the index received to get correct socket
				commandSocket.close();
			}
			catch (IOException e)
			{
				if(DEBUG)
				{
					System.out.println("Disconnect failed");
				}
				e.printStackTrace();
				return;
			}
			return;
		}

		/* Message has been received and verified, obtain necessary information */
		serialNum = buffer[SERIAL_NUM_OFFSET] | (buffer[SERIAL_NUM_OFFSET + 1] << 8);
		deviceType = buffer[DEVICE_TYPE_OFFSET];

		if(DEBUG)
		{
			System.out.println("Serial Number: " + serialNum + " Device type: " + deviceType);
		}

		/* Send the job setup using the tag system */
		//TODO pass the index to the job setup in order to send it to the right icListen
		jobSetup();

		/* No longer using command port so close it */
		if(DEBUG)
		{
			//TODO use the index received to get correct socket
			System.out.println("Disconnecting from "+ commandSocket.getRemoteSocketAddress());
		}
		try
		{
			commandSocket.close();
		}
		catch (IOException e)
		{
			if(DEBUG)
			{
				System.out.println("Disconnect failed");
			}
			e.printStackTrace();
			return;
		}
		connected = false;

		if(DEBUG)
		{
			System.out.println("Disconnected from command port");
		}

		try
		{
			if(DEBUG)
			{
				System.out.println("Connecting to " + ipAddress
					+ " on port " + WAVEFORM_PORT);
			}

			/* Connecting to stream port */
			SocketAddress socketAddress = new InetSocketAddress(ipAddress, WAVEFORM_PORT);

			//TODO use the index received to connect to the proper socket
			streamSocket = new Socket();
			streamSocket.connect(socketAddress, timeOut);

		}
		catch(IOException ex)
		{
			/* no connection found */
			ex.printStackTrace();

			JOptionPane.showMessageDialog(null, "Connection Failed");
			System.out.println("Failed to connect to waveform streaming port");

			return;
		}

		/* Connection has been established */
		connected = true;

		if(DEBUG)
		{
			System.out.println("Just connected to "+ streamSocket.getRemoteSocketAddress());
		}

		/* Enable/disable the appropriate buttons/Settings in the settings panel */
		//TODO these will need to be updated based on settings implementation
		btnConnect.setEnabled(false);
		btnDisconnect.setEnabled(true);

		ipAddressInput.setEnabled(false);
		timeOutInput.setEnabled(false);
		waveformBandwidth.setEnabled(false);

		/* update the status field in the settings */
		updateStatus();

		/* Set up data streams*/
		try
		{
			//TODO create the proper data streams using the index
			outToServer = streamSocket.getOutputStream();
			outStream = new DataOutputStream(outToServer);

			inFromServer = streamSocket.getInputStream();
			inStream = new DataInputStream(inFromServer);
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * Updates the status field in the settings to
	 * give some information on the hydrophone
	 */
	private void updateStatus()
	{
		/* TODO this may need to be changed based on how getSystemName is changed */
		String status;
		/* Check to see if connected */
		if(connected)
		{
			status = getSystemName();
			icListenStatus.setText("Connected to " + status);
		}
		else
		{
			/* if there is no connection change to the proper string */
			icListenStatus.setText(NO_CONNECTION_STATUS);
		}
	}

	/**
	 * Checks the CRC of the message versus what the CRC should be
	 * If they are the same returns true if not returns false
	 * @param msg
	 * @return
	 */
	private boolean checkCommandMessageCrc(CommandMessage msg)
	{
		char[] crcReceived = new char[msg.CRC_ARRAY_LENGTH];
		char[] crcCreated = msg.calculateTxCrc(msg);

		crcReceived[0] =  (char) ((msg.crc[0]) & 0xFF);
		crcReceived[1] = (char) ((msg.crc[1]) & 0xFF);

		if(DEBUG)
		{
			System.out.printf("CRC received: %X %X CRC created: %X %X\n",
			(int)crcReceived[0], (int)crcReceived[1], (int)crcCreated[0], (int)crcCreated[1]);
		}

		return ((crcReceived[0] == crcCreated[0]) && (crcReceived[1] == crcCreated[1]));
	}

	/**
	 * Create and send the job setup using tags
	 * Setup the job using tags to setup the waveform parameters
	 * @return
	 */
	private boolean jobSetup()
	{
		//TODO pass this the index to send the setup to
		/* Get the payload length */
		char[] payloadLength = new char[]
				{0xFF & JOB_SETUP_PAYLOAD_LEN, (0xFF & (JOB_SETUP_PAYLOAD_LEN >> 8))};

		/* Create the payload array */
		char[] payload = new char[]{
			/* Enter the type of job setup */
			(char)(JOB_SETUP_TYPE & 0xFF), (char)((JOB_SETUP_TYPE >> 8) & 0xFF),

			/* The number of tags being used */
			(NUMBER_OF_TAGS & 0xFF), ((NUMBER_OF_TAGS >> 8) & 0xFF),

			/* Set up the sample rate using its tag and the sample rate from the settings */
			(SAMPLE_RATE_TAG & 0xFF), ((SAMPLE_RATE_TAG >> 8) & 0xFF),
			(TAG_VAL_LEN & 0xFF), ((TAG_VAL_LEN & 0xFF) >> 8),
			(char) (currentSampleRate & 0xFF), (char) ((currentSampleRate >> 8) & 0xFF),
			(char) ((currentSampleRate >> 16) & 0xFF), (char) ((currentSampleRate >> 24) & 0xFF),

			/* Set up the bit depth using its tag, set to 24 bit */
			(BIT_DEPTH_TAG & 0xFF), ((BIT_DEPTH_TAG >> 8) & 0xFF),
			(TAG_VAL_LEN & 0xFF), ((TAG_VAL_LEN & 0xFF) >> 8),
			(char) (BIT_DEPTH_VAL & 0xFF), (char) ((BIT_DEPTH_VAL >> 8) & 0xFF),
			(char) ((BIT_DEPTH_VAL >> 16) & 0xFF), (char) ((BIT_DEPTH_VAL >> 24) & 0xFF),

			/* Set up the digital gain using its tag, set to no gain */
			(GAIN_TAG & 0xFF), ((GAIN_TAG >> 8) & 0xFF),
			(TAG_VAL_LEN & 0xFF), ((TAG_VAL_LEN & 0xFF) >> 8),
			(char) (GAIN_VAL & 0xFF), (char) ((GAIN_VAL >> 8) & 0xFF),
			(char) ((GAIN_VAL >> 16) & 0xFF), (char) ((GAIN_VAL >> 24) & 0xFF),

			/* Set up the endianness using its tag, set to Big endian */
			(ENDIAN_TAG & 0xFF), ((ENDIAN_TAG >> 8) & 0xFF),
			(TAG_VAL_LEN & 0xFF), ((TAG_VAL_LEN >> 8) & 0xFF),
			(ENDIAN_VAL & 0xFF), ((ENDIAN_VAL >> 8) & 0xFF),
			((ENDIAN_VAL >> 16) & 0xFF), ((ENDIAN_VAL >> 24) & 0xFF),

			/* Disable logging using its tag */
			(LOGGING_MODE_TAG & 0xFF), ((LOGGING_MODE_TAG >> 8) & 0xFF),
			(TAG_VAL_LEN & 0xFF), ((TAG_VAL_LEN >> 8) & 0xFF),
			(LOGGING_MODE_VAL & 0xFF), ((LOGGING_MODE_VAL >> 8) & 0xFF),
			((LOGGING_MODE_VAL >> 16) & 0xFF), ((LOGGING_MODE_VAL >> 24) & 0xFF),
		};

		/* construct the message */
		CommandMessage jobSetup = new CommandMessage(SYNC_BYTE, JOB_SETUP_COMMAND, payloadLength, payload);

		/* send the job setup message */
		//TODO pass the send the index received to send to the correct icListen
		if(!sendCommandMessage(jobSetup))
		{
			if(DEBUG)
			{
				System.out.println("Error sending job setup");
			}
			return false;
		}

		/* create a buffer to hold the reply message */
		byte[] buffer = new byte[MAX_MESSAGE_SIZE];

		if(DEBUG)
		{
			System.out.println("Job setup message sent\nBytes sent to date: " + outStream.size());
		}

		/* Read the reply */
		try
		{
			//TODO use an index to use the proper inStream
			inStream.read(buffer);
		}
		catch (IOException e)
		{
			System.out.println("IOException when reading enquire response");
			e.printStackTrace();
			return false;
		}

		if(DEBUG)
		{
			/* Print the reply */
			System.out.println("Job setup response received");
			printCommandMessageBuffer(buffer);
		}

		/* Check the crc of the message */
		CommandMessage jobSetupResponse = new CommandMessage(buffer);
		if(!checkCommandMessageCrc(jobSetupResponse))
		{
			/* CRC does not match, disconnect and return */
			System.out.println("CRC of the enquire message incorrect error in transmission");

			JOptionPane.showMessageDialog(null,
					"Error with command transmission.\nConnection aborted");
			icListenDisconnect();
			return false;
		}

		//TODO check response setup for correct configuration

		return true;
	}

	/**
	 * Disconnect from a currently connected icListen
	 * Request the data capture to stop then close the socket
	 */
	private void icListenDisconnect()
	{
		//TODO pass this an index to tell which icListen to disconnect from
		/* request capture to stop */
		stopCapture = true;
		/* now wait for the thread to finish - when it does it
		   will set stopCapture back to false. Set max 2s timeout */
		int count = 0;
		while (stopCapture && captureRunning && ++count < 100) {
			try
			{
				Thread.sleep(20);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
			if(DEBUG)
			{
				System.out.println("Sleeping while thread exits");
			}
		}

		if(DEBUG)
		{
			//TODO use the index to get the proper socket
			System.out.println("Disconnecting from "+ streamSocket.getRemoteSocketAddress());
		}
		/* close data streams and sockets */
		//TODO use the index to get the proper connected statuses
		if(connected)
		{
			try
			{
				//TODO use the index to get the proper socket
				streamSocket.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		connected = false;

		/* Enable/disable the appropriate buttons/settings in the settings panel */
		//TODO these will need to be updated based on dialog box implementation
		btnDisconnect.setEnabled(false);
		btnConnect.setEnabled(true);

		ipAddressInput.setEnabled(true);
		timeOutInput.setEnabled(true);
		waveformBandwidth.setEnabled(true);

		/* update the status field in the settings menu */
		updateStatus();

		if(DEBUG)
		{
			System.out.println("Disconnected");
		}
		//TODO check if it was the last phone connected
		setStreamStatus(STREAM_CLOSED);
	}

	/**
	 * Prepares the system to Start its data acquisition
	 * An icListen should be connected at this point
	 * This function sets up the proper data references and updates the
	 * acquisition control's fields with the proper values
	 */
	@Override
	public boolean prepareSystem(AcquisitionControl daqControl)
	{
		/* Check to make sure an icListen is connected */
		if(!connected)
		{
			JOptionPane.showMessageDialog(null,
					"Please Connect to an icListen using the sound acquisition settings");
			return false;
		}

		this.acquisitionControl = daqControl;

		/* make sure the proper values are set in the acquisition control */
		acquisitionControl.getDaqProcess().setSampleRate(currentSampleRate, true);

		dataUnitSamples = currentSampleRate / 10;

		/* keep a reference to where data will be put. */
		// DOUG changed type of this in latest version for some better queue management
		this.newDataUnits = daqControl.getDaqProcess().getNewDataQueue();
		if (this.newDataUnits == null)
		{
			return false;
		}

		/* Preparation complete */
		return true;
	}

	/**
	 * Start the data acquisition system by telling the icListen to start streaming data
	 * then start separate threads to handle capturing data and processing it
	 * as well as sets up a queue to pass messages between these threads
	 */
	@Override
	public boolean startSystem(AcquisitionControl daqControl)
	{
		/* If not connected return false */
		if(!connected)
		{
			return false;
		}

		/* create a shared queue */
		/* TODO make this an array of queues for every channel */
		BlockingQueue<byte[]> queue = new LinkedBlockingQueue<>();

		if(DEBUG)
		{
			System.out.println("Start stream message sent\nBytes sent to date: " + outStream.size());
		}

		/* start a thread for capturing data */
		//TODO Loop through this code to create a new capture/processing thread for every channel
		Thread captureThread = new Thread(new CaptureThread(queue));
		captureThread.start();
		/* Start thread for processing data */
		//TODO pass the current index to the data process thread
		Thread dataProcessThread = new Thread(new DataProcessThread(queue));
		dataProcessThread.start();

		/* Send a message to tell the icListen to start streaming */
		sendStreamMessage(START_STREAM_MESSAGE);

		setStreamStatus(STREAM_RUNNING);
		return true;
	}

	/**
	 * Send the message that has been passed to this function
	 * This function sets up of a message buffer from a created
	 * CommandMessage class then sends it to the icListen on
	 * the command port
	 * @param msg
	 * @return
	 */
	private boolean sendCommandMessage(CommandMessage msg)
	{
		/* TODO pass this an index to send to the proper icListen */
		int payloadLength;
		int bufferIndex;

		/* get payload length
		 * assemble payload length into one number */
		payloadLength = (msg.payloadLength[0] & 0xFF)
						| (msg.payloadLength[1] & 0xFF << 8);
		/* create transmission buffer and fill it with the correct values from the message */
		byte[] buffer = new byte[msg.MIN_MESSAGE_SIZE + payloadLength];

		buffer[0] = (byte) (msg.sync & 0xFF);
		buffer[1] = (byte)(msg.command & 0xFF);
		buffer[2] = (byte)(msg.payloadLength[0] & 0xFF);
		buffer[3] = (byte)(msg.payloadLength[1] & 0xFF);

		for(bufferIndex = MSG_BEFORE_PAYLOAD;
				bufferIndex < (payloadLength + MSG_BEFORE_PAYLOAD) ; bufferIndex++)
		{
			buffer[bufferIndex] = (byte)(msg.payload[bufferIndex - MSG_BEFORE_PAYLOAD] & 0xFF);

			if(DEBUG)
			{
				System.out.println("Buffer: " + (int)buffer[bufferIndex] + " Payload: "
						+ (int)msg.payload[bufferIndex - MSG_BEFORE_PAYLOAD] + " Index: " + bufferIndex);
			}
		}

		buffer[bufferIndex] = (byte)(msg.crc[0] & 0xFF);
		buffer[bufferIndex + 1] = (byte)(msg.crc[1] & 0xFF);

		if(DEBUG)
		{
			printCommandMessageBuffer(buffer);
		}
		/* Send the created buffer */
		try
		{
			//TODO use the index to get the proper outStream
			outStream.write(buffer, 0, (msg.MIN_MESSAGE_SIZE + payloadLength));
		}
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * Print a command message given a buffer that is properly formatted
	 * Prints the messages bytes as its hexadecimal equivalents
	 * Useful for printing the contents received messages
	 * @param buf
	 * @return
	 */
	private void printCommandMessageBuffer(byte[] buf)
	{
		/* No need to print unless debugging */
		if(DEBUG)
		{
			int payloadLength;
			int bufferIndex, count = 0;

			payloadLength = (buf[2] & 0xFF) | (buf[3] & 0xFF << 8);

			System.out.println("\nPrint Message: (All bytes ordered LS->MS)");
			/* Print sync */
			System.out.printf("Sync: %02X\n", buf[0]);
			/* Print Command */
			System.out.printf("Command: %02X\n", buf[1]);
			/* Print payload length */
			System.out.printf("Payload Length: %02X %02X\n", buf[2], buf[3]);
			/* print payload */
			System.out.printf("payload: \n");

			for(bufferIndex = MSG_BEFORE_PAYLOAD;
					bufferIndex < (payloadLength + MSG_BEFORE_PAYLOAD) ; bufferIndex++)
			{
				System.out.printf("%02X ", buf[bufferIndex]);
				count++;
				if(count == 16)
				{
					System.out.printf("\n");
					count = 0;
				}
			}
			System.out.printf("\n");
			/*print crc*/
			System.out.printf("CRC: %02X %02X\n", buf[bufferIndex], buf[bufferIndex+1]);
		}
	}

	/**
	 * Print a stream message given a buffer that is properly formatted
	 * Prints the messages bytes as its hexadecimal equivalents
	 * Useful for printing the contents received messages
	 * @param buf
	 * @return
	 */
	private void printStreamMessageBuffer(byte[] buf)
	{
		/* No need to print unless debugging */
		if(DEBUG)
		{
			int payloadLength;
			int bufferIndex, count = 0;

			payloadLength = (buf[3] & 0xFF) | (buf[2] & 0xFF << 8);

			System.out.println("\nPrint Message: (All bytes ordered MS->LS)");
			/* Print type */
			System.out.printf("Type: %02X\n", buf[0]);
			/* Print sync */
			System.out.printf("Sync: %02X\n", buf[1]);
			/* Print payload length */
			System.out.printf("Payload Length: %02X %02X\n", buf[2], buf[3]);
			/* print payload */
			System.out.printf("payload: \n");

			for(bufferIndex = MSG_BEFORE_PAYLOAD;
					bufferIndex < (payloadLength + MSG_BEFORE_PAYLOAD) && bufferIndex < MAX_MESSAGE_SIZE ; bufferIndex++)
			{
				System.out.printf("%02X ", buf[bufferIndex]);
				count++;
				if(count == 16)
				{
					System.out.printf("\n");
					count = 0;
				}
			}
			System.out.printf("\n");
		}
	}

	/**
	 * Send the message that has been passed to this function
	 * This function sets up of a message buffer from a created
	 * StreamMessage class then sends it to the icListen on the
	 * waveform port
	 * @param msg
	 * @return
	 */
	private boolean sendStreamMessage(StreamMessage msg)
	{
		/* TODO pass this an index to send to the proper icListen */
		int payloadLength;
		int bufferIndex;

		/* get payload length */
		/* assemble payload length into one number */
		payloadLength = (msg.payloadLength[1] & 0xFF)
						| (msg.payloadLength[0] & 0xFF << 8);
		/* create transmission buffer */
		byte[] buffer = new byte[MSG_BEFORE_PAYLOAD + payloadLength];

		buffer[0] = (byte)(msg.type & 0xFF);
		buffer[1] = (byte) (msg.sync & 0xFF);
		buffer[2] = (byte)(msg.payloadLength[0] & 0xFF);
		buffer[3] = (byte)(msg.payloadLength[1] & 0xFF);

		for(bufferIndex = MSG_BEFORE_PAYLOAD;
				bufferIndex < (payloadLength + MSG_BEFORE_PAYLOAD) ; bufferIndex++)
		{
			buffer[bufferIndex] = (byte)(msg.payload[bufferIndex - MSG_BEFORE_PAYLOAD] & 0xFF);

			if(DEBUG)
			{
				System.out.println("Buffer: " + (int)buffer[bufferIndex] + " Payload: "
						+ (int)msg.payload[bufferIndex - MSG_BEFORE_PAYLOAD] + " Index: " + bufferIndex);
			}
		}

		if(DEBUG)
		{
			printStreamMessageBuffer(buffer);
		}
		/* Send the created buffer */
		try
		{
			//TODO use index to get proper outStream
			outStream.write(buffer, 0, (MSG_BEFORE_PAYLOAD + payloadLength));
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * Stop the data acquisition
	 * Request the capture thread to stop and wait for it to finish up
	 * then close/free all the nesc. objects
	 */
	@Override
	public void stopSystem(AcquisitionControl daqControl)
	{
		/* request capture to stop */
		stopCapture = true;
		/* now wait for the thread to finish - when it does it
		   will set stopCapture back to false.
		   Set max 2s timeout (20ms sleep 100 times) */
		int count = 0;
		while (stopCapture && captureRunning && ++count < 100) {
			try
			{
				Thread.sleep(20);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
			if(DEBUG)
			{
				System.out.println("Sleeping while thread exits");
			}
		}

		/* tell the icListen to stop the data stream */
		//TODO loop through all the channels and send the stop messages
		sendStreamMessage(STOP_STREAM_MESSAGE);

		setStreamStatus(STREAM_CLOSED);
	}

	@Override
	public boolean isRealTime()
	{
		return true;
	}

	@Override
	public boolean canPlayBack(float sampleRate)
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public int getDataUnitSamples()
	{
		return dataUnitSamples;
	}

	@Override
	public void daqHasEnded()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public String getDeviceName()
	{
		// TODO Auto-generated method stub
		return getSystemName();
	}

	@Override
	public String getUnitName()
	{
		return "icListen System";
	}

	@Override
	public String getUnitType()
	{
		return "Acquisition System";
	}

	@Override
	public Serializable getSettingsReference()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getSettingsVersion()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public PlaybackSystem getPlaybackSystem(PlaybackControl playbackControl, DaqSystem daqSystem)
	{
		// TODO Create a playback system
		return null;
	}

}
