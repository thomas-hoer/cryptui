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
import java.io.OutputStream;
import java.security.PublicKey;

/**
 * Represents a RSA public Key. This is usually a key from a different person.
 * It is used to encrypt messages and verify integrity of a received message.
 */
public class RSAPublicKey extends AbstractRSAKey implements IEncrypter {

	private static final long serialVersionUID = -8946272597440918123L;

	private final PublicKey publicKey;
	private final byte[] salt;
	private final String name;

	/**
	 * Load an existing public RSA Key. For creating use AbstractRSAKey.fromFile().
	 *
	 * @param publicKey
	 * @param name
	 * @param salt
	 */
	RSAPublicKey(final PublicKey publicKey, final String name, final byte[] salt) {
		this.publicKey = publicKey;
		this.salt = salt;
		this.name = generateName(name);
	}

	@Override
	public void saveKeyInFile(final File file) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(file)) {
			saveKeyInStream(fos);
		}
	}

	@Override
	public void saveKeyInStream(final OutputStream outputStream) throws IOException {
		writeObjectName(outputStream, name);

		final byte[] publicKeyEncoded = publicKey.getEncoded();
		outputStream.write(DataType.PUBLIC_KEY.getNumber());
		outputStream.write(NumberUtils.intToByteArray(publicKeyEncoded.length));
		outputStream.write(publicKeyEncoded);
		outputStream.write(salt);
	}

	@Override
	public RSAEncryptedData encrypt(final byte[] data) throws RSAException {
		return encrypt(publicKey, data);
	}

	@Override
	public boolean verifySignature(final byte[] sign, final byte[] dat, final byte[] recipient) throws RSAException {
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
