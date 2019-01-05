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
package de.cryptui;

import static org.junit.Assert.assertEquals;

import java.security.GeneralSecurityException;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.Test;

import de.cryptui.crypto.asymetric.RSAException;
import de.cryptui.crypto.asymetric.RSAKeyPair;
import de.cryptui.crypto.container.AESEncryptedData;
import de.cryptui.crypto.container.RSAEncryptedData;
import de.cryptui.crypto.symetric.AES;

public class CryptUITest {

	public static final String TEST = "Teststring";

	@BeforeClass
	public static void setUpClass() {
		Security.addProvider(new BouncyCastleProvider());
	}

	@Test
	public void testRSAEncryption() throws GeneralSecurityException, RSAException {
		final RSAKeyPair rsa = new RSAKeyPair("Test", "Test");

		final RSAEncryptedData data1 = rsa.encrypt(TEST.getBytes());
		final byte[] data2 = rsa.decrypt(data1);
		assertEquals(TEST, new String(data2));

	}

	@Test
	public void testAESEncryption() throws Exception {
		final AES aes = new AES();
		final AESEncryptedData encryptedData = aes.encrypt(TEST.getBytes());
		final byte[] data = aes.decrypt(encryptedData);
		assertEquals(TEST, new String(data));
	}

}
