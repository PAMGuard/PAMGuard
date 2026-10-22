package PamView.panel;

import javax.swing.JComponent;

import PamView.ColorManaged;
import PamView.PamColors.PamColor;

public class JPanelWithPamKey extends PamPanel implements ColorManaged {

//	private GridBagLayout gridBagLayout;
	
	private KeyPanel keyPanel;

	private JComponent keyComponent;
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
		if (keyComponent != null) {
			JComponent current = keyComponent;
			keyComponent = null;
			remove(current);
			replaceKeyComponent(current);
		}
	}

	/**
	 * Get the gap between the key and the edges of the panel.
	 * @return gap in pixels.
	 */
	public int getKeyMargin() {
		return gc.margin;
	}

	/**
	 * Set the gap between the key and the edges of the panel.
	 * @param margin - gap in pixels.
	 */
	public void setKeyMargin(int margin) {
		gc.margin = margin;
		revalidate();
	}

	public KeyPanel getKeyPanel() {
		return keyPanel;
	}

	public void setKeyPanel(KeyPanel keyPanel) {
		JComponent newComponent = keyPanel == null ? null : keyPanel.getPanel();
		this.keyPanel = keyPanel;
		if (newComponent == keyComponent) {
			return;
		}
		replaceKeyComponent(newComponent);
	}

	/**
	 * Get the component currently shown as the key.
	 * @return the key component, or null if there is no key.
	 */
	public JComponent getKeyComponent() {
		return keyComponent;
	}

	/**
	 * Show any component as the key, e.g. a symbol legend from
	 * {@link PamView.symbol.legend.SwingSymbolLegendFactory}. This replaces any
	 * key panel which was set with {@link #setKeyPanel(KeyPanel)}.
	 * @param keyComponent - the key component, or null to remove the key.
	 */
	public void setKeyComponent(JComponent keyComponent) {
		if (this.keyComponent == keyComponent) {
			return;
		}
		this.keyPanel = null;
		replaceKeyComponent(keyComponent);
	}
	
	protected void drawKeyOnTop() {
		if (keyComponent != null) {
			keyComponent.repaint(10);
		}
	}
	
	private void replaceKeyComponent(JComponent newComponent) {
		try {
			if (keyComponent != null) {
				remove(keyComponent);
			}
			if (newComponent != null) {
				add(newComponent, gc);
			}
		}
		catch (Exception e) {

		}
		keyComponent = newComponent;
		
		this.invalidate();
		this.revalidate();
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
