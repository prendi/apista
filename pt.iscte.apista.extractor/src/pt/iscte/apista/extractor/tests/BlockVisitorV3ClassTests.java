package pt.iscte.apista.extractor.tests;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import pt.iscte.apista.core.Sentence;
import pt.iscte.apista.extractor.Analyzer;
import pt.iscte.apista.extractor.BlockVisitorV3;
import pt.iscte.apista.extractor.BlockVisitorV3.InstructionLine;
import pt.iscte.apista.extractor.JavaSourceParser;

public class BlockVisitorV3ClassTests {
	private static final char S = File.separatorChar;
	public static final String LIBSRC = "../swtsrc/src";
	public static final String SWTPACKAGE = "org.eclipse.swt";
	public static final String FILE = "." + S + "src" + S + TestCodeClassSWT.class.getName().replace('.', S) + ".java";

	private static class TestInstruction {
		final String word;
		final int[] deps;

		public TestInstruction(String word, int... deps) {
			this.word = word;
			this.deps = deps;
		}
	}

	private Analyzer analyzer;
	private List<List<InstructionLine>> internalAnalyzer;
	private Iterator<Sentence> sentences;

	@Before
	public void setup() {
		analyzer = new Analyzer();
		analyzer.run(SWTPACKAGE, FILE, LIBSRC);
		BlockVisitorV3 visitor = new BlockVisitorV3(analyzer);
		JavaSourceParser parser = JavaSourceParser.createFromFile(FILE, LIBSRC, "UTF-8");
		parser.parse(visitor);
		sentences = analyzer.getSentences().iterator();
		internalAnalyzer = visitor.getInternalAnalyzer();
	}

	@Test
	public void allTests() {
		testLocal();
		testAnonymousMethod();
		testAnonymousContainer();
	}

	public void testLocal() {
		List<TestInstruction> test = new ArrayList<>();
		test.add(new TestInstruction("Display.new")); // 0
		test.add(new TestInstruction("Shell.new", 0)); // 1
		test.add(new TestInstruction("RowLayout.new"));// 2
		test.add(new TestInstruction("Shell.setLayout", 0, 1, 2));// 3
		test.add(new TestInstruction("Button.new", 0, 1, 2, 3));// 4

		testAnalyzerNext(test);
	}

	public void testAnonymousContainer() {
		List<TestInstruction> test = new ArrayList<>();
		test.add(new TestInstruction("Display.getDefault")); // 0
		test.add(new TestInstruction("Shell.new", 0)); // 1
		test.add(new TestInstruction("Button.new", 0, 1));// 2
		test.add(new TestInstruction("Button.addControlListener", 0, 1, 2));// 2

		testNext(test);
	}

	public void testAnonymousMethod() {
		List<TestInstruction> test1 = new ArrayList<>();
		test1.add(new TestInstruction("Display.new")); // 0
		test1.add(new TestInstruction("Shell.new", 0)); // 1
		test1.add(new TestInstruction("Shell.isEnabled", 0, 1)); // 2
		test1.add(new TestInstruction("Button.new", 0, 1, 2)); // 3

		testAnalyzerNext(test1);
		testAnalyzerNext(test1);
	}

	public void testAnalyzerNext(List<TestInstruction> test) {
		Sentence sentence = sentences.next();
		System.out.println(sentence);
		assertEquals(test.size(), sentence.getInstructions().size());
	}

	int i = 0;

	private void testNext(List<TestInstruction> test) {
		Sentence sentence = sentences.next();
		List<InstructionLine> ilSentence = internalAnalyzer.get(i++);
		System.out.println(sentence);
		assertEquals(test.size(), sentence.getInstructions().size());
		assertEquals(test.size(), ilSentence.size());
		for (int i = 0; i < sentence.getInstructions().size(); i++) {
			assertEquals("inst " + i, test.get(i).word, sentence.getInstructions().get(i).getWord());
			assertEquals("inst " + i, test.get(i).word, ilSentence.get(i).instruction.getWord());
			assertEquals("" + ilSentence.get(i).getDependencies(), test.get(i).deps.length,
					ilSentence.get(i).getDependencies().size());
			for (int depIndex : test.get(i).deps) {
				assertEquals(true, ilSentence.get(i).getDependencies().contains(ilSentence.get(depIndex)));
			}
		}
	}
}
