package generalDatabase.sqlite;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.swing.JOptionPane;

import PamUtils.PamFileFilter;
import generalDatabase.layoutFX.SystemDialogPaneFX;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;

public class SqlitePaneFX implements SystemDialogPaneFX {
	
	/**
	 * ComboBox showing list of database files. 
	 */
	private ComboBox<File> dbList;
	
	/**
	 * Reference to the current SqliteSystem
	 */
	private SqliteSystem sqliteSystem;

	private PamBorderPane mainPane;
	

	public SqlitePaneFX(SqliteSystem sqliteSystem){
		this.sqliteSystem=sqliteSystem;
		mainPane= new PamBorderPane();
		mainPane.setCenter(createSQLBPane());
	}
	
	private Pane createSQLBPane(){
		PamVBox vBox=new PamVBox();
		vBox.setSpacing(5.);
		
		Label serverLabel = new Label("SQLite Database");
//		serverLabel.setFont(PamGuiManagerFX.titleFontSize2);
		PamGuiManagerFX.titleFont2style(serverLabel);

		vBox.getChildren().add(serverLabel);
		
		dbList = new ComboBox<File>();
		dbList.setMaxWidth(Double.MAX_VALUE);
		dbList.setMinWidth(200);
		
		Button addButton=new Button(); 
//		addButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialDesignIcon.PLUS, PamGuiManagerFX.iconSize));
		addButton.setGraphic(PamGlyphDude.createPamIcon("mdi2p-plus", PamGuiManagerFX.iconSize));
		addButton.setOnAction((action)->{
			browseForSQLDB(2);
		});
		
		Button browseButton=new Button(); 
//		browseButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialDesignIcon.FILE, PamGuiManagerFX.iconSize));
		browseButton.setGraphic(PamGlyphDude.createPamIcon("mdi2f-file", PamGuiManagerFX.iconSize));
		browseButton.setOnAction((action)->{
			browseForSQLDB(0);
		});
		
		dbList.setMaxWidth(Double.MAX_VALUE);
		browseButton.prefHeightProperty().bind(dbList.heightProperty());
		addButton.prefHeightProperty().bind(dbList.heightProperty());
		
		PamHBox browseHolder=new PamHBox();
		browseHolder.setSpacing(5);
		browseHolder.getChildren().addAll(dbList,addButton, browseButton);
		
		vBox.getChildren().add(browseHolder); 
		return vBox; 	
	
	}
	
	/**
	 * Browse for a new SQLite database.
	 * @param type - 0 to open a file, 2 to create a new database.
	 */
	private void browseForSQLDB(int type) {			
			String newDB = sqliteSystem.browseDatabasesFX(type);
			if (newDB != null) {

				// see if this file exists in the list and if it does, remove it
				for (int i = 0; i < sqliteSystem.getRecentDatabases().size(); i++) {
					if (sqliteSystem.getRecentDatabases().get(i).toString().equalsIgnoreCase(newDB)) {
						sqliteSystem.getRecentDatabases().remove(i);
					}
				}
				// then insert the file at the top of the list.
				File newFile = new File(newDB);
				// if the file doesn't exit, consider creating it.
				if (newFile.exists() == false) {
					newFile = createNewDatabase(newDB);
					if (newFile == null) {
						return;
					}

				}

				sqliteSystem.getRecentDatabases().add(0, newFile);
				setParams();

			}
		}

	public boolean getParams() {
		// selected item may not be first in the list - so re-order the 
		// list to make sure that it is.
		int ind = dbList.getSelectionModel().getSelectedIndex();
		if (ind >= 0) {
			File selFile = dbList.getSelectionModel().getSelectedItem();
			if (selFile.exists() == false) return false;
			sqliteSystem.getRecentDatabases().remove(selFile);
			sqliteSystem.getRecentDatabases().add(0, selFile);
			return true;
		}
		else {
			return false;
		}
	}
	
	public void setParams() {
		dbList.getItems().removeAll(dbList.getItems());
		for (int i = 0; i < sqliteSystem.getRecentDatabases().size(); i++) {
			dbList.getItems().add(sqliteSystem.getRecentDatabases().get(i));
		}
		dbList.getSelectionModel().select(0);
	}
	
	
	public File createNewDatabase(String newDB) {

		File newFile = new File(newDB);
		newFile = PamFileFilter.checkFileEnd(newFile, ".sqlite3", true);

		int ans = JOptionPane.showConfirmDialog(null, "Create blank database " + newFile.getAbsolutePath() + " ?", "Sqlite", JOptionPane.OK_CANCEL_OPTION);
		if (ans == JOptionPane.CANCEL_OPTION) {
			return null;
		}
		Connection connection = null;

		try {
			// create a database connection;
			// Sqlite will automatically create file if it does not exist; 
			connection = DriverManager.getConnection("jdbc:sqlite:" + newFile);

		}
		catch(SQLException e)
	    {
	      System.err.println(e.getMessage());
	    }
	    finally
	    {
	      try
	      {
	        if(connection != null)
	          connection.close();
	      }
	      catch(SQLException e)
	      {
	        // connection close failed.
	        System.err.println(e);
	      }
	    }
		return newFile;
	}


	@Override
	public Pane getPane() {
		return mainPane;
	}

}
