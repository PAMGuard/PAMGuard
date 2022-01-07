package GPS;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class ShipDimensionsPanel extends JPanel {

	
	public ShipDimensionsPanel(Component shipDrawing, Component fieldsComponent) {
		super();
//		shipDrawing = new ShipDimensionsDrawing();
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.setBorder(new EmptyBorder(5,5,5,5));
		p.setBorder(new TitledBorder("Ship Dimensions"));
		p.add(BorderLayout.CENTER, shipDrawing);
		JPanel q = new JPanel();
		q.setLayout(new BorderLayout());
		q.add(BorderLayout.NORTH, fieldsComponent);
//		q.add(BorderLayout.NORTH, new ShipDimensionsFields());
		p.add(BorderLayout.EAST, q);
		p.add(BorderLayout.NORTH, new JLabel("Relative to GPS receiver"));
		this.add(p);
	}
	
}
