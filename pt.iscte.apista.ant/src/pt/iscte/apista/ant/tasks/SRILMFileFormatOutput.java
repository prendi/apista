package pt.iscte.apista.ant.tasks;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Scanner;

import pt.iscte.apista.ant.APISTATask;
import pt.iscte.apista.core.Instruction;
import pt.iscte.apista.core.Sentence;

public class SRILMFileFormatOutput extends APISTATask {

	@Override
	public void execute() {
		super.execute();

		try {
			loadLanguageModel(new FileInputStream(
					configuration.getResourceFolder() + "ResultLM.lm"));
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
			while (s.hasNext()) {
//				String[] temp = s.nextLine().split("\\t");
//				if(temp.length > 1);
//					System.out.println(s.nextLine().split("\\t")[1]);
				System.out.println(s.nextLine().charAt(0));
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
