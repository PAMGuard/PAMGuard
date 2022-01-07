package qa.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamController.PamController;
import PamView.dialog.PamButton;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.PamLabel;
import PamView.dialog.smart.ImmediateTextField;
import PamView.dialog.warn.WarnOnce;
import PamView.hidingpanel.HidingPanel;
import PamView.panel.PamPanel;
import qa.ClusterParameters;
import qa.QAControl;
import qa.QANotifyable;
import qa.QAParameters;
import qa.QATestDataBlock;
import qa.QATestDataUnit;
import qa.generator.QAGeneratorProcess;
import qa.generator.clusters.QACluster;
import qa.generator.location.LocationManager;
import qa.generator.testset.QATestSet;
import qa.operations.QAOpsDataUnit;

/**
 * Contains the main list of clusters as well as three smaller panels for test range, quick tests and random tests. 
 * @author dg50
 *
 */
public class TestSelectPanel implements QANotifyable {

	private ClusterSelectPanel clusterSelectPanel;

	private JTextField rangesFields[];

	private JButton runImmediateButton;
	
	private JCheckBox immediateReport;

	private JPanel mainPanel;

	private QAControl qaControl;

	private JTextField nSequences;

	private JComboBox<String> locations;

	private ImmediateTextField<Integer> ltInterval;

	private ImmediateTextField<Double> rangeField;
	
	private enum OPSSTATE {VIEWER,NOSELECTION,IDLE,STILLRUNNING,READY,NOOPSSTATE}; 

	public TestSelectPanel(QAControl qaControl, String[] colNames) {
		super();
		this.qaControl = qaControl;

		clusterSelectPanel = new ClusterSelectPanel(qaControl, colNames);

		clusterSelectPanel.addSelectionListener(new ClusterSelectionListener() {
			@Override
			public void clusterSelected(QACluster qaCluster, int column, boolean selected) {
				newSelection(qaCluster, column, selected);
			}
		});

		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		HidingPanel hp = new HidingPanel(mainPanel, clusterSelectPanel.getComponent(), HidingPanel.VERTICAL, false, "Species Clusters", "QAClusterPanel");
		hp.setBorder(new TitledBorder("Species Clusters"));
		mainPanel.add(hp);


		runImmediateButton = new PamButton("Run Quick Tests");
		runImmediateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				runImmediate();
			}
		});
		immediateReport = new JCheckBox("Auto generate report");
		immediateReport.setToolTipText("Generate report immediately quick test sequence ends");
		immediateReport.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				immediateButtonAction();
			}
		});
		/*
		 * Panel for range limit control. 
		 */
		JPanel rPanel = new PamPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
//		rPanel.add(new PamLabel(" Range factor "), c);
//		c.gridx++;
		rangeField = new ImmediateTextField<Double>(Double.class, 4, "Range factor", "", true) {

			@Override
			public void valueChange(Double newValue, boolean valueError, boolean lostFocus) {
				if (valueError && !lostFocus) {
					return;
				}
				if (valueError && lostFocus) {
					WarnOnce.showWarning(qaControl.getGuiFrame(), ltInterval.getPrefix(), "Invalid value", WarnOnce.OK_OPTION);
					return;
				}
				if (lostFocus) {
					qaControl.setNewRangeFactor(newValue);
				}
				
			}
		};
		rangeField.setToolTipText("Test ranges will vary from the nominal range / this factor to the nominal range * this factor for each cluster", true);
		rangeField.addToParent(rPanel, c);

		hp = new HidingPanel(mainPanel, rPanel, HidingPanel.VERTICAL, false, "Test range limits", "Test range limits");
		hp.setBorder(new TitledBorder("Test Range Limits"));
		mainPanel.add(hp);
		
		/*
		 * Panel for quick tests. 
		 */
		JPanel imPanel = new PamPanel(new GridBagLayout());
		String name = QAControl.quickTestName + " Tests";
		c = new PamGridBagContraints();
		imPanel.add(new PamLabel(" Number of Sequences "), c);
		c.gridx++;
		imPanel.add(nSequences = new JTextField(4), c);
		/**
		 * For now, remove options to select varying location types. 
		 * Will always use stepped distances for quick tests and random distances for 
		 * random tests. 
		 */
//		c.gridx = 0;
//		c.gridy ++;
//		imPanel.add(new PamLabel(" Locations "), c);
//		c.gridx++;
//		c.gridwidth = 2;
		locations = new JComboBox<String>();
//		imPanel.add(locations, c);
		String[] names = LocationManager.locatorNames;
		for (int i = 0; i < names.length; i++) {
			locations.addItem(names[i]);
		}
		c.gridx = 0;
		c.gridy ++;
		c.gridwidth = 3;
		imPanel.add(runImmediateButton, c);
		nSequences.setToolTipText("Enter the number of test sequences to generate for each sound type");
		c.gridy++;
		imPanel.add(immediateReport, c);
		hp = new HidingPanel(mainPanel, imPanel, HidingPanel.VERTICAL, false, name, name);
		hp.setBorder(new TitledBorder(name));
		mainPanel.add(hp);

		JPanel ltPanel = new PamPanel(new GridBagLayout());
		name = QAControl.randomTestName + " Tests";
//		ltPanel.setBorder(new TitledBorder(name));
		c = new PamGridBagContraints();
		ltInterval = new ImmediateTextField<Integer>(Integer.class, 4, "Random Drill Interval", "mins", true) {
			@Override
			public void valueChange(Integer newValue, boolean valueError, boolean lostFocus) {
				randomIntervalChange(newValue, valueError, lostFocus);
			}
		};
		ltInterval.addToParent(ltPanel, c);
		ltInterval.setToolTipText("Interval between random drills (will be randomised about this value)", true);
		hp = new HidingPanel(mainPanel, ltPanel, HidingPanel.VERTICAL, false, name, name);
		hp.setBorder(new TitledBorder(name));
		mainPanel.add(hp);

		setClusterList(qaControl.getAvailableClusters());

		setParams();
		getParams();

		qaControl.addNotifyable(this);
	}

	public Component getComponent() {
		return mainPanel;
	}

	public void setClusterList(ArrayList<QACluster> clusterList) {
		clusterSelectPanel.setClusterList(clusterList);
		/*
		 * Now add in a column of mitigation distances
		 */
		JPanel clusterPanel = clusterSelectPanel.getClusterPanel();
		rangesFields = new JTextField[clusterList.size()];
		GridBagConstraints c = new PamGridBagContraints();
		c.gridx = clusterSelectPanel.columStartOffset + clusterSelectPanel.getnColumns();
		c.gridy = clusterSelectPanel.clusterRowOffset-1;
		c.anchor = GridBagConstraints.SOUTH;
		clusterPanel.add(new PamLabel("<html><Center>Nominal<p>Range</center></html>", JLabel.CENTER), c);
		c.anchor = GridBagConstraints.WEST;
		for (int i = 0; i < clusterList.size(); i++) {
			rangesFields[i] = new JTextField(5);
			c.gridy++;
			c.gridx = clusterSelectPanel.columStartOffset + clusterSelectPanel.getnColumns();
			clusterPanel.add(rangesFields[i], c);
			c.gridx++;
			clusterPanel.add(new PamLabel(" m ", JLabel.LEFT), c);
		}

	}

	public void setParams() {
		QAParameters params = qaControl.getQaParameters();
		ArrayList<QACluster> clusterList = clusterSelectPanel.getCurrentClusterList();
		for (int i = 0; i < clusterList.size(); i++) {
			QACluster cluster = clusterList.get(i);
			ClusterParameters clusterParams = params.getClusterParameters(cluster);
			clusterSelectPanel.setSelection(cluster, 0, clusterParams.runImmediate);
			clusterSelectPanel.setSelection(cluster, 1, clusterParams.runRandom);
			rangesFields[i].setText(String.format("%3.0f", clusterParams.monitorRange));
		}

		nSequences.setText(String.format("%d", params.getnQuickTestSequences()));
		locations.setSelectedItem(params.getQuickTestLocationGeneratorName());
		ltInterval.setValue(params.getRandomTestIntervalSeconds()/60);
		immediateReport.setSelected(params.immediateQuickReport);
		rangeField.setValue(params.getRangeFactor());
	}

	public boolean getParams() {
		QAParameters params = qaControl.getQaParameters();
		ArrayList<QACluster> clusterList = clusterSelectPanel.getCurrentClusterList();
		int nImmediate = 0;
		for (int i = 0; i < clusterList.size(); i++) {
			QACluster cluster = clusterList.get(i);
			ClusterParameters clusterParams = params.getClusterParameters(cluster).clone();
			clusterParams.runImmediate = clusterSelectPanel.getSelection(cluster, 0);
			if (clusterParams.runImmediate) {
				nImmediate++;
			}
			clusterParams.runRandom = clusterSelectPanel.getSelection(cluster, 1);
			try {
				clusterParams.monitorRange = Double.valueOf(rangesFields[i].getText());
			}
			catch (NumberFormatException e) {
				runImmediateButton.setEnabled(false);
				return PamDialog.showWarning(PamController.getMainFrame(), "Invalid parameter", "Invalid range data for cluster: " + cluster.getName());
			}
			params.setClusterParameters(cluster, clusterParams);
		}		
		try {
			int nR = Integer.valueOf(nSequences.getText());
			params.setnQuickTestSequences(nR);
		}
		catch (NumberFormatException e) {
			return PamDialog.showWarning(PamController.getMainFrame(), "Invalid parameter", "Invalid number of test sequences");
		}
		params.setQuickTestLocationGeneratorName((String) locations.getSelectedItem());

		runImmediateButton.setEnabled(nImmediate > 0 && qaControl.isViewer() == false);

		return true;
	}

	private String getImmediateButtonToolTip(OPSSTATE state) {
		switch (state) {
		case IDLE:
			return "Only available after PAMGuard is running";
		case NOSELECTION:
			return "No quick tests are selected";
		case READY:
			return "Run quick performance tests";
		case STILLRUNNING:
			return "Quick tests are running. Will enable once all tests complete";
		case VIEWER:
			return "Not available in PAMGuard viewer mode";
		case NOOPSSTATE:
			return "Not available until the operations state has been set";
		default:
			return null;		
		}
	}
	
	/**
	 * Automatically enable or disable the immediate button.
	 */
	public void enableImmediateButton() {
		OPSSTATE state = getOpsState();
		runImmediateButton.setEnabled(isEnableImmediateButton(state));
		runImmediateButton.setToolTipText(getImmediateButtonToolTip(state));
	}

	/**
	 * Work out if we should enable or disable the immediate button
	 * @param state current state
	 * @return true if should be enabled.
	 */
	public boolean isEnableImmediateButton(OPSSTATE state) {
		return state == OPSSTATE.READY;
	}

	/**
	 * Work out if we should enable or disable the immediate button
	 * @return true if shold be enabled.
	 */
	public OPSSTATE getOpsState() {
		if (qaControl.isViewer()) {
			return OPSSTATE.VIEWER;
		}
		// check PAMGuard is running (probably makes the above test unnecessary)
		if (PamController.getInstance().getPamStatus() != PamController.PAM_RUNNING) {
			return OPSSTATE.IDLE;
		}
		/*
		 * first check that some immediate tests are enabled. 
		 */
		QAParameters params = qaControl.getQaParameters();
		ArrayList<QACluster> clusterList = clusterSelectPanel.getCurrentClusterList();
		int nImmediate = 0;
		for (int i = 0; i < clusterList.size(); i++) {
			QACluster cluster = clusterList.get(i);
			ClusterParameters clusterParams = params.getClusterParameters(cluster);
			if (clusterParams.runImmediate) {
				nImmediate++;
			}
		}
		if (nImmediate == 0) {
			return OPSSTATE.NOSELECTION;
		}
		/**
		 * Now check to see if there are immediate tests still running. 
		 */
		QAGeneratorProcess genProc = qaControl.getQaGeneratorProcess();
		QATestDataBlock testDataBlock = genProc.getTestsDataBlock();
		synchronized (testDataBlock.getSynchLock()) {
			ListIterator<QATestDataUnit> it = testDataBlock.getListIterator(0);
			while (it.hasNext()) {
				QATestDataUnit dataUnit = it.next();
				if (dataUnit.getTestType().equals(QAControl.quickTestName)) {
					String status = dataUnit.getQaTestSet().getStatus();
					if (status.equals(QATestSet.STATUS_ACTIVE)) {
						return OPSSTATE.STILLRUNNING;
					}
				}
			}
		}
		if (qaControl.getQaOperationsStatus().getCurrentStatus() == null) {
			return OPSSTATE.NOOPSSTATE;
		}
		return OPSSTATE.READY;
	}

	protected void runImmediate() {
		boolean okParams = getParams();
		if (okParams == false) {
			return;
		}
		/**
		 * Check operations status has been checked with a WarnOnce
		 */
		String state = "Unknown";
		QAOpsDataUnit currentState = qaControl.getQaOperationsStatus().getCurrentStatus();
		if (currentState != null) {
			state = currentState.getOpsStatusName();
		}
		String message = "Check operations status before starting tests: current = " + state;
		int ans = WarnOnce.showWarning(qaControl.getGuiFrame(), "Quick Tests Operations", message, WarnOnce.OK_CANCEL_OPTION);
		if (ans == WarnOnce.CANCEL_OPTION) {
			return;
		}
		qaControl.startQuickTests();		
	}

	protected void newSelection(QACluster qaCluster, int column, boolean selected) {
		boolean okParams = getParams();
		if (okParams) {
			qaControl.tellNotifyables(QANotifyable.TEST_SELECT_CHANGED);
		}
	}

	protected void randomIntervalChange(Integer newValue, boolean valueError, boolean lostFocus) {
		if (valueError && !lostFocus) {
			return;
		}
		if (valueError && lostFocus) {
			WarnOnce.showWarning(qaControl.getGuiFrame(), ltInterval.getPrefix(), "Invalid value", WarnOnce.OK_OPTION);
			return;
		}
		if (lostFocus) {
			qaControl.getQaParameters().setRandomTestIntervalSeconds(newValue*60);
		}

	}

	protected void immediateButtonAction() {
		qaControl.getQaParameters().immediateQuickReport = immediateReport.isSelected();
	}

	@Override
	public void qaNotify(int noteCode, Object noteObject) {
		enableImmediateButton();
	}

}
