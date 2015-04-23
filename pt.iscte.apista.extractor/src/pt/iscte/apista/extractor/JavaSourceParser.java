package pt.iscte.apista.extractor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class JavaSourceParser {
	private final ASTParser parser;
	private CompilationUnit unit;

	private JavaSourceParser(String source, String className, String[] classpath, String[] libSourceRoot, String[] encodings) {
		parser = ASTParser.newParser(AST.JLS8);
		parser.setResolveBindings(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);		
		parser.setEnvironment(classpath, libSourceRoot, encodings, true);
		parser.setUnitName(className);
		parser.setSource(source.toCharArray());
	}
	
	public static JavaSourceParser createFromFile(String javaFilePath, String libSourceRoot, String encoding) {
		validateFilePath(javaFilePath);
		validateLibSourceRoot(libSourceRoot);
		return new JavaSourceParser(readFileToString(javaFilePath), getClassName(javaFilePath), null, new String[]{libSourceRoot}, new String[]{encoding});
	}
	
	public static JavaSourceParser createFromSource(String source, String className, String libSourceRoot, String encoding) {
		validateLibSourceRoot(libSourceRoot);
		return new JavaSourceParser(source, className, null, new String[] {libSourceRoot}, new String[] {encoding});
	}
	
	public static JavaSourceParser createFromJar(String source, String jarFilePath) {
		
		return new JavaSourceParser(readFileToString(source), getClassName(source), new String[]{jarFilePath}, null, null);
	}

	private static void validateFilePath(String filePath) {
		File f = new File(filePath);
		if(!f.exists()) throw new IllegalArgumentException(filePath + " does not exist");
		if(!f.isFile())	throw new IllegalArgumentException(filePath + " is not a file");
		if(!f.getAbsolutePath().endsWith(".java")) throw new IllegalArgumentException(filePath + " is not a java file (.java)");
	}
	
	private static void validateLibSourceRoot(String filePath) {
		File f = new File(filePath);
		if(!f.exists()) throw new IllegalArgumentException(filePath + " does not exist");
		if(!f.isDirectory()) throw new IllegalArgumentException(filePath + " is not a directory");
	}

	public CompilationUnit getCompilationUnit() {
		if(unit == null) throw new IllegalStateException("Parse not executed yet");
		return unit;
	}

	public void parse(ASTVisitor visitor) {
		if(unit != null) throw new IllegalStateException("Parse already executed");
			
		unit = (CompilationUnit) parser.createAST(null);
		
//		if(unit.getProblems().length > 0)
//			throw new RuntimeException("code has compilation errors");
		
		unit.accept(visitor);
	}

	private static String readFileToString(String filePath) {
		StringBuilder fileData = new StringBuilder(1000);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filePath));

			char[] buf = new char[10];
			int numRead = 0;
			while ((numRead = reader.read(buf)) != -1) {
				String readData = String.valueOf(buf, 0, numRead);
				fileData.append(readData);
				buf = new char[1024];
			}
			reader.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		return  fileData.toString();	
	}
	
	private static String getClassName(String javaFilePath) {
		String trim = javaFilePath.substring(0, javaFilePath.lastIndexOf('.'));
		trim = trim.substring(trim.lastIndexOf(File.separatorChar)+1);
		return trim;
	}
}