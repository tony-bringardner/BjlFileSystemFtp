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
 * ~version~V000.01.10-V000.01.09-V000.01.08-V000.01.07-V000.01.06-V000.01.02-V000.01.00-V000.00.01-V000.00.00-
 */
/*
 * Created on Nov 24, 2006
 *
 */
package us.bringardner.io.filesource.ftp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipal;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ProgressMonitor;

import us.bringardner.io.filesource.FileSource;
import us.bringardner.io.filesource.FileSourceFactory;
import us.bringardner.io.filesource.FileSourceFilter;
import us.bringardner.io.filesource.ISeekableInputStream;
import us.bringardner.io.filesource.fileproxy.FileProxy;
import us.bringardner.io.filesource.ftp.FtpFile.Permissions;
import us.bringardner.core.BaseObject;
import us.bringardner.net.ftp.client.FtpClient;
import us.bringardner.net.ftp.client.ClientFtpResponse;

public class FtpFileSource extends BaseObject implements FileSource {

	private static final long serialVersionUID = 1L;

	private FtpFileSourceFactory factory;
	private String name;
	private String parent;

	private volatile FtpFile target;
	private volatile FtpFileSource parentFile;
	private volatile boolean testExist = true;

	private volatile FileSource[] kids_;

	public FtpFileSource(String name) {
		this.factory = (FtpFileSourceFactory)FileSourceFactory.getFileSourceFactory(FtpFileSourceFactory.FACTORY_ID);
		setName(name);
	}

	public FtpFileSource(String name, FtpFileSourceFactory factory) {

		this.factory = factory;
		setName(name);
	}

	/**
	 * @param parent
	 * @param name
	 * @param factory
	 */
	public FtpFileSource(String parent, String name, FtpFileSourceFactory factory) {
		this.parent = parent;
		this.factory = factory;
		setName(name);
	}


	/**
	 * @param file
	 * @param factory
	 */
	public FtpFileSource(String parent,FtpFile file, FtpFileSourceFactory factory) {
		this.parent = parent;
		this.target = file;
		this.factory = factory;
		this.name = file.getName();

	}

	/**
	 * 
	 * @param parentFile
	 * @param path
	 */
	public FtpFileSource(FtpFileSource parentFile, String path) {
		this.parentFile = parentFile;
		this.parent = parentFile.getAbsolutePath();
		this.factory = (FtpFileSourceFactory) parentFile.getFileSourceFactory();
		setName(path);
	}

	/**
	 * 
	 * @param source
	 * @param file
	 */
	public FtpFileSource(FtpFileSource source, FtpFile file) {
		this(source,file.getName());
		this.target = file;
	}

	public FtpFileSource(FtpFileSource p, FtpFile t,			FtpFileSourceFactory f) {
		this.parentFile = p;
		this.target = t;
		this.factory = f;
		this.name = t.getName();
		this.parent = p.getAbsolutePath();
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

	public boolean canRead() throws IOException {
		boolean ret = false;
		FtpFile tmp = getTarget();
		if( tmp != null ) {
			ret = tmp.canOwnerRead();
		}
		return ret;
	}

	public boolean canWrite() throws IOException {
		boolean ret = false;
		FtpFile tmp = getTarget();
		if( tmp != null ) {
			ret = tmp.canOwnerWrite();
		}
		return ret;
	}

	@Override
	public boolean canOwnerRead() throws IOException {
		boolean ret = false;
		FtpFile tmp = getTarget();
		if( tmp != null ) {
			ret = tmp.canOwnerRead();
		}
		return ret;
	}
	@Override
	public boolean canOwnerWrite() throws IOException {
		boolean ret = false;
		FtpFile tmp = getTarget();
		if( tmp != null ) {
			ret = tmp.canOwnerWrite();
		}
		return ret;
	}

	@Override
	public boolean canOwnerExecute() throws IOException {
		boolean ret = false;
		FtpFile tmp = getTarget();
		if( tmp != null ) {
			ret = tmp.canOwnerExecute();
		}
		return ret;
	}

	@Override
	public boolean canGroupRead() throws IOException {
		boolean ret = false;
		FtpFile tmp = getTarget();
		if( tmp != null ) {
			ret = tmp.canGroupRead();
		}
		return ret;
	}

	@Override
	public boolean canGroupWrite() throws IOException {
		boolean ret = false;
		FtpFile tmp = getTarget();
		if( tmp != null ) {
			ret = tmp.canGroupWrite();
		}
		return ret;
	}

	@Override
	public boolean canGroupExecute() throws IOException {
		boolean ret = false;
		FtpFile tmp = getTarget();
		if( tmp != null ) {
			ret = tmp.canGroupExecute();
		}
		return ret;
	}

	@Override
	public boolean canOtherRead() throws IOException {
		boolean ret = false;
		FtpFile tmp = getTarget();
		if( tmp != null ) {
			ret = tmp.canOtherRead();
		}
		return ret;
	}

	@Override
	public boolean canOtherWrite() throws IOException {
		boolean ret = false;
		FtpFile tmp = getTarget();
		if( tmp != null ) {
			ret = tmp.canOtherWrite();
		}
		return ret;
	}

	@Override
	public boolean canOtherExecute() throws IOException {
		boolean ret = false;
		FtpFile tmp = getTarget();
		if( tmp != null ) {
			ret = tmp.canOtherExecute();
		}
		return ret;
	}

	public int compareTo(Object o) {
		try {
			return o.toString().compareTo(getCanonicalPath());
		} catch (IOException e) {
			return -1;
		}
	}

	public boolean createNewFile() throws IOException {
		// This is not supported with FTP
		return false;
	}

	public boolean delete() {
		boolean ret = false;

		try {
			FtpFile tmp = getTarget();

			ret = tmp.delete();
			if( ret ) {
				target = null;
				testExist = true;
				FileSource p = getParentFile();
				if( p != null ) {
					if (p instanceof FtpFileSource) {
						FtpFileSource f = (FtpFileSource) p;
						f.kids_ = null;
					}
				}
			}
		} catch (IOException ex) {
			logError("Error in delete", ex);
		}

		return ret;
	}

	public boolean exists()  throws IOException {

		boolean ret = target != null;
		if( testExist ) {
			ret = getTarget() != null;
			testExist=false;
		}
		return ret;
	}

	public String getAbsolutePath() {

		try {
			return getCanonicalPath();
		} catch (IOException ex) {
			logError("Can't get CanonicalPath", ex);
			throw new RuntimeException("Can't get CanonicalPath ("+ex+")");
		}
	}

	public String getCanonicalPath() throws IOException {
		String ret = getName();
		String p = getParent();
		if(p != null ) {
			String nm = getName();

			if( p.equals("/")) {
				ret = FtpClient.SEPERATOR+nm;
			} else {
				ret = p+FtpClient.SEPERATOR+nm;
			}
		}

		return ret;
	}

	public FileSource getChild(String path) throws IOException {
		// child can't be absolute name
		path = FileSourceFactory.expandDots(path, '/');
		while( path.startsWith("/")) {
			path = path.substring(1);
		}
		FileSource ret = new FtpFileSource(this,path);
		return ret;
	}

	public String getContentType() {

		return FileProxy.getContentType(getName());
	}

	public long getCreateDate()  throws IOException {

		long ret = 0l;
		FtpFile tmp = getTarget();
		if( tmp != null ) {
			ret = tmp.getLastModified();
		}
		return ret;
	}

	public FileSourceFactory getFileSourceFactory() {

		return factory;
	}

	public InputStream getInputStream() throws FileNotFoundException, IOException {
		InputStream ret = null;
		FtpFile tmp = getTarget();
		if( tmp != null ) {
			ret = tmp.getInputStream();
		} else {
			throw new FileNotFoundException(getAbsolutePath()+" does not exist");
		}


		return ret;
	}

	public long getMaxVersion() {
		return getVersion();
	}

	public String getName() {
		return name;
	}

	public OutputStream getOutputStream() throws FileNotFoundException {
		return getOutputStream(false);
	}

	public OutputStream getOutputStream(boolean append) throws FileNotFoundException {
		OutputStream ret = null;

		try {
			String path = getAbsolutePath();
			FtpClient client = ((FtpFileSourceFactory)getFileSourceFactory()).getFtpClient();

			if( append) {
				ret = client.getAppendOutputStream(path); 
			} else {
				ret = client.getOutputStream(path);	 
			}

			// If it did not exist before in may now.


			FileSource p = getParentFile();
			if( p != null ) {
				if (p instanceof FtpFileSource) {
					FtpFileSource pfs = (FtpFileSource) p;
					pfs.kids_ = null;
				}
			}

			target = null;
			testExist = true;

		} catch (IOException ex) {
			logError("IOError geting Stream", ex);
			throw new FileNotFoundException("IOError geting Stream "+ex);
		}

		return ret;
	}


	public String getParent() {
		return parent;
	}

	public FileSource getParentFile() throws IOException {
		if( parentFile == null ) {
			String parent = getParent();
			if( parent != null && parent.length() > 0 ) {
				parentFile = (FtpFileSource)getFileSourceFactory().createFileSource(parent);
			}
		}
		return parentFile;
	}

	public String getPath() {
		String ret = null;

		String parent = getParent();
		if( parent == null ) {
			ret = parent+"/"+name;
		} else {
			ret = name;
		}

		return ret;
	}

	public long getVersion() {
		// Not supported
		return 0;
	}

	public long getVersionDate()  throws IOException {
		// version not supported
		return lastModified();
	}

	public boolean isChildOfMine(FileSource child) {
		boolean ret = (child instanceof FtpFileSource);
		if( ret ){
			try {
				ret = child.getCanonicalPath().startsWith(getCanonicalPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return ret;
	}

	public boolean isDirectory() throws IOException {
		FtpFile tmp = getTarget();
		return tmp != null && tmp.isDirectory();
	}

	/**
	 * @return
	 * @throws IOException 
	 * @throws  
	 */
	private FtpFile getTarget() throws IOException {
		if(target == null) {
			synchronized(this) {
				if(target == null ) {
					String path = getAbsolutePath();
					target = createFtpFile(path);
				}
			}
		}

		return target;
	}

	private FtpFile createFtpFile(String fullPath) throws IOException {
		FtpClient client = ((FtpFileSourceFactory)getFileSourceFactory()).getFtpClient();
		FtpFile ret = null;

		String parent = FtpClient.SEPERATOR;
		String name = fullPath;
		int idx = fullPath.lastIndexOf(FtpClient.SEPERATOR_CHAR);
		if( idx >= 0 ) {
			parent = fullPath.substring(0,idx);
			name = fullPath.substring(idx+1);
		} 

		if(client.isMlstSupported()) {
			//  All server should support this IMHO!

			ClientFtpResponse res = client.executeCommand(FtpClient.MLST,fullPath);
			if(res.isPositiveComplet()) {
				// Review draft-ietf-ftpext-mlst-16.txt for a description of the format.
				String [] lines = res.getResponseText().split("\n");
				if( lines.length == 3) {
					ret = new FtpFile(parent,lines[1],this.factory);
				}
			}
		} else { 
			/*
			 * This seems like a painful way to do this
			 * but FTP out of the box has no way to get a listing for a 
			 * directory (it will only return the files in the directory).
			 */


			FtpFile [] list = factory.listFiles(parent);
			if( list != null ) {
				for (idx = 0; ret == null && idx < list.length; idx++) {
					if(list[idx].getName().equals(name)) {
						ret = list[idx];
					}
				}
			}
		}
		return ret;
	}

	public boolean isFile()  throws IOException {

		return !isDirectory();
	}

	public boolean isVersionSupported() {
		// Not supported
		return false;
	}

	public long lastModified() throws IOException {
		long ret = 0;

		FtpFile tmp = getTarget();
		if( tmp != null ) {
			ret = tmp.getLastModified();
		}
		return ret;
	}

	public long length() throws IOException {
		long ret = 0;

		FtpFile tmp = getTarget();
		if( tmp != null ) {
			ret = tmp.getLength();
		}
		return ret;
	}

	public String[] list()  throws IOException {
		String [] ret = list((FileSourceFilter)null);

		return ret;
	}

	public String[] list(FileSourceFilter filter) throws IOException {
		String [] ret = null;

		FileSource [] list = listFiles(filter);
		if( list != null ) {
			ret = new String[list.length];
			for (int idx = 0; idx < list.length; idx++) {
				ret[idx] = list[idx].getName();
			}
		}

		return ret;

	}

	public FileSource[] listFiles() throws IOException {
		return listFiles((ProgressMonitor)null);
	}

	public FileSource[] listFiles(FileSourceFilter filter) throws IOException {
		FileSource[] ret = null;
		FileSource[] list = listFiles();
		if( list != null && filter != null ) {
			List<FileSource> tmp = new ArrayList<FileSource>();
			for (int idx = 0; idx < list.length; idx++) {
				if(filter.accept(list[idx])) {
					tmp.add(list[idx]);
				}
			}
			ret = (FileSource[])tmp.toArray(new FileSource[tmp.size()]);
		}

		return ret;
	}

	public boolean mkdir() {
		boolean ret = false;
		try {
			FtpFile tmp = getTarget();
			if( tmp != null ) {
				if((ret = tmp.mkdir())) {
					FileSource p = getParentFile();
					if( p != null ) {
						if (p instanceof FtpFileSource) {
							FtpFileSource pfs = (FtpFileSource) p;
							pfs.kids_ = null;
						}
					}
				}

			}
		} catch (IOException ex) {
			logError("Error in mkdir",ex);
		}
		return ret;
	}

	public boolean mkdirs()  throws IOException {

		if(exists()) {
			//already done 
			return true;
		}

		boolean ret = false;
		try {
			String path = getAbsolutePath();
			FtpClient client = ((FtpFileSourceFactory)getFileSourceFactory()).getFtpClient();
			ret = client.mkDirs(path);
			kids_ = null;
		} catch (IOException ex) {
			logError("Error in mkdirs",ex);
		}

		return ret;
	}


	public boolean setLastModified(long time) throws IOException {
		boolean ret = false;
		FtpFile tmp = getTarget();
		if( tmp != null ) {
			ret = tmp.setLastModified(time);
		}
		return ret;
	}

	public boolean setVersion(long version, boolean saveChange) throws IOException {
		return false;
	}

	public boolean setVersionDate(long time) {
		return false;
	}


	public String toString() {		
		try {
			return getCanonicalPath();
		} catch (IOException e) {
			return "Error "+e;
		}
	}

	public URL toURL() throws MalformedURLException {
		URL ret = null;

		String path = null;

		try {
			path = getCanonicalPath();
		} catch (IOException e) {
			throw new MalformedURLException("Can't get path");
		}
		if( path == null ) {
			path = "/";
		} else {
			if( !path.startsWith("/")) {
				path = "/"+path;
			}
		}

		String user = factory.getUser();
		if( user == null ) {
			user = "";
		}
		String pw = factory.getPasswd();
		if( pw == null ) {
			pw = "";
		}

		int port = factory.getPort();		 
		String host = factory.getHost();
		String portStr = port == FtpFileSourceFactory.DEFAULT_PORT ? "":":"+port;

		String url = FileSourceFactory.FILE_SOURCE_PROTOCOL+"://"+user+":"+pw+"@"+host+portStr+path+"?"+FileSourceFactory.QUERY_STRING_SOURCE_TYPE+"="+FtpFileSourceFactory.FACTORY_ID;

		ret = new URL(url);

		return ret;
	}

	public void setFactory(FtpFileSourceFactory factory) {
		this.factory = factory;
	}

	/**
	 * Put a path name into a consistent format
	 * @param path
	 * @return New path using the correct seperator char.
	 * @see FtpClient.PATH_SEPERATOR_CHAR
	 */
	private String setPathSeperator(String path) {
		char badSep = '\\';
		if( FtpClient.SEPERATOR_CHAR== badSep ) {
			badSep = '/';
		}
		return path.replace(badSep,FtpClient.SEPERATOR_CHAR);
	}

	/**
	 * Private method to set the name field.  The parent is determined and set.
	 * @param name
	 */
	private void setName(String name1) {

		String tmpName  = setPathSeperator(name1);
		int idx = tmpName.lastIndexOf(FtpClient.SEPERATOR_CHAR);
		if( idx > 0 ) {
			this.name = tmpName.substring(idx+1);
			if( parent != null ) {
				// Append this part 
				this.parent += FtpClient.SEPERATOR_CHAR+tmpName.substring(0,idx);
			} else {
				this.parent = tmpName.substring(0,idx);
			}
		} else if( idx <= 0 ) {
			//  I'm the root
			// TODO:  Is this right? this.parent = null;
			name = tmpName;
		} 

	}

	/* 
	 * Get an InputStream by doing an REST (if possible, otherwise startPos bytes are read and disgarded)
	 * @see us.bringardner.io.filesource.FileSource#getInputStream(long)
	 */
	public InputStream getInputStream(long startingPos) throws IOException {
		InputStream ret = null;
		FtpFile tmp = getTarget();
		if( tmp != null ) {
			ret = tmp.getInputStream(startingPos);
		} else {
			throw new FileNotFoundException(getAbsolutePath()+" does not exist");
		}

		return ret;
	}

	public void dereferenceChilderen() {
		// Nothing to do for now.  Probably should maintain a list of children :-)

	}

	public void refresh() throws IOException {
		target = null;
		getTarget();		
	}

	public boolean renameTo(FileSource dest) {
		boolean ret = false;

		try {
			FtpFile tmp = getTarget();
			if( tmp != null ) {
				ret = tmp.renameTo(dest.getAbsolutePath());
				if( ret ) {
					testExist = true;
					target = null;
					kids_ = null;
					((FtpFileSource) dest).testExist=true;
					((FtpFileSource) dest).target = null;

					FileSource p  =getParentFile();
					if (p instanceof FtpFileSource) {
						FtpFileSource pfs = (FtpFileSource) p;
						pfs.kids_ = null;
					}
					p  = dest.getParentFile();
					if (p instanceof FtpFileSource) {
						FtpFileSource pfs = (FtpFileSource) p;
						pfs.kids_ = null;
					}
				}
			}
		} catch (IOException ex) {
			logError("Error in rename",ex);
		}

		return ret;
	}


	public String getTitle() throws IOException {
		return factory.getUser()+"@"+"ftp://"+factory.getHost()+":"+factory.getPort()+getCanonicalPath();
	}

	public FileSource[] listFiles(ProgressMonitor progress) throws IOException {
		if( kids_ == null ) {
			synchronized (this) {
				if( kids_ == null ) {
					if( isDirectory() ) {
						FtpFile [] list = factory.listFiles(getAbsolutePath());
						if( list == null ) {
							kids_ = new FtpFileSource[0];
						} else {
							FtpFileSource[] tmp = new FtpFileSource[list.length];
							for (int idx = 0; idx < list.length; idx++) {
								tmp[idx] = new FtpFileSource(this,list[idx],factory);
							}
							kids_ = tmp;
						}
					}
				}
			}
		}

		return kids_;
	}


	public FileSource getLinkedTo() {
		// Not supported in FTP
		return null;
	}

	public boolean isHidden() {
		return getName().startsWith(".");
	}

	public ISeekableInputStream getSeekableInputStream() throws IOException {

		throw new IOException("ISeekableInputStream Not implemented in FTP");
	}

	@Override
	public GroupPrincipal getGroup() throws IOException {

		return new GroupPrincipal() {

			@Override
			public String getName() {
				return target.getGroup();
			}
		};
	}

	@Override
	public UserPrincipal getOwner() throws IOException {
		return new UserPrincipal() {

			@Override
			public String getName() {
				return target.getOwner();
			}
		};
	}

	@Override
	public boolean setExecutable(boolean executable) throws IOException {
		FtpFile tmp = getTarget();
		if( tmp != null ) {
			return tmp.setPermision(FtpFile.Permissions.OwnerExecute,executable);
		}	
		return false;
	}

	@Override
	public boolean setReadable(boolean readable) throws IOException {
		FtpFile tmp = getTarget();
		if( tmp != null ) {
			return tmp.setPermision(FtpFile.Permissions.OwnerRead,readable);
		}
		return false;
	}

	@Override
	public boolean setWritable(boolean writetable) throws IOException {
		FtpFile tmp = getTarget();
		if( tmp != null ) {
			return tmp.setPermision(FtpFile.Permissions.OwnerWrite,writetable);
		}
		return false;
	}

	@Override
	public boolean setGroupReadable(boolean b) throws IOException {
		FtpFile tmp = getTarget();
		if( tmp != null ) {
			return tmp.setPermision(FtpFile.Permissions.GroupRead,b);
		}
		return false;
	}

	@Override
	public boolean setGroupWritable(boolean b) throws IOException {
		FtpFile tmp = getTarget();
		if( tmp != null ) {
			return tmp.setPermision(FtpFile.Permissions.GroupWrite,b);
		}
		return false;
	}

	@Override
	public boolean setGroupExecutable(boolean b) throws IOException {
		FtpFile tmp = getTarget();
		if( tmp != null ) {
			return tmp.setPermision(FtpFile.Permissions.GroupExecute,b);
		}
		return false;
	}


	@Override
	public boolean setOtherReadable(boolean b) throws IOException {
		FtpFile tmp = getTarget();
		if( tmp != null ) {
			return tmp.setPermision(FtpFile.Permissions.OtherRead,b);
		}
		return false;
	}

	@Override
	public boolean setOtherWritable(boolean b) throws IOException {
		FtpFile tmp = getTarget();
		if( tmp != null ) {
			return tmp.setPermision(FtpFile.Permissions.OtherWrite,b);
		}
		return false;
	}

	@Override
	public boolean setOtherExecutable(boolean b) throws IOException {
		FtpFile tmp = getTarget();
		if( tmp != null ) {
			return tmp.setPermision(FtpFile.Permissions.OtherExecute,b);
		}
		return false;
	}



	/**
	 * ownerOnly - If true, the read permission applies only to the owner's read permission; 
	 * otherwise, it applies to everybody. If the underlying file system can not distinguish 
	 * the owner's read permission from that of others, 
	 * then the permission will apply to everybody, regardless of this value.
	 * @throws IOException 
	 */	
	@Override
	public boolean setReadable(boolean readable, boolean ownerOnly) throws IOException {
		boolean ret = setReadable(readable);
		if( ret ) {
			if( !ownerOnly) {
				setGroupReadable(readable);
				setOtherReadable(readable);
			}
		}
		return ret;
	}

	@Override
	public boolean setWritable(boolean writetable, boolean ownerOnly) throws IOException {
		boolean ret = setOtherWritable(writetable);
		if( ret ) {
			if( !ownerOnly ) {
				setGroupWritable(writetable);
				setOtherWritable(writetable);
			}
		} 
		return false;
	}

	@Override
	public boolean setExecutable(boolean executable, boolean ownerOnly) throws IOException {
		boolean ret = setOwnerExecutable(executable);
		if( ret ) {
			if( !ownerOnly ) {
				setGroupExecutable(executable);
				setOtherExecutable(executable);
			}
		}
		return false;
	}

	@Override
	public long lastAccessTime() throws IOException {
		// Not supported
		return 0;
	}

	@Override
	public long creationTime() throws IOException {
		// Not supported
		return 0;
	}

	@Override
	public boolean setLastModifiedTime(long time) throws IOException {
		// Not supported
		return false;
	}

	@Override
	public boolean setLastAccessTime(long time) throws IOException {
		FtpFile tmp = getTarget();
		if( tmp != null ) {
			return tmp.setLastModified(time);
		}
		return false;
	}

	@Override
	public boolean setCreateTime(long time) throws IOException {
		// Not supported
		return false;
	}

	@Override
	public boolean setOwnerExecutable(boolean b) throws IOException {
		FtpFile tmp = getTarget();
		if( tmp != null ) {
			return tmp.setPermision(Permissions.OwnerExecute, b);
		}
		return false;
	}

	@Override
	public boolean setOwnerReadable(boolean b) throws IOException {
		FtpFile tmp = getTarget();
		if( tmp != null ) {
			return tmp.setPermision(Permissions.OwnerRead, b);
		}
		return false;
	}

	@Override
	public boolean setOwnerWritable(boolean b) throws IOException {
		FtpFile tmp = getTarget();
		if( tmp != null ) {
			return tmp.setPermision(Permissions.OwnerWrite, b);
		}
		return false;
	}

	@Override
	public boolean setReadOnly() throws IOException {
		FtpFile tmp = getTarget();
		if( tmp != null ) {
			boolean ret = tmp.setPermision(Permissions.OwnerWrite, false);
			if( ret ) ret = tmp.setPermision(Permissions.OwnerExecute, false);
			if( ret ) ret = tmp.setPermision(Permissions.GroupWrite, false);
			if( ret ) ret = tmp.setPermision(Permissions.GroupExecute, false);
			if( ret ) ret = tmp.setPermision(Permissions.OtherWrite, false);
			if( ret ) ret = tmp.setPermision(Permissions.OtherExecute, false);
			return ret;
		}
		
		return false;
	}

	@Override
	public boolean setGroup(GroupPrincipal group) throws IOException {
		// Not supported
		return false;
	}

	@Override
	public boolean setOwner(UserPrincipal owner) throws IOException {
		// Not supported
		return false;
	}

}
