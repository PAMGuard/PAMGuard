package qa.analyser;

import java.io.Serializable;

public class QAReportOptions implements Serializable, Cloneable {

	public static final long serialVersionUID = 1L;
	
	public boolean showSingleSoundResults = true;
	
	public boolean showIndividualDetectors = true;

	@Override
	public QAReportOptions clone() {
		try {
			return (QAReportOptions) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	

}
