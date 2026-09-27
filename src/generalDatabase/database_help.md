# Database

## MySQL

MySQL can be downloaded for free from [www.mysql.com/downloads/](http://www.mysql.com/downloads/). Conveniently MySQL make a windows installer which contains both MySQL server and a client to access the data outwith PAMGuard and a nice workbench to easily manage everything. It is available from [www.mysql.com/downloads/installer/](http://www.mysql.com/downloads/installer/)

One disadvantage of using a MySQL databases is that the database consists of many files and transferring a database between machines and users is more complicated than for Access and some other database types. 

### Configuring a MySQL database

  


 

To connect to a MySQL database you will need to have a user name and password for the MySQL server you are connecting to. You will get this from whoever is administering MySQL on the machine you are working on. 

Once you have successfully connected to the MySQL server, you can either select an existing database or you can automatically create a new database by pressing the 'Create New' button and filling in the name of the new database.

You do not need to add tables to the database, PAMGuard will automatically create all of the tables it needs. 

  
[Back to database main page](database_database.html)

---

#  Database

##  OpenOffice.org (OOo) databases

**The OO Database is not currently working and should not be used**

To use a Open Document Format databases no external database software or engine is required however if you would like to view the database [LibreOffice](http://www.libreoffice.org/) and [OpenOffice](http://www.openoffice.org/) are good choices. 

PAMGuard works with OpenOffice databases (.odb). 

Like MS Access, using these Open Databases, all your data ends up in a single file which is easy to copy and move and send to other users, however while PAMGuard is open the files are extracted then rebuilt at the end. 

### Configuring an OpenOffice database

  


PAMGuard is able to create an OpenOffice database(.odb) at time of selection provided with a location. 

You do not need to add tables to the database, PAMGuard will automatically create the tables it needs. 

  
[Back to database main page](database_database.html)

---

# Database

## Overview

PAMGuard stores data from many modules in a database. The database not only stores detector output, but also detector configuration and settings so that it will effectively form a single document at the end of the cruise, detailing how PAMGuard was set up and what was detected.

### Adding a database module

From the **_File>Add Modules>Utilities_** menu select _'Database'_ to add a database to your configuration. 

### Configuring the database

PAMGuard can use either MySQL or SQLite databases. Support for Microsoft Access databases has been removed with the upgrade from Java 7 to Java 8. [See here](import_export.html) for information on how to convert old Access databases to one of the supported formats. 

The setup for each databases type is slightly different, but operation and integration into the rest of PAMGuard is the same.

To configure the database, go to the **_File>Database Selection..._** menu to open the Database Selection dialog. At the top of the dialog, select either MySQL, Microsoft Access or OpenOffice from the drop down list. Then refer to specific instructions for each database type below. 

  


[Configure an SQLite database](database_sqlite.html)

[Configure a MySQL database](database_MySQL.html)

[Configure a Microsoft Access database](database_msAccess.html)

### Choosing a database system

Which database system you use is largely a matter of personal choice.

An excellent article on the advantages and disadvantages of SQLite and MySQL is available [here](https://www.sqlite.org/whentouse.html). 

Basically, SQLite is a single file database (rather like MS Access) which can easily be backed up and copied between machines just like any other data file. MySQL is a server based database which is much harder to move between computers but can better handle multiple users accessing the same data simultaneously. So if you're in the field collecting data on a laptop, you're almost definitely better off with SQLite. On the other hand, if you have the technical know how and want a managed database system back at the lab, you might consider MySQL as a solution.

The latest PAMGuard versions contain excellent facilities for [moving data between different database systems](import_export.html). 

### Adding tables to the database

PAMGuard will automatically add the necessary tables to your database.

If a new detector module is added to PAMGuard at any point, the database module is notified and will add the additional tables required.

If a module is removed and a table is no longer needed, that table will be left in the database unaltered.

Should the definition of a table change due to the new release of a module which outputs additional information, any additional columns required will be automatically added to existing tables.

### Other alterations

You may add other tables and queries to the database without affecting PAMGuard operation. Additional tables will be ignored by PAMGuard.

You can even add additional columns to tables which are used by PAMGuard and these additional columns will also be ignored.

However, you should not change the format of any existing columns and do not create relationships between tables which may not always be satisfied or PAMGuard may be unable to write data to the database.

---

# Database

## MS Access

_**  


Please note that PAMGuard is not currently able to connect to Microsoft Access databases.   
This is caused by the [ removal of the JDBC-ODBC bridge in Java 8](http://docs.oracle.com/javase/7/docs/technotes/guides/jdbc/bridge.html).   
We recommend that you use [SQLite databases](database_sqlite.html)   
Limited connectivity to MS Access for the [import and export](import_export.html) of data is provided using the open source UCanAccess library. 

If you are still using Java 7, then the following still apply ... 

**_

To use a MS Access database you will need a licensed copy of MS Access on your computer or the 32-bit MS Access database engine available [here](http://download.microsoft.com/download/2/4/3/24375141-E08D-4803-AB0E-10F2E3A07AAA/AccessDatabaseEngine.exe) supported on XP sp3, Vista sp1 and Windows 7 

PAMGuard works with Microsoft 2000, 2003, 2007 and 2010 databases (.mdb and .accdb).

One advantage of using a MS Access databases is that all your data end up in a single file which is easy to copy and move and send to other users.

### Configuring an MS Access database

  


PAMGuard is able to create an MS access 2007 database(.accdb) at time of selection provided with a location.

You do not need to add tables to the database, PAMGuard will automatically create the tables it needs.

  
[Back to database main page](database_database.html)

---

# Database

## SQLite Databases

[SQLite](http://www.sqlite.org/) is a single file database and is an excellent replacement for Microsoft Access. Data are stored in a single file which can easily be backed up and copied between computers. 

SQLite is cross platform so should work on Mac's and Linux platforms as well as Windows. 

SQLite is open source and free to use. A number of viewers are available to enable you to view data directly within the database file. We have tested and recommend SQLiteStudio which can be downloaded [here](http://sqlitestudio.pl/). 

### Configuring an SQLite database

  


PAMGuard will automatically create the necessary .sqlite3 database file on your computer.

You do not need to add tables to the database, PAMGuard will automatically create the tables it needs.

#### Auto commit

If Auto commit is selected, the database file will be updated every time a new record is written to the database. This can have a serious impact on overall performance, so it not recommended.

If you have NOT selected auto commit, then changes will be written to the database at the following times:

  1. Every three seconds
  2. Whenever the number of uncommitted records reaches 10 
  3. Whenever PAMGuard stops
  4. Whenever PAMGuard exits
  5. Whenever tables are added to the database, or the database structure changed in any way
  6. In [Viewer Mode](../../../overview/PamMasterHelp/docs/viewerMode.html), whenever the data about to be reloaded.



You can also commit changes at any time, using the menu command **File/Database/Commit Changes**.

  
[Back to database main page](database_database.html)

---

# Database

## Importing and Exporting from other Databases

In Viewer Mode, database tables can be imported or exported from the current PAMGuard database to and from external databases. This feature can be used to copy an entire database (for example, you may need to import and old Microsoft Access database into the newer SQLite database format) or you could copy data for an subset of tables (for example, import the UDF_ logger forms definitions from another database).

Currently, in order to avoid duplicating data, it is only possible to copy entire database tables into entirely empty database tables. If you wish to transfer data into a table which already contains data, you will be prompted to delete and recreate that table prior to importing / exporting data.

To export or import data, select one of the **_File>Database>External>Export to database_** or _**Import from database_** menu items. 

You will then be presented with the database selection dialog where you chose the database you want to exchange data with. 

Once you select a database, you will see the Import / Export dialog where you can chose which tables to copy between the two databases. (There may be a short delay while PAMGuard analyses the external database).

  
  
  


If you are transferring data to a new blank database, then it's likely that most of the destination tables will show an Error. Hover the mouse over the buttons to see the error. Most likely it's simply that the tables need creating. Press the "Create" button near the top of the screen and the errors will disappear. If tables already contain data, they will be flagged as "Warning". Again, hover the mouse over each button to see what's wrong. Click the button for a menu of possible actions to fix any problems (generally dropping and recreating tables).

Select the tables you wish to export / import and press "Start". 

If you are importing data, at the end of the import process, by default, PAMGuard will load the latest configuration from the imported configuration tables. Un-check the box if you do not want this to happen.

### Indexing and Cross Referencing

Every PAMGuard database table has an indexed column named "Id". This number is automatically generated as data are written to the table and may therefore change when data are copied between databases. 

For simple data, where the original data were written by PAMGuard and nothing was deleted, then it's likely that the Id's will be the same. However, for data which have been manipulated either by someone messing with the database or by data being added / deleted in the Viewer, then it's likely that the copied data will have different Id's

To keep track of data, each copied database table will contain an additional column with the name "CopyId". This will contain the Id of the data in the table it was copied from.

For most data this is not a problem. However, Click Detector events marked in the PAMGuard Viewer populate two database tables one of which cross references to the other. With the possible change of Id's, these cross references may no longer be correct. PAMGuard will automatically correct these altered cross references.

Otherwise, you will only ever be aware of these Id changes should you have referred to an event by it's Id - e.g. if you emailed a colleague and event number to check over.

  
  
[Back to database main page](database_database.html)
