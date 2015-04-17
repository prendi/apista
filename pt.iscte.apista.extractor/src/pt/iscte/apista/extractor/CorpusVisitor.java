package pt.iscte.apista.extractor;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class CorpusVisitor extends ASTVisitor implements Serializable {
	private String packageRoot;
	public final Set<String> wordsTop = new HashSet<>();
	public final Set<String> types = new HashSet<>();
	
	public CorpusVisitor(String packageRoot) {
		this.packageRoot = packageRoot;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		ITypeBinding binding = node.resolveTypeBinding();
		if(includePackage(binding)){
			wordsTop.add(binding.getName().toString() + ".new");
			types.add(binding.getName().toString());
		}
		return true;
	}
	
	@Override
	public boolean visit(MethodInvocation node) {
		Expression expression = node.getExpression();
		if (expression != null) {
			ITypeBinding binding = expression.resolveTypeBinding();
			if (includePackage(binding)) {
				types.add(binding.getName().toString());
				wordsTop.add(ApiVisitor.getTopMostType(binding, binding, node.getName().toString()) + "." + node.getName().toString());
			}
		}
		return true;
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
}