package Localiser.algorithms.locErrors;

public class ErrorEllipseJSONData extends ErrorJSONData {

	private EllipticalError ellipticalError;

	public ErrorEllipseJSONData(EllipticalError ellipticalError) {
		this.ellipticalError = ellipticalError;
	}

	/* (non-Javadoc)
	 * @see Localiser.algorithms.locErrors.ErrorXMLData#getXMLString()
	 */
	@Override
	public String getXMLString() {
		double[] angles = ellipticalError.getAngles();
		double[] errors = ellipticalError.getEllipseDim();
		String x = "\"NAME\":\"Ellipse\"";
		if (angles != null) {
			x += "," + getJSONElement("ANGLES", angles);
		}
		if (errors != null) {
			x += "," + getJSONElement("ERRORS", errors);
		}
		x += "";
	return x;
	}

}
