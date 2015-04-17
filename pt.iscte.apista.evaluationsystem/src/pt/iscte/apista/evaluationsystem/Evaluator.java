package pt.iscte.apista.evaluationsystem;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import pt.iscte.apista.core.SystemConfiguration;

public class Evaluator {

	public static void main(String[] args) throws Exception {

//		outputProperties();
		runEvaluator();

	}
	
	private static void runEvaluator() {
		SystemConfiguration configuration = new SystemConfiguration();
//		configuration.showSystemConfiguration();
		
//		Analyzer.run(true);

//		Analyzer analyzer = configuration.getAnalyzer();
//
//		CrossValidation cv = new CrossValidation(configuration, 10);
//
//		cv.evaluate();
//		cv.reportData();
		
		
		
//		TokenPrecisionCrossValidation cp = new TokenPrecisionCrossValidation(
//				10, 200);
//		cp.setup(false, configuration);
//		cp.evaluate();
//		cp.reportData("TokenPrecisionCrossValidation_");

	}

	private static void outputProperties() {
		Properties properties = new Properties();
		OutputStream output;
		try {
			output = new FileOutputStream("resources/config.properties");

			properties.setProperty("modelClass",
					"org.eclipselabs.slapis.doctermmatrix.models.NGramModel");
			properties.setProperty("analyzerFile", "Analyzer");
			properties.setProperty("modelFile", "swt.docTermMatrix");
			properties.setProperty("repPath",
					"C:/Users/Gonçalo/Dropbox/Thesis/Repositories ");
			// properties.setProperty("repPath",
			// "C:/Users/Gonçalo/Dropbox/Thesis/org.eclipselabs.slapis.examples");
			// properties.setProperty("repPath",
			// "C:/Users/Gonçalo/Dropbox/Thesis/org.eclipse.jdt.ui/src");
			properties.setProperty("srcPath",
					"C:/Users/Gonçalo/Dropbox/Thesis/swtsrc/src");
			properties.setProperty("libRootPackage", "org.eclipse.swt");
			properties.setProperty("outputFileName", "swt.docTermMatrix");
			properties.setProperty("maxProposals","200");
			
			properties.store(output, null);
			output.close();
		} catch (IOException e) {
			System.err.println("Problem with properties output");
		}
	}
}
