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

import static de.cryptui.util.Assert.assertEqual;

import de.cryptui.DataType;
import de.cryptui.crypto.asymetric.AbstractRSAKey;
import de.cryptui.util.Base64Util;
import de.cryptui.util.NumberUtils;

import java.io.IOException;
import java.io.OutputStream;

import org.bouncycastle.util.Arrays;

public final class RSAEncryptedData {

	private final byte[] encryptedData;
	private final byte[] keyHash;

	/**
	 * Creates a wrapper for RSA encrypted data. At the moment only RSA with 4098
	 * bit (512 byte) is supported.
	 *
	 * @param encryptedData RSA encrypted data
	 * @param keyHash       Hash reference to the public key the data is encrypted
	 *                      with
	 */
	public RSAEncryptedData(final byte[] encryptedData, final byte[] keyHash) {
		assertEqual(AbstractRSAKey.KEY_LENGHT_BYTES, encryptedData.length,
				"RSA encrypted data requires to have lenght of 512 bytes");
		this.encryptedData = Arrays.clone(encryptedData);
		this.keyHash = Arrays.clone(keyHash);
	}

	public byte[] getEncryptedData() {
		return Arrays.clone(encryptedData);
	}

	public String getKeyHash() {
		return Base64Util.encodeToString(keyHash);
	}

	/**
	 * Write encrypted data and reference hash to OutputStream.
	 *
	 * @param outputStream Stream on which the data gets written.
	 * @throws IOException if an I/O error occurs. In particular,an IOException may
	 *                     be thrown if the output stream has been closed.
	 */
	public void writeToOutputStream(final OutputStream outputStream) throws IOException {
		outputStream.write(DataType.RSA_ENCRYPTED_DATA.getNumber());
		outputStream.write(keyHash);
		outputStream.write(NumberUtils.intToByteArray(encryptedData.length));
		outputStream.write(encryptedData);
	}

}
