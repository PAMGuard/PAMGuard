package loggerForms.symbol;

import java.awt.Color;

import PamView.GeneralProjector;
import PamView.PamColors;
import PamView.PamSymbolType;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataUnit;
import loggerForms.FormDescription;
import loggerForms.controlDescriptions.ControlDescription;

/**
 * Integer symbol modifier. Basically returns the whale colour rotation. 
 * @author dg50
 *
 */
public class IntegerSymbolModifier extends LoggerSymbolModifier {

	public IntegerSymbolModifier(FormDescription formDescription, ControlDescription controlDescription,
			PamSymbolChooser symbolChooser, int modifyableBits) {
		super(formDescription, controlDescription, symbolChooser, modifyableBits);
	}

	@Override
	public SymbolData getSymbolData(GeneralProjector projector, PamDataUnit dataUnit) {
		Object data = getControlData(dataUnit);
		if (data == null) {
			return null;
		}
		int number = 0;
		if (data instanceof String) {
			try {
				number = Integer.valueOf(data.toString());
			}
			catch (NumberFormatException e) {
				return null;
			}
		}
		if (data instanceof Number) {
			try {
				number = Integer.valueOf(data.toString());
			}
			catch (Exception e) {
				return null;
			}
		}
		Color col = PamColors.getInstance().getChannelColor(number);
		return new SymbolData(PamSymbolType.SYMBOL_CIRCLE, 1, 1, true, col, col);
	}

}
