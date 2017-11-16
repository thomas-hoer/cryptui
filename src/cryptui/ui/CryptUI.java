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
import cryptui.crypto.asymetric.RSA;
import cryptui.crypto.hash.SHA3Hash;
import cryptui.crypto.symetric.AES;
import cryptui.crypto.symetric.AESEncryptedData;
import cryptui.util.Base64Util;
import cryptui.util.NumberUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.ImageIcon;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author Ich
 */
public class CryptUI extends javax.swing.JFrame {

    private static File HOME_DIRECTORY;
    private static Map<String, RSA> keyMap;
    private final DefaultListModel list;

    /**
     * Creates new form CryptUI
     */
    public CryptUI() {
        initComponents();
        setIconImage();
        list = new DefaultListModel();
        keyList.setModel(list);
        File home = getHomeDirectory();
        File keyDir = getKeysDirectory(home);
        for (File file : keyDir.listFiles()) {
            if (file.isFile()) {
                loadKey(file);
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

        loadKeyButton = new javax.swing.JButton();
        newKeyButton = new javax.swing.JButton();
        importKeyButton = new javax.swing.JButton();
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
        exportPublicKeyButton = new javax.swing.JButton();
        encryptFileButton = new javax.swing.JButton();
        decryptFileButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Crypt UI");

        loadKeyButton.setText("Load Key");
        loadKeyButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                loadKeyButtonMouseClicked(evt);
            }
        });

        newKeyButton.setText("Create New Key");
        newKeyButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                newKeyMouseClicked(evt);
            }
        });

        importKeyButton.setText("Import Key");

        jScrollPane1.setViewportView(keyList);

        newKeyNameLabel.setText("Name");

        newKeyCommentLabel.setText("Comment");

        newKeyName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newKeyNameActionPerformed(evt);
            }
        });

        newKeyTypeLabel.setText("Type");

        newKeyTypeRadioRSA.setSelected(true);
        newKeyTypeRadioRSA.setText("RSA");
        newKeyTypeRadioRSA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newKeyTypeRadioRSAActionPerformed(evt);
            }
        });

        newKeyStrenghtLabel.setText("Strenght");

        newKeyStrengthRadio4096.setSelected(true);
        newKeyStrengthRadio4096.setText("4096 Bit");

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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        exportPublicKeyButton.setText("Export Public Key");

        encryptFileButton.setText("Encrypt File");
        encryptFileButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                encryptFileButtonMouseClicked(evt);
            }
        });

        decryptFileButton.setText("Decrypt File");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(loadKeyButton, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(newKeyButton)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(importKeyButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(exportPublicKeyButton))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(encryptFileButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(decryptFileButton)
                .addGap(0, 8, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(loadKeyButton)
                    .addComponent(newKeyButton)
                    .addComponent(importKeyButton)
                    .addComponent(exportPublicKeyButton)
                    .addComponent(encryptFileButton)
                    .addComponent(decryptFileButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

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
            RSA rsa = new RSA(newKeyName.getText(), newKeyComment.getText());
            newKeyName.setText("");
            newKeyComment.setText("");
            File keysDir = getKeysDirectory();
            File newKey = new File(keysDir.getAbsolutePath() + "/" + rsa.hashCode() + ".key");

            rsa.saveKeyInFile(newKey);
            list.addElement(rsa);
        } catch (GeneralSecurityException ex) {
            Logger.getLogger(CryptUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_newKeyMouseClicked

    private void newKeyTypeRadioRSAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newKeyTypeRadioRSAActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_newKeyTypeRadioRSAActionPerformed

    private void newKeyNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newKeyNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_newKeyNameActionPerformed

    private void encryptFileButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_encryptFileButtonMouseClicked
        JFileChooser fc = new JFileChooser();
        int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File openFile = fc.getSelectedFile();
            try (FileInputStream fis = new FileInputStream(openFile)) {
                byte[] bytes = IOUtils.toByteArray(fis);
                AES aes = new AES();
                AESEncryptedData encryptedBytes = aes.encrypt(bytes);
                JFileChooser fc2 = new JFileChooser();
                int returnVal2 = fc2.showSaveDialog(this);
                RSA rsa = (RSA) list.getElementAt(keyList.getSelectedIndex());
                if (returnVal2 == JFileChooser.APPROVE_OPTION) {
                    File saveFile = fc2.getSelectedFile();
                    try (FileOutputStream fos = new FileOutputStream(saveFile)) {
                        fos.write(DataType.RSA_ENCRYPTED_DATA.getNumber());
                        System.out.println(Base64Util.encodeToString(aes.getKey()));
                        final byte[] rsaEncryptKey = rsa.encrypt(aes.getKey());

                        fos.write(NumberUtils.intToByteArray(rsaEncryptKey.length + 64));
                        fos.write(SHA3Hash.hash(rsa.getPublicKeyEncoded()));
                        fos.write(rsaEncryptKey);

                        fos.write(DataType.AES_ENCRYPTED_DATA.getNumber());
                        fos.write(NumberUtils.intToByteArray(encryptedBytes.getData().length));
                        fos.write(encryptedBytes.getData());
                    } catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {
                        Logger.getLogger(CryptUI.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            } catch (IOException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException ex) {
                Logger.getLogger(CryptUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }//GEN-LAST:event_encryptFileButtonMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton decryptFileButton;
    private javax.swing.JButton encryptFileButton;
    private javax.swing.JButton exportPublicKeyButton;
    private javax.swing.JButton importKeyButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList<String> keyList;
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
    // End of variables declaration//GEN-END:variables

    private void loadKey(File file) {
        RSA rsa = new RSA(file);
        list.addElement(rsa);
        keyMap.put(Base64Util.encodeToString(rsa.getPublicKeyEncoded()), rsa);
    }
}
