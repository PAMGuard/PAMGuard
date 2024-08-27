/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation 
 * of marine mammals (cetaceans).
 *  
 * Copyright (C) 2006 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package PamView;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

/**
 * Dialog for user control of PamSymbols
 * @author Douglas Gilespie
 * @see PamSymbol
 *
 */
public class PamSymbolDialog extends PamDialog implements ActionListener {

	//	private JButton okButton, cancelButton;

	private JPanel samplePanel;

	private JButton symbolButton;

	private JCheckBox fillCheckBox;

	private JColorChooser fillColorChooser, lineColorChooser;

	private ColorListener colorListener = new ColorListener();

	private JSpinner lineThickness, symbolHeight, symbolWidth;

	private PamSymbol givenSymbol, returnedSymbol;

	private JPanel fillColourPanel;

	private static JPopupMenu typeMenu;

	static private PamSymbolDialog pamSymbolDialog;

	/** 
	 * Private constructor - use Show(PamSymbol ...) to create
	 * the dialog
	 *
	 */
	public PamSymbolDialog(Window parentFrame) {
		super(parentFrame, "PAMGUARD Symbol Selection", false);
		// TODO Auto-generated constructor stub

		//		this.setTitle("Symbol Types");

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setOpaque(true);

		JPanel c = new JPanel();
		//		c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
		c.setLayout(new BorderLayout());
		//		c.setBorder(new TitledBorder("Select ..."));
		c.add(BorderLayout.CENTER, samplePanel = new SamplePanel());


		//		c.add(symbolButton = new JButton("Select Symbol"));
		symbolButton = new JButton("Select Symbol");

		JPanel l = new JPanel();
		//		l.setLayout((new BoxLayout(l, BoxLayout.X_AXIS)));
		l.setLayout(new GridBagLayout());
		//		l.setBorder(new EmptyBorder(5, 0, 6, 0));
		l.setBorder(new TitledBorder("Size"));
		GridBagConstraints con = new PamGridBagContraints();
		con.gridwidth = 2;
		con.fill = GridBagConstraints.HORIZONTAL;
		addComponent(l, symbolButton, con);
		con.gridy++;
		con.gridx = 0;
		con.gridwidth = 1;
		addComponent(l, new JLabel("Line thickness"), con);
		con.gridx++;
		addComponent(l, lineThickness = new JSpinner(), con);
		//		lineThickness.setMaximumSize(new Dimension(100, 30));
		lineThickness.addChangeListener(new ThickListener());

		con.gridy++;
		con.gridx = 0;
		con.gridwidth = 1;
		addComponent(l, new JLabel("Width"), con);
		con.gridx++;
		addComponent(l, symbolWidth = new JSpinner(), con);
		//		lineThickness.setMaximumSize(new Dimension(100, 30));
		symbolWidth.addChangeListener(new WidthListener());

		con.gridy++;
		con.gridx = 0;
		con.gridwidth = 1;
		addComponent(l, new JLabel("Height"), con);
		con.gridx++;
		addComponent(l, symbolHeight = new JSpinner(), con);
		//		lineThickness.setMaximumSize(new Dimension(100, 30));
		symbolHeight.addChangeListener(new HeightListener());


		con.gridy++;
		con.gridx = 1;
		addComponent(l, fillCheckBox = new JCheckBox("Fill"), con);


		c.add(BorderLayout.SOUTH, l);
		// c.add(colorButton = new JButton("Color"));
		mainPanel.add(BorderLayout.CENTER, c);

		symbolButton.addActionListener(this);
		// colorButton.addActionListener(this);
		fillCheckBox.addActionListener(this);

		//		JPanel s = new JPanel();
		//		s.add(okButton = new JButton("  Ok  "));
		//		getRootPane().setDefaultButton(okButton);
		//		s.add(cancelButton = new JButton("Cancel"));
		//		getContentPane().add(BorderLayout.SOUTH, s);
		//		okButton.addActionListener(this);
		//		cancelButton.addActionListener(this);
		//
		//		mainPanel.add(BorderLayout.SOUTH, s);


		JPanel e = new JPanel();
		e.setLayout(new BoxLayout(e, BoxLayout.Y_AXIS));
		JPanel f = new JPanel();
		f.setBorder(new TitledBorder("Line Colour"));
		//		e.add(new JLabel("Line Colour"));
		f.add(lineColorChooser = new JColorChooser());
		fillColourPanel = new JPanel();
		fillColourPanel.setBorder(new TitledBorder("Fill Colour"));
		//		e.add(new JLabel("Fill Color"));
		fillColourPanel.add(fillColorChooser = new JColorChooser());
		e.add(f);
		e.add(fillColourPanel);
		mainPanel.add(BorderLayout.EAST, e);

		lineColorChooser.setPreviewPanel(new JPanel());
		fillColorChooser.setPreviewPanel(new JPanel());
		lineColorChooser.getSelectionModel().addChangeListener(colorListener);
		fillColorChooser.getSelectionModel().addChangeListener(colorListener);

		//		this.setContentPane(mainPanel);
		setDialogComponent(mainPanel);

		//		pack();
		//		setLocation(300, 200);
		//
		this.setModal(true);
		//
		//		this.setResizable(false);
	}


	/**
	 * Show the dialog as a specific x,y, location
	 * @param parentFrame parent Frame for component
	 * @param pamSymbol Existing PamSymbol
	 * @param x x coordinate for dialog on screen
	 * @param y y coordinate for dialog on screen
	 * @return modified or new symbol
	 */
	static public PamSymbol show(Window parentFrame, PamSymbol pamSymbol, int x, int y) {
		if (pamSymbolDialog == null || parentFrame != pamSymbolDialog.getOwner()) {
			pamSymbolDialog = new PamSymbolDialog(parentFrame);
		}
		pamSymbolDialog.setLocation(x, y);
		return show(parentFrame, pamSymbol);
	}


	/**
	 * Show the dialog at a default (or most recent) location
	 * @param pamSymbol Existing Symbol
	 * @return modified or new symbol
	 */
	static public PamSymbol show(Window parentFrame, PamSymbol pamSymbol) {
		if (pamSymbolDialog == null || parentFrame != pamSymbolDialog.getOwner()) {
			pamSymbolDialog = new PamSymbolDialog(null);
		}
		if (pamSymbol == null) {
			pamSymbol = new PamSymbol();
		}
		pamSymbolDialog.givenSymbol = pamSymbol.clone();
		
		pamSymbolDialog.fillCheckBox.setSelected(pamSymbol.isFill());
		pamSymbolDialog.lineThickness.setValue(pamSymbol.getLineThickness());
		pamSymbolDialog.symbolHeight.setValue(pamSymbol.getHeight());
		pamSymbolDialog.symbolWidth.setValue(pamSymbol.getWidth());
		pamSymbolDialog.lineColorChooser.setColor(pamSymbol.getLineColor());
		pamSymbolDialog.fillColorChooser.setColor(pamSymbol.getFillColor());
		pamSymbolDialog.enableControls();
		pamSymbolDialog.setVisible(true);
		return pamSymbolDialog.returnedSymbol;
	}

	/*
	 *  (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == symbolButton) {
			newSymbolType(symbolButton);
		}
		else if (e.getSource() == fillCheckBox) {
			if (givenSymbol == null) {
				return;
			}
			givenSymbol.setFill(fillCheckBox.isSelected());

		} else { // hopefully it's a menu item from the symbol selector !
			if (givenSymbol == null) {
				return;
			}
			PamSymbolType symbolType;
			try {
				symbolType = PamSymbolType.valueOf(((JMenuItem) e.getSource()).getActionCommand());
				givenSymbol.setSymbol(symbolType);
				// symbolButton.setIcon(givenSymbol);
			} catch (Exception Ex) {
				Ex.printStackTrace();
			}
		}
		samplePanel.repaint();
		samplePanel.invalidate();
		enableControls();
	}

	@Override
	public void cancelButtonPressed() {
		returnedSymbol = null;
		setVisible(false);		
	}

	@Override
	public boolean getParams() {
		returnedSymbol = givenSymbol;
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

	private void enableControls() {
		if (givenSymbol == null) {
			return;
		}
//		fillColourPanel.setEnabled(givenSymbol.isSolidShape());
//		fillColorChooser.setVisible(givenSymbol.isSolidShape());
		enableWithChildren(fillColourPanel, givenSymbol.isSolidShape() && fillCheckBox.isSelected());
		fillCheckBox.setEnabled(givenSymbol.isSolidShape());
		symbolButton.setEnabled(givenSymbol.getIconStyle() != PamSymbol.ICON_STYLE_LINE);
	}
	
	private void enableWithChildren(JComponent component, boolean enable) {
		component.setEnabled(enable);
		int n = component.getComponentCount();
		for (int i = 0; i < n; i++) {
			Component c = component.getComponent(i);
			if (JComponent.class.isAssignableFrom(c.getClass())) {
				enableWithChildren((JComponent) c, enable);
			}
		}
	}

	/**
	 * Redraws the dialog display when colours change
	 * @author Doug
	 *
	 */
	class ColorListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			if (givenSymbol == null) {
				return;
			}
			givenSymbol.setFillColor(fillColorChooser.getColor());
			givenSymbol.setLineColor(lineColorChooser.getColor());
			samplePanel.repaint();
			samplePanel.invalidate();
		}
	}

	/**
	 * Redraws the dialog when line thicknesses change
	 * @author Doug
	 *
	 */
	class ThickListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			if (givenSymbol == null) {
				return;
			}
			float thick = 1;
			String s = new String(lineThickness.getValue().toString());
			try {
				thick = Float.valueOf(s);
			} catch (Exception Ex) {
				Ex.printStackTrace();
			}
			givenSymbol.setLineThickness(thick);
			samplePanel.repaint();
			samplePanel.invalidate();
		}

	}

	class WidthListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			int width = 8;
			String s = new String(symbolWidth.getValue().toString());
			try {
				width = Integer.valueOf(s);
			} catch (Exception Ex) {
				Ex.printStackTrace();
			}
			givenSymbol.setWidth(width);
			samplePanel.repaint();
			samplePanel.invalidate();

		}
	}

	class HeightListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			int height = 8;
			String s = new String(symbolHeight.getValue().toString());
			try {
				height = Integer.valueOf(s);
			} catch (Exception Ex) {
				Ex.printStackTrace();
			}
			givenSymbol.setHeight(height);
			samplePanel.repaint();
			samplePanel.invalidate();

		}
	}

	/**
	 * Draws a sample panel showing what the symbol will look like
	 * blown up and at a smaller size
	 * @author Doug
	 *
	 */
	class SamplePanel extends JPanel {
		public SamplePanel() {
			super();
			setBorder(new TitledBorder("Example"));
			// TODO Auto-generated constructor stub
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			int w = getWidth();
			int h = getHeight();
			int size = Math.min(w, h) - 10;
			// Draw it in the centre of the window and also a small one above it
			givenSymbol.draw(g, new Point(w / 2, h / 2), size, size);
			givenSymbol.draw(g, new Point(w / 2 - size / 2 + (int) givenSymbol.getWidth(), 
					h / 2 - size / 2 - (int) givenSymbol.getHeight() / 2),
					(int) givenSymbol.getWidth(), (int) givenSymbol.getHeight());
			//			PamSymbol.draw(g, new Point(w / 2, h / 2), givenSymbol.getSymbol(),
			//					size, size, givenSymbol.isFill(), givenSymbol
			//							.getLineThickness(), givenSymbol.getFillColor(),
			//					givenSymbol.getLineColor());
			//			PamSymbol.draw(g, new Point(w / 2 - size / 2
			//					+ givenSymbol.getIconWidth(), h / 2 - size / 2
			//					- givenSymbol.getIconHeight() / 2),
			//					givenSymbol.getSymbol(), givenSymbol.getIconWidth(),
			//					givenSymbol.getIconHeight(), givenSymbol.isFill(),
			//					givenSymbol.getLineThickness(), givenSymbol.getFillColor(),
			//					givenSymbol.getLineColor());
		}
	}

	/**
	 * Menu created when the type button on the dialog is pressed
	 * @param e
	 */
	private void newSymbolType(JButton e) {
		PamSymbol pamSymbol = new PamSymbol();
//		if (typeMenu == null) {
			typeMenu = new JPopupMenu();
			addTypeItem(typeMenu, PamSymbolType.SYMBOL_CIRCLE);
			addTypeItem(typeMenu, PamSymbolType.SYMBOL_SQUARE);
			addTypeItem(typeMenu, PamSymbolType.SYMBOL_POINT);
			addTypeItem(typeMenu, PamSymbolType.SYMBOL_TRIANGLEU);
			addTypeItem(typeMenu, PamSymbolType.SYMBOL_TRIANGLED);
			addTypeItem(typeMenu, PamSymbolType.SYMBOL_TRIANGLEL);
			addTypeItem(typeMenu, PamSymbolType.SYMBOL_TRIANGLER);
			addTypeItem(typeMenu, PamSymbolType.SYMBOL_DIAMOND);
			addTypeItem(typeMenu, PamSymbolType.SYMBOL_STAR);
			addTypeItem(typeMenu, PamSymbolType.SYMBOL_CROSS);
			addTypeItem(typeMenu, PamSymbolType.SYMBOL_CROSS2);
			addTypeItem(typeMenu, PamSymbolType.SYMBOL_PENTAGRAM);
			addTypeItem(typeMenu, PamSymbolType.SYMBOL_HEXAGRAM);
//		}
		// MouseInfo.getPointerInfo().
		typeMenu.show(e, 0, 0);
	}

	private void addTypeItem(JPopupMenu menu, PamSymbolType symbolType) {
		JMenuItem item;
		PamSymbol pamSymbol = new PamSymbol(symbolType, 16, 16, true, 
				givenSymbol.getFillColor(), givenSymbol.getLineColor());
//		pamSymbol.setSymbol(symbolType);
		item = new JMenuItem(pamSymbol.toString(), pamSymbol);
		item.addActionListener(this);
		item.setActionCommand(symbolType.toString());
		menu.add(item);
	}
	
}
