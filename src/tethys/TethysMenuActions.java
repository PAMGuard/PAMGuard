package tethys;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import tethys.dbxml.TethysException;
import tethys.niluswraps.PDeployment;

/*
 * Some standard meny dirven functions which we may want to call from
 * a few different places.
 */
public class TethysMenuActions {

	private TethysControl tethysControl;

	public TethysMenuActions(TethysControl tethysControl) {
		super();
		this.tethysControl = tethysControl;
	}

	public void deploymentMouseActions(MouseEvent e, PDeployment pDeployment) {
		ArrayList<String> detDocNames = tethysControl.getDbxmlQueries().getDetectionsDocuments(pDeployment.deployment.getId());
//		System.out.println("Detections for deployment " + pDeployment.deployment.getId());
//		for (String detName : detDocNames) {
//			System.out.println(detName);
//		}
		JPopupMenu menu = new JPopupMenu();
		if (detDocNames.size() == 0) {
			JMenuItem menuItem = new JMenuItem("Delete deployment " + pDeployment.deployment.getId());
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						deleteDeployment(pDeployment);
					} catch (TethysException e1) {
						tethysControl.showException(e1);
					}
				}
			});
			menu.add(menuItem);
		}
		else {
			String str = String.format("Delete deployment %s and %d Detections documents", pDeployment.deployment.getId(), detDocNames.size());
			JMenuItem menuItem = new JMenuItem(str);
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						deleteDeployment(pDeployment);
					} catch (TethysException e1) {
						tethysControl.showException(e1);
					}
				}
			});
			menu.add(menuItem);
		}
		menu.show(e.getComponent(), e.getX(), e.getY());
	}

	protected void deleteDeployment(PDeployment pDeployment) throws TethysException {
		tethysControl.getDbxmlConnect().deleteDeployment(pDeployment.deployment.getId());
	}
}
