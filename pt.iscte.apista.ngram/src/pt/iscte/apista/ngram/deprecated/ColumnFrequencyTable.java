package pt.iscte.apista.ngram.deprecated;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import pt.iscte.apista.extractor.Analyzer;
import pt.iscte.apista.extractor.Instruction;
import pt.iscte.apista.ngram.InstructionInfo;
import pt.iscte.apista.ngram.InstructionWrapper;

import com.google.common.collect.Table;

public class ColumnFrequencyTable implements IFrequencyTable {
	
	private IFrequencyTableQueryMethod queryMethod = new ColumnQueryMethod();
//	private IFrequencyTableQueryMethod queryMethod = new WeightedReducedContextColumnQueryMethod();
//	private IFrequencyTableQueryMethod queryMethod = new ReducedContextColumnQueryMethod();
//	private IFrequencyTableQueryMethod queryMethod = new TemporalWeightingQueryMethod();
	private IWeightTableMethod weightedTable = new FrequencyBasedWeightTableMethod();

	
	@Override
	public Table<Instruction, Instruction, InstructionInfo> buildFrequencyTable(
			Analyzer analyzer,
			Table<Instruction, Instruction, InstructionInfo> table) {

		// Build frequency by column

		for (Instruction column : table.columnKeySet()) {

			// Gets the total absolute frequency from each column
			
			int columnTotalFrequency = getTotalFrequency(table.column(column));

			for (Instruction row : table.column(column).keySet()) {

				int instFreq = table.get(row, column).getFrequency();
				table.get(row, column).setRelativeFrequency(
						((double) instFreq) / ((double) columnTotalFrequency));
			}
		}

		return weightedTable.createWeightTable(table, analyzer.getTotalOccurrences());
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

		List<InstructionWrapper> list = queryMethod.query(context, maxInstructions, table);
		Collections.sort(list);
		return list;

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
