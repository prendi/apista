package pt.iscte.apista.extractor;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.IType;

import pt.iscte.apista.core.ConstructorInstruction;
import pt.iscte.apista.core.Filter;
import pt.iscte.apista.core.IAnalyzer;
import pt.iscte.apista.core.ITypeCache;
import pt.iscte.apista.core.Instruction;
import pt.iscte.apista.core.MethodInstruction;
import pt.iscte.apista.core.Sentence;
import pt.iscte.apista.core.SystemConfiguration;

public class Analyzer implements IAnalyzer, Serializable {

	private static final long serialVersionUID = 1L;

	private List<Sentence> sentences;
	private long time;
	private int nFiles;
	private String packageRoot;
	private Filter[] filters;
	private BufferedOutputStream fos;

	public Filter[] getFilters() {
		return filters;
	}

	public void setFilters(Filter... filters) {
		this.filters = filters;
	}

	public Analyzer() {
		sentences = new ArrayList<>();
		packageRoot = "";
	}

	// public Analyzer(String packageRoot) {
	// this.packageRoot = packageRoot;
	// sentences = new ArrayList<>();
	// }

	// public Sentence newSentence(int startLine) {
	//
	// if (!sentences.isEmpty()
	// && sentences.get(sentences.size() - 1).isEmpty()) {
	// return sentences.get(sentences.size() - 1);
	// } else {
	// Sentence s = new Sentence(filePath, startLine);
	// sentences.add(s);
	// return s;
	// }
	// }

	private Sentence sentence;

	public Sentence newSentence(int startLine) {

		if (sentence == null) {
			sentence = new Sentence(filePath, startLine);
		}

		if (sentence.isEmpty()) {
			return sentence;
		} else {
			try {
				if (sentence.getInstructions().size() > 1) {
					for (Instruction instruction : sentence.getInstructions()) {
						fos.write(instruction.getWord().getBytes());
						fos.write(" ".getBytes());
					}
					fos.write("\n".getBytes());
				}
				sentence = new Sentence(filePath, startLine);
				return sentence;

			} catch (IOException e) {
				// TODO
				e.printStackTrace();
			}
		}
		return null;
	}

	public String getPackageRoot() {
		return packageRoot;
	}

	@Override
	public void randomizeSentences(long seed) {
		Collections.shuffle(sentences, new Random(seed));
	}

	// FILTERS ACCEPT CONTENT
	public List<Sentence> getSentences() {
		if (filters == null || filters.length == 0) {
			return sentences;
		}
		List<Sentence> list = new ArrayList<Sentence>();
		for (int i = 0; i < sentences.size(); i++) {
			double progress = i / (double) sentences.size();
			if (accept(progress)) {
				list.add(sentences.get(i));
				// System.out.println("PROGRESS: " + progress);
			}
		}
		return list;
	}

	public List<Sentence> getAllSentences() {
		return sentences;
	}

	private boolean accept(double progress) {
		for (Filter f : filters)
			if (f.accept(progress))
				return true;

		return false;
	}

	// void addInstruction(Instruction instruction) {
	// sentences.get(sentences.size() - 1).addInstruction(instruction);
	// }

	public int getTotalOccurrences() {
		int total = 0;
		for (Sentence s : sentences) {
			total += s.getInstructions().size();
		}

		return total;

	}

	private void parse(String repositoryRoot, String[] libSrcRoot) {
		long t = System.currentTimeMillis();
		nFiles = 0;
		parse(new File(repositoryRoot), libSrcRoot);
		time = (System.currentTimeMillis() - t) / 1000;
		System.out.println("FILES FAILED: " + numberOfFilesFailed);
	}

	public long getTime() {
		return time;
	}

	public int getNumberOfFiles() {
		return nFiles;
	}

	public int getNumberOfSentences() {
		return sentences.size();
	}

	public Set<String> getClasses() {
		Set<String> classes = new HashSet<>();
		for (Sentence s : sentences)
			for (Instruction i : s)
				classes.add(i.getClassName());

		return classes;
	}

	public Set<String> getWords() {
		Set<String> words = new HashSet<>();
		for (Sentence s : sentences)
			for (Instruction i : s)
				words.add(i.getWord().intern());

		return words;
	}

	public int getNumberOfWords() {
		return getWords().size();
	}

	public int minSentenceSize() {
		int min = Integer.MAX_VALUE;
		for (Sentence s : sentences) {
			int n = s.getInstructions().size();
			if (n < min)
				min = n;
		}
		return min;
	}

	public int maxSentenceSize() {
		int max = 0;
		for (Sentence s : sentences) {
			int n = s.getInstructions().size();
			if (n > max)
				max = n;
		}
		return max;
	}

	public double avgSentenceSize() {
		double sum = 0;
		for (Sentence s : sentences)
			sum += s.getInstructions().size();

		return sum / sentences.size();
	}

	private String filePath;

	// private void parse(File file, String[] libSrc) {
	//
	// if (file.isFile() && file.getName().endsWith(".java")) {
	// long time = System.currentTimeMillis();
	// filePath = file.getAbsolutePath();
	//
	// // JavaSourceParser parser = JavaSourceParser.createFromJar(
	// // filePath, libSrc);
	// JavaSourceParser parser = JavaSourceParser.createFromFile(filePath,
	// libSrc, "UTF-8");
	//
	// BlockVisitorV3 v = new BlockVisitorV3(this);
	// try{
	// parser.parse(v);
	// }catch(Exception e){
	// System.out.println("ERROR ON FILE: " + file.getAbsolutePath() + ":" +
	// v.getCurrentLine());
	// e.printStackTrace();
	// }
	// nFiles++;
	// if(System.currentTimeMillis() - time > 1000){
	// System.err.println("TOOK - " +(System.currentTimeMillis() - time)
	// +" ERROR ON: " + nFiles + " - " + file.getAbsolutePath());
	// }
	// if(nFiles % 1000 == 0){
	// System.out.println("In progress " + nFiles + " - "
	// +file.getAbsolutePath());
	// }
	//
	// } else if (file.isDirectory() && !file.getName().startsWith(".")) {
	// for (File c : file.listFiles())
	// parse(c, libSrc);
	// }
	// }

	private int numberOfFilesFailed = 0;

	private void parse(File file, final String[] libSrc) {

		if (file.isFile() && file.getName().endsWith(".java")) {
			long time = System.currentTimeMillis();
			filePath = file.getAbsolutePath();

			// JavaSourceParser parser = JavaSourceParser.createFromJar(
			// filePath, libSrc);

			try {
				ExecutorService executor = Executors.newSingleThreadExecutor();
				Callable<String> callable = new Callable<String>() {

					@Override
					public String call() throws Exception {
						JavaSourceParser parser = JavaSourceParser
								.createFromFile(filePath, libSrc, "UTF-8");
						BlockVisitorV3 v = new BlockVisitorV3(Analyzer.this);
						parser.parse(v);
						return null;
					}
				};
				Future<String> future = executor.submit(callable);
				future.get(5, TimeUnit.MINUTES);

			} catch (Exception e) {
				numberOfFilesFailed++;
				System.out.println("ERROR ON FILE: " + file.getAbsolutePath());// +
																				// ":"
																				// +
																				// v.getCurrentLine());
				e.printStackTrace();
			}
			nFiles++;
			if (nFiles % 1000 == 0) {
				System.out.println("In progress " + nFiles + " - "
						+ file.getAbsolutePath());
			}

		} else if (file.isDirectory() && !file.getName().startsWith(".")) {
			for (File c : file.listFiles())
				parse(c, libSrc);
		}
	}

	public Sentence getFirst() {
		return sentences.get(0);
	}

	@Override
	public void loadSentences(File file) {
		try {
			Scanner s = new Scanner(file);
			while (s.hasNextLine()) {
				String[] words = s.nextLine().split(" ");

				Sentence newSentence = new Sentence();
				for (String word : words) {
					String[] splittedMethodWord = word.split("\\.");

					if (splittedMethodWord[1].equals("new")) {
						newSentence.addInstruction(new ConstructorInstruction(
								splittedMethodWord[0]));
					} else {
						newSentence.addInstruction(new MethodInstruction(
								splittedMethodWord[0], splittedMethodWord[1]));
					}
				}
				sentences.add(newSentence);
			}
			System.out.println("ADDED " + sentences.size() + " SENTENCES");

		} catch (FileNotFoundException e) {
			System.err.println("The file " + file.getName() + " was not found");
			e.printStackTrace();
		}

	}

	@Override
	public void run(SystemConfiguration configuration) {
		try {
			fos = new BufferedOutputStream(new FileOutputStream(new File(
					configuration.getResourceFolder()
							+ configuration.getSrilmAnalyzerFilename() )));

			run(configuration.getLibRootPackage(), configuration.getRepPath(),
					configuration.getApiSrcPath());

			fos.close();

			loadAndSerializeAnalyzer(new File(
					configuration.getResourceFolder()
					+ configuration.getSrilmAnalyzerFilename()),
					new File(configuration.getResourceFolder()
					+ configuration.getSerializedAnalyzerFileName()));

		} catch (IOException e) {
			System.err.println("Problem running analyzer:" + e.getMessage());
			e.printStackTrace();
		}
	}

	public void run(String packageRoot, String repPath, String srcPath) {
		run(packageRoot, repPath, new String[] { srcPath });
	}

	public void run(String packageRoot, String repPath, String[] srcPath) {
		this.packageRoot = packageRoot;
		sentences = new ArrayList<>();
		parse(repPath, srcPath);
	}

	@Override
	public void loadAndSerializeAnalyzer(File loadFile, File outputfile) {
		try {

			loadSentences(loadFile);
			
			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(outputfile));

			oos.writeObject(this);

			oos.close();
		} catch (IOException e) {
			// TODO
		}
	}

}
