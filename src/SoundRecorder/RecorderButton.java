package SoundRecorder;

import java.awt.Color;

import javax.swing.Action;
import javax.swing.Icon;

import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamView.dialog.PamButton;

public class RecorderButton extends PamButton {

	private boolean pressed = false;
	
	private static Color pressedColor = new Color(128, 128, 128);
	
	public RecorderButton() {
		// TODO Auto-generated constructor stub
		super();
	}

	public RecorderButton(Action a) {
		super(a);
		// TODO Auto-generated constructor stub
	}

	public RecorderButton(Icon icon) {
		super(icon);
		// TODO Auto-generated constructor stub
	}

	public RecorderButton(String text, Icon icon) {
		super(text, icon);
		// TODO Auto-generated constructor stub
	}

	public RecorderButton(String text) {
		super(text);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setBackground(Color bg) {
		setOpaque(true);
		if (!pressed) {
			super.setBackground(PamColors.getInstance().getColor(PamColor.BUTTONFACE));
			super.setForeground(PamColors.getInstance().getColor(PamColor.AXIS));
		}
		else {
			setPressedColour();
		}
	}

	public boolean isPressed() {
		return pressed;
	}

	@Override
	public void setSelected(boolean b) {
		super.setSelected(b);
		setPressed(b);
		
	}

	public void setPressed(boolean pressed) {
		this.pressed = pressed;
		if (pressed) {
			setPressedColour();
		}
		else {
			super.setBackground(PamColors.getInstance().getColor(PamColor.BUTTONFACE));
		}
	}
	
	private void setPressedColour() {
		super.setBackground(pressedColor);
	}
}
