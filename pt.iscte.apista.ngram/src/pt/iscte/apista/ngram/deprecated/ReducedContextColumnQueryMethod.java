package pt.iscte.apista.ngram.deprecated;

import java.util.List;

import pt.iscte.apista.extractor.Instruction;
import pt.iscte.apista.ngram.InstructionInfo;
import pt.iscte.apista.ngram.InstructionWrapper;

import com.google.common.collect.Table;

public class ReducedContextColumnQueryMethod implements IFrequencyTableQueryMethod{

	private static final int NUMBER_OF_RELEVANT_INSTRUCTIONS = 4;
	
	
	@Override
	public List<InstructionWrapper> query(List<Instruction> context,
			int maxInstructions,
			Table<Instruction, Instruction, InstructionInfo> table) {
			
		if(context.size() > NUMBER_OF_RELEVANT_INSTRUCTIONS){
			List<Instruction> tempSubContext = context.subList(context.size()-1-NUMBER_OF_RELEVANT_INSTRUCTIONS, context.size()-1);
			return new ColumnQueryMethod().query(tempSubContext, maxInstructions, table);
		}
		else{
			return new ColumnQueryMethod().query(context, maxInstructions, table);
		}
	}

}
