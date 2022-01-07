package PamView;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JToolBar;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import PamController.memory.PamMemory;
import PamView.PamColors.PamColor;
import PamView.dialog.PamLabel;
import PamView.dialog.PamTextDisplay;
import PamView.panel.PamPanel;

public class PamStatusBar {

	private PamToolBar statusBar;
	
	private PamPanel leftPart, rightPart;
	
	private static PamStatusBar pamStatusBar;

	private PamTextDisplay memory;
	
	private PamStatusBar (){
		statusBar = new PamToolBar("Pamguard");
		statusBar.setFloatable(true);
//		statusBar.setAutoscrolls(true);
//		FlowLayout flow = new FlowLayout(FlowLayout.LEFT);
//		flow.setAlignment(FlowLayout.)
//		statusBar.setLayout(new FlowLayout(FlowLayout.LEFT));
		FlowLayout fl1, fl2;
		statusBar.setLayout(new BorderLayout());
		leftPart = new PamPanel();
		leftPart.setLayout(fl1 = new FlowLayout(FlowLayout.LEFT));
		rightPart = new PamPanel();
		rightPart.setLayout(fl2 = new FlowLayout(FlowLayout.LEFT));
		fl1.setVgap(0);
		fl2.setVgap(0);
		
		statusBar.setBorder(new EmptyBorder(0, 0, 0, 0));
		leftPart.setBorder(new EmptyBorder(0, 0, 0, 0));
		rightPart.setBorder(new EmptyBorder(0, 0, 0, 0));
		
		statusBar.add(leftPart, BorderLayout.CENTER);
//		statusBar.addSeparator();
		statusBar.add(rightPart, BorderLayout.EAST);
//		rightPart.add(new PamLabel("Available Memory "));
		memory = new PamTextDisplay("Available Memory ......");
		rightPart.add(memory);
		Timer t = new Timer(2000, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				updateMemory();
			}
		});
		t.start();

		PamMemory mem = new PamMemory();
		memory.setToolTipText(String.format("Max memory is %s", mem.formatMemory(mem.getMax())));

//		statusBar.setLayout(new BorderLayout());
		// the PCU's are responsible for putting these in now
		// since there may be multiple instances.
	}
	public static PamStatusBar getStatusBar() {
		if (pamStatusBar == null) {
			pamStatusBar = new PamStatusBar();
		}
		return pamStatusBar;
	}
	public JToolBar getToolBar() {
		return statusBar;
	}
	
	public void resize() {
		statusBar.invalidate();
//		statusBar.pack();
//		System.out.println("Resize status bar");
	}
	
	public void add(Component statusBarComponent) {
		leftPart.add(statusBarComponent);
	}
	
	public void remove(Component statusBarComponent) {
		leftPart.remove(statusBarComponent);
	}

	private void updateMemory() {
		PamMemory mem = new PamMemory();
		memory.setText(" Available Memory: " + mem.formatMemory(mem.getAvailable()) + " ");
//		Color col = mem.isCritical() ? Color.red : Color.GREEN;
//		memory.setForeground(col);
//		rightPart.setBackground(mem.isCritical() ? Color.red : leftPart.getBackground());
	}
	
//	public JLabel getDaqStatus() {
//		return daqStatus;
//	}
//	public JLabel getLoggingStatus() {
//		return loggingStatus;
//	}
	/*
	 * package PamView;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.Timer;

import PamController.memory.PamMemory;
import PamView.PamColors.PamColor;
import PamView.dialog.PamTextDisplay;
import PamView.panel.PamPanel;

public class PamStatusBar {

	private PamToolBar statusBar;
	
	//private JLabel daqStatus;
	
	//private JLabel loggingStatus;
	
	private static PamStatusBar pamStatusBar = null;
	
	private PamPanel leftPart, rightPart;
	
	private PamTextDisplay memory;
	
	private PamStatusBar (){
		statusBar = new PamToolBar("Pamguard");
		statusBar.setFloatable(true);
		leftPart = new PamPanel(new FlowLayout(FlowLayout.LEFT));
		rightPart = new PamPanel();
		statusBar.setLayout(new FlowLayout(FlowLayout.LEFT));
		statusBar.add(leftPart);
		statusBar.add(rightPart);
		rightPart.setLayout(new FlowLayout(FlowLayout.LEFT));
		rightPart.add(new JLabel("Available Mem "));
		memory = new PamTextDisplay(6);
		
//		Timer t = new Timer(2000, new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				updateMemory();
//			}
//
//		});
		// the PCU's are responsible for putting these in now
		// since there may be multiple instances.
	}
	
	public static PamStatusBar getStatusBar() {
		if (pamStatusBar == null) {
			pamStatusBar = new PamStatusBar();
		}
		return pamStatusBar;
	}
	
	public void add(Component component) {
		leftPart.add(component);
	}
	
	public JToolBar getToolBar() {
		return statusBar;
	}
	
	public void resize() {
		statusBar.invalidate();
	}

	public void remove(Component statusBarComponent) {
		leftPart.remove(statusBarComponent);
	}

	private void updateMemory() {
		PamMemory mem = new PamMemory();
		memory.setText(mem.formatMemory(mem.getAvailable()));
		memory.setBackground(mem.isCritical() ? Color.red : PamColors.getInstance().getColor(PamColor.BORDER));
	}
	
}

	 */
}
