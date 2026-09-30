package us.bringardner.io.filesource.ftp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Properties;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import us.bringardner.core.ILogger.Level;
import us.bringardner.io.filesource.FileSource;
import us.bringardner.io.filesource.FileSourceFactory;
import us.bringardner.io.filesource.fileproxy.FileProxyFactory;
import us.bringardner.net.ftp.server.FtpServer;

/**
 * BJL-14: getCanonicalPath() meets the contract the shared isChildOfMine relies on
 * (absolute, "." and ".." resolved as the server's commands see them; FTP has no
 * links to follow), and isChildOfMine can't be escaped. Uses an in-process FTP server.
 */
public class FtpPathContractTest {

	static final int PORT = Integer.parseInt(System.getProperty("FtpContractPort", "8022"));
	static FtpServer server;
	static FtpFileSourceFactory factory;
	static Path serverRoot;

	@BeforeAll
	public static void setUp() throws Exception {
		serverRoot = Paths.get("target", "FtpContractRoot").toAbsolutePath();
		deleteAll(serverRoot);
		Files.createDirectories(serverRoot.resolve("base/root/sub"));
		Files.createDirectories(serverRoot.resolve("base/rootX"));
		Files.createDirectories(serverRoot.resolve("base/outside"));
		write(serverRoot.resolve("base/root/sub/file.txt"));
		write(serverRoot.resolve("base/rootX/x.txt"));
		write(serverRoot.resolve("base/outside/secret.txt"));

		server = new FtpServer();
		server.setFtpRoot(FileSourceFactory.getDefaultFactory().createFileSource(serverRoot.toString()));
		server.setPort(PORT);
		server.getLogger().setLevel(Level.ERROR);
		server.start();
		long start = System.currentTimeMillis();
		while( !server.isRunning() && System.currentTimeMillis()-start < 5000) {
			Thread.sleep(100);
		}
		assertTrue(server.isRunning(), "FTP server did not start");

		Properties prop = new Properties();
		prop.setProperty(FtpFileSourceFactory.PROP_USER, "ftp");
		prop.setProperty(FtpFileSourceFactory.PROP_PSWD, "foo@bar.com");
		prop.setProperty(FtpFileSourceFactory.PROP_HOST, "localhost");
		prop.setProperty(FtpFileSourceFactory.PROP_PORT, ""+PORT);
		prop.setProperty(FtpFileSourceFactory.PROP_SECURE, "false");
		factory = new FtpFileSourceFactory();
		factory.getLogger().setLevel(Level.ERROR);
		assertTrue(factory.connect(prop), "can't connect to the FTP server");
	}

	@AfterAll
	public static void tearDown() throws Exception {
		if( factory != null ) {
			factory.disConnect();
		}
		if( server != null ) {
			server.stop();
			long start = System.currentTimeMillis();
			while( server.isRunning() && System.currentTimeMillis()-start < 6000) {
				Thread.sleep(100);
			}
		}
		deleteAll(serverRoot);
	}

	private static FileSource at(String path) throws IOException {
		return factory.createFileSource("/base/"+path);
	}

	@Test
	public void pathsAreNormalized() throws IOException {
		assertEquals("/", FtpFileSource.normalize(""));
		assertEquals("/", FtpFileSource.normalize("/.."));
		assertEquals("/a/c", FtpFileSource.normalize("//a/./b/../c/"));
		assertEquals("/a/b", FtpFileSource.normalize("a\\b"));

		assertEquals("/base/root/sub/file.txt", at("root/./sub/../sub//file.txt").getCanonicalPath());
		assertEquals("/base/outside", at("root/../outside").getAbsolutePath());
		assertEquals("/", factory.createFileSource("/..").getCanonicalPath());
		// the server agrees: the normalized path names the real file
		assertTrue(at("root/./sub/../sub/file.txt").exists());
		assertTrue(at("root/../outside/secret.txt").exists());
	}

	@Test
	public void theTreeIsConsistent() throws IOException {
		FileSource top = factory.createFileSource("/base");
		assertEquals("base", top.getName());
		assertEquals("/", top.getParent());
		assertEquals("/", top.getParentFile().getCanonicalPath());
		FileSource root = factory.createFileSource("/");
		assertEquals("/", root.getCanonicalPath());
		assertEquals(null, root.getParent());
	}

	@Test
	public void relativePathsStartAtTheServersCurrentDirectory() throws IOException {
		String pwd = factory.getCurrentDirectory().getCanonicalPath();
		String expected = FtpFileSource.normalize(pwd+"/base/root");
		assertEquals(expected, factory.createFileSource("base/root").getCanonicalPath());
		assertTrue(factory.createFileSource("base/root").getCanonicalPath().startsWith("/"));
	}

	@Test
	public void getChildTakesPaths() throws IOException {
		FileSource root = at("root");
		assertEquals("/base/root/sub/file.txt", root.getChild("sub/file.txt").getCanonicalPath());
		assertTrue(root.getChild("sub/file.txt").exists());
		assertEquals("/base", root.getChild("..").getCanonicalPath());
		assertEquals("/base/outside/secret.txt", root.getChild("../outside/secret.txt").getCanonicalPath());
		assertTrue(root.getChild("../outside/secret.txt").exists());
		assertEquals("/base/root", root.getChild(".").getCanonicalPath());
		// a leading "/" is still relative to the parent
		assertEquals("/base/root/sub", root.getChild("/sub").getCanonicalPath());
	}

	@Test
	public void isChildOfMineCantBeEscaped() throws IOException {
		FileSource root = at("root");
		assertTrue(root.isChildOfMine(root));
		assertTrue(root.isChildOfMine(at("root/sub")));
		assertTrue(root.isChildOfMine(at("root/sub/file.txt")));
		assertTrue(root.isChildOfMine(root.getChild("sub/../sub/file.txt")));
		assertTrue(root.isChildOfMine(root.getChild("new/../also-new")));

		assertFalse(root.isChildOfMine(root.getChild("..")));
		assertFalse(root.isChildOfMine(root.getChild("../outside/secret.txt")));
		assertFalse(root.isChildOfMine(root.getChild("sub/../../outside/secret.txt")));
		// same name prefix, different directory
		assertFalse(root.isChildOfMine(at("rootX/x.txt")));
		assertFalse(root.isChildOfMine(root.getChild("a/../../rootX/x.txt")));
		assertFalse(root.isChildOfMine(at("")));
		assertFalse(root.isChildOfMine(null));
	}

	@Test
	public void theSharedIsChildOfMineIsUsed() {
		assertThrows(NoSuchMethodException.class,
				() -> FtpFileSource.class.getDeclaredMethod("isChildOfMine", FileSource.class));
	}

	@Test
	public void sameFileSystemMeansSameServerAccount() throws IOException {
		assertTrue(factory.isSameFileSystem(factory));
		assertTrue(factory.isSameFileSystem(account("LOCALHOST", PORT, "ftp")));
		assertFalse(factory.isSameFileSystem(account("localhost", PORT, "someone-else")));
		assertFalse(factory.isSameFileSystem(account("localhost", PORT+1, "ftp")));
		assertFalse(factory.isSameFileSystem(account("otherhost", PORT, "ftp")));
		assertFalse(factory.isSameFileSystem(new FileProxyFactory()));
		// a local file with the same path is never inside
		assertFalse(at("root").isChildOfMine(new FileProxyFactory().createFileSource("/base/root/sub")));
	}

	private static FtpFileSourceFactory account(String host, int port, String user) {
		FtpFileSourceFactory f = new FtpFileSourceFactory();
		Properties p = f.getConnectProperties();
		p.setProperty(FtpFileSourceFactory.PROP_HOST, host);
		p.setProperty(FtpFileSourceFactory.PROP_PORT, ""+port);
		p.setProperty(FtpFileSourceFactory.PROP_USER, user);
		f.setConnectionProperties(p);
		return f;
	}

	private static void write(Path file) throws IOException {
		Files.write(file, "data".getBytes(StandardCharsets.UTF_8));
	}

	private static void deleteAll(Path dir) throws IOException {
		if( !Files.exists(dir)) {
			return;
		}
		try(Stream<Path> s = Files.walk(dir)) {
			s.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
		}
	}
}
