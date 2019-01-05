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
import de.cryptui.crypto.hash.SHA3Hash;
import de.cryptui.util.NumberUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import org.apache.commons.lang3.StringUtils;

public class RSAKeyPair extends AbstractRSAKey implements IEncrypter {

	private static final long serialVersionUID = -5002230889389879796L;
	private static final Logger LOGGER = Logger.getLogger(RSAKeyPair.class.getName());

	private final String name;
	private final String comment;
	private final PrivateKey privateKey;
	private final PublicKey publicKey;
	private final byte[] salt;

	public RSAKeyPair(final String name, final String comment) throws RSAException {
		this.comment = comment;
		final KeyPair keyPair = generateKeyPair();
		this.privateKey = keyPair.getPrivate();
		this.publicKey = keyPair.getPublic();
		this.salt = generateSalt();
		this.name = generateName(name);
	}

	public RSAKeyPair(final String name, final String comment, final PrivateKey privateKey, final PublicKey publicKey,
			final byte[] salt) {
		this.comment = comment;
		this.privateKey = privateKey;
		this.publicKey = publicKey;
		this.salt = salt;
		this.name = name;
	}

	public RSAPublicKey getPublicKey() {
		return new RSAPublicKey(publicKey, name, salt);
	}

	@Override
	public void saveKeyInFile(final File file) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(DataType.OBJECT_NAME.getNumber());
			final byte[] nameBytes = name.getBytes("UTF-8");
			if (nameBytes.length < 128) {
				fos.write(nameBytes.length);
				fos.write(nameBytes);
			} else {
				fos.write(127);
				fos.write(nameBytes, 0, 127);
			}

			fos.write(DataType.DESCRIPTION_SHORT.getNumber());
			final byte[] commentBytes = comment.getBytes("UTF-8");
			if (commentBytes.length < 128) {
				fos.write(commentBytes.length);
				fos.write(commentBytes);
			} else {
				fos.write(127);
				fos.write(commentBytes, 0, 127);
			}

			final byte[] privateKeyEncoded = privateKey.getEncoded();
			fos.write(DataType.PRIVATE_KEY.getNumber());
			fos.write(NumberUtils.intToByteArray(privateKeyEncoded.length));
			fos.write(privateKeyEncoded);
			final byte[] publicKeyEncoded = publicKey.getEncoded();
			fos.write(DataType.PUBLIC_KEY.getNumber());
			fos.write(NumberUtils.intToByteArray(publicKeyEncoded.length));
			fos.write(publicKeyEncoded);
			fos.write(salt);
		}
	}

	@Override
	public RSAEncryptedData encrypt(final byte[] data) throws RSAException {
		try {
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			return new RSAEncryptedData(cipher.doFinal(data), getHash());
		} catch (IllegalBlockSizeException | InvalidKeyException | BadPaddingException ex) {
			throw new RSAException(ex);
		}
	}

	public byte[] decrypt(final RSAEncryptedData data) throws RSAException {
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

	@Override
	public boolean verifySignature(final byte[] sign, final byte[] dat, final byte[] recipient) throws RSAException {
		return verifySignature(publicKey, sign, dat, recipient);
	}

	public byte[] createSignature(final byte[] dat, final byte[] recipient) throws RSAException {
		try {
			final Signature signature = Signature.getInstance(AbstractRSAKey.SIGNATURE_ALGORITHM);
			signature.initSign(privateKey);
			signature.update(dat);
			signature.update(recipient);
			return signature.sign();
		} catch (SignatureException | InvalidKeyException | NoSuchAlgorithmException ex) {
			LOGGER.log(Level.SEVERE, null, ex);
			throw new RSAException(ex);
		}

	}
}
