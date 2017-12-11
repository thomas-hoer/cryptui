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

import cryptui.crypto.container.RSAEncryptedData;
import cryptui.DataType;
import cryptui.crypto.hash.SHA3Hash;
import static cryptui.util.Assert.assertTrue;
import cryptui.util.NumberUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class RSAPublicKey extends RSABase implements IEncrypter {

    private final PublicKey publicKey;
    private final byte[] salt;
    private final String name;

    public RSAPublicKey(PublicKey publicKey, String name, byte[] salt) {
        this.publicKey = publicKey;
        this.salt = salt;
        this.name = generateName(name);
    }

    public RSAPublicKey(File file) throws RSAException {
        try (FileInputStream fis = new FileInputStream(file)) {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");

            //TODO: Remove duplication
            DataType nameType = DataType.fromByte(fis.read());
            assertTrue(nameType == DataType.OBJECT_NAME);
            int nameLenght = fis.read();
            byte[] nameBytes = new byte[nameLenght];
            fis.read(nameBytes);
            name = new String(nameBytes, "UTF-8");

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

            byte[] publicKeyEncoded = publicKey.getEncoded();
            fos.write(DataType.PUBLIC_KEY.getNumber());
            fos.write(NumberUtils.intToByteArray(publicKeyEncoded.length));
            fos.write(publicKeyEncoded);
            fos.write(salt);
        }
    }

    @Override
    public RSAEncryptedData encrypt(byte[] data) throws RSAException {
        return encrypt(publicKey, data);
    }

    @Override
    public boolean verifySignature(byte[] sign, byte[] dat, byte[] recipient) throws RSAException {
        return verifySignature(publicKey, sign, dat, recipient);
    }

    @Override
    public final byte[] getHash() {
        return SHA3Hash.hash(publicKey.getEncoded(), salt);
    }

    @Override
    public String toString() {
        return name;
    }
}
