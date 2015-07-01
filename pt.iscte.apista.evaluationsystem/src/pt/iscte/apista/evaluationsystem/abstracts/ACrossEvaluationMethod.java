package pt.iscte.apista.evaluationsystem.abstracts;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pt.iscte.apista.core.Filter;
import pt.iscte.apista.core.Filter.Range;
import pt.iscte.apista.core.SystemConfiguration;
import pt.iscte.apista.evaluationsystem.EvaluationData;

public abstract class ACrossEvaluationMethod extends AEvaluator {

	/**
	 * List of the evaluation data retrieved produced from the validations
	 */
	protected List<EvaluationData> dataList = new ArrayList<>();

	/**
	 * Evaluation Method's class to be instantiated and used in the evaluation
	 */
	private Class<? extends AEvaluationMethod> method;

	public ACrossEvaluationMethod(SystemConfiguration configuration, Class<? extends AEvaluationMethod> method) {
		super(configuration);

		this.method = method;

		// Builds the filters for the cross validation, according to the number
		// of validations
		if(!configuration.usesSrilm())
			filters = Range.getCrossValidationTestFilters(configuration.getNumberOfValidations());

		// double testPercentage = 1 / (double) (numberOfValidations);
		//
		// for (int i = 0; i != numberOfValidations; i++) {
		// double min = (double) i / (double) numberOfValidations;
		//
		// double max = min + testPercentage;
		//
		// filters[i] = new Filter.Range(min, max);
		// }
	}

	@Override
	public void evaluate() {
		AEvaluationMethod evaluationMethod = null;
		if(!configuration.usesSrilm()){
			for (int i = 0; i != filters.length; i++) {

				try {
					// Instantiates the evaluation method with the configuration
					// and the corresponding filter

					evaluationMethod = method
							.getDeclaredConstructor(SystemConfiguration.class,
									Filter[].class).newInstance(configuration,
									new Filter[] { filters[i] });
					
					// Builds the model with the previous given configuration
					evaluationMethod.runModel();
					evaluationMethod.setup();
				} catch (Exception e) {   
					System.err
							.println("Error on the evaluate() method on class ACrossEvaluationMethod");
					e.printStackTrace();
				}
			}
		} else {
			for(int i = 0; i != configuration.getNumberOfValidations(); i++){
				try {
					evaluationMethod = method
							.getDeclaredConstructor(SystemConfiguration.class,
									Filter[].class).newInstance(configuration,null);
					evaluationMethod.setupWithSRILM(new File(configuration.getResourceFolder() + configuration.getModelFileName()+(i+1)));
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
		}
		evaluationMethod.evaluate();
		dataList.addAll(evaluationMethod.reportData());
	}

	protected final String getFilename() {

		return configuration.getOutputFolder() + "CrossValidation_"
				+ method.getSimpleName() + "_" + getFilenameData();

	}

	@Override
	protected final String getFilenameData() {
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
		Date date = new Date();
		return dateFormat.format(date) + "_";
	}

}
