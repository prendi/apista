package pt.iscte.apista.eclipse;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;

import com.google.common.collect.Lists;

import pt.iscte.apista.core.APIModel;
import pt.iscte.apista.core.ITypeCache;
import pt.iscte.apista.core.Instruction;
import pt.iscte.apista.core.MethodInstruction;
import pt.iscte.apista.core.Sentence;
import pt.iscte.apista.core.SystemConfiguration;
import pt.iscte.apista.extractor.BlockVisitorV3;
import pt.iscte.apista.extractor.JavaSourceParser;


public class ApistaProposalComputer implements IJavaCompletionProposalComputer, IContextInformation, ITypeCache {

	public static final String EXT_POINT_ID = "pt.iscte.apista.eclipse.api";

	private Map<String, IType> typeCache = new HashMap<String, IType>();

	private Map<IType, ITypeHierarchy> cache = new HashMap<IType, ITypeHierarchy>();
	
	//TODO: tmp
	static APIModel apiModel;
	static String libSrcRoot;

	private Map<APIModel,Image> icons;
	
	
	private IJavaProject project;

	public ApistaProposalComputer() {
		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint(EXT_POINT_ID);
		for(IExtension ext : extensionPoint.getExtensions()) {
			IConfigurationElement[] configurationElements = ext.getConfigurationElements();
			APIModel model = null;
			for(IConfigurationElement api : configurationElements)
				try {

					Bundle bundle = Platform.getBundle(ext.getContributor().getName());
					URL fileURL = bundle.getEntry(api.getAttribute("configuration"));
					
					InputStream inputStream = fileURL.openConnection().getInputStream();
					SystemConfiguration conf = new SystemConfiguration(inputStream);
					libSrcRoot = conf.getApiSrcPath();
					model = (APIModel) api.createExecutableExtension("model");
					apiModel = model;
					apiModel.setup(conf.getModelParameters());
					
					URL modelFileURL = bundle.getEntry(api.getAttribute("modelFile"));
					
					InputStream modelInputStream = modelFileURL.openConnection().getInputStream();
					apiModel.load(modelInputStream);
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			System.out.println(model);
		}
		ImageDescriptor desc = AbstractUIPlugin.imageDescriptorFromPlugin("pt.iscte.apista.eclipse", "swt.gif");
		Image icon = desc.createImage();
	}


	@Override
	public IType getType(String qualifiedName) {
		if(project == null)
			return null;
		else {
			if(typeCache.containsKey(qualifiedName))
				return typeCache.get(qualifiedName);

			IType t = null;
			try {
				t = project.findType(qualifiedName, new NullProgressMonitor());
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
			if(t == null)
				return null;

			typeCache.put(qualifiedName, t);
			return t;
		}
	}

	@Override
	public ITypeHierarchy getTypeHierarchy(IType type) {
		if(cache.containsKey(type))
			return cache.get(type);
		else {
			ITypeHierarchy h = null;
			try {
				h = type.newTypeHierarchy(new NullProgressMonitor());
				if(h == null)
					System.err.println("NOT: " + type);
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
			cache.put(type, h);
			return h;
		}
	}

	


	@Override
	public List<ICompletionProposal> computeCompletionProposals(
			ContentAssistInvocationContext context, IProgressMonitor monitor) {

		JavaContentAssistInvocationContext javaContext = (JavaContentAssistInvocationContext) context;
		System.out.println("expected: " + javaContext.getExpectedType());

		project = javaContext.getProject();

		try {
			System.out.println("id prefix: " + context.computeIdentifierPrefix());
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		List<ICompletionProposal> list = new ArrayList<>();
		IDocument document = context.getDocument();

		int line = getLineNumber(context);
		String lineContent = getLineContent(document, line);
		BlockVisitorV3 visitor = new BlockVisitorV3(line);
		int offset = context.getInvocationOffset() - lineContent.length();
		String src = document.get().substring(0, offset).concat(document.get().substring(context.getInvocationOffset()));

		//TODO remove hard-coded "Test"
		JavaSourceParser parser = JavaSourceParser.createFromSource(src, "Test", libSrcRoot, "UTF-8");
		parser.parse(visitor);

		System.out.println(visitor.getExistingVariables());

		// adds if line has no text
		if(lineContent.isEmpty()) {

			List<Instruction> blockContext = new ArrayList<Instruction>();
			//			List<Sentence> blockSentences = visitor.getAnalyzer().getSentences();

			//			Sentence last = blockSentences.get(blockSentences.size()-1);

			for(Sentence s : visitor.getAnalyzer().getSentences())
				blockContext.addAll(s.getInstructions());

			blockContext = Collections.unmodifiableList(blockContext);

			System.out.println("Context:  " + Arrays.toString(blockContext.toArray()));

			Map<String, IType> vars = convert(visitor.getExistingVariables(), project);

			List<Instruction> proposals = apiModel.query(blockContext, 20);
			for(Instruction instruction : proposals) {
				if(instruction instanceof MethodInstruction) { // !isStatic
					IType t = getType(instruction.getQualifiedTypeName());
					if(!vars.containsValue(t))
						continue;
				}

				list.add(new ApistaProposal(context, this, instruction, visitor, vars, this));
			}
		}
		return list;
	}


	private Map<String, IType> convert(Map<String, String> existingVariables, IJavaProject project) {
		Map<String, IType> map = new LinkedHashMap<String, IType>();
		List<Entry<String, String>> list = new ArrayList<Map.Entry<String,String>>(existingVariables.entrySet());

		for(Entry<String, String> e : Lists.reverse(list)) {
			map.put(e.getKey(), getType(e.getValue()));
		}

		return map;
	}


	private int getLineNumber(ContentAssistInvocationContext context) {
		try {
			return context.getDocument().getLineOfOffset(context.getInvocationOffset());
		} catch (BadLocationException e) {
			e.printStackTrace();
			return 0;
		}
	}

	// trimmed
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
	public List<IContextInformation> computeContextInformation(
			ContentAssistInvocationContext context, IProgressMonitor monitor) {

		List<IContextInformation> list = new ArrayList<>();
		list.add(this);
		return list;
	}

	@Override
	public String getInformationDisplayString() {
		return null;
	}

	@Override
	public String getContextDisplayString() {
		return null;
	}

	@Override
	public Image getImage() {
		return null;
	}


	@Override
	public void sessionStarted() {

	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public void sessionEnded() {

	}

}