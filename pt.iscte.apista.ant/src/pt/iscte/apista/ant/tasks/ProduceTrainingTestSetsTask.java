package pt.iscte.apista.ant.tasks;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import pt.iscte.apista.ant.APISTATask;
import pt.iscte.apista.core.Filter;
import pt.iscte.apista.core.IAnalyzer;
import pt.iscte.apista.core.Instruction;
import pt.iscte.apista.core.SystemConfiguration;
import pt.iscte.apista.core.Filter.Range;
import pt.iscte.apista.core.Sentence;
import pt.iscte.apista.extractor.Analyzer;

public class ProduceTrainingTestSetsTask extends APISTATask{

	@Override
	public void execute() {
		//Must be executed in order to initialize the configuration object
		super.execute();
		
		try {
			String path = configuration.getResourceFolder() + configuration.getSrilmAnalyzerFilename();
			IAnalyzer analyzer = new Analyzer();
			analyzer.loadSentences(new File(path));
			analyzer.randomizeSentences(2015);
			Filter[] filters = Range.getCrossValidationTestFilters(5);

			for (int i = 0; i != filters.length ; i++) {
				writeAnalyzerPair(analyzer, filters[i],i+1);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void writeAnalyzerPair(IAnalyzer analyzer, Filter filter,int f){
		BufferedOutputStream bos = null;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(new File(
					configuration.getResourceFolder() + configuration.getSrilmAnalyzerFilename() + 
					SystemConfiguration.ANALYZER_TEST_SUFFIX + f)));
			analyzer.setFilters(filter);
			for (Sentence sentence : analyzer.getSentences()) {
				if(sentence.getInstructions().size() > 1){
					for(Instruction instruction : sentence.getInstructions()){
						bos.write(instruction.getWord().getBytes());
						bos.write(" ".getBytes());
					}
					bos.write("\n".getBytes());
				}
			}
			bos.close();
		} catch (IOException e) {
			System.err.println("Problem writing test analyzer when producing pairs");
			e.printStackTrace();
		}
		try{
			Filter[] filters = filter.getInverseFilters();
			bos = new BufferedOutputStream(new FileOutputStream(new File(
					configuration.getResourceFolder() + configuration.getSrilmAnalyzerFilename() + 
					SystemConfiguration.ANALYZER_TRAINING_SUFFIX +f)));
			analyzer.setFilters(filters);
			for (Sentence sentence : analyzer.getSentences()) {
				if(sentence.getInstructions().size() > 1){
					for(Instruction instruction : sentence.getInstructions()){
						bos.write(instruction.getWord().getBytes());
						bos.write(" ".getBytes());
					}
					bos.write("\n".getBytes());
				}
			}
			bos.close();
		}catch (IOException e) {
			System.err.println("Problem writing training analyzer when producing pairs");
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		new ProduceTrainingTestSetsTask().execute();
	}
	
}
