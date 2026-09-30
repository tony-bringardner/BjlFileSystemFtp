package us.bringardner.io.filesource.ftp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

import us.bringardner.io.filesource.ftp.FtpFileSourceFactory;

/** BJL-21: the factory names its secret connection properties exactly. */
public class FtpSecretPropertiesTest {

	@Test
	public void secretPropertiesAreExactlyTheseOnes() {
		FtpFileSourceFactory factory = new FtpFileSourceFactory();
		List<String> secrets = Arrays.asList(FtpFileSourceFactory.PROP_PSWD, FtpFileSourceFactory.PROP_ACCT);
		TreeSet<String> names = new TreeSet<>(Arrays.asList(FtpFileSourceFactory.PROP_USER, FtpFileSourceFactory.PROP_HOST, FtpFileSourceFactory.PROP_PORT));
		names.addAll(secrets);
		names.addAll(factory.getConnectProperties().stringPropertyNames());
		for(String name : names) {
			assertEquals(secrets.contains(name), factory.isSecretProperty(name), name);
		}
	}
}
