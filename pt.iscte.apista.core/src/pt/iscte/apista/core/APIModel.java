package pt.iscte.apista.core;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface APIModel extends Parametrizable{

	/**
	 * Build the model using the given analyzer
	 */
	void build(IAnalyzer analyzer);

	/**
	 * Save the model to the file
	 */
	void save(File file) throws IOException;

	/**
	 * Loads the model from the file
	 */
	void load(File file) throws IOException;

	/**
	 * Return a list of instruction proposals (with max size), given the list of previous instructions (context)
	 */
	List<Instruction> query(List<Instruction> context, int max);

	/**
	 * 
	 * @param instruction provided to calculate the probability in the given context
	 * @param context used to calculate the probability of the instruction
	 * @return returns the probability of the instruction in a given context
	 */
	double probability(Instruction instruction, List<Instruction> context);
}
