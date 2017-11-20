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
package cryptui.crypto.symetric;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.util.Arrays;

/**
 *
 * @author thomas-hoer
 */
public class AES {

    public static final int IV_LENGTH = 12;
    public static final int KEY_SIZE_BITS = 128;

    private Cipher cipher;
    private byte[] keyBytes;
    private Key key;
    private SecureRandom sr = new SecureRandom();

    public AES() {
        keyBytes = new byte[16];
        sr.nextBytes(keyBytes);
        key = new SecretKeySpec(keyBytes, "AES");
        try {
            cipher = Cipher.getInstance("AES/GCM/NoPadding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {
            Logger.getLogger(AES.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public AES(byte[] keyBytes) {
        assert (keyBytes.length == 16);
        this.keyBytes = keyBytes;
        key = new SecretKeySpec(keyBytes, "AES");
        try {
            cipher = Cipher.getInstance("AES/GCM/NoPadding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {
            Logger.getLogger(AES.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public AESEncryptedData encrypt(byte[] src) throws AESException {
        try {
            byte[] iv = new byte[IV_LENGTH];
            sr.nextBytes(iv);
            GCMParameterSpec params = new GCMParameterSpec(KEY_SIZE_BITS, iv, 0, IV_LENGTH);
            cipher.init(Cipher.ENCRYPT_MODE, key, params);
            byte[] cipherText = cipher.doFinal(src);
            return new AESEncryptedData(iv, cipherText);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException ex) {
            throw new AESException(ex);
        }
    }

    public byte[] decrypt(AESEncryptedData encryptedData) throws AESException {
        try {
            GCMParameterSpec params = new GCMParameterSpec(KEY_SIZE_BITS, encryptedData.getIv(), 0, IV_LENGTH);
            cipher.init(Cipher.DECRYPT_MODE, key, params);
            return cipher.doFinal(encryptedData.getData(), 0, encryptedData.getData().length);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException ex) {
            throw new AESException(ex);
        }
    }

    public byte[] getKey() {
        return Arrays.clone(this.keyBytes);
    }
}
