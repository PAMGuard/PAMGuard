package PamView.help;

import java.net.URL;

import javax.help.BadIDException;
import javax.help.DefaultHelpBroker;
import javax.help.HelpSet;

public class PamHelpBroker extends DefaultHelpBroker {

	public PamHelpBroker() {
		super();
		// TODO Auto-generated constructor stub
	}

	public PamHelpBroker(HelpSet arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setCurrentID(String arg0) throws BadIDException {
		System.out.println("PamHelpBroker.SetCurrentID " + arg0);
		// TODO Auto-generated method stub
		super.setCurrentID(arg0);
	}

	@Override
	public void setCurrentURL(URL arg0) {
		// TODO Auto-generated method stub
		System.out.println("PamHelpBroker.setCurrentURL " + arg0);
		super.setCurrentURL(arg0);
	}

	@Override
	public void setCurrentView(String arg0) {
		System.out.println("PamHelpBroker.setCurrentView " + arg0);
		// TODO Auto-generated method stub
		super.setCurrentView(arg0);
	}

}
