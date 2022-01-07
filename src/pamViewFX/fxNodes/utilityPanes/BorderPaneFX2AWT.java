/*
 *  PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation
 * of marine mammals (cetaceans).
 *
 * Copyright (C) 2006
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */



package pamViewFX.fxNodes.utilityPanes;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import javafx.scene.Node;
import javafx.scene.layout.VBox;

/**
 * @author mo55
 *
 */
public class BorderPaneFX2AWT extends VBox {
	
	private JDialog awtParent;

	public BorderPaneFX2AWT(JDialog awtParent ) {
		super();
		this.awtParent = awtParent;
	}

	/**
	 * @return the awtParent
	 */
	public JDialog getAwtParent() {
		return awtParent;
	}

	/**
	 * @param awtParent the awtParent to set
	 */
	public void setAwtParent(JDialog awtParent) {
		this.awtParent = awtParent;
	}
	
	public static boolean repackSwingDialog(Node contentNode) {
		Node thisNode = contentNode;
		while (thisNode!=null) {
//			thisNode.autosize();
			thisNode = thisNode.getParent();
			if (thisNode instanceof BorderPaneFX2AWT) {
				JDialog swingDialog =  ((BorderPaneFX2AWT) thisNode).getAwtParent();
				SwingUtilities.invokeLater(new Runnable() {
				    @Override
				    public void run() {
						swingDialog.pack();
				    }
				});		
				return true;
			}
			else {
				thisNode = thisNode.getParent();
			}
		}
		return false;
	}
	
	

}
