package loggerForms;

import java.awt.Component;
import java.util.ArrayList;

import javax.swing.JOptionPane;

public class UDFErrors {
	

	ArrayList<String> errors=new ArrayList<String>();
	FormDescription formDescription;
	
	public UDFErrors(FormDescription formDescription){
		this.formDescription=formDescription;
	}
	
	public void add(String error){
		System.out.println(error);
		errors.add(error);
	}
	
	/**
	 * Clear the error list. 
	 */
	public void clear() {
		errors.clear();
	}
	
	public void add(ItemInformation itDesc,String s){
		// commented by Doug since not currently working. 
//		System.out.printf("Table %s\t order %d\t item %s\t has an error at",/*formDescription.getUdfName()*/
//				itDesc.getFormDescription().getUdfName(),itDesc.getOrder(),itDesc.getTitle(),s);
	}
	
	public void printAll(){
		System.out.println("The following errors appear in "+formDescription.getUdfName());
		for (String s:errors){
			System.out.println(s);
		}
	}
	
	public boolean popupAll(Component owner){
		
		String fullError = null;
		if (errors.size()>0){
			fullError="";
			for (String error:errors)
				fullError+=error+"\n";
			String tit = String.format("Logger table \"%s\" contains errors:", formDescription.getUdfName());
			JOptionPane.showMessageDialog(owner, fullError, 
					tit, JOptionPane.ERROR_MESSAGE);
			return true;
		}
		return false;
		
		
//		JOptionPane
//		System.out.println("The following errors appear in "+formDescription.getUdfName());
//		for (String s:errors){
//			System.out.println(s);
//		}
	}

	/**
	 * @return the errors
	 */
	public ArrayList<String> getErrors() {
		return errors;
	}

}