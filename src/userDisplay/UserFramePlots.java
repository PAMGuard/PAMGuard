package userDisplay;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import Layout.PamFramePlots;

abstract public class UserFramePlots extends PamFramePlots implements InternalFrameListener {


	public static final int FRAME_TYPE_SPECTROGRAM = 1;
	public static final int FRAME_TYPE_RADAR = 2;
	
	private UserFrameParameters userFrameParameters;
	
	protected UserDisplayControl userDisplayControl;
	
	public UserFramePlots(UserDisplayControl userDisplayControl) {
		super();
		this.userDisplayControl = userDisplayControl;
		
	}

	abstract public void notifyModelChanged(int changeType);
	
	abstract public int getFrameType();

	public UserFrameParameters getUserFrameParameters() {
		return userFrameParameters;
	}

	public void setUserFrameParameters(UserFrameParameters userFrameParameters) {
		this.userFrameParameters = userFrameParameters;
	}

	@Override
	public void internalFrameActivated(InternalFrameEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void internalFrameClosed(InternalFrameEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void internalFrameClosing(InternalFrameEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void internalFrameDeactivated(InternalFrameEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void internalFrameDeiconified(InternalFrameEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void internalFrameIconified(InternalFrameEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void internalFrameOpened(InternalFrameEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
