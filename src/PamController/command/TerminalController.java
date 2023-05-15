package PamController.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import PamController.PamController;

public class TerminalController extends CommandManager {

	private BufferedReader reader;

	private static String unitName = "Terminal Controller";
	
	public TerminalController(PamController pamController) {
		super(pamController, unitName);
	}

	@Override
	public boolean sendData(ExtCommand extcommand, String dataString) {
		System.out.println(dataString);
		return true;
	}

	@Override
	public void pamClose() {
		super.pamClose();
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void getTerminalCommands() {
		Thread t = new Thread(new TerminalTread());
		t.start();
	}
	
	private class TerminalTread implements Runnable {

		@Override
		public void run() {
			readCommands();
		}
		
	}
	
	private void readCommands() {

        reader = new BufferedReader(
        		new InputStreamReader(System.in));
        try {
        	while (true) {
        		String command = reader.readLine();
        		if (command != null && command.length() > 0) {
        			interpretCommand(command);
        		}
//        		System.out.println("you typed: " + inLine);
//        		if (inLine.contains("exit")) {
//        			break;
//        		}
        	}
        } catch (IOException e) {
        	e.printStackTrace();
        } 
        System.out.println("Exiting PAMGuard, leave control thread");
	}
}
