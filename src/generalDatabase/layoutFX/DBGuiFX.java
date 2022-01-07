package generalDatabase.layoutFX;

import java.util.Optional;

import PamController.SettingsPane;
import annotation.userforms.UserFormAnnotationType;
import generalDatabase.DBControl;
import generalDatabase.DBParameters;
import generalDatabase.EmptyTableDefinition;
import generalDatabase.lookupTables.LookUpTables;
import generalDatabase.lookupTables.LookupList;
import javafx.stage.Modality;
import javafx.stage.Stage;
import loggerForms.FormsControl;
import pamViewFX.PamControlledGUIFX;
import pamViewFX.fxNodes.pamDialogFX.PamSettingsDialogFX;

/**
 * Simple FX implementation of the database GUI. 
 * @author Jamie Macaulay
 *
 */
public class DBGuiFX extends PamControlledGUIFX {

	/**
	 * Reference to the database control. 
	 */
	private DBControl dBControl;
	private generalDatabase.layoutFX.DBPaneFX dBSettingsPane;

	public DBGuiFX(DBControl dBControl){
		this.dBControl=dBControl; 
	}
	
	/**
	 * Select a database using the FX database dialog system. 
	 * @param stage - the main stage for the dialog to belong to. 
	 * @param selectTitle - the title of the dialog. 
	 * @return true if a database has been successfully selected. 
	 */
	public boolean selectDatabase(Stage stage, String selectTitle, boolean alwaysOnTop) {
		//AWT
		//DBParameters newParams = DBDialog.showDialog(this, frame, dbParameters, selectTitle);
		
		//JavaFX
		@SuppressWarnings("unchecked")
		PamSettingsDialogFX<DBParameters> dBsettingsDialog=new PamSettingsDialogFX<DBParameters>((SettingsPane<DBParameters>) getSettingsPane());
		dBsettingsDialog.setParams(dBControl.getDbParameters());
		if (alwaysOnTop) {
			dBsettingsDialog.setOnShown((value)->{
				getSettingsPane().paneInitialized();
				//fix to make sure the dialog appearsa in pre PG GUI FX insitialisation i.e. when selecting viewer database 
				//on PG start up/. 
				((Stage) dBsettingsDialog.getDialogPane().getScene().getWindow()).setAlwaysOnTop(true);;
			});
		}
				
		Optional<DBParameters> dBParams=dBsettingsDialog.showAndWait();
		
		DBParameters newParams = dBParams.get();
		if (newParams != null) {
			
			// first, check if there is a Lookup table.  If so, make sure to copy the contents over before
			// we lose the reference to the old database
			EmptyTableDefinition dummyTableDef = new EmptyTableDefinition("Lookup");
			boolean thereIsALookupTable = dBControl.getDbProcess().tableExists(dummyTableDef);
			LookupList lutList = null;
			if (thereIsALookupTable) {
				lutList = LookUpTables.getLookUpTables().getLookupList(null);
			}
			
			// Make sure we have info for the Logger (UDF) tables.  The getFormsControl call will
			// create a dummy FormsControl object if it doesn't already exist, and then load
			// any UDF tables it finds in the current database into memory.
			// This is important, because the user doesn't necessarily need a User Forms module
			// in their setup.  If they are only using the forms for annotations (say, for the
			// detection group localiser) then all they need is the tables in the database.  But
			// without a FormsControl object, the repopulateLoggerTables call below will fail.
			FormsControl formsControlTemp = UserFormAnnotationType.getFormsControl();
			formsControlTemp.readUDFTables();

			dBControl.setDBParameters(newParams.clone());
			dBControl.selectSystem(dBControl.getDbParameters().getDatabaseSystem(), true);
			dBControl.getDbProcess().checkTables();
			dBControl.getDbProcess().repopulateLoggerTables(formsControlTemp);
			if (thereIsALookupTable && lutList!=null) {
				LookUpTables.getLookUpTables().addListToDB(lutList);
			}
			dBControl.fillSettingsStore();
			return true;
		}
		return false;
	}
	
	@Override
	public SettingsPane<DBParameters> getSettingsPane(){
		if (dBSettingsPane==null){
			dBSettingsPane=new DBPaneFX(dBControl);
		}
		dBSettingsPane.setParams(dBControl.getDbParameters());
		return dBSettingsPane;
	}
	
}
