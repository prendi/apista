package pt.iscte.apista.integration;

import pt.iscte.apista.core.ModelBuilder;
import pt.iscte.apista.core.SystemConfiguration;

public class ModelBuilderRunner {

	public static void main(String[] args) throws Exception {

		
		
//		Filter f = new Filter.Range(0, 0.7);

		SystemConfiguration configuration = new SystemConfiguration(
				"C:/Users/Gonçalo/Dropbox/Thesis/Code/pt.iscte.apista.evaluationsystem/resources/config.properties");

		ModelBuilder.run(configuration);
	}
}
