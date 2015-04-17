package pt.iscte.apista.ngram.deprecated;

import java.util.List;

import pt.iscte.apista.extractor.Analyzer;
import pt.iscte.apista.extractor.Instruction;
import pt.iscte.apista.ngram.InstructionInfo;
import pt.iscte.apista.ngram.InstructionWrapper;

import com.google.common.collect.Table;

public interface IFrequencyTable {

	Table<Instruction, Instruction, InstructionInfo> buildFrequencyTable(
			Analyzer analyzer,
			Table<Instruction, Instruction, InstructionInfo> table);

	List<InstructionWrapper> queryFrequencyTable(List<Instruction> context,
			int maxInstructions,
			Table<Instruction, Instruction, InstructionInfo> table);

	Table<Instruction, Instruction, InstructionInfo> getTable();

	double probability(Instruction instruction, List<Instruction> context, Table<Instruction, Instruction, InstructionInfo> table);
	
}
