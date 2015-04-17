package pt.iscte.apista.ngram.deprecated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pt.iscte.apista.extractor.Instruction;
import pt.iscte.apista.ngram.InstructionInfo;
import pt.iscte.apista.ngram.InstructionWrapper;

import com.google.common.collect.Table;

public class ColumnQueryMethod implements IFrequencyTableQueryMethod {

	private static final double WEIGHT_VALUE = 1.0;

	@Override
	public List<InstructionWrapper> query(List<Instruction> context,
			int maxInstructions,
			Table<Instruction, Instruction, InstructionInfo> table) {

		
		HashMap<Instruction, InstructionWrapper> recommendationMap = new HashMap<Instruction, InstructionWrapper>();
		List<InstructionWrapper> list = new ArrayList<InstructionWrapper>();

		if (context.size() == 0) {

			for (Instruction instruction : table.row(Instruction.START)
					.keySet()) {
				
				list.add(new InstructionWrapper(instruction, table.get(
						Instruction.START, instruction)
						.getRelativeFrequency()));

			}

		}
		
		double columnWeight = WEIGHT_VALUE / context.size();
		for (Instruction instruction : context) {

			if (table.containsRow(instruction)) {

				
				for (Instruction column : table.row(instruction).keySet()) {

					if (recommendationMap.containsKey(column)) {

						InstructionWrapper temp = recommendationMap.get(column);

						double relativeFrequency = temp.getrFrequency()
								+ table.get(instruction, column)
										.getRelativeFrequency()
								* columnWeight;
						

						
						temp.setrFrequency(relativeFrequency);

						
					} else {
						InstructionWrapper iw = new InstructionWrapper(column,
								table.get(instruction, column)
										.getRelativeFrequency()
										* columnWeight);
						recommendationMap.put(column, iw);
						if (!context.contains(iw.getInstruction()))
							list.add(iw);

					}
				}

			}

		}

		return list;
	}

}
