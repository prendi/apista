package pt.iscte.apista.ant.tasks;

import pt.iscte.apista.ant.APISTATask;
import pt.iscte.apista.core.ModelBuilder;

/**
 * Task used to build the model and test with an Eclipse Application
 */
public class ModelBuilderTask extends APISTATask{

	@Override
	public void execute() {
		//Must be called in order to initialize the configuration object
		super.execute();
		
		try {
			//Builds the model with the configuration and the filters defined in the properties file
			ModelBuilder.run(configuration, configuration.getFilters());
		} catch (Exception e) {
			System.err.println("Error executing ModelBuilderTask");
			e.printStackTrace();
		}
	}
}
