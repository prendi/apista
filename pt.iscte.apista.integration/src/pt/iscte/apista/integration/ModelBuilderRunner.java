package pt.iscte.apista.integration;

import pt.iscte.apista.core.ModelBuilder;
import pt.iscte.apista.core.SystemConfiguration;

public class ModelBuilderRunner {

	public static void main(String[] args) throws Exception {

		SystemConfiguration configuration = new SystemConfiguration("/Users/andresantos/git/apista/pt.iscte.apista.integration/config.properties");
		ModelBuilder.run(configuration);
	}
}
