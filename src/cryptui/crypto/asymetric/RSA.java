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
package cryptui.crypto.asymetric;

import cryptui.DataType;
import cryptui.crypto.hash.SHA3Hash;
import cryptui.util.Base64Util;
import cryptui.util.NumberUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.apache.commons.lang3.StringUtils;

public class RSA {

    private static Cipher cipher;

    static {
        try {
            cipher = Cipher.getInstance("RSA/None/OAEPWithSHA3-512AndMGF1Padding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {
            Logger.getLogger(RSA.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private final String name;
    private final String comment;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    public RSA(String name, String comment) throws GeneralSecurityException {
        this.name = name;
        this.comment = comment;
        generateKeyPair();
    }

    public RSA(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");

            //TODO: Remove duplication
            DataType nameType = DataType.fromByte(fis.read());
            assert (nameType == DataType.OBJECT_NAME);
            int nameLenght = fis.read();
            byte[] nameBytes = new byte[nameLenght];
            fis.read(nameBytes);
            name = new String(nameBytes, "UTF-8");

            DataType commentType = DataType.fromByte(fis.read());
            assert (commentType == DataType.DESCRIPTION_SHORT);
            int commentLenght = fis.read();
            byte[] commentBytes = new byte[commentLenght];
            fis.read(commentBytes);
            comment = new String(commentBytes, "UTF-8");

            DataType privateType = DataType.fromByte(fis.read());
            assert (privateType == DataType.PRIVATE_KEY);
            int privateLenght = NumberUtils.intFromInputStream(fis);
            byte[] privateKeyData = new byte[privateLenght];
            fis.read(privateKeyData);
            privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyData));

            DataType publicType = DataType.fromByte(fis.read());
            assert (publicType == DataType.PUBLIC_KEY);
            int publicLenght = NumberUtils.intFromInputStream(fis);
            byte[] publicKeyData = new byte[publicLenght];
            fis.read(publicKeyData);
            publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyData));

        } catch (IOException | NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void generateKeyPair() throws GeneralSecurityException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(new RSAKeyGenParameterSpec(4096, RSAKeyGenParameterSpec.F4));
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();
    }

    public void saveKeyInFile(File file) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(DataType.OBJECT_NAME.getNumber());
            byte[] nameBytes = name.getBytes("UTF-8");
            if (nameBytes.length < 128) {
                fos.write(nameBytes.length);
                fos.write(nameBytes);
            } else {
                fos.write(127);
                fos.write(nameBytes, 0, 127);
            }

            fos.write(DataType.DESCRIPTION_SHORT.getNumber());
            byte[] commentBytes = comment.getBytes("UTF-8");
            if (commentBytes.length < 128) {
                fos.write(commentBytes.length);
                fos.write(commentBytes);
            } else {
                fos.write(127);
                fos.write(commentBytes, 0, 127);
            }

            byte[] privateKeyEncoded = privateKey.getEncoded();
            fos.write(DataType.PRIVATE_KEY.getNumber());
            fos.write(NumberUtils.intToByteArray(privateKeyEncoded.length));
            fos.write(privateKeyEncoded);
            byte[] publicKeyEncoded = publicKey.getEncoded();
            fos.write(DataType.PUBLIC_KEY.getNumber());
            fos.write(NumberUtils.intToByteArray(publicKeyEncoded.length));
            fos.write(publicKeyEncoded);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPublicKeyFromFile(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[fis.available()];
            fis.read(data);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
            publicKey = keyFactory.generatePublic(new PKCS8EncodedKeySpec(data));
        } catch (IOException | NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    public byte[] encrypt(byte[] data) throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    public byte[] decrypt(byte[] data) throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(data);
    }

    @Override
    public String toString() {
        if (StringUtils.isNotEmpty(comment)) {
            return name + " - " + comment;
        } else if (StringUtils.isNoneEmpty(name)) {
            return name;
        } else {
            return "No Name";
        }
    }

    public byte[] getPublicKeyEncoded() {
        return publicKey.getEncoded();
    }
}
