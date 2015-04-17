package pt.iscte.apista.extractor;


import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

import pt.iscte.apista.core.ConstructorInstruction;
import pt.iscte.apista.core.MethodInstruction;




public class BlockVisitorV1 extends ASTVisitor {

	private int blockLine;
	//	private List<Instruction> instructions;
	private Analyzer analizer;
	
	private String packageRoot;

	public BlockVisitorV1() {
		this(new Analyzer(), -1, "");
	}

	/**
	 * Creates a visitor without code block restriction
	 */
	public BlockVisitorV1(Analyzer analizer) {
		this(analizer, -1, analizer.getPackageRoot());
	}

	public BlockVisitorV1(int blockLine) {
		this(new Analyzer(), blockLine, "");
	}
	
	/**
	 * Creates a visitor for the code block containing <code>blockline</code>
	 * @param blockLine number of the line contained in the block
	 */
	private BlockVisitorV1(Analyzer analizer, int blockLine, String packageRoot) {
		this.blockLine = blockLine;
		this.analizer = analizer;
		this.packageRoot = packageRoot;
	}

	public Analyzer getAnalyzer() {
		return analizer;
	}

	private boolean includePackage(ITypeBinding typeBinding) {
		if(typeBinding!=null){
			IPackageBinding packageBinding = typeBinding.getPackage();
			if(packageBinding == null)
				return false;
			
			return packageBinding.getName().startsWith(packageRoot);
		}
		return false;
	}

	private static int getLineNumber(ASTNode node) {
		return ((CompilationUnit) node.getRoot()).getLineNumber(node.getStartPosition());	
	}

	private static int getEndLineNumber(ASTNode node) {
		return ((CompilationUnit) node.getRoot()).getLineNumber(node.getStartPosition()+node.getLength());
	}

//	private Instruction createInstruction(ASTNode node, String word) {
//		return new Instruction(node, word, getLineNumber(node));
//	}



	/*
	 *  so entra dentro de blocos de metodos/construtores - i.e. salta blocos de ifs e ciclos
	 */
	@Override
	public boolean visit(Block node) {

		// ideia: parametros como features

		// ideia: contexto eh aumentado em nested blocks
		boolean visit = node.getParent() instanceof MethodDeclaration && withinRange(node);

		if(visit)
			analizer.newSentence(getLineNumber(node));

		return visit;
	}

	private boolean withinRange(Block node) {
		int start = getLineNumber(node);
		int end = getEndLineNumber(node);
		return blockLine == -1 || blockLine >= start && blockLine <=end;
	}



	private boolean includeInstruction(ASTNode node) {
		return blockLine == -1 || getLineNumber(node) <= blockLine;
	}

	/*
	 * tanto na chamada a construtores como metodos, so eh adicionado no final (endVisit), 
	 * para que os argumentos sejam processados primeiro
	 */

	@Override
	public void endVisit(ClassInstanceCreation node) {
		ITypeBinding binding = node.resolveTypeBinding();
		if(includeInstruction(node) && includePackage(binding))
			analizer.addInstruction(new ConstructorInstruction(node, binding)); 
	}

	
	@Override
	public void endVisit(MethodInvocation node) {
		Expression e = node.getExpression();
		if(e != null) {
			ITypeBinding binding = e.resolveTypeBinding();
			if(includeInstruction(node) && includePackage(binding))
				analizer.addInstruction(new MethodInstruction(node, binding));
		}
	}

}

