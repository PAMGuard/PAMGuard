package PamView.paneloverlay.overlaymark;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.VerticalLabel;

public class SwingRelationshipsDialog extends PamDialog {

	private static SwingRelationshipsDialog singleInstance;
	
	private JCheckBox[][] linkBoxes;
	
	private JPanel linksPanel, mainPanel;
	
	private boolean ok;
	
	private JCheckBox immediateMenus;
	
	private SwingRelationshipsDialog(Window parentFrame) {
		super(parentFrame, "Display mark relationships", false);
		
		mainPanel = new JPanel(new BorderLayout());
		JTabbedPane tabPane = new JTabbedPane();
		mainPanel.add(tabPane, BorderLayout.CENTER);
		
		linksPanel = new JPanel(new BorderLayout());
//		linksPanel.setBorder(new TitledBorder("Marker links"));
		tabPane.add("Marker Links", linksPanel);
		
		JPanel optionsPanel = new JPanel();
		tabPane.add("Options", optionsPanel);
		optionsPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		c.gridx = c.gridy = 0;
		optionsPanel.add(immediateMenus = new JCheckBox("Show popup menus immediately"), c);
		immediateMenus.setToolTipText("Show pop up menus on marked displays immediatly when a mark is complete");
		
		
		setHelpPoint("displays.displaymarks.docs.displaymarks");
		setDialogComponent(mainPanel);
		
//		setResizable(true);
	}
	
	public static boolean showDialog(JFrame frame) {
		if (singleInstance == null || singleInstance.getOwner() != frame) {
			singleInstance = new SwingRelationshipsDialog(frame);
		}
		singleInstance.setParams();
		singleInstance.pack();
		singleInstance.setVisible(true);
		
		return singleInstance.ok;
	}
	
	private void setParams() {
		buildPanel();

		MarkRelationships links = MarkRelationships.getInstance();
		MarkRelationshipsData params = links.getMarkRelationshipsData();
		immediateMenus.setSelected(params.isImmediateMenus());
	}

	private void buildPanel() {
		linksPanel.removeAll();
		
		JPanel boxPanel = new JPanel();
//		boxPanel.setBackground(Color.GREEN);
		boxPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		
		MarkRelationships links = MarkRelationships.getInstance();
		ArrayList<OverlayMarker> markers = OverlayMarkProviders.singleInstance().getMarkProviders();
		ArrayList<OverlayMarkObserver> observers = OverlayMarkObservers.singleInstance().getMarkObservers();
		linkBoxes = new JCheckBox[markers.size()][observers.size()];
		int rowOffset = 2;
		int colOffset = 2;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.LAST_LINE_END;
		c.gridx = colOffset - 1;
		c.gridy = rowOffset - 1;
		JLabel lm;
		boxPanel.add(lm = new JLabel(" Mark source ", JLabel.RIGHT), c);
//		lm.setBackground(Color.CYAN);
		c.gridx = colOffset - 1;
		c.gridy = 0;
		JLabel lo;
		boxPanel.add(lo = new JLabel("Observer", JLabel.RIGHT), c);
//		lo.setBackground(Color.green);
		c = new PamGridBagContraints();
		for (int i = 0; i < markers.size(); i++) {
			OverlayMarker marker = markers.get(i);
			c.gridx = colOffset-1;
			c.gridy = rowOffset + i;
			boxPanel.add(new JLabel(marker.getMarkerName() + " ", JLabel.RIGHT), c);
			for (int j = 0; j < observers.size(); j++) {
				OverlayMarkObserver observer = observers.get(j);
				linkBoxes[i][j] = new JCheckBox();
				c.gridx = colOffset + j;
				boxPanel.add(linkBoxes[i][j], c);
				boolean en = marker.canMark(observer);
				if (en) {
					linkBoxes[i][j].setSelected(links.getRelationship(marker, observer));
				}
				else {
					linkBoxes[i][j].setEnabled(false);
					linkBoxes[i][j].setVisible(false);
				}
				
			}
		}
		for (int j = 0; j < observers.size(); j++) {
			OverlayMarkObserver observer = observers.get(j);
			c.gridx = colOffset+j;
			c.gridy = 0;
			c.gridheight = 2;
			c.fill = GridBagConstraints.VERTICAL;
			c.anchor = GridBagConstraints.BASELINE;
			boxPanel.add(new VerticalLabel(observer.getObserverName(), JLabel.LEFT), c);
		}
		linksPanel.add(boxPanel, BorderLayout.CENTER);
		
		
	}

	@Override
	public boolean getParams() {
		MarkRelationships links = MarkRelationships.getInstance();
		ArrayList<OverlayMarker> markers = OverlayMarkProviders.singleInstance().getMarkProviders();
		ArrayList<OverlayMarkObserver> observers = OverlayMarkObservers.singleInstance().getMarkObservers();
		for (int i = 0; i < markers.size(); i++) {
			OverlayMarker marker = markers.get(i);
			for (int j = 0; j < observers.size(); j++) {
				OverlayMarkObserver observer = observers.get(j);
				links.setRelationship(marker, observer, linkBoxes[i][j].isSelected());				
			}
		}
		ok = true;

		MarkRelationshipsData params = links.getMarkRelationshipsData();
		params.setImmediateMenus(immediateMenus.isSelected());
		
		return ok;
	}

	@Override
	public void cancelButtonPressed() {
		ok = false;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
