package pt.iscte.apista.ant.tasks;

import pt.iscte.apista.ant.APISTATask;
import pt.iscte.apista.evaluationsystem.methods.CrossValidationTrainTestEvaluation;
import pt.iscte.apista.evaluationsystem.methods.CrossValidationTokenPrecision;

/**
 * Task used to evaluate the model
 */
public class EvaluatorTask extends APISTATask{
	
	@Override
	public void execute(){
		//Must be called in order to initialize the configuration object
		super.execute();
		
		//Makes a Train-Test Cross Validation to the model, and reports the data to the Results folder
		CrossValidationTrainTestEvaluation cv = new CrossValidationTrainTestEvaluation(configuration, 10);
		cv.evaluate();
		cv.reportData();
		
		//Makes a Token Cross Validation with the model provided, and reports the data to the Results folder
		CrossValidationTokenPrecision tpcv = new CrossValidationTokenPrecision(configuration, 10);
		tpcv.evaluate();
		tpcv.reportData();
	}

}
