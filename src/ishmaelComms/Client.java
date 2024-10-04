package ishmaelComms;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
//import NMEA.GpsData;
//import NMEA.GpsData.*;
import java.net.Socket;
import java.net.SocketAddress;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;



public class Client extends JFrame implements Runnable
{

	JTextArea displayArea;

	int NUMPHONES=6;
	int PHONEDIM=3;
	int NUMCMNDS=6;
	int NUMFIELDS=50;

	String serverIP;
	
	int command=0;
	int ni=0;
	int i=0;
	int j=0;
	int n=0;
    int numphones;
    int phonedim;
    


//	Common Position/Bearing variables

	float start;
	float end;
	float lofreq;
	float hifreq;
	String input;

	//int  [][] phonelocs ;
	float [][] phonelocs =new float [NUMPHONES][PHONEDIM];
	int quality;
	float speed;
	String method;
	String tag_cert;


//	NewBearing variables

	float angle;
	int ambig;
	int bearoffset;


//	NewPosition variables	
	int locdim =0;
	float xpos =0;
	float ypos =0;
	float zpos =0;
	int posoffset;

//	------------------------------------	


	String datestr;
	String timestr;
	String word;
	String key;
	String[] field = new String[NUMFIELDS];
	//String cmndlist [] = new String[NUMCMNDS];
	
	boolean GPS =true;
	boolean showlocflag =false;		
	boolean Pamdiscflag =false;

//	Socket PamSock = new Socket("137.195.182.236",8001); //nope, use loopback

	Socket PamSock;
	SocketAddress sockaddr;
	InputStream in;
	OutputStream out;

	PrintWriter pout;
	InputStreamReader isr;
	BufferedReader bin;
	String [] cmndlist  = {"IshConnected", "IshDisconnected", "UTCRequest", "UTCOK", "NewBearing", "NewPosition"};

	public Client()  //class constructor
	{
		super("PAMGUARD TCP CLIENT");
		try
		{
		

		System.setProperty("line.separator", "\r\n");

		displayArea = new JTextArea();
		add(new JScrollPane(displayArea), BorderLayout.CENTER);
		setSize(300,150);
		setVisible(true);

		

	
		serverIP = JOptionPane.showInputDialog("Enter IP address of Ishmael server");
		PamSock = new Socket(serverIP, 8001);
		
		in = PamSock.getInputStream();
		out = PamSock.getOutputStream();

		pout=new PrintWriter(out,true);
		isr = new InputStreamReader(in);
		bin =new BufferedReader(isr);
		
		pout.println("PGConnected;PGVersion;0.2;");
		Thread t = new Thread(this);
		t.start();
		}
		catch (IOException io) {
			io.printStackTrace();
		}
	
		

		
	}

	@Override
	public void run() 
	{
		

		while(!Pamdiscflag)
		{

			try
			{

//				Socket PamSock = new Socket("137.195.182.236",8001); //nope, use loopback
				if(!PamSock.isConnected())
				{
					PamSock.close();
					PamSock = new Socket(serverIP,8001);
					pout.println("PGConnected;PGVersion;0.2;");

					//SocketAddress sockaddr = new InetSocketAddress("127.0.0.1",8001);
					//PamSock.connect(sockaddr);
				}





//				Pamguard makes the phone call, so go through the formality of introductory
//				text messages, and any Pamguard initiated lineouts  (The socket connection 
//				has already been established for 
//				this to be possible).  Ishmael will send a similar message.


				

				//	Client client = new Client();

				if (showlocflag)
					pout.println("ShowLoc;");

				if(Pamdiscflag)
				{
					pout.println("PGDisconnected;");
					PamSock.close();
				}


//				Now handle lineouts from Ishmael


//				Array indexing starts with zero; substring is inclusive of first index, 
//				exclusive of last 

				String linein = bin.readLine();

				if(linein != "")
				{
					displayMessage(linein+"\n");
					for(i=0; i <= linein.length()-1; i++)
					{
						if (linein.charAt(i) != ';')
							j++;
						else if (j>0)
						{
							word = linein.substring(i-j,i);
							if(n==0) key =word;
							j=0;
							field[n]=word;
							n++;
						}
					}

					for(i=0; i<=NUMCMNDS-1; i++)
					{

						if (key.equals((cmndlist[i])))
							command = i;
					}

					//GpsData gpsData = new GpsData();
					//gpsData.getDate()
					switch (command)
					{
					case 0:                      
						//                          i.e. Ishconnected

						break;



					case 1:
						//							ie. IshDisconnected
						PamSock.close(  );
						Pamdiscflag =true;
						break;
					case 2:
						
						if(!GPS)
						{
							
						
						//					          ie. UTCRequest
//						if (day < 10)
//						{
//						// Integer.parseInt(s);
//						// String.valueOf(i)
						// Integer.tostring
						//Float.tostring


//						daystr ="0".concat(String.valueOf(day));   
//						}

//						if (month <10)
//						{
//						monthstr ="0".concat(String.valueOf(month));
//						}

//						if (hours <10)
//						{
//						hoursstr="0".concat(String.valueOf(hours));
//						}

//						if (hours <10)
//						{
//						hoursstr="0".concat(String.valueOf(hours));
//						}

//						if (hours <10)
//						{
//						hoursstr="0".concat(String.valueOf(hours));
//						}

						pout.println("UTCUpdate;1;");//.concat(datestr).concat(";").concat(timestr).concat(";"));
						break;
						} // if GPS

					case 3:
						//					          ie.  UTCOk
						break;



					case 4:
						//					         ie.  NewBearing

						//
						
						angle =Float.parseFloat(field[1]);
						start =Float.parseFloat(field[2]);
						end =Float.parseFloat(field[3]);
						lofreq=Float.parseFloat(field[4]);
						hifreq=Float.parseFloat(field[5]);
						ambig=Integer.parseInt(field[6]);
						input =field[7];
						numphones=Integer.parseInt(field[8]);
						phonedim=Integer.parseInt(field[9]);
						

						for (i=0; i<=(numphones-1);i++)
						{
							for (j=0; j<=phonedim-1; j++)
							{
								phonelocs[i][j] =Float.parseFloat(field[10+3*i +j]);
//								System.out.println(phonelocs[i][j]);
							}
						}
							
						i=i-1; j=j-1;
						bearoffset =10+3*i+j;
						quality=Integer.parseInt(field[bearoffset+1]);
						speed =Float.parseFloat(field[bearoffset+2]);
						method =field[bearoffset+3];
						tag_cert =field[bearoffset+4];
//						System.out.println(angle+","+start+","+end+","+lofreq+","+hifreq+","+  
//								ambig+","+input+","+numphones+","+phonedim+","+bearoffset+","+ 
//								quality+","+speed+","+method+","+tag_cert);
						i=0;j=0;
						

						break;


					case 5:
//						ie.  NewPosition
						locdim =Integer.parseInt(field[1]);
						xpos = Float.parseFloat(field[2]);
						ypos =Float.parseFloat(field[3]);
						zpos = Float.parseFloat(field[4]);

						start =Float.parseFloat(field[5]);
						end =Float.parseFloat(field[6]);
						lofreq=Float.parseFloat(field[7]);
						hifreq=Float.parseFloat(field[8]);

						input =field[9];
						phonedim=Integer.parseInt(field[10]);
						numphones=Integer.parseInt(field[11]);


						for (i=0; i<=numphones -1;i++)
						{
							for (j=0; j<=phonedim-1; j++)
							{
								phonelocs[i][j] =Float.parseFloat(field[12+3*i +j]);
//							System.out.println(phonelocs[i][j]);
							}

						}
						i=i-1;j=j-1;
						posoffset =12+3*i+j;

						quality=Integer.parseInt(field[posoffset+1]);
						speed =Float.parseFloat(field[posoffset+2]);
						method =field[posoffset+3];
						tag_cert =field[posoffset+4];
						
//						System.out.println(locdim+","+xpos+","+ypos+","+zpos+","+start+","+
//								end+","+lofreq+","+hifreq+","+  
//								input+","+phonedim+","+numphones+","+posoffset+","+ 
//								quality+","+speed+","+method+","+tag_cert);
						i=0;j=0;

						break;

					default:
						displayMessage("invalid command");

					}  //switch

					n=0;
					linein="";

				} //  if linein...


				//	isr.read(ibuf,0,100);



				//	System.out.println(ibuf);







//				PamSock.close(  );
			} // try

			catch (IOException io) // If any errors, exit threaded loop and try again

			{
				closeConnection();

			}


		} // while(true)


	} //runClient
	
	void displayMessage(final String incoming)
	{
		SwingUtilities.invokeLater
		(
				new Runnable()
				{
					@Override
					public void run()
					{
						displayArea.append(incoming);
					}
				}
		);
	}

		

	void closeConnection()
	{
		try
		{
			PamSock.close();
		}

		catch(IOException io){}

	}
	
	public static void main(String args[])
	{
		
	    Client PGClient =new Client();
		PGClient.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		

	}

} //Client

