package pt.iscte.apista.ngram.deprecated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pt.iscte.apista.extractor.Instruction;
import pt.iscte.apista.ngram.InstructionInfo;
import pt.iscte.apista.ngram.InstructionWrapper;

import com.google.common.collect.Table;

public class WeightedReducedContextColumnQueryMethod implements IFrequencyTableQueryMethod{

	private static final int NUMBER_OF_RELEVANT_INSTRUCTIONS = 4;
	private static final double MAX_RELEVANCE = 0.4;
	private static final double DECREASE_RATE = 0.1;
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
		double relevance = MAX_RELEVANCE;
		
		if(context.size() > 4 ){
			context = context.subList(context.size()-1-NUMBER_OF_RELEVANT_INSTRUCTIONS, context.size()-1);
		}
		
		for(Instruction instruction : context){
			
			if(table.containsRow(instruction)){
				
				for(Instruction column : table.row(instruction).keySet()){
					
					if (recommendationMap.containsKey(column)) {

						InstructionWrapper temp = recommendationMap.get(column);

						double relativeFrequency = temp.getrFrequency()
								+ table.get(instruction, column)
										.getRelativeFrequency()
								* columnWeight * relevance;
						

						
						temp.setrFrequency(relativeFrequency);

						
					} else {
						InstructionWrapper iw = new InstructionWrapper(column,
								table.get(instruction, column)
										.getRelativeFrequency()
										* columnWeight * relevance);
						recommendationMap.put(column, iw);
						if (!context.contains(iw.getInstruction()))
							list.add(iw);

					}
					relevance -= DECREASE_RATE;
					
				}
			}
			
		}
		
		
		return list;
	}

}
