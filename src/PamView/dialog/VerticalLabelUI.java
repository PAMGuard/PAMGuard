package PamView.dialog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.plaf.basic.BasicLabelUI;

/**
 * UI for rotating text in VerticalLabel
 * @author Gratefully taken from http://tech.chitgoks.com/2009/11/13/rotate-jlabel-vertically/
 *
 */
public class VerticalLabelUI extends BasicLabelUI {

	protected double rotAngle;

	/**
	 * @param rotAngle
	 */
	public VerticalLabelUI(double rotAngle) {
		super();
		this.rotAngle = rotAngle;
	}

	public VerticalLabelUI(boolean clockwise) {
		super();
		if (clockwise) {
			rotAngle = Math.PI / 2;
		}
		else {
			rotAngle = -Math.PI / 2;
		}
	}

	@Override
	public Dimension getPreferredSize(JComponent c) {
		Dimension dim = super.getPreferredSize(c);
		return new Dimension( dim.height, dim.width );
	}

	private static Rectangle paintIconR = new Rectangle();
	private static Rectangle paintTextR = new Rectangle();
	private static Rectangle paintViewR = new Rectangle();
	private static Insets paintViewInsets = new Insets(0, 0, 0, 0);

	@Override
	public void paint(java.awt.Graphics g, JComponent c) {
		JLabel label = (JLabel)c;
		String text = label.getText();
		Icon icon = (label.isEnabled()) ? label.getIcon() : label.getDisabledIcon();

		if ((icon == null) && (text == null)) {
			return;
		}

		FontMetrics fm = g.getFontMetrics();
		paintViewInsets = c.getInsets(paintViewInsets);

		paintViewR.x = paintViewInsets.left;
		paintViewR.y = paintViewInsets.top;

		// Use inverted height &amp; width
		paintViewR.height = c.getWidth() - (paintViewInsets.left + paintViewInsets.right);
		paintViewR.width = c.getHeight() - (paintViewInsets.top + paintViewInsets.bottom);

		paintIconR.x = paintIconR.y = paintIconR.width = paintIconR.height = 0;
		paintTextR.x = paintTextR.y = paintTextR.width = paintTextR.height = 0;

		String clippedText = layoutCL(label, fm, text, icon, paintViewR, paintIconR, paintTextR);

		Graphics2D g2 = (Graphics2D) g;
		AffineTransform tr = g2.getTransform();
		g2.rotate(rotAngle);
		if (rotAngle > 0) {
//			g2.rotate( Math.PI / 2 );
			g2.translate( 0, - c.getWidth() );
		} else {
//			g2.rotate( - Math.PI / 2 );
			g2.translate( - c.getHeight(), 0 );
		}

		if (icon != null) {
			icon.paintIcon(c, g, paintIconR.x, paintIconR.y);
		}

		if (text != null) {
			int textX = paintTextR.x;
			int textY = paintTextR.y + fm.getAscent();

			if (label.isEnabled()) {
				paintEnabledText(label, g, clippedText, textX, textY);
			} else {
				paintDisabledText(label, g, clippedText, textX, textY);
			}
		}
		g2.setTransform( tr );
//		g2.setColor(Color.RED);
//		g2.fill(c.getBounds());
	}
}
