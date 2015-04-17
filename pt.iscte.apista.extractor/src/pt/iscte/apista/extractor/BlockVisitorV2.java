package pt.iscte.apista.extractor;


import java.util.LinkedHashMap;
import java.util.Map;

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
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import pt.iscte.apista.core.ConstructorInstruction;
import pt.iscte.apista.core.Instruction;
import pt.iscte.apista.core.MethodInstruction;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;





public class BlockVisitorV2 extends ASTVisitor {

	private int blockLine;
	//	private List<Instruction> instructions;
	private Analyzer analizer;

	private String packageRoot;

	public BlockVisitorV2() {
		this(new Analyzer(), -1, "");
	}

	/**
	 * Creates a visitor without code block restriction
	 */
	public BlockVisitorV2(Analyzer analizer) {
		this(analizer, -1, analizer.getPackageRoot());
	}

	public BlockVisitorV2(int blockLine) {
		this(new Analyzer(), blockLine, "");
	}

	/**
	 * Creates a visitor for the code block containing <code>blockline</code>
	 * @param blockLine number of the line contained in the block
	 */
	private BlockVisitorV2(Analyzer analizer, int blockLine, String packageRoot) {
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

		if(visit) {
			analizer.newSentence(getLineNumber(node));
			clear();
		}

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

	private boolean isInvocation(ASTNode node) {
		return node instanceof ClassInstanceCreation ||
				node instanceof MethodInvocation;
	}

	private Multimap<ASTNode, ASTNode> depsMap = ArrayListMultimap.create();
	private Map<ASTNode, Instruction> instMap = new LinkedHashMap<>();
	private Multimap<String, ASTNode> varMap = ArrayListMultimap.create();

	private void clear() {
		depsMap.clear();
		instMap.clear();
		varMap.clear();
	}
	
	private void addDependencies(ASTNode node, Instruction inst) {
		for(ASTNode d : depsMap.get(node)) {
			if(instMap.containsKey(d)) {
				inst.addDependency(instMap.get(d));
			}
		}
		
		for(Instruction prev : instMap.values()) {
			if(inst.getDependencies().contains(prev))
				inst.addDependencies(prev.getDependencies());
		
			for(Instruction d : inst.getDependencies().toArray(new Instruction[inst.getDependencies().size()]))
				if(prev.getDependencies().contains(d)) {
					inst.addDependency(prev);
					inst.addDependencies(prev.getDependencies());
				}
		}
	}

	private void addInstruction(ASTNode node, Instruction inst) {
		addDependencies(node, inst);
		analizer.addInstruction(inst);
		instMap.put(node, inst);
	}
	

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		Expression e = node.getInitializer();

		if(isInvocation(e)) {
			ITypeBinding binding = e.resolveTypeBinding();

			if(includeInstruction(e) && includePackage(binding))
				varMap.put(node.getName().getIdentifier(), e);
		}
		return super.visit(node);
	}

	
	

	@Override
	public boolean visit(ClassInstanceCreation node) {
		if(includePackage(node.resolveTypeBinding()) && isInvocation(node.getParent()))
			depsMap.put(node.getParent(), node);

		for(Object arg : node.arguments()) {
			if(arg instanceof SimpleName) {
				String var =  ((SimpleName) arg).getIdentifier();
				if(varMap.containsKey(var))
					depsMap.putAll(node, varMap.get(var));
			}
		}
		return super.visit(node);
	}


	@Override
	public boolean visit(MethodInvocation node) {
		if(includePackage(node.resolveTypeBinding()) && isInvocation(node))
			depsMap.put(node.getParent(), node);
		
		Expression e = node.getExpression();
		if(e instanceof SimpleName) {
			depsMap.putAll(node, varMap.get(((SimpleName) e).getIdentifier()));
		}
		return super.visit(node);
	}

	/*
	 * tanto na chamada a construtores como metodos, so eh adicionado no final (endVisit), 
	 * para que os argumentos sejam processados primeiro
	 */

	@Override
	public void endVisit(ClassInstanceCreation node) {
		ITypeBinding binding = node.resolveTypeBinding();

		if(includeInstruction(node) && includePackage(binding)) {
			Instruction inst = new ConstructorInstruction(node, binding);	
			addInstruction(node, inst);
		}
	}

	


	@Override
	public void endVisit(MethodInvocation node) {
		Expression e = node.getExpression();
		if(e != null) {
			ITypeBinding binding = e.resolveTypeBinding();
			if(includeInstruction(node) && includePackage(binding)) {
				Instruction inst = new MethodInstruction(node, binding);
				addInstruction(node, inst);
			}
		}
	}

}

