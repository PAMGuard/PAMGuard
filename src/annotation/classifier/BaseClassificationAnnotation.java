package annotation.classifier;

import annotation.DataAnnotation;

public class BaseClassificationAnnotation extends DataAnnotation<BaseClassificationAnnotationType> {
	
	private double score;
	private String label;
	private String method;

	public BaseClassificationAnnotation(BaseClassificationAnnotationType dataAnnotationType, double score, String label, String method) {
		super(dataAnnotationType);
		this.score = score;
		this.setLabel(label);
		this.setMethod(method);
	}

	/**
	 * @return the score
	 */
	public double getScore() {
		return score;
	}

	/**
	 * @param score the score to set
	 */
	public void setScore(double score) {
		this.score = score;
	}

	/**
	 * @return the label assigned to the call
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label of the classification
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the method
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * @param method the method to set
	 */
	public void setMethod(String method) {
		this.method = method;
	}

	@Override
	public String toString() {
		return String.format("Classfication %s %s Score %5.3f", getMethod(), getLabel(), getScore());
	}

}
