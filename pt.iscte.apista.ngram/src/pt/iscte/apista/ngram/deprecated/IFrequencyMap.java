package pt.iscte.apista.ngram.deprecated;

import java.util.List;
import java.util.Map;

import pt.iscte.apista.extractor.Instruction;

public interface IFrequencyMap {

	Map<String, Map<Instruction, Double>> createFrequencyMap(
			Map<String, Map<Instruction, Integer>> instructionsMap);

	List<Instruction> queryFrequecyMap(List<Instruction> context,
			int maxInstructions,
			Map<String, Map<Instruction, Double>> frequencyMap);

	int getTotalFrequency(Map<Instruction, Integer> mapToCalculateFrequency);

	double getRelativeFrequency(int totalFrequency, int frequency);

}
