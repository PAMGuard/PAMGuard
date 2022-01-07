package annotation.userforms;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import PamView.GeneralProjector;
import PamView.PamSymbol;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.SymbolData;
import PamView.symbol.modifier.SymbolModType;
import PamView.symbol.modifier.SymbolModifier;
import PamView.symbol.modifier.SymbolModifierParams;
import PamguardMVC.PamDataUnit;
import annotation.DataAnnotation;
import generalDatabase.lookupTables.LookupItem;
import loggerForms.FormDescription;
import loggerForms.controlDescriptions.CdLookup;
import loggerForms.controlDescriptions.ControlDescription;
import loggerForms.controlDescriptions.ControlTypes;
import loggerForms.controlDescriptions.InputControlDescription;

/**
 * User form symbol modifier for use in user form annotations. This will need modification
 * before it can be used in a logger form directly. 
 * @author Dougl
 *
 */
public class UserFormSymbolModifier extends SymbolModifier {

	private FormDescription formDescription;
	
	private UserFormSymbolOptions userFormSymbolOptions = new UserFormSymbolOptions();

	public UserFormSymbolModifier(FormDescription formDescription, PamSymbolChooser symbolChooser) {
		super(formDescription.getFormName(), symbolChooser, SymbolModType.EVERYTHING);
		this.formDescription = formDescription;
		setToolTipText("Use symbols from the selected LOOKUP control in the form");
	}

	@Override
	public SymbolData getSymbolData(GeneralProjector projector, PamDataUnit dataUnit) {
		
		DataAnnotation annotation = dataUnit.findDataAnnotation(UserFormAnnotation.class);		
		if (annotation instanceof UserFormAnnotation == false) {
			return null;
		}
		UserFormAnnotation formAnnotation = (UserFormAnnotation) annotation;
		UserFormAnnotationType annotType = (UserFormAnnotationType) formAnnotation.getDataAnnotationType();
		FormDescription formsDesc = annotType.findFormDescription();
		if (formsDesc == null) {
			return null;
		}
		ArrayList<InputControlDescription> ctrls = formsDesc.getInputControlDescriptions();
		Object[] data = formAnnotation.getLoggerFormData();
		if (data == null || ctrls == null) {
			return null;
		}
		String ctrlTitle = userFormSymbolOptions.controlTitle;
		SymbolData userSymbol = null;
		try {
			int n = Math.min(data.length, ctrls.size()); 
			for (int i = 0; i < n; i++) {
				ControlDescription cd = ctrls.get(i);
				if (ctrlTitle != null && ctrlTitle.equals(cd.getTitle()) == false) {
					continue;
				}
				Object dat = data[i];
				if (dat == null) {
					continue;
				}
				if (cd.getEType() == ControlTypes.LOOKUP) {
					CdLookup luList = (CdLookup) cd;
					String code = dat.toString().trim();
					LookupItem luItem = luList.getLookupList().findSpeciesCode(code);
					if (luItem == null) {
						continue;
					}
					PamSymbol symbol = luItem.getSymbol();
					if (symbol != null) {
						userSymbol = symbol.getSymbolData();
						return userSymbol;
					}
				}
			}
		}
		catch (Exception e) {

		}

		return null;
	}

	@Override
	public JMenuItem getModifierOptionsMenu() {
		JMenu menu = new JMenu("Select control");
		ArrayList<InputControlDescription> ctrls = formDescription.getInputControlDescriptions();
		int n = 0;
		for (InputControlDescription cd : ctrls) {
			if (cd.getEType() == ControlTypes.LOOKUP) {
				JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(cd.getTitle());
				menuItem.setSelected(cd.getTitle().equals(userFormSymbolOptions.controlTitle));
//				menuItem.setText(cd.getHint());
				menuItem.setToolTipText("Lookup " + cd.getTopic());
				menuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						selectControl(cd);
					}
				});
				menu.add(menuItem);
				n++;
			}
		}
		if (n == 0) {
			return null;
		}
		return menu;
	}

	protected void selectControl(InputControlDescription cd) {
		if (cd != null) {
			userFormSymbolOptions.controlTitle = cd.getTitle(); 
		}
		
	}

	@Override
	public SymbolModifierParams getSymbolModifierParams() {
		if (userFormSymbolOptions == null) {
			userFormSymbolOptions = new UserFormSymbolOptions();
		}
		return userFormSymbolOptions;
	}

	@Override
	public void setSymbolModifierParams(SymbolModifierParams symbolModifierParams) {
		if (symbolModifierParams instanceof UserFormSymbolOptions) {
			this.userFormSymbolOptions = (UserFormSymbolOptions) symbolModifierParams;
		}
		super.setSymbolModifierParams(symbolModifierParams);
	}



}
