package pt.iscte.apista.extractor.util;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;

import pt.iscte.apista.extractor.ApiVisitor;
import pt.iscte.apista.extractor.CorpusVisitor;
import pt.iscte.apista.extractor.JavaSourceParser;

public class Coverage {
	public static final String FOLDERPATH = "C:/Users/HugoSousa/Documents/eclipse_src/Repositories";
	public static final String SWTPACKAGE = "org.eclipse.swt";
	public static final String LIBSRC = "../swtsrc/src";

	static final Set<String> apiWords = new HashSet<>();
	static final Set<String> apiWordsTop = new HashSet<>();
	static final Set<String> apiTypes = new HashSet<>();
	
	public static void main(String[] args) throws Exception {
		// Parse API words
		File dir = new File(LIBSRC);
		if (!dir.exists() || !dir.isDirectory())
			throw new RuntimeException("nope");
		parseApiDir(dir);
		System.out.println("api parsed");
		
		dir = new File(FOLDERPATH);
		if (!dir.exists() || !dir.isDirectory())
			throw new RuntimeException("nope");
		
		// Get CorpusVisitor result
		
		File corpusVisitorResult = new File("CorpusVisitorResult");
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
				corpusVisitorResult));
		CorpusVisitor visitor = (CorpusVisitor) ois.readObject();
		ois.close();
		
		//Parse corpus with CorpusVisitor
//		CorpusVisitor visitor = new CorpusVisitor(SWTPACKAGE);
//		parseVisitorDir(dir, visitor);
//		System.out.println("Corpus parsed.");
		
		System.out.println("API contains " + apiWordsTop.size() + " top words.");
		System.out.println("Corpus contains " + visitor.wordsTop.size() + " top words.");
		int count = 0;
		for(String word: visitor.wordsTop)
			if(apiWordsTop.contains(word))
				count++;
			else
				System.out.println("API does not contain top word: " + word);
		System.out.println("Corpus contains " + count + " api top words. " + (double)count/(double)apiWordsTop.size());
		System.out.println("API contains " + apiTypes.size() + " types.");
		System.out.println("CorpusVisitor found " + visitor.types.size() + " types.");
		count = 0;
		for(String type: visitor.types)
			if(apiTypes.contains(type))
				count++;
			else
				System.out.println("API does not contain type: " + type);
		System.out.println("Corpus contains " + count + " types. " + (double)count/(double)apiTypes.size());
		
		// Serialize CorpusVisitor result
//		ObjectOutputStream oos = null;
//		try {
//			File corpusVisitorResult = new File("CorpusVisitorResult");
//			corpusVisitorResult.createNewFile();
//			oos = new ObjectOutputStream(new FileOutputStream(corpusVisitorResult));
//			oos.writeObject(visitor);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//			if (oos != null)
//				try {
//					oos.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//		}
		
		// Get Analyzer
		
//		File analyzerFile = new File("Analyzer");
//		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(analyzerFile));
//		Analyzer analyzer = (Analyzer) ois.readObject();
//		ois.close();
		
		// Parse Corpus
//		dir = new File(FOLDERPATH);
//		if (!dir.exists() || !dir.isDirectory())
//			throw new RuntimeException("nope");
//
//		Analyzer analyzer = new Analyzer(SWTPACKAGE);
//		analyzer.parse(FOLDERPATH, LIBSRC);
//		System.out.println("Corpus parsed.");

		// Serialize Analyzer
//		ObjectOutputStream oos = null;
//		try {
//			File analyzerFile = new File("Analyzer");
//			analyzerFile.createNewFile();
//			oos = new ObjectOutputStream(new FileOutputStream(analyzerFile));
//			oos.writeObject(analyzer);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//			if (oos != null)
//				try {
//					oos.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//		}

		// Flat sentences into set of words
//		Set<String> wordsFound = new HashSet<>();
//		for (Sentence s : analyzer.getSentences())
//			for (Instruction inst : s.getInstructions())
//				wordsFound.add(inst.getWord());

		// Compare API words with words found in corpus
//		int foundCount = 0;
//		int foundTopCount = 0;
//		for (String word : wordsFound) {
//			if (apiWords.contains(word))
//				foundCount++;
//			if (apiWordsTop.contains(word))
//				foundTopCount++;
//		}
//		System.out.println("Found: " + foundCount + "/" + apiWords.size());
//		System.out.println("Found in top: " + foundTopCount + "/" + apiWordsTop.size());
//		System.out.println("Total sentences: " + analyzer.getNumberOfSentences());
//		System.out.println("Avg sentence size: " + analyzer.avgSentenceSize());
		
//		for (int i = 1; i <= 10; i++) {
//			int count = 0;
//			for(Sentence s : analyzer.getAllSentences())
//				if(s.getInstructions().size() == i)
//					count++;
//			System.out.println("With " + i + " words: " + count);
//		}
//		int countGreaterThanTen = 0;
//		for(Sentence s : analyzer.getAllSentences())
//			if(s.getInstructions().size() > 10)
//				countGreaterThanTen++;
//		System.out.println("With >10 words: " + countGreaterThanTen);
//		
//		for(Sentence s : analyzer.getAllSentences())
//			if(s.getInstructions().size() > 100)
//				System.out.println(s.getFilePath() + "\n" + s.getInstructions());
//		
//	}

//	public static void parseDir(File dir, Analyzer analyzer) {
//		for (File f : dir.listFiles())
//			if (f.isDirectory())
//				parseDir(f, analyzer);
//			else if (f.getName().endsWith(".java"))
//				parseFile(f.getAbsolutePath(), analyzer);
//	}
//
//	public static void parseFile(String file, Analyzer analyzer) {
//		BlockVisitorV3 visitor = null;
//		try {
//			visitor = new BlockVisitorV3(analyzer);
//			JavaSourceParser parser = JavaSourceParser.createFromFile(file, LIBSRC, "UTF-8");
//			parser.parse(visitor);
//		} catch (Exception e) {
//			System.out.println("ERROR IN FILE: " + file);
//			System.out.println(visitor.getInternalAnalyzer());
//			throw e;
//		}
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
			JavaSourceParser parser = JavaSourceParser.createFromFile(file, LIBSRC, "UTF-8");
			parser.parse(visitor);
		} catch (Exception e) {
			System.out.println("ERROR IN FILE: " + file);
			throw e;
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
			throw e;
		}
	}
}
