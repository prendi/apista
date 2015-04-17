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
/**
 * Since "if" constructs are the most sophisticated, this whole class
 * is dedicated to test cases involving functions containing "ifs".
 * 
 * This is currently just a placeholder for all the tests already
 * written. It is not expected to work as of now.
 *
 */
public class BlockVisitorV3IfTests {
	private static final char S = File.separatorChar;
	public static final String LIBSRC = "../swtsrc/src";
	public static final String SWTPACKAGE = "org.eclipse.swt";
	public static final String FILE = "." + S + "src" + S
			+ TestCodeIfSWT.class.getName().replace('.', S) + ".java";

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
		testIf1();
		testIf2();
		testIf3();
		testIf4();
		testIf5();
		testIf6();
		testIfElse();
		testIfElse2();
		testIfElse3();
		testIfElse4();
		testIfElse5();
		testIfElse6();
		testIfElse7();
		testElse1();
		testIfJoin();
		System.out.println("last test");
		testElseJoin();
	}
	
	public void testIf1(){
		List<TestInstruction> test1 = new ArrayList<>();
		test1.add(new TestInstruction("Display.new")); //0
		test1.add(new TestInstruction("Shell.new", 0)); //1
		test1.add(new TestInstruction("Button.new", 0, 1));//2
		test1.add(new TestInstruction("Button.getText", 0, 1, 2));//3
		test1.add(new TestInstruction("Button.setText", 0, 1, 2, 3));
		
		List<TestInstruction> test2 = new ArrayList<>();
		test2.add(new TestInstruction("Display.new")); //0
		test2.add(new TestInstruction("Shell.new", 0)); //1
		test2.add(new TestInstruction("Button.new", 0, 1));//2
		test2.add(new TestInstruction("Button.getText", 0, 1, 2));
		
		
		testNext(test2);
		testNext(test1);
	}
	
	public void testIf2(){
		List<TestInstruction> test1 = new ArrayList<>();
		test1.add(new TestInstruction("Display.new")); //0
		
		testNext(test1);
	}
	
	public void testIf3(){
		List<TestInstruction> test1 = new ArrayList<>();
		test1.add(new TestInstruction("Display.new")); //0
		
		List<TestInstruction> test2 = new ArrayList<>();
		test2.add(new TestInstruction("Display.new")); //0
		test2.add(new TestInstruction("Shell.new", 0)); //1
		
		testNext(test1);
		testNext(test2);
	}
	
	public void testIf4(){
		List<TestInstruction> test1 = new ArrayList<>();
		test1.add(new TestInstruction("Display.new")); //0
		
		List<TestInstruction> test2 = new ArrayList<>();
		test2.add(new TestInstruction("Display.new")); //0
		test2.add(new TestInstruction("Shell.new", 0)); //1
		test2.add(new TestInstruction("Button.new", 0, 1)); //2
		
		testNext(test1);
		testNext(test2);
	}
	
	public void testIf5(){
		List<TestInstruction> test1 = new ArrayList<>();
		test1.add(new TestInstruction("Display.new")); //0
		test1.add(new TestInstruction("Shell.new", 0)); //1		
		
		List<TestInstruction> test2 = new ArrayList<>();
		test2.add(new TestInstruction("Display.new")); //0
		test2.add(new TestInstruction("Shell.new", 0)); //1
		test2.add(new TestInstruction("Button.new", 0, 1)); //2
		test2.add(new TestInstruction("Button.setText", 0, 1, 2)); //3
		
		List<TestInstruction> test3 = new ArrayList<>();
		test3.add(new TestInstruction("Display.getDefault")); //0
		test3.add(new TestInstruction("Shell.new", 0)); //1	
		
		List<TestInstruction> test4 = new ArrayList<>();
		test4.add(new TestInstruction("Display.getDefault")); //0
		test4.add(new TestInstruction("Shell.new", 0)); //1
		test4.add(new TestInstruction("Button.new", 0, 1)); //2
		test4.add(new TestInstruction("Button.setText", 0, 1, 2)); //3
		
		testNext(test1);
		testNext(test3);
		testNext(test2);
		testNext(test4);
	}
	
	public void testIf6(){ testIf5(); }
	
	public void testIfElse(){
		List<TestInstruction> test1 = new ArrayList<>();
		test1.add(new TestInstruction("Display.new")); //0
		test1.add(new TestInstruction("Shell.new", 0)); //1
		test1.add(new TestInstruction("Shell.isEnabled", 0, 1)); //2
		test1.add(new TestInstruction("Button.new", 0, 1, 2)); //3
		
		testNext(test1);
		testNext(test1);
	}
	
	public void testIfElse2(){
		List<TestInstruction> test1 = new ArrayList<>();
		test1.add(new TestInstruction("Display.new")); //0
		test1.add(new TestInstruction("Shell.new", 0)); //1
		test1.add(new TestInstruction("Button.new", 0, 1)); //2
		test1.add(new TestInstruction("Shell.isEnabled", 0, 1, 2)); //3
		test1.add(new TestInstruction("Button.new", 0, 1, 2, 3)); //2
		
		List<TestInstruction> test2 = new ArrayList<>();
		test2.add(new TestInstruction("Display.new")); //0
		test2.add(new TestInstruction("Shell.new", 0)); //1
		test2.add(new TestInstruction("Button.new", 0, 1)); //2
		test2.add(new TestInstruction("Shell.isEnabled", 0, 1, 2)); //3
		test2.add(new TestInstruction("Button.setText", 0, 1, 2, 3)); //3
		
		testNext(test1);
		testNext(test2);
	}
	
	public void testIfElse3(){
		List<TestInstruction> test1 = new ArrayList<>();
		test1.add(new TestInstruction("Display.new")); //0
		test1.add(new TestInstruction("Shell.new", 0)); //1
		test1.add(new TestInstruction("Button.new", 0, 1)); //2
		test1.add(new TestInstruction("Shell.isEnabled", 0, 1, 2)); //3
		test1.add(new TestInstruction("Button.new", 0, 1, 2, 3)); //2
		test1.add(new TestInstruction("Display.dispose", 0, 1, 2, 3));
		
		List<TestInstruction> test2 = new ArrayList<>();
		test2.add(new TestInstruction("Display.new")); //0
		test2.add(new TestInstruction("Shell.new", 0)); //1
		test2.add(new TestInstruction("Button.new", 0, 1)); //2
		test2.add(new TestInstruction("Shell.isEnabled", 0, 1, 2)); //3
		test2.add(new TestInstruction("Button.setText", 0, 1, 2, 3)); //3
		test2.add(new TestInstruction("Display.dispose", 0, 1, 2, 3));
		
		testNext(test1);
		testNext(test2);
	}
	
	public void testIfElse4(){
		List<TestInstruction> test1 = new ArrayList<>();
		test1.add(new TestInstruction("Display.new")); //0
		test1.add(new TestInstruction("Shell.new", 0)); //1
		test1.add(new TestInstruction("Button.new", 0, 1)); //2
		test1.add(new TestInstruction("Shell.isEnabled", 0, 1, 2)); //3
		test1.add(new TestInstruction("Button.new", 0, 1, 2, 3)); //4
		test1.add(new TestInstruction("Shell.forceFocus", 0, 1, 2, 3));
		
		List<TestInstruction> test2 = new ArrayList<>();
		test2.add(new TestInstruction("Display.new")); //0
		test2.add(new TestInstruction("Shell.new", 0)); //1
		test2.add(new TestInstruction("Button.new", 0, 1)); //2
		test2.add(new TestInstruction("Shell.isEnabled", 0, 1, 2)); //3
		test2.add(new TestInstruction("Button.setText", 0, 1, 2, 3)); //4
		test2.add(new TestInstruction("Shell.forceFocus", 0, 1, 2, 3));//5
		
		List<TestInstruction> test3 = new ArrayList<>();
		test3.addAll(test1);
		test3.add(new TestInstruction("Shell.addKeyListener", 0, 1, 2, 3, 5));
		
		List<TestInstruction> test4 = new ArrayList<>();
		test4.addAll(test2);
		test4.add(new TestInstruction("Shell.addKeyListener", 0, 1, 2, 3, 5));
		
		testNext(test1);
		testNext(test2);
		testNext(test3);
		testNext(test4);
	}
	
	public void testIfElse5(){
		List<TestInstruction> test1 = new ArrayList<>();
		test1.add(new TestInstruction("Display.new")); //0
		test1.add(new TestInstruction("Shell.new", 0)); //1
		test1.add(new TestInstruction("Button.new", 0, 1)); //2
		test1.add(new TestInstruction("Shell.isEnabled", 0, 1, 2)); //3
		test1.add(new TestInstruction("Button.new", 0, 1, 2, 3)); //4
		test1.add(new TestInstruction("Shell.forceFocus", 0, 1, 2, 3));//5
		test1.add(new TestInstruction("Shell.addControlListener", 0, 1, 2, 3, 5));
		
		List<TestInstruction> test2 = new ArrayList<>();
		test2.add(new TestInstruction("Display.new")); //0
		test2.add(new TestInstruction("Shell.new", 0)); //1
		test2.add(new TestInstruction("Button.new", 0, 1)); //2
		test2.add(new TestInstruction("Shell.isEnabled", 0, 1, 2)); //3
		test2.add(new TestInstruction("Button.setText", 0, 1, 2, 3)); //4
		test2.add(new TestInstruction("Shell.forceFocus", 0, 1, 2, 3));//5
		test2.add(new TestInstruction("Shell.addControlListener", 0, 1, 2, 3, 5));
		
		List<TestInstruction> test3 = new ArrayList<>();
		test3.add(new TestInstruction("Display.new")); //0
		test3.add(new TestInstruction("Shell.new", 0)); //1
		test3.add(new TestInstruction("Button.new", 0, 1)); //2
		test3.add(new TestInstruction("Shell.isEnabled", 0, 1, 2)); //3
		test3.add(new TestInstruction("Button.new", 0, 1, 2, 3)); //4
		test3.add(new TestInstruction("Shell.forceFocus", 0, 1, 2, 3));//5
		test3.add(new TestInstruction("Shell.addKeyListener", 0, 1, 2, 3, 5));
		
		List<TestInstruction> test4 = new ArrayList<>();
		test4.add(new TestInstruction("Display.new")); //0
		test4.add(new TestInstruction("Shell.new", 0)); //1
		test4.add(new TestInstruction("Button.new", 0, 1)); //2
		test4.add(new TestInstruction("Shell.isEnabled", 0, 1, 2)); //3
		test4.add(new TestInstruction("Button.setText", 0, 1, 2, 3)); //4
		test4.add(new TestInstruction("Shell.forceFocus", 0, 1, 2, 3));//5
		test4.add(new TestInstruction("Shell.addKeyListener", 0, 1, 2, 3, 5));
		
	
		testNext(test1);
		testNext(test2);
		testNext(test3);
		testNext(test4);
	}
	
	public void testIfElse6(){
		List<TestInstruction> test1 = new ArrayList<>();
		test1.add(new TestInstruction("Display.new")); //0
		test1.add(new TestInstruction("Shell.new", 0)); //1
		test1.add(new TestInstruction("Button.new", 0, 1)); //2
		test1.add(new TestInstruction("Shell.isEnabled", 0, 1, 2)); //3
		test1.add(new TestInstruction("Button.new", 0, 1, 2, 3)); //4
		
		List<TestInstruction> test2 = new ArrayList<>();
		test2.add(new TestInstruction("Display.new")); //0
		test2.add(new TestInstruction("Shell.new", 0)); //1
		test2.add(new TestInstruction("Button.new", 0, 1)); //2
		test2.add(new TestInstruction("Shell.isEnabled", 0, 1, 2)); //3
		test2.add(new TestInstruction("Shell.forceFocus", 0, 1, 2, 3)); //4
		test2.add(new TestInstruction("Shell.addControlListener", 0, 1, 2, 3, 4));
		
		List<TestInstruction> test3 = new ArrayList<>();
		test3.add(new TestInstruction("Display.new")); //0
		test3.add(new TestInstruction("Shell.new", 0)); //1
		test3.add(new TestInstruction("Button.new", 0, 1)); //2
		test3.add(new TestInstruction("Shell.isEnabled", 0, 1, 2)); //3
		test3.add(new TestInstruction("Shell.forceFocus", 0, 1, 2, 3)); //4
		test3.add(new TestInstruction("Shell.addKeyListener", 0, 1, 2, 3, 4));
		
		testNext(test1);
		testNext(test2);
		testNext(test3);
	}
	
	public void testIfElse7(){
		List<TestInstruction> test1 = new ArrayList<>();
		test1.add(new TestInstruction("Display.new")); //0
		test1.add(new TestInstruction("Shell.new", 0)); //1
		test1.add(new TestInstruction("Button.new", 0, 1)); //2
		test1.add(new TestInstruction("Shell.isEnabled", 0, 1, 2)); //3
		test1.add(new TestInstruction("Shell.forceFocus", 0, 1, 2, 3)); //4
		test1.add(new TestInstruction("Shell.addControlListener", 0, 1, 2, 3, 4)); //5
		
		List<TestInstruction> test2 = new ArrayList<>();
		test2.add(new TestInstruction("Display.new")); //0
		test2.add(new TestInstruction("Shell.new", 0)); //1
		test2.add(new TestInstruction("Button.new", 0, 1)); //2
		test2.add(new TestInstruction("Shell.isEnabled", 0, 1, 2)); //3
		test2.add(new TestInstruction("Shell.forceFocus", 0, 1, 2, 3)); //4
		test2.add(new TestInstruction("Shell.addKeyListener", 0, 1, 2, 3, 4));
		
		List<TestInstruction> test3 = new ArrayList<>();
		test3.add(new TestInstruction("Display.new")); //0
		test3.add(new TestInstruction("Shell.new", 0)); //1
		test3.add(new TestInstruction("Button.new", 0, 1)); //2
		test3.add(new TestInstruction("Shell.isEnabled", 0, 1, 2)); //3
		test3.add(new TestInstruction("Button.setText", 0, 1, 2, 3)); //4
		
		testNext(test1);
		testNext(test3);
		testNext(test2);
	}
	
	public void testElse1(){
		List<TestInstruction> test1 = new ArrayList<>();
		test1.add(new TestInstruction("Display.new")); //0
		test1.add(new TestInstruction("Shell.new", 0)); //1
		test1.add(new TestInstruction("Button.new", 0, 1)); //2
		test1.add(new TestInstruction("Shell.isEnabled", 0, 1, 2)); //3
		test1.add(new TestInstruction("Button.setText", 0, 1, 2, 3)); //4
		
		List<TestInstruction> test2 = new ArrayList<>();
		test2.add(new TestInstruction("Display.new")); //0
		test2.add(new TestInstruction("Shell.new", 0)); //1
		test2.add(new TestInstruction("Button.new", 0, 1)); //2
		test2.add(new TestInstruction("Shell.isEnabled", 0, 1, 2)); //3
		
		testNext(test1);
		testNext(test2);
	}
	
	public void testIfJoin(){
		List<TestInstruction> test1 = new ArrayList<>();
		test1.add(new TestInstruction("Button.new")); //0
		
		List<TestInstruction> test2 = new ArrayList<>();
		test2.add(new TestInstruction("Color.new")); //0
		
		List<TestInstruction> test4 = new ArrayList<>();
		test4.add(new TestInstruction("Button.new")); //0
		test4.add(new TestInstruction("Color.new")); //1
		test4.add(new TestInstruction("Button.setBackground", 0, 1)); //2
		
		testNext(test1);
		testNext(test2);
		testNext(test4);
	}
	
	private void testElseJoin() {
		testIfJoin();
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
