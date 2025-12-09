package PamView.component;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JScrollPane;
import javax.swing.JViewport;

import PamView.ColorManaged;
import PamView.PamColors.PamColor;

public class PamScrollPane extends JScrollPane implements ColorManaged {

	public PamScrollPane() {
	}

	public PamScrollPane(Component view) {
		super(view);
		setVerticalScrollBar(new PamScrollBar());
	}

	public PamScrollPane(int vsbPolicy, int hsbPolicy) {
		super(vsbPolicy, hsbPolicy);
		setVerticalScrollBar(new PamScrollBar());
	}

	public PamScrollPane(Component view, int vsbPolicy, int hsbPolicy) {
		super(view, vsbPolicy, hsbPolicy);
		setVerticalScrollBar(new PamScrollBar());
		// TODO Auto-generated constructor stub
	}

	@Override
	public PamColor getColorId() {
		return PamColor.BORDER;
	}

	@Override
	public void setBackground(Color bg) {
		super.setBackground(bg);
		JViewport vp = getViewport();
		if (vp != null) {
			vp.setBackground(bg);
		}
	}

	@Override
	public void setForeground(Color fg) {
		super.setForeground(fg);
		JViewport vp = getViewport();
		if (vp != null) {
			vp.setForeground(fg);
		}
	}
	
//	@Override
//	public void setBackground(Color bg) {
//		try{
//		javax.swing.UIManager.put("ScrollBar.thumb", new javax.swing.plaf.ColorUIResource(33,129,176));
//		javax.swing.UIManager.put("Button.foreground", new javax.swing.plaf.ColorUIResource(0,0,0));
//		}catch(Exception e){
//		e.printStackTrace();
//		}
//	}

}
