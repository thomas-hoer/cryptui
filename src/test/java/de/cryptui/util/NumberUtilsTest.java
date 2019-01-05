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
package de.cryptui.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NumberUtilsTest {

	@Test
	public void testIntToByteArray() {
		final int number = 123456789;
		final byte[] bytes = NumberUtils.intToByteArray(number);
		final int encodedNumber = NumberUtils.byteArrayToInt(bytes);
		assertEquals(number, encodedNumber);
	}

	@Test
	public void testIntToByteArray2() {
		final byte[] bytes = NumberUtils.intToByteArray(0xFFF9F3F0);
		assertEquals((byte) 0xFF, bytes[0]);
		assertEquals((byte) 0xF9, bytes[1]);
		assertEquals((byte) 0xF3, bytes[2]);
		assertEquals((byte) 0xF0, bytes[3]);
	}

	@Test
	public void testByteArrayToInt() {
		final byte[] bytes = { (byte) 0x45, (byte) 0xF1, (byte) 0x44, (byte) 0xFF };
		final int number = NumberUtils.byteArrayToInt(bytes);
		assertEquals(0x45F144FF, number);
	}
}
