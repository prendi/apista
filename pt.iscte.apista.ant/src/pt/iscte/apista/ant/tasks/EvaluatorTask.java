package pt.iscte.apista.ant.tasks;

import java.io.File;
import java.io.FileNotFoundException;

import pt.iscte.apista.ant.APISTATask;
import pt.iscte.apista.core.IAnalyzer;
import pt.iscte.apista.evaluationsystem.methods.CrossValidationTrainTestEvaluation;
import pt.iscte.apista.evaluationsystem.methods.CrossValidationTokenPrecision;
import pt.iscte.apista.extractor.Analyzer;

/**
 * Task used to evaluate the model
 */
public class EvaluatorTask extends APISTATask{
	
	@Override
	public void execute(){
//		//Must be called in order to initialize the configuration object
		super.execute();
		/*
		try {
			IAnalyzer analyzer = new Analyzer();
			analyzer.loadAndSerializeAnalyzer(new File(configuration.getResourceFolder() + configuration.getSerializedAnalyzerFileName()), 
					new File(configuration.getResourceFolder() + configuration.getSerializedAnalyzerFileName()));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		//Makes a Train-Test Cross Validation to the model, and reports the data to the Results folder
		CrossValidationTrainTestEvaluation cv = new CrossValidationTrainTestEvaluation(configuration);
		cv.evaluate();
		cv.reportData();
//		
//		//Makes a Token Cross Validation with the model provided, and reports the data to the Results folder
//		CrossValidationTokenPrecision tpcv = new CrossValidationTokenPrecision(configuration, 10);
//		tpcv.evaluate();
//		tpcv.reportData();
		
	}
	
	public static void main(String[] args) {
		new EvaluatorTask().execute();
	}

}
