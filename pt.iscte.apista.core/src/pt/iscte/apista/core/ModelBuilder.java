package pt.iscte.apista.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.osgi.framework.Configurable;
/**
 * Class used to build a sentence model.
 **/
public class ModelBuilder {
	/**
	 * Method used to create the sentence model, with the SystemConfiguration provided, 
	 * that must contain an APIModel, an implementation of an IAnalyzer and an array of Filters.
	 * Produces a file containing the model.
	 * @param configuration SystemConfiguration containing the APIModel and the implementation of IAnalyzer
	 * @param filters array of training filters that will be set on the analyzer in order to build the model
	 * @throws IOException may throw an exception if there is a problem saving the model to the file
	 */
	public static void run(SystemConfiguration configuration,Filter... filters)
					throws IOException {
		
		//APIModel described in the SystemConfiguration, that will be used to produce the sentence model.
		APIModel model = configuration.getModel();

		//Implementation of an IAnalyzer containing the List of sentences used to produce the model
		IAnalyzer analyzer = configuration.getAnalyzer();
		
		//Set the training filters on the analyzer
		analyzer.setFilters(filters);
		
		//Setup the model with the configuration parameters described in the SystemConfiguration
		model.setup(configuration.getModelParameters());
		
		//Builds the model with the analyzer
		model.build(analyzer);

		//Save the model to a file
		File output = new File(configuration.getResourceFolder()+configuration.getOutputFileName());
		model.save(output);
		
	}
	
	public static void load(SystemConfiguration configuration, File modelFile) throws IOException{
		
		APIModel model = configuration.getModel();
		
		model.load(modelFile);
		
		//Save the model to a file
		File output = new File(configuration.getResourceFolder()+configuration.getOutputFileName());
		model.save(output);
		
	}
}
