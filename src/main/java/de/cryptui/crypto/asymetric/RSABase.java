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
package de.cryptui.crypto.asymetric;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import de.cryptui.DataType;
import de.cryptui.crypto.container.RSAEncryptedData;
import de.cryptui.util.Base64Util;
import de.cryptui.util.NumberUtils;

public abstract class RSABase {

	private static final Logger LOGGER = Logger.getLogger(RSAKeyPair.class.getName());
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
			LOGGER.log(Level.SEVERE, null, ex);
		}
	}

	public static RSABase fromFile(final File file) {
		final FileInputStream fis;
		try {
			final KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
			fis = new FileInputStream(file);
			final byte[] bytes = IOUtils.toByteArray(fis);
			int currentPosition = 0;
			String name = null;
			String comment = null;
			PrivateKey privateKey = null;
			PublicKey publicKey = null;
			byte[] salt = null;

			while (currentPosition < bytes.length) {
				final DataType dataType = DataType.fromByte(bytes[currentPosition]);
				currentPosition++;
				switch (dataType) {
				case OBJECT_NAME:
					final int nameLenght = bytes[currentPosition++];
					name = new String(Arrays.copyOfRange(bytes, currentPosition, currentPosition + nameLenght),
							"UTF-8");
					currentPosition += nameLenght;
					break;

				case DESCRIPTION_SHORT:
					final int commentLenght = bytes[currentPosition++];
					comment = new String(Arrays.copyOfRange(bytes, currentPosition, currentPosition + commentLenght),
							"UTF-8");
					currentPosition += commentLenght;
					break;

				case PRIVATE_KEY:
					final int privateKeyLenght = NumberUtils.byteArrayToInt(bytes, currentPosition);
					currentPosition += NumberUtils.SIZE_OF_INT_IN_BYTES;
					final byte[] privateKeyData = Arrays.copyOfRange(bytes, currentPosition,
							currentPosition + privateKeyLenght);
					currentPosition += privateKeyLenght;
					privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyData));
					break;

				case PUBLIC_KEY:
					final int publicKeyLenght = NumberUtils.byteArrayToInt(bytes, currentPosition);
					currentPosition += NumberUtils.SIZE_OF_INT_IN_BYTES;
					final byte[] publicKeyData = Arrays.copyOfRange(bytes, currentPosition,
							currentPosition + publicKeyLenght);
					currentPosition += publicKeyLenght;
					salt = Arrays.copyOfRange(bytes, currentPosition, currentPosition + RSABase.SALT_LENGTH);
					currentPosition += RSABase.SALT_LENGTH;
					publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyData));
					break;

				default:
					throw new IllegalStateException();
				}
			}
			if (privateKey != null) {
				return new RSAKeyPair(name, comment, privateKey, publicKey, salt);
			} else {
				return new RSAPublicKey(publicKey, name, salt);
			}
		} catch (IOException | NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException ex) {
			Logger.getLogger(RSABase.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	protected RSAEncryptedData encrypt(final Key key, final byte[] data) throws RSAException {
		try {
			cipher.init(Cipher.ENCRYPT_MODE, key);
			return new RSAEncryptedData(cipher.doFinal(data), getHash());
		} catch (IllegalBlockSizeException | InvalidKeyException | BadPaddingException ex) {
			throw new RSAException(ex);
		}
	}

	protected byte[] decrpyt(final Key key, final RSAEncryptedData data) throws RSAException {
		try {
			cipher.init(Cipher.DECRYPT_MODE, key);
			return cipher.doFinal(data.getEncryptedData());
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
			throw new RSAException(ex);
		}

	}

	protected boolean verifySignature(final PublicKey publicKey, final byte[] sign, final byte[] dat,
			final byte[] recipient) throws RSAException {
		try {
			final Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
			signature.initVerify(publicKey);
			signature.update(dat);
			signature.update(recipient);
			return signature.verify(sign);
		} catch (SignatureException | InvalidKeyException | NoSuchAlgorithmException ex) {
			LOGGER.log(Level.SEVERE, null, ex);
			throw new RSAException(ex);
		}
	}

	protected final KeyPair generateKeyPair() throws RSAException {
		try {
			final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(new RSAKeyGenParameterSpec(KEY_LENGHT_BITS, RSAKeyGenParameterSpec.F4));
			return keyPairGenerator.generateKeyPair();
		} catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException ex) {
			throw new RSAException(ex);
		}
	}

	protected final byte[] generateSalt() {
		final byte[] salt = new byte[SALT_LENGTH];
		new SecureRandom().nextBytes(salt);
		return salt;
	}

	protected final String generateName(final String suggestedName) {
		if (StringUtils.isEmpty(suggestedName)) {
			return Base64Util.encodeToString(getHash()).substring(0, 16);
		} else {
			return suggestedName;
		}
	}

	public abstract byte[] getHash();

	public abstract void saveKeyInFile(File file) throws IOException;

}
