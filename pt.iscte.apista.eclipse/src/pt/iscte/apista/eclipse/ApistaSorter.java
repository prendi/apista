package pt.iscte.apista.eclipse;

import java.util.List;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.internal.ui.text.java.JavaMethodCompletionProposal;
import org.eclipse.jdt.ui.text.java.AbstractProposalSorter;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

import pt.iscte.apista.core.Instruction;
import pt.iscte.apista.core.MethodInstruction;
import pt.iscte.apista.extractor.BlockVisitorV3;
import pt.iscte.apista.extractor.ContextInfo;
import pt.iscte.apista.extractor.JavaSourceParser;

public class ApistaSorter extends AbstractProposalSorter {

	private List<Instruction> blockContext;
	private List<Instruction> proposals;
	private String expectedType;

	@Override
	public void beginSorting(ContentAssistInvocationContext context) {
		IDocument document = context.getDocument();

		int line = getLineNumber(context);

		String lineContent = getLineContent(document, line);
		String src = document.get();

		if (!lineContent.isEmpty()) {
			int offset = context.getInvocationOffset() - lineContent.length();
			src = document.get().substring(0, offset).concat(document.get().substring(context.getInvocationOffset()));
		}

		ContextInfo contextInfo = new ContextInfo(line);
		BlockVisitorV3 visitor = new BlockVisitorV3(line);

		ApistaProposalComputer proposalComputer = ApistaProposalComputer.getInstance();

		String[] srcPaths = proposalComputer.getSourcePaths();

		// TODO remove hard-coded "Test"
		JavaSourceParser parser = JavaSourceParser.createFromSource(src, "Test", srcPaths, "UTF-8");
		parser.parse(visitor);

		parser = JavaSourceParser.createFromSource(src, "Test", srcPaths, "UTF-8");
		parser.parse(contextInfo);

		blockContext = visitor.getAnalyzer().getFirst().getInstructions();

		proposals = proposalComputer.query(blockContext);

		if (lineContent.length() > 0) {
			expectedType = contextInfo.varType(lineContent.replace(".", ""));
			expectedType = expectedType.substring(expectedType.lastIndexOf(".") + 1);
		}

	}

	// TODO: refactor
	private int getLineNumber(ContentAssistInvocationContext context) {
		try {
			return context.getDocument().getLineOfOffset(context.getInvocationOffset());
		} catch (BadLocationException e) {
			e.printStackTrace();
			return 0;
		}
	}

	// TODO: refactor
	private String getLineContent(IDocument document, int line) {
		String lineContent = null;
		try {
			int lineLength = document.getLineLength(line);
			lineContent = document.get(document.getLineOffset(line), lineLength);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return lineContent.trim();
	}

	@Override
	public int compare(ICompletionProposal p1, ICompletionProposal p2) {
		// pulls up APISTA proposals
		if (ApistaProposal.class.isInstance(p1) && !ApistaProposal.class.isInstance(p2))
			return -1;
		else if (!ApistaProposal.class.isInstance(p1) && ApistaProposal.class.isInstance(p2))
			return 1;

		// in case of two Java method proposals
		else if (JavaMethodCompletionProposal.class.isInstance(p1)
				&& JavaMethodCompletionProposal.class.isInstance(p2)) {
			IMethod m1 = (IMethod) ((JavaMethodCompletionProposal) p1).getJavaElement();
			IMethod m2 = (IMethod) ((JavaMethodCompletionProposal) p2).getJavaElement();

			if (m1 == null || m2 == null)
				return 0;

			MethodInstruction mi1 = new MethodInstruction(expectedType, m1.getElementName());
			int indexI1 = proposals.indexOf(mi1);

			MethodInstruction mi2 = new MethodInstruction(expectedType, m2.getElementName());
			int indexI2 = proposals.indexOf(mi2);

			if (indexI1 == -1 && indexI2 > -1) {
				return 1;
			} else if (indexI2 == -1 && indexI1 > -1) {
				return -1;
			} else if (indexI2 == -1 && indexI1 == -1) {
				return 0;
			}

			return indexI1 == indexI2 ? 0 : indexI1 > indexI2 ? 1 : -1;
		} else
			return 0;
	}

}