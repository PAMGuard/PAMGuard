package tethys.dbxml;

import dbxml.JerseyClient;
import tethys.output.TethysExportParams;

public class DMXMLQueryTest {

	public static void main(String[] args) {
		new DMXMLQueryTest().runTest();
	}

	private void runTest() {
		TethysExportParams params = new TethysExportParams();
		
		JerseyClient jerseyClient = new JerseyClient(params.getFullServerName());
		
		// web browse to http://localhost:9779/Client
		
		
	}

}
