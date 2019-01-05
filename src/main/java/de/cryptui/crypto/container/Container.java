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
package de.cryptui.crypto.container;

import de.cryptui.DataType;
import de.cryptui.crypto.KeyStore;
import de.cryptui.crypto.asymetric.AbstractRSAKey;
import de.cryptui.crypto.asymetric.IEncrypter;
import de.cryptui.crypto.asymetric.RSAException;
import de.cryptui.crypto.asymetric.RSAKeyPair;
import de.cryptui.crypto.hash.SHA3Hash;
import de.cryptui.crypto.symetric.AES;
import de.cryptui.crypto.symetric.AESException;
import de.cryptui.util.Base64Util;
import de.cryptui.util.NumberUtils;

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

	private static final Logger LOGGER = Logger.getLogger(Container.class.getName());
	private byte[] senderKeyHash;
	private final List<RSAEncryptedData> rsaEncryptedData = new ArrayList<>();
	private AESEncryptedData aesEncryptedData;
	private final byte[] recipients;

	private byte[] signature;
	private byte[] decryptedData;

	public Container(final File openFile) throws IOException, DecryptionException {
		byte[] bytes;
		try (FileInputStream fis = new FileInputStream(openFile)) {
			bytes = IOUtils.toByteArray(fis);
		}
		int currentPosition = 0;
		final ByteArrayOutputStream recipientsBuilder = new ByteArrayOutputStream();
		while (currentPosition < bytes.length) {
			final DataType dataType = DataType.fromByte(bytes[currentPosition]);
			currentPosition++;
			switch (dataType) {
			case SENDER_HASH:
				senderKeyHash = Arrays.copyOfRange(bytes, currentPosition, currentPosition + SHA3Hash.HASH_SIZE);
				currentPosition += SHA3Hash.HASH_SIZE;
				break;

			case RSA_ENCRYPTED_DATA:
				final byte[] encryptedKeyHash = Arrays.copyOfRange(bytes, currentPosition,
						currentPosition + SHA3Hash.HASH_SIZE);
				currentPosition += SHA3Hash.HASH_SIZE;
				final int encryptedKeyLength = NumberUtils.byteArrayToInt(bytes, currentPosition);
				currentPosition += NumberUtils.SIZE_OF_INT_IN_BYTES;
				final byte[] encryptedKeyData = Arrays.copyOfRange(bytes, currentPosition,
						currentPosition + encryptedKeyLength);
				currentPosition += encryptedKeyLength;
				rsaEncryptedData.add(new RSAEncryptedData(encryptedKeyData, encryptedKeyHash));
				recipientsBuilder.write(encryptedKeyHash);
				break;

			case AES_ENCRYPTED_DATA:
				final byte[] iv = Arrays.copyOfRange(bytes, currentPosition, currentPosition + AES.IV_LENGTH);
				currentPosition += AES.IV_LENGTH;
				final int encryptedDataLenght = NumberUtils.byteArrayToInt(bytes, currentPosition);
				currentPosition += NumberUtils.SIZE_OF_INT_IN_BYTES;
				final byte[] encryptedData = Arrays.copyOfRange(bytes, currentPosition,
						currentPosition + encryptedDataLenght);
				currentPosition += encryptedDataLenght;
				aesEncryptedData = new AESEncryptedData(iv, encryptedData);
				break;

			default:
				throw new DecryptionException();
			}
		}
		this.recipients = recipientsBuilder.toByteArray();
	}

	public boolean decrypt() {
		for (final RSAEncryptedData rsaData : rsaEncryptedData) {
			final RSAKeyPair rsaKey = KeyStore.getPrivate(rsaData.getKeyHash());
			if (rsaKey != null) {
				try {
					final byte[] aesKey = rsaKey.decrypt(rsaData);
					final AES aes = new AES(aesKey);
					decryptedData = aes.decrypt(aesEncryptedData);
					final byte[] aesDecryptedData = aes.decrypt(aesEncryptedData);
					signature = Arrays.copyOfRange(aesDecryptedData, 0, AbstractRSAKey.SIGN_LENGTH);
					decryptedData = Arrays.copyOfRange(aesDecryptedData, AbstractRSAKey.SIGN_LENGTH,
							decryptedData.length);
					return true;
				} catch (RSAException | AESException ex) {
					LOGGER.log(Level.SEVERE, null, ex);
				}
			}
		}
		return false;
	}

	public boolean verify() {
		final IEncrypter sender = KeyStore.getPublic(Base64Util.encodeToString(senderKeyHash));
		if (sender == null) {
			return false;
		}
		try {
			return sender.verifySignature(signature, decryptedData, recipients);
		} catch (final RSAException ex) {
			LOGGER.log(Level.SEVERE, null, ex);
			return false;
		}

	}

	public byte[] getDecryptedData() {
		return decryptedData;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Encrypted for:\n");
		rsaEncryptedData.forEach(rsaData -> {
			final IEncrypter rsa = KeyStore.getPublic(rsaData.getKeyHash());
			if (rsa == null) {
				builder.append(rsaData.getKeyHash().substring(0, 8));
			} else {
				builder.append(rsa.toString());
			}
			builder.append("\n");
		});
		final IEncrypter sender = KeyStore.getPublic(Base64Util.encodeToString(senderKeyHash));
		builder.append("\nSigned by:\n");
		if (sender == null) {
			builder.append(Base64Util.encodeToString(senderKeyHash).substring(0, 8));
		} else {
			builder.append(sender.toString());
		}
		return builder.toString();
	}
}
