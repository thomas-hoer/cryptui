/*
 * Copyright 2017 thomas-hoer.
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
import static de.cryptui.util.Assert.assertTrue;
import de.cryptui.util.Base64Util;
import de.cryptui.util.MultipartUtility;
import de.cryptui.util.UserConfiguration;
import static de.cryptui.util.UserConfiguration.getKeysDirectory;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

public class CryptUI extends javax.swing.JFrame {

    private final DefaultListModel directoryListModel = new DefaultListModel();
    private final DefaultListModel directoryDetailListModel = new DefaultListModel();
    private final DefaultListModel fileListModel = new DefaultListModel();
    private RSAKeyPair signingKeyPair;

    /**
     * Creates new form CryptUI
     */
    public CryptUI() {
        initComponents();
        setIconImage();

        privateKeyList.addListSelectionListener((e) -> {
            int[] selectedIndices = privateKeyList.getSelectedIndices();
            if (selectedIndices.length > 1) {
                privateKeyList.setSelectedIndex(selectedIndices[0]);
            }
            signingKeyPair = privateKeyList.getSelectedValue();
            UserConfiguration.setProperty(UserConfiguration.SELECTED_KEY, Base64Util.encodeToString(signingKeyPair.getHash()));
            usedKey.setText(signingKeyPair.toString());
        });

        File keyDir = getKeysDirectory();
        for (File file : keyDir.listFiles()) {
            if (file.isFile()) {
                loadKey(file);
            }
        }

        // Start with Encryption Page if at least 1 private Key exists
        if (!KeyStore.getPrivateKeyListModel().isEmpty()) {
            tabbedPane.setSelectedIndex(1);
        }

        //privateKeyList.setSelectedIndex(0);
        publicKeyList.setSelectedIndex(0);
        File userHome = new File(System.getProperty("user.home"));
        setDirectoryForFileList(userHome);
        privateKeyList.setSelectedValue(KeyStore.getPrivate(UserConfiguration.getProperty(UserConfiguration.SELECTED_KEY)), true);
    }

    private void setDirectoryForFileList(File userHome) {
        directoryListModel.clear();
        fileListModel.clear();
        directoryDetailListModel.clear();
        File canonicalFile;
        try {
            canonicalFile = userHome.getCanonicalFile();
        } catch (IOException e) {
            canonicalFile = userHome;
        }
        StringBuilder pathString = new StringBuilder();
        for (String path : canonicalFile.getAbsolutePath().replace('\\', '/').split("/")) {
            pathString.append(path);
            pathString.append("/");
            directoryListModel.addElement(new File(pathString.toString()));
        }
        File[] files = canonicalFile.listFiles();
        directoryDetailListModel.addElement(new File(canonicalFile + "/.."));
        if (files != null) {
            Arrays.sort(files, (a, b) -> a.isFile() == b.isFile() ? a.compareTo(b) : a.isFile() ? 1 : -1);
            for (File file : files) {
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
            byte[] b = IOUtils.toByteArray(in);
            ImageIcon icon = new ImageIcon(b);
            super.setIconImage(icon.getImage());
        } catch (IOException e) {
            // Do nothing, its only a ui icon
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabbedPane = new javax.swing.JTabbedPane();
        keyManagementTab = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        publicKeyList = new javax.swing.JList<>();
        jPanel1 = new javax.swing.JPanel();
        newKeyNameLabel = new javax.swing.JLabel();
        newKeyCommentLabel = new javax.swing.JLabel();
        newKeyComment = new javax.swing.JTextField();
        newKeyName = new javax.swing.JTextField();
        newKeyTypeLabel = new javax.swing.JLabel();
        newKeyTypeRadioRSA = new javax.swing.JRadioButton();
        newKeyStrenghtLabel = new javax.swing.JLabel();
        newKeyStrengthRadio4096 = new javax.swing.JRadioButton();
        newKeyButton = new javax.swing.JButton();
        importKeyButton = new javax.swing.JButton();
        exportPublicKeyButton = new javax.swing.JButton();
        encryptFor = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        privateKeyList = new javax.swing.JList<>();
        encryptFileButton = new javax.swing.JButton();
        decryptFileButton = new javax.swing.JButton();
        settingsButton = new javax.swing.JButton();
        exportToServerButton = new javax.swing.JButton();
        fileManagementTab = new javax.swing.JPanel();
        usedKey = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        directoryDetailList = new javax.swing.JList<>();
        jScrollPane4 = new javax.swing.JScrollPane();
        infoBoxText = new javax.swing.JTextArea();
        encryptSelectedFile = new javax.swing.JButton();
        decryptSelectedFile = new javax.swing.JButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        directoryList = new javax.swing.JList<>();
        jScrollPane6 = new javax.swing.JScrollPane();
        fileList = new javax.swing.JList<>();
        jScrollPane7 = new javax.swing.JScrollPane();
        encryptForList = new javax.swing.JList<>();
        encryptForLabel = new javax.swing.JLabel();
        encryptAndUploadButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
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
        newKeyButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                newKeyMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(newKeyStrengthRadio4096)
                    .addComponent(newKeyTypeRadioRSA)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(newKeyNameLabel)
                        .addComponent(newKeyCommentLabel)
                        .addComponent(newKeyComment, javax.swing.GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
                        .addComponent(newKeyName))
                    .addComponent(newKeyTypeLabel)
                    .addComponent(newKeyStrenghtLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(newKeyButton)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(newKeyNameLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(newKeyName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(13, 13, 13)
                .addComponent(newKeyCommentLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(newKeyComment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(newKeyTypeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(newKeyTypeRadioRSA)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(newKeyStrenghtLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(newKeyStrengthRadio4096)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(newKeyButton)
                .addContainerGap(22, Short.MAX_VALUE))
        );

        importKeyButton.setText("Import Key");
        importKeyButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                importKeyButtonMouseClicked(evt);
            }
        });

        exportPublicKeyButton.setText("Export Public Key");
        exportPublicKeyButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                exportPublicKeyButtonMouseClicked(evt);
            }
        });

        encryptFor.setText("Public Keys:");

        jLabel1.setText("Private Keys:");

        privateKeyList.setModel(KeyStore.getPrivateKeyListModel());
        privateKeyList.setCellRenderer(new KeyListRenderer());
        privateKeyList.setSelectedIndex(0);
        jScrollPane3.setViewportView(privateKeyList);

        encryptFileButton.setText("Encrypt File");
        encryptFileButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                encryptFileButtonMouseClicked(evt);
            }
        });

        decryptFileButton.setText("Decrypt File");
        decryptFileButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                decryptFileButtonMouseClicked(evt);
            }
        });

        settingsButton.setText("Settings");
        settingsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                settingsButtonMouseClicked(evt);
            }
        });

        exportToServerButton.setText("Export to Server");
        exportToServerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportToServerButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout keyManagementTabLayout = new javax.swing.GroupLayout(keyManagementTab);
        keyManagementTab.setLayout(keyManagementTabLayout);
        keyManagementTabLayout.setHorizontalGroup(
            keyManagementTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(keyManagementTabLayout.createSequentialGroup()
                .addGroup(keyManagementTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(keyManagementTabLayout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(keyManagementTabLayout.createSequentialGroup()
                        .addComponent(encryptFor)
                        .addGap(80, 80, 80)
                        .addComponent(jLabel1)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(keyManagementTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(exportPublicKeyButton, javax.swing.GroupLayout.DEFAULT_SIZE, 131, Short.MAX_VALUE)
                    .addComponent(importKeyButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(encryptFileButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(decryptFileButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(settingsButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(exportToServerButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(187, Short.MAX_VALUE))
        );
        keyManagementTabLayout.setVerticalGroup(
            keyManagementTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(keyManagementTabLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(keyManagementTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(encryptFor)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(keyManagementTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addComponent(jScrollPane3)))
            .addGroup(keyManagementTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(keyManagementTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(keyManagementTabLayout.createSequentialGroup()
                        .addComponent(importKeyButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(exportPublicKeyButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(encryptFileButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(decryptFileButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(settingsButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(exportToServerButton)))
                .addGap(0, 90, Short.MAX_VALUE))
        );

        encryptFor.getAccessibleContext().setAccessibleName("Public Keys");
        jLabel1.getAccessibleContext().setAccessibleName("Private Keys");

        tabbedPane.addTab("Key Management", keyManagementTab);

        fileManagementTab.setAutoscrolls(true);
        fileManagementTab.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        fileManagementTab.setFocusCycleRoot(true);

        usedKey.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cryptui/ui/list/key_pair.png"))); // NOI18N
        usedKey.setText("key");

        directoryDetailList.setModel(directoryDetailListModel);
        directoryDetailList.setCellRenderer(new FileListRenderer());
        directoryDetailList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
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
        encryptSelectedFile.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                encryptSelectedFileMouseClicked(evt);
            }
        });

        decryptSelectedFile.setText("Decrypt File");
        decryptSelectedFile.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                decryptSelectedFileMouseClicked(evt);
            }
        });

        directoryList.setModel(directoryListModel);
        directoryList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        directoryList.setAutoscrolls(false);
        directoryList.setCellRenderer(new DirectoryListRenderer());
        directoryList.setLayoutOrientation(javax.swing.JList.HORIZONTAL_WRAP);
        directoryList.setVisibleRowCount(-1);
        directoryList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                directoryListMouseClicked(evt);
            }
        });
        jScrollPane5.setViewportView(directoryList);

        fileList.setModel(fileListModel);
        fileList.setCellRenderer(new FileListRenderer());
        fileList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fileListMouseClicked(evt);
            }
        });
        jScrollPane6.setViewportView(fileList);

        encryptForList.setModel(KeyStore.getPublicKeyListModel());
        jScrollPane7.setViewportView(encryptForList);

        encryptForLabel.setText("Encrypt For:");

        encryptAndUploadButton.setText("Encrypt and Upload");
        encryptAndUploadButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                encryptAndUploadButtonMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout fileManagementTabLayout = new javax.swing.GroupLayout(fileManagementTab);
        fileManagementTab.setLayout(fileManagementTabLayout);
        fileManagementTabLayout.setHorizontalGroup(
            fileManagementTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fileManagementTabLayout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(fileManagementTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(encryptSelectedFile, javax.swing.GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)
                    .addComponent(usedKey, javax.swing.GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)
                    .addComponent(decryptSelectedFile, javax.swing.GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)
                    .addComponent(encryptAndUploadButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(fileManagementTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(encryptForLabel)
                    .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(128, Short.MAX_VALUE))
            .addComponent(jScrollPane5)
        );
        fileManagementTabLayout.setVerticalGroup(
            fileManagementTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fileManagementTabLayout.createSequentialGroup()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(fileManagementTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(fileManagementTabLayout.createSequentialGroup()
                        .addComponent(encryptForLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane7))
                    .addGroup(fileManagementTabLayout.createSequentialGroup()
                        .addComponent(usedKey)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(encryptSelectedFile)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(decryptSelectedFile)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(encryptAndUploadButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane4)
                        .addContainerGap())
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.Alignment.TRAILING)))
        );

        tabbedPane.addTab("File Management", fileManagementTab);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPane)
        );

        tabbedPane.getAccessibleContext().setAccessibleName("Key Management");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void importKeyButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_importKeyButtonMouseClicked
        JFileChooser fc = new JFileChooser();
        int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            RSABase rsa = loadKey(file);
            if (rsa != null) {

                File keysDir = getKeysDirectory();
                File newKey = new File(keysDir.getAbsolutePath() + "/" + rsa.hashCode() + ".key");
                try {
                    rsa.saveKeyInFile(newKey);
                } catch (IOException ex) {
                    Logger.getLogger(CryptUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }//GEN-LAST:event_importKeyButtonMouseClicked

    private void newKeyMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_newKeyMouseClicked
        try {
            RSAKeyPair rsa = new RSAKeyPair(newKeyName.getText(), newKeyComment.getText());
            newKeyName.setText("");
            newKeyComment.setText("");
            File keysDir = getKeysDirectory();
            File newKey = new File(keysDir.getAbsolutePath() + "/" + rsa.hashCode() + ".key");
            rsa.saveKeyInFile(newKey);
            KeyStore.addPrivate(rsa);
        } catch (RSAException | IOException ex) {
            Logger.getLogger(CryptUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_newKeyMouseClicked

    private void encryptFileButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_encryptFileButtonMouseClicked
        JFileChooser fc = new JFileChooser();
        int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File openFile = fc.getSelectedFile();
            JFileChooser fc2 = new JFileChooser();
            int returnVal2 = fc2.showSaveDialog(this);
            if (returnVal2 == JFileChooser.APPROVE_OPTION) {
                File saveFile = fc2.getSelectedFile();
                try {
                    encryptFile(openFile, saveFile);
                } catch (IOException | RSAException | AESException ex) {
                    Logger.getLogger(CryptUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }//GEN-LAST:event_encryptFileButtonMouseClicked

    private void decryptFileButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_decryptFileButtonMouseClicked
        JFileChooser fc = new JFileChooser();
        int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File openFile = fc.getSelectedFile();

            JFileChooser fc2 = new JFileChooser();
            int returnVal2 = fc2.showSaveDialog(this);
            if (returnVal2 == JFileChooser.APPROVE_OPTION) {
                File saveFile = fc2.getSelectedFile();
                decryptFile(openFile, saveFile);
            }

        }
    }//GEN-LAST:event_decryptFileButtonMouseClicked

    private void exportPublicKeyButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_exportPublicKeyButtonMouseClicked
        RSAKeyPair rsa = privateKeyList.getSelectedValue();
        RSAPublicKey publicKey = rsa.getPublicKey();
        JFileChooser fileChooser = new JFileChooser();
        int returnVal2 = fileChooser.showSaveDialog(this);
        if (returnVal2 == JFileChooser.APPROVE_OPTION) {
            File saveFile = fileChooser.getSelectedFile();
            try {
                publicKey.saveKeyInFile(saveFile);
            } catch (IOException ex) {
                Logger.getLogger(CryptUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_exportPublicKeyButtonMouseClicked

    private void decryptSelectedFileMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_decryptSelectedFileMouseClicked
        File openFile = getSelectedFile();
        File saveFile = getDecryptionFileFor(openFile);
        decryptFile(openFile, saveFile);
    }//GEN-LAST:event_decryptSelectedFileMouseClicked

    private void encryptSelectedFileMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_encryptSelectedFileMouseClicked
        File openFile = getSelectedFile();
        File saveFile = getEncryptionFileFor(openFile);
        if (openFile == null || saveFile == null) {
            return;
        }
        try {
            encryptFile(openFile, saveFile);
        } catch (IOException | RSAException | AESException ex) {
            Logger.getLogger(CryptUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_encryptSelectedFileMouseClicked

    private void directoryDetailListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_directoryDetailListMouseClicked
        File file = (File) directoryDetailListModel.get(directoryDetailList.getSelectedIndex());
        if (evt.getClickCount() == 2) {
            setDirectoryForFileList(file);
        }
    }//GEN-LAST:event_directoryDetailListMouseClicked

    private void directoryListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_directoryListMouseClicked
        File newDirectory = (File) directoryListModel.get(directoryList.getSelectedIndex());
        setDirectoryForFileList(newDirectory);
    }//GEN-LAST:event_directoryListMouseClicked

    private void fileListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fileListMouseClicked
        File file = (File) fileListModel.get(fileList.getSelectedIndex());
        showInfo(file);
    }//GEN-LAST:event_fileListMouseClicked

    private Settings settings;
    private void settingsButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_settingsButtonMouseClicked
        settings = new Settings();
        settings.setVisible(true);
    }//GEN-LAST:event_settingsButtonMouseClicked

    private void exportToServerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportToServerButtonActionPerformed
        try {
            RSAKeyPair rsa = privateKeyList.getSelectedValue();
            RSAPublicKey publicKey = rsa.getPublicKey();
            File file = File.createTempFile("temp_", ".pubkey");
            publicKey.saveKeyInFile(file);
            String httpsURL = UserConfiguration.getServer() + "/upload.php";
            MultipartUtility multipart = new MultipartUtility(httpsURL);
            multipart.addFormField("submit", "true");
            multipart.addFilePart("fileToUpload", file);
            multipart.finish();
            file.delete();
        } catch (MalformedURLException ex) {
            Logger.getLogger(CryptUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CryptUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_exportToServerButtonActionPerformed

    private void encryptAndUploadButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_encryptAndUploadButtonMouseClicked
        File openFile = getSelectedFile();
        if (openFile == null) {
            return;
        }
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            encryptFile(openFile, byteArrayOutputStream);
            if (byteArrayOutputStream.size() > 0) {
                String httpsURL = UserConfiguration.getServer() + "/upload.php";
                MultipartUtility multipart = new MultipartUtility(httpsURL);
                multipart.addFormField("submit", "true");
                multipart.addFilePart("fileToUpload", "file", byteArrayOutputStream.toByteArray());
                multipart.finish();
            }

        } catch (IOException | RSAException | AESException ex) {
            Logger.getLogger(CryptUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_encryptAndUploadButtonMouseClicked

    private File getSelectedFile() {
        int selectedIndex = fileList.getSelectedIndex();
        if (selectedIndex < 0) {
            return null;
        }
        File openFile = (File) fileListModel.get(selectedIndex);
        if (openFile.isFile()) {
            return openFile;
        }
        return null;
    }

    private void encryptFile(File openFile, File saveFile) throws IOException, RSAException, AESException {
        if (!checkRecipients()) {
            return;
        }
        try (FileOutputStream fileOutputStream = new FileOutputStream(saveFile)) {
            encryptFile(openFile, fileOutputStream);
        } catch (IOException | RSAException | AESException ex) {
            Logger.getLogger(CryptUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void encryptFile(File openFile, OutputStream outputStream) throws IOException, RSAException, AESException {
        if (!checkRecipients()) {
            return;
        }
        byte[] bytes = FileUtils.readFileToByteArray(openFile);
        AES aes = new AES();
        AESEncryptedData encryptedBytes;
        RSAEncryptedData rsaEncryptKey;
        outputStream.write(DataType.SENDER_HASH.getNumber());
        outputStream.write(signingKeyPair.getHash());
        ByteArrayOutputStream recipients = new ByteArrayOutputStream();

        List<IEncrypter> selectedReceiver = encryptForList.getSelectedValuesList();
        for (IEncrypter rsa : selectedReceiver) {
            rsaEncryptKey = rsa.encrypt(aes.getKey());
            rsaEncryptKey.writeToOutputStream(outputStream);
            recipients.write(rsa.getHash());
        }

        encryptedBytes = aes.encrypt(ArrayUtils.addAll(signingKeyPair.createSignature(bytes, recipients.toByteArray()), bytes));
        encryptedBytes.writeToOutputStream(outputStream);
    }

    /**
     * Checks the number of recipients and shows a waring dialog if none are
     * selected.
     *
     * @return true if at least one recipient is selected
     */
    private boolean checkRecipients() {
        List<IEncrypter> selectedReceiver = encryptForList.getSelectedValuesList();
        if (selectedReceiver.isEmpty()) {
            JOptionPane.showMessageDialog(null, "You have to select at least recipient.");
            return false;
        }
        return true;
    }

    private void decryptFile(final File openFile, File saveFile) {
        if (openFile == null || saveFile == null) {
            return;
        }
        try {
            Container container = new Container(openFile);
            assertTrue(container.decrypt());
            assertTrue(container.verify());
            try (FileOutputStream fos = new FileOutputStream(saveFile)) {
                fos.write(container.getDecryptedData());
            }
        } catch (IOException ex) {
            Logger.getLogger(CryptUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void showInfo(File file) {
        StringBuilder text = new StringBuilder();
        if (file.isDirectory()) {
            try {
                text.append(file.getCanonicalFile().getName());
            } catch (IOException e) {
                text.append(file.getName());
            }
        } else if (file.isFile()) {
            try {
                Container container = new Container(file);
                text.append(container.toString());
            } catch (IOException ex) {
                text.append("Can not open File");
            } catch (Exception e) {
                text.append(file.getName());
                text.append(" is not encrypted.");
            }
        }
        infoBoxText.setText(text.toString());
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton decryptFileButton;
    private javax.swing.JButton decryptSelectedFile;
    private javax.swing.JList<String> directoryDetailList;
    private javax.swing.JList<String> directoryList;
    private javax.swing.JButton encryptAndUploadButton;
    private javax.swing.JButton encryptFileButton;
    private javax.swing.JLabel encryptFor;
    private javax.swing.JLabel encryptForLabel;
    private javax.swing.JList<IEncrypter> encryptForList;
    private javax.swing.JButton encryptSelectedFile;
    private javax.swing.JButton exportPublicKeyButton;
    private javax.swing.JButton exportToServerButton;
    private javax.swing.JList<String> fileList;
    private javax.swing.JPanel fileManagementTab;
    private javax.swing.JButton importKeyButton;
    private javax.swing.JTextArea infoBoxText;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JPanel keyManagementTab;
    private javax.swing.JButton newKeyButton;
    private javax.swing.JTextField newKeyComment;
    private javax.swing.JLabel newKeyCommentLabel;
    private javax.swing.JTextField newKeyName;
    private javax.swing.JLabel newKeyNameLabel;
    private javax.swing.JLabel newKeyStrenghtLabel;
    private javax.swing.JRadioButton newKeyStrengthRadio4096;
    private javax.swing.JLabel newKeyTypeLabel;
    private javax.swing.JRadioButton newKeyTypeRadioRSA;
    private javax.swing.JList<RSAKeyPair> privateKeyList;
    private javax.swing.JList<IEncrypter> publicKeyList;
    private javax.swing.JButton settingsButton;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JLabel usedKey;
    // End of variables declaration//GEN-END:variables

    private RSABase loadKey(File file) {
        RSABase rsa = RSABase.fromFile(file);
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

    private File getDecryptionFileFor(File openFile) {
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
        int lastIndex = fileName.lastIndexOf(".");
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

    private File getEncryptionFileFor(File openFile) {
        if (openFile == null) {
            return null;
        }
        File newFile = new File(openFile.getPath() + ".asc");
        if (!newFile.exists()) {
            return newFile;
        }
        String fileName = openFile.getPath();
        int lastIndex = fileName.lastIndexOf(".");
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