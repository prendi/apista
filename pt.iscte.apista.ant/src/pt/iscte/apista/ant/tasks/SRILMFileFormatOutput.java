package pt.iscte.apista.ant.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import pt.iscte.apista.ant.APISTATask;
import pt.iscte.apista.core.ConstructorInstruction;
import pt.iscte.apista.core.Instruction;
import pt.iscte.apista.core.MethodInstruction;
import pt.iscte.apista.core.Sentence;

public class SRILMFileFormatOutput extends APISTATask {

	@Override
	public void execute() {
		super.execute();

		try {
			loadLanguageModel(new FileInputStream(
					configuration.getResourceFolder() + configuration.getModelFileName()));
			/*lexiconOutput(new FileOutputStream(configuration.getOutputFolder()
					+ "Lexicon.txt"));
			sentencesOutput(new FileOutputStream(
					configuration.getOutputFolder() + "Sentences.Train"));
					*/
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void loadLanguageModel(FileInputStream fileInputStream)
			throws IOException {
		Scanner s = null;
		try {
			s = new Scanner(fileInputStream);
			boolean found3Gram = false;
			while (s.hasNext() && !found3Gram) {
				String temp = s.nextLine();
				if (temp.contains("3-grams:")) {
					found3Gram = true;
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
						list.add(new ConstructorInstruction(ngrams[i]));
					}else if(ngrams[i].equals("</s>")){
						list.add(new ConstructorInstruction(ngrams[i]));
					}else{
						list.add(new MethodInstruction(ngrams[i]));
					}
				}
				System.out.println(list.size());
//				System.out.println(nextline.split("\t")[0] + " OMG " + nextline.split("\t")[1]);
//				System.out.println(nextline.split("\t")[1].split(" ")[0] + " " +nextline.split("\t")[1].split(" ")[1] + " " + nextline.split("\t")[1].split(" ")[2]);
			}
		} finally {
			if(s != null)
				s.close();
		}


	}

	private void sentencesOutput(FileOutputStream fileOutputStream)
			throws IOException {
		for (Sentence sentence : configuration.getAnalyzer().getAllSentences()) {
			for (Instruction instruction : sentence.getInstructions()) {
				fileOutputStream.write(instruction.getWord().getBytes());
				fileOutputStream.write(" ".getBytes());
			}
			fileOutputStream.write("\n".getBytes());
		}

	}

	private void lexiconOutput(OutputStream fileOutputStream)
			throws IOException {
		for (String word : configuration.getAnalyzer().getWords()) {

			fileOutputStream.write(word.getBytes());
			fileOutputStream.write("\n".getBytes());

		}

	}

	public static void main(String[] args) {

		new SRILMFileFormatOutput().execute();
	}

}
