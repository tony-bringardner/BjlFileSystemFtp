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
 * ~version~V000.01.02-V000.00.01-V000.00.00-
 */
package us.bringardner.io.filesource.ftp;

import java.awt.Dimension;
import java.util.Properties;

import us.bringardner.io.filesource.IConnectionPropertiesEditor;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 *
 * @author  Tony Bringardner
 */
public class FtpEditPropertiesPanel extends javax.swing.JPanel implements
		IConnectionPropertiesEditor {

	private static final long serialVersionUID = 1L;
	private Properties prop = new Properties();
	private javax.swing.JTextField hostNameFld;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JLabel jLabel4;
	private javax.swing.JPasswordField passwordFld;
	private javax.swing.JTextField portFld;
	private javax.swing.JTextField userIdFld;
	private JTextField accountTextField;

	
	/** Creates new form FtpEditPropertiesPanel */
	public FtpEditPropertiesPanel() {
		initComponents();
	}

	public FtpEditPropertiesPanel(Properties prop) {
		initComponents();
		setProperties(prop);
	}

	private void initComponents() {
		setPreferredSize(new Dimension(472, 208));		
		jLabel1 = new javax.swing.JLabel();
		jLabel1.setBounds(44, 40, 53, 14);
		jLabel2 = new javax.swing.JLabel();
		jLabel2.setBounds(44, 71, 83, 14);
		jLabel3 = new javax.swing.JLabel();
		jLabel3.setBounds(44, 102, 83, 14);
		jLabel4 = new javax.swing.JLabel();
		jLabel4.setBounds(44, 147, 53, 14);
		userIdFld = new javax.swing.JTextField();
		userIdFld.setText("tony");
		userIdFld.setBounds(137, 34, 54, 20);
		userIdFld.setColumns(6);
		passwordFld = new javax.swing.JPasswordField();
		passwordFld.setBounds(137, 65, 126, 20);
		passwordFld.setColumns(15);
		hostNameFld = new javax.swing.JTextField();
		hostNameFld.setBounds(137, 96, 246, 20);
		hostNameFld.setColumns(30);
		portFld = new javax.swing.JTextField();
		portFld.setBounds(137, 141, 54, 20);
		portFld.setColumns(6);
		setLayout(null);

		jLabel1.setText("User :");
		add(jLabel1);

		jLabel2.setText("Password :");
		add(jLabel2);

		jLabel3.setText("Host Name :");
		add(jLabel3);

		jLabel4.setText("Port :");
		add(jLabel4);

		userIdFld.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				userIdFldActionPerformed(evt);
			}
		});
		userIdFld.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusLost(java.awt.event.FocusEvent evt) {
				userIdFldFocusLost(evt);
			}
		});
		add(userIdFld);

		passwordFld.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				passwordFldActionPerformed(evt);
			}
		});
		passwordFld.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusLost(java.awt.event.FocusEvent evt) {
				passwordFldFocusLost(evt);
			}
		});
		add(passwordFld);

		hostNameFld.setText("ftp.bringardner.com");
		hostNameFld.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				hostNameFldActionPerformed(evt);
			}
		});
		hostNameFld.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusLost(java.awt.event.FocusEvent evt) {
				hostNameFldFocusLost(evt);
			}
		});
		add(hostNameFld);

		portFld.setText("10021");
		portFld.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				portFldActionPerformed(evt);
			}
		});
		portFld.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusLost(java.awt.event.FocusEvent evt) {
				portFldFocusLost(evt);
			}
		});
		add(portFld);
		
		JLabel lblNewLabel = new JLabel("Account : ");
		lblNewLabel.setBounds(202, 39, 75, 16);
		add(lblNewLabel);
		
		accountTextField = new JTextField();
		accountTextField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				setAccount();
			}
		});
		accountTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setAccount();
			}
		});
		accountTextField.setText("bringardner.us");
		accountTextField.setBounds(271, 34, 130, 26);
		add(accountTextField);
		accountTextField.setColumns(10);
	}
	
	private void setAccount() {
		setProperty(FtpFileSourceFactory.PROP_ACCT, accountTextField.getText());		
	}

	private void portFldFocusLost(java.awt.event.FocusEvent evt) {
		setPort();
	}

	private void hostNameFldFocusLost(java.awt.event.FocusEvent evt) {
		setHostName();
	}

	private void passwordFldFocusLost(java.awt.event.FocusEvent evt) {
		setPassword();
	}

	private void userIdFldFocusLost(java.awt.event.FocusEvent evt) {
		setUserId();
	}

	private void passwordFldActionPerformed(java.awt.event.ActionEvent evt) {
		setPassword();

	}

	/**
	 * 
	 */
	private void setPassword() {
		char[] tmp = passwordFld.getPassword();
		String pw = new String(tmp);

		setProperty(FtpFileSourceFactory.PROP_PSWD, pw);

	}

	private void portFldActionPerformed(java.awt.event.ActionEvent evt) {
		setPort();
	}

	private void hostNameFldActionPerformed(java.awt.event.ActionEvent evt) {
		setHostName();
	}

	/**
	 * 
	 */
	private void setHostName() {
		setProperty(FtpFileSourceFactory.PROP_HOST, hostNameFld.getText());
	}

	private void setProperty(String name, String host) {
		if (host != null && (host = host.trim()).length() > 0) {
			prop.setProperty(name, host);
		} else {
			prop.remove(name);
		}

	}

	private void userIdFldActionPerformed(java.awt.event.ActionEvent evt) {
		setUserId();
	}

	/**
	 * 
	 */
	private void setUserId() {
		setProperty(FtpFileSourceFactory.PROP_USER, userIdFld.getText());
	}

	private void setPort() {
		String port = portFld.getText().trim();
		if (port.length() == 0) {
			port = "25";
			portFld.setText(port);
		}

		setProperty(FtpFileSourceFactory.PROP_PORT, port);
	}

	
	
	/* (non-Javadoc)
	 * @see us.bringardner.io.filesource.IConnectionPropertiesEditor#getProperties()
	 */
	public Properties getProperties() {
		return prop;
	}

	/* (non-Javadoc)
	 * @see us.bringardner.io.filesource.IConnectionPropertiesEditor#setProperties(java.util.Properties)
	 */
	public void setProperties(Properties properties) {
		prop = new Properties(properties);
		hostNameFld.setText(prop.getProperty(FtpFileSourceFactory.PROP_HOST));
		portFld.setText(prop.getProperty(FtpFileSourceFactory.PROP_PORT));
		userIdFld.setText(prop.getProperty(FtpFileSourceFactory.PROP_USER));
		passwordFld.setText(prop.getProperty(FtpFileSourceFactory.PROP_PSWD));
		accountTextField.setText(prop.getProperty(FtpFileSourceFactory.PROP_ACCT));
	}
}