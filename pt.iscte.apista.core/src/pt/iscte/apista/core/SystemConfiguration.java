package pt.iscte.apista.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.EnumMap;
import java.util.Properties;

import pt.iscte.apista.core.Parametrizable.Parameters;

public class SystemConfiguration {

	// Separator used on multiple value properties
	private static final char PROPERTIES_SEPARATOR = ',';
	// Model class used to build the model
	private static final String MODEL_CLASS_KEY = "modelClass";
	// Analyzer file path used when built when evaluating
	private static final String ANALYZER_FILE_KEY = "analyzerFile";
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
	// Folder to output all the data (Model, Analyzer and results)
	private static final String OUTPUT_FOLDER_KEY = "outputFolder";
	//Folder to input all the data (Model, Analyzer)
	private static final String RESOURCE_INPUT_FOLDER_KEY = "resourceFolder";

	
	private String outputFileName;
	private String modelFilename;
	private String analyzerFilename;
	private String repPath;
	private String srcPath;
	private String libRootPackage;
	private String outputFolderName;
	private String resourcesFolderName = "";
	private Properties properties;
	private APIModel model;
	private Parameters modelParameters;
	private Class<? extends APIModel> modelClass;
	private int maxProposals;
	private IAnalyzer analyzer;

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
			analyzerFilename = properties.getProperty(ANALYZER_FILE_KEY);

			outputFolderName = properties.getProperty(OUTPUT_FOLDER_KEY);
			resourcesFolderName = properties.getProperty(RESOURCE_INPUT_FOLDER_KEY);

			repPath = properties.getProperty(REP_PATH_KEY);
			srcPath = properties.getProperty(SRC_PATH_KEY);
			libRootPackage = properties.getProperty(LIB_ROOT_PACKAGE_KEY);

			maxProposals = Integer.parseInt(properties
					.getProperty(MAX_PROPOSALS_KEY));

			String[] rawParameters = splitProperties(properties
					.getProperty(PARAMETERS_KEY));

			modelParameters = convertRawParameters(rawParameters);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	public void dumpProperties(OutputStream os){
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

	private String[] splitProperties(String s) {
		return s.split("" + PROPERTIES_SEPARATOR);
	}

	public String getOutputFileName() {
		return outputFileName;
	}

	public String getModelFileName() {
		return modelFilename;
	}

	public String getAnalyzerFileName() {
		return analyzerFilename;
	}

	public String getRepPath() {
		return repPath;
	}

	public String getSrcPath() {
		return srcPath;
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

		if(model == null){
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
		if(modelClass == null ){
			try {
				modelClass =  Class.forName(properties.getProperty(MODEL_CLASS_KEY)).asSubclass(APIModel.class);
			} catch (ClassNotFoundException e) {
				System.err.println("Error getting model class from SystemConfiguration");
				e.printStackTrace();
			}
		}
		return modelClass;
	}

	public IAnalyzer getAnalyzer() {
		if(analyzer == null)
			analyzer= loadAnalyzerFromFile();
		return analyzer;
	}

	/**
	 * Displays the system configuration provided in the console
	 * 
	 * @param configuration
	 *            to be displayed
	 */
	public void showSystemConfiguration() {
		System.out.println("-----CONFIGURATION-----");
		System.out.println("Repository path: " + getRepPath());
		System.out.println("Source path: " + getSrcPath());
		System.out.println("Library root package: " + getLibRootPackage());
		System.out.println("Output file name: " + getOutputFileName());
		System.out.println("Model file path: " + getModelFileName());
		System.out.println("Max Proposals: " + getMaxProposals());
		System.out.println("Parameters: " + getModelParameters());
	}



	public String getOutputFolder() {
		File f = new File(outputFolderName);
		if(!f.exists())
			f.mkdir();
		return outputFolderName + File.separator;
	}


	public String getResourceFolder() throws FileNotFoundException {
		if(resourcesFolderName != ""){
			File f = new File(resourcesFolderName);
			if(!f.exists())
				throw new FileNotFoundException("The input folder specified was not found");
		}
		return resourcesFolderName + File.separator;
	}

	public IAnalyzer loadAnalyzerFromFile() {

		FileInputStream fis;
		IAnalyzer analyzer = null;
		try {
			System.out.println(getResourceFolder() + analyzerFilename);
			fis = new FileInputStream(new File(getResourceFolder() + analyzerFilename));
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
