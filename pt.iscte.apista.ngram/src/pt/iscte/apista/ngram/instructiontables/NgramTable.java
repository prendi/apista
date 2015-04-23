package pt.iscte.apista.ngram.instructiontables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import pt.iscte.apista.core.IAnalyzer;
import pt.iscte.apista.core.Instruction;
import pt.iscte.apista.core.Sentence;
import pt.iscte.apista.ngram.InstructionInfo;
import pt.iscte.apista.ngram.InstructionSequence;
import pt.iscte.apista.ngram.InstructionWrapper;
import pt.iscte.apista.ngram.NGramSentence;
import pt.iscte.apista.ngram.UnigramTable;
import pt.iscte.apista.ngram.interfaces.IInstructionTable;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;

public class NgramTable extends IInstructionTable {

	@Override
	public Table<Instruction, Instruction, InstructionInfo> buildTable(IAnalyzer analyzer, UnigramTable unigramTable) {
		table = HashBasedTable.create();

		List<Instruction> startList = new ArrayList<>();
		for (int i = 0; i < n - 1; i++) {
			startList.add(Instruction.START);
		}

		table.put(Instruction.START, new InstructionSequence(startList, 0, n - 2),
				new InstructionInfo(analyzer.getSentences().size()));
		List<Sentence> listSentences = analyzer.getSentences();
		for (Sentence sentence : listSentences) {

			NGramSentence list = new NGramSentence(n, sentence.getInstructions(), unigramTable);

			for (int i = 0; i != list.size() - n + 1; i++) {

				InstructionSequence is = new InstructionSequence(list, i + 1, Math.min(list.size() - 1, i + n - 1));

				if (table.contains(list.get(i), is)) {
					table.get(list.get(i), is).addFrequency();
				} else {
					table.put(list.get(i), is, new InstructionInfo(1));
				}
			}
		}
		return table;
	}

	@Override
	public void calculateFrequency() {
		for (Cell cell : table.cellSet()) {
			int frequency = 0;

			List<Instruction> list = new ArrayList<>();
			list.add((Instruction) cell.getRowKey());
			list.addAll(((InstructionSequence) cell.getColumnKey()).getInstructionSequence());
			InstructionSequence is = new InstructionSequence(list, 0, list.size() - 2);

			Map<Instruction, InstructionInfo> rows = table.column(is);

			for (InstructionInfo value : rows.values()) {
				frequency += value.getFrequency();
			}

			InstructionInfo info = ((InstructionInfo) cell.getValue());
			info.setRelativeFrequency((double) info.getFrequency() / frequency);
		}

	}

	@Override
	public List<InstructionWrapper> queryFrequencyTable(List<Instruction> context, int maxInstructions) {

		List<InstructionWrapper> returnList = new ArrayList<InstructionWrapper>();

		for (Instruction column : table.row(context.get(0)).keySet()) {

			InstructionSequence sequence = (InstructionSequence) column;
			if (sequence.startsWith(context, 1, context.size() - 1) && !sequence.getLast().equals(Instruction.END)
					&& !sequence.getLast().equals(Instruction.UNK)) {

				double prob = table.get(context.get(0), column).getRelativeFrequency();

				returnList.add(new InstructionWrapper(sequence.getLast(), prob));
			}
		}

		Collections.sort(returnList);

		return returnList;
	}

	@Override
	public double probability(Instruction instruction, List<Instruction> context) {

		InstructionSequence seq = new InstructionSequence(context, 1, context.size() - 1);
		seq.add(instruction);
		InstructionInfo instructionInfo = table.get(context.get(0), seq);

		return instructionInfo == null ? 0.0 : instructionInfo.getRelativeFrequency();
	}

}
