package pt.iscte.apista.ant.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import pt.iscte.apista.ant.APISTATask;
import pt.iscte.apista.core.IAnalyzer;
import pt.iscte.apista.extractor.Analyzer;

/**
 *  Task used to extract the sentences from the repositories and serialize the Analyzer
 */
public class ExtractorTask extends APISTATask{

	
	@Override
	public void execute(){
		
		try {
		
			//Must be called in order to initialize the configuration object
			super.execute();
			
			//Initialize the analyzer
			IAnalyzer analyzer = new Analyzer();

			//Run the analyzer with the configuration provided. Extracts the sentences from the repository
			analyzer.run(configuration);
			
			//Randomizes the sentences in the analyzer in order to 
			analyzer.randomizeSentences(2015);
			
			//Serialize the analyzer
			FileOutputStream fos = new FileOutputStream(new File(configuration.getResourceFolder() + configuration.getAnalyzerFileName()));
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(analyzer);
			oos.close();
			
			analyzer = configuration.getAnalyzer();
			
			System.out.println(analyzer.getAllSentences().size());
			
		} catch (Exception e) {
			System.err.println("Problem executing ExtractorTask");
			e.printStackTrace();
		}
		
	}
}
