package generalDatabase;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;

/**
 * Dialog which shows the progress in data map making. 
 * @author Doug Gillespie
 *
 */
public class DBMapMakingDialog extends PamDialog {

	private static DBMapMakingDialog singleInstance;
	
	private JProgressBar streamProgress;
	
	private JLabel streamName, databaseName;

	private DBMapMakingDialog(Window parentFrame) {
		super(parentFrame, "Database data mapping", false);

		JPanel p = new JPanel();
		p.setBorder(new TitledBorder("Creating data map"));
		p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
		p.add(databaseName = new JLabel("  "));
		p.add(streamName = new JLabel("  "));
		p.add(streamProgress = new JProgressBar());
//		p.setPreferredSize(new Dimension(400, 200));
		streamName.setPreferredSize(new Dimension(250, 5));
		
		setDialogComponent(p);
		
		getOkButton().setVisible(false);
		getCancelButton().setVisible(false);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setModalityType(Dialog.ModalityType.MODELESS);
	}
	
	public static DBMapMakingDialog showDialog(Frame parentFrame) {
		if (singleInstance == null || singleInstance.getOwner() == null ||
				singleInstance.getOwner() != parentFrame) {
			singleInstance = new DBMapMakingDialog(parentFrame);
		}
		singleInstance.setVisible(true);
		return singleInstance;
	}
	
	@Override
	public void setVisible(boolean visible) {
		if (visible == false) {
//			dispose();
//			singleInstance = null;
			closeLater();
		}
		else {
			super.setVisible(visible);
		}
	}
	
	public void newData(CreateMapInfo mapInfo) {
		switch (mapInfo.getStatus()) {
		case CreateMapInfo.BLOCK_COUNT:
			databaseName.setText(mapInfo.getDatabaseName());
			streamProgress.setMaximum(mapInfo.getNumBlocks());
			streamProgress.setValue(0);
			break;
		case CreateMapInfo.START_TABLE:
			streamName.setText(mapInfo.getTableName());
			streamProgress.setValue(mapInfo.getTableNum()+1);
		}
	}

	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean getParams() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
