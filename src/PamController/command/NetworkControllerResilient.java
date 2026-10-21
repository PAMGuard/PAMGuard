package PamController.command;

import PamController.PamController;

/**
 * Same as network controller with some additional resilience to bad commands and allowing for 
 * the external controller to poll for initialization completion 
 * 
 * @author tabbutt
 */
public class NetworkControllerResilient extends NetworkController {

	public NetworkControllerResilient(PamController pamController) {
		super(pamController);
	}

	/**
	 * Infinite loop. The program sits here waiting for
	 * commands and interpreting them as needs. 
	 * <br>It will exit when InterpretCommand returns
	 * false, which it should only do when the exit 
	 * program command has been sent. 
	 */
	public void sitInLoop() {
		
		String udpCommand = null;
		
		while (true) {
			udpCommand = getCommand();
			if (udpCommand == null) {
				continue;
			}
			boolean interpretCommandResponse = true;
			try {
				interpretCommandResponse = interpretCommand(udpCommand);
				if (interpretCommandResponse == false) {
					System.out.println("Received unknown or failed udp command: "+udpCommand);
				}
			}catch(Exception e) {
				interpretCommandResponse = false;
				System.err.println("Error interpreting UDP command '"+udpCommand+"': "+e.getMessage());
			}
			
			if(udpCommand.contains("exit")) {
				break;
			}
		}
	}

	
}
