/*
 * Copyright 2019 Thomas Hoermann
 * https://github.com/thomas-hoer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.cryptui.ui;

import static de.cryptui.util.Assert.assertTrue;
import static de.cryptui.util.UserConfiguration.getKeysDirectory;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

import de.cryptui.DataType;
import de.cryptui.crypto.KeyStore;
import de.cryptui.crypto.asymetric.IEncrypter;
import de.cryptui.crypto.asymetric.RSABase;
import de.cryptui.crypto.asymetric.RSAException;
import de.cryptui.crypto.asymetric.RSAKeyPair;
import de.cryptui.crypto.asymetric.RSAPublicKey;
import de.cryptui.crypto.container.AESEncryptedData;
import de.cryptui.crypto.container.Container;
import de.cryptui.crypto.container.RSAEncryptedData;
import de.cryptui.crypto.symetric.AES;
import de.cryptui.crypto.symetric.AESException;
import de.cryptui.ui.list.DirectoryListRenderer;
import de.cryptui.ui.list.FileListRenderer;
import de.cryptui.ui.list.KeyListRenderer;
import de.cryptui.util.Base64Util;
import de.cryptui.util.MultipartUtility;
import de.cryptui.util.UserConfiguration;

public class CryptUI extends JFrame {

	private static final long serialVersionUID = -4482400391222382424L;
	private static final Logger LOGGER = Logger.getLogger(CryptUI.class.getName());
	private final DefaultListModel<File> directoryListModel = new DefaultListModel<>();
	private final DefaultListModel<File> directoryDetailListModel = new DefaultListModel<>();
	private final DefaultListModel<File> fileListModel = new DefaultListModel<>();
	private RSAKeyPair signingKeyPair;

	private JList<File> directoryDetailList;
	private JList<File> directoryList;
	private JList<IEncrypter> encryptForList;
	private JList<File> fileList;
	private JTextArea infoBoxText;
	private JTextField newKeyComment;
	private JTextField newKeyName;
	private JList<RSAKeyPair> privateKeyList;
	private JList<IEncrypter> publicKeyList;
	private JTabbedPane tabbedPane;
	private JLabel usedKey;

	/**
	 * Creates new form CryptUI
	 */
	public CryptUI() {
		initComponents();
		setIconImage();

		privateKeyList.addListSelectionListener(e -> {
			final int[] selectedIndices = privateKeyList.getSelectedIndices();
			if (selectedIndices.length > 1) {
				privateKeyList.setSelectedIndex(selectedIndices[0]);
			}
			signingKeyPair = privateKeyList.getSelectedValue();
			UserConfiguration.setProperty(UserConfiguration.SELECTED_KEY,
					Base64Util.encodeToString(signingKeyPair.getHash()));
			usedKey.setText(signingKeyPair.toString());
		});

		final File keyDir = getKeysDirectory();
		for (final File file : keyDir.listFiles()) {
			if (file.isFile()) {
				loadKey(file);
			}
		}

		// Start with Encryption Page if at least 1 private Key exists
		if (!KeyStore.getPrivateKeyListModel().isEmpty()) {
			tabbedPane.setSelectedIndex(1);
		}

		publicKeyList.setSelectedIndex(0);
		final File userHome = new File(System.getProperty("user.home"));
		setDirectoryForFileList(userHome);
		privateKeyList.setSelectedValue(
				KeyStore.getPrivate(UserConfiguration.getProperty(UserConfiguration.SELECTED_KEY)), true);
	}

	private static File getCanonicalFile(final File file) {
		try {
			return file.getCanonicalFile();
		} catch (final IOException e) {
			LOGGER.log(Level.INFO, "Not able to get Canonical File from " + file.getName(), e);
			return file;
		}
	}

	private void setDirectoryForFileList(final File userHome) {
		directoryListModel.clear();
		fileListModel.clear();
		directoryDetailListModel.clear();
		final File canonicalFile = getCanonicalFile(userHome);
		final StringBuilder pathString = new StringBuilder();
		for (final String path : canonicalFile.getAbsolutePath().replace('\\', '/').split("/")) {
			pathString.append(path);
			pathString.append("/");
			directoryListModel.addElement(new File(pathString.toString()));
		}
		final File[] files = canonicalFile.listFiles();
		directoryDetailListModel.addElement(new File(canonicalFile + "/.."));
		if (files != null) {
			Arrays.sort(files, (a, b) -> a.isFile() == b.isFile() ? a.compareTo(b) : (a.isFile() ? 1 : -1));
			for (final File file : files) {
				if (file.isFile()) {
					fileListModel.addElement(file);
				} else {
					directoryDetailListModel.addElement(file);
				}
			}
		}
	}

	private void setIconImage() {
		try (InputStream in = getClass().getResourceAsStream("/de/cryptui/ui/logo_ui.png")) {
			final byte[] b = IOUtils.toByteArray(in);
			final ImageIcon icon = new ImageIcon(b);
			super.setIconImage(icon.getImage());
		} catch (final IOException e) {
			LOGGER.log(Level.WARNING, "Cannot load Resource /de/cryptui/ui/logo_ui.png", e);
		}
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() {

		tabbedPane = new JTabbedPane();
		final JPanel keyManagementTab = new JPanel();
		final JScrollPane jScrollPane1 = new JScrollPane();
		publicKeyList = new JList<>();
		final JPanel jPanel1 = new JPanel();
		final JLabel newKeyNameLabel = new JLabel();
		final JLabel newKeyCommentLabel = new JLabel();
		newKeyComment = new JTextField();
		newKeyName = new JTextField();
		final JLabel newKeyTypeLabel = new JLabel();
		final JRadioButton newKeyTypeRadioRSA = new JRadioButton();
		final JLabel newKeyStrenghtLabel = new JLabel();
		final JRadioButton newKeyStrengthRadio4096 = new JRadioButton();
		final JButton newKeyButton = new JButton();
		final JButton importKeyButton = new JButton();
		final JButton exportPublicKeyButton = new JButton();
		final JLabel encryptFor = new JLabel();
		final JLabel jLabel1 = new JLabel();
		final JScrollPane jScrollPane3 = new JScrollPane();
		privateKeyList = new JList<>();
		final JButton encryptFileButton = new JButton();
		final JButton decryptFileButton = new JButton();
		final JButton settingsButton = new JButton();
		final JButton exportToServerButton = new JButton();
		final JPanel fileManagementTab = new JPanel();
		usedKey = new JLabel();
		final JScrollPane jScrollPane2 = new JScrollPane();
		directoryDetailList = new JList<>();
		final JScrollPane jScrollPane4 = new JScrollPane();
		infoBoxText = new JTextArea();
		final JButton encryptSelectedFile = new JButton();
		final JButton decryptSelectedFile = new JButton();
		final JScrollPane jScrollPane5 = new JScrollPane();
		directoryList = new JList<>();
		final JScrollPane jScrollPane6 = new JScrollPane();
		fileList = new JList<>();
		final JScrollPane jScrollPane7 = new JScrollPane();
		encryptForList = new JList<>();
		final JLabel encryptForLabel = new JLabel();
		final JButton encryptAndUploadButton = new JButton();

		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setTitle("Crypt UI");

		publicKeyList.setModel(KeyStore.getPublicKeyListModel());
		publicKeyList.setCellRenderer(new KeyListRenderer());
		publicKeyList.setSelectedIndex(0);
		jScrollPane1.setViewportView(publicKeyList);

		newKeyNameLabel.setText("Name");

		newKeyCommentLabel.setText("Comment");

		newKeyTypeLabel.setText("Type");

		newKeyTypeRadioRSA.setSelected(true);
		newKeyTypeRadioRSA.setText("RSA");

		newKeyStrenghtLabel.setText("Strenght");

		newKeyStrengthRadio4096.setSelected(true);
		newKeyStrengthRadio4096.setText("4096 Bit");

		newKeyButton.setText("Create New Key");
		newKeyButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent evt) {
				newKeyMouseClicked();
			}
		});

		final GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(jPanel1Layout.createSequentialGroup().addContainerGap().addGroup(jPanel1Layout
						.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(newKeyStrengthRadio4096)
						.addComponent(newKeyTypeRadioRSA)
						.addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
								.addComponent(newKeyNameLabel).addComponent(newKeyCommentLabel)
								.addComponent(newKeyComment, GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
								.addComponent(newKeyName))
						.addComponent(newKeyTypeLabel).addComponent(newKeyStrenghtLabel))
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addGroup(jPanel1Layout.createSequentialGroup().addComponent(newKeyButton).addGap(0, 0,
						Short.MAX_VALUE)));
		jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(jPanel1Layout.createSequentialGroup().addContainerGap().addComponent(newKeyNameLabel)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(newKeyName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(13, 13, 13).addComponent(newKeyCommentLabel)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(newKeyComment, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addComponent(newKeyTypeLabel)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(newKeyTypeRadioRSA)
						.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addComponent(newKeyStrenghtLabel)
						.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addComponent(newKeyStrengthRadio4096)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(newKeyButton)
						.addContainerGap(22, Short.MAX_VALUE)));

		importKeyButton.setText("Import Key");
		importKeyButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent evt) {
				importKeyButtonMouseClicked();
			}
		});

		exportPublicKeyButton.setText("Export Public Key");
		exportPublicKeyButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent evt) {
				exportPublicKeyButtonMouseClicked();
			}
		});

		encryptFor.setText("Public Keys:");

		jLabel1.setText("Private Keys:");

		privateKeyList.setModel(KeyStore.getPrivateKeyListModel());
		privateKeyList.setCellRenderer(new KeyListRenderer());
		privateKeyList.setSelectedIndex(0);
		jScrollPane3.setViewportView(privateKeyList);

		encryptFileButton.setText("Encrypt File");
		encryptFileButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent evt) {
				encryptFileButtonMouseClicked();
			}
		});

		decryptFileButton.setText("Decrypt File");
		decryptFileButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent evt) {
				decryptFileButtonMouseClicked();
			}
		});

		settingsButton.setText("Settings");
		settingsButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent evt) {
				settingsButtonMouseClicked();
			}
		});

		exportToServerButton.setText("Export to Server");
		exportToServerButton.addActionListener(event -> exportToServerButtonActionPerformed());

		final GroupLayout keyManagementTabLayout = new GroupLayout(keyManagementTab);
		keyManagementTab.setLayout(keyManagementTabLayout);
		keyManagementTabLayout.setHorizontalGroup(keyManagementTabLayout
				.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(keyManagementTabLayout.createSequentialGroup().addGroup(keyManagementTabLayout
						.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(keyManagementTabLayout.createSequentialGroup()
								.addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 130, GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(jScrollPane3,
										GroupLayout.PREFERRED_SIZE, 130, GroupLayout.PREFERRED_SIZE))
						.addGroup(keyManagementTabLayout.createSequentialGroup().addComponent(encryptFor)
								.addGap(80, 80, 80).addComponent(jLabel1)))
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(keyManagementTabLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
								.addComponent(exportPublicKeyButton, GroupLayout.DEFAULT_SIZE, 131, Short.MAX_VALUE)
								.addComponent(importKeyButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)
								.addComponent(encryptFileButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)
								.addComponent(decryptFileButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)
								.addComponent(settingsButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)
								.addComponent(exportToServerButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE))
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, 171, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(187, Short.MAX_VALUE)));
		keyManagementTabLayout
				.setVerticalGroup(keyManagementTabLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(keyManagementTabLayout.createSequentialGroup().addGap(6, 6, 6)
								.addGroup(keyManagementTabLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
										.addComponent(encryptFor).addComponent(jLabel1))
								.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
								.addGroup(keyManagementTabLayout
										.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(jScrollPane1)
										.addComponent(jScrollPane3)))
						.addGroup(keyManagementTabLayout.createSequentialGroup().addContainerGap()
								.addGroup(keyManagementTabLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
										.addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addGroup(keyManagementTabLayout.createSequentialGroup()
												.addComponent(importKeyButton)
												.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(exportPublicKeyButton)
												.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(encryptFileButton)
												.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(decryptFileButton)
												.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(settingsButton)
												.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(exportToServerButton)))
								.addGap(0, 90, Short.MAX_VALUE)));

		encryptFor.getAccessibleContext().setAccessibleName("Public Keys");
		jLabel1.getAccessibleContext().setAccessibleName("Private Keys");

		tabbedPane.addTab("Key Management", keyManagementTab);

		fileManagementTab.setAutoscrolls(true);
		fileManagementTab.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
		fileManagementTab.setFocusCycleRoot(true);

		usedKey.setIcon(new ImageIcon(getClass().getResource("/de/cryptui/ui/list/key_pair.png"))); // NOI18N
		usedKey.setText("key");

		directoryDetailList.setModel(directoryDetailListModel);
		directoryDetailList.setCellRenderer(new FileListRenderer());
		directoryDetailList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent evt) {
				directoryDetailListMouseClicked(evt);
			}
		});
		jScrollPane2.setViewportView(directoryDetailList);

		infoBoxText.setEditable(false);
		infoBoxText.setBackground(new java.awt.Color(240, 240, 240));
		infoBoxText.setColumns(20);
		infoBoxText.setLineWrap(true);
		infoBoxText.setRows(5);
		jScrollPane4.setViewportView(infoBoxText);

		encryptSelectedFile.setText("Encrypt File");
		encryptSelectedFile.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent evt) {
				encryptSelectedFileMouseClicked();
			}
		});

		decryptSelectedFile.setText("Decrypt File");
		decryptSelectedFile.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent evt) {
				decryptSelectedFileMouseClicked();
			}
		});

		directoryList.setModel(directoryListModel);
		directoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		directoryList.setAutoscrolls(false);
		directoryList.setCellRenderer(new DirectoryListRenderer());
		directoryList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		directoryList.setVisibleRowCount(-1);
		directoryList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent evt) {
				directoryListMouseClicked();
			}
		});
		jScrollPane5.setViewportView(directoryList);

		fileList.setModel(fileListModel);
		fileList.setCellRenderer(new FileListRenderer());
		fileList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent evt) {
				fileListMouseClicked();
			}
		});
		jScrollPane6.setViewportView(fileList);

		encryptForList.setModel(KeyStore.getPublicKeyListModel());
		jScrollPane7.setViewportView(encryptForList);

		encryptForLabel.setText("Encrypt For:");

		encryptAndUploadButton.setText("Encrypt and Upload");
		encryptAndUploadButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent evt) {
				encryptAndUploadButtonMouseClicked();
			}
		});

		final GroupLayout fileManagementTabLayout = new GroupLayout(fileManagementTab);
		fileManagementTab.setLayout(fileManagementTabLayout);
		fileManagementTabLayout.setHorizontalGroup(fileManagementTabLayout
				.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(fileManagementTabLayout.createSequentialGroup()
						.addComponent(jScrollPane2, GroupLayout.PREFERRED_SIZE, 175, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jScrollPane6, GroupLayout.PREFERRED_SIZE, 175, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(fileManagementTabLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
								.addComponent(encryptSelectedFile, GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)
								.addComponent(usedKey, GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)
								.addComponent(decryptSelectedFile, GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)
								.addComponent(jScrollPane4, GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)
								.addComponent(encryptAndUploadButton, GroupLayout.DEFAULT_SIZE,
										GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						.addGroup(fileManagementTabLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addComponent(encryptForLabel).addComponent(jScrollPane7, GroupLayout.PREFERRED_SIZE,
										123, GroupLayout.PREFERRED_SIZE))
						.addContainerGap(128, Short.MAX_VALUE))
				.addComponent(jScrollPane5));
		fileManagementTabLayout.setVerticalGroup(fileManagementTabLayout
				.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(fileManagementTabLayout.createSequentialGroup()
						.addComponent(jScrollPane5, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(fileManagementTabLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addGroup(fileManagementTabLayout.createSequentialGroup().addComponent(encryptForLabel)
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(jScrollPane7))
								.addGroup(fileManagementTabLayout.createSequentialGroup().addComponent(usedKey)
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(encryptSelectedFile)
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(decryptSelectedFile)
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(encryptAndUploadButton)
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(jScrollPane4).addContainerGap())
								.addComponent(jScrollPane2, GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)
								.addComponent(jScrollPane6, GroupLayout.Alignment.TRAILING))));

		tabbedPane.addTab("File Management", fileManagementTab);

		final GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(tabbedPane));
		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(tabbedPane));

		tabbedPane.getAccessibleContext().setAccessibleName("Key Management");

		pack();
	}

	private void importKeyButtonMouseClicked() {
		final JFileChooser fc = new JFileChooser();
		final int returnVal = fc.showOpenDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			final File file = fc.getSelectedFile();
			final RSABase rsa = loadKey(file);
			if (rsa != null) {

				final File keysDir = getKeysDirectory();
				final File newKey = new File(keysDir.getAbsolutePath() + "/" + rsa.hashCode() + ".key");
				try {
					rsa.saveKeyInFile(newKey);
				} catch (final IOException ex) {
					LOGGER.log(Level.SEVERE, null, ex);
				}
			}
		}
	}

	private void newKeyMouseClicked() {
		try {
			final RSAKeyPair rsa = new RSAKeyPair(newKeyName.getText(), newKeyComment.getText());
			newKeyName.setText("");
			newKeyComment.setText("");
			final File keysDir = getKeysDirectory();
			final File newKey = new File(keysDir.getAbsolutePath() + "/" + rsa.hashCode() + ".key");
			rsa.saveKeyInFile(newKey);
			KeyStore.addPrivate(rsa);
		} catch (RSAException | IOException ex) {
			LOGGER.log(Level.SEVERE, null, ex);
		}
	}

	private void encryptFileButtonMouseClicked() {
		final JFileChooser fc = new JFileChooser();
		final int returnVal = fc.showOpenDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			final File openFile = fc.getSelectedFile();
			final JFileChooser fc2 = new JFileChooser();
			final int returnVal2 = fc2.showSaveDialog(this);
			if (returnVal2 == JFileChooser.APPROVE_OPTION) {
				final File saveFile = fc2.getSelectedFile();
				try {
					encryptFile(openFile, saveFile);
				} catch (IOException | RSAException | AESException ex) {
					LOGGER.log(Level.SEVERE, null, ex);
				}
			}
		}
	}

	private void decryptFileButtonMouseClicked() {
		final JFileChooser fc = new JFileChooser();
		final int returnVal = fc.showOpenDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			final File openFile = fc.getSelectedFile();

			final JFileChooser fc2 = new JFileChooser();
			final int returnVal2 = fc2.showSaveDialog(this);
			if (returnVal2 == JFileChooser.APPROVE_OPTION) {
				final File saveFile = fc2.getSelectedFile();
				decryptFile(openFile, saveFile);
			}

		}
	}

	private void exportPublicKeyButtonMouseClicked() {
		final RSAKeyPair rsa = privateKeyList.getSelectedValue();
		final RSAPublicKey publicKey = rsa.getPublicKey();
		final JFileChooser fileChooser = new JFileChooser();
		final int returnVal2 = fileChooser.showSaveDialog(this);
		if (returnVal2 == JFileChooser.APPROVE_OPTION) {
			final File saveFile = fileChooser.getSelectedFile();
			try {
				publicKey.saveKeyInFile(saveFile);
			} catch (final IOException ex) {
				LOGGER.log(Level.SEVERE, null, ex);
			}
		}
	}

	private void decryptSelectedFileMouseClicked() {
		final File openFile = getSelectedFile();
		final File saveFile = getDecryptionFileFor(openFile);
		decryptFile(openFile, saveFile);
	}

	private void encryptSelectedFileMouseClicked() {
		final File openFile = getSelectedFile();
		if (openFile == null) {
			return;
		}
		final File saveFile = getEncryptionFileFor(openFile);
		try {
			final boolean success = encryptFile(openFile, saveFile);
			if (success) {
				final int selectedIndex = fileList.getSelectedIndex();
				fileListModel.add(selectedIndex + 1, saveFile);
			}
		} catch (IOException | RSAException | AESException ex) {
			LOGGER.log(Level.SEVERE, null, ex);
		}
	}

	private void directoryDetailListMouseClicked(final MouseEvent evt) {
		final File file = directoryDetailListModel.get(directoryDetailList.getSelectedIndex());
		if (evt.getClickCount() == 2) {
			setDirectoryForFileList(file);
		}
	}

	private void directoryListMouseClicked() {
		final File newDirectory = directoryListModel.get(directoryList.getSelectedIndex());
		setDirectoryForFileList(newDirectory);
	}

	private void fileListMouseClicked() {
		final File file = fileListModel.get(fileList.getSelectedIndex());
		showInfo(file);
	}

	private void settingsButtonMouseClicked() {
		final Settings settings = new Settings();
		settings.setVisible(true);
	}

	private void exportToServerButtonActionPerformed() {
		try {
			final RSAKeyPair rsa = privateKeyList.getSelectedValue();
			final RSAPublicKey publicKey = rsa.getPublicKey();
			final File file = File.createTempFile("temp_", ".pubkey");
			publicKey.saveKeyInFile(file);
			final String httpsURL = UserConfiguration.getServer() + "/upload.php";
			final MultipartUtility multipart = new MultipartUtility(httpsURL);
			multipart.addFormField("submit", "true");
			multipart.addFilePart("fileToUpload", file);
			multipart.finish();
			file.delete();
		} catch (final IOException ex) {
			LOGGER.log(Level.SEVERE, null, ex);
		}
	}

	private void encryptAndUploadButtonMouseClicked() {
		final File openFile = getSelectedFile();
		if (openFile == null) {
			return;
		}
		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
			encryptFile(openFile, byteArrayOutputStream);
			if (byteArrayOutputStream.size() > 0) {
				final String httpsURL = UserConfiguration.getServer() + "/upload.php";
				final MultipartUtility multipart = new MultipartUtility(httpsURL);
				multipart.addFormField("submit", "true");
				multipart.addFilePart("fileToUpload", "file", byteArrayOutputStream.toByteArray());
				multipart.finish();
			}

		} catch (IOException | RSAException | AESException ex) {
			LOGGER.log(Level.SEVERE, null, ex);
		}
	}

	private File getSelectedFile() {
		final int selectedIndex = fileList.getSelectedIndex();
		if (selectedIndex < 0) {
			return null;
		}
		final File openFile = fileListModel.get(selectedIndex);
		if (openFile.isFile()) {
			return openFile;
		}
		return null;
	}

	private boolean encryptFile(final File openFile, final File saveFile)
			throws IOException, RSAException, AESException {
		if (!checkRecipients()) {
			return false;
		}
		try (FileOutputStream fileOutputStream = new FileOutputStream(saveFile)) {
			return encryptFile(openFile, fileOutputStream);
		} catch (IOException | RSAException | AESException ex) {
			LOGGER.log(Level.SEVERE, null, ex);
		}
		return false;
	}

	private boolean encryptFile(final File openFile, final OutputStream outputStream)
			throws IOException, RSAException, AESException {
		if (!checkRecipients()) {
			return false;
		}
		final byte[] bytes = FileUtils.readFileToByteArray(openFile);
		final AES aes = new AES();
		AESEncryptedData encryptedBytes;
		RSAEncryptedData rsaEncryptKey;
		outputStream.write(DataType.SENDER_HASH.getNumber());
		outputStream.write(signingKeyPair.getHash());
		final ByteArrayOutputStream recipients = new ByteArrayOutputStream();

		final List<IEncrypter> selectedReceiver = encryptForList.getSelectedValuesList();
		for (final IEncrypter rsa : selectedReceiver) {
			rsaEncryptKey = rsa.encrypt(aes.getKey());
			rsaEncryptKey.writeToOutputStream(outputStream);
			recipients.write(rsa.getHash());
		}

		encryptedBytes = aes
				.encrypt(ArrayUtils.addAll(signingKeyPair.createSignature(bytes, recipients.toByteArray()), bytes));
		encryptedBytes.writeToOutputStream(outputStream);
		return true;
	}

	/**
	 * Checks the number of recipients and shows a waring dialog if none are
	 * selected.
	 *
	 * @return true if at least one recipient is selected
	 */
	private boolean checkRecipients() {
		final List<IEncrypter> selectedReceiver = encryptForList.getSelectedValuesList();
		if (selectedReceiver.isEmpty()) {
			JOptionPane.showMessageDialog(null, "You have to select at least recipient.");
			return false;
		}
		return true;
	}

	private void decryptFile(final File openFile, final File saveFile) {
		if (openFile == null || saveFile == null) {
			return;
		}
		try {
			final Container container = new Container(openFile);
			assertTrue(container.decrypt());
			assertTrue(container.verify());
			try (FileOutputStream fos = new FileOutputStream(saveFile)) {
				fos.write(container.getDecryptedData());
			}
		} catch (final IOException ex) {
			LOGGER.log(Level.SEVERE, null, ex);
		}
	}

	public void showInfo(final File file) {
		final StringBuilder text = new StringBuilder();
		if (file.isDirectory()) {
			try {
				text.append(file.getCanonicalFile().getName());
			} catch (final IOException e) {
				text.append(file.getName());
			}
		} else if (file.isFile()) {
			try {
				final Container container = new Container(file);
				text.append(container.toString());
			} catch (final IOException ex) {
				LOGGER.log(Level.WARNING, "Can not open File " + file.getName(), ex);
				text.append("Can not open File");
			} catch (final RuntimeException e) {
				text.append(file.getName());
				text.append(" is not encrypted.");
			}
		}
		text.append("\n");
		text.append(file.length());
		text.append(" Bytes");
		infoBoxText.setText(text.toString());
	}

	private static RSABase loadKey(final File file) {
		final RSABase rsa = RSABase.fromFile(file);
		if (rsa != null) {
			if (rsa instanceof RSAKeyPair) {
				KeyStore.addPrivate((RSAKeyPair) rsa);
			} else if (rsa instanceof IEncrypter) {
				KeyStore.addPublic((IEncrypter) rsa);
			}
			return rsa;
		}
		return null;
	}

	private static File getDecryptionFileFor(final File openFile) {
		if (openFile == null) {
			return null;
		}
		String fileName = openFile.getPath();
		if (fileName.endsWith(".asc")) {
			fileName = fileName.substring(0, fileName.length() - 4);
		}
		File newFile = new File(fileName);
		if (!newFile.exists()) {
			return newFile;
		}
		final int lastIndex = fileName.lastIndexOf('.');
		String extension;
		if (lastIndex == -1) {
			extension = "";
		} else {
			extension = fileName.substring(lastIndex);
			fileName = fileName.substring(0, lastIndex);
		}
		int index = 1;
		do {
			newFile = new File(fileName + "(" + index + ")" + extension);
			index++;
		} while (newFile.exists());
		return newFile;
	}

	private static File getEncryptionFileFor(final File openFile) {
		File newFile = new File(openFile.getPath() + ".asc");
		if (!newFile.exists()) {
			return newFile;
		}
		String fileName = openFile.getPath();
		final int lastIndex = fileName.lastIndexOf('.');
		String extension;
		if (lastIndex == -1) {
			extension = "";
		} else {
			extension = fileName.substring(lastIndex);
			fileName = fileName.substring(0, lastIndex);
		}
		int index = 1;
		do {
			newFile = new File(fileName + "(" + index + ")" + extension + ".asc");
			index++;
		} while (newFile.exists());
		return newFile;
	}

}
