package PamView;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;

/**
 * Text area with a character limit
 * @author dg50
 *
 */
public class DBTextArea  {

	private int maxChars;
	
	private JTextArea textArea;
	
	private JScrollPane scrollPane;
	
	private static final int POLICY_STOP_TYPING = 1;
	private static final int POLICY_KEEP_TYPING = 2;
	
	private int policy = POLICY_STOP_TYPING;
	
	
	public DBTextArea(int rows, int columns, int maxChars) {
//		super(rows, columns);
		textArea = new JTextArea(rows, columns);
		this.maxChars = maxChars;
//		textArea.setPreferredSize(new Dimension(1, 50));
//		textArea.setBorder(BorderFactory.createLoweredBevelBorder());
		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(true);
		textArea.setToolTipText(String.format("Comments > %d characters long will be truncated in the database", 
				maxChars));
		textArea.addKeyListener(new CommentListener());
		scrollPane = new JScrollPane(textArea);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		if (rows > 10) {
			scrollPane.setPreferredSize(new Dimension(300, 200));
		}
	}

	class CommentListener implements KeyListener {
		@Override
		public void keyPressed(KeyEvent e) {			
		}
		@Override
		public void keyReleased(KeyEvent e) {			
		}
		@Override
		public void keyTyped(KeyEvent e) {
			checkCommentLength();
		}
	}
	
	private void checkCommentLength() {
		int commentLength = 0;
		String txt = textArea.getText();
		if (txt != null) {
			commentLength = txt.length();
		}
		if (commentLength <= maxChars) {
			textArea.setBackground(normalBackground());
		}
		else {
			textArea.setBackground(warningBackground());
			if (policy == POLICY_STOP_TYPING) {
				textArea.setText(txt.substring(0,maxChars));
			}
		}
	}

	/**
	 * Normal background for the text area, taken from the look and feel rather than
	 * being hard wired to white, so that the box doesn't turn white (with white text
	 * on it) as soon as anything is typed into it in a dark colour scheme.
	 *
	 * @return background colour for a comment of acceptable length
	 */
	private Color normalBackground() {
		Color col = UIManager.getColor("TextArea.background");
		return col == null ? Color.WHITE : col;
	}

	/**
	 * Background for a comment which is too long. Pink only works against a light
	 * background, so blend red into whatever the normal background is instead.
	 *
	 * @return warning background colour
	 */
	private Color warningBackground() {
		Color bg = normalBackground();
		return new Color((bg.getRed() + 255) / 2, bg.getGreen() / 2, bg.getBlue() / 2);
	}

	public void setDimension(Dimension dim) {
//		textArea.setMaximumSize(dim);
//		textArea.setMinimumSize(dim);
		textArea.setPreferredSize(dim);
	}

	public JComponent getComponent() {
		return scrollPane;
	}
	
	public void setText(String text) {
		textArea.setText(text);
	}
	
	public String getText() {
		return textArea.getText();
	}

}
