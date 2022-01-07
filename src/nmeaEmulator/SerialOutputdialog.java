package nmeaEmulator;

public class SerialOutputdialog {// extends PamDialog{
	
//	private static SerialOutputdialog singleInstace; 
//	
//	private SerialOutputParameters serialOutputParameters;
//	
//	private JComboBox comPorts;
//	
//	private JComboBox baudRate;
//	
//	private int[] bitsPerSecondList = {110, 300, 1200, 2400, 4800, 9600, 
//			19200, 38400, 57600, 115200, 230400, 460800, 921600};
//
//	private ArrayList<String> portList;
//
//	private SerialOutputdialog(Window parentFrame) {
//		super(parentFrame, "Serial Output", false);
//		
//		JPanel p = new JPanel(new GridBagLayout());
//		p.setBorder(new TitledBorder("Serial output options"));
//		GridBagConstraints c = new PamGridBagContraints();
//		addComponent(p, new JLabel("Port ", JLabel.RIGHT), c);
//		c.gridx++;
//		addComponent(p, comPorts = new JComboBox(), c);
//		c.gridx = 0;
//		c.gridy++;
//		addComponent(p, new JLabel("Baud Rate ", JLabel.RIGHT), c);
//		c.gridx++;
//		addComponent(p, baudRate = new JComboBox(), c);
//
//		portList = SerialPortCom.getPortArrayList();
//		for(int i = 0; i<portList.size(); i++){
//			comPorts.addItem(portList.get(i));
//		}
//		
//		for (int i = 0; i < bitsPerSecondList.length; i++) {
//			baudRate.addItem(bitsPerSecondList[i]);
//		}
//		
//		setDialogComponent(p);
//	}
//	
//	public static SerialOutputParameters showDialog(Window parentFrame, SerialOutputParameters serialOutputParameters) {
//		if (singleInstace == null || singleInstace.getOwner() != parentFrame) {
//			singleInstace = new SerialOutputdialog(parentFrame);
//		}
//		singleInstace.serialOutputParameters = serialOutputParameters.clone();
//		singleInstace.setParams();
//		singleInstace.setVisible(true);
//		return singleInstace.serialOutputParameters;
//	}
//
//	@Override
//	public void cancelButtonPressed() {
//		serialOutputParameters = null;
//	}
//	
//	private void setParams() {
//		baudRate.setSelectedItem(serialOutputParameters.baudRate);
//		for (int i = 0; i < portList.size(); i++) {
//			if (portList.get(i).equals(serialOutputParameters.portName)) {
//				comPorts.setSelectedIndex(i);
//			}
//		}
//	}
//
//	@Override
//	public boolean getParams() {
//		serialOutputParameters.baudRate = (Integer) baudRate.getSelectedItem();
//		int iPort = comPorts.getSelectedIndex();
//		if (iPort < 0) {
//			return this.showWarning("No COM port selected");
//		}
//		serialOutputParameters.portName = portList.get(iPort);
//		return true;
//	}
//
//	@Override
//	public void restoreDefaultSettings() {
//		// TODO Auto-generated method stub
//		
//	}
//
//	
}
