package pt.iscte.apista.ngram.deprecated;

import java.util.List;

import pt.iscte.apista.extractor.Analyzer;
import pt.iscte.apista.extractor.Instruction;
import pt.iscte.apista.ngram.InstructionInfo;
import pt.iscte.apista.ngram.InstructionWrapper;

import com.google.common.collect.Table;

public class BinaryTable implements IFrequencyTable{

	@Override
	public Table<Instruction, Instruction, InstructionInfo> buildFrequencyTable(
			Analyzer analyzer,
			Table<Instruction, Instruction, InstructionInfo> table) {
		// TODO Auto-generated method stub
		return null;
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
