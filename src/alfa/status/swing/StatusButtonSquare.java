package alfa.status.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.SwingConstants;

import PamController.status.ModuleStatus;
import PamView.dialog.PamLabel;
import PamView.panel.PamPanel;

/**
 * Create a square status button. 
 * 
 * @author Jamie Macaulay
 *
 */
public class StatusButtonSquare extends StatusButton {

	/**
	 * Label for the panel
	 */
	private PamLabel label;

	/**
	 * Holder panel. 
	 */
	private PamPanel holder;

	/**
	 * The name text.
	 */
	private String name;

	private Dimension prefDim;

	/**
	 * Constructor for the status button
	 * @param name - the name of the button
	 * @param buttonsize 
	 */
	public StatusButtonSquare(String name, Dimension size) {
		super(name);
		this.name=name; 
	}

	@Override
	protected PamPanel createButton(String name, Dimension size) {

		label = new PamLabel(name, SwingConstants.CENTER); 
		label.setSize(size);
		label.setFont(new Font(null, Font.BOLD, 16));
		label.setOpaque(true); 

		holder = new PamPanel(new BorderLayout()); 	
		holder.add(label, BorderLayout.CENTER);
		holder.setPreferredSize(size);
		holder.setOpaque(true);

		return holder;
	}

	@Override
	public synchronized void setStatus(boolean exists, ModuleStatus moduleStatus) {
		String txt;
		Color col = null;
//		Debug.out.println("Hello STATUS BUTTON I am: " +moduleStatus.getStatus());
		if (moduleStatus != null) {
			txt = moduleStatus.toString();
			int stat = Math.max(0, Math.min(moduleStatus.getStatus(), buttonColours.length-1));
			col = buttonColours[stat];
//			Debug.out.println("Hello STATUS BUTTON Col: " +col + " stat: " +stat);

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

		switch (moduleStatus.getStatus()) {
		case ModuleStatus.STATUS_OK:
			label.setText(name +" OK");
			break;
		case ModuleStatus.STATUS_WARNING :
			label.setText(name +" WARNING");
			break;
		case ModuleStatus.STATUS_ERROR :
			label.setText(name +" ERROR");
			break;
		}

		label.setToolTipText("<html>"+txt);
		if (col == null) {
			//			simpleButton.setIcon(null);
			label.setBackground(Color.RED);
			holder.setBackground(Color.RED);
		}
		else {
			label.setText(txt);
			label.setBackground(col);
			holder.setBackground(col);
		}		
		
		label.setOpaque(true); 
		holder.setOpaque(true);
		label.validate();
		label.repaint();

	}

}
