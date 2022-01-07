package PamguardMVC;

/**
 * Class for passing information around with Datablocks giving a record of
 * what has happened to the data within them throughout the whole processing chain.
 * <p>
 * Since this has been added rather late into PAMGUARD, PamProcesses will automatically
 * add this using the type from their owning PamControlledUnit and the process name. 
 * <p>
 * Processes that need to add multiple annotations, such as the spectrogram noise reduction, 
 * will have to override default annotations.  
 * <p>
 *   
 * @author Doug Gillespie
 *
 */
public class ProcessAnnotation {

	/**
	 * Annotation type - defaults to the type of the PamControlledUnit
	 */
	private String type;
	
	/**
	 * Annotation name - defaults to the name of the PamProcess.
	 */
	private String name;
	
	/**
	 * Process that created the annotation. Generally, this will be the 
	 * PamProcess, but may be a sub process. 
	 */
	private Object owner;
	
	/**
	 * PamProcess that created the annotation
	 */
	private PamProcess pamProcess;

	public ProcessAnnotation(PamProcess pamProcess, Object owner, String type,
			String name) {
		super();
		this.pamProcess = pamProcess;
		this.owner = owner;
		this.type = type;
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getOwner() {
		return owner;
	}

	public void setOwner(Object owner) {
		this.owner = owner;
	}

	public PamProcess getPamProcess() {
		return pamProcess;
	}

	public void setPamProcess(PamProcess pamProcess) {
		this.pamProcess = pamProcess;
	}
	
}
