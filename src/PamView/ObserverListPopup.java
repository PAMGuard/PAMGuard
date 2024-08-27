package PamView;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamObserver;

public class ObserverListPopup extends JDialog implements ActionListener {

	private static final String cpuWarning = "Values are CPU used by this process thread and will read zero if multi-threading is in operation";

	static ObserverListPopup singleInstance;
	
	PamDataBlock dataBlock;
	
	JButton closeButton;
	
	JPanel observerPanel, titlePanel;
	
	GridBagLayout gridLayout;
	
	GridBagConstraints constraints;
	
	JLabel cpuLabels[];
	
	private ObserverListPopup(JFrame frame) {
		super(frame);
		
		JPanel p = new JPanel();
		p.setBorder(new EmptyBorder(10,10,5,10));
		p.setLayout(new BorderLayout());
		
		observerPanel = new JPanel();
		titlePanel = new JPanel();
		observerPanel.setLayout(gridLayout = new GridBagLayout());
		constraints = new GridBagConstraints();

		JPanel s = new JPanel();
		s.add(closeButton = new JButton("  Ok  "));
		getRootPane().setDefaultButton(closeButton);
		getContentPane().add(BorderLayout.SOUTH, s);
		closeButton.addActionListener(this);

		//p.add(BorderLayout.NORTH, titlePanel);
		p.add(BorderLayout.CENTER, observerPanel);
		p.add(BorderLayout.SOUTH, s);
		setContentPane(p);

		pack();
		this.setModal(false);
		this.setResizable(false);
//		setAlwaysOnTop(true);
	}
	
	public static void show(JFrame frame, Point location, PamDataBlock dataBlock) {
		if (singleInstance == null) {
			singleInstance = new ObserverListPopup(frame);
		}
		singleInstance.setDataBlock(dataBlock);
		singleInstance.setLocation(location);
		singleInstance.setVisible(true);
	}

	public void setDataBlock(PamDataBlock dataBlock) {
		this.dataBlock = dataBlock;
		this.setTitle("All observers of " + dataBlock.getDataName());
		
		observerPanel.removeAll();
		cpuLabels = new JLabel[dataBlock.countObservers()];
		constraints.insets = new Insets(3,3,3,3);
		constraints.gridy = 0;
		constraints.anchor = GridBagConstraints.WEST;
//		List<PamObserver> observers = dataBlock.getPamObservers();
		String str;
		constraints.gridx = 0;
		addComponent(observerPanel, new JLabel("Observer name"), constraints);
		constraints.gridx = 1;
		addComponent(observerPanel, new JLabel("Required time"), constraints);
		constraints.gridx = 2;
		JLabel cpuLabel;
		addComponent(observerPanel, cpuLabel = new JLabel("CPU"), constraints);
		cpuLabel.setToolTipText(cpuWarning);
		constraints.gridy ++;
		for (int i = 0; i < dataBlock.countObservers(); i++) {
			PamObserver obs = dataBlock.getPamObserver(i);
			constraints.gridx = 0;
			constraints.anchor = GridBagConstraints.WEST;
			addComponent(observerPanel, new JLabel(obs.getObserverName()), constraints);
			constraints.gridx = 1;
			constraints.anchor = GridBagConstraints.CENTER;
			str = String.format("%.1f s", (double) obs.getRequiredDataHistory(dataBlock, null)/1000);
			addComponent(observerPanel, new JLabel(str), constraints);
			constraints.gridx = 2;
			constraints.anchor = GridBagConstraints.WEST;
			addComponent(observerPanel, cpuLabels[i] = new JLabel(), constraints);
			cpuLabels[i].setToolTipText(cpuWarning);
			cpuLabels[i].setText(String.format("%.1f%%", dataBlock.getCPUPercent(i)));
			constraints.gridy ++;
		}
		pack();
	}

	void addComponent(JPanel panel, Component p, GridBagConstraints constraints){
		((GridBagLayout) panel.getLayout()).setConstraints(p, constraints);
		panel.add(p);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == closeButton) {
			setVisible(false);
		}		
	}

	Timer t = new Timer(1000, new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent evt) {
			for (int i = 0; i < Math.min(dataBlock.countObservers(), cpuLabels.length); i++) {
				cpuLabels[i].setText(String.format("%.1f%%", dataBlock.getCPUPercent(i)));
			}
		}
	});

	@Override
	public void setVisible(boolean b) {
		if (b) t.start();
		else t.stop();
		super.setVisible(b);
	}
}
