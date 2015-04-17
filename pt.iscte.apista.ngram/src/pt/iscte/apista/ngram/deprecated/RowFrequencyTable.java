package pt.iscte.apista.ngram.deprecated;

import java.util.List;
import java.util.Map;

import pt.iscte.apista.extractor.Analyzer;
import pt.iscte.apista.extractor.Instruction;
import pt.iscte.apista.ngram.InstructionInfo;
import pt.iscte.apista.ngram.InstructionWrapper;

import com.google.common.collect.Table;

public class RowFrequencyTable implements IFrequencyTable{

	@Override
	public Table<Instruction, Instruction, InstructionInfo> buildFrequencyTable(
			Analyzer analyzer,
			Table<Instruction, Instruction, InstructionInfo> table) {
		
		// Build frequency by row

		for (Instruction row : table.rowKeySet()) {

			// Gets the total absolute frequency from each row
			
			int rowTotalFrequency = getTotalFrequency(table.row(row));

			// On all rows, for each column calculate its relative frequency
			
			for (Instruction column : table.row(row).keySet()) {
				
				int instFreq = table.get(row, column).getFrequency();
				table.get(row, column).setRelativeFrequency(
						((double) instFreq) / ((double) rowTotalFrequency));
			
			}
			
		}
		
		return table;
	}
	
	private int getTotalFrequency(Map<Instruction, InstructionInfo> map) {
		int total = 0;

		for (Instruction instruction : map.keySet()) {
			total += map.get(instruction).getFrequency();
		}

		return total;

	}

	@Override
	public List<InstructionWrapper> queryFrequencyTable(List<Instruction> context,
			int maxInstructions,
			Table<Instruction, Instruction, InstructionInfo> table) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Table<Instruction, Instruction, InstructionInfo> getTable() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double probability(Instruction instruction,
			List<Instruction> context,
			Table<Instruction, Instruction, InstructionInfo> table) {
		// TODO Auto-generated method stub
		return 0;
	}

	


}
