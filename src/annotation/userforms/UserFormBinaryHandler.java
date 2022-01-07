package annotation.userforms;

import PamguardMVC.PamDataUnit;
import annotation.DataAnnotation;
import annotation.DataAnnotationType;
import annotation.binary.AnnotationBinaryData;
import annotation.binary.AnnotationBinaryHandler;
import binaryFileStorage.BinaryStore;
import loggerForms.FormDescription;

public class UserFormBinaryHandler extends AnnotationBinaryHandler {
	
	private UserFormAnnotationType userFormType;
	
	private static final short CURRENTVERSION = 1;

	public UserFormBinaryHandler(UserFormAnnotationType dataAnnotationType) {
		super(dataAnnotationType);
		userFormType = dataAnnotationType;
	}

	@Override
	public AnnotationBinaryData getAnnotationBinaryData(PamDataUnit pamDataUnit, DataAnnotation annotation) {
		UserFormAnnotation userFormAnnotation = (UserFormAnnotation) annotation;
		FormDescription formDescription = userFormType.getFormDescription();
		if (formDescription == null) {
			return null;
		}
		String jsonString = formDescription.getJSONData(userFormAnnotation.getLoggerFormData());
		if (jsonString == null) {
			return null;
		}
		AnnotationBinaryData abd = new AnnotationBinaryData(CURRENTVERSION, userFormType, 
				userFormType.getShortIdCode(), jsonString.getBytes());
		return abd;
	}

	@Override
	public DataAnnotation setAnnotationBinaryData(PamDataUnit pamDataUnit, AnnotationBinaryData annotationBinaryData) {
		FormDescription formDescription = userFormType.getFormDescription();
		if (formDescription == null) {
			return null;
		}
		String jsonString = new String(annotationBinaryData.data);
		Object[] data = formDescription.fromJSONString(jsonString); 
		UserFormAnnotation ann = new UserFormAnnotation(userFormType, data);
		return ann;
	}

}
