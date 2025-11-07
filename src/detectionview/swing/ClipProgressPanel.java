package detectionview.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import detectionview.DVControl;
import detectionview.DVObserver;
import detectionview.LoadProgress;

/**
 * Progress panel to show temporarily while generating clips. 
 * @author dg50
 *
 */
public class ClipProgressPanel implements DVObserver {

	private JLabel message;
	private JProgressBar progress;
	private JButton stopButton;
	
	private JPanel mainPanel;
	
	public ClipProgressPanel(DVControl dvControl) {
		// TODO Auto-generated constructor stub
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(BorderLayout.WEST, message = new JLabel());
		mainPanel.add(BorderLayout.CENTER, progress = new JProgressBar());
		mainPanel.add(BorderLayout.EAST, stopButton = new JButton("Stop"));
		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stopPressed();
			}

		});
		progress.setStringPainted(true);
		dvControl.addObserver(this);
	}

	private void stopPressed() {
		// TODO Auto-generated method stub
		
	}
	
	public JPanel getPanel() {
		return mainPanel;
	}

	@Override
	public void updateData(int updateType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadProgress(LoadProgress loadProgress) {
		message.setText(loadProgress.getMessage());
		int nTot = loadProgress.getnTotalData();
		progress.setMaximum(nTot);
		progress.setValue(loadProgress.getnCreated() + loadProgress.getnFails());
		String str = String.format("Loading data for detection %d of %d", loadProgress.getnCreated(), loadProgress.getnTotalData());
		if (loadProgress.getnFails() > 0) {
			str += String.format(", %d fails", loadProgress.getnFails());
		}
		progress.setString(str);
		progress.setForeground(Color.BLACK);
		
		if (loadProgress.getLoadState() == loadProgress.LOAD_DONE) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					mainPanel.setVisible(false);
				}
			});
		}
		else {
			mainPanel.setVisible(true);
		}
	}

	@Override
	public void updateConfig() {
		// TODO Auto-generated method stub
		
	}

}
