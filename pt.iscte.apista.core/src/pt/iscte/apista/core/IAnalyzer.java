package pt.iscte.apista.core;

import java.util.List;
import java.util.Set;


public interface IAnalyzer {

	void run(SystemConfiguration configuration);
	
	List<Sentence> getSentences();
	
	Set<String> getWords();
	
	Set<String> getClasses();
	
	void setFilters(Filter... filters);

	Filter[] getFilters();
	
	List<Sentence> getAllSentences();
	 
	void randomizeSentences(long seed);
	
}
