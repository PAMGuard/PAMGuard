package tethys.swing;

import java.awt.Window;
import java.lang.reflect.Field;
import java.util.ArrayList;

import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import PamView.dialog.PamDialog;
import PamView.dialog.warn.WarnOnce;
import PamView.wizard.PamWizard;
import PamView.wizard.PamWizardCard;
import tethys.niluswraps.NilusChecker;

/**
 * Wizard card with a few extra functions for Tethys. 
 * @author dg50
 *
 * @param <T>
 */
abstract public class TethysWizardCard<T extends Object> extends PamWizardCard<T> {

	private static final long serialVersionUID = 1L;

	public TethysWizardCard(PamWizard pamWizard, String title) {
		super(pamWizard, title);
	}
	
	/**
	 * Get a string out of a text component, but if the field is
	 * empty, return null rather than an empty, 0 length, string. 
	 * @param textField
	 * @return string, or null if it's empty. 
	 */
	public String getText(JTextComponent textField) {
		String str = textField.getText();
		if (str == null) {
			return null;
		}
		if (str.length() == 0) {
			return null;
		}
		return str;
	}
	
	/**
	 * Get a string from a text component, and warn if it is empty or null. 
	 * @param owner parent window for warning
	 * @param textComponent text component
	 * @param name control name for warning text
	 * @return String if there was one, or null if it was null or empty.
	 */
	public String warnNotNull(Window owner, JTextComponent textComponent, String name) {
		String str = getText(textComponent);
		if (str != null) {
			return str;
		}
		String warn = String.format("The field %s cannot be empty", name);
		PamDialog.showWarning(owner, "Empty or missing data", warn);
		return null;
	}
	
	/**
	 * Check for required empty fields. 
	 * @param nilusObject
	 * @return true if all OK, false if there are required fields
	 */
	public boolean checkEmptyFields(Object nilusObject) {
		NilusChecker.removeEmptyFields(nilusObject);
		ArrayList<Field> emptyList = NilusChecker.checkEmptyFields(nilusObject);
		if (emptyList.size() == 0) {
			return true;
		}
		Field first = emptyList.get(0);
		String msg = String.format("The field \"%s\" in \"%s\" is required by Tethys and must be completed", first.getName(), first.getDeclaringClass().getCanonicalName());
		return PamDialog.showWarning(getPamWizard(), "Missing required data", msg);
	}

}
