package pt.iscte.apista.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;

public abstract class Instruction implements Serializable{
	public int line;
	protected final String className;
	protected final String qualifiedName;

	private Set<Instruction> dependencies;

	public static final Instruction START = new SentenceStart();
	public static final Instruction END = new SentenceEnd();
	public static final Instruction UNK = new UnknownInstruction();

	protected Instruction(ASTNode node, ITypeBinding binding) {
		this.line = ((CompilationUnit) node.getRoot()).getLineNumber(node.getStartPosition());
		qualifiedName = binding.getQualifiedName();
		if(binding.isAnonymous() && binding.getSuperclass() != null)
			className = binding.getSuperclass().getName();
		else
			className = binding.getName();

		dependencies = new HashSet<>();
	}

	protected Instruction(String qualifiedName) {
		this.qualifiedName = qualifiedName;
		this.className = qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1);
	}

	public abstract String getWord();

	public abstract String resolveInstruction(IType type, Map<String, IType> vars, ITypeCache typeCache);

	public String getQualifiedTypeName() {
		return qualifiedName;
	}

	@Override
	public String toString() {
		return getWord(); //+ "(" + Arrays.toString(dependencies.toArray()) + ")";
	}

	//	public static Instruction evalutate(String value) {
	//
	//		if(!value.equals("$END$") && !value.equals("$START$")){
	//
	//			String[] parts = value.split("\\.");
	//			if(parts[1].equals("new"))
	//				return new ConstructorInstruction(parts[0]);
	//			else
	//				return new MethodInstruction(parts[0], parts[1]);
	//		}else{
	//			return null;
	//		}
	//	}

	public void addDependency(Instruction inst) {
		assert inst != null;

		dependencies.add(inst);
	}

	public void addDependencies(Collection<Instruction> collection) {
		dependencies.addAll(collection);
	}

	public boolean dependsOn(Instruction inst) {
		return dependencies.contains(inst);
		//		if(dependencies.isEmpty())
		//			return false;
		//		else if(dependencies.contains(inst))
		//			return true;
		//		else
		//			for(Instruction d : dependencies)
		//				return d.dependsOn(inst);
		//		
		//		return false;
	}

	public Collection<Instruction> getDependencies() {
		return Collections.unmodifiableSet(dependencies);
	}


	public List<Instruction> getContext() {
		List<Instruction> context = new ArrayList<Instruction>();
		for(Instruction d : dependencies)
			context.add(d);

		context.add(this);
		return context;
	}

	//	public Set<Instruction> getAllDependencies() {
	//		Set<Instruction> deps = new HashSet<>();
	//		alldeps(this, deps);
	//		return deps;
	//	}

	//	private static void alldeps(Instruction inst, Set<Instruction> set) {
	//		set.addAll(inst.dependencies);
	//		for(Instruction d : inst.dependencies)
	//			alldeps(d, set);
	//	}

	public String getClassName() {
		return className;
	}

	private static class SentenceStart extends Instruction {
		protected SentenceStart() {
			super("");
		}

		@Override
		public String getWord() {
			return "$START$";
		}

		@Override
		public String resolveInstruction(IType type, Map<String, IType> vars, ITypeCache typeCache) {
			throw new UnsupportedOperationException();
		}	
	}

	private static class SentenceEnd extends Instruction {
		protected SentenceEnd() {
			super("");
		}

		@Override
		public String getWord() {
			return "$END$";
		}

		@Override
		public String resolveInstruction(IType type, Map<String, IType> vars, ITypeCache typeCache) {
			throw new UnsupportedOperationException();
		}	
	}
	
	
	private static class UnknownInstruction extends Instruction {
		protected UnknownInstruction() {
			super("");
		}

		@Override
		public String getWord() {
			return "$UNK$";
		}

		@Override
		public String resolveInstruction(IType type, Map<String, IType> vars, ITypeCache typeCache) {
			throw new UnsupportedOperationException();
		}	
	}
	
	@Override
	public int hashCode() {
		return this.getWord().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof Instruction) && this.getWord().equals(((Instruction)obj).getWord());
	}

	protected String inferParameters(IType type, IMethod method, Map<String, IType> vars, ITypeCache typeCache) {
		String params = "";

		String[] paramNames;
		try {
			paramNames = method.getParameterNames();
			String[] paramTypes = method.getParameterTypes();

			int firstNotFound = -1;
			for(int i = 0; i < paramNames.length; i++) {
				if(!params.isEmpty())
					params += ", ";

				String paramSig = paramTypes[i];
				if(Signature.getTypeSignatureKind(paramSig) == Signature.BASE_TYPE_SIGNATURE) {
					params += paramNames[i];
				}
				else {
					
					String paramTypeName = Signature.getSignatureQualifier(paramSig) + "." + Signature.getSignatureSimpleName(paramSig);
					IType paramType = typeCache.getType(paramTypeName);
					String v = getCompatibleVar(paramType, vars, typeCache);

					params += v == null ? paramNames[i] : v;
					if(v == null) {
						if(firstNotFound == -1)
							firstNotFound = i;
					}
				}
			}
		}
		catch (JavaModelException e) {
			e.printStackTrace();
		}
		return params;
	}

	private String getCompatibleVar(IType type, Map<String, IType> vars, ITypeCache typeCache) {
		if(type == null)
			return null;

		ITypeHierarchy typeHierarchy = typeCache.getTypeHierarchy(type);

		if(typeHierarchy == null)
			return null;

		IType[] subtypes = typeHierarchy.getSubtypes(type);
		for(Entry<String, IType> e : vars.entrySet()) {
			if(type.equals(e.getValue()))
				return e.getKey();
			for(IType s : subtypes)
				if(s.equals(e.getValue()))
					return e.getKey();
		}
		return null;
	}



}