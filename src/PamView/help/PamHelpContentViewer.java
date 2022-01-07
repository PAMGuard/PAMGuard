package PamView.help;

import java.net.URL;

import javax.help.BadIDException;
import javax.help.HelpSet;
import javax.help.InvalidHelpSetContextException;
import javax.help.JHelpContentViewer;
import javax.help.Map.ID;
import javax.help.plaf.HelpContentViewerUI;

public class PamHelpContentViewer extends JHelpContentViewer {

	private static final long serialVersionUID = 1L;
	
	public PamHelpContentViewer(HelpSet arg0) {
		super(arg0);
	}

	@Override
	public void setCurrentID(ID arg0) throws InvalidHelpSetContextException {
//		System.out.println("Current ID " + arg0.toString());
		super.setCurrentID(arg0);
	}

	@Override
	public void setCurrentID(String arg0) throws BadIDException {
		System.out.println("Current ID String " + arg0.toString());
		// TODO Auto-generated method stub
		super.setCurrentID(arg0);
	}

	@Override
	public void setUI(HelpContentViewerUI arg0) {
		System.out.println("setUI " + arg0.toString());
		super.setUI(arg0);
	}

	@Override
	public void reload() {
		System.out.println("Reload ");
		super.reload();
	}

	@Override
	public void updateUI() {
		System.out.println("updateUI ");
		super.updateUI();
	}

	@Override
	public void setCurrentURL(URL arg0) {
		System.out.println("Current URL " + arg0.toString());
		// TODO Auto-generated method stub
		super.setCurrentURL(arg0);
	}
	

}
