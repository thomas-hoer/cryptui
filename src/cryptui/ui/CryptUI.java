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

import cryptui.crypto.asymetric.RSA;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private final DefaultListModel list;

    /**
     * Creates new form CryptUI
     */
    public CryptUI() {
        initComponents();
        setIconImage();
        list = new DefaultListModel();
        jList1.setModel(list);
        File home = getHomeDirectory();
        File keyDir = getKeysDirectory(home);
        for (File file:keyDir.listFiles()){
            if(file.isFile()){
                loadKey(file);
            }
        }
    }

    private File getKeysDirectory(){
                File home = getHomeDirectory();
                return getKeysDirectory(home);
    }
    private File getKeysDirectory(File home) {
        File[] keys = home.listFiles((d,f)->"key".equals(f));
        File keyDir;
        if (keys.length==0){
            keyDir = new File(home.getAbsolutePath()+"/key");
            keyDir.mkdir();
        }else{
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
    
    private File getHomeDirectory(){
        if (HOME_DIRECTORY != null){
            return HOME_DIRECTORY;
        }
        
        String dir;
        dir = System.getenv("LOCALAPPDATA");
        if (dir != null){
            File file = new File(dir+"/cryptui");
            file.mkdir();
            if(file.exists()){
                HOME_DIRECTORY = file;
                return HOME_DIRECTORY;
            }
        }
        dir = System.getProperty("user.home");
        if (dir != null){
            File file = new File(dir+"/cryptui");
            file.mkdir();
            if(file.exists()){
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

        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Crypt UI");

        jButton1.setText("Load Key");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton1MouseClicked(evt);
            }
        });

        jButton2.setText("Create New Key");
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton2MouseClicked(evt);
            }
        });

        jButton3.setText("Create Group Key");

        jButton4.setText("Import Key");

        jScrollPane1.setViewportView(jList1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton4)
                .addGap(0, 42, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2)
                    .addComponent(jButton3)
                    .addComponent(jButton4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseClicked
        JFileChooser fc = new JFileChooser();
        int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
        }
    }//GEN-LAST:event_jButton1MouseClicked

    private void jButton2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton2MouseClicked
        try {
            KeyPair keyPair = RSA.generateKeyPair();
            PrivateKey privateKey = keyPair.getPrivate();
                    File keysDir = getKeysDirectory();
                    File newKey = new File(keysDir.getAbsolutePath()+"/"+privateKey.toString()+".key");

            RSA.saveKeyInFile(privateKey, newKey);
            list.addElement(privateKey);
        } catch (GeneralSecurityException ex) {
            Logger.getLogger(CryptUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton2MouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JList<String> jList1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    private void loadKey(File file) {
        PrivateKey key = RSA.loadPrivateKeyFromFile(file);
        list.addElement(key);
    }
}
