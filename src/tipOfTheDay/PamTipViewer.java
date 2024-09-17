package tipOfTheDay;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import PamView.dialog.PamDialog;

/**
 * Viewer dialog for PAMGUARD tip of the day
 * @author Doug Gillespie
 * @see TipOfTheDayManager
 *
 */
public class PamTipViewer extends PamDialog {

	private static PamTipViewer singleInstance;

	private TipOfTheDayManager tipManager;

	private PamTip currentTip;

	private JTextPane tipText;

	private JTextArea tipTitle;

	private JCheckBox showAtStartup;

	private JButton nextTip, prevTip, close;

	private String[] styles = new String[3];

	private StyledDocument doc; 

	private PamTipViewer(TipOfTheDayManager tipManager, Window parentFrame) {
		super(parentFrame, "PAMGUARD Tip of the day", false);
		this.tipManager = tipManager;
		getCancelButton().setVisible(false);
		getOkButton().setText("Close");
		tipText = new JTextPane();
		tipText.setPreferredSize(new Dimension(460, 160));
		tipText.setEditable(false);
		tipText.setBorder(new EmptyBorder(20,20,20,20));
		//		tipText.setBorder(BorderFactory.createBevelBorder(1));

		JScrollPane scrollPane = new JScrollPane(tipText);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setWheelScrollingEnabled(true);
		JPanel p = new JPanel(new BorderLayout());
		p.setBorder(new EmptyBorder(10, 5, 0, 5));
		p.add(BorderLayout.CENTER, scrollPane);
//		this.getButtonPanel().setBorder(new EmptyBorder(0,0,0,5));
		getButtonPanel().setVisible(false);

		JPanel buttonPanel = new JPanel();

		JPanel bp2 = new JPanel(new BorderLayout());
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		bp2.add(BorderLayout.WEST, showAtStartup = new JCheckBox("Show tips at startup"));
		buttonPanel.add(prevTip = new JButton("Prev'"));
		buttonPanel.add(nextTip = new JButton("Next"));
		buttonPanel.add(close = new JButton("Close"));
		prevTip.addActionListener(new PrevButton());
		nextTip.addActionListener(new NextButton());
		close.addActionListener(new CloseButton());
		bp2.add(BorderLayout.EAST, buttonPanel);

		p.add(BorderLayout.SOUTH, bp2);

		setDialogComponent(p);

		setupStyles();
		this.setResizable(true);
	}

	private void setupStyles(){

		styles[0] = "icon";
		styles[1] = "bold";
		styles[2] = "regular";        
		Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
		Style s;
		doc = tipText.getStyledDocument();

		s = doc.addStyle("icon", def);
		StyleConstants.setAlignment(s, StyleConstants.ALIGN_CENTER);
		ImageIcon bulbIcon = new ImageIcon(ClassLoader
				.getSystemResource("Resources/Bulbgraph.png"));
		if (bulbIcon != null) {
			StyleConstants.setIcon(s, bulbIcon);
		}

		Style regular = doc.addStyle("regular", def); 

		s = doc.addStyle("bold", regular);
		StyleConstants.setBold(s, true);


	}

	static public void showTip(TipOfTheDayManager tipManager, Window window, PamTip pamTip) {
		if (singleInstance == null || singleInstance.getOwner() != window) {
			singleInstance = new PamTipViewer(tipManager, window);
		}
		singleInstance.setTip(pamTip);
		singleInstance.setParams();
		singleInstance.setVisible(true);
	}

	//	private void setTip(PamTip pamTip) {
	//		currentTip = pamTip;
	//		URL url = ClassLoader.getSystemResource("Resources/pamguardIcon.png");
	//		String txt = String.format("<html><img alt=\"\" src=\"./Resources/Bulbgraph.png\"/> " +
	//				" <em><b>%s</b></em><br><br>%s</html>", 
	//				pamTip.getTopic(), pamTip.getTip());
	////		tipText.setContentType("text/html; charset=EUC-JP");
	//		tipText.setContentType("text/html");
	//		tipText.setText(txt);
	//	}
	private void setTip(PamTip pamTip) {
		currentTip = pamTip;
		String[] txt = new String[3];


		txt[0] = "  ";
		txt[1] = "  " + pamTip.getTopic() + "\n\n";
		txt[2] = pamTip.getTip();
		try {
			doc.remove(0, doc.getLength());

			for (int i = 0; i < 3; i++) {
//				System.out.println("Doc length = " + doc.getLength());
				doc.insertString(doc.getLength(), txt[i], doc.getStyle(styles[i]));
			} 
		}catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub

	}

	private void setParams() {
		showAtStartup.setSelected(tipManager.isShowAtStart());
	}

	@Override
	public boolean getParams() {
		tipManager.setShowAtStart(showAtStartup.isSelected());
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

	private void prevButton() {
		setTip(tipManager.getPrevTip(currentTip));
	}

	private void nextButton() {
		setTip(tipManager.getNextTip(currentTip));
	}

	private void closeButton() {
		if (getParams()) {
			setVisible(false);
		}
	}

	class PrevButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			prevButton();
		}
	}

	class NextButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			nextButton();
		}
	}
	class CloseButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			closeButton();
		}
	}

}
