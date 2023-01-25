package PamModel.parametermanager.swing;

import java.awt.Window;

import PamModel.parametermanager.ManagedParameters;
import PamView.dialog.PamDialog;

public class ManagedParameterDialog<T extends ManagedParameters> extends PamDialog {
	
	private T params;
	
	private ManagedParameterPanel<T> parameterPanel;

	public ManagedParameterDialog(Window parentFrame, String title, T params) {
		super(parentFrame, title, false);
		parameterPanel = new ManagedParameterPanel<T>(params);
		setDialogComponent(parameterPanel.getPanel());
	}
	
	public T showDialog(Window parentFrame, String title, T parameters) {
//		ManagedParameterDialog dialog = new ManagedParameterDialog<>(parentFrame, title, parameters);
		setParams(parameters);
		setVisible(true);
		
		return params;
	}
	
	private void setParams(T params) {
		this.params = params;
		this.parameterPanel.setParams(params);
	}

	@Override
	public boolean getParams() {
		return parameterPanel.getParams(params);
	}

	@Override
	public void cancelButtonPressed() {
		params = null;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
