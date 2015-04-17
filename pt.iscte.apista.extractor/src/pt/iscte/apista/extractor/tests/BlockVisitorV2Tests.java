package pt.iscte.apista.extractor.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.junit.Before;
import org.junit.Test;

import pt.iscte.apista.core.Instruction;
import pt.iscte.apista.core.Sentence;
import pt.iscte.apista.extractor.Analyzer;
import pt.iscte.apista.extractor.BlockVisitorV2;
import pt.iscte.apista.extractor.JavaSourceParser;


public class BlockVisitorV2Tests {

	private static final char S = File.separatorChar;
	public static final String LIBSRC = "../swtsrc/src";
	public static final String SWTPACKAGE = "org.eclipse.swt";
	public static final String FILE =
			"." + S + "src" + S + BlockVisitorV2Tests.class.getName().replace('.', S) + ".java";

	private static class TestInst {
		final String word;
		final int[] deps;
		public TestInst(String word, int[] deps) {
			this.word = word;
			this.deps = deps;
		}
	}

	public void testMethod2() {

	}

	private static List<List<TestInst>> test = new ArrayList<List<TestInst>>();
	private static int testI = -1;

	private static void newTest() {
		test.add(new ArrayList<TestInst>());
		testI++;
	}
	private static void t(String word, int ... deps) {
		test.get(testI).add(new TestInst(word, deps));
	}



	public void testMethod() {
		Display d = new Display();
		Shell s = new Shell(d);
		Shell s2 = new Shell(Display.getDefault());
		s.setLayout(new RowLayout());
		Button a = new Button(s, SWT.PUSH);
		String aText = "A";
		a.setText(aText);
	}

	static {
		newTest();
		t("Display.new"); //0
		t("Shell.new", 0); //1
		t("Display.getDefault"); //2
		t("Shell.new", 2); //3
		t("RowLayout.new");//4
		t("Shell.setLayout", 0, 1, 4);//5
		t("Button.new", 0, 1, 4, 5);//6
		t("Button.setText", 0, 1, 4, 5, 6);//7
	}

	public void testInterlace() {
		Shell s1 = new Shell();
		Text t1 = new Text(s1, SWT.NONE);
		Shell s2 = new Shell();
		Text t2 = new Text(s2, SWT.NONE);
		t1.setText("t1");
		t2.setText("t2");
		t1.setEnabled(true);
		t2.setEnabled(false);
	}



	static {
		newTest();
		t("Shell.new");//0
		t("Text.new", 0);//1
		t("Shell.new");//2
		t("Text.new", 2);//3
		t("Text.setText",0,1);//4
		t("Text.setText",2,3);//5
		t("Text.setEnabled",0,1,4);//6
		t("Text.setEnabled",2,3,6);//7
	}

	private Analyzer analyzer;

	@Before
	public void setup() {
		analyzer = new Analyzer();
		analyzer.run(SWTPACKAGE, FILE, LIBSRC);
		BlockVisitorV2 visitor = new BlockVisitorV2(analyzer);
		JavaSourceParser parser = JavaSourceParser.createFromFile(FILE, LIBSRC, "UTF-8");
		parser.parse(visitor);
//		analyzer.finish();
	}

	@Test
	public void testInstructions() {
		List<Sentence> sentences = analyzer.getSentences();
		
		for(int j = 0; j < test.size(); j++) {
			Sentence s = sentences.get(j);
			assertEquals(test.get(j).size(), s.getInstructions().size());

			List<TestInst> list = test.get(j);
			for(int i = 0; i < s.getInstructions().size(); i++)
				assertEquals("inst " + i, list.get(i).word, s.getInstructions().get(i).getWord()) ;
		}
	}

	@Test
	public void testDependencies() {
		List<Sentence> sentences = analyzer.getSentences();
		
		for(int j = 0; j < test.size(); j++) {
			Sentence s = sentences.get(j);
			List<TestInst> list = test.get(j);

			for(int i = 0; i < s.getInstructions().size(); i++) {
				Instruction inst = s.getInstructions().get(i);
				System.out.println(inst.getWord() + " -> " + Arrays.toString(inst.getDependencies().toArray()));
				assertEquals(list.get(i).word, list.get(i).deps.length, inst.getDependencies().size());

				boolean ok = list.get(i).deps.length == 0;
				for(int x : list.get(i).deps) {
					String w = list.get(x).word;
					for(Instruction d : inst.getDependencies()) {
						if(d.getWord().equals(w))
							ok = true;
					}
				}
				assertTrue(ok);
			}
		}

	}


}
