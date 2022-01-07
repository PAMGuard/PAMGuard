package loggerForms.controls;

import generalDatabase.DBControlUnit;
import generalDatabase.EmptyTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.pamCursor.PamCursor;
import generalDatabase.pamCursor.PamCursorManager;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;



import loggerForms.OutputTableDefinition;

/**
 * used to make sure counters are unique based on table name
 * @author Graham Weatherup
 *
 */
public class FormCounterManagement {
	
	ArrayList<FormCounter> formCounters;
	static FormCounterManagement formCounterManagement;
	
	private FormCounterManagement(){
		formCounters=new ArrayList<FormCounter>() ;
	}
	
	public static synchronized FormCounterManagement getInstance(){
		if (formCounterManagement==null){
			formCounterManagement= new FormCounterManagement();
		}
		
		return formCounterManagement;
	}
	
	
	public synchronized int getCounterNumber(CounterControl counterControl,String tableName){
//		System.out.println(this.hashCode());
		
		for (FormCounter fc:formCounters){
//			System.out.printf(" tblename eq? %s %s %s \n",fc.getTableName(),tableName,(fc.getTableName()==tableName));
//			System.out.println(fc.getTableName()+"!");
//			System.out.println(tableName+"!");
//			System.out.println((fc.getTableName()==tableName));
			if (fc.getTableName().equals(tableName)){
				return fc.getNewNumber();
			}
		}
		FormCounter newFormCounter=new FormCounter( counterControl,tableName);
		formCounters.add(newFormCounter);
		return newFormCounter.getNewNumber();
		
		
	}
	
	
	private class FormCounter{
		String tableName;
		int number;
		
		
		 FormCounter(CounterControl counterControl,String tableName){
			this.tableName=tableName;
			Integer tInt=getFromDB(counterControl);
			
//			System.out.println("tInt"+tInt);
//			System.out.println("Counter hash code="+this.hashCode());
			if (tInt==null){
				number = -1;
			}else{
				number = tInt;
			}
			
			
		}
		
		public synchronized int getNewNumber(){
			number++;
			return number;
		}
		
		protected String getTableName(){
			return tableName;
		}
		
		
		
		private Integer getFromDB(CounterControl counterControl){
//System.out.println("Getting Counter from DB...");
			OutputTableDefinition out = new OutputTableDefinition(tableName);
			String counterTitle = counterControl.getControlDescription().getDbTitle();
			counterTitle = EmptyTableDefinition.deblankString(counterTitle);
			try {
//				System.out.println("design message: Slow DB access "+tableName);
				out.addTableItem(new PamTableItem(counterTitle, Types.CHAR, 5));
				
				DBControlUnit dbControl= DBControlUnit.findDatabaseControl();
				PamCursor outputCursor=PamCursorManager.createCursor(out);
				
				outputCursor.openScrollableCursor(dbControl.getConnection(), 
						true, true, "ORDER By \""+counterTitle+"\" DESC");
				outputCursor.beforeFirst();
				/*
				 * if a record exists
				 */
				if (outputCursor.next()){
				
					outputCursor.moveDataToTableDef(true);
					PamTableItem ti = out.findTableItem(counterTitle);
					if (ti == null) {
						System.out.printf("Logger form % unable to find counter item %s in table\n", out.getTableName(), counterTitle);
						return null;
					}
					String counterSt = ti.getStringValue();
//					System.out.println(counterSt);
					/*
					 * check something is found
					 */
					if (counterSt==null||counterSt.length()==0){
						return null;
					}
					
					/*
					 * remove last char until it isDigit.
					 * left as recurring as may have A-Z,AA-AZ,BA-BZ in future.
					 */
					
					
					for (int i=0;i<counterSt.length();i++){
//						System.out.println(counterSt);
						Character cha=counterSt.charAt(counterSt.length()-1);
						if (!Character.isDigit(cha)){
							counterSt=counterSt.substring(0, counterSt.length()-1);
						}
					}
					
					/*
					 * check something is left
					 */
					if (counterSt==null||counterSt.length()==0){
						return null;
					}
					
					try{
						
//						System.out.println("worked:"+Integer.parseInt(counterSt));
						return Integer.parseInt(counterSt);
						
					}catch (NumberFormatException e){
						System.out.println(ti.getStringValue()+" cannot be made into an integer by removing a suffix");
						return null;
					}
					
				}else{
//					System.out.println("No last saved data for Counter was found in "+out.getTableName());
					return null;
				}
				
				

			} catch (SQLException e) {
				System.out.println("No table "+out.getTableName()+" was found in the database");
				e.printStackTrace();
				return null;
			}
		}
		
		
	}
	
	
	
}
