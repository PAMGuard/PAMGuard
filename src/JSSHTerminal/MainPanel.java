package JSSHTerminal;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;

/**
 * SSH Terminal frame
 *
 * @author JL PONS
 * 
 * Adapted Doug Gillespie to separate JFrame from the working part of this so that I can more
 * easily incorporate the terminal as a component within more complex GUI. 
 * Stop extending JFrame and make available a Swing component instead.  
 */

public class MainPanel extends JPanel implements AdjustmentListener,MouseWheelListener {

	public static final String DEFAULT_VERSION = "-.-";
	public static final String VERSION = getVersion();
	final static boolean isWindows;

	private JFrame parentFrame;
	private JPanel contentPanel;

	private TerminalEvent textArea;
	private JScrollBar    scrollBar;
	private SSHSession    session;
	private String        _host;
	private String        _user;
	private String        _password;
	private boolean       exitOnClose = false;
	private boolean       scrollUpdate;
	private String        command = null;

	static {
		String OS_NAME = System.getProperty("os.name");
		String _OS_NAME = OS_NAME.toLowerCase();
		isWindows = _OS_NAME.startsWith("windows");
	}

	/**
	 * Construct a SSH terminal frame
	 * @param parentFrame Parent frame - this will need to set up a couple of listeners on the frame, so it's needed. 
	 * @param host Host to connect
	 * @param user Username
	 * @param password Password (if null, password will be prompted)
	 * @param width Terminal width (character)
	 * @param height Terminal height (character)
	 * @param scrollSize ScrollBar height (lines)
	 * @param center Center on screen
	 */
	public MainPanel(JFrame parentFrame, String host, String user, String password, int width, int height, int scrollSize, boolean center) {

		_host = host;
		_user = user;
		_password = password;

		// Use a TextTerminal without antialiasaed font under X11
		if(!isWindows)
			textArea = new TextTerminal(this,width,height);
		else
			textArea = new GraphicTerminal(this,width,height);

		session = new SSHSession(this,width,height,scrollSize);
		textArea.setSession(session);

		contentPanel = this;
		contentPanel.setLayout(new BorderLayout());
		contentPanel.add(textArea, BorderLayout.CENTER);
		contentPanel.setBackground(Color.BLACK);
		scrollBar = new JScrollBar();
		scrollBar.setMinimum(0);
		scrollBar.setMaximum(height);
		scrollBar.setValue(0);
		scrollBar.setVisibleAmount(height);
		scrollBar.addAdjustmentListener(this);
		scrollUpdate = false;
		contentPanel.add(scrollBar,BorderLayout.EAST);
		//    setTitle("JSSHTerminal " + VERSION + " " + user + "@" + host);

		if (parentFrame != null) {
			parentFrame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowOpened(WindowEvent e) {
					openTerminal();
					super.windowOpened(e);
				}

				@Override
				public void windowClosing(WindowEvent e) {
					exitFrame();
				}

			});
			addMouseWheelListener(this);

			parentFrame.pack();
//			if(center)
//				parentFrame.setLocationRelativeTo(null);
//			parentFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

		}
	}
	
	public void openTerminal() {
		try {
			session.connect(_host, _user, _password);
			if(command!=null) session.execCommand(command);
			textArea.notifySizeChange();
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(null, "Cannot connect :" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			//          setVisible(false);
			exitFrame();
		}
	}

	/**
	 * Construct a SSH terminal frame
	 * @param host Host to connect
	 * @param user Username
	 * @param password Password (if null, password will be prompted)
	 * @param width Terminal width (character)
	 * @param height Terminal height (character)
	 * @param scrollSize ScrollBar height (lines)
	 */
	public MainPanel(JFrame parentFrame, String host, String user, String password, int width, int height, int scrollSize) {
		this(parentFrame, host,user,password,width,height,scrollSize,true);
	}

	/**
	 * Exit when terminal is closed or exit
	 * @param exitOnClose
	 */
	public void setExitOnClose(boolean exitOnClose) {
		this.exitOnClose = exitOnClose;
	}

	/**
	 * Execute the given command after connection
	 * @param cmd Command to be executed (Do not add \n at the end)
	 */
	public void setCommand(String cmd) {
		command = cmd;
	}

	/**
	 * Sets the SSH port
	 * @param port Port number
	 */
	public void setSSHPort(int port) {
		session.setSshPort(port);
	}

	/**
	 * Automatically answer yes to question
	 * @param enable Enalbe auto yes
	 */
	public void setAnswerYes(boolean enable) {
		session.setAnswerYes(enable);
	}

	/**
	 * Enable X11 forwarding, multi display not supported
	 * @param enable
	 */
	public void setX11Forwarding(boolean enable) {
		session.setX11Forwarding(enable);
	}

	void setExtraTilte(String tilte) {
		if (parentFrame != null) {
			parentFrame.setTitle("JSSHTerminal " + VERSION + " " + tilte);
		}
	}

	void exitFrame() {
		/**
		 * DG - get rid of all this except the close session stuff. 
		 */
		if(session!=null)
			session.close();
		textArea.dispose();
//		if(exitOnClose) System.exit(0);
//		else parentFrame.setVisible(false);
	}

	@Override
	public void adjustmentValueChanged(AdjustmentEvent e) {
		if(!scrollUpdate) {
			int sbValue = e.getValue();
			textArea.setScrollPos(sbValue);
		}
	}

	void updateScrollBar() {
		scrollUpdate = true;
		int scrollPos = textArea.scrollPos;
		int scrollSize = textArea.terminal.getScrollSize();
		int height = textArea.termHeight;
		scrollBar.setValues(scrollSize - scrollPos - height, height, 0, scrollSize);
		scrollUpdate = false;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		textArea.moveScroll(-3*e.getWheelRotation());
		updateScrollBar();
		e.consume();
	}

	private static String getVersion() {
		Package p = MainPanel.class.getPackage();

		//if version is set in MANIFEST.mf
		if(p.getImplementationVersion() != null) return p.getImplementationVersion();

		return DEFAULT_VERSION;
	}
	// ----------------------------------------------------------------------

	static void dumpCharSet(String set) {

		System.out.print("\u001B[m");
		System.out.print("\u001B[0m");
		System.out.println("---------- Set ------- " + set);
		System.out.print("\u001B"+set);

		char c = 32;
		for(int i=0;i<6;i++) {
			for(int j=0;j<16;j++) {
				if(c<127) System.out.print(c);
				c++;
			}
			System.out.println();
		}

		// Bold
		System.out.print("\u001B[1m");
		c = 32;
		for(int i=0;i<6;i++) {
			for(int j=0;j<16;j++) {
				if(c<127) System.out.print(c);
				c++;
			}
			System.out.println();
		}

		System.out.print("\u001B(A");

	}

	static void dumpCharSets() {

		dumpCharSet("(A");
		dumpCharSet(")A");
		dumpCharSet("(B");
		dumpCharSet(")B");
		dumpCharSet("(0");
		dumpCharSet(")0");
		dumpCharSet("(1");
		dumpCharSet(")1");
		dumpCharSet("(2");
		dumpCharSet(")2");

	}

	// ----------------------------------------------------------------------

	public static void printUsage() {

		System.out.println("Usage: jterminal username@host [-p password] [-P port] [-y] [-s WxHxS] [-X] [-c command]");
		System.out.println("       username@host username used to login on host");
		System.out.println("       -p password password used to login");
		System.out.println("       -P SSH port number (default is 22)");
		System.out.println("       -y Answer yes to question");
		System.out.println("       -s WxHxS terminal size WidthxHeightxScrollbar");
		System.out.println("       -X Enable X11 forwarding");
		System.out.println("       -c command Execute command after connection");
		System.exit(0);

	}

	public static void main(String[] args) {

		int W = 80;
		int H = 24;
		int S = 500;
		int P = 22;
		String password = null;
		boolean yes = false;
		boolean X11 = false;
		String command = null;

		if(args.length==0)
			printUsage();

		String[] uh = args[0].split("@");
		if(uh.length!=2)
			printUsage();

		int argc = 1;
		while( argc<args.length ) {

			if(args[argc].equals("-y")) {
				yes = true;
				argc++;
				continue;
			} if(args[argc].equals("-X")) {
				X11 = true;
				argc++;
				continue;
			} else if( args[argc].equals("-p") ) {
				if(argc+1<args.length)
					password = args[argc+1];
				else
					printUsage();
				argc+=2;
				continue;
			} else if( args[argc].equals("-c") ) {
				if(argc+1<args.length)
					command = args[argc+1];
				else
					printUsage();
				argc+=2;
				continue;
			} else if( args[argc].equals("-P") ) {
				if(argc+1<args.length)
					P = Integer.parseInt(args[argc+1]);
				else
					printUsage();
				argc+=2;
				continue;
			} else if( args[argc].equals("-s") ) {
				if(argc+1<args.length) {
					String sz[] = args[argc+1].split("x");
					if(sz.length==3) {
						W = Integer.parseInt(sz[0]);
						H = Integer.parseInt(sz[1]);
						S = Integer.parseInt(sz[2]);
					} else
						printUsage();
				} else
					printUsage();
				argc+=2;
				continue;
			} else {
				System.out.println("Invalid option " + args[argc]);
				printUsage();
			}

		}
		openTerminal(uh[1],uh[0],password,P,W,H,S);
	}

	
	public static void openTerminal(String host, String user, String password) {
		openTerminal(host, user, password, 22, 80, 24, 500);
	}
	
	public static void openTerminal(String host, String user, String password, int port, int width, int height, int scrollSize) {
		
		JFrame f = new JFrame("Terminal: " + host);
//		f.setIconImage(image);
		f.setSize(600, 400);

		final MainPanel mainPanel = new MainPanel(f,host,user,password,width, height,scrollSize);
		f.getContentPane().add(mainPanel.getContentPanel());
		mainPanel.setExitOnClose(true);
		mainPanel.setSSHPort(port);
		mainPanel.setAnswerYes(false);
		mainPanel.setX11Forwarding(false);
//		mainPanel.setCommand(command);
		f.pack();
		f.setVisible(true);

	}

	/**
	 * @return the contentPanel
	 */
	public JPanel getContentPanel() {
		return contentPanel;
	}

	/**
	 * @return the _host
	 */
	public String get_host() {
		return _host;
	}

}
