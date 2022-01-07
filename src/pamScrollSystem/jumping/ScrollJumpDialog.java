package pamScrollSystem.jumping;

import java.awt.BorderLayout;
import java.awt.Window;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;
import javax.swing.text.html.StyleSheet.BoxPainter;

import PamView.dialog.PamDialog;

public class ScrollJumpDialog extends PamDialog {
	
	private ScrollJumpParams jumpParams;
	
	private JRadioButton scrollEnds, scrollCentre;
	
	private JCheckBox allowOuter;

	private ScrollJumpDialog(Window parentFrame) {
		super(parentFrame, "Scroll Arrows", true);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		JPanel posPanel = new JPanel();
		posPanel.setLayout(new BoxLayout(posPanel, BoxLayout.Y_AXIS));
		mainPanel.add(posPanel, BorderLayout.CENTER);
		JPanel outerPanel = new JPanel();
		outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));
		mainPanel.add(outerPanel, BorderLayout.SOUTH);
		
		posPanel.setBorder(new TitledBorder("Scroll Position"));
		scrollEnds = new JRadioButton("Scoll data to edge of display");
		scrollCentre = new JRadioButton("Scoll data to centre of display");
		ButtonGroup bg = new ButtonGroup();
		bg.add(scrollEnds);
		bg.add(scrollCentre);
		posPanel.add(new JLabel("<html>Hold down the Ctrl key, use left and right <br>arrows to move to next or previous data<br> </html>"));
		posPanel.add(scrollEnds);
		posPanel.add(scrollCentre);
		
		outerPanel.setBorder(new TitledBorder("Scroll Limits"));
		allowOuter = new JCheckBox("Allow scrolling beyond loaded data");
		outerPanel.add(allowOuter);
		
		scrollEnds.setToolTipText("Next selected data will be positioned at the left or right edge of the display");
		scrollCentre.setToolTipText("Next selected data will be positioned in the centre of the display");
		allowOuter.setToolTipText("If at the scroll limits of loaded data, the scroller will advance and load new data");
		
		setDialogComponent(mainPanel);
	}
	
	public static ScrollJumpParams showDialog(Window frame, JComponent parent, ScrollJumpParams oldParams) {
		ScrollJumpDialog dialog = new ScrollJumpDialog(frame);
		dialog.setParams(oldParams);
		if (parent != null) {
			dialog.setLocationRelativeTo(parent);
		}
		dialog.setVisible(true);
		return dialog.jumpParams;
	}

	private void setParams(ScrollJumpParams oldParams) {
		jumpParams = oldParams.clone();
		scrollEnds.setSelected(jumpParams.alignment == ScrollJumpParams.ALIGN_AT_EDGE);
		scrollCentre.setSelected(jumpParams.alignment == ScrollJumpParams.ALIGN_AT_CENTRE);
		allowOuter.setSelected(jumpParams.allowOuterScroll);
	}

	@Override
	public boolean getParams() {
		jumpParams.alignment = scrollEnds.isSelected() ? ScrollJumpParams.ALIGN_AT_EDGE : ScrollJumpParams.ALIGN_AT_CENTRE;
		jumpParams.allowOuterScroll = allowOuter.isSelected();
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		jumpParams = null;
	}

	@Override
	public void restoreDefaultSettings() {
		setParams(new ScrollJumpParams());
	}

}
