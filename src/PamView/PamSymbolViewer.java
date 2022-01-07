package PamView;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import PamView.panel.PamFlowLayout;

/**
 * Class to show a PamSymbol alongside a button which allows the
 * user to change it. 
 * @author Doug Gillespie
 *
 */
public class PamSymbolViewer {
	
	private PamSymbol symbol;

	private SymbolPanel symbolPanel;
	
	private JButton changeButton;
	
	private JPanel panel;
	
	private Window parentFrame;
	
	public PamSymbolViewer(Window parentFrame, String title) {
		FlowLayout flow;
		this.parentFrame = parentFrame;
		panel = new JPanel(flow = new PamFlowLayout(FlowLayout.LEFT));
		if (title != null) {
			panel.add(new JLabel(title));
		}
		panel.add(symbolPanel = new SymbolPanel());
		panel.add(changeButton = new JButton("Change..."));
		changeButton.addActionListener(new ChangeButton());
	}
	
	public Component getComponent() {
		return panel;
	}
	
	public void setSymbol(PamSymbol symbol) {
		if (symbol == null) {
			this.symbol = null;
		}
		else {
			this.symbol = symbol.clone();
		}
		symbolPanel.repaint();
	}
	
	public PamSymbol getSymbol() {
		return symbol;
	}
	
	private void changeButton() {
		PamSymbol newSymbol = PamSymbolDialog.show(parentFrame, getSymbol());
		if (newSymbol != null) {
			setSymbol(newSymbol);
		}
	}
	
	private class ChangeButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			changeButton();
		}
	}
	
	private class SymbolPanel extends JPanel {
		private final int w = 32;
		private final int h = 32;
		private SymbolPanel() {
			setPreferredSize(new Dimension(w, h));
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (symbol == null) {
				return;
			}
			symbol.draw(g, new Point(w/2,h/2), w/2, h/2);
		}

	}
	
}
