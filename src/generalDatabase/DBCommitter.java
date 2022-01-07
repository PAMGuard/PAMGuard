package generalDatabase;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import javax.swing.Timer;

import PamUtils.PamCalendar;

/**
 * Class to handle regular commits of the data when Auto Commit is not
 * set. Data commits will occur at least every n seconds and at least
 * every m data writes, but not committing every record should speed things up 
 * a little.  
 * @author dg50
 *
 */
public class DBCommitter {

	private DBControl dbControl;
	
	private int unComitted = 0;
	
	private long lastCommitTime = 0;

	private Timer swingTimer;

	public DBCommitter(DBControl dbControl) {
		super();
		this.dbControl = dbControl;
		swingTimer = new Timer(3000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				commitTimer();
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		swingTimer.stop();
	}

	protected void commitTimer() {
		PamConnection con = dbControl.getConnection();
		if (con == null) return;
		commitNow(con);
	}

	public boolean checkCommit(PamConnection pamCon) {
		unComitted ++;
		if (unComitted >= 10) {
			commitNow(pamCon);
		}
		return true;
	}
	
	public synchronized boolean commitNow(PamConnection pamCon) {
		if (unComitted == 0) {
			return true;
		}
		unComitted = 0;
		
		if (dbControl.dbParameters.getUseAutoCommit()) {
			return true;
		}
		if (pamCon == null || pamCon.getConnection() == null) {
			return false;
		}
		try {
			if (pamCon.getConnection().getAutoCommit() == false) {
				pamCon.getConnection().commit();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		lastCommitTime = PamCalendar.getTimeInMillis();
		swingTimer.restart();
		return true;
	}
}
