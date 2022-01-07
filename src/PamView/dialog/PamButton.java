package PamView.dialog;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.plaf.ButtonUI;

import PamView.ColorManaged;
import PamView.PamColors;
import PamView.PamColors.PamColor;

//import com.sun.java.swing.plaf.windows.WindowsButtonUI;

public class PamButton extends JButton implements ColorManaged {

//	private PamButtonUI buttonUI;
	
	public PamButton() {
		super();
		setOpaque(true);
//		PamColors.getInstance().registerComponent(this, PamColor.BORDER);
	}

	public PamButton(Action a) {
		super(a);
		setOpaque(true);
//		PamColors.getInstance().registerComponent(this, PamColor.BORDER);
	}

	public PamButton(Icon icon) {
		super(icon);
		setOpaque(true);
//		PamColors.getInstance().registerComponent(this, PamColor.BORDER);
	}

	public PamButton(String text, Icon icon) {
		super(text, icon);
		setOpaque(true);
//		PamColors.getInstance().registerComponent(this, PamColor.BORDER);
	}

	public PamButton(String text) {
		super(text);
		setOpaque(true);
//		ui = buttonUI = new PamButtonUI();
//		setBorderPainted(false);
//		set
//		ButtonUI bui = this.getUI();
//		PamColors.getInstance().registerComponent(this, PamColor.BORDER);
	}

	
	private PamColor defaultColor = PamColor.BUTTONFACE;
	
	public PamColor getDefaultColor() {
		return defaultColor;
	}

	public void setDefaultColor(PamColor defaultColor) {
		this.defaultColor = defaultColor;
	}

	@Override
	public PamColor getColorId() {
		return defaultColor;
	}
//    @Override
//    public void paint(Graphics g)
//    {
//        g.setColor( Color.green );
//        g.fillRect(0, 0, getSize().width, getSize().height);
////        super.paint(g);
//    }
	
	@Override
	public void setBackground(Color bg) {
		super.setBackground(bg);
		this.setForeground(PamColors.getInstance().getColor(PamColor.AXIS));
//		if (buttonUI != null) {
//			buttonUI.setColours();
//		}
//		if (WindowsButtonUI.class.isAssignableFrom(ui.getClass())) {
//			WindowsButtonUI wbui = (WindowsButtonUI) ui;
//			wbui.
//		}
	}

//	class PamButtonUI extends WindowsButtonUI {
//		public void setColours() {
//			this.focusColor = Color.pink;
//			this.
//		}
//	}
}
