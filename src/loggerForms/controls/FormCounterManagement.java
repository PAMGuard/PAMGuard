package loggerForms.controls;

import generalDatabase.DBControlUnit;
import generalDatabase.EmptyTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.pamCursor.PamCursor;
import generalDatabase.pamCursor.PamCursorManager;

import java.nio.charset.Charset;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;



import loggerForms.OutputTableDefinition;
import loggerForms.network.LoggerNetworkManager;
import loggerForms.network.LoggerNetworkObserver;
import loggerForms.network.LoggerNetworkSystem;

/**
 * used to make sure counters are unique based on table name
 * @author Graham Weatherup
 *
 */
public class FormCounterManagement implements LoggerNetworkObserver {

	private ArrayList<FormCounter> formCounters;
	static FormCounterManagement formCounterManagement;

	private FormCounterManagement(){
		formCounters=new ArrayList<FormCounter>() ;
	}

	public static synchronized FormCounterManagement getInstance(){
		if (formCounterManagement==null){
			formCounterManagement= new FormCounterManagement();
		}
		
		LoggerNetworkSystem.getManager().addNetworkObserver(formCounterManagement);

		return formCounterManagement;
	}


	/**
	 * Get the next counter number. 
	 * @param counterControl
	 * @param tableName
	 * @return
	 */
	public synchronized int getCounterNumber(CounterControl counterControl, String tableName){		
		FormCounter counter = findFormCounter(tableName);
		if (counter == null) {
			counter = new FormCounter( counterControl,tableName);
			formCounters.add(counter);
		}
		int newCount = counter.getNewNumber();
		
		if (counterControl.controlDescription.getTopic() != null) {
			// send this counter to network listeners, i.e. so scans app can update display.
			sendNetworkCounter(counterControl.controlDescription.getTopic(), newCount);
		}

		return newCount;
	}
	
	/**
	 * find form counter based on table name
	 * @param tableName
	 * @return
	 */
	public FormCounter findFormCounter(String tableName) {	
		for (FormCounter fc:formCounters){
			if (fc.getTableName().equals(tableName)){
				return fc;
			}
		}
		return null;
	}

	/**
	 * Called when a connection is made to update counters in listeners. 
	 */
	private void sendAllNetworkCounts() {	
		for (FormCounter fc:formCounters){
			String topic = fc.counterControl.controlDescription.getTopic();
			if (topic == null) {
				continue;
			}
			sendNetworkCounter(topic, getCurrentCounterNumber(fc.counterControl, fc.tableName));
		}
		
	}
	
	private void sendNetworkCounter(String topic, int newCount) {
		LoggerNetworkManager networkManager = LoggerNetworkSystem.getManager();
		if (networkManager == null) {
			return;
		}
		String str = Integer.valueOf(newCount).toString();
		byte[] data = str.getBytes();
		networkManager.sendData("", "LoggerCounter/" + topic, data);
	}

	/**
	 * Get the current counter number
	 * @param control
	 * @param tableName
	 * @return current counter number, or null if this counter is not yet created. 
	 */
	public synchronized Integer getCurrentCounterNumber(CounterControl control, String tableName) {

		FormCounter fc = findFormCounter(tableName);
		if (fc == null) {
			fc = new FormCounter(control, tableName);
			formCounters.add(fc);
		}

		return fc.getCurrentNumber();
	}


	private class FormCounter{
		String tableName;
		int number;
		private CounterControl counterControl;


		FormCounter(CounterControl counterControl,String tableName){
			this.counterControl = counterControl;
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

		public synchronized int getCurrentNumber() {
			return number;
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


	@Override
	public void updateState(boolean connected, int nClient) {
		sendAllNetworkCounts();
	}




}
