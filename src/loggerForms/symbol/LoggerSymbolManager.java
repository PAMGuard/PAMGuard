package loggerForms.symbol;

import PamView.GeneralProjector;
import PamView.PamSymbol;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.PamSymbolManager;
import PamView.symbol.StandardSymbolChooser;
import PamView.symbol.StandardSymbolManager;
import PamView.symbol.SymbolData;
import PamView.symbol.modifier.SymbolModType;
import PamguardMVC.PamDataBlock;
import loggerForms.FormDescription;
import loggerForms.FormsDataBlock;
import loggerForms.LoggerFormGraphics;
import loggerForms.PropertyDescription;
import loggerForms.PropertyTypes;
import loggerForms.controlDescriptions.ControlDescription;
import loggerForms.formdesign.FormList;
import loggerForms.formdesign.propertypanels.SymbolPanel;

public class LoggerSymbolManager extends StandardSymbolManager {

	private FormsDataBlock formsDataBlock;
	private SymbolData standardFormSymbol;
	
	public LoggerSymbolManager(FormsDataBlock pamDataBlock) {
		super(pamDataBlock, LoggerFormGraphics.defaultSymbol);
		this.formsDataBlock = pamDataBlock;
	}

	@Override
	protected LoggerSymbolChooser createSymbolChooser(String displayName, GeneralProjector projector) {
		return new LoggerSymbolChooser(this, formsDataBlock, displayName, getDefaultSymbol(), projector);
	}

	@Override
	public SymbolData getDefaultSymbol() {
		/**
		 * the default default is a generic one for logger data. See if it is overridden by a symbol 
		 * specified for the form. 
		 */
		SymbolData formSymbol = getFormSymbol();
		if (formSymbol != null) {
			return formSymbol;
		}
		else {
			return super.getDefaultSymbol();
		}
	}

	/**
	 * Get the default symbol for the form. Might be null, 
	 * though might have been set in the form properties. 
	 * @return
	 */
	public SymbolData getFormSymbol() {
		if (standardFormSymbol == null) {
			FormDescription formDescription = formsDataBlock.getFormDescription();
			PropertyDescription formProperty = formDescription.findProperty(PropertyTypes.SYMBOLTYPE);
			if (formProperty == null) {
				return null;
			}
			PamSymbol symbol = SymbolPanel.createSymbol(formProperty.getItemInformation());
			if (symbol != null) {
				standardFormSymbol = symbol.getSymbolData();
			}
		}
		return standardFormSymbol;
	}

	@Override
	public void addSymbolModifiers(PamSymbolChooser psc) {
		// probably don't want the standard ones. 
//		super.addSymbolModifiers(psc);
		/*
		 *  now add symbol modifiers for each control in the form.
		 *  This will primarily be lookups which should have defined colours. 
		 *  May be able to do other controls based on their type / null / >0 values. 
		 *  Focus on lut's for now.  
		 */
		FormDescription formDescription = formsDataBlock.getFormDescription();
		FormList<ControlDescription> ctrls = formDescription.getControlDescriptions();
		for (ControlDescription ctrlDescription : ctrls) {
			LoggerSymbolModifier modifier = createSymbolModifier(formDescription, ctrlDescription, psc);
			if (modifier != null) {
				psc.addSymbolModifier(modifier);
			}
		}
	}

	private LoggerSymbolModifier createSymbolModifier(FormDescription formDescription,
			ControlDescription ctrlDescription, PamSymbolChooser psc) {
		switch (ctrlDescription.getEType()) {
		case CHAR:
			break;
		case CHECKBOX:
			break;
		case COUNTER:
			return new IntegerSymbolModifier(formDescription, ctrlDescription, psc, SymbolModType.FILLCOLOUR | SymbolModType.LINECOLOUR);
		case DOUBLE:
			break;
		case INTEGER:
			return new IntegerSymbolModifier(formDescription, ctrlDescription, psc, SymbolModType.FILLCOLOUR | SymbolModType.LINECOLOUR);
		case LATLONG:
			break;
		case LOOKUP:
			return new LookupSymbolModifier(formDescription, ctrlDescription, psc, SymbolModType.EVERYTHING);
		case NMEACHAR:
			break;
		case NMEAFLOAT:
			break;
		case NMEAINT:
			break;
		case SHORT:
			return new IntegerSymbolModifier(formDescription, ctrlDescription, psc, SymbolModType.FILLCOLOUR | SymbolModType.LINECOLOUR);
		case SINGLE:
			break;
		case STATIC:
			break;
		case TIME:
			break;
		case TIMESTAMP:
			break;
		default:
			break;
		
		}
		return null;
	}
}
