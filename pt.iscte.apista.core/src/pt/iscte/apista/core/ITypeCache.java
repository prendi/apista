package pt.iscte.apista.core;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;

public interface ITypeCache {

	IType getType(String qualifiedName);
	
	ITypeHierarchy getTypeHierarchy(IType type);
	
//	public static final ITypeCache NO_CACHE = new ITypeCache() {
//
//		@Override
//		public ITypeHierarchy getTypeHierarchy(IType type) {
//			try {
//				return type.newTypeHierarchy(null);
//			} catch (JavaModelException e) {
//				e.printStackTrace();
//			}
//			return null;
//		}
//		
//	};
}
