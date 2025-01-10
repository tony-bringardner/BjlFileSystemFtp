/**
 * <PRE>
 * 
 * Copyright Tony Bringarder 1998, 2025 <A href="http://bringardner.com/tony">Tony Bringardner</A>
 * 
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       <A href="http://www.apache.org/licenses/LICENSE-2.0">http://www.apache.org/licenses/LICENSE-2.0</A>
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  </PRE>
 *   
 *   
 *	@author Tony Bringardner   
 *
 *
 * ~version~V000.01.10-V000.01.09-V000.01.07-V000.01.06-V000.01.02-V000.00.01-V000.00.00-
 */
/*
 * Created on Dec 14, 2006
 *
 */
package us.bringardner.io.filesource.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import us.bringardner.core.BaseObject;
import us.bringardner.core.util.ThreadSafeDateFormat;
import us.bringardner.net.ftp.FTP;
import us.bringardner.net.ftp.client.ClientFtpResponse;
import us.bringardner.net.ftp.client.FtpClient;
import us.bringardner.net.ftp.server.commands.List;
import us.bringardner.net.ftp.server.commands.Mlst;
import us.bringardner.net.ftp.server.commands.Site;



public class FtpFile extends BaseObject {

	public static final char TYPE_DIR = 'd';
	public static final char TYPE_FILE = '-';
	//public static final ThreadSafeDateFormat YOUNG_DATE_FORMAT = new ThreadSafeDateFormat("MMM dd HH:mm yyyy");
	//public static final ThreadSafeDateFormat OLD_DATE_FORMAT = new ThreadSafeDateFormat("MMM dd yyyy");

	//public static final ThreadSafeDateFormat MLST_DATE_FORMAT = new ThreadSafeDateFormat("yyyyMMddHHmmSS.sss");
	//public static final ThreadSafeDateFormat MLST_SHORT_DATE_FORMAT = new ThreadSafeDateFormat("yyyyMMddHHmmSS");

	private String listEntry;
	private String parent;
	private FtpFileSourceFactory factory;
	private String name;
	private String owner;
	private String group;
	private long length;
	private long lastModified;
	private char type;
	private char[] permissions;
	// mls permissions are worthless and all servers support List so that what we'll use for permissions
	//private String mlstPermissions;
	private FtpFile parentFile;


	public FtpFile(String dirPath, String listEntry, FtpFileSourceFactory factory) throws IOException {
		this.factory = factory;
		this.listEntry = listEntry;
		this.parent = dirPath.trim();

		if( listEntry != null ) {
			parseEntry(listEntry);
		} else {
			//  Assume this is a directory
			this.type = TYPE_DIR;
			int idx = dirPath.lastIndexOf(FtpClient.SEPERATOR_CHAR);
			if(idx >= 0){
				this.parent = dirPath.substring(0,idx);
				this.name = dirPath.substring(idx+1);
			} else {
				this.parent = "";
				this.name = dirPath;

			}
		}
	}

	/**
	 * Only used in getParetFile
	 * 
	 * @param factory
	 */
	public FtpFile(FtpFileSourceFactory factory) {
		this.factory = factory;
	}

	@Override
	public void logDebug(String msg) {factory.logDebug(msg);}
	@Override
	public void logDebug(String msg, Throwable error) {
		factory.logDebug(msg, error);
	}

	@Override
	public void logError(String msg) {
		factory.logError(msg);
	}

	@Override
	public void logError(String msg, Throwable error) {
		factory.logError(msg, error);
	}

	@Override
	public void logInfo(String msg) {
		factory.logInfo(msg);
	}

	@Override
	public void logInfo(String msg, Throwable error) {
		factory.logInfo(msg, error);
	}

	@Override
	public boolean isDebugEnabled() {
		return factory.isDebugEnabled();
	}

	@Override
	public boolean isErrorEnabled() {
		return factory.isErrorEnabled();
	}
	@Override
	public boolean isInfoEnabled() {
		return factory.isInfoEnabled();
	}

	public String toString() {
		String ret = null;

		if(isDirectory()) {
			ret = getAbsolutePath()+" Directory";
		} else {
			ret = getAbsolutePath()+" "+getLength()+" "+(new Date(getLastModified()));
		}
		return ret;
	}


	public String getListEntry() {
		return listEntry;
	}

	public String getParent() {
		return parent;
	}

	public FtpFile getParetFile() {
		if( parentFile == null && name.length()>0 && !name.equals(FtpClient.SEPERATOR)) {
			synchronized (this) {
				if( parentFile == null ) {
					int idx = parent.lastIndexOf(FtpClient.SEPERATOR_CHAR);
					if( idx > 0 ) {
						String pp = parent.substring(0,idx);
						String pn = parent.substring(idx+1);
						parentFile = new FtpFile(factory);
						parentFile.listEntry = "";
						parentFile.name = pn;                        
						parentFile.parent = pp;
						parentFile.type = TYPE_DIR;

						/*
						 * These are probably wrong but FTP does not
						 * provide a way to get information about a directory 
						 * without listing every file in the parents parent.
						 */
						parentFile.owner = owner;
						parentFile.permissions = permissions;
						parentFile.lastModified = lastModified;
					}

				}
			}

		}

		return parentFile;
	}

	/**
	 * Local helper function to clean up the entry 
	 * by removing unwanted spaces.  This just makes it 
	 * easier to parser the entry.
	 *  
	 * @param entry received from remote system
	 * @return entry with unwanted spaces removed.
	 */
	private String cleanup(String entry) {

		StringBuffer ret = new StringBuffer(entry.length());
		byte [] data = entry.getBytes();
		byte lst = '\0';
		/*  /services/home/thewallicks.com/Backup/marie/My Documents/JFS
		 * 
		 * There are 9 data sections in an entry separated by whitespace.
		 * the last one is the name but it could contain whitespace.
		 * So, we want to stop before we change the name.
		 */
		int section=0;
		int idx=0;
		for (; section < 8 && idx < data.length; idx++) {
			if((lst=data[idx]) == ' ') {
				section++;
				while((data[idx+1]==' ' || data[idx+1]=='\t') && idx < data.length ) {
					idx++;
				}

			}
			ret.append((char)lst);
		}

		//  We've found the name so use it to set our field
		name = entry.substring(idx).trim();

		return ret.toString();
	}

	private void parseEntry(String entry) throws IOException {

		if(factory.getFtpClient().isMlstSupported() ){
			parseMlstEntry(entry);
		} else {
			parseUnixEntry(entry);
		}

	}

	private void parseMlstEntry(String entry) {
		String [] parts = entry.split(";");
		if( parts.length < 4 ) {
			//  Can't be a valid MLST entry
			parseUnixEntry(entry);
			return;
		}
		name = parts[parts.length-1].trim();
		if( name.length()>0 && name.charAt(0)=='/') {
			name = name.substring(1);
		}

		for (int idx = 0,sz=parts.length-1; idx < sz; idx++) {
			String [] tmp = parts[idx].split("=");
			String fact = tmp[0].trim().toUpperCase();
			if( fact.equals(FTP.MODIFY)) {
				/*
				 *    Symbolically, a time-val may be viewed as
				 *
				 * YYYYMMDDHHMMSS.sss
				 *
				 * The "." and subsequent digits ("sss") are optional.  However the "."
				 * MUST NOT appear unless at least one following digit also appears.
				 * 
				 */
				try {
					String time = tmp[1];
					if( tmp[1].indexOf('.') < 0 ) {
						time = time+".000";
					}
					lastModified = Mlst.TIME_FORMAT.parse(time).getTime();					
				} catch (ParseException e) {
					logError("Can't parse time",e);					
				}
			} else if( fact.equals(FTP.SIZE)) {
				length = Long.parseLong(tmp[1]);
			} else if( fact.equals(FTP.TYPE)) {
				if(tmp[1].equalsIgnoreCase("file")) {
					type = TYPE_FILE;
				} else {
					type = TYPE_DIR;
				}
			}
		}

	}



	private void parseUnixEntry(String entry) {
		//  perms   links owner       group  size  mm  dd hh:mm name   
		//drwxrwxrwx   4 QSYS           0    51200 Feb  9 21:28 home
		//-rw-------   1 peter                848  Dec 14 11:22 00README.txt
		//2> validate format
		// ??  How ??
		//1> Eliminate any double spaces in the text
		//int permPos = 0;
		//int linksPos = 1;
		int ownerPos = 2;
		int groupPos = 3;
		int sizePos = 4;
		int monthPos = 5;
		int dayPos = 6;
		int timePos = 7;

		/*
		 * Cleanup will remove filler spaces and set the name 
		 */
		entry = cleanup(entry.trim());

		type = entry.charAt(0);
		permissions = entry.substring(1,10).toCharArray();
		String [] parts = entry.split(" ");
		owner = parts[ownerPos];
		group = parts[groupPos];

		length = Long.parseLong(parts[sizePos]);

		String tmp = parts[monthPos]+" "+parts[dayPos]+" "+parts[timePos];


		ThreadSafeDateFormat format = List.oldDateFmt;
		if(tmp.indexOf(':') > 0 ) {
			// young format (what were these people thinking???
			format = List.newDateFmt;
			Calendar cal = Calendar.getInstance();
			tmp += " "+cal.get(Calendar.YEAR);
		} 

		try {
			lastModified = format.parse(tmp).getTime();
		} catch (ParseException ex) {
			logError("Can't parse date / time val ='"+tmp+"' entry="+entry);
		}


	}

	public boolean isDirectory() {
		return (type == TYPE_DIR);
	}

	public boolean isFile () {
		return !isDirectory();
	}

	public long getLastModified() {
		return lastModified;
	}

	public long getLength() {
		return length;
	}

	public String getName() {
		return name;
	}

	public String getOwner() {
		return owner;
	}

	public String getGroup() {
		return group;
	}

	public enum Permissions {
		OwnerRead('r'),
		OwnerWrite('w'),
		OwnerExecute('x'),

		GroupRead('r'),
		GroupWrite('w'),
		GroupExecute('x'),

		OtherRead('r'),
		OtherWrite('w'),
		OtherExecute('x');

	    public final char label;

	    private Permissions(char label) {
	        this.label = label;
	    }
	}
	
	//012345678
	//rwxr-xr-x
	public boolean canOwnerRead() throws IOException {
		return getPermissions()[Permissions.OwnerRead.ordinal()] == 'r';
	}

	public boolean canOwnerWrite() throws IOException {
		return getPermissions()[Permissions.OwnerWrite.ordinal()] == 'w';
	}

	public boolean canOwnerExecute() throws IOException {
		return getPermissions()[Permissions.OwnerExecute.ordinal()] == 'x';		
	}

	//012 345 678
	//rwx r-x r-x
	public boolean canGroupRead() throws IOException {
		return getPermissions()[Permissions.GroupRead.ordinal()] == 'r';
	}

	public boolean canGroupWrite() throws IOException {
		return getPermissions()[Permissions.GroupWrite.ordinal()] == 'w';
	}

	public boolean canGroupExecute() throws IOException {
		return getPermissions()[Permissions.GroupExecute.ordinal()] == 'x';		
	}

	//012 345 678
	//rwx r-x r-x
	public boolean canOtherRead() throws IOException {
		return getPermissions()[Permissions.OtherRead.ordinal()] == 'r';
	}

	public boolean canOtherWrite() throws IOException {
		return getPermissions()[Permissions.OtherWrite.ordinal()] == 'w';
	}

	public boolean canOtherExecute() throws IOException {
		return getPermissions()[Permissions.OtherExecute.ordinal()] == 'x';		
	}


	public char[] getPermissions() throws IOException {
		if( permissions == null || permissions.length != 9) {
			synchronized (this) {
				if( permissions == null || permissions.length != 9) {
					String[] resp= factory.getFtpClient().executeList(true, getAbsolutePath());
					// should be one and only one line
					if( resp!=null && resp.length==1) {
						if( resp[0].length()>=9) {
							permissions = resp[0].substring(1,10).toCharArray();
						}
					}
				}
			}			
		}

		if( permissions == null ) {
			// default to no permissions 
			return "---------".toCharArray();
		}

		return permissions;
	}

	public InputStream getInputStream() throws IOException {
		return getInputStream(false);
	}

	public InputStream getInputStream(long startingPos) throws IOException {
		return getInputStream(false, startingPos);
	}

	public InputStream getInputStream(boolean ascii) throws IOException {
		if( !isFile() ) {
			throw new IOException("Can't create stream from directory");
		}
		return factory.getFtpClient().getInputStream(getParent()+FtpClient.SEPERATOR+name, ascii);
	}

	public InputStream getInputStream(boolean ascii, long startingPos) throws IOException {
		if( !isFile() ) {
			throw new IOException("Can't create stream from directory");
		}
		return factory.getFtpClient().getInputStream(getParent()+FtpClient.SEPERATOR+name, ascii, startingPos);
	}

	public OutputStream getOutputStream(boolean ascii, boolean append) throws IOException {
		if( !isFile() ) {
			throw new IOException("Can't create stream from directory");
		}
		return factory.getFtpClient().getOutputStream(getParent()+FtpClient.SEPERATOR+name, ascii, append);
	}

	public String getAbsolutePath() {
		String p = getParent();
		String nm = getName();
		String ret = null;
		if( p.equals("/")) {
			ret = FtpClient.SEPERATOR+nm;
		} else {
			ret = p+FtpClient.SEPERATOR+nm;
		}


		return ret;
	}

	public boolean delete() throws IOException {

		return factory.getFtpClient().delete(getAbsolutePath());
	}

	public OutputStream getOutputStream() throws IOException {
		return getOutputStream(false, false);
	}

	public boolean mkdir() throws IOException {
		boolean ret = factory.getFtpClient().mkDir(getAbsolutePath());
		return ret;
	}

	public boolean mkdirs() throws IOException {
		boolean ret = factory.getFtpClient().mkDirs(getAbsolutePath());
		return ret;
	}

	public boolean renameTo(String newAbsolutePath) throws IOException {

		return factory.getFtpClient().rename(getAbsolutePath(), newAbsolutePath);
	}

	public OutputStream getAppendOutputStream() throws IOException {

		return getOutputStream(false, false);
	}

	/**
	 * This is not supported by standard Ftp.
	 * However, us.bringardner.net.ftp.server.Server supports a 'SITE' command
	 * that allows us to do it.
	 * 
	 * @param lastModifiedTime
	 * @return 
	 * @see us.bringardner.net.ftp.server.FtpServer
	 */
	public boolean setLastModified(long lastModifiedTime) {
		boolean ret = false;
		try {
			FtpClient client = factory.getFtpClient();
			ClientFtpResponse res = client.executeCommand(FTP.SITE,"modDate "+lastModifiedTime+" "+getAbsolutePath());

			if( res._getResponseCode() == FTP.REPLY_213_FILE_STATUS) {
				this.lastModified = lastModifiedTime;
				ret = true;
			}
		} catch (IOException e) {
			logError("Error setting modDate",e);
		}
		return ret;
	}

	public void dereferenceChildern() {
		// Nothing to do but probably should either here or in FtpFileSource.

	}
	
	
	public int getUnixPermitionValue(char perms []) throws IOException {
		
		int user = ((perms[Permissions.OwnerRead.ordinal()]=='r') ? 4:0)
				| ((perms[Permissions.OwnerWrite.ordinal()]=='w') ? 2:0)
				| ((perms[Permissions.OwnerExecute.ordinal()]=='x') ? 1:0)
				;
		
		int group = ((perms[Permissions.GroupRead.ordinal()]=='r') ? 4:0)
				| ((perms[Permissions.GroupWrite.ordinal()]=='w') ? 2:0)
				| ((perms[Permissions.GroupExecute.ordinal()]=='x') ? 1:0)
				;
		int other = ((perms[Permissions.OtherRead.ordinal()]=='r') ? 4:0)
				| ((perms[Permissions.OtherWrite.ordinal()]=='w') ? 2:0)
				| ((perms[Permissions.OtherExecute.ordinal()]=='x') ? 1:0)
				;
		
		int ret = (user<<6) | (group<<3) | other;
		
		return ret;
	}
	
	public boolean setPermision(Permissions p, boolean b) throws IOException {
		int idx = p.ordinal();
		char perms [] = getPermissions();
		char label = b ? p.label:'-';
		boolean ret = perms[idx] == label;
		// nothing to do if it's already set
		if( !ret ) {
			perms[idx] = label;
			int val = getUnixPermitionValue(perms);
			String arg = Integer.toOctalString(val);
			String path = getAbsolutePath();
			ClientFtpResponse resp = factory.getFtpClient().executeCommand(FTP.SITE, Site.CMD_CHMOD,arg,path);
			ret = resp.isPositiveComplet();
			//  just to make sure client stays in sync with server;
			permissions = null;
		}
		
		return ret;
	}




}
