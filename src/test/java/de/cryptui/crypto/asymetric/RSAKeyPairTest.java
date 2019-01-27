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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import de.cryptui.crypto.container.RSAEncryptedData;
import de.cryptui.util.Base64Util;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.Test;

public class RSAKeyPairTest {

	private static RSAKeyPair rsaKeyPair;
	public static final String TEST = "Teststring";
	private static final byte[] TEST_DATA = "Teststring".getBytes();
	private static final byte[] USER_DATA = "Recipient".getBytes();

	private static final String PREPARED_CIPHER_TEXT = "fQuMyf4Zvb/tWnX9Bh28xkusPiP0ni7LTR3O84OPeCuR2I6vIu/CxbnEdWlbE8e4poAG5ZeX7x/UKEFDYOASuQ4L4kBSZilIcjIGAZGIj8Y90vmLC0oqNQR6Yxeplz58DGK0cepiRgp5k2c0rwCPM8YfyTXVTXW/+X4/jTr7M7xCsa+6C4eJw/lrl9T5ArovbUTz68WGaiLfMbACFSDidojK2mN+3jhe7WDqyWpjd+UeXlM5+ctRSCLs2qm14dnSQLag/5wU3oxvvfVTqYBthrwCnAp9imngr+NWoKyn6jWpA5K08l0nKFt+xbJIyeCuC5/soAYNRAtVGpWxfn11FT2qFI53UNaHrOfgozWNn5AyOVm6mCuy5frgMuslyQvq93eindsyfCIsOLIVJpsoBTHN+cCM1BsV1MqCI9kwD7NME+Jt4ozH/uc1OdU0qyfT3JI8Pv+bASaNcKZ45X1nsdK0qYjc7e9YSbmiC2ybBg4T/yg+mgVjzDA3mFI8W+d/ddOrOX42gQmvQjNchYk/jHfTm4Q8pTVv1bnNLRLUiPLmcPRGz/LvKC5r0BvH0n4flOeQXKPEmQV7fOQkor49RtrXZr3FHFUPNCibPkSW6DrqCOxVsqowAczBErYKTB+0hVh3Qx6Dk9TmpZZbtxuh4ZbAeYWZer3UV1VNNhRqhHA=";
	private static final String PREPARED_PLAIN_TEXT = "This is a Test!";

	@BeforeClass
	public static void setUpClass() throws RSAException {
		Security.addProvider(new BouncyCastleProvider());
		// Use a pre generated Key, since the generation consumes a lot of time
		rsaKeyPair = (RSAKeyPair) RSAKeyPair.fromStream(RSAKeyPairTest.class.getResourceAsStream("/rsa/keyPair"));
	}

	@Test
	public void testRSAEncryption() throws GeneralSecurityException, RSAException, IOException {
		final RSAEncryptedData data1 = rsaKeyPair.encrypt(TEST.getBytes());
		final byte[] data2 = rsaKeyPair.decrypt(data1);
		assertEquals(TEST, new String(data2));

	}

	@Test
	public void testSignAndVerify() throws Exception {
		final byte[] sign = rsaKeyPair.createSignature(TEST_DATA, USER_DATA);
		final boolean verify = rsaKeyPair.verifySignature(sign, TEST_DATA, USER_DATA);
		assertTrue(verify);
	}

	@Test
	public void testSignAndVerifyFail1() throws Exception {
		final byte[] sign = rsaKeyPair.createSignature(USER_DATA, USER_DATA);
		final boolean verify = rsaKeyPair.verifySignature(sign, TEST_DATA, USER_DATA);
		assertFalse(verify);
	}

	@Test
	public void testSignAndVerifyFail2() throws Exception {
		final byte[] sign = rsaKeyPair.createSignature(TEST_DATA, TEST_DATA);
		final boolean verify = rsaKeyPair.verifySignature(sign, TEST_DATA, USER_DATA);
		assertFalse(verify);
	}

	@Test
	public void testSignAndVerifyFail3() throws Exception {
		final byte[] sign = rsaKeyPair.createSignature(USER_DATA, TEST_DATA);
		final boolean verify = rsaKeyPair.verifySignature(sign, TEST_DATA, USER_DATA);
		assertFalse(verify);
	}

	@Test
	public void testDecrypt() throws RSAException {
		final byte[] cipherText = Base64Util.decode(PREPARED_CIPHER_TEXT);
		final RSAEncryptedData encryptedData = new RSAEncryptedData(cipherText, rsaKeyPair.getHash());
		final byte[] plainText = rsaKeyPair.decrypt(encryptedData);

		assertArrayEquals(PREPARED_PLAIN_TEXT.getBytes(), plainText);
	}

	@Test
	public void testEncryptionMaximum() throws RSAException {
		final byte[] plainText = new byte[382];
		final RSAEncryptedData encryptedData = rsaKeyPair.encrypt(plainText);
		assertNotNull(encryptedData);
	}

	@Test(expected = RSAException.class)
	public void testEncryptionMaximumFail() throws RSAException {
		final byte[] plainText = new byte[383];
		rsaKeyPair.encrypt(plainText);
	}

	@Test
	public void testPublicKeyCreation() throws RSAException {
		final RSAEncryptedData keyPairCipherText = rsaKeyPair.encrypt(TEST_DATA);

		final RSAPublicKey publicKey = rsaKeyPair.getPublicKey();

		final RSAEncryptedData publicKeyCipherText = publicKey.encrypt(TEST_DATA);

		assertEquals(keyPairCipherText.getKeyHash(), publicKeyCipherText.getKeyHash());
		final byte[] keyPairDecrypted = rsaKeyPair.decrypt(keyPairCipherText);
		final byte[] publKeyDecrypted = rsaKeyPair.decrypt(publicKeyCipherText);
		assertArrayEquals(keyPairDecrypted, publKeyDecrypted);
	}
}
