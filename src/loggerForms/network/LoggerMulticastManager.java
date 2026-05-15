package loggerForms.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.zip.CRC32;
import java.util.zip.CRC32C;

import javafx.util.Duration;

public class LoggerMulticastManager extends LoggerNetworkManager {
	
	private static final int BUFFLEN = 1024*1024;
	private ArrayList<MulticastUser> users = new ArrayList<>();
	private Thread listenerThread;
	
	private MulticastSocket multicastSocket;
	
	private CRC32C crc32 = new CRC32C();
	
	public LoggerMulticastManager() {
		setupListener();
	}

	@Override
	public boolean sendData(String station, String topic, byte[] payload) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void subsribeTopic(String topic, LoggerNetworkReceiver messageUser) {
		users.add(new MulticastUser(topic, messageUser));
	}

	@Override
	public boolean unsubscribeTopic(String topic, LoggerNetworkReceiver loggerReceiver) {
		if (topic == null) {
			return unsubscribeUser(loggerReceiver);
		}
		else if (loggerReceiver == null) {
			return unsubscribeTopic(topic);
		}
		else {
			int n = 0;
			Iterator<MulticastUser> li = users.iterator();
			while (li.hasNext()) {
				MulticastUser user = li.next();
				if (topic.equals(user.topic) && loggerReceiver == user.loggerNetworkReceiver) {
					li.remove();
					n++;
				}
			}
			return n>0;
		}
	}

	/**
	 * Unsubscribe all users with given topic (maybe > 1)
	 * @param topic
	 * @return
	 */
	private boolean unsubscribeTopic(String topic) {
		int n = 0;
		Iterator<MulticastUser> li = users.iterator();
		while (li.hasNext()) {
			MulticastUser user = li.next();
			if (topic.equals(user.topic)) {
				li.remove();
				n++;
			}
		}
		return n>0;
	}

	private boolean unsubscribeUser(LoggerNetworkReceiver loggerReceiver) {
		int n = 0;
		Iterator<MulticastUser> li = users.iterator();
		while (li.hasNext()) {
			MulticastUser user = li.next();
			if (loggerReceiver == user.loggerNetworkReceiver) {
				li.remove();
				n++;
			}
		}
		return n>0;
	}

	private class MulticastUser {
		private String topic;
		private LoggerNetworkReceiver loggerNetworkReceiver;
		/**
		 * @param topic
		 * @param loggerNetworkReceiver
		 */
		public MulticastUser(String topic, LoggerNetworkReceiver loggerNetworkReceiver) {
			super();
			this.topic = topic;
			this.loggerNetworkReceiver = loggerNetworkReceiver;
		}
	}

	@Override
	public boolean closeListener() {
		if (listenerThread == null || multicastSocket == null) {
			return true;
		}
		boolean ok = true;
		multicastSocket.close();
		try {
			listenerThread.join(3000);
		} catch (InterruptedException e) {
			System.out.println(e.getMessage());
			ok = false;
		}
		multicastSocket = null;
		listenerThread = null;
		return ok;
	}

	@Override
	public boolean setupListener() {
		closeListener();
		listenerThread = new Thread(new Runnable() {
			@Override
			public void run() {
				runListenerLoop();
			}
		});
		listenerThread.start();
		return false;
	}

	protected void runListenerLoop() {
		
		byte[] buff = new byte[BUFFLEN];
		InetAddress group = null;
		try {
			multicastSocket = new MulticastSocket(4446);
			group = InetAddress.getByName("230.0.0.0");
			multicastSocket.joinGroup(group);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		System.out.println("Enter logger multicast receive loop");
        while (true) {
            DatagramPacket packet = new DatagramPacket(buff, buff.length);
            try {
				multicastSocket.receive(packet);
			} catch (IOException e) {
				break;
			}
            usePacket(packet);
            
        }
        try {
			multicastSocket.leaveGroup(group);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        multicastSocket.close();
		System.out.println("Leave logger multicast receive loop");
		
	}

	/**
	 * Use a received datagram packet. 
	 * @param packet
	 */
	private void usePacket(DatagramPacket packet) {
		LoggerNetworkMessage msg = unpackDatagram(packet);
		if (msg == null) {
			return;
		}
		/*
		 *  might decide to queue the data here to free the socket to receive ?
		 *  Might help packet delivery if datarates are high. Don't bother for now though.  
		 */
		int nUsers = 0;
		int userErrors = 0;
		Iterator<MulticastUser> it = users.iterator();
		while (it.hasNext()) {
			MulticastUser user = it.next();
			if (topicMatch(msg.getTopic(), user.topic)) {
				boolean ok = user.loggerNetworkReceiver.newMessage(msg);
				if (ok == false) {
					userErrors++;
				}
				nUsers++;
			}
		}
		if (nUsers == 0) {
			System.out.printf("Message %s had no users\n", msg);
		}
		
	}
	
	private boolean topicMatch(String topic, String topic2) {
		return topic.equals(topic2);
	}

	private LoggerNetworkMessage unpackDatagram(DatagramPacket packet) {
		
		byte[] data = packet.getData();
		int len = packet.getLength();
		
		ByteArrayInputStream bis = new ByteArrayInputStream(data, 0, len);
		// then read the stream. Assuming in network byte order 
		DataInputStream dis = new DataInputStream(bis);
		String topic = null;
		byte[] payload = null;
		// this is a bit silly, but might as well try a slightly organised system for unpacking. 
		byte[] bitName = new byte[4];
		int nRead;
		boolean checksumwarning = false;
		try {
			while (bis.available() > 0) {
				nRead = dis.read(bitName);
				String item = new String(bitName);
				int dataLen = dis.readInt();
				byte[] itemData = new byte[dataLen];
				dis.read(itemData);
				int checksum = dis.readInt();
				long checksumL = Integer.toUnsignedLong(checksum);
				crc32.reset();
				crc32.update(itemData);
				long chk = crc32.getValue();
				if (checksumL != chk) {
					checksumwarning = true;
				}
				switch (item) {
				case "TOPC":
					topic = new String(itemData);
					break;
				case "DATA":
					payload = itemData;
					break;
				default:
					System.out.printf("Unknown item \"%s\" length %d in Multicast received data\n", item, dataLen);
					break;
				}
			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return new LoggerNetworkMessage(topic, payload, checksumwarning ? "Checksum warning" : null);		
	}
}
