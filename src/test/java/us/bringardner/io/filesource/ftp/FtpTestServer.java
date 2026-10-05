package us.bringardner.io.filesource.ftp;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Properties;

import us.bringardner.core.BjlLogger;
import us.bringardner.core.ILogger.Level;
import us.bringardner.io.filesource.FileSource;
import us.bringardner.io.filesource.FileSourceFactory;
import us.bringardner.io.filesource.test.FileSourceTestSupport;
import us.bringardner.io.filesource.test.TestServerController;
import us.bringardner.net.ftp.server.FtpServer;

/**
 * The FTP server the shared FileSource tests use: started on port FtpPort
 * (default 8021), anonymous login, files in target/FtpRoot.
 */
final class FtpTestServer implements TestServerController {

	static final int PORT = Integer.parseInt(System.getProperty("FtpPort", "8021"));
	static final long TIMEOUT = 6000;

	private FtpServer svr;
	private FileSource root;

	/**
	 * Starts a server, connects a factory to it, and sets them as the factory
	 * and server under test (FileSourceTestSupport stops it after the class).
	 */
	static FtpTestServer startAndConnect() throws IOException {
		System.setProperty("ILogger", BjlLogger.class.getName());
		FtpTestServer server = new FtpTestServer();
		FileSourceTestSupport.startServer(server, TIMEOUT);

		Properties prop = new Properties();
		prop.setProperty(FtpFileSourceFactory.PROP_USER, "ftp");
		prop.setProperty(FtpFileSourceFactory.PROP_PSWD, "foo@bar.com");
		prop.setProperty(FtpFileSourceFactory.PROP_HOST, "localhost");
		prop.setProperty(FtpFileSourceFactory.PROP_PORT, ""+PORT);
		prop.setProperty(FtpFileSourceFactory.PROP_ACCT, "bar.com");
		prop.setProperty(FtpFileSourceFactory.PROP_SECURE, "false");
		FtpFileSourceFactory factory = new FtpFileSourceFactory();		
		factory.getLogger().setLevel(Level.ERROR);
		assertTrue(factory.connect(prop),"Can't connect to server");
		FileSourceTestSupport.factory = factory;
		FileSourceTestSupport.server = server;
		return server;
	}

	@Override
	public boolean isRunning() {
		return svr != null && svr.isRunning();
	}

	@Override
	public void start() throws IOException {
		root = FileSourceFactory.getDefaultFactory().createFileSource("target/FtpRoot");
		if( !root.exists() ) {
			root.mkdirs();
		}
		svr = new FtpServer();
		svr.setFtpRoot(root);
		svr.setPort(PORT);
		svr.getLogger().setLevel(Level.ERROR);
		try {
			svr.start();
		} catch (Exception e) {
			throw new IOException("Can't start the FTP server", e);
		}
	}

	@Override
	public void stop() throws IOException {
		if( svr != null ) {
			try {
				svr.stop();
			} catch (Exception e) {
				throw new IOException("Can't stop the FTP server", e);
			}
		}
	}

	@Override
	public String getName() {
		return "FTP server on port "+PORT;
	}
}
