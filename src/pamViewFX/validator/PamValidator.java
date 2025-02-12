package pamViewFX.validator;

import java.util.List;
import java.util.stream.Collectors;

import net.synedra.validatorfx.ValidationMessage;
import net.synedra.validatorfx.Validator;

/**
 * Extension of the validator class to add PAMGuard specific functions. 
 * @author Jamie Macaulay
 *
 */
public class PamValidator extends Validator {

	public PamValidator() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * Create one string from a list of error messages. 
	 * @param messages - the list of error messages
	 * @return
	 */
	public static String list2String(List<ValidationMessage> messages) {
		String string = "";
		
		//grab only unique messages
		List<ValidationMessage> uniqueMessages = messages.stream()
		         .distinct()
		         .collect(Collectors.toList());

		for (ValidationMessage msg:uniqueMessages) {
			System.out.println(msg);
			string += msg.getText() + "\n";
		}
		
		return string;
	}

}
