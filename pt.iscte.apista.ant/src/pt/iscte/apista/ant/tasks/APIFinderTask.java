package pt.iscte.apista.ant.tasks;

//import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.io.File;

import org.apache.tools.ant.taskdefs.optional.ssh.Directory;
import org.apache.tools.ant.util.FileUtils;

import pt.iscte.apista.ant.APISTATask;

/**
 * Task used to find if the repository folder provided, contains projects that
 * use the API
 */
public class APIFinderTask extends APISTATask {

	@Override
	public void execute() {

		// Must be called in order to initialize the configuration object
		super.execute();

		// Returns a list of all the files that contain an import with the
		// libRootPackage defined in the properties file.
		List<String> apiUsage = getAPIUsageOnRepositories(
				configuration.getLibRootPackage(), configuration.getRepPath());
		// Filters the previous list in order to get only the project's root
		// folder
		Set<String> set = getRootFolders(apiUsage, configuration.getRepPath());

		// Writes the project's folder absolute path to a file called
		// "results.txt" in the Resources Folder
		PrintWriter pw = null;
		try {
			System.out.println("PROJECTS FOUND: " + set.size());
			pw = new PrintWriter(new File(configuration.getOutputFolder()
					+ "results.txt"));
			for (String s : set) {
				pw.write(s + "\n");
//				System.out.println(Paths.get(s).getParent());
//				System.out.println(Paths.get(configuration.getTargetPath()));
				Files.walkFileTree(Paths.get(s), new CopyDirVisitor(Paths.get(configuration.getRepPath()),Paths.get(configuration.getTargetPath())));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			pw.close();
		}

		System.out.println(apiUsage);

	}

	private class CopyDirVisitor extends SimpleFileVisitor<Path> {
		private Path fromPath;
	    private Path toPath;
		
	    public CopyDirVisitor(Path fromPath, Path toPath) {
	    	this.fromPath = fromPath;
	        this.toPath = toPath;
	    }

	    @Override
	    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {

	        Path targetPath = toPath.resolve(fromPath.relativize(dir));
	        if(!Files.exists(targetPath)){
	            Files.createDirectory(targetPath);
	        }
	        return FileVisitResult.CONTINUE;
	    }

	    @Override
	    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
	        Files.copy(file, toPath.resolve(fromPath.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
	        return FileVisitResult.CONTINUE;
	    }
	}

	/**
	 * @param apiUsage
	 *            List of strings with the files that contain an import to the
	 *            API
	 * @param rootFolder
	 *            folder that contains a set of projects
	 * @return returns a list of unique paths to the projects that use the API
	 */
	private Set<String> getRootFolders(List<String> apiUsage, String rootFolder) {
		// Pattern used to escape especial characters in the file paths
		String pattern = Pattern.quote(File.separator);
		// Splits the root folder in order to obtain its length to concatenate
		// with the project's folder
		String[] splittedRootFolder = rootFolder.split(pattern);
		// Using a Set so that there are no duplicates
		Set<String> set = new HashSet<>();
		for (String s : apiUsage) {
			// Splits the file's path
			String[] splittedString = s.split(pattern);
			// Concatenates the project's root folder with the the rest of the
			// absolute path
			String absolutePath = rootFolder + File.separator
					+ splittedString[splittedRootFolder.length];
			set.add(absolutePath);
		}

		return set;
	}

	/**
	 * @param libRootPackage
	 *            API's root package to search in the .java files' imports
	 * @param repPath
	 *            root folder containing the repository projects
	 * @return returns a list of all the files that contain an import with the
	 *         API's libRootPackage
	 */
	private List<String> getAPIUsageOnRepositories(String libRootPackage,
			String repPath) {
		// Initialize a new List that will contain the paths to the files
		List<String> list = new ArrayList<>();
		// Recursive method to fill the previous list with the paths to the
		// files
		searchForAPIUsage(list, libRootPackage, new File(repPath));
		return list;
	}

	/**
	 * Recursive method that adds the file paths containing imports with the
	 * API's libRootPackage
	 * 
	 * @param list
	 *            List that will contain the files' paths
	 * @param libRootPackage
	 *            API's root package to lookup
	 * @param file
	 *            File initially contains the root folder of the repository and
	 *            recursively changes to contain the .java files in those
	 *            projects
	 * @return Returns whether the file provided contains an import to the API's
	 *         libRootPackage
	 */
	private boolean searchForAPIUsage(List<String> list, String libRootPackage,
			File file) {

		// Condition to stop the recursiveness. Checks if the File is a file
		// i.e. not a directory and contains the .java extension
		if (file.getAbsolutePath().endsWith(".java") && file.isFile()) {
			// Checks if the file contains an import statement to the API's
			// libRootPackage
			if (fileContainsImportWithAPIPackage(file, libRootPackage)) {
				// Returns true if it does
				return true;
			}
		}

		// Method's recursive part. Checks if the file is a directory and not a
		// hidden one
		if (file.isDirectory() && !file.isHidden()) {
			// If so, lists the files and recursively call this method in order
			// to check if it contains the import statement.
			for (File f : file.listFiles()) {
				if (searchForAPIUsage(list, libRootPackage, f)) {
					// If this method returns true i.e. if the file contains an
					// import statement to the API's libRootPackage,
					// the file's absolute path will be added to the list that
					// will be returned
					list.add(f.getAbsolutePath());
				}
			}
		}
		// Returns false if the file was a directory or a file not containing
		// the .java extension in order to go up one level
		return false;
	}

	/**
	 * Checks if the file provided contains an import statement with the API's
	 * libRootPackage
	 * 
	 * @param f
	 *            File to be opened and checked if it contains an import to the
	 *            API's libRootPackage
	 * @param libRootPackage
	 *            API's package to lookup for
	 * @return Returns true if the file contains the import statement and false
	 *         if it doesn't.
	 */
	private boolean fileContainsImportWithAPIPackage(File f,
			String libRootPackage) {
		Scanner scanner = null;
		try {
			// Open the file and iterate over its files to lookup for the import
			// statement
			scanner = new Scanner(f);
			// Iterates over the whole file
			while (scanner.hasNextLine()) {
				// Checks if the the line contains the libRootPackage
				if (scanner.nextLine().contains(libRootPackage)) {
					// Returns true if the line contains the statement
					return true;
				}
			}
		} catch (FileNotFoundException e) {
			System.err.println("File " + f.getName()
					+ " was not found in task APIFinderTask");
			e.printStackTrace();
		} finally {
			scanner.close();
		}
		// Returns false if the file doesn't contain any statement containing
		// the libRootPackage
		return false;
	}

	public static void main(String[] args) {
		new APIFinderTask().execute();
	}
}
