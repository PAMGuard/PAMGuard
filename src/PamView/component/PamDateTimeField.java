package PamView.component;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JTextField;

import PamUtils.PamCalendar;

/**
 * Edit box for entering datetimes.
 * Will go red if the time is invalid.  
 * @author dg50
 *
 */
public class PamDateTimeField extends JTextField {

	private static final long serialVersionUID = 1L;
	private boolean includeMillseconds = false;

	public PamDateTimeField(int columns) {
		super(columns);
		addKeyListener(new ActOnKeys());
	}

	public PamDateTimeField() {
		this(16);
	}

	/**
	 * Set the datetime and display in default format. 
	 * @param timeMillseconds time in milliseconds. 
	 */
	public void setDateTime(long timeMillseconds) {
		setText(PamCalendar.formatDBDateTime(timeMillseconds, includeMillseconds));
	}

	/**
	 * @return the time in milliseconds, returning null if an invalid date form 
	 * is entered. 
	 */
	public Long getDateTime() {
		long t = PamCalendar.msFromDateString(getText());
		return t > 0 ? t : null;
	}

	private class ActOnKeys implements KeyListener { 

		@Override
		public void keyTyped(KeyEvent e) {
			boolean textOK = (getDateTime() != null);
			setBackground(textOK ? Color.WHITE : Color.PINK);
		}

		@Override
		public void keyPressed(KeyEvent e) {
		}

		@Override
		public void keyReleased(KeyEvent e) {
		}
	}

	/**
	 * Include milliseconds in the displayed time
	 * @return the includeMillseconds
	 */
	public boolean isIncludeMillseconds() {
		return includeMillseconds;
	}

	/**
	 * Include milliseconds in the displayed time
	 * @param includeMillseconds the includeMillseconds to set
	 */
	public void setIncludeMillseconds(boolean includeMillseconds) {
		this.includeMillseconds = includeMillseconds;
	}
}
