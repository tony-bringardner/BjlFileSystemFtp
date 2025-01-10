package us.bringardner.io.filesource.sftp;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Properties;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import junit.framework.TestCase;
import us.bringardner.core.BjlLogger;
import us.bringardner.core.ILogger.Level;
import us.bringardner.io.filesource.FileSource;
import us.bringardner.io.filesource.FileSourceFactory;
import us.bringardner.io.filesource.ftp.FtpFileSourceFactory;
import us.bringardner.io.filesource.test.FileSourceAbstractTestClass;
import us.bringardner.net.ftp.server.FtpServer;

public class TestFtp extends FileSourceAbstractTestClass {


	private static int ftpPort;
	private static FtpServer svr;
	private static FileSource root;

	@BeforeAll
	public static void setUpBeforeAll() throws IOException {
		
		System.setProperty("ILogger", BjlLogger.class.getName());
		startFtpServer();
		
		FileSourceAbstractTestClass.localTestFileDirPath = "TestFiles";
		FileSourceAbstractTestClass.localCacheDirPath = "target/CacheFiles";
		FileSourceAbstractTestClass.remoteTestFileDirPath = "TestFiles";
		
		
		
		
		Properties prop = new Properties();
		prop.setProperty(FtpFileSourceFactory.PROP_USER, "ftp");
		prop.setProperty(FtpFileSourceFactory.PROP_PSWD, "foo@bar.com");
		prop.setProperty(FtpFileSourceFactory.PROP_HOST, "localhost");
		prop.setProperty(FtpFileSourceFactory.PROP_PORT, ""+ftpPort);
		prop.setProperty(FtpFileSourceFactory.PROP_ACCT, "bar.com");
		prop.setProperty(FtpFileSourceFactory.PROP_SECURE, "false");
		factory = new FtpFileSourceFactory();		
		factory.getLogger().setLevel(Level.ERROR);
		assertTrue(factory.connect(prop),"Can't connect to server");	
	}
	
	public  static void startFtpServer()  {
		// start a server
		try {
			ftpPort = Integer.parseInt(System.getProperty("FtpPort", "8021"));

			//  Force a FileProxy storage

			String path = "target/FtpRoot";
			root = FileSourceFactory.getDefaultFactory().createFileSource(path);
			if( !root.exists() ) {
				root.mkdirs();
			}

			svr = new FtpServer();
			svr.setFtpRoot(root);
			svr.setPort(ftpPort);
			svr.getLogger().setLevel(Level.ERROR);
			svr.start();
			int cnt = 0;
			// wait for svr to start
			while(cnt < 5 && !svr.isRunning()) {
				cnt++;
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
			TestCase.assertTrue(svr.isRunning());
			

		} catch(Throwable e) {
			System.out.println("Can't start the FTP Server error = "+e);
		}
	}

	@AfterAll
	public static  void stopFtpServer() throws IOException {
		if( svr != null ) {
			try {
				svr.stop();
				long start = System.currentTimeMillis();
				while(svr.isRunning() && System.currentTimeMillis()-start < 6000) {
					Thread.sleep(100);
				}
				
			} catch (Exception e) {
			}
			if( root != null ) {
				try {
					root.delete();
				} catch (Exception e) {
				}
			}
		}
	}
	
}
