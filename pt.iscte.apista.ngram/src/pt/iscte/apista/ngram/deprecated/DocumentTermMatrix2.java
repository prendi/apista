package pt.iscte.apista.ngram.deprecated;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.iscte.apista.extractor.APIModel;
import pt.iscte.apista.extractor.Analyzer;
import pt.iscte.apista.extractor.Instruction;
import pt.iscte.apista.extractor.Sentence;

@SuppressWarnings("serial")
public class DocumentTermMatrix2 implements APIModel, Serializable {

	private Map<String, Map<Instruction, Integer>> map = new HashMap<>();
	private Map<String, Map<Instruction, Double>> frequencyMap = new HashMap<>();

	@Override
	public void build(Analyzer analyzer) {

		map.put("", new HashMap<>());

		// Add the first instruction to the map with an empty key String to mark
		// as first instruction

		Instruction firstInstruction = analyzer.getSentences().get(0)
				.getLastInstruction();
		
		System.out.println(analyzer.getSentences().get(0));
		System.out.println(analyzer.getSentences().get(1));
		System.out.println(analyzer.getSentences().get(2));
		System.out.println(analyzer.getSentences().get(3));
		System.out.println(analyzer.getSentences().get(4));
		System.out.println(analyzer.getSentences().get(5));
		System.out.println(analyzer.getSentences().get(6));
		

		
		map.get("").put(firstInstruction, 1);

		// Iterate over all sentences on the analyzer and build the map

		for (int i = 0 ; i != analyzer.getSentences().size() ; i ++) {

			Sentence sentence = analyzer.getSentences().get(i);
			
			if (sentence.getInstructions().size() > 0) {

				// Get the current instruction
				String instruction = sentence.getLastInstruction().toString();

				// Get the next instruction
				Instruction nextInstruction = analyzer.getSentences().get(i+1)
						.getLastInstruction();

				// Check if the current instruction is the last instruction;
				if (nextInstruction != null) {

					// Current instruction already exists on map
					if (map.containsKey(instruction)) {

						// If current instruction exists check for a
						// recommendation for the next instruction
						if (!frequencyAdded(map.get(instruction),
								nextInstruction)) {

							// If there is no recommendation, create it
							map.get(instruction).put(nextInstruction, 1);
						}

					}

					else {

						// Add current instruction to the map since it doesn't
						// exist

						HashMap<Instruction, Integer> temp = new HashMap<>();
						temp.put(nextInstruction, 1);
						map.put(instruction, temp);
					}
				}
			}
		}

		createFrequencyMatrix();

	}

	private void createFrequencyMatrix() {

		for (String keyInstruction : map.keySet()) {

			Map<Instruction, Integer> instructionMap = map.get(keyInstruction);
			int totalFrequency = getTotalFrequency(instructionMap);

			Map<Instruction, Double> mapWithRelativeFrequency = new HashMap<>();
			frequencyMap.put(keyInstruction, mapWithRelativeFrequency);

			for (Instruction inst : instructionMap.keySet()) {
				mapWithRelativeFrequency.put(
						inst,
						getRelativeFrequency(totalFrequency,
								instructionMap.get(inst)));

			}

			System.out.println("Instruction: " + keyInstruction
					+ " TotalFrequency: " + totalFrequency
					+ " Number of Recomendations: " + mapWithRelativeFrequency.size());

		}
		System.out.println("Total key instructions: " + map.keySet().size());
		
	}

	private double getRelativeFrequency(int totalFrequency, int frequency) {
		return frequency / totalFrequency;
	}

	private int getTotalFrequency(
			Map<Instruction, Integer> mapToCalculateFrequency) {
		int result = 0;
		for (Integer i : mapToCalculateFrequency.values()) {
			result += i;
		}
		return result;
	}

	private boolean frequencyAdded(Map<Instruction, Integer> instructionMap,
			Instruction nextInstruction) {
		if (!instructionMap.isEmpty()) {

			if (instructionMap.containsKey(nextInstruction)) {

				Integer i = instructionMap.get(nextInstruction);
				i++;
				instructionMap.replace(nextInstruction, i);
				return true;

			}
		}
		return false;
	}

	@Override
	public void save(File file) throws IOException {

		OutputStream output = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(output);
		oos.writeObject(frequencyMap);
		oos.close();

	}

	@SuppressWarnings("unchecked")
	@Override
	public void load(File file) throws IOException {
		InputStream input = new FileInputStream(file);
		ObjectInputStream ois = new ObjectInputStream(input);
		try {
			frequencyMap = (Map<String, Map<Instruction, Double>>) ois
					.readObject();
		} catch (ClassNotFoundException e) {
			System.out
					.println("Error loading file on DocumentTermMatrix class");
		}finally{
			ois.close();
			
		}

	}

	@Override
	public List<Instruction> query(List<Instruction> context, int max) {

		return Collections.emptyList();
	}

	@Override
	public double probability(Instruction instruction, List<Instruction> context) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public void setup(Parameters params) {
		// TODO Auto-generated method stub
		
	}

}
