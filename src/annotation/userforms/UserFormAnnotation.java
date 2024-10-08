package annotation.userforms;

import annotation.DataAnnotation;
import annotation.DataAnnotationType;

public class UserFormAnnotation<T extends DataAnnotationType<?>> extends DataAnnotation<T> {

	/**
	 * Data extracted from the logger form. 
	 */
	private Object[] loggerFormData;
	
	public UserFormAnnotation(T dataAnnotationType, Object[] loggerFormData) {
		super(dataAnnotationType);
		this.setLoggerFormData(loggerFormData);
	}

	/**
	 * @return the loggerFormData
	 */
	public Object[] getLoggerFormData() {
		return loggerFormData;
	}

	/**
	 * @param loggerFormData the loggerFormData to set
	 */
	public void setLoggerFormData(Object[] loggerFormData) {
		this.loggerFormData = loggerFormData;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		int nFields = (loggerFormData == null ? 0 : loggerFormData.length);
		if (nFields == 0) {
			return "No Data";
		}
//		return String.format("Logger form data with %d fields", nFields);
		String str = "";
		for (int i = 0; i < nFields; i++) {
			if (i >=1) {
				str+= "; ";
			}
			str += loggerFormData[i];
		}
		return str;
	}

}
