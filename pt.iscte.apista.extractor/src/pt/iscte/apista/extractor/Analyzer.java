package pt.iscte.apista.extractor;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import pt.iscte.apista.core.Filter;
import pt.iscte.apista.core.IAnalyzer;
import pt.iscte.apista.core.Instruction;
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

	//	public Analyzer(String packageRoot) {
	//		this.packageRoot = packageRoot;
	//		sentences = new ArrayList<>();
	//	}

//	public Sentence newSentence(int startLine) {
//
//		if (!sentences.isEmpty()
//				&& sentences.get(sentences.size() - 1).isEmpty()) {
//			return sentences.get(sentences.size() - 1);
//		} else {
//			Sentence s = new Sentence(filePath, startLine);
//			sentences.add(s);
//			return s;
//		}
//	}

	private Sentence sentence;
	
	public Sentence newSentence(int startLine) {
		
		if(sentence == null){
			sentence = new Sentence(filePath, startLine);
		}
		
		if(sentence.isEmpty()){
			return sentence;
		}else {
			try{
				if(sentence.getInstructions().size() > 1){
					for(Instruction instruction : sentence.getInstructions()){
						fos.write(instruction.getWord().getBytes());
						fos.write(" ".getBytes());
					}
					fos.write("\n".getBytes());
				}
				sentence = new Sentence(filePath, startLine);
				return sentence;

			}catch(IOException e){
				//TODO
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
				//				System.out.println("PROGRESS: " + progress);
			}
		}
		return list;
	}

	public List<Sentence> getAllSentences(){
		return sentences;
	}

	private boolean accept(double progress) {
		for (Filter f : filters)
			if (f.accept(progress))
				return true;

		return false;
	}
	

//	void addInstruction(Instruction instruction) {
//		sentences.get(sentences.size() - 1).addInstruction(instruction);
//	}

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

//	private void parse(File file, String[] libSrc) {
//
//		if (file.isFile() && file.getName().endsWith(".java")) {
//			long time = System.currentTimeMillis();
//			filePath = file.getAbsolutePath();
//
////			JavaSourceParser parser = JavaSourceParser.createFromJar(
////					filePath, libSrc);
//			JavaSourceParser parser = JavaSourceParser.createFromFile(filePath, libSrc, "UTF-8");
//
//			BlockVisitorV3 v = new BlockVisitorV3(this);
//			try{
//				parser.parse(v);
//			}catch(Exception e){
//				System.out.println("ERROR ON FILE: " + file.getAbsolutePath() + ":" + v.getCurrentLine());
//				e.printStackTrace();
//			}
//			nFiles++;
//			if(System.currentTimeMillis() - time > 1000){
//				System.err.println("TOOK - " +(System.currentTimeMillis() - time) +" ERROR ON: " + nFiles + " - " + file.getAbsolutePath());
//			}
//			if(nFiles % 1000 == 0){
//				System.out.println("In progress " + nFiles  + " - " +file.getAbsolutePath());
//			}
//			
//		} else if (file.isDirectory() && !file.getName().startsWith(".")) {
//			for (File c : file.listFiles())
//				parse(c, libSrc);
//		}
//	}
	
	private void parse(File file, final String[] libSrc) {

		if (file.isFile() && file.getName().endsWith(".java")) {
			long time = System.currentTimeMillis();
			filePath = file.getAbsolutePath();

//			JavaSourceParser parser = JavaSourceParser.createFromJar(
//					filePath, libSrc);
			
			try{
				 ExecutorService executor = Executors.newSingleThreadExecutor();
				 Callable<String> callable = new Callable<String>() {

						@Override
						public String call() throws Exception {
							JavaSourceParser parser = JavaSourceParser.createFromFile(filePath, libSrc, "UTF-8");
							BlockVisitorV3 v = new BlockVisitorV3(Analyzer.this);
							parser.parse(v);
							return null;
						}
					};
				 Future<String> future = executor.submit(callable);
				 future.get(5, TimeUnit.MINUTES);
				 
			}catch(Exception e){
				System.out.println("ERROR ON FILE: " + file.getAbsolutePath());// + ":" + v.getCurrentLine());
				e.printStackTrace();
			}
			nFiles++;
			if(nFiles % 1000 == 0){
				System.out.println("In progress " + nFiles  + " - " +file.getAbsolutePath());
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
	public void run(SystemConfiguration configuration) {
		try {
			fos = new BufferedOutputStream(new FileOutputStream( new File(configuration.getOutputFolder() + configuration.getAnalyzerFileName())));
			
			run(configuration.getLibRootPackage(), configuration.getRepPath(), configuration.getApiSrcPath());
			
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run(String packageRoot, String repPath, String srcPath){
		run(packageRoot, repPath, new String[] {srcPath});
	}
	
	public void run(String packageRoot, String repPath, String[] srcPath){
		this.packageRoot = packageRoot;
		sentences = new ArrayList<>();
		parse(repPath, srcPath);
	}




}
