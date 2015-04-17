package pt.iscte.apista.evaluationsystem.abstracts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pt.iscte.apista.core.Filter;
import pt.iscte.apista.core.ModelBuilder;
import pt.iscte.apista.core.SystemConfiguration;
import pt.iscte.apista.evaluationsystem.EvaluationData;

public abstract class AEvaluationMethod extends AEvaluator{
	
	/**
	 * Data produced from the evaluation
	 */
	protected EvaluationData data;
	
	public AEvaluationMethod(SystemConfiguration configuration,Filter... filters) {
		super(configuration);
		this.filters = filters;
		data = new EvaluationData(configuration.getMaxProposals());
	}

	/**
	 * Calculates the inverse of the testing filters provided and builds the model
	 */
	public void runModel(){
		// Setup training filters
		Filter[] invertedFilters = getInvertedFilters();

		try {
			ModelBuilder.run(configuration,
					invertedFilters);
		} catch (Exception e) {
			System.err.println("Problem building the model on AEvaluationMethod");
			e.printStackTrace();
		}
	}
	
	/**
	 * Sets up the system by opening the model file and loading it into the
	 * configuration and set the filters in the analyzer
	 */
	public void setup() {
		try {

			File modelFile = new File(configuration.getResourceFolder() + configuration.getModelFileName());

			if (modelFile.exists()) {
				configuration.getModel().load(modelFile);
			}else{
				throw new FileNotFoundException("Model file was not found when setting up the evaluation system");
			}

			configuration.getAnalyzer().setFilters(filters);
		} catch (IOException e) {
			System.err.println("Problem setting up evaluation system on AEvaluationMethod");
			e.printStackTrace();
		}
	}

	/**
	 * returns the inverse of the testing filters, i.e. the training filters to build the model
	 * @return returns an array with the inverse of the testing filters
	 */
	private Filter[] getInvertedFilters() {
		List<Filter> invertedFilters = new ArrayList<Filter>();
		for (Filter filter : filters) {
			Filter[] tempInv = filter.getInverseFilters();
			for (Filter invFilt : tempInv) {
				invertedFilters.add(invFilt);
			}
		}
		return  invertedFilters.toArray(new Filter[invertedFilters.size()]);
	}
	
	protected final String getFilename(String methodName){
		return configuration.getOutputFolder()  + methodName + getFilenameData();
		
	}
	
	/**
	 * Returns the filename data which includes the present Date and Filters used on this evaluation
	 */
	@Override
	protected final String getFilenameData(){
		String filenameData = "";
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
		Date date = new Date();
		filenameData = dateFormat.format(date) + "_";
		
		for (Filter f : configuration.getAnalyzer().getFilters())
			filenameData += f.toString();
		
		return filenameData;
	}

	protected static List<EvaluationData> getListFromEvaluationData(EvaluationData data){
		ArrayList<EvaluationData> list = new ArrayList<EvaluationData>();
		list.add(data);
		return list;
	}
	
}
