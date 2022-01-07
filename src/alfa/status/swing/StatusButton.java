package alfa.status.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.TitledBorder;


import PamController.status.ModuleStatus;
import PamController.status.RemedialAction;
import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.dialog.PamLabel;
import PamView.panel.PamPanel;

public class StatusButton {
	
	protected JLabel textOutput;
	
	private DIYButton diyButton;
	
	protected PamPanel mainPanel;
	
	private static final int DEFULATSIZE = 30;
	
	private PamSymbol colouredIcon = new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, 20, 20, true, Color.BLUE, Color.BLUE);

	protected PamLabel nameLabel;

	protected ModuleStatus latestStatus;
	
	protected Color[] buttonColours = {Color.GREEN, Color.ORANGE, Color.RED};

	public StatusButton(String name) {
		this(name, new Dimension(DEFULATSIZE, DEFULATSIZE));
	}
	
	public StatusButton(String name, Dimension size) {
		this.mainPanel = createButton(name, size);
	}
	
	protected PamPanel createButton(String name, Dimension size) {
		mainPanel = new PamPanel(PamColor.BORDER);
//		simpleButton = new JButton(colouredIcon);
//		Border bb = simpleButton.getBorder();
//		System.out.println(bb);
		textOutput = new PamLabel("Status information");
		diyButton = new DIYButton(size);
		nameLabel  = new PamLabel(name + " ", JLabel.RIGHT);
//		if (false) {
//			mainPanel.setBorder(new TitledBorder(name));
//			mainPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
//			mainPanel.add(diyButton);
//			mainPanel.add(textOutput);
//		}
//		else {
			mainPanel.setLayout(new BorderLayout());
			mainPanel.add(nameLabel);
			mainPanel.add(diyButton, BorderLayout.EAST);
//			mainPanel.add(textOutput);
//		}
		diyButton.addMouseListener(new StatusMouse());
		
		return mainPanel;
	}
	
	/**
	 * Get the main component. 
	 * @return the main component. 
	 */
	public JComponent getComponent() {
		return mainPanel;
	}
	
	/**
	 * Set the colour for a particular state
	 * @param state 0 1 or 2
	 * @param color colour to display. 
	 */
	public void setColor(int state, Color color) {
		if (state >= buttonColours.length) {
			return;
		}
		buttonColours[state] = color;
	}
	
	private class DIYButton extends PamPanel {

		public DIYButton(Dimension d) {
			super(PamColor.BORDER);
			setPreferredSize(d);
		}
		
		/**
		 * 
		 */
		public DIYButton() {
			this(new Dimension(DEFULATSIZE, DEFULATSIZE));
		}

		/* (non-Javadoc)
		 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
		 */
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			double h = g.getClipBounds().getHeight();
			double w = g.getClipBounds().getWidth();
			int h2 = getHeight();
			int sz = (int) Math.min(h, getWidth());
			int ww = sz - 6;
			colouredIcon.draw(g, new Point(getWidth()/2-1, getHeight()/2-1), ww, ww);
		}


		
		
	}
	
	public void setName(String name) {
		nameLabel.setText(name + " ");
	}

	/**
	 * Update the status of this control, tooltips, colour, etc
	 * all depend on the status
	 * @param b
	 * @param moduleStatus
	 */
	public synchronized void setStatus(boolean exists, ModuleStatus moduleStatus) {
		String txt;
		Color col = null;
		if (moduleStatus != null) {
			txt = moduleStatus.toString();
			int stat = Math.max(0, Math.min(moduleStatus.getStatus(), buttonColours.length-1));
			col = buttonColours[stat];
//			switch (moduleStatus.getStatus()) {
//			case 0:
//				col = Color.GREEN;
//				break;
//			case 1:
//				col = Color.ORANGE;
//				break;
//			default:
//				col = Color.RED;
//				break;			
//			}
			if (moduleStatus.getName() != null) {
				nameLabel.setText(moduleStatus.getName() + " ");
			}
		}
		else if (exists == false) {
			txt = "not present\n";	
			col = Color.RED;
		}
		else {		
			txt = "No status data";
			col = Color.ORANGE;
		}
		
		latestStatus = moduleStatus;
		
		textOutput.setText(txt);
		diyButton.setToolTipText("<html>"+txt);
		if (col == null) {
//			simpleButton.setIcon(null);
		}
		else {
			colouredIcon.setFillColor(col);
			colouredIcon.setLineThickness(1);
			colouredIcon.setLineColor(PamColors.getInstance().getForegroudColor(PamColor.AXIS));
//			simpleButton.setIcon(colouredIcon);
		}
		diyButton.repaint();
	}
	
	private class StatusMouse extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				showRemedialMenu(e);
			}
		}
		
	}

	public synchronized void showRemedialMenu(MouseEvent e) {
		if (latestStatus == null) {
			return;
		}
		RemedialAction remedialAction = latestStatus.getRemedialAction();
		if (remedialAction == null) {
			return;
		}
		JPopupMenu popMenu = new JPopupMenu(remedialAction.getInfo());
		JMenuItem menuItem = new JMenuItem("Fix problem: " + remedialAction.getInfo());
		menuItem.addActionListener(new RemedialActionListener(latestStatus, remedialAction));
		popMenu.add(menuItem);
		
		popMenu.show(e.getComponent(), e.getX(), e.getY());
	}
	
	private class RemedialActionListener implements ActionListener {

		private RemedialAction RemedialAction;
		private ModuleStatus currentStatus;
		
		public RemedialActionListener(ModuleStatus currentStatus, RemedialAction remedialAction) {
			super();
			this.currentStatus = currentStatus;
			RemedialAction = remedialAction;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			RemedialAction.takeAction(currentStatus);
		}
		
	}

}
