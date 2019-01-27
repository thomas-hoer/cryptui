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

import de.cryptui.DataType;
import de.cryptui.crypto.container.RSAEncryptedData;
import de.cryptui.util.Base64Util;
import de.cryptui.util.NumberUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
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

/**
 * Base Class for RSA Keys. It provides features for persisting and loading RSA
 * Key from and to Files as well as naming the keys.
 */
public abstract class AbstractRSAKey {

	private static final Logger LOGGER = Logger.getLogger(RSAKeyPair.class.getName());
	private static final String CIPHER_ALGORITHM = "RSA/None/OAEPWithSHA3-512AndMGF1Padding";
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

	/**
	 * Loads existing RSA Key from file.
	 *
	 * @param file File containing a RSA Key in the CryptUI file format
	 * @return RSAKeyPair or RSAPublicKey
	 * @throws RSAException Throws exception when file can not be loaded or the file
	 *                      is in the wrong format.
	 */
	public static AbstractRSAKey fromFile(final File file) throws RSAException {
		try (final FileInputStream fis = new FileInputStream(file)) {
			return fromStream(fis);
		} catch (final IOException e) {
			throw new RSAException(e);
		}
	}

	/**
	 * Loads existing RSA Key from InputStream.
	 *
	 * @param inputStream InputStream containing a RSA Key in the CryptUI file
	 *                    format
	 * @return RSAKeyPair or RSAPublicKey
	 * @throws RSAException Throws exception when key can not be loaded or is in the
	 *                      wrong format.
	 */
	public static AbstractRSAKey fromStream(final InputStream inputStream) throws RSAException {
		try {
			final KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
			final byte[] bytes = IOUtils.toByteArray(inputStream);
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
							StandardCharsets.UTF_8);
					currentPosition += nameLenght;
					break;

				case DESCRIPTION_SHORT:
					final int commentLenght = bytes[currentPosition++];
					comment = new String(Arrays.copyOfRange(bytes, currentPosition, currentPosition + commentLenght),
							StandardCharsets.UTF_8);
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
					salt = Arrays.copyOfRange(bytes, currentPosition, currentPosition + AbstractRSAKey.SALT_LENGTH);
					currentPosition += AbstractRSAKey.SALT_LENGTH;
					publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyData));
					break;

				default:
					throw new IllegalStateException();
				}
			}
			if (privateKey != null) {
				return new RSAKeyPair(name, comment, privateKey, publicKey, salt);
			}
			return new RSAPublicKey(publicKey, name, salt);
		} catch (IOException | NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException ex) {
			throw new RSAException(ex);
		}
	}

	protected static final byte[] generateSalt() {
		final byte[] salt = new byte[SALT_LENGTH];
		new SecureRandom().nextBytes(salt);
		return salt;
	}

	protected static void writeObjectName(final OutputStream fos, final String name) throws IOException {
		fos.write(DataType.OBJECT_NAME.getNumber());
		final byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
		if (nameBytes.length < 128) {
			fos.write(nameBytes.length);
			fos.write(nameBytes);
		} else {
			fos.write(127);
			fos.write(nameBytes, 0, 127);
		}
	}

	protected RSAEncryptedData encrypt(final Key key, final byte[] data) throws RSAException {
		try {
			cipher.init(Cipher.ENCRYPT_MODE, key);
			return new RSAEncryptedData(cipher.doFinal(data), getHash());
		} catch (IllegalBlockSizeException | InvalidKeyException | BadPaddingException ex) {
			throw new RSAException(ex);
		}
	}

	protected static byte[] decrpyt(final Key key, final RSAEncryptedData data) throws RSAException {
		try {
			cipher.init(Cipher.DECRYPT_MODE, key);
			return cipher.doFinal(data.getEncryptedData());
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
			throw new RSAException(ex);
		}

	}

	protected static boolean verifySignature(final PublicKey publicKey, final byte[] sign, final byte[] dat,
			final byte[] recipient) throws RSAException {
		try {
			final Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
			signature.initVerify(publicKey);
			signature.update(dat);
			signature.update(recipient);
			return signature.verify(sign);
		} catch (SignatureException | InvalidKeyException | NoSuchAlgorithmException ex) {
			throw new RSAException(ex);
		}
	}

	protected final String generateName(final String suggestedName) {
		if (StringUtils.isEmpty(suggestedName)) {
			return Base64Util.encodeToString(getHash()).substring(0, 16);
		}
		return suggestedName;
	}

	public abstract byte[] getHash();

	public abstract void saveKeyInFile(File file) throws IOException;

	public abstract void saveKeyInStream(OutputStream outputStream) throws IOException;

}
