package pt.iscte.apista.extractor;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class ApiVisitor extends ASTVisitor {
	public final Set<String> words = new HashSet<>();
	public final Set<String> wordsTop = new HashSet<>();
	public final Set<String> types = new HashSet<>();
	
	@Override
	public boolean visit(TypeDeclaration node) {
		if(!Modifier.isPublic(node.getModifiers()))
			return false;
		types.add(node.getName().toString());
		return true;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		if (Modifier.isAbstract(node.getModifiers()) || !Modifier.isPublic(node.getModifiers()))
			return false;
		ITypeBinding binding = node.resolveBinding().getDeclaringClass();
		String operation = node.isConstructor() ? "new" : node.getName().toString();
		words.add(binding.getName() + "." + operation);
		
		String topMostType = getTopMostType(binding, binding, node.getName().toString());
		wordsTop.add(topMostType + "." + operation);
		
		return false;
	}

	public static String getTopMostType(ITypeBinding binding, ITypeBinding result, String name) {
		//if an interface has said method -> return interface name
		for (ITypeBinding interf : binding.getInterfaces()) {
			ITypeBinding c = result;
			for(IMethodBinding m : interf.getDeclaredMethods())
				if(m.getName().equals(name))
					c = interf;
			String top = getTopMostType(interf, c, name);
			if(!top.equals(result.getName()))
				return top;
		}
		//else if a superclass has said method -> return superclass name
		ITypeBinding superclass = binding.getSuperclass();
		
		if(superclass == null)
			return result.getName();
		else {
			ITypeBinding c = result;
			for(IMethodBinding m : superclass.getDeclaredMethods())
				if(m.getName().equals(name))
					c = superclass;
			return getTopMostType(superclass, c, name);
		}
	}
}
