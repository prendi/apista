package pt.iscte.apista.ngram.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pt.iscte.apista.core.APIModel;
import pt.iscte.apista.core.IAnalyzer;
import pt.iscte.apista.core.Instruction;
import pt.iscte.apista.ngram.InstructionWrapper;
import pt.iscte.apista.ngram.NGramSentence;
import pt.iscte.apista.ngram.UnigramTable;
import pt.iscte.apista.ngram.instructiontables.NgramTable;
import pt.iscte.apista.ngram.interfaces.IInstructionTable;

@SuppressWarnings("serial")
public class StupidBackoffNGramModel implements APIModel, Serializable {

	private List<IInstructionTable> models = new ArrayList<>();
	private UnigramTable unigramTable;
	private int maxNgramSize = 3;
	private int minNgramSize = 2;
	private double stupidBackoffMultiplier = 0.4;

	@Override
	public void setup(Parameters params) {
		maxNgramSize = params.getIntValue("nmax");
		minNgramSize = params.getIntValue("nmin");
		stupidBackoffMultiplier = params.getDoubleValue("multiplier");
	}

	@Override
	public void build(IAnalyzer analyzer) {
		unigramTable = new UnigramTable(analyzer);

		for (int n = maxNgramSize; n >= minNgramSize; n--) {

			IInstructionTable instructionTable = new NgramTable();
			
			Parameters params = new Parameters();
			params.addParameter("n", ""+n);
			
			instructionTable.setup(params);
			instructionTable.buildTable(analyzer, unigramTable);
			instructionTable.calculateFrequency();

			models.add(instructionTable);
		}

	}

	@Override
	public void save(File file) throws IOException {

		OutputStream output = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(output);
		oos.writeObject(models);
		oos.writeObject(unigramTable);
		oos.close();

	}

	@SuppressWarnings("unchecked")
	@Override
	public void load(File file) throws IOException {
		InputStream input = new FileInputStream(file);
		ObjectInputStream ois = new ObjectInputStream(input);
		try {
			models = (List<IInstructionTable>) ois.readObject();
			unigramTable = (UnigramTable) ois.readObject();
		} catch (ClassNotFoundException e) {
			System.out.println("Error loading file on NgramModel class");
		} finally {
			ois.close();
		}

	}

	@Override
	public List<Instruction> query(List<Instruction> context, int max) {
		List<InstructionWrapper> proposals = new ArrayList<>();
		stupidBackoff(proposals, context, 0, maxNgramSize);

		return convertFromWrapperToList(proposals);
	}

	private void stupidBackoff(List<InstructionWrapper> proposals,
			List<Instruction> context, int level, int n) {

		NGramSentence ngs = new NGramSentence(n, context, unigramTable);
		IInstructionTable model = models.get(level);

		List<InstructionWrapper> levelProposals = model.queryFrequencyTable(
				ngs.tail(), 200);
		for (InstructionWrapper iw : levelProposals) {
			if (!hasInstruction(proposals, iw)) {
				iw.setrFrequency(iw.getrFrequency()
						* Math.pow(stupidBackoffMultiplier, level));
				proposals.add(iw);
			}
		}
		if (n != minNgramSize)
			stupidBackoff(proposals, context, level + 1, n - 1);
	}

	private boolean hasInstruction(List<InstructionWrapper> proposals,
			InstructionWrapper iw) {
		for (InstructionWrapper instructionWrapper : proposals) {
			if (instructionWrapper.getInstruction().equals(iw.getInstruction())) {
				return true;
			}
		}
		return false;

	}

	@Override
	public double probability(Instruction instruction, List<Instruction> context) {
		// List<InstructionWrapper> proposals = new ArrayList<>();
		// boolean useMultipler = false;
		// boolean found = false;
		// for (int i = models.size()-1 ; i != 0 && !found; i--) {
		// proposals = models.get(i).queryFrequencyTable(context, 200,
		// models.get(i).getTable());
		//
		// if(proposals.size() > 0 ){
		// if(!useMultipler)
		// found = true;
		// else{
		// for (InstructionWrapper instructionWrapper : proposals) {
		// instructionWrapper.setrFrequency(instructionWrapper.getrFrequency()*STUPID_BACKOFF_MULTIPLIER);
		// }
		// found = true;
		// }
		// }else{
		// useMultipler = true;
		// }
		// }
		// for (InstructionWrapper i : proposals) {
		// if(instruction.equals(i.getInstruction())){
		// return i.getrFrequency();
		// }
		// }
		throw new UnsupportedOperationException();
		// return 0;
	}

	private List<Instruction> convertFromWrapperToList(
			List<InstructionWrapper> list) {
		Collections.sort(list);
		List<Instruction> returnList = new ArrayList<>();

		for (InstructionWrapper instruction : list) {
			returnList.add(instruction.getInstruction());
		}

		return returnList;

	}

}
