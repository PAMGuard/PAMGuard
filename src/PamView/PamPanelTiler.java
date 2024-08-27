package PamView;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * Class to lay out windows within a JDesktopPane
 * @author Doug
 *
 */
public class PamPanelTiler {
	
	JDesktopPane desktopPane;
	
	public PamPanelTiler(JDesktopPane desktopPane) {
		
		this.desktopPane = desktopPane;
		
	}
	
	/**
	 * auto tile the windows
	 * always have zero or one more window in x than in y and increase until
	 * there are enough spaces in the grid.
	 *
	 */
	public void tile() {
		int nX = 1;
		int nY = 1;
		int nPanels = desktopPane.getComponentCount();
		while (true) {
			if (nX*nY >= nPanels) break;
			nX++;
			if (nX*nY >= nPanels) break;
			nY++;
		}
		tileWindows(nX, nY);
	}
	
	public void tileHorizontal() {
		tileWindows(1, desktopPane.getComponentCount());
	}
	
	public void tileVertical() {
		tileWindows(desktopPane.getComponentCount(), 1);
	}
	
	private void tileWindows(int nX, int nY) {
		int nPanels = desktopPane.getComponentCount();
		Dimension d = desktopPane.getSize();
		int dX = d.width / nX;
		int dY = d.height / nY;
		int oX = 0;
		int oY = 0;
		JInternalFrame frames[] = desktopPane.getAllFrames();
		for (int i = 0; i < nPanels; i++) {
			if (frames[i].isMaximum()) {
				try {
					frames[i].setMaximum(false);
				}
				catch (PropertyVetoException Ex) {	
				}
			}
			if (frames[i].isIcon()) {
				try {
					frames[i].setIcon(false);
				}
				catch (PropertyVetoException Ex) {	
				}
			}
			frames[i].setSize(dX, dY);
			frames[i].setLocation(oX, oY);
			oX += dX;
			if (oX >= d.width - dX/2) {
				oX = 0;
				oY += dY;
			}
		}
	}
	
	public void cascade() {
		int nPanels = desktopPane.getComponentCount();
		Dimension d = desktopPane.getSize();
		int sX = d.width * 2 / 3;
		int sY = d.height * 2 / 3;
		int oX = 0;
		int oY = 0;
		int dX = 40;
		int dY = 40;
		JInternalFrame frames[] = desktopPane.getAllFrames();
		for (int i = 0; i < nPanels; i++) {
			frames[i].setSize(sX, sY);
			frames[i].setLocation(oX, oY);
			oX += dX;
			oY += dY;
		}
	}
	
	public JMenu getMenu() {
		JMenu tileMenu = new JMenu("Arrange Windows ...");
		JMenuItem menuItem;
		
		menuItem = new JMenuItem("Tile");
		menuItem.addActionListener(new Tile());
		menuItem.setToolTipText("Tile windows in a grid");
		tileMenu.add(menuItem);
		
		menuItem = new JMenuItem("Tile Horizontal");
		menuItem.addActionListener(new TileHorizontal());
		menuItem.setToolTipText("Place windows one above the other");
		tileMenu.add(menuItem);
		
		menuItem = new JMenuItem("Tile Vertical");
		menuItem.addActionListener(new TileVertical());
		menuItem.setToolTipText("Place windows side by side");
		tileMenu.add(menuItem);
		
		menuItem = new JMenuItem("Cascade");
		menuItem.addActionListener(new Cascade());
		menuItem.setToolTipText("Cascade windows across screen");
		tileMenu.add(menuItem);
		
		return tileMenu;
	}
	
	private class Tile implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			tile();
		}
	}
	
	private class TileHorizontal implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			tileHorizontal();
		}
	}
	
	private class TileVertical implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			tileVertical();
		}
	}
	
	private class Cascade implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			cascade();
		}
	}
}
