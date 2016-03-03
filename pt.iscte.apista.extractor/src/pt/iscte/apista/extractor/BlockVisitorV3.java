package pt.iscte.apista.extractor;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.WhileStatement;

import pt.iscte.apista.core.ConstructorInstruction;
import pt.iscte.apista.core.Instruction;
import pt.iscte.apista.core.MethodInstruction;
import pt.iscte.apista.core.Sentence;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

public class BlockVisitorV3 extends ASTVisitor{
	private Analyzer analyzer;
	private List<List<InstructionLine>> internalAnalyzer = new LinkedList<>();
	private String packageRoot;
	private int startLine = -1;
	private int endLine = -1;
	
	private int blockLine = -1;
	
	private int currentLine = - 1;
	
	public BlockVisitorV3(Analyzer analizer) {
		this(analizer, analizer.getPackageRoot());
	}
	
	public BlockVisitorV3(int line) {
		this(new Analyzer(), "");
		blockLine = line;
		
	}
	
	public BlockVisitorV3(Analyzer analyzer, String packageRoot){
		this.analyzer = analyzer;
		this.packageRoot = packageRoot;
	}
	
	private int conditionalNesting = 0;
	private Multimap<ASTNode, ASTNode> depsMap = ArrayListMultimap.create();
	private Map<ASTNode, InstructionLine> instMap = new LinkedHashMap<>();
	private Map<InstructionLine, Integer> nestingMap = new HashMap<>();
	//private Multimap<String, ASTNode> varMap = ArrayListMultimap.create();
	private Map<String, ASTNode> varMap = Maps.newHashMap();

	private Multimap<IfStatement, InstructionLine> ifMap = ArrayListMultimap.create();
	private Multimap<IfStatement, InstructionLine> elseMap = ArrayListMultimap.create();
	private Stack<IfStatement> ifElseStack = new Stack<>();
	private Stack<Boolean> withinThenStack = new Stack<>();
	private boolean withinIfBlock = false;
	
	private List<String> existingImports = new ArrayList<String>();
	private Map<String, String> existingVariables = new LinkedHashMap<String, String>();
	
	private void clear() {
		depsMap.clear();
		instMap.clear();
		nestingMap.clear();
		varMap.clear();
		
		ifMap.clear();
		elseMap.clear();
		ifElseStack.clear();
		withinThenStack.clear();
		withinIfBlock = false;
	}
	
	public Analyzer getAnalyzer(){ 
		return analyzer;
	}
	
	@Override
	public void preVisit(ASTNode node) {
		currentLine = getLineNumber(node);
		if(withinRange(node) && isNestedStatement(node)){
			conditionalNesting++;
		}
		if(node instanceof EnhancedForStatement){
			SimpleName nodeName = ((EnhancedForStatement) node).getParameter().getName();
			if(includePackage(nodeName.resolveTypeBinding()))
				varMap.put(nodeName.getIdentifier(), ((EnhancedForStatement) node).getExpression());
		}
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
	
	@Override
	public boolean visit(Block node){
		boolean visit = 
				node.getParent() instanceof MethodDeclaration || withinRange(node)
				&& (node.getParent() instanceof DoStatement || isNestedStatement(node.getParent()))
				|| (node.getParent() instanceof IfStatement && withinIfBlock);

		if (node.getParent() instanceof MethodDeclaration) {
			clear();
			startLine = getLineNumber(node);
			endLine = getEndLineNumber(node);
			
			if(blockLine != -1 && (blockLine < startLine || blockLine > endLine))//|| endLine-startLine > 200
				return false;
		}
		return visit;
	}
	
	private void addDependencies(ASTNode node, InstructionLine inst) {
		for(ASTNode d : depsMap.get(node)) {
			if(instMap.containsKey(d)) {
				inst.addDependency(instMap.get(d));
			}
		}
		for (InstructionLine prev : instMap.values()) {
			if (canDependOn(inst, prev)) {
				if (inst.getDependencies().contains(prev))
					inst.addDependencies(prev.getDependencies());

				for (InstructionLine d : inst.getDependencies().toArray(
						new InstructionLine[inst.getDependencies().size()]))
					if (prev.getDependencies().contains(d)) {
						inst.addDependency(prev);
						inst.addDependencies(prev.getDependencies());
					}
			}
		}
	}
	
	private boolean canDependOn(InstructionLine inst, InstructionLine prev){
		boolean nesting = nestingMap.get(prev) <= conditionalNesting;
		if(!nesting) return false;
		
		boolean prevWithinMaps = ifMap.containsValue(prev) || elseMap.containsValue(prev);
		
		if(prevWithinMaps && !ifElseStack.isEmpty()){
			IfStatement prevKey = ifElseMapsKeyOf(prev);
			boolean stackContainsPrevKey = ifElseStack.contains(prevKey);
			if(stackContainsPrevKey && ifElseStack.indexOf(prevKey) < withinThenStack.size()){
				return (ifMap.containsValue(prev) && withinThenStack.get(ifElseStack.indexOf(prevKey)))
						|| (elseMap.containsValue(prev) && !withinThenStack.get(ifElseStack.indexOf(prevKey)));
			} else
				return false;
		}else
			return true;
	}
	
	private IfStatement ifElseMapsKeyOf(InstructionLine inst){
		assert ifMap.containsValue(inst) || elseMap.containsValue(inst); 
		if(ifMap.containsValue(inst))
			for (IfStatement st: ifMap.keySet()) {
				if(ifMap.get(st).contains(inst))
					return st;
			}
		if(elseMap.containsValue(inst))
			for (IfStatement st: elseMap.keySet()) {
				if(elseMap.get(st).contains(inst))
					return st;
			}
		return null;
	}

	private void addInstruction(ASTNode node, InstructionLine inst) {
		// skip if after code completion request line
		if(blockLine != -1 && inst.line > blockLine)
			return;
		
		addDependencies(node, inst);
		if(withinIfBlock)
			if(withinThenStack.peek())
				ifMap.put(ifElseStack.peek(), inst);
			else
				elseMap.put(ifElseStack.peek(), inst);
		instMap.put(node, inst);
		ASTNode grandParent = node.getParent().getParent();
		if(isNestedStatement(node.getParent()))
			nestingMap.put(inst, conditionalNesting - 1);
		else if(node.getParent() instanceof Expression && (isNestedStatement(grandParent) || isNestedStatement(grandParent.getParent())))
			nestingMap.put(inst, conditionalNesting - 1);
		else if(grandParent.getParent() instanceof Expression && (isNestedStatement(grandParent.getParent().getParent().getParent())))
			nestingMap.put(inst, conditionalNesting - 1);
		else
			nestingMap.put(inst, conditionalNesting);
	}

	private boolean isInvocation(ASTNode node) {
		return node instanceof ClassInstanceCreation ||
				node instanceof MethodInvocation;
	}
	
	private boolean isNestedStatement(ASTNode node){
		return node instanceof WhileStatement 
				|| node instanceof ForStatement
				|| node instanceof EnhancedForStatement;
	}
	
	
	
	@Override
	public boolean visit(ImportDeclaration node) {
		existingImports.add(node.getName().getFullyQualifiedName());
		return true;
	}
	
	public List<String> getExistingImports() {
		return Collections.unmodifiableList(existingImports);
	}
	
	public Map<String, String> getExistingVariables() {
		return Collections.unmodifiableMap(existingVariables);
	}
	
	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		final BlockVisitorV3 visitor = new BlockVisitorV3(analyzer, packageRoot);
		List<Object> bodyDecs = node.bodyDeclarations();
		for(Object dec: bodyDecs)
			if(dec instanceof MethodDeclaration)
				((MethodDeclaration)dec).accept(visitor);
		return false;
	}
	
	@Override
	public boolean visit(TypeDeclaration node) {
		String[] packagePath = packageRoot.split("\\.");
		String[] validImports = Arrays.copyOf(packagePath, packagePath.length);
		
		for (int i = 0; i < packagePath.length; i++){
			for (int j = i - 1; j >= 0; j--)
				validImports[i] = packagePath[j] + "." + validImports[i];
			if(i < packagePath.length - 1)
				validImports[i] += ".*";
		}
		
		boolean foundValidImport = false;
		for(String importStr : existingImports)
			for(String validImportPrefix : validImports)
				if(importStr.startsWith(validImportPrefix))
					foundValidImport = true;

		if(!foundValidImport)
			return false;
		
		if(node.getParent().getClass().equals(CompilationUnit.class))
			return true;
		final BlockVisitorV3 visitor = new BlockVisitorV3(analyzer, packageRoot);
		List<Object> bodyDecs = node.bodyDeclarations();
		for(Object dec: bodyDecs)
			if(dec instanceof MethodDeclaration)
				((MethodDeclaration)dec).accept(visitor);
		return false;
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		Expression e = node.getInitializer();
		ITypeBinding binding = e != null ? e.resolveTypeBinding() : null;
		if(isInvocation(e) && includePackage(binding)) {
			varMap.put(node.getName().getIdentifier(), e);
		}else if(e instanceof ArrayAccess){
			Expression array = ((ArrayAccess)e).getArray();
			ITypeBinding varBinding = node.getName().resolveTypeBinding();
			if(includePackage(varBinding)){
				varMap.put(node.getName().getIdentifier(), array);
			}
		}
	
		if(binding != null)
			existingVariables.put(node.getName().getIdentifier(), binding.getQualifiedName());
	
		return super.visit(node);
	}
	
	@Override
	public boolean visit(Assignment node) {
		Expression leftSide = node.getLeftHandSide(); 
		if(leftSide instanceof SimpleName){
			ITypeBinding binding = leftSide.resolveTypeBinding();
			if(includePackage(binding))
				varMap.put(((SimpleName)leftSide).getIdentifier(), node.getRightHandSide());
			
			if(binding != null)
				existingVariables.put(((SimpleName) leftSide).getIdentifier(), binding.getQualifiedName());
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
//					depsMap.putAll(node, varMap.get(var));
					depsMap.put(node, varMap.get(var));
			}
		}
		return super.visit(node);
	}
	
	@Override
	public boolean visit(MethodInvocation node) {
		if(includePackage(node.resolveTypeBinding()) && isInvocation(node.getParent()))
			depsMap.put(node.getParent(), node);
		if(node.getParent() instanceof ArrayAccess)
			depsMap.put(node.getParent().getParent(), node);
		
		Expression e = node.getExpression();
		if(e instanceof SimpleName) {
//			depsMap.putAll(node, varMap.get(((SimpleName) e).getIdentifier()));
			depsMap.put(node, varMap.get(((SimpleName) e).getIdentifier()));
		}
		
		for(Object arg : node.arguments()) {
			if(arg instanceof SimpleName) {
				String var =  ((SimpleName) arg).getIdentifier();
				if(varMap.containsKey(var))
//					depsMap.putAll(node, varMap.get(var));
					depsMap.put(node, varMap.get(var));
			}
		}
		
		return super.visit(node);
	}
	
	@Override
	public boolean visit(IfStatement node) {
		node.getExpression().accept(this);
		conditionalNesting++;
		
		withinIfBlock = true;
		ifElseStack.push(node);
		withinThenStack.push(true);
		node.getThenStatement().accept(this);
		withinThenStack.pop();

		if(node.getElseStatement() != null) {
			withinIfBlock = true;
			withinThenStack.push(false);
			node.getElseStatement().accept(this);
			withinThenStack.pop();
		}
		conditionalNesting--;
		ifElseStack.pop();
		withinIfBlock = false;
		return false;
	}

	/*
	 * tanto na chamada a construtores como metodos, so eh adicionado no final (endVisit), 
	 * para que os argumentos sejam processados primeiro
	 */
	
	@Override
	public void endVisit(ClassInstanceCreation node) {
		ITypeBinding binding = node.resolveTypeBinding();

		if(includePackage(binding)) {
			Instruction inst = new ConstructorInstruction(node, binding);
			InstructionLine instLine = new InstructionLine(inst, inst.line);
			addInstruction(node, instLine);
		}
	}

	@Override
	public void endVisit(MethodInvocation node) {
		
		Expression e = node.getExpression();
		if(e != null) {
			ITypeBinding binding = e.resolveTypeBinding();
				
			if(includePackage(binding)) {
				IMethodBinding mBinding = node.resolveMethodBinding();
				boolean isStatic = mBinding != null && Modifier.isStatic(mBinding.getModifiers());
				Instruction inst = new MethodInstruction(node, binding, isStatic);
				InstructionLine instLine = new InstructionLine(inst, inst.line);
				addInstruction(node, instLine);
				
			}
		}
		
	}
	
	
	
	public int getCurrentLine() {
		return currentLine;
	}
	
	@Override
	public void endVisit(WhileStatement node) {
		conditionalNesting--;
	}
	
	@Override
	public void endVisit(ForStatement node) {
		conditionalNesting--;
	}
	
	@Override
	public void endVisit(EnhancedForStatement node) {
		conditionalNesting--;
	}
	
	@Override
	public void endVisit(Block node) {
		if(!(node.getParent() instanceof MethodDeclaration))
			return;
//		System.out.println();
		
		List<List<InstructionLine>> sentenceList = new ArrayList<>();
		for(InstructionLine instruction: instMap.values()){
			
//			System.out.println(nestingMap.get(instruction) + "->" + instruction.instruction + " " + instruction.line);
//			System.out.print("deps: ");
//			for (Iterator<InstructionLine> iterator = instruction.getDependencies().iterator(); iterator.hasNext();){ 
//				InstructionLine inst = iterator.next();
//				System.out.print(inst.instruction + "(" + inst.line + ") ");	
//			}
//			System.out.println();
//			for(ASTNode key: instMap.keySet())
//				if(instMap.get(key).equals(instruction)){
//					System.out.println("AST & parents: " + key.getClass() + key.getParent().getClass() + " " + key.getParent().getParent().getClass());
//					break;
//				}
//			System.out.println();
			// if no dependencies -> new sentence
			if(instruction.getDependencies().isEmpty()){
//				System.out.println("no dependencies");
				List<InstructionLine> sentence = new LinkedList<>();
				sentence.add(instruction);
				sentenceList.add(sentence);
			}else{
				// if no dependencies on same level -> make new sentence with all dependencies, then add to that sentence
				boolean dependenciesOnSameLevel = false;
				for(InstructionLine dep: instruction.getDependencies()){
					if(nestingMap.get(dep) == nestingMap.get(instruction)){
						dependenciesOnSameLevel = true;
						break;
					}
				}
				if(!dependenciesOnSameLevel){
					// if no dependencies on same level && within else:
					//	- if sentence was duplicated due to if -> add to original sentence
					//	- if sentence was not duplicated by if && all dependencies in one sentence -> duplicate sentence, then add to it
					//  - if sentence was not duplicated by if && not all in one -> duplicate sentence, then add to it
					if(elseMap.containsValue(instruction)){
						if(ifMap.containsKey(ifElseMapsKeyOf(instruction))){
							//boolean foundAllInSame = false;
							for (List<InstructionLine> sentence : sentenceList) {
								if (sentence.containsAll(instruction.getDependencies())) {
									boolean canDepend = true;
									for(InstructionLine sentenceInst: sentence)
										if(ifMap.get(ifElseMapsKeyOf(instruction)).contains(sentenceInst)){
											canDepend = false;
											break;
										}
									//foundAllInSame = true;
									if(canDepend)
										sentence.add(instruction);
//									System.out.println("add to original sentence");
									//break;
								}
							}
						}else{
							boolean foundAllInSame = false;
							List<List<InstructionLine>> sentencesToDup = new ArrayList<>();
							for (List<InstructionLine> sentence : sentenceList) 
								if (sentence.containsAll(instruction.getDependencies())) {
									sentencesToDup.add(sentence);
									foundAllInSame = true;
								}
							if(foundAllInSame)
								for (List<InstructionLine> sentence : sentencesToDup){
									List<InstructionLine> dup = new ArrayList<>();
									dup.addAll(sentence);
									sentenceList.add(dup);
									sentence.add(instruction);
//									System.out.println("duplicating and adding (else w/ no if)");
								}
							else{
								List<List<InstructionLine>> sentencesToJoin = new ArrayList<>();
								for(List<InstructionLine> sentence : sentenceList)
									for (InstructionLine sentenceInst : sentence)
										if(instruction.getDependencies().contains(sentenceInst))
											sentencesToJoin.add(sentence);
								List<InstructionLine> newSentence = joinSentences(sentencesToJoin);
								newSentence.add(instruction);
								sentenceList.add(newSentence);
							}
						}
					}else{
						// if no dependencies on same level && all dependencies in one sentence -> duplicate sentence, then add to that sentence
						// if no dependencies on same level && not all in one -> make new sentence with all dependencies, then add to that sentence
//						System.out.println("no dependencies on same level");
						boolean foundAllInSame = false;
						List<List<InstructionLine>> newSentences = new ArrayList<>();
						
						for(List<InstructionLine> sentence : sentenceList) {
							if(sentence.containsAll(instruction.getDependencies())){
								foundAllInSame = true;
								List<InstructionLine> dupSentence = new LinkedList<>();
								dupSentence.addAll(sentence);
								dupSentence.add(instruction);
								newSentences.add(dupSentence);
							}
						}
						sentenceList.addAll(newSentences);
						
						if(!foundAllInSame){
							List<List<InstructionLine>> sentencesToJoin = new ArrayList<>();
							for(List<InstructionLine> sentence : sentenceList)
								for (InstructionLine sentenceInst : sentence)
									if(instruction.getDependencies().contains(sentenceInst))
										sentencesToJoin.add(sentence);
							List<InstructionLine> newSentence = joinSentences(sentencesToJoin);
							newSentence.add(instruction);
							sentenceList.add(newSentence);
						}
					}
				} else {
					// if has dependencies on same level && dependencies all on one sentence -> add to all sentences with dependencies
					boolean foundAllInSame = false;
					for (List<InstructionLine> sentence : sentenceList) {
						if (sentence.containsAll(instruction.getDependencies())) {
							foundAllInSame = true;
							sentence.add(instruction);
//							System.out.println("dependencies all on one sentence");
						}
					}
					// if has dependencies on same level && dependencies on multiple sentences -> join sentences into one, then add to that sentence
					if (!foundAllInSame) {
//						System.out.println("dependencies on multiple sentences, joining them");
						List<List<InstructionLine>> sentencesToGroup = new ArrayList<>();
						
						for (List<InstructionLine> sentence : sentenceList) {
							for (InstructionLine dep : instruction.getDependencies()) {
								if (sentence.contains(dep)) {
									sentencesToGroup.add(sentence);
									break;
								}
							}
						}
						sentenceList.removeAll(sentencesToGroup);

						List<InstructionLine> groupedSentences = joinSentences(sentencesToGroup);
						groupedSentences.add(instruction);

						sentenceList.add(groupedSentences);
					}
				}
			}
		}
		
//		if(sentenceList.size() > 0)
//			System.out.println(node.getParent());
		for (List<InstructionLine> sentence: sentenceList) {
//			System.out.println(sentence);
//			if(sentence.size() == 1) System.out.println("deps: " + sentence.get(0).dependencies);
			Sentence newSentence = analyzer.newSentence(getLineNumber(node));
			for(InstructionLine instLine: sentence){
				newSentence.addInstruction(instLine.instruction);
			}
		}
		internalAnalyzer.addAll(sentenceList);
	}
	
	private List<InstructionLine> joinSentences(List<List<InstructionLine>> sentencesToGroup) {
		List<InstructionLine> joined = new LinkedList<>();
		for (List<InstructionLine> sentence : sentencesToGroup) {
			for (InstructionLine instructionLine : sentence) {
				if(!joined.contains(instructionLine))
					joined.add(instructionLine);
			}
		}
		joined.sort(new Comparator<InstructionLine>() {
			@Override
			public int compare(InstructionLine a, InstructionLine b) {
				return a.line > b.line? 1: -1;
			}
		});
		return joined;
	}

	public List<List<InstructionLine>> getInternalAnalyzer() {
		return internalAnalyzer;
	}
	
	private static int getLineNumber(ASTNode node) {
		return ((CompilationUnit) node.getRoot()).getLineNumber(node.getStartPosition());	
	}

	private static int getEndLineNumber(ASTNode node) {
		return ((CompilationUnit) node.getRoot()).getLineNumber(node.getStartPosition()+node.getLength());
	}
	
	private boolean withinRange(ASTNode node){
		int nodeLine = getLineNumber(node);
		return nodeLine >= startLine && nodeLine <= endLine;
	}
	
	public static class InstructionLine {
		public final Instruction instruction;
		private final int line;
		private final Set<InstructionLine> dependencies = new HashSet<>();
		
		public InstructionLine(Instruction instruction, int line) {
			this.instruction = instruction;
			this.line = line;
		}
		
		public void addDependency(InstructionLine inst) {
			assert inst != null;
			dependencies.add(inst);
			instruction.addDependency(inst.instruction);
		}

		public void addDependencies(Collection<InstructionLine> collection) {
			for (Iterator<InstructionLine> iterator = collection.iterator(); iterator.hasNext();) {
				InstructionLine instructionLine = iterator.next();
				addDependency(instructionLine);
			}
		}
		
		public Collection<InstructionLine> getDependencies() {
			return Collections.unmodifiableSet(dependencies);
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof InstructionLine
					&& ((InstructionLine) obj).instruction.equals(instruction)
					&& ((InstructionLine) obj).line == line;
		}

		@Override
		public int hashCode() {
			return instruction.hashCode() + Integer.hashCode(line);
		}
		@Override
		public String toString() {
			return instruction.toString() + "(" + line + ")";
		}
		
	}
}