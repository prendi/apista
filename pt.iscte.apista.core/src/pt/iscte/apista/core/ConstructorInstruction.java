package pt.iscte.apista.core;

import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ITypeBinding;

public class ConstructorInstruction extends Instruction {


	public ConstructorInstruction(ClassInstanceCreation node, ITypeBinding binding) {
		super(node, binding);
	}

	public ConstructorInstruction(String className) {
		super(className);
	}

	@Override
	public String resolveInstruction(IType type, Map<String, IType> vars, ITypeCache typeCache) {
		String var = className.toLowerCase().substring(0, 1);
		int i = 1;
		while(vars.containsKey(var)) {
			var += i; 
			i++;
		}
		
		String params = "?";
		try {
			for(IJavaElement m : type.getChildren())
				if(m instanceof IMethod && ((IMethod)m).isConstructor()) {
					params = inferParameters(type, (IMethod) m, vars, typeCache);
				}
		}
		catch (JavaModelException e) {
			e.printStackTrace();
		}
		return className + " " + var + " = new " + className + "(" + params + ")";
	}

	@Override
	public String getWord() {
		return  className + ".new";
	}

}