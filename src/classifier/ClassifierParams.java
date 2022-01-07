package classifier;

import java.io.Serializable;

/** 
 * in an attempt to generalise the classifiers and keep them
 * separate from anything whistle specific, use this
 * abstract class to hold classifier specific parameters 
 * and cast it into something more concrete when it's used
 * by a particular class;
 *  
 * @author Doug Gillespie
 *
 */
public abstract class ClassifierParams implements Serializable, Cloneable {


	private static final long serialVersionUID = 1L;
	
	private Class classifierClass;


	public ClassifierParams(Class classifierClass) {
		super();
		this.setClassifierClass(classifierClass);
	}

	/**
	 * @param classifierClass the classifierClass to set
	 */
	public void setClassifierClass(Class classifierClass) {
		this.classifierClass = classifierClass;
	}

	/**
	 * @return the classifierClass
	 */
	public Class getClassifierClass() {
		return classifierClass;
	}

	
	@Override
	protected ClassifierParams clone()  {
		try {
			return (ClassifierParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
}
