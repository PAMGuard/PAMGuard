To build an executable jar file from the PAMGuard source using Eclipse, you need to use this build configuration
'Build PAMGuard.launch'
You cannot simply export the project as a runnable jar file, or you will not get the required Maven dependencies. 
To use this with Eclipse, from your workspace, select File, then import, then in the list of things to be imported select 
Run/Debug Launch Configurations, hit Next
Then in the Import Launch Configurations panel, browse to this folder. Select the folder (not a specific file)
then in the left panel, select the folder, then in the right panel the configuration you wish to import 
(i.e. Build PAMGuard.launch) and hit Finish
Then go to 'Run Configurations' and find the launch in the 'Maven Builds' section. You'll probably have to change the 
Base directory at the top of the panel to select the right project within your workspace. 
Once that's done, you can Run the configuration. It will take a while to get all the Maven dependencies and will output 
a runnable jar file into the 'targets' folder in your workspace. 
The name and version number of the created files are taken from the POM.xml file, so edit that if you want a different name.
For unknown reasons, it makes three files. They are all the same, you can delete the ones starting with 'original-' and 
ending with '-shared'

