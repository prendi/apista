package pt.iscte.apista.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import pt.iscte.apista.core.Parametrizable.Parameters;

public class SystemConfiguration {

	// Suffix output of the analyzer's test set
	public static final String ANALYZER_TEST_SUFFIX = "_TEST_";
	// Suffix output of the analyzer's training set
	public static final String ANALYZER_TRAINING_SUFFIX = "_TRAINING_";
	
	// Separator used on multiple value properties
	private static final char PROPERTIES_SEPARATOR = ',';
	// Separator used to define the filter's range
	private static final char FILTER_SEPARATOR = '-';
	
	// Model class used to build the model
	private static final String MODEL_CLASS_KEY = "modelClass";
	// Analyzer file path used to be serialized and evaluate the model (must be in SRILM format)
	private static final String SRILM_ANALYZER_FILE_KEY = "SRILMAnalyzerFile";
	// Analyzer file path used to load to the system and used to build the model if srilm is not being used, and to evaluate
	private static final String SERIALIZED_ANALYZER_FILE_KEY = "SerializedAnalyzerFile";
	// Model file path used when built when evaluating
	private static final String MODEL_FILE_KEY = "modelFile";
	// Repository path
	private static final String REP_PATH_KEY = "repPath";
	// API Source path
	private static final String SRC_PATH_KEY = "srcPath";
	// API root package
	private static final String LIB_ROOT_PACKAGE_KEY = "libRootPackage";
	// Model's output file name
	private static final String OUTPUT_FILE_NAME_KEY = "outputFileName";
	// Max proposals
	private static final String MAX_PROPOSALS_KEY = "maxProposals";
	// Properties file path
	private static final String PROPERTIES_FILE_PATH = "config.properties";
	// Model's parameters used to configure it
	private static final String PARAMETERS_KEY = "params";
	// Model's filters used to build a new model 
	private static final String FILTERS_KEY = "filters";
	// Folder to output all the data (Model, Analyzer and results)
	private static final String OUTPUT_FOLDER_KEY = "outputFolder";
	// Folder to input all the data (Model, Analyzer)
	private static final String RESOURCE_INPUT_FOLDER_KEY = "resourceFolder";
	// Folder to copy the projects that contain the API
	private static final String TARGET_FOLDER_KEY = "targetPath";
	// Determines if SRILM is used to produce the language models
	private static final String SRILM_USAGE_KEY = "useSRILM";
	// Number of validations if evaluation is executed
	private static final String NUMBER_OF_VALIDATIONS_KEY = "numberOfValidations";

	private String targetPath;
	private String outputFileName;
	private String modelFilename;
	private String serializedAnalyzerFilename;
	private String srilmAnalyzerFilename;
	private String repPath;
	private Filter[] filters;
	private String libRootPackage;
	private String outputFolderName;
	private String resourcesFolderName = "";
	private Properties properties;
	private APIModel model;
	private Parameters modelParameters;
	private Class<? extends APIModel> modelClass;
	private int maxProposals;
	private boolean srilm;
	private int numberOfValidations;
	private IAnalyzer analyzer;

	private String[] apiSrcPath;

	public SystemConfiguration() {
		this(PROPERTIES_FILE_PATH);
	}

	public SystemConfiguration(String propertiesPath) {
		try {
			loadFromInputStream(new FileInputStream(propertiesPath));
		} catch (FileNotFoundException e) {
			System.err.println("File not found: " + propertiesPath);
		}
	}

	public SystemConfiguration(InputStream stream) {
		loadFromInputStream(stream);
	}

	private void loadFromInputStream(InputStream stream) {
		properties = new Properties();
		try {
			properties.load(stream);

			modelFilename = properties.getProperty(MODEL_FILE_KEY);
			outputFileName = properties.getProperty(OUTPUT_FILE_NAME_KEY);
			serializedAnalyzerFilename = properties.getProperty(SERIALIZED_ANALYZER_FILE_KEY);
			srilmAnalyzerFilename = properties.getProperty(SRILM_ANALYZER_FILE_KEY);
			targetPath = properties.getProperty(TARGET_FOLDER_KEY);
			outputFolderName = properties.getProperty(OUTPUT_FOLDER_KEY);
			resourcesFolderName = properties.getProperty(RESOURCE_INPUT_FOLDER_KEY);
			repPath = properties.getProperty(REP_PATH_KEY);
			libRootPackage = properties.getProperty(LIB_ROOT_PACKAGE_KEY);
			
			srilm = Boolean.parseBoolean(properties.getProperty(SRILM_USAGE_KEY));
			
			numberOfValidations = Integer.parseInt(properties.getProperty(NUMBER_OF_VALIDATIONS_KEY));

			maxProposals = Integer.parseInt(properties.getProperty(MAX_PROPOSALS_KEY));

			String[] rawParameters = splitProperties(properties.getProperty(PARAMETERS_KEY));
			
			String[] rawFilters = splitProperties(properties.getProperty(FILTERS_KEY));
			
			modelParameters = convertRawParameters(rawParameters);
			
			filters = convertRawFilters(rawFilters);
			
			apiSrcPath = getSourcePathsFromRootFolder(properties.getProperty(SRC_PATH_KEY), libRootPackage);

		} catch (IOException e) {
			System.err.println("Problem loading the system properties on PropertiesHolder");
			e.printStackTrace();
		}
	}

	private Filter[] convertRawFilters(String[] rawFilters) {
		Filter[] filters = new Filter.Range[rawFilters.length];
		
		for(int i = 0 ; i != rawFilters.length ; i ++){
			String[] splittedFilter = rawFilters[i].split("" + FILTER_SEPARATOR);
			filters[i] = new Filter.Range(Double.parseDouble(splittedFilter[0]), Double.parseDouble(splittedFilter[1]));
		}
		
		return filters;
	}

	private static String[] getSourcePathsFromRootFolder(String rootFolder, String rootPackage) {
		ArrayList<String> list = new ArrayList<>();
		getFoldersContainingRootPackage(rootFolder, rootPackage.split("\\.")[0], list);

		if (list.size() == 0)
			throw new IllegalArgumentException("The root package " + rootPackage + " was not found in this directory");

		return list.toArray(new String[list.size()]);
	}

	private static boolean getFoldersContainingRootPackage(String rootFolder, String splittedRootPackage,
			List<String> list) {

		if (rootFolder.endsWith(splittedRootPackage))
			return true;

		File rootFolderFile = new File(rootFolder);

		if (rootFolderFile.isDirectory() && !rootFolderFile.getName().startsWith(".")) {
			for (File f : rootFolderFile.listFiles()) {
				if (getFoldersContainingRootPackage(f.getAbsolutePath(), splittedRootPackage, list)) {
					list.add(rootFolderFile.getAbsolutePath());
					return false;
				}
			}
		}
		return false;

	}

	public void dumpProperties(OutputStream os) {
		try {
			properties.store(os, null);
		} catch (IOException e) {
			System.err.println("Unable to dump properties");
			e.printStackTrace();
		}
	}

	/**
	 * Converts the parameters string into an array of Param
	 * 
	 * @param rawParameters
	 *            array of strings in format name=value;
	 * @return returns an array of Params
	 */
	private Parameters convertRawParameters(String[] rawParameters) {
		Parameters parameters = new Parameters();
		for (int i = 0; i < rawParameters.length; i++) {
			String[] param = rawParameters[i].split("=");
			parameters.addParameter(param[0], param[1]);
		}
		return parameters;
	}
	
	public String getSrilmAnalyzerFilename() {
		return srilmAnalyzerFilename;
	}
	
	public int getNumberOfValidations() {
		return numberOfValidations;
	}
	
	public boolean usesSrilm() {
		return srilm;
	}
	
	public void setAnalyzer(IAnalyzer analyzer) {
		this.analyzer = analyzer;
	}
	
	public Filter[] getFilters() {
		return filters;
	}
	
	private String[] splitProperties(String s) {
		return s.split("" + PROPERTIES_SEPARATOR);
	}

	public String getTargetPath() {
		return targetPath;
	}
	
	public String getOutputFileName() {
		return outputFileName;
	}

	public String getModelFileName() {
		return modelFilename;
	}

	public String getSerializedAnalyzerFileName() {
		return serializedAnalyzerFilename;
	}

	public String getRepPath() {
		return repPath;
	}

	public String[] getApiSrcPath() {
		return apiSrcPath;
	}

	public String getLibRootPackage() {
		return libRootPackage;
	}

	public Properties getProperties() {
		return properties;
	}

	public Parameters getModelParameters() {
		return modelParameters;
	}

	public int getMaxProposals() {
		return maxProposals;
	}

	public APIModel getModel() {

		if (model == null) {
			try {

				model = (APIModel) getModelClass().newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				System.err.println("Error getting model from SystemConfiguration");
				e.printStackTrace();
			}
		}
		return model;
	}

	public Class<? extends APIModel> getModelClass() {
		if (modelClass == null) {
			try {
				modelClass = Class.forName(properties.getProperty(MODEL_CLASS_KEY)).asSubclass(APIModel.class);
			} catch (ClassNotFoundException e) {
				System.err.println("Error getting model class from SystemConfiguration");
				e.printStackTrace();
			}
		}
		return modelClass;
	}

	public IAnalyzer getAnalyzer() {
		return getAnalyzer(serializedAnalyzerFilename);
	}
	
	public IAnalyzer getAnalyzer(String filename){
		if(analyzer == null)
			analyzer = loadSerializedAnalyzerFromFile(filename);
		return analyzer;
	}

	public String getOutputFolder() {
		File f = new File(outputFolderName);
		if (!f.exists())
			f.mkdir();
		return outputFolderName + File.separator;
	}

	public String getResourceFolder() throws FileNotFoundException {
		
		if (resourcesFolderName != "") {
			File f = new File(resourcesFolderName);
			if (!f.exists())
				throw new FileNotFoundException("The input folder specified was not found");
		}
		
		return resourcesFolderName + File.separator;
	}

	private IAnalyzer loadSerializedAnalyzerFromFile(String filename) {

		FileInputStream fis;
		IAnalyzer analyzer = null;
		try {
			System.out.println(getResourceFolder() + filename);
			fis = new FileInputStream(new File(filename));
			ObjectInputStream ois = new ObjectInputStream(fis);

			analyzer = (IAnalyzer) ois.readObject();
			ois.close();
		} catch (IOException | ClassNotFoundException e) {
			System.err.println("The analyzer could not be loaded");
			e.printStackTrace();
		}
		return analyzer;
	}
	
}
