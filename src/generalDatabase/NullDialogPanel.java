package generalDatabase;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class NullDialogPanel implements SystemDialogPanel {

	private JPanel panel;
	
	public NullDialogPanel() {
		panel = new JPanel(new BorderLayout());
		panel.setBorder(new EmptyBorder(20, 20, 20, 20));
		panel.add(new JLabel("No database drivers available on this system"));
	}
	
	@Override
	public JPanel getPanel() {
		return panel;
	}

	@Override
	public boolean getParams() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setParams() {
		// TODO Auto-generated method stub
		
	}

}
