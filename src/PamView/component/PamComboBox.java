package PamView.component;

import java.awt.Color;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

import PamView.ColorManaged;
import PamView.PamColors.PamColor;

public class PamComboBox<T> extends JComboBox<T> implements ColorManaged {

	public PamComboBox() {
		// TODO Auto-generated constructor stub
	}

	public PamComboBox(ComboBoxModel aModel) {
		super(aModel);
		// TODO Auto-generated constructor stub
	}

	public PamComboBox(T[] items) {
		super(items);
		// TODO Auto-generated constructor stub
	}

	public PamComboBox(Vector<T> items) {
		super(items);
		// TODO Auto-generated constructor stub
	}

	@Override
	public PamColor getColorId() {		// TODO Auto-generated method stub
		return PamColor.EDITCTRL;
	}

	@Override
	public void setBackground(Color bg) {
		super.setBackground(bg);
	}

}
