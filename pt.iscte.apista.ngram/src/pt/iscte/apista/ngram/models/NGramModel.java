package pt.iscte.apista.ngram.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.eclipse.jdt.core.IType;

import pt.iscte.apista.core.APIModel;
import pt.iscte.apista.core.ConstructorInstruction;
import pt.iscte.apista.core.IAnalyzer;
import pt.iscte.apista.core.ITypeCache;
import pt.iscte.apista.core.Instruction;
import pt.iscte.apista.core.MethodInstruction;
import pt.iscte.apista.ngram.InstructionInfo;
import pt.iscte.apista.ngram.InstructionSequence;
import pt.iscte.apista.ngram.InstructionWrapper;
import pt.iscte.apista.ngram.NGramSentence;
import pt.iscte.apista.ngram.Reporter;
import pt.iscte.apista.ngram.UnigramTable;
import pt.iscte.apista.ngram.instructiontables.NgramTable;
import pt.iscte.apista.ngram.interfaces.IInstructionTable;

@SuppressWarnings("serial")
public class NGramModel implements APIModel, Serializable {

	public static final Instruction START = new SentenceStart();
	public static final Instruction END = new SentenceEnd();
	public static final Instruction UNK = new UnknownInstruction();
	
	private int n;

	private UnigramTable unigramTable;
	private IInstructionTable instructionTable;

	@Override
	public void setup(Parameters params) {
		instructionTable = new NgramTable();
		instructionTable.setup(params);
		n = params.getIntValue("n");
	}

	@Override
	public void build(IAnalyzer analyzer) {

		unigramTable = new UnigramTable(analyzer);

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
	
	@Override
	public void load(File file) throws IOException {
		load(new FileInputStream(file));
	}

	@Override
	public void load(InputStream stream) throws IOException {
		
		Scanner s = null;
		try {
			s = new Scanner(stream);
			boolean foundNGram = false;
			while (s.hasNext() && !foundNGram) {
				String temp = s.nextLine();
				if (temp.contains(n+"-grams:")) {
					foundNGram = true;
				}
			}
			String nextline;
			while (s.hasNext() && !((nextline = s.nextLine()).equals(""))) {
				String[] splittedLine = nextline.split("\t");
				double probability = Double.parseDouble(splittedLine[0]);
				List<Instruction> list = new ArrayList<Instruction>();
				String[] ngrams = splittedLine[1].split(" ");
				for (int i = 0; i < ngrams.length; i++) {
					if(ngrams[i].contains(".new")){
						list.add(new ConstructorInstruction(ngrams[i].replace(".new", ""))); 
					}else if(ngrams[i].equals("<s>")){
						list.add(START);
					}else if(ngrams[i].equals("</s>")){
						list.add(END);
					}else{
						list.add(new MethodInstruction(ngrams[i]));
					}
				}
				InstructionSequence is = new InstructionSequence(list, 1, list.size()-1);
//				System.out.println("TABLE SIZE: " + instructionTable.getTable().size());
				instructionTable.getTable().put(list.get(0), is, new InstructionInfo(probability));
			}
		} finally {
			if(s!= null)
				s.close();
			System.out.println("TABLE SIZE: " + instructionTable.getTable().size());
		}
//		ObjectInputStream ois = new ObjectInputStream(stream);
//		try {
//			instructionTable = (IInstructionTable) ois.readObject();
//			unigramTable = (UnigramTable) ois.readObject();
//		} 
//		catch (ClassNotFoundException e) {
//			System.out.println("Error loading file on NGramModel class");
//		} finally {
//			ois.close();
//		}

	}

	@Override
	public List<Instruction> query(List<Instruction> context, int max) {
		NGramSentence ngs = null;
		if(unigramTable == null){
			ngs = new NGramSentence(n, context, null);
		}else{
			ngs = new NGramSentence(n, context, unigramTable);
		}
		List<InstructionWrapper> proposals = instructionTable.queryFrequencyTable(ngs.tail(), max);
		return InstructionWrapper.convertFromWrapperToList(proposals);
	}

	@Override
	public double probability(Instruction instruction, List<Instruction> context) {
		NGramSentence ngs = new NGramSentence(n, context, unigramTable);

		if ((unigramTable != null && unigramTable.isRare(instruction)) || (unigramTable == null && instruction.equals(UNK)))
			instruction = UNK;

		double prob = instructionTable.probability(instruction, ngs.tail());
		return prob == 0.0 ? unigramTable.rareFrequency() : prob;
	}
	
	public IInstructionTable getInstructionTable() {
		return instructionTable;
	}
	
	private static class SentenceStart extends Instruction {
		protected SentenceStart() {
			super("");
		}

		@Override
		public String getWord() {
			return "<s>";
		}

		@Override
		public String resolveInstruction(IType type, Map<String, IType> vars, ITypeCache typeCache) {
			throw new UnsupportedOperationException();
		}	
	}

	private static class SentenceEnd extends Instruction {
		protected SentenceEnd() {
			super("");
		}

		@Override
		public String getWord() {
			return "</s>";
		}

		@Override
		public String resolveInstruction(IType type, Map<String, IType> vars, ITypeCache typeCache) {
			throw new UnsupportedOperationException();
		}	
	}
	
	
	private static class UnknownInstruction extends Instruction {
		protected UnknownInstruction() {
			super("");
		}

		@Override
		public String getWord() {
			return "<unk>";
		}

		@Override
		public String resolveInstruction(IType type, Map<String, IType> vars, ITypeCache typeCache) {
			throw new UnsupportedOperationException();
		}	
	}




}
