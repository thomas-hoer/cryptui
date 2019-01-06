package de.cryptui.crypto.symetric;

import static org.junit.Assert.assertEquals;

import de.cryptui.crypto.container.AESEncryptedData;
import de.cryptui.util.Base64Util;

import java.lang.reflect.Field;
import java.security.Security;
import java.util.Random;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.test.FixedSecureRandom;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class AESTest {

	private static final String ENCRYPTED_TEST_STRING = "lJIxYtIlWSAvLJKR5j1iGSQAlCkFKgg=";
	private static final byte[] FIXED_IV = new byte[] { 118, -6, 34, -6, 109, -23, 7, 39, -25, 4, -31, 99 };
	private static final byte[] FIXED_KEY = new byte[] { 120, 96, -32, -30, 45, 48, 117, 86, -102, -54, 35, -13, -76,
			-18, 66, 37 };
	private static final String TEST = "AESTest";

	@BeforeClass
	public static void init() {
		Security.addProvider(new BouncyCastleProvider());
	}

	private static void resetRNG(final AES aes)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		final Field secureRandomField = AES.class.getDeclaredField("secureRandom");
		secureRandomField.setAccessible(true);
		secureRandomField.set(aes, new FixedSecureRandom(FIXED_IV));
	}

	@Test
	public void testAESEncryption() throws AESException {
		final AES aes = new AES();
		final AESEncryptedData encryptedData = aes.encrypt(TEST.getBytes());
		final byte[] data = aes.decrypt(encryptedData);
		assertEquals(TEST, new String(data));
	}

	@Test
	public void testEncrypt() throws AESException, NoSuchFieldException, SecurityException, IllegalArgumentException,
			IllegalAccessException {
		final AES aes = new AES(FIXED_KEY);
		resetRNG(aes);
		final AESEncryptedData encryptedData = aes.encrypt(TEST.getBytes());

		final byte[] expected = Base64Util.decode(ENCRYPTED_TEST_STRING);
		Assert.assertArrayEquals(FIXED_IV, encryptedData.getIv());
		Assert.assertArrayEquals(expected, encryptedData.getData());
	}

	@Test
	public void testDecrypt() throws AESException {
		final AES aes = new AES(FIXED_KEY);
		final byte[] cipherText = Base64Util.decode(ENCRYPTED_TEST_STRING);

		final byte[] plainText = aes.decrypt(new AESEncryptedData(FIXED_IV, cipherText));

		Assert.assertArrayEquals(TEST.getBytes(), plainText);

	}

	/**
	 * Verify that for every encryption a new IV is chosen.
	 *
	 * @throws AESException
	 */
	@Test
	public void testIV() throws AESException {
		final AES aes = new AES();

		final byte[] plainData = new byte[100];
		(new Random()).nextBytes(plainData);

		final AESEncryptedData encryptedData1 = aes.encrypt(plainData);
		final AESEncryptedData encryptedData2 = aes.encrypt(plainData);

		Assert.assertFalse(Arrays.areEqual(encryptedData1.getIv(), encryptedData2.getIv()));
		Assert.assertFalse(Arrays.areEqual(encryptedData1.getData(), encryptedData2.getData()));

		final byte[] plainData1 = aes.decrypt(encryptedData1);
		final byte[] plainData2 = aes.decrypt(encryptedData2);

		Assert.assertArrayEquals(plainData, plainData1);
		Assert.assertArrayEquals(plainData, plainData2);
	}

	/**
	 * Check that the AES can not be changed by incident from the outside code.
	 *
	 * @throws AESException
	 */
	@Test
	public void testImmutableGeneratedKey() throws AESException {
		// For testing "normal" Random is okay
		final Random random = new Random();
		final AES aes = new AES();
		final byte[] aesKey = aes.getKey();
		final byte[] originalKey = Arrays.clone(aesKey);
		random.nextBytes(aesKey);

		Assert.assertFalse(Arrays.areEqual(aesKey, originalKey));
		final AESEncryptedData encryptedData = aes.encrypt(TEST.getBytes());

		final AES aesDecrypter = new AES(originalKey);
		final byte[] decrypted = aesDecrypter.decrypt(encryptedData);

		Assert.assertArrayEquals(TEST.getBytes(), decrypted);
		Assert.assertArrayEquals(originalKey, aes.getKey());
	}

	/**
	 * Check that the AES can not be changed by incident from the outside code.
	 *
	 * @throws AESException
	 */
	@Test
	public void testImmutablePredefinedKey() throws AESException {
		// For testing "normal" Random is okay
		final Random random = new Random();
		final byte[] originalKey = new byte[16];
		random.nextBytes(originalKey);
		final byte[] aesKey = Arrays.clone(originalKey);
		final AES aes = new AES(aesKey);
		random.nextBytes(aesKey);

		Assert.assertFalse(Arrays.areEqual(aesKey, originalKey));
		final AESEncryptedData encryptedData = aes.encrypt(TEST.getBytes());

		final AES aesDecrypter = new AES(originalKey);
		final byte[] decrypted = aesDecrypter.decrypt(encryptedData);

		Assert.assertArrayEquals(TEST.getBytes(), decrypted);
		Assert.assertArrayEquals(originalKey, aes.getKey());
	}
}
