package tethys.deployment;

/**
 * options for separating deployments. 
 * @author dg50
 *
 */
public enum SepDeployment {
	
	ALWAYSSINGLE, AUTOSCHEDULE, ALWAYSSEPARATE;

	@Override
	public String toString() {
		switch (this) {
		case ALWAYSSEPARATE:
			return "Export separate deployment documents for each ad-hoc recording period";
		case ALWAYSSINGLE:
			return "Export a single deployment document for all data";
		case AUTOSCHEDULE:
			return "Automatically determine duty cycle and decide automatically";
		default:
			break;
		
		}
		return super.toString();
	}
	
	public String getTip() {
		switch (this) {
		case ALWAYSSEPARATE:
			return "<html>Separate every recording period into a different document.<br>This might create a lot of documents on some surveys but<br>"
					+ "will give the most accurate record of deployment effort.</html>";
		case ALWAYSSINGLE:
			return "<html>Create only a single deployment document, with an assessment of duty cycle. <br>This is best for data collected on a regular duty cycle "
					+ "but is less ideal for<br>more ad-hoc starts and stops such as on a vessel survey.</html>";
		case AUTOSCHEDULE:
			return "<html>PAMGuard will decide if there is a regular recording schedule, in which <br>case it will export a single document"
					+ "along with duty cycle information.<br>Otherwise it will export multiple documents, one for each recording period.";
		default:
			break;
		
		}
		return toString();
	}
	
}
