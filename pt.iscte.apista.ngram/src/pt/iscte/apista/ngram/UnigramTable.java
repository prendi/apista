package pt.iscte.apista.ngram;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import pt.iscte.apista.core.IAnalyzer;
import pt.iscte.apista.core.Instruction;
import pt.iscte.apista.core.Sentence;

public  class UnigramTable implements Serializable {
		
		private HashMap<Instruction, Integer> map = new HashMap<>();
		private int totalOccurrences = 0;
		private static final double COVER_PERCENTAGE_WANTED = 0.9;
		private static final int NUMBER_OF_OCCURRENCES_FOR_RARE = 3;
		
		public UnigramTable(IAnalyzer analyzer) {
			for (Sentence sentence : analyzer.getAllSentences()) {

				for (Instruction instruction : sentence.getInstructions()) {

					if (map.containsKey(instruction)) {
						int value = map.get(instruction);
						value++;
						map.put(instruction, value);
					} else {
						map.put(instruction, 1);
					}
					totalOccurrences++;
				}
			}
		}

		public int getTotal() {
			return totalOccurrences;
		}
		
		public boolean isRare(Instruction i) {
			return !map.containsKey(i) || map.get(i) < NUMBER_OF_OCCURRENCES_FOR_RARE;		
		}
		
		public double rareFrequency() {
			int rareTotal = 0;
			for(Entry<Instruction, Integer> e : map.entrySet())
				if(isRare(e.getKey()))
					rareTotal += e.getValue();
			
			return (double) rareTotal / totalOccurrences;
		}
		
		
		public void reportFrequencyTable() {
			File dataFile = new File("InstructionOccurrences.csv");
			BufferedWriter bw;
			try {
				bw = new BufferedWriter(new FileWriter(dataFile));
				bw.write("Instruction, Occurrences \n");
				for (Instruction instruction : map.keySet()) {
					bw.write(instruction.toString());
					bw.write(",");
					bw.write("" + map.get(instruction));
					bw.write("\n");
				}
				reportCoverageData(bw);
				bw.flush();
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		private void reportCoverageData(BufferedWriter bw) throws IOException{
			int occurrences = 0;
			List<Integer> list = new ArrayList<>(map.values());
			Collections.sort(list);
			
			for (int i = 0; i != list.size(); i++) {
				occurrences += list.get(i);
				if(((double)occurrences / (double)totalOccurrences) >= 1 - COVER_PERCENTAGE_WANTED ){
					bw.write("Minimum Instruction Occurrences for Cover Percentage: ");
					bw.write(",");
					bw.write(list.get(i));
					bw.write("\n");
					bw.write("Occurrences for Cover Percentage: ");
					bw.write(",");
					bw.write(totalOccurrences - occurrences);
					bw.write("\n");
					bw.write("Occurrences below cover percentage: ");
					bw.write(",");
					bw.write(occurrences);
					bw.write("\n");
					bw.write("Number of words below Cover Percentage: ");
					bw.write(",");
					bw.write(i);
					bw.flush();
					return;
				}
			}
		}
	}