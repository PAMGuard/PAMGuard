package dataPlots;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.hidingpanel.HidingGridBagContraints;
import PamView.panel.PamPanel;
import dataPlots.data.TDDataInfo;
import dataPlots.data.TDDataProvider;
import dataPlots.data.TDDataProviderRegister;
import dataPlots.data.TDInfoMonitor;
import dataPlots.layout.TDGraph;
import pamScrollSystem.AbstractPamScrollerAWT;

public class HidingControlPanel extends PamPanel {

	private TDControl tdControl;
	private JButton addGraph;
	private JButton removeGraph;
	private JCheckBox vertical;
	private JPopupMenu popMenu;
	private ArrayList<JButton> graphButtons = new ArrayList<>();
	private JPanel graphPanel;
	private JPanel graphOutPanel;

	public HidingControlPanel(TDControl tdControl)  {
		super();
		this.tdControl = tdControl;

		this.setLayout(new BorderLayout());
		this.setBorder(new TitledBorder("Configure graphs and graph content"));

		JPanel controlPanel = new JPanel();
		this.add(controlPanel, BorderLayout.WEST);

		GridBagConstraints c = new HidingGridBagContraints();
		controlPanel.setLayout(new GridBagLayout());
		c.insets = new Insets(0,2,0,2);
//		c.anchor=GridBagConstraints.NORTH;

		PamDialog.addComponent(controlPanel, addGraph = new JButton("Add graph"), c);
		c.gridx++;
		//		addGraph.setMargin(HidingDialogPanel.getDefaultButtonInsets());
		PamDialog.addComponent(controlPanel, removeGraph = new JButton("Remove graph"), c);
		c.gridx++;
		//		removeGraph.setMargin(HidingDialogPanel.getDefaultButtonInsets());
		PamDialog.addComponent(controlPanel, vertical = new JCheckBox("Vertical time axis"), c);
		c.gridx++;
		//		vertical.setMargin(HidingDialogPanel.getDefaultButtonInsets());
		addGraph.addActionListener(new AddGraph());
		removeGraph.addActionListener(new RemoveGraph());
		vertical.addActionListener(new Vertical());

		graphOutPanel = new JPanel(new BorderLayout());
		graphPanel = new JPanel();
		graphPanel.setLayout(new GridBagLayout());
		graphOutPanel.add(BorderLayout.WEST, graphPanel);
		this.add(BorderLayout.CENTER, graphOutPanel);
		c.gridx = 0;
		c.gridy = 0;
		graphPanel.add(new JLabel(" Configure graphs: "),c);
		
		vertical.setSelected(tdControl.getTdParameters().orientation == AbstractPamScrollerAWT.VERTICAL);
		checkGraphButtons();
		
	}
	private void setGraphTips() {
		ArrayList<TDGraph> graphs = tdControl.getGraphs();
		int n = Math.min(graphs.size(), graphButtons.size());
		for (int i = 0; i < n; i++) {
			String tip = String.format("Configure graph %d (%s)", i, graphs.get(i).getGraphLabel());
			graphButtons.get(i).setToolTipText(tip);
		}
	}

	private void checkGraphButtons() {
		ArrayList<TDGraph> graphs = tdControl.getGraphs();
		boolean changes = false;
		GridBagConstraints c = new HidingGridBagContraints();
		c.insets = new Insets(0,2,0,2);
		while (graphButtons.size() < graphs.size()) {
			JButton newButton = new JButton("Graph " + graphButtons.size());
			newButton.addActionListener(new GraphButton(graphButtons.size()));
			graphButtons.add(newButton);
			c.gridx = graphButtons.size()+1;
			graphPanel.add(newButton, c);
			changes = true;
		}
		while (graphButtons.size() > graphs.size()) {
			JButton remButton = graphButtons.get(graphButtons.size()-1);
			graphButtons.remove(remButton);
			graphPanel.remove(remButton);
			changes = true;
		}
		if (changes) {
			//TODO-checks repacks everything OK. 
			this.invalidate();
			graphPanel.invalidate();
			graphOutPanel.invalidate();
			setGraphTips();
			//			mainPanel.invalidate();
			//			mainPanel.repaint();
		}
	}

	private class AddGraph implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			addGraph(arg0);
			checkGraphButtons();
		}
	}

	private class RemoveGraph implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			removeGraph(arg0);
		}
	}

	private class Vertical implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			tdControl.getTdParameters().orientation = (vertical.isSelected() ? AbstractPamScrollerAWT.VERTICAL : AbstractPamScrollerAWT.HORIZONTAL);
			tdControl.layoutGraphs();
		}
	}

	public void addGraph(ActionEvent arg0) {
		tdControl.addGraph();
	}

	public void removeGraph(ActionEvent arg0) {
		popMenu = new JPopupMenu();
		int graphInd = 0;
		for (TDGraph aGraph:tdControl.getGraphs()) {
			JMenuItem menuItem = popMenu.add(String.format("%d - %s", graphInd, aGraph.getGraphLabel()));
			menuItem.addActionListener(new RemoveAGraph(graphInd));
			graphInd++;
		}
		popMenu.show(removeGraph, 0, 0);
	}

	public class RemoveAGraph implements ActionListener {

		int iGraph;
		public RemoveAGraph(int iGraph) {
			super();
			this.iGraph = iGraph;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			tdControl.removeGraph(iGraph);
			checkGraphButtons();
		}

	}


//	@Override
//	public JComponent getComponent() {
//		return mainPanel;
//	}
//
//	@Override
//	public boolean canHide() {
//		return (popMenu == null || !popMenu.isVisible());
//	}
//
//	@Override
//	public void showComponent(boolean visible) {
//		vertical.setSelected(tdControl.getTdParameters().orientation == PamScroller.VERTICAL);
//		checkGraphButtons();
//	}

	@Override
	public String getName() {
		return "Plot control";
	}

	private class GraphButton implements ActionListener {

		int iGraph;

		public GraphButton(int iGraph) {
			super();
			this.iGraph = iGraph;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			configureGraphMenu(iGraph);
			//			System.out.println("got menu");
		}

	}

	public void configureGraphMenu(int iGraph) {
		ArrayList<TDGraph> graphs = tdControl.getGraphs();
		TDGraph aGraph = graphs.get(iGraph);

		popMenu = new JPopupMenu();
		JMenuItem menuItem = new JMenu("Add data");
		popMenu.add(menuItem);
		TDDataProviderRegister.getInstance().addMenuItems(menuItem, new AddData(aGraph));

		menuItem = new JMenu("Remove data");
		popMenu.add(menuItem);
		if (aGraph.getDataList().size() == 0) {
			menuItem.setEnabled(false);
		}
		for (TDDataInfo aData:aGraph.getDataList()) {
			JMenuItem removeItem = new JMenuItem(aData.getDataName());
			removeItem.addActionListener(new RemoveDataMenu(aGraph, aData));
			menuItem.add(removeItem);
		}


		Component jButton = graphButtons.get(iGraph);
		Point p = jButton.getMousePosition();
		if (p == null) {
			p = new Point(0,0);
		}
		//			p = MouseInfo.getPointerInfo().getLocation();
		popMenu.show(jButton, p.x, p.y);
	}

	private class AddData implements TDInfoMonitor {

		private TDGraph aGraph;
		public AddData(TDGraph aGraph) {
			super();
			this.aGraph = aGraph;
		}
		@Override
		public void selectProvider(TDDataProvider dataProvider) {
			aGraph.addDataItem(dataProvider);
			setGraphTips();
		}

	}

	private class RemoveDataMenu implements ActionListener {

		private TDGraph aGraph;
		private TDDataInfo dataInfo;

		public RemoveDataMenu(TDGraph aGraph, TDDataInfo dataInfo) {
			super();
			this.aGraph = aGraph;
			this.dataInfo = dataInfo;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			aGraph.removeDataItem(dataInfo);
			setGraphTips();
		}

	}

}
