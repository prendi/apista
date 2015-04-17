package pt.iscte.apista.ngram.deprecated;

import java.util.List;

import pt.iscte.apista.extractor.Instruction;
import pt.iscte.apista.ngram.InstructionInfo;
import pt.iscte.apista.ngram.InstructionWrapper;

import com.google.common.collect.Table;

public interface IFrequencyTableQueryMethod {

	List<InstructionWrapper> query(List<Instruction> context, int maxInstructions,
			Table<Instruction, Instruction, InstructionInfo> table);

}
