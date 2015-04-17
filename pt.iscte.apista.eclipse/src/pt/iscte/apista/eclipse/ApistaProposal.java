package pt.iscte.apista.eclipse;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavadocContentAccess;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import pt.iscte.apista.core.ITypeCache;
import pt.iscte.apista.core.Instruction;
import pt.iscte.apista.core.MethodInstruction;
import pt.iscte.apista.extractor.BlockVisitorV3;


public class ApistaProposal implements ICompletionProposal {

	private ContentAssistInvocationContext context;
	private IContextInformation information;
	private Instruction instruction;
	private BlockVisitorV3 visitor;
	
	private String code;
	private String display;
	private IType type;
	
	private int offset = 0;
	
	public ApistaProposal(ContentAssistInvocationContext context, IContextInformation information, Instruction instruction, 
			BlockVisitorV3 visitor, Map<String, IType> vars, ITypeCache typeCache) {
		this.context = context;
		this.information = information;
		this.instruction = instruction;
		this.visitor = visitor;
		type = typeCache.getType(instruction.getQualifiedTypeName());
		code = instruction.resolveInstruction(type, vars, typeCache);
		display = " " + (code.indexOf('=') != -1 ? code.substring(code.indexOf('=')+2) : code);
		code += ";\n";
	}
	
	@Override
	public String getAdditionalProposalInfo() {
		
		String html = extractDocumentation(type);
		return html;
	}

	@Override
	public void apply(IDocument document) {
		try {
			if(!visitor.getExistingImports().contains(instruction.getQualifiedTypeName())) {
				String importStatement = "import " + instruction.getQualifiedTypeName() + ";\n";
				document.replace(0, 0, importStatement);
				offset = importStatement.length();
			}
			
			document.replace(context.getInvocationOffset() + offset, 0, code);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Point getSelection(IDocument document) {
		int p = context.getInvocationOffset() + offset + code.length();
		return new Point(p-1, 0);
	}

	@Override
	public Image getImage() {
		return information.getImage();
	}

	@Override
	public String getDisplayString() {
		return display;
	}

	@Override
	public IContextInformation getContextInformation() {
		return information;
	}
	
	private String extractDocumentation(IMember member) {
		try {
			if(member == null)
				return "Could not load documentation";

			if(instruction instanceof MethodInstruction) {
				String method = ((MethodInstruction) instruction).getMethodName();

				// TODO super classes
				for(IJavaElement e : member.getChildren())
					if(e instanceof IMethod && ((IMethod) e).getElementName().equals(method))
						member = (IMethod) e;
			}

			Reader reader = JavadocContentAccess.getHTMLContentReader(member, true, true);
			if(reader == null)
				return "Could not load documentation";

			return getString(reader);
		} 
		catch (JavaModelException e) {
			e.printStackTrace();
		}
		return "";
	}

	private static String getString(Reader reader) {
		StringBuffer buf= new StringBuffer();
		char[] buffer= new char[1024];
		int count;
		try {
			while ((count= reader.read(buffer)) != -1)
				buf.append(buffer, 0, count);
		} catch (IOException e) {
			return null;
		}
		return buf.toString();
	}

}
