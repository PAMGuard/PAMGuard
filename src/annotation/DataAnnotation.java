package annotation;

/**
 * Some kind of data annotation, i.e. data, such as a comment
 * added to a data unit. 
 * @author Doug Gillespie
 *
 */
public abstract class DataAnnotation<TDataAnnotationType extends DataAnnotationType> {

	private TDataAnnotationType dataAnnotationType;

	public DataAnnotation(TDataAnnotationType dataAnnotationType) {
		super();
		this.dataAnnotationType = dataAnnotationType;
	}

	public TDataAnnotationType getDataAnnotationType() {
		return dataAnnotationType;
	}


}
