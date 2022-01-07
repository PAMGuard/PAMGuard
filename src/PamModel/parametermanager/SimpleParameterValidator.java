package PamModel.parametermanager;

/**
 * Simple parameter validator which return OK for all values. 
 * @author Jamie Macaulay
 *
 */
public class SimpleParameterValidator implements ParameterValidator {

	@Override
	public int isParameterOK(Number value) {
		return ParameterValidator.PARAMOK;
	}

}
