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
import java.util.List;

import pt.iscte.apista.core.APIModel;
import pt.iscte.apista.core.IAnalyzer;
import pt.iscte.apista.core.Instruction;
import pt.iscte.apista.ngram.InstructionWrapper;
import pt.iscte.apista.ngram.NGramSentence;
import pt.iscte.apista.ngram.Reporter;
import pt.iscte.apista.ngram.UnigramTable;
import pt.iscte.apista.ngram.instructiontables.NgramTable;
import pt.iscte.apista.ngram.interfaces.IInstructionTable;

@SuppressWarnings("serial")
public class NGramModel implements APIModel, Serializable {

	private int n;
	
	private UnigramTable unigramTable;
	private IInstructionTable instructionTable = new NgramTable();

	
	@Override
	public void setup(Parameters params) {
		n = params.getIntValue("n");
	}
	
	@Override
	public void build(IAnalyzer analyzer) {
		
		unigramTable = new UnigramTable(analyzer);
		
		Parameters params = new Parameters();
		params.addParameter("n", ""+n);
		
		instructionTable.setup(params);
		
		instructionTable.buildTable(analyzer, unigramTable);
		
		instructionTable.calculateFrequency();
	}

	@Override
	public void save(File file) throws IOException {

		OutputStream output = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(output);
		oos.writeObject(instructionTable);
		oos.writeObject(unigramTable);
		Reporter.reportFrequencyTable(instructionTable.getTable());
		oos.close();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void load(File file) throws IOException {
		InputStream input = new FileInputStream(file);
		ObjectInputStream ois = new ObjectInputStream(input);
		try {
			instructionTable = (IInstructionTable) ois
					.readObject();
			
			unigramTable = (UnigramTable) ois.readObject();
		} catch (ClassNotFoundException e) {
			System.out
					.println("Error loading file on NGramModel class");
		}finally{
			ois.close();
		}

	}
	

	@Override
	public List<Instruction> query(List<Instruction> context, int max) {
		NGramSentence ngs = new NGramSentence(n, context, unigramTable);
		List<InstructionWrapper> proposals = instructionTable.queryFrequencyTable(ngs.tail(), max);
		return InstructionWrapper.convertFromWrapperToList(proposals);
	}

	@Override
	public double probability(Instruction instruction, List<Instruction> context) {
		NGramSentence ngs = new NGramSentence(n, context, unigramTable);
		
		if(unigramTable.isRare(instruction))
			instruction = Instruction.UNK;
		
		double prob = instructionTable.probability(instruction, ngs.tail());
		
		return prob == 0.0 ? unigramTable.rareFrequency() : prob;
		
	}

}
