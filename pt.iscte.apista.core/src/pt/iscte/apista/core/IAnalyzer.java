package pt.iscte.apista.core;

import java.util.List;
import java.util.Set;

/**
 * Describes the behavior of the analyzer with the essential methods needed.
 */
public interface IAnalyzer {

	/**
	 * Runs the analyzer with the given System Configuration. Should retrieve sentences of tokens from the Repository.
	 * @param configuration
	 */
	void run(SystemConfiguration configuration);
	
	/**
	 * Returns a list with the sentences within the filters' boundaries.
	 * @return List with sentences filtered according to the filters set.
	 */
	List<Sentence> getSentences();
	
	/**
	 * Set of unique words (tokens) retrieved from the sentences
	 * @return Set of Strings representing the tokens retrieved from the sentences
	 */
	Set<String> getWords();
	
	/**
	 * Set of unique Classes retrieved from the sentences
	 * @return Set of Strings representing the classes used in the tokens
	 */
	Set<String> getClasses();
	
	/**
	 * Sets the filters that will be used to determine which sentences will be returned in the getSentences() method.
	 * @param filters Array of filters to be set on the analyzer.
	 */
	void setFilters(Filter... filters);

	/**
	 * Returns the filters that are currently assigned to the analyzer
	 * @return Array of filters set on the analyzer
	 */
	Filter[] getFilters();
	
	/**
	 * List of non-filtered sentences
	 * @return List of all the sentences retrieved by the analyzer, ignoring the filters
	 */
	List<Sentence> getAllSentences();
	
	/**
	 * Randomizes the list of all sentences in order to mitigate regularities
	 * @param seed used in the randomization
	 */
	void randomizeSentences(long seed);
	
}
