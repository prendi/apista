package pt.iscte.apista.ant;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.apache.tools.ant.Task;

import pt.iscte.apista.core.SystemConfiguration;

public abstract class APISTATask extends Task{
	
	//Properties id on ant file
	private static final String PROPERTIES_FILE_PATH_KEY = "propertiesPath";
	
	//Configuration used to execute the different tasks. Loaded from the properties file provided in the ant command
	protected SystemConfiguration configuration;
	
	/**
	 * Must be called in order to load the properties into the configuration.
	 * By default gets the properties file from the local Resources folder if no path is provided when the ant task is called.
	 * Else it uses the properties provided when the ant task is called
	 */
	@Override
	public void execute(){
		
		String propertiesPath = "";
		
		//Gets the properties file path from the property on the ant file
		if(getProject() != null)
			propertiesPath = getProject().getProperty(PROPERTIES_FILE_PATH_KEY);
		else
			propertiesPath = "../../Resources/config.properties";
			
		//Initializes a new SystemConfiguration with the properties provided
		configuration = new SystemConfiguration(propertiesPath);
		
		try {
			configuration.dumpProperties(new FileOutputStream(new File(configuration.getOutputFolder() + "config.properties")));
			configuration.dumpProperties(System.out);
		} catch (FileNotFoundException e) {
			System.err.println("There was a problem with the properties file output in the APISTATask");
			e.printStackTrace();
		}
		
	}
	
}
