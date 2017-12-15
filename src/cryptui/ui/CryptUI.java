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
package cryptui.ui;

import cryptui.DataType;
import cryptui.crypto.KeyStore;
import cryptui.crypto.asymetric.IEncrypter;
import cryptui.crypto.asymetric.RSABase;
import cryptui.crypto.asymetric.RSAException;
import cryptui.crypto.asymetric.RSAKeyPair;
import cryptui.crypto.asymetric.RSAPublicKey;
import cryptui.crypto.container.AESEncryptedData;
import cryptui.crypto.container.Container;
import cryptui.crypto.container.RSAEncryptedData;
import cryptui.crypto.symetric.AES;
import cryptui.crypto.symetric.AESException;
import cryptui.ui.list.DirectoryListRenderer;
import cryptui.ui.list.FileListRenderer;
import cryptui.ui.list.KeyListRenderer;
import static cryptui.util.Assert.assertTrue;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

public class CryptUI extends javax.swing.JFrame {

    private static File HOME_DIRECTORY;
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

        privateKeyList.setSelectedIndex(0);
        publicKeyList.setSelectedIndex(0);
        File userHome = new File(System.getProperty("user.home"));
        setDirectoryForFileList(userHome);

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

    private File getKeysDirectory() {
        File home = getHomeDirectory();
        File[] keys = home.listFiles((d, f) -> "key".equals(f));
        File keyDir;
        if (keys.length == 0) {
            keyDir = new File(home.getAbsolutePath() + File.pathSeparator + "key");
            keyDir.mkdir();
        } else {
            keyDir = keys[0];
        }
        return keyDir;
    }

    private void setIconImage() {
        try (InputStream in = getClass().getResourceAsStream("/cryptui/ui/logo_ui.png")) {
            byte[] b = IOUtils.toByteArray(in);
            ImageIcon icon = new ImageIcon(b);
            super.setIconImage(icon.getImage());
        } catch (IOException e) {
            // Do nothing, its only a ui icon
        }
    }

    private File getHomeDirectory() {
        if (HOME_DIRECTORY != null) {
            return HOME_DIRECTORY;
        }

        String dir;
        dir = System.getenv("LOCALAPPDATA");
        if (dir != null) {
            File file = new File(dir + "/cryptui");
            file.mkdir();
            if (file.exists()) {
                HOME_DIRECTORY = file;
                return HOME_DIRECTORY;
            }
        }
        dir = System.getProperty("user.home");
        if (dir != null) {
            File file = new File(dir + "/cryptui");
            file.mkdir();
            if (file.exists()) {
                HOME_DIRECTORY = file;
                return HOME_DIRECTORY;
            }
        }
        return null;
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
        loadKeyButton = new javax.swing.JButton();
        importKeyButton = new javax.swing.JButton();
        exportPublicKeyButton = new javax.swing.JButton();
        encryptFor = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        privateKeyList = new javax.swing.JList<>();
        encryptFileButton = new javax.swing.JButton();
        decryptFileButton = new javax.swing.JButton();
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

        loadKeyButton.setText("Load Key");
        loadKeyButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                loadKeyButtonMouseClicked(evt);
            }
        });

        importKeyButton.setText("Import Public Key");
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
                    .addComponent(exportPublicKeyButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(loadKeyButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(importKeyButton, javax.swing.GroupLayout.DEFAULT_SIZE, 131, Short.MAX_VALUE)
                    .addComponent(encryptFileButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(decryptFileButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                        .addComponent(loadKeyButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(importKeyButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(exportPublicKeyButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(encryptFileButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(decryptFileButton)))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        encryptFor.getAccessibleContext().setAccessibleName("Public Keys");
        jLabel1.getAccessibleContext().setAccessibleName("Private Keys");

        tabbedPane.addTab("Key Management", keyManagementTab);

        fileManagementTab.setAutoscrolls(true);
        fileManagementTab.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        fileManagementTab.setFocusCycleRoot(true);

        usedKey.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cryptui/ui/list/key_pair.png"))); // NOI18N
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

        javax.swing.GroupLayout fileManagementTabLayout = new javax.swing.GroupLayout(fileManagementTab);
        fileManagementTab.setLayout(fileManagementTabLayout);
        fileManagementTabLayout.setHorizontalGroup(
            fileManagementTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fileManagementTabLayout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(fileManagementTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(encryptSelectedFile, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(usedKey, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(decryptSelectedFile, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(261, Short.MAX_VALUE))
            .addComponent(jScrollPane5)
        );
        fileManagementTabLayout.setVerticalGroup(
            fileManagementTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fileManagementTabLayout.createSequentialGroup()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(fileManagementTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(fileManagementTabLayout.createSequentialGroup()
                        .addComponent(usedKey)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(encryptSelectedFile)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(decryptSelectedFile)
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

    private void loadKeyButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_loadKeyButtonMouseClicked
        JFileChooser fc = new JFileChooser();
        int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            loadKey(file);
        }
    }//GEN-LAST:event_loadKeyButtonMouseClicked

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
                encryptFile(openFile, saveFile);
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

    private void importKeyButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_importKeyButtonMouseClicked
        JFileChooser fc = new JFileChooser();
        int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File openFile = fc.getSelectedFile();
            IEncrypter publicKey = RSABase.fromFile(openFile);
            KeyStore.addPublic(publicKey);
        }
    }//GEN-LAST:event_importKeyButtonMouseClicked

    private void decryptSelectedFileMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_decryptSelectedFileMouseClicked
        int selectedIndex = fileList.getSelectedIndex();
        if (selectedIndex < 0) {
            return;
        }
        File openFile = (File) fileListModel.get(selectedIndex);
        if (!openFile.isFile()) {
            return;
        }
        File saveFile = getDecryptionFileFor(openFile);
        decryptFile(openFile, saveFile);
    }//GEN-LAST:event_decryptSelectedFileMouseClicked

    private void encryptSelectedFileMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_encryptSelectedFileMouseClicked
        int selectedIndex = fileList.getSelectedIndex();
        if (selectedIndex < 0) {
            return;
        }
        File openFile = (File) fileListModel.get(selectedIndex);
        if (!openFile.isFile()) {
            return;
        }
        File saveFile = getEncryptionFileFor(openFile);
        encryptFile(openFile, saveFile);
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

    private void encryptFile(File openFile, File saveFile) {
        byte[] bytes;
        try (final FileInputStream fis = new FileInputStream(openFile)) {
            bytes = IOUtils.toByteArray(fis);
        } catch (IOException ex) {
            Logger.getLogger(CryptUI.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        AES aes = new AES();
        List<IEncrypter> selectedReceiver = publicKeyList.getSelectedValuesList();
        AESEncryptedData encryptedBytes;
        RSAEncryptedData rsaEncryptKey;
        try (FileOutputStream fos = new FileOutputStream(saveFile)) {
            fos.write(DataType.SENDER_HASH.getNumber());
            fos.write(signingKeyPair.getHash());
            ByteArrayOutputStream recipients = new ByteArrayOutputStream();

            for (IEncrypter rsa : selectedReceiver) {
                rsaEncryptKey = rsa.encrypt(aes.getKey());
                rsaEncryptKey.writeToOutputStream(fos);
                recipients.write(rsa.getHash());
            }

            encryptedBytes = aes.encrypt(ArrayUtils.addAll(signingKeyPair.createSignature(bytes, recipients.toByteArray()), bytes));
            encryptedBytes.writeToOutputStream(fos);
        } catch (RSAException | AESException | IOException ex) {
            Logger.getLogger(CryptUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void decryptFile(final File openFile, File saveFile) {
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
    private javax.swing.JButton encryptFileButton;
    private javax.swing.JLabel encryptFor;
    private javax.swing.JButton encryptSelectedFile;
    private javax.swing.JButton exportPublicKeyButton;
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
    private javax.swing.JPanel keyManagementTab;
    private javax.swing.JButton loadKeyButton;
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
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JLabel usedKey;
    // End of variables declaration//GEN-END:variables

    private boolean loadKey(File file) {
        IEncrypter rsa = RSABase.fromFile(file);
        if (rsa != null) {
            if (rsa instanceof RSAKeyPair) {
                KeyStore.addPrivate((RSAKeyPair) rsa);
            } else {
                KeyStore.addPublic(rsa);
            }
            return true;
        } else {
            return false;
        }
    }

    private File getDecryptionFileFor(File openFile) {
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
