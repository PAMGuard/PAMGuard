package warnings;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

public class SingleLineWarningDisplay implements WarningDisplay {

	private WarningLabel lastWarning = new WarningLabel(" ");
	
	private Timer disTimer;
	
	private PamWarning latestWarning;
	
	private int currentListPosition = 0;
	
	public SingleLineWarningDisplay() {
		WarningSystem.getWarningSystem().addDisplay(this);
		disTimer = new Timer(2000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				displayTimer();
			}
		});
		disTimer.start();
	}

	protected void displayTimer() {
		pickWarning();
	}

	/**
	 * Pick a warning to display
	 */
	private void pickWarning() {
		WarningSystem ws = WarningSystem.getWarningSystem();
		synchronized (ws) {
			int nWarnings = ws.getNumbWarnings();
			if (nWarnings == 0) {
				showWarning(null);
				return;
			}
			if (--currentListPosition < 0) {
				currentListPosition = nWarnings - 1;
			}
			showWarning(ws.getWarning(currentListPosition));
		}
	}

	@Override
	public void updateWarnings() {
		WarningSystem ws = WarningSystem.getWarningSystem();
		PamWarning w = ws.getLastWarning();
		if (w != latestWarning) {
			showWarning(w);
			latestWarning = w;
			disTimer.restart();
		}
	}

	private void showWarning(PamWarning w) {
		if (w == null) {
			lastWarning.setText("");
			lastWarning.setWarningLevel(0);
			lastWarning.setToolTipText(null);
		}
		else {
			String str = w.getWarnignLevel() > 0 ? "Warning: " : "";
			str += String.format("%s-%s   ", w.getWarningSource(), w.getWarningMessage());
			lastWarning.setText(str);
			lastWarning.setWarningLevel(w.getWarnignLevel());
			lastWarning.setToolTipText(w.getWarningTip());
		}		
	}

	public Component getComponent() {
		return lastWarning;
	}

}
