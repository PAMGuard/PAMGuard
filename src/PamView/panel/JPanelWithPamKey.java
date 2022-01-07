package PamView.panel;

import PamView.ColorManaged;
import PamView.PamColors.PamColor;

public class JPanelWithPamKey extends PamPanel implements ColorManaged {

//	private GridBagLayout gridBagLayout;
	
	private KeyPanel keyPanel;
//	GridBagLayout.
//	private int keyPosition = GridBagConstraints.NORTHWEST;
	
	private CornerLayout cornerLayout;
	private CornerLayoutContraint gc = new CornerLayoutContraint();
		
	public JPanelWithPamKey() {
		super(PamColor.PlOTWINDOW);
		setLayout(cornerLayout = new CornerLayout(gc));
	}


	public void setLayout(CornerLayout mgr) {
		this.cornerLayout = mgr;
		super.setLayout(mgr);
	}



	public int getKeyPosition() {
		return gc.anchor;
	}

	public void setKeyPosition(int keyPosition) {	
		gc.anchor = keyPosition;
		if (keyPanel != null) {
			replaceKeyPanel(keyPanel);
		}
	}

	public KeyPanel getKeyPanel() {
		return keyPanel;
	}

	public void setKeyPanel(KeyPanel keyPanel) {
		if (this.keyPanel == keyPanel) {
			return;
		}
		else {
			replaceKeyPanel(keyPanel);
		}
	}
	
	protected void drawKeyOnTop() {
		if (keyPanel != null) {
			keyPanel.getPanel().repaint(10);
		}
	}
	
	private void replaceKeyPanel(KeyPanel newPanel) {
		if (keyPanel != null) {
//			synchronized (keyPanel) {
				remove(keyPanel.getPanel());
//			}
		}
		keyPanel = newPanel;
		if (keyPanel != null) {
//			synchronized (newPanel) {	
				add(keyPanel.getPanel(), gc);
//			}
		}
		this.invalidate();
		this.repaint(10);
		drawKeyOnTop();
	}


//	@Override
//	public Component getComponent(int i) {
////		int n = getComponentCount();
//		try {
//			return super.getComponent(i);
//		}
//		catch (ArrayIndexOutOfBoundsException e) {
//			return null;
//		}
//	}
	
}
