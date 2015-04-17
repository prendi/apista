package pt.iscte.apista.ngram.deprecated;

import pt.iscte.apista.extractor.Instruction;
import pt.iscte.apista.ngram.InstructionInfo;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class FrequencyBasedWeightTableMethod implements
		IWeightTableMethod {

	@Override
	public Table<Instruction, Instruction, InstructionInfo> createWeightTable(
			Table<Instruction, Instruction, InstructionInfo> table, int totalOccurrences) {
		
		Table<Instruction, Instruction, InstructionInfo> weightedTable = HashBasedTable.create();
		
		for(Instruction row : table.rowKeySet()){
			
			for(Instruction column : table.row(row).keySet()){
				
				InstructionInfo info = table.get(row, column);
				double weight = (double)info.getFrequency() / (double) totalOccurrences;
				
				weightedTable.put(row, column, new InstructionInfo(
						info.getFrequency(), info.getRelativeFrequency() * weight));
				
				
			}
			
			
		}
		return weightedTable;
	}

	
}
