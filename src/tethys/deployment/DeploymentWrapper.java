package tethys.deployment;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.validation.Schema;

//import nilus.Deployment.Data.Audio;
//import nilus.Deployment;
//import nilus.Deployment.Data;
//import nilus.Deployment.Instrument;

public class DeploymentWrapper<T extends Serializable> {

	public DeploymentWrapper(T tethysObject) {
		
	}
	
	public List<String> getFieldNames() {
		return null;
	}
	
	public List<Serializable> getComplexObjects() {
		
		return null;
	}

	public static void main(String[] args) {
		// quick play with some JAXB objects to see what they can do. 
//		Deployment deployment = new Deployment();
//
//		Class<? extends Deployment> deploymentClass = deployment.getClass();
//		Annotation[] annots = deploymentClass.getAnnotations();
//		AnnotatedType[] annotInterfaces = deploymentClass.getAnnotatedInterfaces();
//		Annotation[] declAnnots = deploymentClass.getDeclaredAnnotations();
//		
//		Instrument instrument = new Instrument();
//		instrument.setID("22");
//		instrument.setType("SoundTrap");
//		QName qName = new QName("Instrument");
//		JAXBElement<Instrument> jInst = new JAXBElement<Deployment.Instrument>(qName, Instrument.class, instrument);
//		deployment.getContent().add(jInst);
//		
//		Deployment.Data data = new Data();
//		Audio audio = new Audio();
//		audio.setProcessed("??");
//		data.setAudio(audio);
//		JAXBElement jData = new JAXBElement<Deployment.Data>(new QName("Data"), Data.class, data);
//		deployment.getContent().add(jData);
//		
//		String project = "Project Name";
//		JAXBElement<String> jProj = new JAXBElement<String>(new QName("Project"), String.class, project);
//		deployment.getContent().add(jProj);
//
//		String aaa = "Project Something else";
//		JAXBElement<String> jProj2 = new JAXBElement<String>(new QName("Region"), String.class, aaa);
//		deployment.getContent().add(jProj2);
//		
//		
//		try {
//			JAXBContext jContext = JAXBContext.newInstance(Deployment.class);
//			Marshaller mar = (Marshaller) jContext.createMarshaller();
//			mar.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, true);
//			
//			ByteArrayOutputStream bos = new ByteArrayOutputStream();
//			mar.marshal(deployment, bos);
//			String xml = new String(bos.toByteArray());
//			System.out.println(xml);
////			Schema schema = mar.getSchema(); // is null. Can't generate it's own it seems. 
//			
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
	}

}
