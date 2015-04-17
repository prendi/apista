package pt.iscte.apista.ngram.deprecated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import pt.iscte.apista.extractor.Analyzer;
import pt.iscte.apista.extractor.Instruction;
import pt.iscte.apista.extractor.InstructionSequence;
import pt.iscte.apista.ngram.InstructionInfo;
import pt.iscte.apista.ngram.InstructionWrapper;

import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;

@SuppressWarnings("serial")
public class NgramFrequencyTable implements IFrequencyTable, Serializable {

	private Table<Instruction, Instruction, InstructionInfo> table;


	public Table<Instruction, Instruction, InstructionInfo> getTable() {
		return table;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Table<Instruction, Instruction, InstructionInfo> buildFrequencyTable(
			Analyzer analyzer,
			Table<Instruction, Instruction, InstructionInfo> table) {
		
		for (Cell cell : table.cellSet()) {
			int frequency = 0;
			
			List<Instruction> list = new ArrayList<>();
			list.add((Instruction)cell.getRowKey());
			list.addAll(((InstructionSequence) cell.getColumnKey()).getInstructionSequence());
			InstructionSequence is = new InstructionSequence(list, 0 , list.size()-2);
			
			Map<Instruction, InstructionInfo> rows = table.column(is);
			
			for (InstructionInfo value : rows.values()) {
				frequency += value.getFrequency();
			}
			
			InstructionInfo info = ((InstructionInfo) cell.getValue());
			info.setRelativeFrequency((double) info.getFrequency() / frequency);
		}
		this.table = table;
		return table;
	}

	@Override
	public List<InstructionWrapper> queryFrequencyTable(List<Instruction> context,
			int maxInstructions,
			Table<Instruction, Instruction, InstructionInfo> table) {
				
//		List<Instruction> shortContext = new ArrayList<>();
//		for (int i = context.size() - 1; i >= 0 && shortContext.size() < n - 1; i--) {
//			// context.subList(context.size()-n+1, context.size()-1);
//			shortContext.add(0, context.get(i));
//		}
//		while (shortContext.size() < n - 1) {
//			shortContext.add(0, Instruction.START);
//		}

			
		List<InstructionWrapper> returnList = new ArrayList<InstructionWrapper>();

			for (Instruction column : table.row(context.get(0)).keySet()) {

				InstructionSequence sequence = (InstructionSequence) column;
				if (sequence.startsWith(context, 1, context.size() - 1) && 
						!sequence.getLast().equals(Instruction.END) && 
						!sequence.getLast().equals(Instruction.UNK)) {
					
					double prob = table.get(context.get(0), column).getRelativeFrequency();
					
					returnList.add(new InstructionWrapper(sequence.getLast(), prob));
				}
			}

			Collections.sort(returnList);

			return returnList;
	}

	@Override
	public double probability(Instruction instruction, List<Instruction> context, Table<Instruction, Instruction, InstructionInfo> table) {
		InstructionSequence seq = new InstructionSequence(context, 1, context.size() - 1);
		seq.add(instruction);
		InstructionInfo instructionInfo = table.get(context.get(0), seq);
		
		return instructionInfo == null ? 0.0 : instructionInfo.getRelativeFrequency();
	}
}
