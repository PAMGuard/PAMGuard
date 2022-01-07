package PamView.dialog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.LineMetrics;

import javax.swing.JLabel;
import javax.swing.Timer;

import org.docx4j.wml.CTFFTextInput.MaxLength;

import PamView.ColorManaged;
import PamView.PamColors;
import PamView.PamColors.PamColor;

public class ScrollingPamLabel extends JLabel implements ColorManaged {

	private static final long serialVersionUID = 1L;

	private PamColor labelColor = PamLabel.defaultColor;
	
	private Timer scrollTimer;
	
	private int textOffset = 0;

	private String packedText;

	private int maxChars;
	
	private String packer;

	private int preferredWidth;

	private String trueText;

	public ScrollingPamLabel(int maxChars) {
		this.maxChars = maxChars;
		packer = new String("                 ");
		while (packer.length() < maxChars) {
			packer += "                ";
		}
		scrollTimer = new Timer(150, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				scrollText();
			}
		});
		scrollTimer.start();
	}
	
	public ScrollingPamLabel(int maxChars, String text) {
		this(maxChars);
		setText(text);
	}
	
	/**
	 * Sort out the width which keeps changing in an annoying way
	 * @param maxChars2
	 */
	private int getPreferredWidth(int maxChars) {
		if (trueText == null || trueText.length() < maxChars) {
			return 0;
		}
		Graphics g = getGraphics();
		if (g == null) {
			return 0;
		}
		FontMetrics fm = g.getFontMetrics();
		if (fm == null) {
			return 0;
		}
//		return fm.getMaxAdvance()*maxChars;
		 return (int) fm.getStringBounds(trueText.substring(0, maxChars), g).getWidth();
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
//		if (preferredWidth == 0) {
			preferredWidth = getPreferredWidth(maxChars);
//		}
		if (preferredWidth != 0) {
			d.width = preferredWidth;
		}
		return d;
	}

	public PamColor getDefaultColor() {
		return labelColor;
	}

	public void setDefaultColor(PamColor defaultColor) {
		this.labelColor = defaultColor;
	}

	@Override
	public PamColor getColorId() {
		return labelColor;
	}
	
	@Override
	public void setBackground(Color bg) {
		super.setBackground(bg);
		this.setForeground(PamColors.getInstance().getColor(PamColor.AXIS));
	}

	@Override
	public void setText(String text) {
		trueText = packedText = text;
		if (text != null && text.length() > maxChars) {
			if (packedText != null && packer != null) {
//				trueText = packer.substring(0, trueText.length()/2) + trueText + "";
				packedText = packer.substring(0, this.maxChars) + packedText + "";
			}
		}
	}

	protected void scrollText() {
		if (packedText == null) {
			super.setText(null);
			return;
		}
		textOffset++;
		if (textOffset >= packedText.length()) {
			textOffset = 0;
		}
		scrollText(textOffset);
	}
	
	private void scrollText(int offset) {
		if (packedText == null) {
			super.setText(null);
			return;
		}
		if (packedText.length() <= maxChars) {
			super.setText(packedText);
			return;
		}
		int c1 = offset;
		int c2 = offset+maxChars;
		c2 = Math.min(c2, packedText.length());
		String dispText = packedText.substring(c1);
//		if (maxChars > dispText.length()) {
//			dispText = packer.substring(0, maxChars-dispText.length()) + dispText;
//		}
//		System.out.printf("Text: \"%s\"\n", dispText);
		super.setText(dispText);
	}
 
}
