package pt.iscte.apista.ngram.deprecated;

import java.util.List;

import pt.iscte.apista.extractor.Analyzer;
import pt.iscte.apista.extractor.Instruction;
import pt.iscte.apista.extractor.Sentence;
import pt.iscte.apista.extractor.Analyzer.UnigramTable;
import pt.iscte.apista.ngram.InstructionInfo;
import pt.iscte.apista.ngram.InstructionWrapper;
import pt.iscte.apista.ngram.interfaces.IInstructionTable;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class DocumentTermMatrixBuilder extends IInstructionTable {

	private Table<Instruction, Instruction, InstructionInfo> table;

	public Table<Instruction, Instruction, InstructionInfo> buildTable(
			Analyzer analyzer) {

		table = HashBasedTable.create();

		for (Sentence sentence : analyzer.getSentences()) {

			Instruction firstInstruction = sentence.getInstructions().get(0);

			// Add the first instruction to the table with
			// Instruction.START
			if (table.get(Instruction.START, firstInstruction) != null) {
				table.get(Instruction.START, firstInstruction).addFrequency();
			} else {
				table.put(Instruction.START, firstInstruction,
						new InstructionInfo(1));
			}

			if (sentence.getInstructions().size() > 1) {

				for (int i = 1; i < sentence.getInstructions().size(); i++) {
					
					// Get the current instruction to map as row
					Instruction nextInstruction = sentence.getInstructions().get(i);
					
					for(int j = Math.max(0, i-4) ; j != i ; j++){
						Instruction instruction = sentence.getInstructions().get(j);
						
						insertInstructionsIntoTable(instruction, nextInstruction);
						
					}
				}
			}
		}

		return table;
	}

	private void insertInstructionsIntoTable(Instruction instruction,
			Instruction nextInstruction) {

		// Checks if the instructions are mapped on the table
		// If they are, simply add +1 to its frequency

		if (table.get(instruction, nextInstruction) != null) {
			table.get(instruction, nextInstruction).addFrequency();
		}

		else {
			// If the instructions are not mapped on the table,
			// create a new mapping and add it to the table

			// InstructionInfo info =
			table.put(instruction, nextInstruction, new InstructionInfo(1));
			// System.out.println(info);
			// System.out.println(table.row(instruction));
			// System.out.println(table.column(nextInstruction));
			// System.out.println(table.get(instruction, nextInstruction));
		}

	}


	@Override
	public Table<Instruction, Instruction, InstructionInfo> buildTable(
			Analyzer analyzer, UnigramTable unigramTable) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void calculateFrequency() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<InstructionWrapper> queryFrequencyTable(
			List<Instruction> context, int maxInstructions) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double probability(Instruction instruction, List<Instruction> context) {
		// TODO Auto-generated method stub
		return 0;
	}
}
