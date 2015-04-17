package pt.iscte.apista.ngram.deprecated;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.iscte.apista.extractor.Instruction;
import pt.iscte.apista.ngram.InstructionWrapper;

public class VerticalMap implements IFrequencyMap {
	
	//TODO: frequencia relativa na linha
	// 		frequencia relativa na coluna
	//		total de ocorrências no corpus
	// 		penalizar frequencias de acordo com as ocorrências no corpus
	
	@Override
	public Map<String, Map<Instruction, Double>> createFrequencyMap(
			Map<String, Map<Instruction, Integer>> instructionsMap) {

		Map<String, Map<Instruction, Double>> frequencyMap = new HashMap<>();

		for (String keyInstruction : instructionsMap.keySet()) {

			Map<Instruction, Integer> map = instructionsMap.get(keyInstruction);

			int totalFrequency = getTotalFrequency(map);

			Map<Instruction, Double> mapWithRelativeFrequency = new HashMap<>();
			frequencyMap.put(keyInstruction, mapWithRelativeFrequency);

			for (Instruction inst : map.keySet()) {
				mapWithRelativeFrequency.put(inst,
						getRelativeFrequency(totalFrequency, map.get(inst)));

			}

			System.out.println("Instruction: " + keyInstruction
					+ " TotalFrequency: " + totalFrequency
					+ " Number of Recomendations: "
					+ mapWithRelativeFrequency.size());

		}
		System.out.println("Total key instructions: "
				+ instructionsMap.keySet().size());

		return frequencyMap;
	}

	@Override
	public List<Instruction> queryFrequecyMap(List<Instruction> context,
			int maxInstructions,
			Map<String, Map<Instruction, Double>> frequencyMap) {

		List<InstructionWrapper> list = new ArrayList<>();

		if (context.size() == 0) {
			for (Instruction instruction : frequencyMap.get("").keySet()) {
				list.add(new InstructionWrapper(
						instruction, frequencyMap.get("").get(instruction)));
			}
			Collections.sort(list);
			return convertFromWrapperToList(list);
		}

		for (Instruction instruction : context) {
			if (frequencyMap.containsKey(instruction.getWord())) {
				for (Instruction keyInstruction : frequencyMap.get(
						instruction.getWord()).keySet()) {
					list.add(new InstructionWrapper(
							keyInstruction, frequencyMap.get(instruction.getWord()).get(
									keyInstruction)));
//					System.out.println(keyInstruction + " - " + frequencyMap.get(instruction).get(keyInstruction));
					
				}
			}
		}
		Collections.sort(list);

		return convertFromWrapperToList(list);

	}

	private List<Instruction> convertFromWrapperToList(
			List<InstructionWrapper> list) {
		List<Instruction> returnList = new ArrayList<>();

		for (InstructionWrapper instruction : list) {
			returnList.add(instruction.getInstruction());
		}
		return returnList;

	}

	@Override
	public double getRelativeFrequency(int totalFrequency, int frequency) {
		return (double)frequency / (double)totalFrequency;
	}

	@Override
	public int getTotalFrequency(
			Map<Instruction, Integer> mapToCalculateFrequency) {
		int result = 0;
		for (Integer i : mapToCalculateFrequency.values()) {
			result += i;
		}
		return result;
	}

}
