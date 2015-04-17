package pt.iscte.apista.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class MethodInstruction extends Instruction {

	private final String methodName;
	private boolean isStatic; // TODO

	public MethodInstruction(MethodInvocation node, ITypeBinding binding) {
		super(node, binding);
		methodName = node.getName().getFullyQualifiedName();
		isStatic = false;
	}

	public MethodInstruction(String qualifiedName, String methodName) {
		super(qualifiedName);
		this.methodName = methodName;
	}

	public String getMethodName() {
		return methodName;
	}

	@Override
	public String resolveInstruction(IType type, Map<String, IType> vars, ITypeCache typeCache) {
		ITypeHierarchy typeHierarchy = typeCache.getTypeHierarchy(type);

		IType[] subtypes = typeHierarchy.getAllSubtypes(type);
		String var = null;
		for(String v : vars.keySet()) {
			if(type.equals(vars.get(v)))
				var = v;

			for(IType s : subtypes)
				if(s.equals(vars.get(v)))
					var = v;
			
			if(var != null)
				break;
		}
		
		if(var == null)
			var = className;
		
		String params = "?";
		String returnType = null;
		try {
			IType[] supertypes = typeHierarchy.getAllSuperclasses(type);
			List<IType> types = new ArrayList<IType>();
			types.add(type);
			for(IType t : supertypes)
				types.add(t);
			
			for(IType t : types)
				for(IJavaElement m : t.getChildren())
					if(m instanceof IMethod && ((IMethod)m).getElementName().equals(methodName)) {
						IMethod method = (IMethod) m;
						params = inferParameters(type, method, vars, typeCache);
//						if(method.getReturnType())
					}
		}
		catch (JavaModelException e) {
			e.printStackTrace();
		}

		// TODO: return type
		return var + "." + methodName + "(" + params + ")";
	}

	@Override
	public String getWord() {
		return className + "." + methodName;
	}

}