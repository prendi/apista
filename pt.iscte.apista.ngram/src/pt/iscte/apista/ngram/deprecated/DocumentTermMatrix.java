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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.iscte.apista.extractor.APIModel;
import pt.iscte.apista.extractor.Analyzer;
import pt.iscte.apista.extractor.Instruction;
import pt.iscte.apista.extractor.Sentence;

@SuppressWarnings("serial")
public class DocumentTermMatrix implements APIModel, Serializable{

	private transient Map<String, List<InstructionWrapperWithFrequency>> map = new HashMap<>();
	private Map<String, List<InstructionWrapperWithRelativeFrequency>> frequencyMap = new HashMap<>();

	private class InstructionWrapperWithFrequency{

		private Instruction instruction;
		private int frequency;

		public InstructionWrapperWithFrequency(Instruction instruction,
				int frequency){
			this.instruction = instruction;
			this.frequency = frequency;
		}

		public Instruction getInstruction() {
			return instruction;
		}

		public int getFrequency() {
			return frequency;
		}

		public void addFrequency() {
			frequency++;
		}

	}

	private class InstructionWrapperWithRelativeFrequency implements Serializable{

		private Instruction instruction;
		private double rFrequency;

		public InstructionWrapperWithRelativeFrequency(Instruction instruction,
				double rFrequency) {
			this.instruction = instruction;
			this.rFrequency = rFrequency;
		}

		@SuppressWarnings("unused")
		public Instruction getInstruction() {
			return instruction;
		}

		@SuppressWarnings("unused")
		public double getrFrequency() {
			return rFrequency;
		}

	}

	@Override
	public void build(Analyzer analyzer) {

		ArrayList<InstructionWrapperWithFrequency> list = new ArrayList<>();

		// Add the first instruction to the map with an empty key String to mark
		// as first instruction

		Instruction firstInstruction = analyzer.getSentences().get(0)
				.getLastInstruction();

		InstructionWrapperWithFrequency iw = new InstructionWrapperWithFrequency(
				firstInstruction, 1);
		list.add(iw);

		map.put("", list);

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
							InstructionWrapperWithFrequency recomendation = new InstructionWrapperWithFrequency(
									nextInstruction, 1);
							map.get(instruction).add(recomendation);

						}

					}

					else {

						// Add current instruction to the map since it doesn't
						// exist
						InstructionWrapperWithFrequency inst = new InstructionWrapperWithFrequency(
								nextInstruction, 1);
						ArrayList<InstructionWrapperWithFrequency> temp = new ArrayList<>();
						temp.add(inst);
						map.put(instruction, temp);

					}
				}
			}
		}

		createFrequencyMatrix();

	}

	private void createFrequencyMatrix() {

		for (String keyInstruction : map.keySet()) {

			List<InstructionWrapperWithFrequency> instructionList = map
					.get(keyInstruction);
			int totalFrequency = getTotalFrequency(instructionList);
			List<InstructionWrapperWithRelativeFrequency> frequencyList = new ArrayList<>();
			frequencyMap.put(keyInstruction, frequencyList);

			for (InstructionWrapperWithFrequency inst : instructionList) {
				frequencyList.add(new InstructionWrapperWithRelativeFrequency(
						inst.getInstruction(), getRelativeFrequency(
								totalFrequency, inst.getFrequency())));
			}

			System.out.println("Instruction: " + keyInstruction
					+ " TotalFrequency: " + totalFrequency
					+ " Number of Recomendations: " + frequencyList.size());

		}
		
		System.out.println("Total key instructions: " + map.keySet().size());
	}

	private double getRelativeFrequency(int totalFrequency, int frequency) {
		return frequency / totalFrequency;
	}

	private int getTotalFrequency(List<InstructionWrapperWithFrequency> list) {
		int result = 0;
		for (InstructionWrapperWithFrequency inst : list) {
			result += inst.getFrequency();
		}
		return result;
	}

	private boolean frequencyAdded(List<InstructionWrapperWithFrequency> list,
			Instruction nextInstruction) {
		if (!list.isEmpty()) {

			for (InstructionWrapperWithFrequency inst : list) {
				if (inst.toString().equals(nextInstruction.toString())) {
					inst.addFrequency();
					return true;
				}
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
			frequencyMap = (Map<String, List<InstructionWrapperWithRelativeFrequency>>) ois
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

		return null;
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
