package ishmaelComms;

import ishmaelComms.IshmaelData.IshmaelDataType;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;


import PamUtils.PamCalendar;
import PamView.symbol.StandardSymbolManager;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import autecPhones.AutecGraphics;

public class ProcessIshmaelData extends PamProcess implements ActionListener {

	PamDataBlock<IshmaelDataUnit> ishmaelDataBlock;

	IshmaelDataControl ishmaelController;
	private Thread ishmaelClientThread;
	
	volatile Vector<IshmaelData> newIshmaelData = new Vector<IshmaelData>();
	
	Timer timer;

	@Override
	public void pamStart() {
	}

	@Override
	public void pamStop() {
	}

	/**
	 * @param pamControlledUnit
	 *            Reference to the NMEAController
	 */
	ProcessIshmaelData(IshmaelDataControl pamControlledUnit) {
		super(pamControlledUnit, null);
		
		ishmaelController = pamControlledUnit;

		setProcessName("Ishmael data processing");

		addOutputDataBlock((ishmaelDataBlock = new PamDataBlock<IshmaelDataUnit>(IshmaelDataUnit.class,
				 "Ishmael Data", this, 1)));
		
		ishmaelDataBlock.setOverlayDraw(new IshmaelGraphics(ishmaelController));
		ishmaelDataBlock.setPamSymbolManager(new StandardSymbolManager(ishmaelDataBlock, IshmaelGraphics.defaultSymbol, true));

		ishmaelDataBlock.setNaturalLifetime(600);
		
		timer = new Timer(100, this);
		
		timer.start();
		
		ishmaelClientThread = new Thread(new Client());
		ishmaelClientThread.start();
		
	}


	public void actionPerformed(ActionEvent e) {

		while(newIshmaelData.size() > 0) {
			//	PR IF VALID ISHMAEL DATA create a data unit here
			IshmaelDataUnit newDataUnit = new IshmaelDataUnit(PamCalendar
					.getTimeInMillis());
			
			newDataUnit.setIshmaelData(newIshmaelData.get(0));
			
			newIshmaelData.remove(0);

			// newUnit.data;  = stringBuffer; Assign some values to the data unit
			ishmaelDataBlock.addPamData(newDataUnit);
			
		}
		
	}
	
	// Ishmael Client
	// TODO GUI debug code to be Relocated/Recoded
	class Client implements Runnable {
		JFrame ishFrame = new JFrame();

		JTextArea displayArea;

		int NUMCMNDS = 6;
		int NUMFIELDS = 50;

		String serverIP;

		int command = 0;

		int ni = 0;
		int i = 0;
		int j = 0;
		int n = 0;

		// ------------------------------------

		String datestr;
		String timestr;
		String word;
		String key;

		String[] field = new String[NUMFIELDS];

		// String cmndlist [] = new String[NUMCMNDS];

		boolean GPS = true;

		boolean showlocflag = false;

		boolean Pamdiscflag = false;

		// Socket PamSock = new Socket("137.195.182.236",8001); //nope, use
		// loopback

		Socket PamSock;

		SocketAddress sockaddr;

		InputStream in;

		OutputStream out;

		PrintWriter pout;

		InputStreamReader isr;

		BufferedReader bin;

		String[] cmndlist = { "IshConnected", "IshDisconnected", "UTCRequest",
				"UTCOK", "NewBearing", "NewPosition" };

		public Client() {
			// super("PAMGUARD TCP CLIENT");
		} // constructor

		public void run() {

			try {
				System.setProperty("line.separator", "\r\n");

				//displayArea = new JTextArea();
				//ishFrame.add(new JScrollPane(displayArea), BorderLayout.CENTER);
				//ishFrame.setSize(300, 150);
			//	ishFrame.setVisible(true);

				serverIP = JOptionPane
						.showInputDialog("Enter IP address of Ishmael server");
				PamSock = new Socket(serverIP, 8001);

				in = PamSock.getInputStream();
				out = PamSock.getOutputStream();

				pout = new PrintWriter(out, true);
				isr = new InputStreamReader(in);
				bin = new BufferedReader(isr);

				pout.println("PGConnected;PGVersion;0.2;");
			//	Thread t = new Thread(this);
				//t.start();
			} catch (IOException io) {
				JOptionPane.showMessageDialog(null,
						"DataSource Error: " + io.getLocalizedMessage()
		  	            + "\n" + io.toString(),
		  	            getProcessName(),
		  	            JOptionPane.ERROR_MESSAGE);    
				io.printStackTrace();
			} // constructor

			while (!Pamdiscflag) {
				IshmaelData ishData = new IshmaelData();
				try {
					// Socket PamSock = new Socket("137.195.182.236",8001);
					// //nope, use loopback
					if (!PamSock.isConnected()) {
						PamSock.close();
						PamSock = new Socket(serverIP, 8001);
						pout.println("PGConnected;PGVersion;0.2;");

						// SocketAddress sockaddr = new
						// InetSocketAddress("127.0.0.1",8001);
						// PamSock.connect(sockaddr);
					}

					// Pamguard makes the phone call, so go through the
					// formality of introductory
					// text messages, and any Pamguard initiated lineouts (The
					// socket connection
					// has already been established for
					// this to be possible). Ishmael will send a similar message

					// Client client = new Client();

					if (showlocflag)
						pout.println("ShowLoc;");

					if (Pamdiscflag) {
						pout.println("PGDisconnected;");
						PamSock.close();
					}

					// Now handle lineouts from Ishmael

					// Array indexing starts with zero; substring is inclusive
					// of first index,
					// exclusive of last

					String linein = bin.readLine();

					if (linein != "") {
//						System.out.println(linein+"\n");
						for (i = 0; i <= linein.length() - 1; i++) {
							if (linein.charAt(i) != ';')
								j++;
							else if (j > 0) {
								word = linein.substring(i - j, i);
								if (n == 0)
									key = word;
								j = 0;
								field[n] = word;
								n++;
							}
						}

						for (i = 0; i <= NUMCMNDS - 1; i++) {

							if (key.equals((cmndlist[i])))
								command = i;
						}

						// GpsData gpsData = new GpsData();
						// gpsData.getDate()
						switch (command) {
						case 0:
							// i.e. Ishconnected

							break;

						case 1:
							// ie. IshDisconnected
							PamSock.close();
							Pamdiscflag = true;
							break;
						case 2:

							if (!GPS) {


								pout.println("UTCUpdate;1;");// .concat(datestr).concat(";").concat(timestr).concat(";"));
								break;
							} // if GPS

						case 3:
							// ie. UTCOk
							break;

						case 4:
							// ie. NewBearing

							//
							ishData.ishmaelDataType = IshmaelDataType.NEWBEARING;
							ishData.angle = Float.parseFloat(field[1]);
							ishData.start = Float.parseFloat(field[2]);
							ishData.end = Float.parseFloat(field[3]);
							ishData.lofreq = Float.parseFloat(field[4]);
							ishData.hifreq = Float.parseFloat(field[5]);
							ishData.ambig = Integer.parseInt(field[6]);
							ishData.input = field[7];
							ishData.numphones = Integer.parseInt(field[8]);
							ishData.phonedim = Integer.parseInt(field[9]);

							for (i = 0; i <= (ishData.numphones - 1); i++) {
								for (j = 0; j <= ishData.phonedim - 1; j++) {
									ishData.phonelocs[i][j] = Float.parseFloat(field[10
											+ 3 * i + j]);
									//System.out.println(ishData.phonelocs[i][j]);
								}
							}

							i = i - 1;
							j = j - 1;
							ishData.bearoffset = 10 + 3 * i + j;
							ishData.quality = Integer.parseInt(field[ishData.bearoffset + 1]);
							ishData.speed = Float.parseFloat(field[ishData.bearoffset + 2]);
							ishData.method = field[ishData.bearoffset + 3];
							ishData.tag_cert = field[ishData.bearoffset + 4];
							//System.out.println(ishData.angle + "," + ishData.start + "," + ishData.end
								//	+ "," + ishData.lofreq + "," + ishData.hifreq + "," + ishData.ambig
								//	+ "," + ishData.input + "," + ishData.numphones + ","
								//	+ ishData.phonedim + "," + ishData.bearoffset + ","
								//	+ ishData.quality + "," + ishData.speed + "," + ishData.method
								//	+ "," + ishData.tag_cert);
							i = 0;
							j = 0;

							break;

						case 5:
							// ie. NewPosition
							ishData.ishmaelDataType = IshmaelDataType.NEWPOSITION;
							
							ishData.locdim = Integer.parseInt(field[1]);
							ishData.xpos = Float.parseFloat(field[2]);
							ishData.ypos = Float.parseFloat(field[3]);
							ishData.zpos = Float.parseFloat(field[4]);

							ishData.start = Float.parseFloat(field[5]);
							ishData.end = Float.parseFloat(field[6]);
							ishData.lofreq = Float.parseFloat(field[7]);
							ishData.hifreq = Float.parseFloat(field[8]);

							ishData.input = field[9];
							ishData.phonedim = Integer.parseInt(field[10]);
							ishData.numphones = Integer.parseInt(field[11]);

							for (i = 0; i <= ishData.numphones - 1; i++) {
								for (j = 0; j <= ishData.phonedim - 1; j++) {
									ishData.phonelocs[i][j] = Float.parseFloat(field[12
											+ 3 * i + j]);
									//System.out.println(ishData.phonelocs[i][j]);
								}

							}
							i = i - 1;
							j = j - 1;
							ishData.posoffset = 12 + 3 * i + j;

							ishData.quality = Integer.parseInt(field[ishData.posoffset + 1]);
							ishData.speed = Float.parseFloat(field[ishData.posoffset + 2]);
							ishData.method = field[ishData.posoffset + 3];
							ishData.tag_cert = field[ishData.posoffset + 4];

							//System.out.println(ishData.locdim + "," + ishData.xpos + "," + ishData.ypos
								//	+ "," + ishData.zpos + "," + ishData.start + "," + ishData.end
								//	+ "," + ishData.lofreq + "," + ishData.hifreq + "," + ishData.input
								//	+ "," + ishData.phonedim + "," + ishData.numphones + ","
								//	+ ishData.posoffset + "," + ishData.quality + "," + ishData.speed
								//	+ "," + ishData.method + "," + ishData.tag_cert);
							i = 0;
							j = 0;

							break;

						default:
							displayMessage("invalid command");

						} // switch

						if (ishData.ishmaelDataType == IshmaelData.IshmaelDataType.NEWBEARING ||
								ishData.ishmaelDataType == IshmaelData.IshmaelDataType.NEWPOSITION) {
							newIshmaelData.add(ishData);
						}

						n = 0;
						linein = "";
						

					} // if linein...

					// isr.read(ibuf,0,100);

					// System.out.println(ibuf);

					// PamSock.close( );
				} // try

				catch (IOException io) // If any errors, exit threaded loop and
				// try again
				{
					JOptionPane.showMessageDialog(null,
							"DataSource Error: " + io.getLocalizedMessage()
			  	            + "\n" + io.toString(),
			  	            getProcessName(),
			  	            JOptionPane.ERROR_MESSAGE);    
					
					io.printStackTrace( );
					closeConnection();
				}
				
				
			} // while(true)

		} // runClient

		
		//===================================================
		void displayMessage(final String incoming) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					displayArea.append(incoming);
				}
			});
		}

		//===================================================
		void closeConnection() {
			try {
				PamSock.close();
			} catch (IOException io) {
			}
		}
		
	}
}
