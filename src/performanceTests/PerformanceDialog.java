package performanceTests;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import PamView.dialog.PamDialog;

public class PerformanceDialog extends PamDialog {
	
	private JButton startButton, closeButton;
	private JTextArea outputText;
	private ArrayList<PerformanceTest> performanceTests;
	private String generalIntro = 
		"Running these performance tests will help tell you whether or " +
		"not your machine is up to the task of running PAMGUARD.\n" +
		"The tests only take a few seconds to execute.\n" +
		"The output can copied and pasted into an email to " +
		"the PAMGUARD support team.";
	
	private PerformanceDialog(Window parentFrame) {
		super(parentFrame, "System Performance Tests", false);
		
		performanceTests = new ArrayList<PerformanceTest>();
		performanceTests.add(new SystemInfo());
		performanceTests.add(new PamguardInfo());
		performanceTests.add(new CPUFFTTest());
		performanceTests.add(new GraphicsDotTest());
		performanceTests.add(new GraphicsDotTestBuffered());
		performanceTests.add(new GraphicsDotTest2());
		
		setHelpPoint("overview.PamMasterHelp.docs.performanceTests");
		
		startButton = getOkButton();
		startButton.setText("Start tests");
		startButton.addActionListener(new RunTests());
		closeButton = getCancelButton();
		closeButton.setText("Close");
	
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		
		JLabel label = new JLabel();
		JTextArea introLabel;
		JPanel introPanel = new JPanel(new BorderLayout());
		introLabel = new JTextArea(generalIntro);
		introLabel.setEditable(false);
		introLabel.setLineWrap(true);
		introLabel.setWrapStyleWord(true);
		introLabel.setFont(label.getFont());
		introLabel.setBackground(p.getBackground());
		introPanel.setBorder(new EmptyBorder(5,5,5,5));
		introPanel.add(BorderLayout.CENTER, introLabel);
		p.add(BorderLayout.NORTH, introPanel);
//		
		outputText = new JTextArea("");
		outputText.setEditable(false);
		outputText.setLineWrap(true);
		JScrollPane scrollPane = new JScrollPane(outputText);
		scrollPane.setPreferredSize(new Dimension(500, 300));
		p.add(scrollPane, BorderLayout.CENTER);
		
		setDialogComponent(p);
		
		setResizable(true);
	}
	
	static public void showDialog(Frame parentFrame) {
		PerformanceDialog performanceDialog = new PerformanceDialog(parentFrame);
		performanceDialog.setVisible(true);
	}
	
	class RunTests implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			runTests();
		}
	}
	
	private void runTests() {
//		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		enableButtons(false);
		outputText.setText("");
		for (int i = 0; i < performanceTests.size(); i++) {
			runTest(i, performanceTests.get(i));
		}
		enableButtons(true);
	}
	
	private boolean runTest(int iTest, PerformanceTest pt) {
		String str;
		str = String.format("%d. %s", iTest+1, pt.getName());
		appendString(str);
		boolean ran = pt.runTest();
		if (!ran) {
			appendString(String.format("****** TEST FAILED ******"));
		}
		appendString(pt.getResultString());
		appendString("");
		return ran;
	}
	
	private void appendString(String string) {
		if (string == null) {
			return;
		}
//		outputText.append(string+"\n");
		TextUpdate t = new TextUpdate(string);
		t.run();
//		Thread t = new Thread(new TextUpdate(string));
//		t.start();
//		while(t.isAlive()) {
//			try {
//				Thread.sleep(1);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
	}
	
	Object synchObject = new Object();
	
	class TextUpdate implements Runnable {

		private String newString;
		
		public TextUpdate(String newString) {
			super();
			this.newString = newString;
		}

		@Override
		public void run() {
			if (newString == null) {
				return;
			}
			newString += "\n";
			synchronized (outputText) {
				outputText.append(newString);
				outputText.update(outputText.getGraphics());
				outputText.setCaretPosition(outputText.getText().length());
			}
		}
		
	}
	
	private void enableButtons(boolean enable) {
		startButton.setEnabled(enable);
		closeButton.setEnabled(enable);
	}
	
	@Override
	public void cancelButtonPressed() {
		for (int i = 0; i < performanceTests.size(); i++) {
			performanceTests.get(i).cleanup();
		}

	}

	@Override
	public boolean getParams() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
