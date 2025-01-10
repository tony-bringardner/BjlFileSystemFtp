package us.bringardner.io.filesource.ftp;

public class FtpsFileSourceFactory extends FtpFileSourceFactory {

	private static final long serialVersionUID = 1L;

	public FtpsFileSourceFactory() {
		super();
		setSecure(true);
	}
	
	@Override
	public String getTitle() {
		return super.getTitle()+" Secure";
	}
	
	@Override
	public String getTypeId() {
		return "ftps";
	}
	
}
