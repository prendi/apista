package pt.iscte.apista.extractor.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.core.dom.ASTVisitor;

import com.google.common.util.concurrent.SimpleTimeLimiter;

import pt.iscte.apista.core.Instruction;
import pt.iscte.apista.core.Sentence;
import pt.iscte.apista.extractor.Analyzer;
import pt.iscte.apista.extractor.ApiVisitor;
import pt.iscte.apista.extractor.BlockVisitorV3;
import pt.iscte.apista.extractor.CorpusVisitor;
import pt.iscte.apista.extractor.JavaSourceParser;

public class Coverage {
	//public static final String FOLDERPATH = "/media/goncalo/A47C5F6D7C5F38EE/Users/goncalo/Analyzers/JacksonResources/";
	public static final String LIB_PACKAGE = "javax.swing";
	public static final String LIBSRC = "/home/goncalo/sources/jswingsources";

	static final Set<String> apiWords = new HashSet<>();
	static final Set<String> apiWordsTop = new HashSet<>();
	static final Set<String> apiTypes = new HashSet<>();

	public static void main(String[] args) throws Exception {
		//Builds field sets, always call
		parseApiWords();
		
		//Call if you want to visit corpus to find existing api calls 
		//parseCorpus();
		
		//Call if you want to parse sentences and serialize used analyzer
		//parseSentencesAndSerializeAnalyzer();
		
		//Call if you want results from serialized analyzer
		compareFromSerializedAnalyzer();
	}

	public static void compareFromParsedCorpus() throws Exception {
		// Get CorpusVisitor result
		File corpusVisitorResult = new File("CorpusVisitorResult");
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
				corpusVisitorResult));
		CorpusVisitor visitor = (CorpusVisitor) ois.readObject();
		ois.close();
		
		//TODO
	}

	public static void compareFromSerializedAnalyzer() throws Exception {
		// Get Analyzer
		File analyzerFile = new File("AnalyzerJSwing");
		Analyzer analyzer = new Analyzer();
		analyzer.loadSentences(analyzerFile);
		
		// Flat sentences into set of words
		Set<String> wordsFound = new HashSet<>();
		for (Sentence s : analyzer.getSentences())
			for (Instruction inst : s.getInstructions())
				wordsFound.add(inst.getWord());
		
		// Compare API words with words found in corpus
		int foundCount = 0;
		int foundTopCount = 0;
		for (String word : wordsFound) {
			if (apiWords.contains(word))
				foundCount++;
			if (apiWordsTop.contains(word))
				foundTopCount++;
		}
		System.out.println("Found: " + foundCount + "/" + apiWords.size());
		System.out.println("Found in top: " + foundTopCount + "/"
				+ apiWordsTop.size());
		System.out.println("Total sentences: "
				+ analyzer.getNumberOfSentences());
		System.out.println("Avg sentence size: " + analyzer.avgSentenceSize());

		int sentencesWith1Word = 0;
		for (Sentence s : analyzer.getAllSentences())
			if (s.getInstructions().size() == 1)
				sentencesWith1Word++;

		for (int i = 2; i <= 20; i++) {
			int count = 0;
			for (Sentence s : analyzer.getAllSentences())
				if (s.getInstructions().size() == i)
					count++;
			System.out.println(count);
		}
		int biggerThan20 = 0;
		for (Sentence s : analyzer.getAllSentences())
			if (s.getInstructions().size() > 20)
				biggerThan20++;
		System.out.println("Num sentences w/ >=20 words: " + biggerThan20);

		System.out.println("Num sentences w/ >=2 words: "
				+ (analyzer.getAllSentences().size() - sentencesWith1Word));

		// Total tokens
		int totalTokens = 0;
		Map<String, Integer> tokenOccurences = new HashMap<>();
		for (Sentence s : analyzer.getAllSentences()) {
			totalTokens += s.getInstructions().size();
			for (Instruction inst : s.getInstructions())
				if (tokenOccurences.containsKey(inst.toString()))
					tokenOccurences.put(inst.toString(),
							tokenOccurences.get(inst.toString()) + 1);
				else
					tokenOccurences.put(inst.toString(), 1);
		}
		int rareTokens = 0;
		for (String key : tokenOccurences.keySet())
			if (tokenOccurences.get(key) < 3)
				rareTokens++;

		System.out.println("Total tokens: " + totalTokens);
		System.out.println("Total unique tokens: "
				+ tokenOccurences.keySet().size());
		System.out.println("Rare unique tokens: " + rareTokens);

		// Total Unique Tokens
		// Total tokens c/ <= 2 ocorrencias

		int countGreaterThanTen = 0;
		for (Sentence s : analyzer.getAllSentences())
			if (s.getInstructions().size() > 10)
				countGreaterThanTen++;
		System.out.println("With >10 words: " + countGreaterThanTen);
		
		/*
		for (Sentence s : analyzer.getAllSentences())
			if (s.getInstructions().size() > 100)
				System.out.println(s.getFilePath() + "\n" + s.getInstructions());
		*/
	}
	
	/*
	public static void parseSentencesAndSerializeAnalyzer() {
		File dir = new File(FOLDERPATH);
		if (!dir.exists() || !dir.isDirectory())
			throw new RuntimeException("nope");
		Analyzer analyzer = new Analyzer(LIB_PACKAGE);
		System.out.println("Started");
		parseSentencesDir(dir, analyzer);
		
		System.out.println("Finished parse");
		
		// Serialize Analyzer
		ObjectOutputStream oos = null;
		try {
			File analyzerFile = new File("Analyzer");
			analyzerFile.createNewFile();
			oos = new ObjectOutputStream(new FileOutputStream(analyzerFile));
			oos.writeObject(analyzer);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		} finally {
			if (oos != null)
				try {
					oos.close();
				} catch (IOException e3) {
					e3.printStackTrace();
				}
		}
		
		System.out.println("Finished analyzer file.");
	}
	*/
	public static void parseSentencesDir(File dir, Analyzer analyzer) {
		for (File f : dir.listFiles())
			if (f.isDirectory())
				parseSentencesDir(f, analyzer);
			else if (f.getName().endsWith(".java"))
				parseSentencesFile(f.getAbsolutePath(), analyzer);
	}

	public static <V> void parseSentencesFile(final String file, final Analyzer analyzer) {
		try {
			new SimpleTimeLimiter().callWithTimeout(new Callable<Boolean>() {
				@Override
				public Boolean call() {
					BlockVisitorV3 visitor = new BlockVisitorV3(analyzer);
					try {
						JavaSourceParser parser = JavaSourceParser.createFromFile(file, LIBSRC, "UTF-8");
						parser.parse(visitor);
						System.out.println("parsed " + file);
					} catch (Exception e) {
						System.out.println("ERROR " + e.getClass().getName() + ": " + file);
						e.printStackTrace();
					
					}
					return true;
				}
			}, 60, TimeUnit.SECONDS, true);
		} catch (Exception e) {
			System.out.println("ERROR " + e.getClass().getName() + " on file " + file);
		}
	}

	/*public static void parseCorpus() {
		File dir = new File(FOLDERPATH);
		if (!dir.exists() || !dir.isDirectory())
			throw new RuntimeException("nope");
		
		// Parse corpus with CorpusVisitor
		CorpusVisitor visitor = new CorpusVisitor(LIBSRC);
		parseVisitorDir(dir, visitor);
		System.out.println("Corpus parsed.");
		
		// Sysout corpusvisitor result data
		System.out.println("API contains " + apiWordsTop.size() + " top words.");
		System.out.println("Corpus contains " + visitor.wordsTop.size() + " top words.");
		int count = 0;
		for(String word: visitor.wordsTop)
			if(apiWordsTop.contains(word))
				count++;
			else
				System.out.println("API does not contain top word: " + word);
		System.out.println("Corpus contains " + count + " api top words. " +
			(double)count/(double)apiWordsTop.size());
		System.out.println("API contains " + apiTypes.size() + " types.");
		System.out.println("CorpusVisitor found " + visitor.types.size() + " types.");
		
		count = 0;
		for(String type: visitor.types)
			if(apiTypes.contains(type))
				count++;
			else
				System.out.println("API does not contain type: " + type);
		System.out.println("Corpus contains " + count + " types. " +
			(double)count/(double)apiTypes.size());
		
		// Serialize CorpusVisitor result
		ObjectOutputStream oos = null;
		try {
			File corpusVisitorResult = new File("CorpusVisitorResult");
			corpusVisitorResult.createNewFile();
			oos = new ObjectOutputStream(new FileOutputStream(
					corpusVisitorResult));
			oos.writeObject(visitor);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (oos != null)
				try {
					oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}

	}*/

	public static void parseApiWords() {
		// Parse API words
		File dir = new File(LIBSRC);
		if (!dir.exists() || !dir.isDirectory())
			throw new RuntimeException("nope");
		// for (File subdir : dir.listFiles()) {
		// apiWords.clear();
		// apiWordsTop.clear();
		// apiTypes.clear();
		parseApiDir(dir);
		System.out.println("parsed api " + dir.getName());

		System.out.println("API words: " + apiWords.size());
		System.out.println("API words top : " + apiWordsTop.size());
		System.out.println("API types: " + apiTypes.size());
		System.out.println();
		// }

	}

	public static void parseVisitorDir(File dir, ASTVisitor visitor) {
		for (File f : dir.listFiles())
			if (f.isDirectory())
				parseVisitorDir(f, visitor);
			else if (f.getName().endsWith(".java"))
				parseVisitorFile(f.getAbsolutePath(), visitor);
	}

	public static void parseVisitorFile(String file, ASTVisitor visitor) {
		try {
			JavaSourceParser parser = JavaSourceParser.createFromFile(file,
					LIBSRC, "UTF-8");
			parser.parse(visitor);
		} catch (Exception e) {
			System.out.println("ERROR IN FILE: " + file);
			e.printStackTrace();;
		}
	}

	public static void parseApiDir(File dir) {
		for (File f : dir.listFiles())
			if (f.isDirectory())
				parseApiDir(f);
			else if (f.getName().endsWith(".java"))
				parseApiFile(f.getAbsolutePath());
	}

	public static void parseApiFile(String file) {
		ApiVisitor visitor = null;
		try {
			visitor = new ApiVisitor();
			JavaSourceParser parser = JavaSourceParser.createFromFile(file,
					LIBSRC, "UTF-8");
			parser.parse(visitor);
			apiWords.addAll(visitor.words);
			apiWordsTop.addAll(visitor.wordsTop);
			apiTypes.addAll(visitor.types);
		} catch (Exception e) {
			System.out.println("ERROR IN FILE: " + file);
			e.printStackTrace();
		}
	}
}