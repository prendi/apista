package pt.iscte.apista.ngram.deprecated;

import pt.iscte.apista.extractor.Instruction;
import pt.iscte.apista.ngram.InstructionInfo;

import com.google.common.collect.Table;

public interface IWeightTableMethod {

	Table<Instruction, Instruction, InstructionInfo> createWeightTable(
			Table<Instruction, Instruction, InstructionInfo> table,
			int totalOccurrences);
	
	
}
