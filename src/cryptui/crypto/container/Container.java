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
package cryptui.crypto.container;

import cryptui.DataType;
import cryptui.crypto.KeyStore;
import cryptui.crypto.asymetric.IEncrypter;
import cryptui.crypto.asymetric.RSABase;
import cryptui.crypto.asymetric.RSAException;
import cryptui.crypto.asymetric.RSAKeyPair;
import cryptui.crypto.hash.SHA3Hash;
import cryptui.crypto.symetric.AES;
import cryptui.crypto.symetric.AESException;
import cryptui.util.AssertionException;
import cryptui.util.Base64Util;
import cryptui.util.NumberUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;

public class Container {

    private byte[] senderKeyHash;
    private List<RSAEncryptedData> rsaEncryptedData = new ArrayList<>();
    private AESEncryptedData aesEncryptedData;
    private final byte[] recipients;

    private byte[] signature;
    private byte[] decryptedData;

    public Container(File openFile) throws IOException {
        byte[] bytes;
        try (FileInputStream fis = new FileInputStream(openFile)) {
            bytes = IOUtils.toByteArray(fis);
        }
        int currentPosition = 0;
        ByteArrayOutputStream recipientsBuilder = new ByteArrayOutputStream();
        while (currentPosition < bytes.length) {
            DataType dataType = DataType.fromByte(bytes[currentPosition]);
            currentPosition++;
            switch (dataType) {
                case SENDER_HASH: {
                    senderKeyHash = Arrays.copyOfRange(bytes, currentPosition, currentPosition + SHA3Hash.HASH_SIZE);
                    currentPosition += SHA3Hash.HASH_SIZE;
                    break;
                }
                case RSA_ENCRYPTED_DATA: {
                    byte[] encryptedKeyHash = Arrays.copyOfRange(bytes, currentPosition, currentPosition + SHA3Hash.HASH_SIZE);
                    currentPosition += SHA3Hash.HASH_SIZE;
                    int encryptedKeyLength = NumberUtils.byteArrayToInt(bytes, currentPosition);
                    currentPosition += 4;
                    byte[] encryptedKeyData = Arrays.copyOfRange(bytes, currentPosition, currentPosition + encryptedKeyLength);
                    currentPosition += encryptedKeyLength;
                    rsaEncryptedData.add(new RSAEncryptedData(encryptedKeyData, encryptedKeyHash));
                    recipientsBuilder.write(encryptedKeyHash);
                    break;
                }
                case AES_ENCRYPTED_DATA: {
                    byte[] iv = Arrays.copyOfRange(bytes, currentPosition, currentPosition + AES.IV_LENGTH);
                    currentPosition += AES.IV_LENGTH;
                    int encryptedDataLenght = NumberUtils.byteArrayToInt(bytes, currentPosition);
                    currentPosition += 4;
                    byte[] encryptedData = Arrays.copyOfRange(bytes, currentPosition, currentPosition + encryptedDataLenght);
                    currentPosition += encryptedDataLenght;
                    aesEncryptedData = new AESEncryptedData(iv, encryptedData);
                    break;
                }
                default:
                    throw new AssertionException();
            }
        }
        this.recipients = recipientsBuilder.toByteArray();
    }

    public boolean decrypt() {
        for (RSAEncryptedData rsaData : rsaEncryptedData) {
            RSAKeyPair rsaKey = KeyStore.getPrivate(rsaData.getKeyHash());
            if (rsaKey != null) {
                try {
                    byte[] aesKey = rsaKey.decrypt(rsaData);
                    AES aes = new AES(aesKey);
                    decryptedData = aes.decrypt(aesEncryptedData);
                    byte[] aesDecryptedData = aes.decrypt(aesEncryptedData);
                    signature = Arrays.copyOfRange(aesDecryptedData, 0, RSABase.SIGN_LENGTH);
                    decryptedData = Arrays.copyOfRange(aesDecryptedData, RSABase.SIGN_LENGTH, decryptedData.length);
                    return true;
                } catch (RSAException | AESException ex) {
                    Logger.getLogger(Container.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return false;
    }

    public boolean verify() {
        IEncrypter sender = KeyStore.getPublic(Base64Util.encodeToString(senderKeyHash));
        if (sender == null) {
            return false;
        }
        try {
            return sender.verifySignature(signature, decryptedData, recipients);
        } catch (RSAException ex) {
            Logger.getLogger(Container.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

    }

    public byte[] getDecryptedData() {
        return decryptedData;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Encrypted for:\n");
        rsaEncryptedData.forEach((rsaData) -> {
            IEncrypter rsa = KeyStore.getPublic(rsaData.getKeyHash());
            if (rsa == null) {
                builder.append(rsaData.getKeyHash().substring(0, 8));
            } else {
                builder.append(rsa.toString());
            }
            builder.append("\n");
        });
        IEncrypter sender = KeyStore.getPublic(Base64Util.encodeToString(senderKeyHash));
        builder.append("\nSigned by:\n");
        if (sender == null) {
            builder.append(Base64Util.encodeToString(senderKeyHash).substring(0, 8));
        } else {
            builder.append(sender.toString());
        }
        return builder.toString();
    }
}
