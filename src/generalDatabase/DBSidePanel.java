package generalDatabase;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamController.PamController;
import PamView.ColorManaged;
import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamView.PamSidePanel;
import PamView.dialog.PamDialog;
import PamView.dialog.PamLabel;

public class DBSidePanel implements PamSidePanel {

	JPanel p;
	
	DBControl dbControl;
	
	JLabel databaseName, databaseStatus;
//	databaseType;
	JLabel writeOKs, writeErrors;
	
	TitledBorder titledBorder;
	
	public DBSidePanel(DBControl dbControl) {
		super();
		this.dbControl = dbControl;
		p = new DBPanel();
//		PamColors.getInstance().registerComponent(p, PamColor.BORDER);
		p.setBorder(titledBorder = new TitledBorder("Database"));
		titledBorder.setTitleColor(PamColors.getInstance().getColor(PamColor.AXIS));
		p.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = c.gridy = 0;
		c.gridwidth = 2;
//		PamDialog.addComponent(p, databaseType = new JLabel("System"), c);
//		c.gridwidth = 1;
		
//		c.gridx = 0;
//		c.gridy ++;
//		PamDialog.addComponent(p, new PamLabel("Database : "), c);
//		c.gridx ++;
		PamDialog.addComponent(p, databaseName = new PamLabel(" "), c);

		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy ++;
		c.anchor = GridBagConstraints.EAST;
		PamDialog.addComponent(p, new PamLabel("Writes / second : "), c);
		c.gridx ++;
		c.anchor = GridBagConstraints.WEST;
		PamDialog.addComponent(p, writeOKs = new PamLabel(""), c);
		c.gridx = 0;
		c.gridy ++;
		c.anchor = GridBagConstraints.EAST;
		PamDialog.addComponent(p, new PamLabel("Write failures : "), c);
		c.gridx ++;
		c.anchor = GridBagConstraints.WEST;
		PamDialog.addComponent(p, writeErrors = new PamLabel(""), c);
		
	}
	
	public void updatePanel() {
		DBSystem dbSystem = dbControl.databaseSystem;
		if (dbSystem == null) {
			titledBorder.setTitle("Database Error");
			databaseName.setText("No connection");
			p.repaint();
			return;
		}
		titledBorder.setTitle(dbSystem.getSystemName());
		if (PamController.getInstance().isInitializationComplete()) {
//			if (dbSystem.getConnection() == null) {
			if (dbControl.getConnection() == null) {
				databaseName.setText("No connection");		
			}
			databaseName.setText(dbSystem.getShortDatabaseName());
		}
		writeCount(0,0);
		p.repaint();
	}
	
	private boolean lastErrors;
	public void writeCount(int writeOKs, int writeErrors) {
		this.writeOKs.setText(String.format("%d", writeOKs));
		this.writeErrors.setText(String.format("%d", writeErrors));
		if (lastErrors != (writeErrors > 0)) {
			lastErrors = (writeErrors > 0);
			setColours(lastErrors);
		}
	}
	private void setColours(boolean errors) {
		if (errors) {
			p.setBackground(Color.RED);
		}
		else {
			p.setBackground(PamColors.getInstance().getColor(PamColor.BORDER));
		}
	}

	@Override
	public JComponent getPanel() {
		// TODO Auto-generated method stub
		return p;
	}
	
	

	@Override
	public void rename(String newName) {
		// TODO Auto-generated method stub
		
	}
	
	class DBPanel extends JPanel implements ColorManaged {
		
		@Override
		public void setBackground(Color bg) {
			super.setBackground(bg);
			if (titledBorder != null) {
				titledBorder.setTitleColor(PamColors.getInstance().getColor(PamColor.AXIS));
			}
		}

		@Override
		public PamColor getColorId() {
			return PamColor.BORDER;
		}
	}

}
