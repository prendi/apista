package pt.iscte.apista.evaluationsystem.abstracts;

import java.util.List;

import pt.iscte.apista.core.Filter;
import pt.iscte.apista.core.SystemConfiguration;
import pt.iscte.apista.evaluationsystem.EvaluationData;

public abstract class AEvaluator {

	/**
	 * System configuration used to configure the system in terms of paths and
	 * analyzer
	 */
	protected SystemConfiguration configuration;
	
	/**
	 * Filters to be used when training or testing the model
	 */
	protected Filter[] filters;

	/**
	 * @param configuration
	 *            System configuration used to configure the system
	 **/
	public AEvaluator(SystemConfiguration configuration) {

		this.configuration = configuration;
	}

	/**
	 * Evaluate the model with the previous setup
	 */
	public abstract void evaluate();
	
	/**
	 * Report the data produced from the evaluation
	 * 
	 * @return returns a List with the EvaluationData objects produced from the
	 *         evaluation
	 */
	public abstract List<EvaluationData> reportData();

//	/**
//	 * 
//	 * @return returns the filename to be used when creating a file to report the evaluation data
//	 */
//	protected abstract String getFilename(String methodName);
	
	/**
	 * 
	 * @return returns the file's data to be used in the file's name
	 */
	protected abstract String getFilenameData();
	
}
