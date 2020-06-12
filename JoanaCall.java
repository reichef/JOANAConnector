package edu.kit.joana.component.connector;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Call for analyze
 */
public class JoanaCall {

	public static final int SERVER_PORT = 8004;

	public final String classPath;

	public final Flows knownFlows;

	public final List<Method> sources;

	public final List<Method> sinks;

	public final Optional<List<String>> allowedPackagesForUninitializedFields;

	public final Level logLevel;

	/**
	 * The passed lists have to have a working add method or else the serialization
	 * won't work
	 */
	public JoanaCall(String classPath, Flows knownFlows, List<Method> sources, List<Method> sinks,
			Optional<List<String>> allowedPackagesForUninitializedFields, Level logLevel) {
		this.classPath = classPath;
		this.knownFlows = knownFlows;
		this.sources = sources;
		this.sinks = sinks;
		this.allowedPackagesForUninitializedFields = allowedPackagesForUninitializedFields;
		this.logLevel = logLevel;
	}

	/**
	 * The passed lists have to have a working add method or else the serialization
	 * won't work
	 */
	public JoanaCall(String classPath, Flows knownFlows, List<Method> sources, List<Method> sinks,
			List<String> allowedPackagesForUninitializedFields, Level logLevel) {
		this(classPath, knownFlows, sources, sinks, Optional.of(allowedPackagesForUninitializedFields), logLevel);
	}

	/**
	 * The passed lists have to have a working add method or else the serialization
	 * won't work
	 */
	public JoanaCall(String classPath, Flows knownFlows, List<Method> sources, List<Method> sinks, Level logLevel) {
		this(classPath, knownFlows, sources, sinks, Optional.empty(), logLevel);
	}

	public JoanaCall setClassPath(String newClassPath) {
		return new JoanaCall(newClassPath, knownFlows, sources, sinks, allowedPackagesForUninitializedFields, logLevel);
	}

	public void store(Path path) {
		Util.store(path, this);
	}

	public static JoanaCall load(Path path) {
		return Util.load(path);
	}

	private static void deleteFolder(Path folder) {
		try {
			Files.walk(folder).map(Path::toFile).sorted((o1, o2) -> -o1.compareTo(o2)).forEach(File::delete);
			Files.deleteIfExists(folder);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void roundTrip(Consumer<JoanaCall> processor) {
		Path tmpFile;
		Path tmpFolder = null;
		try {
			tmpFile = Files.createTempFile("", ".zip");
			tmpFolder = Files.createTempDirectory("");
			storeWithClassPath(tmpFile);
			processor.accept(loadZipFile(tmpFile, tmpFolder));
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (tmpFolder != null) {
				deleteFolder(tmpFolder);
			}
		}
	}

	/**
	 * Creates a zip file that contains the content of the classpath and this object
	 * (in file call.json)
	 *
	 * @param path path of the generated zip file
	 */
	public void storeWithClassPath(Path path) {
		try {
			Files.createDirectories(path.toAbsolutePath().getParent());
			Map<String, String> env = new HashMap<>();
			env.put("create", "true");
			if (Files.exists(path)) {
				Files.delete(path);
			}
			try (FileSystem fs = FileSystems.newFileSystem(URI.create("jar:" + path.toUri().toString()), env, null)) {
				for (String classPathPart : classPath.split(System.getProperty("path.separator"))) {
					Path classPathPartPath = Paths.get(classPathPart).toAbsolutePath();
					if (Files.isDirectory(classPathPartPath)) {
						Files.walk(classPathPartPath).forEach(filePath -> {
							try {
								Path newPath = fs.getPath("/" + classPathPartPath.relativize(filePath).toString());
								if (Files.exists(newPath)) {
									return;
								}
								if (Files.isDirectory(filePath)) {
									Files.createDirectories(newPath);
								} else {
									copyFileAndMergeIfNecessary(filePath, newPath, fs);
								}
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						});
					} else {
						copyFileAndMergeIfNecessary(classPathPartPath,
								fs.getPath(classPathPartPath.getFileName().toString()), fs);
					}
				}
				Path tmpPath = Files.createTempFile("", "");
				setClassPath(".").store(tmpPath);
				Files.copy(tmpPath, fs.getPath("/call.json"));
				Files.delete(tmpPath);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void copyFileAndMergeIfNecessary(Path file, Path newFile, FileSystem fs) {
		if (isZip(file)) {
			mergeFromZip(file, fs);
		} else {
			try {
				Files.copy(file, newFile);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static JoanaCall loadZipFile(Path zip, Path resultPath) {
		try {
			if (!Files.exists(resultPath)) {
				Files.createDirectory(resultPath);
			}
			ZipFile zipFile = new ZipFile(zip.toFile());
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry zipEntry = entries.nextElement();
				Path newFile = resultPath.resolve(Paths.get(zipEntry.getName()));
				if (!Files.exists(newFile.getParent())) {
					Files.createDirectories(newFile.getParent());
				}
				if (!zipEntry.isDirectory()) {
					Files.copy(zipFile.getInputStream(zipEntry), newFile);
				}
			}
			JoanaCall loaded = load(resultPath.resolve("call.json"));
			if (!loaded.classPath.equals(".")) {
				throw new RuntimeException();
			}
			return loaded.setClassPath(resultPath.toAbsolutePath().toString());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static boolean isZip(Path path) {
		try {
			return new ZipInputStream(Files.newInputStream(path)).getNextEntry() != null;
		} catch (IOException ignored) {
		}
		return false;
	}

	private static void mergeFromZip(Path zip, FileSystem fs) {
		try {
			ZipFile zipFile = new ZipFile(zip.toFile());
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry zipEntry = entries.nextElement();
				Path newFile = fs.getPath("/" + zipEntry.getName());
				if (!Files.exists(newFile.getParent())) {
					Files.createDirectories(newFile.getParent());
				}
				if (!zipEntry.isDirectory() && !Files.exists(newFile)) {
					Files.copy(zipFile.getInputStream(zipEntry), newFile);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Loads a zip file into a temporary folder and processes it with the passed
	 * method
	 */
	public static void loadZipFile(Path path, Consumer<JoanaCall> processor) {
		try {
			Path tmpFolder = Files.createTempDirectory("");
			JoanaCall joanaCall = loadZipFile(path, tmpFolder);
			processor.accept(joanaCall);
			deleteFolder(tmpFolder);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public JoanaCallReturn processOnServer(String server) {
		return processOnServer(server, SERVER_PORT);
	}
	
	public JoanaCallReturn processOnServer(String server, String persistingLocation, String persistingName) {
		return processOnServer(server, SERVER_PORT, persistingLocation, persistingName);
	}


	public JoanaCallReturn processOnServer(String server, int port) {
		Path tmpFile = null;
		try {
			tmpFile = createJOANAAnalysisContent();
			return executeRemoteAnalysis(tmpFile, server, port);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				cleanUpTmpAnalysisContentFile(tmpFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public JoanaCallReturn processOnServer(String server, int port, String persistingLocation, String persistingName) {
		Path tmpFile = null;
		try {
			tmpFile = createJOANAAnalysisContent();
			return executeRemoteAnalysis(tmpFile, server, port);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				cleanUpTmpAnalysisContentFile(tmpFile, persistingLocation, persistingName);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public Path createJOANAAnalysisContent() throws IOException {
		Path tmpFile = null;
		tmpFile = Files.createTempFile("", ".zip");
		storeWithClassPath(tmpFile);

		return tmpFile;
	}

	public JoanaCallReturn executeRemoteAnalysis(Path tmpFile, String server, int port) throws IOException {
		URL url = new URL("http://" + server + ":" + port);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setDoOutput(true);
		con.setChunkedStreamingMode(10000);
		OutputStream outputStream = con.getOutputStream();
		Files.copy(tmpFile, outputStream);
		outputStream.close();
		String str = new BufferedReader(new InputStreamReader(con.getInputStream())).lines()
				.collect(Collectors.joining("\n"));
		return JoanaCallReturn.fromJson(str);
	}

	public void cleanUpTmpAnalysisContentFile(Path tmpFile) throws IOException {	
		if (tmpFile != null) {
			Files.delete(tmpFile);
		}
	}
	
	public void cleanUpTmpAnalysisContentFile(Path tmpFile, String persistingLocation, String persistingName) throws IOException {	
		if (tmpFile != null) {
			Files.move(tmpFile, Paths.get(persistingLocation, persistingName + ".zip"), StandardCopyOption.REPLACE_EXISTING);
		}
	}
}
