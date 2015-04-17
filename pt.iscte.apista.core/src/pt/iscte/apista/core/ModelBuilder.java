package pt.iscte.apista.core;

import java.io.File;

public class ModelBuilder {


	public static void run(SystemConfiguration configuration,Filter... filters)
					throws Exception {
		
		APIModel model = configuration.getModel();

		IAnalyzer analyzer = configuration.getAnalyzer();
		
		analyzer.setFilters(filters);
		
		model.setup(configuration.getModelParameters());
		
		model.build(analyzer);

		File output = new File(configuration.getResourceFolder()+configuration.getOutputFileName());
		model.save(output);

	}

}
