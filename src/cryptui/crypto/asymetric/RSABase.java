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

import cryptui.util.Base64Util;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.apache.commons.lang3.StringUtils;

public abstract class RSABase {

    public static final String CIPHER_ALGORITHM = "RSA/None/OAEPWithSHA3-512AndMGF1Padding";
    public static final String SIGNATURE_ALGORITHM = "SHA512withRSAandMGF1";

    public static final int KEY_LENGHT_BITS = 4096;
    public static final int KEY_LENGHT_BYTES = 512;
    public static final int SALT_LENGTH = 128;
    public static final int SIGN_LENGTH = 512;

    protected static Cipher cipher;

    static {
        try {
            cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {
            Logger.getLogger(RSAKeyPair.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected RSAEncryptedData encrypt(Key key, byte[] data) throws RSAException {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return new RSAEncryptedData(cipher.doFinal(data), getHash());
        } catch (IllegalBlockSizeException | InvalidKeyException | BadPaddingException ex) {
            throw new RSAException(ex);
        }
    }

    protected byte[] decrpyt(Key key, RSAEncryptedData data) throws RSAException {
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(data.getEncryptedData());
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            throw new RSAException(ex);
        }

    }

    protected boolean verifySignature(PublicKey publicKey, byte[] sign, byte[] dat, byte[] recipient) throws RSAException {
        try {
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(publicKey);
            signature.update(dat);
            signature.update(recipient);
            return signature.verify(sign);
        } catch (SignatureException | InvalidKeyException | NoSuchAlgorithmException ex) {
            Logger.getLogger(RSAKeyPair.class.getName()).log(Level.SEVERE, null, ex);
            throw new RSAException(ex);
        }
    }

    protected final KeyPair generateKeyPair() throws RSAException {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(new RSAKeyGenParameterSpec(KEY_LENGHT_BITS, RSAKeyGenParameterSpec.F4));
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException ex) {
            throw new RSAException(ex);
        }
    }

    protected final byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    protected final String generateName(String suggestedName) {
        if (StringUtils.isEmpty(suggestedName)) {
            return Base64Util.encodeToString(getHash()).substring(0, 8);
        } else {
            return suggestedName;
        }
    }

    public abstract byte[] getHash();

}
