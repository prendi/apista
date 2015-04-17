package pt.iscte.apista.ngram.deprecated;

import java.util.HashMap;
import java.util.Map;

import pt.iscte.apista.extractor.Analyzer;
import pt.iscte.apista.extractor.Instruction;
import pt.iscte.apista.extractor.Sentence;

public class DocumentTermMatrix3 implements IInstructionMap {

	// Does not use sentences with only one instruction
	// First instruction is always the first instruction even if it's the only one however
	// the method is done but might need some changes
	
	// TODO: mudar para Table
	
	private static final int MIN_NUMBER_OF_INSTRUCTIONS_PER_SENTENCE = 2;
	
	

	@Override
	public Map<String, Map<Instruction, Integer>> buildInstructionMap(
			Analyzer analyzer) {
		Map<String, Map<Instruction, Integer>> map = new HashMap<>();

		map.put("", new HashMap<>());

		// Add the first instruction to the map with an empty key String to mark
		// as first instruction

		Instruction firstInstruction = analyzer.getFirst().getInstructions().get(0);

//		if (firstInstruction != null) {

			map.get("").put(firstInstruction, 1);

			// Iterate over all sentences on the analyzer and build the map

			for (Sentence sentence : analyzer.getSentences()) {

				if (sentence.getInstructions().size() >= MIN_NUMBER_OF_INSTRUCTIONS_PER_SENTENCE) {

					for (int i = 0; i < sentence.getInstructions().size(); i++) {
						// Get the current instruction
						String instruction = sentence.getInstructions().get(i)
								.getWord();

						Instruction nextInstruction = null;

						if (i + 1 < sentence.getInstructions().size()) {
							// Get the next instruction
							nextInstruction = sentence.getInstructions().get(
									i + 1);
						}

						// Check if the current instruction is the last
						// instruction;
						if (nextInstruction != null) {

							// Current instruction already exists on map
							if (map.containsKey(instruction)) {

								// If current instruction exists check for a
								// recommendation for the next instruction
								if (!frequencyAdded(map.get(instruction),
										nextInstruction)) {

									// If there is no recommendation, create it
									map.get(instruction)
											.put(nextInstruction, 1);
								}

							}

							else {

								// Add current instruction to the map since it
								// doesn't
								// exist
								HashMap<Instruction, Integer> temp = new HashMap<>();
								temp.put(nextInstruction, 1);
								map.put(instruction, temp);
							}
						}
					}
				}
			}
//		}
		return map;
	}

//	private Instruction getFirstInstruction(Analyzer analyzer) {
//
//		boolean gotFirst = false;
//		Instruction firstInstruction = null;
//
//		for (int i = 0; i != analyzer.getSentences().size() && !gotFirst; i++) {
//
//			if (analyzer.getSentences().get(i).getInstructions().size() > 0) {
//				gotFirst = true;
//				firstInstruction = analyzer.getSentences().get(i)
//						.getInstructions().get(0);
//			}
//		}
//
//		if (firstInstruction == null) {
//			try {
//				throw new Exception(
//						"There are no sentences with more than one instruction");
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//
//		return firstInstruction;
//
//	}

	private boolean frequencyAdded(Map<Instruction, Integer> instructionMap,
			Instruction nextInstruction) {
		if (!instructionMap.isEmpty()) {

			if (instructionMap.containsKey(nextInstruction)) {

				Integer i = instructionMap.get(nextInstruction);
				instructionMap.replace(nextInstruction, ++i);
				return true;

			}
		}
		return false;
	}

}
