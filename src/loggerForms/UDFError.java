package loggerForms;

import java.util.ArrayList;

public class UDFError {
	
	/* %d integer
	 * %f float
	 * %s string
	 * 
	 * \t tab
	 * \n new line
	 */
	
	
	
	ArrayList<String> errors=new ArrayList<String>();
	FormDescription formDescription;
	
	public UDFError(FormDescription formDescription){
		this.formDescription=formDescription;
	}
	
	public void add(String error){
		System.out.println(error);
		errors.add(error);
	}
	
	
	public void add(ItemInformation itDesc,String s){
		System.out.printf("Table %s\t order %d\t item %s\t has an error at",/*formDescription.getUdfName()*/
				itDesc.getFormDescription().getUdfName(),
				itDesc.getIntegerProperty(UDColName.Order.toString()),
				itDesc.getStringProperty(UDColName.Title.toString()),s);
	}
	
	public void printAll(){
		System.out.println("The following errors appear in "+formDescription.getUdfName());
		for (String s:errors){
			System.out.println(s);
		}
	}
	
	public void popupAll(){
		
	}

}