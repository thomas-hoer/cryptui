/*
 * Copyright 2019 Thomas Hoermann
 * https://github.com/thomas-hoer
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
package de.cryptui.crypto.symetric;

import static de.cryptui.util.Assert.assertEqual;

import de.cryptui.crypto.container.AESEncryptedData;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.util.Arrays;

/**
 * Wrapper Class for AES/GCM/NoPadding. It provides easy access to encryption
 * and decryption of data. This class is immutable.
 */
public final class AES {

	public static final int IV_LENGTH = 12;
	public static final int KEY_SIZE_BITS = 128;
	public static final int KEY_SIZE_BYTES = 16;

	private final Cipher cipher;
	private final byte[] keyBytes;
	private final Key key;
	private final SecureRandom secureRandom = new SecureRandom();

	/**
	 * Creates a new AES Cipher with a new random generated Key.
	 *
	 * @throws AESException if cipher can not be loaded
	 */
	public AES() throws AESException {
		keyBytes = new byte[KEY_SIZE_BYTES];
		secureRandom.nextBytes(keyBytes);
		key = new SecretKeySpec(keyBytes, "AES");
		try {
			cipher = Cipher.getInstance("AES/GCM/NoPadding");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {
			throw new AESException(ex);
		}
	}

	/**
	 * Creates a new AES Cipher with predefined Key.
	 *
	 * @param keyBytes byte array of size 16, the values should have high entropy
	 * @throws AESException if cipher can not be loaded
	 */
	public AES(final byte[] keyBytes) throws AESException {
		assertEqual(KEY_SIZE_BYTES, keyBytes.length, "AES Key requires to have lenght of 16 bytes");
		this.keyBytes = Arrays.clone(keyBytes);
		key = new SecretKeySpec(this.keyBytes, "AES");
		try {
			cipher = Cipher.getInstance("AES/GCM/NoPadding");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {
			throw new AESException(ex);
		}
	}

	public AESEncryptedData encrypt(final byte[] src) throws AESException {
		try {
			final byte[] iv = new byte[IV_LENGTH];
			secureRandom.nextBytes(iv);
			final GCMParameterSpec params = new GCMParameterSpec(KEY_SIZE_BITS, iv, 0, IV_LENGTH);
			cipher.init(Cipher.ENCRYPT_MODE, key, params);
			final byte[] cipherText = cipher.doFinal(src);
			return new AESEncryptedData(iv, cipherText);
		} catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException
				| BadPaddingException ex) {
			throw new AESException(ex);
		}
	}

	public byte[] decrypt(final AESEncryptedData encryptedData) throws AESException {
		try {
			final GCMParameterSpec params = new GCMParameterSpec(KEY_SIZE_BITS, encryptedData.getIv(), 0, IV_LENGTH);
			cipher.init(Cipher.DECRYPT_MODE, key, params);
			return cipher.doFinal(encryptedData.getData(), 0, encryptedData.getData().length);
		} catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException
				| BadPaddingException ex) {
			throw new AESException(ex);
		}
	}

	/**
	 * Get key of current cipher.
	 *
	 * @return a copy of the current key. The key should be kept secret.
	 */
	public byte[] getKey() {
		return Arrays.clone(this.keyBytes);
	}
}
