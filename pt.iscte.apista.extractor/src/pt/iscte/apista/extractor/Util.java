package pt.iscte.apista.extractor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import pt.iscte.apista.core.IAnalyzer;
import pt.iscte.apista.core.SystemConfiguration;

public class Util {

	public static void run(boolean randomizeSentences) throws Exception {

		IAnalyzer analyzer = new Analyzer();

		SystemConfiguration configuration = new SystemConfiguration(
				"C:/Users/Gonçalo/Dropbox/Thesis/Code/Resources/config.properties");

		configuration.dumpProperties(System.out);
//		analyzer.run(configuration);
//		
////		analyzer = configuration.getAnalyzer();
//		
//		System.out.println(analyzer.getAllSentences().size());
//		
//		if (randomizeSentences)
//			analyzer.randomizeSentences(2015);
//
//		// System.out.println("parse time: " + analyzer.getTime() + " secs");
//		// System.out.println("sentences: " + analyzer.getNumberOfSentences());
//		// System.out.println("\tmin: " + analyzer.minSentenceSize());
//		// System.out.println("\tmax: " + analyzer.maxSentenceSize());
//		// System.out.println("\tavg: " + analyzer.avgSentenceSize());
//		// System.out.println("classes: " + analyzer.getClasses().size());
//		// System.out.println("words: " + analyzer.getNumberOfWords());
//		// System.out.println("total occurrences: "
//		// + analyzer.getTotalOccurrences());
//
//		FileOutputStream fos = new FileOutputStream(new File(configuration.getResourceFolder() + configuration.getAnalyzerFileName()));
//		ObjectOutputStream oos = new ObjectOutputStream(fos);
//		oos.writeObject(analyzer);
//		oos.close();

	}

	public static void main(String[] args) throws Exception {
		run(true);
	}

}
