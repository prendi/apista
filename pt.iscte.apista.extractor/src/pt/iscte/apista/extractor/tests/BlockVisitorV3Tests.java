package pt.iscte.apista.extractor.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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

public class BlockVisitorV3Tests {

	private static final char S = File.separatorChar;
	public static final String LIBSRC = "../swtsrc/src";
	public static final String SWTPACKAGE = "org.eclipse.swt";
	public static final String FILE = "." + S + "src" + S
			+ TestCodeSWT.class.getName().replace('.', S) + ".java";

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
		JavaSourceParser parser = JavaSourceParser.createFromFile(FILE, LIBSRC,
				"UTF-8");
		parser.parse(visitor);
		sentences = analyzer.getSentences().iterator();
		internalAnalyzer = visitor.getInternalAnalyzer();
	}
	
	@Test
	public void testAll(){
		System.out.println("=======================SENTENCES=======================");
		testEmpty();
		testSimple();
		testArg();
		testVariableReassignment();
		testInterlace();
		testDoWhile();
		testDoWhile2();
		testDoWhile3();
		testWhile1();
		testWhile2();
		testWhile3();
		testWhileInterlace();
		testFor1();
		testFor2();
		testFor3();
		testFor4();
		testFor5();
		testForEach1();
		System.out.println("last test");
		testForEach2();
	}

	public void testEmpty() {
		for(Sentence sentence: analyzer.getSentences()){
			assertFalse(sentence.isEmpty());
		}
	}

	public void testSimple() {
		List<TestInstruction> test = new ArrayList<>();
		test.add(new TestInstruction("Display.new")); // 0
		test.add(new TestInstruction("Shell.new", 0)); // 1
		test.add(new TestInstruction("RowLayout.new"));// 2
		test.add(new TestInstruction("Shell.setLayout", 0, 1, 2));// 3
		test.add(new TestInstruction("Button.new", 0, 1, 2, 3));// 4

		testNext(test);
	}
	
	public void testArg(){
		List<TestInstruction> test = new ArrayList<>();
		test.add(new TestInstruction("Display.new")); // 0
		test.add(new TestInstruction("Shell.new", 0)); // 1
		test.add(new TestInstruction("RowLayout.new"));// 2
		test.add(new TestInstruction("Shell.setLayout", 0, 1, 2));// 3

		testNext(test);
	}
	
	public void testVariableReassignment(){
		List<TestInstruction> test = new ArrayList<>();
		test.add(new TestInstruction("Display.new")); // 0
		test.add(new TestInstruction("Shell.new", 0)); // 1

		testNext(test);
		testNext(test);
	}
	
	
	public void testInterlace(){
		List<TestInstruction> test1 = new ArrayList<>();
		test1.add(new TestInstruction("Display.new")); //0
		test1.add(new TestInstruction("Shell.new", 0)); //1
		test1.add(new TestInstruction("RowLayout.new"));//2
		test1.add(new TestInstruction("Shell.setLayout", 0, 1, 2));//3
		test1.add(new TestInstruction("Button.new", 0, 1, 2, 3));//4
		test1.add(new TestInstruction("Button.setText", 0, 1, 2, 3, 4));//5
		
		List<TestInstruction> test2 = new ArrayList<>();
		test2.add(new TestInstruction("Display.getDefault")); //0
		test2.add(new TestInstruction("Shell.new", 0)); //1
		
		testNext(test2);
		testNext(test1);
	}
	
	public void testDoWhile(){
		List<TestInstruction> test = new ArrayList<>();
		test.add(new TestInstruction("Display.new")); //0
		test.add(new TestInstruction("Shell.new", 0)); //1
		test.add(new TestInstruction("Button.new", 0, 1));//2
		test.add(new TestInstruction("Button.setText", 0, 1, 2));//3
		test.add(new TestInstruction("Button.getText", 0, 1, 2, 3));//4
		
		testNext(test);
	}
	
	public void testDoWhile2(){
		List<TestInstruction> test = new ArrayList<>();
		test.add(new TestInstruction("Display.new")); //0
		test.add(new TestInstruction("Shell.new", 0)); //1
		test.add(new TestInstruction("Button.new", 0, 1));//2
		test.add(new TestInstruction("Button.setText", 0, 1, 2));//3
		
		testNext(test);
	}
	
	public void testDoWhile3(){
		List<TestInstruction> test = new ArrayList<>();
		test.add(new TestInstruction("Display.new")); //0
		test.add(new TestInstruction("Shell.new", 0)); //1
		test.add(new TestInstruction("Button.new", 0, 1));//2
		test.add(new TestInstruction("Button.setText", 0, 1, 2));//3
		test.add(new TestInstruction("Button.getText", 0, 1, 2, 3));//4
		test.add(new TestInstruction("Display.close", 0, 1, 2, 3, 4));//5
		
		testNext(test);
	}
	
	public void testWhile1(){
		List<TestInstruction> test1 = new ArrayList<>();
		test1.add(new TestInstruction("Display.new")); //0
		test1.add(new TestInstruction("Shell.new", 0)); //1
		test1.add(new TestInstruction("Button.new", 0, 1));//2
		test1.add(new TestInstruction("Button.getText", 0, 1, 2));
		
		List<TestInstruction> test2 = new ArrayList<>();
		test2.add(new TestInstruction("Display.new")); //0
		test2.add(new TestInstruction("Shell.new", 0)); //1
		test2.add(new TestInstruction("Button.new", 0, 1));//2
		test2.add(new TestInstruction("Button.getText", 0, 1, 2));//3
		test2.add(new TestInstruction("Button.setText", 0, 1, 2, 3));//4
		
		testNext(test1);
		testNext(test2);
	}
	
	public void testWhile2(){
		List<TestInstruction> test1 = new ArrayList<>();
		test1.add(new TestInstruction("Display.new")); //0
		test1.add(new TestInstruction("Shell.new", 0)); //1
		test1.add(new TestInstruction("Button.new", 0, 1));//2
		test1.add(new TestInstruction("Button.getText", 0, 1, 2));//3
		test1.add(new TestInstruction("Display.close", 0, 1, 2, 3));//4
		
		List<TestInstruction> test2 = new ArrayList<>();
		test2.add(new TestInstruction("Display.new")); //0
		test2.add(new TestInstruction("Shell.new", 0)); //1
		test2.add(new TestInstruction("Button.new", 0, 1));//2
		test2.add(new TestInstruction("Button.getText", 0, 1, 2));//3
		test2.add(new TestInstruction("Button.setText", 0, 1, 2, 3));//4
		test2.add(new TestInstruction("Display.close", 0, 1, 2, 3));//5
		
		testNext(test1);
		testNext(test2);
	}
	
	public void testWhile3(){
		List<TestInstruction> test1 = new ArrayList<>();
		test1.add(new TestInstruction("Display.new")); //0
		test1.add(new TestInstruction("Shell.new", 0)); //1
		test1.add(new TestInstruction("Button.new", 0, 1));//2
		test1.add(new TestInstruction("Button.getText", 0, 1, 2));//3
		test1.add(new TestInstruction("Display.close", 0, 1, 2, 3));//4
		
		List<TestInstruction> test2 = new ArrayList<>();
		test2.add(new TestInstruction("Display.new")); //0
		test2.add(new TestInstruction("Shell.new", 0)); //1
		test2.add(new TestInstruction("Button.new", 0, 1));//2
		test2.add(new TestInstruction("Button.getText", 0, 1, 2));//3
		test2.add(new TestInstruction("Button.setText", 0, 1, 2, 3));//4
		test2.add(new TestInstruction("Button.getVisible", 0, 1, 2, 3, 4));//5
		test2.add(new TestInstruction("Button.getAccessible", 0, 1, 2, 3, 4, 5));//6
		test2.add(new TestInstruction("Display.close", 0, 1, 2, 3));//7

		List<TestInstruction> test3 = new ArrayList<>();
		test3.add(new TestInstruction("Display.new")); //0
		test3.add(new TestInstruction("Shell.new", 0)); //1
		test3.add(new TestInstruction("Button.new", 0, 1));//2
		test3.add(new TestInstruction("Button.getText", 0, 1, 2));//3
		test3.add(new TestInstruction("Button.setText", 0, 1, 2, 3));//4
		test3.add(new TestInstruction("Button.getVisible", 0, 1, 2, 3, 4));//5
		test3.add(new TestInstruction("Button.setVisible", 0, 1, 2, 3, 4, 5));//6
		test3.add(new TestInstruction("Button.getAccessible", 0, 1, 2, 3, 4, 5));//7
		test3.add(new TestInstruction("Display.close", 0, 1, 2, 3));//8
		
		testNext(test1);
		testNext(test2);
		testNext(test3);
	}
	
	public void testWhileInterlace(){
		List<TestInstruction> test1 = new ArrayList<>();
		test1.add(new TestInstruction("Display.new")); //0
		test1.add(new TestInstruction("Shell.new", 0)); //1
		test1.add(new TestInstruction("Button.new", 0, 1));//2
		test1.add(new TestInstruction("Button.getText", 0, 1, 2));//3
		test1.add(new TestInstruction("Display.close", 0, 1, 2, 3));//4
		
		List<TestInstruction> test2 = new ArrayList<>();
		test2.add(new TestInstruction("Display.new")); //0
		test2.add(new TestInstruction("Shell.new", 0)); //1
		test2.add(new TestInstruction("Button.new", 0, 1));//2
		test2.add(new TestInstruction("Button.getText", 0, 1, 2));//3
		test2.add(new TestInstruction("Button.setText", 0, 1, 2, 3));//4
		test2.add(new TestInstruction("Display.close", 0, 1, 2, 3));//5
		
		List<TestInstruction> test3 = new ArrayList<>();
		test3.add(new TestInstruction("Display.getDefault")); //0
		test3.add(new TestInstruction("Shell.new", 0)); //1
		
		List<TestInstruction> test4 = new ArrayList<>();
		test4.add(new TestInstruction("Display.getDefault")); //0
		test4.add(new TestInstruction("Shell.new", 0)); //1
		test4.add(new TestInstruction("Shell.dispose", 0, 1)); //1
		
		
		testNext(test1);
		testNext(test3);
		testNext(test2);
		testNext(test4);
	}
	
	public void testFor1(){
		List<TestInstruction> test1 = new ArrayList<>();
		test1.add(new TestInstruction("Display.new")); //0
		test1.add(new TestInstruction("Shell.new", 0)); //1
		test1.add(new TestInstruction("Shell.getChildren", 0, 1));//2
		
		List<TestInstruction> test2 = new ArrayList<>();
		test2.add(new TestInstruction("Display.new")); //0
		test2.add(new TestInstruction("Shell.new", 0)); //1
		test2.add(new TestInstruction("Shell.getChildren", 0, 1));//2
		test2.add(new TestInstruction("Shell.dispose", 0, 1, 2));//3
		
		testNext(test1);
		testNext(test2);
	}
	
	public void testFor2(){
		List<TestInstruction> test1 = new ArrayList<>();
		test1.add(new TestInstruction("Display.new")); //0
		test1.add(new TestInstruction("Shell.new", 0)); //1
		test1.add(new TestInstruction("Shell.getChildren", 0, 1));//2
		test1.add(new TestInstruction("Display.dispose", 0, 1, 2));//3
		
		List<TestInstruction> test2 = new ArrayList<>();
		test2.add(new TestInstruction("Display.new")); //0
		test2.add(new TestInstruction("Shell.new", 0)); //1
		test2.add(new TestInstruction("Shell.getChildren", 0, 1));//2
		test2.add(new TestInstruction("Shell.getImages", 0, 1, 2));//3
		test2.add(new TestInstruction("Image.dispose", 0, 1, 2, 3));//4
		test2.add(new TestInstruction("Display.dispose", 0, 1, 2));//3
		
		testNext(test1);
		testNext(test2);
	}
	
	public void testFor3(){
		List<TestInstruction> test1 = new ArrayList<>();
		test1.add(new TestInstruction("Display.new")); //0
		test1.add(new TestInstruction("Shell.new", 0)); //1
		test1.add(new TestInstruction("Shell.getChildren", 0, 1));//2
		test1.add(new TestInstruction("Display.dispose", 0, 1, 2));//3
		
		List<TestInstruction> test2 = new ArrayList<>();
		test2.add(new TestInstruction("Display.new")); //0
		test2.add(new TestInstruction("Shell.new", 0)); //1
		test2.add(new TestInstruction("Shell.getChildren", 0, 1));//2
		test2.add(new TestInstruction("Shell.getChildren", 0, 1, 2));//3
		test2.add(new TestInstruction("Control.dispose", 0, 1, 2, 3));//4 | 2 doesn't work!!
		test2.add(new TestInstruction("Display.dispose", 0, 1, 2));//3
		
		testNext(test1);
		testNext(test2);
	}
	
	public void testFor4(){
		List<TestInstruction> test1 = new ArrayList<>();
		test1.add(new TestInstruction("Display.new"));
		test1.add(new TestInstruction("Shell.new", 0)); //1
		test1.add(new TestInstruction("Shell.getChildren", 0, 1));//2
		test1.add(new TestInstruction("Display.dispose", 0, 1, 2));//3
		
		List<TestInstruction> test2 = new ArrayList<>();
		test2.add(new TestInstruction("Display.new")); //0
		test2.add(new TestInstruction("Shell.new", 0)); //1
		test2.add(new TestInstruction("Shell.getChildren", 0, 1));//2
		test2.add(new TestInstruction("Shell.getChildren", 0, 1, 2));//3
		test2.add(new TestInstruction("Control.getListeners", 0, 1, 2, 3));//4
		test2.add(new TestInstruction("Display.dispose", 0, 1, 2));//5
		
		List<TestInstruction> test3 = new ArrayList<>();
		test3.add(new TestInstruction("Display.new")); //0
		test3.add(new TestInstruction("Shell.new", 0)); //1
		test3.add(new TestInstruction("Shell.getChildren", 0, 1));//2
		test3.add(new TestInstruction("Shell.getChildren", 0, 1, 2));//3
		test3.add(new TestInstruction("Control.getListeners", 0, 1, 2, 3));//4
		test3.add(new TestInstruction("Shell.getChildren", 0, 1, 2, 3, 4));//5
		test3.add(new TestInstruction("Control.getListeners", 0, 1, 2, 3, 4, 5));//6
		test3.add(new TestInstruction("Display.dispose", 0, 1, 2));//7
		
		testNext(test1);
		testNext(test2);
		testNext(test3);
	}
	
	public void testFor5(){
		List<TestInstruction> test1 = new ArrayList<>();
		test1.add(new TestInstruction("Display.new"));
		test1.add(new TestInstruction("Shell.new", 0)); //1
		test1.add(new TestInstruction("Shell.getChildren", 0, 1));//2
		test1.add(new TestInstruction("Display.dispose", 0, 1, 2));//3
		
		List<TestInstruction> test2 = new ArrayList<>();
		test2.add(new TestInstruction("Display.new")); //0
		test2.add(new TestInstruction("Shell.new", 0)); //1
		test2.add(new TestInstruction("Shell.getChildren", 0, 1));//2
		test2.add(new TestInstruction("Shell.getChildren", 0, 1, 2));//3
		test2.add(new TestInstruction("Control.getListeners", 0, 1, 2, 3));//4
		test2.add(new TestInstruction("Display.dispose", 0, 1, 2));//5
		
		List<TestInstruction> test3 = new ArrayList<>();
		test3.add(new TestInstruction("Display.new")); //0
		test3.add(new TestInstruction("Shell.new", 0)); //1
		test3.add(new TestInstruction("Shell.getChildren", 0, 1));//2
		test3.add(new TestInstruction("Shell.getChildren", 0, 1, 2));//3
		test3.add(new TestInstruction("Control.getListeners", 0, 1, 2, 3));//4
		test3.add(new TestInstruction("Control.getListeners", 0, 1, 2, 3, 4));//5
		test3.add(new TestInstruction("Display.dispose", 0, 1, 2));//6
		
		testNext(test1);
		testNext(test2);
		testNext(test3);
	}
	
	public void testForEach1(){
		List<TestInstruction> test1 = new ArrayList<>();
		test1.add(new TestInstruction("Display.new")); //0
		test1.add(new TestInstruction("Shell.new", 0)); //1
		test1.add(new TestInstruction("Shell.getChildren", 0, 1));//2
		
		List<TestInstruction> test2 = new ArrayList<>();
		test2.add(new TestInstruction("Display.new")); //0
		test2.add(new TestInstruction("Shell.new", 0)); //1
		test2.add(new TestInstruction("Shell.getChildren", 0, 1));//2
		test2.add(new TestInstruction("Control.dispose", 0, 1, 2));//3
		
		testNext(test1);
		testNext(test2);
	}
	
	public void testForEach2(){
		List<TestInstruction> test1 = new ArrayList<>();
		test1.add(new TestInstruction("Display.new")); //0
		test1.add(new TestInstruction("Shell.new", 0)); //1
		test1.add(new TestInstruction("Shell.getChildren", 0, 1));//2
		test1.add(new TestInstruction("Display.dispose", 0, 1, 2));
		
		List<TestInstruction> test2 = new ArrayList<>();
		test2.add(new TestInstruction("Display.new")); //0
		test2.add(new TestInstruction("Shell.new", 0)); //1
		test2.add(new TestInstruction("Shell.getChildren", 0, 1));//2
		test2.add(new TestInstruction("Control.getListeners", 0, 1, 2));//3
		test2.add(new TestInstruction("Display.dispose", 0, 1, 2));
		
		List<TestInstruction> test3 = new ArrayList<>();
		test3.add(new TestInstruction("Display.new")); //0
		test3.add(new TestInstruction("Shell.new", 0)); //1
		test3.add(new TestInstruction("Shell.getChildren", 0, 1));//2
		test3.add(new TestInstruction("Control.getListeners", 0, 1, 2));//3
		test3.add(new TestInstruction("Shell.addControlListener", 0, 1, 2, 3));//4
		test3.add(new TestInstruction("Display.dispose", 0, 1, 2));
		
		testNext(test1);
		testNext(test2);
		testNext(test3);
	}
	
	int i = 0;
	private void testNext(List<TestInstruction> test){
		Sentence sentence = sentences.next();
		List<InstructionLine> ilSentence = internalAnalyzer.get(i++);
		System.out.println(sentence);
		assertEquals(test.size(), sentence.getInstructions().size());
		assertEquals(test.size(), ilSentence.size());
		for (int i = 0; i < sentence.getInstructions().size(); i++){
			assertEquals("inst " + i, test.get(i).word, sentence.getInstructions().get(i).getWord());
			assertEquals("inst " + i, test.get(i).word, ilSentence.get(i).instruction.getWord());
			assertEquals("" + ilSentence.get(i).getDependencies(), test.get(i).deps.length, ilSentence.get(i).getDependencies().size());
			for(int depIndex: test.get(i).deps){
				assertEquals(true, ilSentence.get(i).getDependencies().contains(ilSentence.get(depIndex)));
			}
		}
	}
}