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
import cryptui.crypto.asymetric.IEncrypter;
import cryptui.crypto.asymetric.RSABase;
import cryptui.crypto.asymetric.RSAEncryptedData;
import cryptui.crypto.asymetric.RSAException;
import cryptui.crypto.asymetric.RSAKeyPair;
import cryptui.crypto.asymetric.RSAPublicKey;
import cryptui.crypto.hash.SHA3Hash;
import cryptui.crypto.symetric.AES;
import cryptui.crypto.symetric.AESEncryptedData;
import cryptui.crypto.symetric.AESException;
import cryptui.ui.list.FileListRenderer;
import cryptui.ui.list.KeyListRenderer;
import static cryptui.util.Assert.assertTrue;
import cryptui.util.AssertionException;
import cryptui.util.Base64Util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

public class CryptUI extends javax.swing.JFrame {

    private static File HOME_DIRECTORY;
    private static final Map<String, RSAKeyPair> KEY_MAP = new HashMap<>();
    private static final Map<String, IEncrypter> PUBLIC_KEY_MAP = new HashMap<>();
    private final DefaultListModel keyListModel = new DefaultListModel();
    private final DefaultListModel fileListModel = new DefaultListModel();
    private RSAKeyPair signingKeyPair;

    /**
     * Creates new form CryptUI
     */
    public CryptUI() {
        initComponents();
        setIconImage();
        keyList.setModel(keyListModel);
        fileList.setModel(fileListModel);
        File home = getHomeDirectory();
        File keyDir = getKeysDirectory(home);
        keyList.setCellRenderer(new KeyListRenderer());
        for (File file : keyDir.listFiles()) {
            if (file.isFile()) {
                if (loadKey(file)) {
                    tabbedPane.setSelectedIndex(1);
                }

            }
        }
        fileList.setCellRenderer(new FileListRenderer());
        File userHome = new File(System.getProperty("user.home"));
        setDirectoryForFileList(userHome);

    }

    private void setDirectoryForFileList(File userHome) {
        fileListModel.clear();
        File canonicalFile;
        try {
            canonicalFile = userHome.getCanonicalFile();
        } catch (IOException e) {
            canonicalFile = userHome;
        }
        File[] files = canonicalFile.listFiles();
        fileListModel.addElement(new File(canonicalFile + "/.."));
        if (files != null) {
            Arrays.sort(files, (a, b) -> a.isFile() == b.isFile() ? a.compareTo(b) : a.isFile() ? 1 : -1);
            for (File file : files) {
                fileListModel.addElement(file);
            }
        }
    }

    private File getKeysDirectory() {
        File home = getHomeDirectory();
        return getKeysDirectory(home);
    }

    private File getKeysDirectory(File home) {
        File[] keys = home.listFiles((d, f) -> "key".equals(f));
        File keyDir;
        if (keys.length == 0) {
            keyDir = new File(home.getAbsolutePath() + "/key");
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
        keyList = new javax.swing.JList<>();
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
        fileManagementTab = new javax.swing.JPanel();
        usedKey = new javax.swing.JLabel();
        encryptFileButton = new javax.swing.JButton();
        decryptFileButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        fileList = new javax.swing.JList<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Crypt UI");

        jScrollPane1.setViewportView(keyList);

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

        javax.swing.GroupLayout keyManagementTabLayout = new javax.swing.GroupLayout(keyManagementTab);
        keyManagementTab.setLayout(keyManagementTabLayout);
        keyManagementTabLayout.setHorizontalGroup(
            keyManagementTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(keyManagementTabLayout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(keyManagementTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(importKeyButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(exportPublicKeyButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(loadKeyButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(197, Short.MAX_VALUE))
        );
        keyManagementTabLayout.setVerticalGroup(
            keyManagementTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(keyManagementTabLayout.createSequentialGroup()
                .addGroup(keyManagementTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(keyManagementTabLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(loadKeyButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(importKeyButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(exportPublicKeyButton))
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 37, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Key Management", keyManagementTab);

        usedKey.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cryptui/ui/list/key_pair.png"))); // NOI18N
        usedKey.setText("key");

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

        fileList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fileListMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(fileList);

        javax.swing.GroupLayout fileManagementTabLayout = new javax.swing.GroupLayout(fileManagementTab);
        fileManagementTab.setLayout(fileManagementTabLayout);
        fileManagementTabLayout.setHorizontalGroup(
            fileManagementTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fileManagementTabLayout.createSequentialGroup()
                .addGroup(fileManagementTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(fileManagementTabLayout.createSequentialGroup()
                        .addComponent(usedKey, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(encryptFileButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(decryptFileButton))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 332, Short.MAX_VALUE))
        );
        fileManagementTabLayout.setVerticalGroup(
            fileManagementTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fileManagementTabLayout.createSequentialGroup()
                .addGroup(fileManagementTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(usedKey)
                    .addComponent(encryptFileButton)
                    .addComponent(decryptFileButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 251, Short.MAX_VALUE))
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
            keyListModel.addElement(rsa);
        } catch (RSAException | IOException ex) {
            Logger.getLogger(CryptUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_newKeyMouseClicked

    private void encryptFileButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_encryptFileButtonMouseClicked
        JFileChooser fc = new JFileChooser();
        int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File openFile = fc.getSelectedFile();
            byte[] bytes;
            try (FileInputStream fis = new FileInputStream(openFile)) {
                bytes = IOUtils.toByteArray(fis);
            } catch (IOException ex) {
                Logger.getLogger(CryptUI.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
            AES aes = new AES();
            IEncrypter rsa = (IEncrypter) keyListModel.getElementAt(keyList.getSelectedIndex());
            AESEncryptedData encryptedBytes;
            RSAEncryptedData rsaEncryptKey;
            try {
                encryptedBytes = aes.encrypt(ArrayUtils.addAll(signingKeyPair.createSignature(bytes, rsa.getHash()), bytes));
                rsaEncryptKey = rsa.encrypt(aes.getKey());
            } catch (RSAException | AESException ex) {
                Logger.getLogger(CryptUI.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
            JFileChooser fc2 = new JFileChooser();
            int returnVal2 = fc2.showSaveDialog(this);
            if (returnVal2 == JFileChooser.APPROVE_OPTION) {
                File saveFile = fc2.getSelectedFile();
                try (FileOutputStream fos = new FileOutputStream(saveFile)) {
                    fos.write(DataType.SENDER_HASH.getNumber());
                    fos.write(signingKeyPair.getHash());
                    rsaEncryptKey.writeToOutputStream(fos);
                    encryptedBytes.writeToOutputStream(fos);
                } catch (IOException ex) {
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
            try (FileInputStream fis = new FileInputStream(openFile)) {
                DataType rsaType = DataType.fromByte(fis.read());
                assertTrue(rsaType == DataType.SENDER_HASH);
                byte[] senderKeyHash = new byte[SHA3Hash.HASH_SIZE];
                fis.read(senderKeyHash);

                RSAEncryptedData encryptedAesKey = RSAEncryptedData.fromInputStream(fis);
                RSAKeyPair rsa = KEY_MAP.get(encryptedAesKey.getKeyHash());
                if (rsa == null) {
                    JOptionPane.showMessageDialog(this, "Can not decrypt file. No matching key found.");
                    return;
                }
                byte[] aesKey = rsa.decrypt(encryptedAesKey);

                AESEncryptedData aesEncryptedData = AESEncryptedData.fromInputStream(fis);
                AES aes = new AES(aesKey);
                byte[] decryptedData = aes.decrypt(aesEncryptedData);
                byte[] sign = Arrays.copyOfRange(decryptedData, 0, RSABase.SIGN_LENGTH);
                byte[] decryptedUseData = Arrays.copyOfRange(decryptedData, RSABase.SIGN_LENGTH, decryptedData.length);
                IEncrypter sender = PUBLIC_KEY_MAP.get(Base64Util.encodeToString(senderKeyHash));
                assertTrue(sender.verifySignature(sign, decryptedUseData, rsa.getHash()));

                JFileChooser fc2 = new JFileChooser();
                int returnVal2 = fc2.showSaveDialog(this);
                if (returnVal2 == JFileChooser.APPROVE_OPTION) {
                    File saveFile = fc2.getSelectedFile();
                    try (FileOutputStream fos = new FileOutputStream(saveFile)) {
                        fos.write(decryptedUseData);
                    }
                }

            } catch (IOException | AESException | RSAException ex) {
                Logger.getLogger(CryptUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_decryptFileButtonMouseClicked

    private void exportPublicKeyButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_exportPublicKeyButtonMouseClicked
        RSAKeyPair rsa = (RSAKeyPair) keyListModel.getElementAt(keyList.getSelectedIndex());
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
            try {
                File openFile = fc.getSelectedFile();
                RSAPublicKey publicKey = new RSAPublicKey(openFile);
                keyListModel.addElement(publicKey);
                PUBLIC_KEY_MAP.put(Base64Util.encodeToString(publicKey.getHash()), publicKey);

            } catch (RSAException ex) {
                Logger.getLogger(CryptUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_importKeyButtonMouseClicked

    private void fileListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fileListMouseClicked
        if (evt.getClickCount() == 2) {
            File file = (File) fileListModel.get(fileList.getSelectedIndex());
            if (file.isDirectory()) {
                setDirectoryForFileList(file);
            }

        }
    }//GEN-LAST:event_fileListMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton decryptFileButton;
    private javax.swing.JButton encryptFileButton;
    private javax.swing.JButton exportPublicKeyButton;
    private javax.swing.JList<String> fileList;
    private javax.swing.JPanel fileManagementTab;
    private javax.swing.JButton importKeyButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JList<String> keyList;
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
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JLabel usedKey;
    // End of variables declaration//GEN-END:variables

    private boolean loadKey(File file) {
        try {
            RSAKeyPair rsa = new RSAKeyPair(file);
            if (signingKeyPair == null) {
                signingKeyPair = rsa;
                usedKey.setText(rsa.toString());
            }
            keyListModel.addElement(rsa);
            final String encodeToString = Base64Util.encodeToString(rsa.getHash());
            KEY_MAP.put(encodeToString, rsa);
            PUBLIC_KEY_MAP.put(encodeToString, rsa);
            return true;
        } catch (AssertionException | RSAException ex) {
            Logger.getLogger(CryptUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
}
