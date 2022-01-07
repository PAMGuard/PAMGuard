package alfa.swinggui;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;

import PamView.PamColors.PamColor;
import PamView.panel.PamPanel;

public class ALFALayout1 implements ALFALayout {
	
	private PamPanel alfaPanel;
	
	private PamPanel mainCentral, mainNorth, mainSplit, mainWest;
	private PamPanel splitWestNorth, splitWestSouth, splitWest, splitEast, helpPane;
		
	public ALFALayout1() {
			
		alfaPanel = new PamPanel(new BorderLayout());
		mainCentral = new PamPanel(new BorderLayout());
		mainWest = new PamPanel(new BorderLayout());
		mainNorth = new PamPanel();
		mainSplit = new PamPanel();
		splitWest = new PamPanel();
		splitEast = new PamPanel(new BorderLayout());
		splitWestNorth = new PamPanel();
		splitWestSouth = new PamPanel();
		helpPane = new PamPanel(new BorderLayout());

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Messaging", mainCentral);
		tabbedPane.addTab("HELP", helpPane);
		
		alfaPanel.setLayout(new BorderLayout());
		alfaPanel.add(BorderLayout.CENTER, tabbedPane);
		alfaPanel.add(BorderLayout.WEST, mainWest);
		
		mainCentral.setLayout(new BorderLayout());
		mainCentral.add(BorderLayout.NORTH, mainNorth);
		mainCentral.add(BorderLayout.CENTER, mainSplit);
//		mainSplit.setLayout(new BoxLayout(mainSplit, BoxLayout.X_AXIS));
		mainSplit.setLayout(new GridLayout(1, 1));
		mainSplit.add(splitWest);
//		mainSplit.add(splitEast);
//		splitWest.setLayout(new BoxLayout(splitWest, BoxLayout.Y_AXIS));
		splitWest.setLayout(new GridLayout(2, 1));
		splitWest.add(splitWestNorth);
		splitWest.add(splitWestSouth);
		
//		splitEast.setBorder(new TitledBorder("Map"));
		splitWestNorth.setBorder(new TitledBorder("Sperm Whales"));
		splitWestSouth.setBorder(new TitledBorder("Comms"));
	}

	@Override
	public JComponent getComponent() {
		return alfaPanel;
	}

	@Override
	public void setOptionsComponent(JComponent optionsComponent) {
		mainWest.add(optionsComponent, BorderLayout.NORTH);
	}

	@Override
	public void setWestStatusComponent(JComponent statusComponent) {
//		mainWest.removeAll();
		mainWest.add(statusComponent, BorderLayout.CENTER);
	}

	@Override
	public void setMapComponent(JComponent mapComponent) {
		splitEast.removeAll();
		splitEast.add(mapComponent);
	}

	@Override
	public void setSpermSummaryComponents(JComponent spermSummary) {
		splitWestNorth.setLayout(new BorderLayout());
		splitWestNorth.setBorder(null);
		splitWestNorth.removeAll();
		splitWestNorth.add(spermSummary, BorderLayout.CENTER);
	}

	@Override
	public void setCommsComponent(JComponent commsComponent) {
		splitWestSouth.setLayout(new BorderLayout());
		splitWestSouth.add(BorderLayout.CENTER, commsComponent);
	}

	@Override
	public void setNorthStatusComponents(JComponent northComponent) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void setHelpcomponent(JComponent helpComponent) {
		helpPane.setLayout(new BorderLayout());
		helpPane.add(BorderLayout.CENTER, helpComponent);
	}

}
