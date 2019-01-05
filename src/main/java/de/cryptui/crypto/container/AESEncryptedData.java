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

import static de.cryptui.util.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStream;

import de.cryptui.DataType;
import de.cryptui.crypto.symetric.AES;
import de.cryptui.util.NumberUtils;

public class AESEncryptedData {

	private final byte[] iv;
	private final byte[] data;

	public AESEncryptedData(final byte[] iv, final byte[] data) {
		assertTrue(iv.length == AES.IV_LENGTH);
		this.iv = iv;
		this.data = data;
	}

	public byte[] getIv() {
		return iv;
	}

	public byte[] getData() {
		return data;
	}

	public void writeToOutputStream(final OutputStream fos) throws IOException {
		fos.write(DataType.AES_ENCRYPTED_DATA.getNumber());
		fos.write(iv);
		fos.write(NumberUtils.intToByteArray(data.length));
		fos.write(data);
	}

}