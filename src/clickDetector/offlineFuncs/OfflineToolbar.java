package clickDetector.offlineFuncs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

import soundPlayback.PlaybackControl;
import clickDetector.BTDisplayParameters;
import clickDetector.ClickBTDisplay;
import clickDetector.ClickControl;
import clickDetector.ClickDisplay;
import clickDetector.ClickClassifiers.ClickIdentifier;
import PamView.PamToolBar;
import PamView.dialog.PamCheckBox;
import PamView.dialog.PamLabel;
import PamView.dialog.PamRadioButton;
import PamView.panel.PamPanel;

public class OfflineToolbar {

	private PamToolBar toolBar;
	
	private ClickControl clickControl;
	
	private JButton playClicks, reAnalyseClicks;
	
	private JCheckBox[] speciesButtons;
	
	private JCheckBox showNonSpecies;
	
	private JCheckBox showEchoes;
		
	private JCheckBox clicksInAnEvent;
	
	private ClickBTDisplay currentBTDisplay;

	private boolean isViewer;

	private JComboBox<String> andOrSelection;

	private boolean firstSetup;
	
	public JToolBar getToolBar() {
		return toolBar;
	}

	public void setToolBar(PamToolBar toolBar) {
		this.toolBar = toolBar;
	}


	public OfflineToolbar(ClickControl clickControl) {
		super();
		this.clickControl = clickControl;
		isViewer = clickControl.isViewerMode();
		
		toolBar = new PamToolBar("Offline Click Analysis");
		
		if (isViewer) {
			playClicks = new JButton(new ImageIcon(ClassLoader
					.getSystemResource("Resources/clickPlayStart.png")));
			playClicks.addActionListener(new PlayClicks());
			playClicks.setToolTipText("Play clicks (pack empty space with 0's)");
			PlaybackControl.registerPlayButton(playClicks);

			reAnalyseClicks = new JButton(new ImageIcon(ClassLoader
					.getSystemResource("Resources/reanalyseClicks.png")));
			reAnalyseClicks.addActionListener(new ReanalyseClicks());
			reAnalyseClicks.setToolTipText("Re-analyse clicks");
		}
		
		createStandardButtons();
		createSpeciesButtons();
		
	}
	
	private void createStandardButtons() {
		if (playClicks != null) {
			toolBar.add(playClicks);
		}
		if (reAnalyseClicks != null) {
			toolBar.add(reAnalyseClicks);
		}
		
		enableButtons();
	}
	
	public void setupToolBar() {
		toolBar.removeAll();
		createStandardButtons();
		createSpeciesButtons();
		
		// if we have parameters, set them in the display
		if (currentBTDisplay != null) {
			checkButtons(currentBTDisplay.getBtDisplayParameters());
		}
	}

//	public void addButtons(JButton[] buttons) {
//		toolBar.removeAll();
//		for (int i = 0; i < buttons.length; i++) {
//			if (buttons[i] != null) {
//				toolBar.add(buttons[i]);
//			}
//			else {
//				toolBar.addSeparator();
//			}
//		}
//		createStandardButtons();
//	}
	
	private void createSpeciesButtons() {
		ClickIdentifier clickId = clickControl.getClickIdentifier();
		String space = "  ";
		ShowClicks showClicks = new ShowClicks();
		toolBar.add(new PamLabel("  Show: "));
		showEchoes = new SpeciesCheckBox("Echoes" + space);
		toolBar.add(showEchoes);
		showEchoes.addActionListener(showClicks);
		
		JPanel speciesBar1 = new JPanel(new BorderLayout());
//		speciesBar1.setBackground(Color.RED);
//		PamPanel speciesBar = new PamPanel(new FlowLayout());
		JPanel speciesBar = new PamPanel();
//		speciesBar.setBackground(Color.BLUE);
		
//		BoxLayout bl;
//		speciesBar.setLayout(bl = new BoxLayout(speciesBar, BoxLayout.X_AXIS));
		speciesBar.setLayout(new GridBagLayout());
		speciesBar.setBorder(new EmptyBorder(0,0,0,0));
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = c.gridy = 0;
		c.ipady = 0;
		c.ipadx = 5;
		c.fill = GridBagConstraints.NONE;
		c.insets = new Insets(0, 0, 0, 0);
		
		speciesBar1.add(BorderLayout.WEST, speciesBar);
		toolBar.add(speciesBar1);
		LayoutManager toolLayout = toolBar.getLayout();
//		speciesBar.add(new PamLabel("   "));
		showNonSpecies = new SpeciesCheckBox("Unclassified clicks" + space);
		speciesBar.add(showNonSpecies, c);
		showNonSpecies.addActionListener(showClicks);
		if (clickId == null) {
			return;
		}
		String[] speciesList = clickId.getSpeciesList();
		if (speciesList == null || speciesList.length == 0) {
			return;
		}
		speciesButtons = new JCheckBox[speciesList.length];
		for (int i = 0; i < speciesList.length; i++) {
			speciesButtons[i] = new SpeciesCheckBox(speciesList[i] + space);
			c.gridx++;
			speciesBar.add(speciesButtons[i]);
			speciesButtons[i].addActionListener(showClicks);
		}
		c.gridx++;
		speciesBar.add(andOrSelection = new JComboBox<String>());
		c.gridx++;
		speciesBar.add(new PamLabel(space));
		c.gridx++;
		speciesBar.add(clicksInAnEvent = new SpeciesCheckBox("Event clicks only"));
		andOrSelection.addItem("AND");
		andOrSelection.addItem("OR");
		andOrSelection.addActionListener(showClicks);
		clicksInAnEvent.addActionListener(showClicks);
		
	}
	
	private class SpeciesCheckBox extends PamCheckBox {

		public SpeciesCheckBox(String text) {
			super(text);
		}

		/* (non-Javadoc)
		 * @see javax.swing.JComponent#getPreferredSize()
		 */
		@Override
		public Dimension getPreferredSize() {
			return super.getMinimumSize();
		}
		
	}

	private void enableButtons() {
//		boolean storeOpen = (clickControl.getClicksOffline() != null && clickControl.getClicksOffline().isOpen());
		if (reAnalyseClicks != null) {
			reAnalyseClicks.setEnabled(true);
		}
	}

	class ReanalyseClicks implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			clickControl.getClicksOffline().reAnalyseClicks();
		}
	}

	class PlayClicks implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			clickControl.playClicks();
		}
	}
	class ShowClicks implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			showClicksChanged();
		}
	}

	public void displayActivated(ClickDisplay clickDisplay) {
		if (clickDisplay.getClass() == ClickBTDisplay.class) {
			ClickBTDisplay btDisplay = (ClickBTDisplay) clickDisplay;
			currentBTDisplay = btDisplay;
			if (showEchoes==null||showNonSpecies==null){
//				System.out.println("stdButtonsNull in clicks offline toolbar");//createSpeciesButtons();
				return;
			}
			checkButtons(btDisplay.getBtDisplayParameters());
		}
	}

	public void showClicksChanged() {
		if (currentBTDisplay == null || firstSetup == false) {
			return;
		}
		try {
			BTDisplayParameters btDisplayParameters = currentBTDisplay.getBtDisplayParameters();
			btDisplayParameters.setShowSpecies(0, showNonSpecies.isSelected());
			btDisplayParameters.showEchoes = showEchoes.isSelected();
			if (clicksInAnEvent != null) {
				btDisplayParameters.showEventsOnly = clicksInAnEvent.isSelected();
			}
			if (andOrSelection != null) {
				btDisplayParameters.showANDEvents = (andOrSelection.getSelectedIndex() == 0);
			}
			if (speciesButtons != null) {
				int n = speciesButtons.length;
				for (int i = 0; i < n; i++) {
					btDisplayParameters.setShowSpecies(i+1, speciesButtons[i].isSelected());
				}
			}
			currentBTDisplay.repaintTotal();
		}
		catch (NullPointerException e) {

		}
	}

	private void checkButtons(BTDisplayParameters btDisplayParameters) {
		showEchoes.setSelected(btDisplayParameters.showEchoes);
		showNonSpecies.setSelected(btDisplayParameters.getShowSpecies(0));
		if (clicksInAnEvent != null) {
			clicksInAnEvent.setSelected(btDisplayParameters.showEventsOnly);
		}
		if (speciesButtons != null) {
			int n = speciesButtons.length;
			for (int i = 0; i < n; i++) {
				speciesButtons[i].setSelected(btDisplayParameters.getShowSpecies(i+1));
			}
		}
		 // setting combo box fires actionlistener, so we have to make sure that all checkboxes have been properly set first
		// or else they will get cleared later
		if (andOrSelection != null) {
			andOrSelection.setSelectedIndex(btDisplayParameters.showANDEvents ? 0: 1);
		}
		firstSetup = true;
	}
	
}
