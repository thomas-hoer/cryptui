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
import static cryptui.util.Assert.assertTrue;
import cryptui.util.NumberUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import org.apache.commons.lang3.StringUtils;

public class RSAKeyPair extends RSABase {

    private final String name;
    private final String comment;
    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final byte[] salt;

    public RSAKeyPair(String name, String comment) throws RSAException {
        this.name = name;
        this.comment = comment;
        KeyPair keyPair = generateKeyPair();
        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();
        this.salt = generateSalt();
        getHash();
    }

    public RSAKeyPair(File file) throws RSAException {
        try (FileInputStream fis = new FileInputStream(file)) {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");

            //TODO: Remove duplication
            DataType nameType = DataType.fromByte(fis.read());
            assertTrue(nameType == DataType.OBJECT_NAME);
            int nameLenght = fis.read();
            byte[] nameBytes = new byte[nameLenght];
            fis.read(nameBytes);
            name = new String(nameBytes, "UTF-8");

            DataType commentType = DataType.fromByte(fis.read());
            assertTrue(commentType == DataType.DESCRIPTION_SHORT);
            int commentLenght = fis.read();
            byte[] commentBytes = new byte[commentLenght];
            fis.read(commentBytes);
            comment = new String(commentBytes, "UTF-8");

            DataType privateType = DataType.fromByte(fis.read());
            assertTrue(privateType == DataType.PRIVATE_KEY);
            int privateLenght = NumberUtils.intFromInputStream(fis);
            byte[] privateKeyData = new byte[privateLenght];
            fis.read(privateKeyData);
            privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyData));

            DataType publicType = DataType.fromByte(fis.read());
            assertTrue(publicType == DataType.PUBLIC_KEY);
            int publicLenght = NumberUtils.intFromInputStream(fis);
            byte[] publicKeyData = new byte[publicLenght];
            fis.read(publicKeyData);
            this.salt = new byte[RSABase.SALT_LENGTH];
            fis.read(salt);
            publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyData));

        } catch (IOException | NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException e) {
            throw new RSAException(e);
        }
    }

    public void saveKeyInFile(File file) throws IOException {
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
            fos.write(salt);
        }
    }

    public RSAEncryptedData encrypt(byte[] data) throws RSAException {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return new RSAEncryptedData(cipher.doFinal(data), getHash());
        } catch (IllegalBlockSizeException | InvalidKeyException | BadPaddingException ex) {
            throw new RSAException(ex);
        }
    }

    public byte[] decrypt(RSAEncryptedData data) throws RSAException {
        return decrpyt(privateKey, data);
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

    @Override
    public byte[] getHash() {
        return SHA3Hash.hash(publicKey.getEncoded(), salt);
    }
}
