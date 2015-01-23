package at.bestsolution.bindex;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.osgi.service.indexer.impl.RepoIndex;

@Mojo(name = "bindex")
public class BIndexMojo extends AbstractMojo {

	/**
	 * directory containing bundles.
	 */
	@Parameter(required = true)
	private File repoDir;

	/**
	 * index to be generated.
	 */
	@Parameter(required = true)
	private File indexFile;

	
	@Parameter(defaultValue="true")
	private boolean compressed;
	
	@Parameter(defaultValue="false")
	private boolean pretty;
	
	/**
	 *  license URL of the repository
	 */
	@Parameter(defaultValue="")
	private String licenseUrl;
	
	/**
	 * Name of the repository.
	 */
	@Parameter(defaultValue="Unnamed")
	private String repositoryName;
	
	/**
	 * root (directory) URL of the repository
	 */
	@Parameter(required=true)
	private String rootUrl;
	
	/**
	 * Name of the configuration variable for the template for the URLs in the
	 * XML representation. A template can contain the following symbols:
	 * <ul>
	 * <li>%s is the symbolic name</li>
	 * <li>%v is the version number</li>
	 * <li>%f is the filename</li>
	 * <li>%p is the directory path</li>
	 * </ul>
	 */
	@Parameter
	private String urlTemplate;
	
	private Map<String, String> buildConfiguration() {
		final Map<String, String> configuration = new HashMap<>();
		
		configuration.put(RepoIndex.COMPRESSED, String.valueOf(compressed));
		configuration.put(RepoIndex.PRETTY, String.valueOf(pretty));
		configuration.put(RepoIndex.LICENSE_URL, licenseUrl);
		configuration.put(RepoIndex.REPOSITORY_NAME, repositoryName);
		configuration.put(RepoIndex.ROOT_URL, rootUrl);
		configuration.put(RepoIndex.URL_TEMPLATE, urlTemplate);
		
		return configuration;
	}
	
	public void execute() throws MojoExecutionException
    {
        getLog().info( "Hello, world." );
        getLog().info( "repoDir" + repoDir );
        getLog().info( "indexFile " + indexFile);
        
        final RepoIndex repoIndex = new RepoIndex();
        
        
       final Set<File> files = new HashSet<>();
       try {
	        Files.walkFileTree(Paths.get(repoDir.toURI()), new SimpleFileVisitor<Path>() {
	        	@Override
	        	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					getLog().debug("Checking " + file);
	        		if (file.getFileName().toString().endsWith(".jar")) {
	        			getLog().debug("Adding " + file);
		        		files.add(file.toFile());
					}
	        		return FileVisitResult.CONTINUE;
	        	}
	        });
       }
       catch (IOException e) {
    	   throw new MojoExecutionException("damn it", e);
       }
       
       Path indexFilePath = Paths.get(indexFile.toURI());
       
       if (!Files.exists(indexFilePath)) {
    	   try {
			Files.createFile(indexFilePath);
		} catch (IOException e) {
			throw new MojoExecutionException("damn it", e);
		}
       }
       
       try (OutputStream out = Files.newOutputStream(indexFilePath)) {
    	   repoIndex.index(files, out, buildConfiguration());
       }
       catch (IOException e) {
    	   throw new MojoExecutionException("damn it", e);
       }
       catch (Exception e) {
    	   throw new MojoExecutionException("damn it", e);
       }
        
       
    }
}