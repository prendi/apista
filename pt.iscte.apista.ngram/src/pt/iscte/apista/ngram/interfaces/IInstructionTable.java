package pt.iscte.apista.ngram.interfaces;

import java.io.Serializable;
import java.util.List;

import pt.iscte.apista.core.IAnalyzer;
import pt.iscte.apista.core.Instruction;
import pt.iscte.apista.core.Parametrizable;
import pt.iscte.apista.ngram.InstructionInfo;
import pt.iscte.apista.ngram.InstructionWrapper;
import pt.iscte.apista.ngram.UnigramTable;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public abstract class IInstructionTable implements Parametrizable, Serializable{
	
	protected int n;
	protected Table<Instruction, Instruction, InstructionInfo> table;
	
	public IInstructionTable() {
		table = HashBasedTable.create();
	}
	
	@Override
	public void setup(Parameters params) {
		n = params.getIntValue("n");
	}
	
	public abstract Table<Instruction, Instruction, InstructionInfo> buildTable(IAnalyzer analyzer, UnigramTable unigramTable);
	
	public abstract void calculateFrequency();
	
	public abstract List<InstructionWrapper> queryFrequencyTable(List<Instruction> context,
			int maxInstructions);
	
	public abstract double probability(Instruction instruction, List<Instruction> context);
	
	public Table<Instruction, Instruction, InstructionInfo> getTable(){
		return table;
	}
	
}
