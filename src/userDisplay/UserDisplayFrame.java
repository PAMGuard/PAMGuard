package userDisplay;

import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;

import PamView.PamIcon;

/**
 * Frames for general purpose user displays. 
 * Doesn't do much apart from use the normal frame functions, but
 * does store a reference to the source of the plot so that
 * it can be remembered and recreated when PAMGuard restarts.  
 * @author Doug Gillespie
 *
 */
public class UserDisplayFrame extends JInternalFrame {
	
	private UserDisplayProvider userDisplayProvider;
	
	private UserDisplayComponent userDisplayComponent;

	public UserDisplayFrame(UserDisplayControl userDisplayControl, UserDisplayProvider userDisplayProvider, String uniqueDisplayName) {
		super(userDisplayProvider.getName(), true, true, true, true);
		this.userDisplayProvider = userDisplayProvider;
		userDisplayComponent = userDisplayProvider.getComponent(userDisplayControl, uniqueDisplayName);
		if (userDisplayComponent == null) {
			return;
		}
		userDisplayComponent.setUniqueName(uniqueDisplayName);
//		getLayeredPane().add(userDisplayComponent.getComponent(), JLayeredPane.DEFAULT_LAYER);
		add(userDisplayComponent.getComponent());
		setTitle(userDisplayComponent.getFrameTitle());

		try {
			setFrameIcon(PamIcon.getPAMGuardImageIcon(PamIcon.SMALL));
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		setSize(900, 400);
		setVisible(true);
	}

	/**
	 * @return the userDisplayProvider
	 */
	public UserDisplayProvider getUserDisplayProvider() {
		return userDisplayProvider;
	}

	public UserDisplayComponent getUserDisplayComponent() {
		return userDisplayComponent;
	}
/*
	@Override
	public PamColor getColorId() {
		return PamColor.BORDER;
	}

	@Override
	public void setForeground(Color fg) {
//		// TODO Auto-generated method stub
		super.setForeground(fg);
	}

	@Override
	public void setBackground(Color bg) {
//		// TODO Auto-generated method stub
		super.setBackground(bg);
		Border border = this.getBorder();
		if (border != null) {
			try {
//				System.out.println(border.getClass().getName());
				if (border instanceof CompoundBorder) {
					CompoundBorder cb = (CompoundBorder) border;
					setBorderCol(cb.getInsideBorder(), bg);
					setBorderCol(cb.getOutsideBorder(), bg);
				}
			}
			catch (Exception e) {

			}
		}
	}
	
	private void setBorderCol(Border border, Color col) {
		if (border == null) {
			return;
		}
		System.out.println(border.getClass().getName());
		if (border instanceof InternalFrameLineBorder) {
			InternalFrameLineBorder ifb = (InternalFrameLineBorder) border;
//			ifb.s
		}
		else if (border instanceof BevelBorder) {
			BevelBorder bb = (BevelBorder) border;
//			bb.
		}
	}
	*/
	
}
