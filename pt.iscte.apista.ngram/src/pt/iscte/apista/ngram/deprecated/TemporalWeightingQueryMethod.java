package pt.iscte.apista.ngram.deprecated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pt.iscte.apista.extractor.Instruction;
import pt.iscte.apista.ngram.InstructionInfo;
import pt.iscte.apista.ngram.InstructionWrapper;

import com.google.common.collect.Table;

public class TemporalWeightingQueryMethod implements IFrequencyTableQueryMethod {

	private static final double DECAY_RATE = 0.5;
	
	
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
			
			return list;

		}
		for (int i = 0 ; i != context.size() ; i++) {
			
			Instruction instruction = context.get(i);
			double weightValue = getTemporalDecay(i, context.size());
			
			if (table.containsRow(instruction)) {
				
				for (Instruction column : table.row(instruction).keySet()) {

					if (recommendationMap.containsKey(column)) {

						InstructionWrapper temp = recommendationMap.get(column);

						double relativeFrequency = temp.getrFrequency()
								+ table.get(instruction, column)
										.getRelativeFrequency()
								* weightValue;
						
						temp.setrFrequency(relativeFrequency);

						
					} else {
						
						InstructionWrapper iw = new InstructionWrapper(column,
								table.get(instruction, column)
										.getRelativeFrequency()
										* weightValue);
						
						recommendationMap.put(column, iw);
						
						if (!context.contains(iw.getInstruction()))
							list.add(iw);

					}
				}

			}

		}

		return list;
	}
	
	private double getTemporalDecay(int index, int size){
		
		double weight = 0.0;
		
		weight = Math.exp((-DECAY_RATE) * ( index - size ));
		System.out.println("Weight: " + weight + " Index: " + index + " Size: " +size);
		return weight;
	}

}
