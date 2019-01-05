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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public final class NumberUtils {

	public static final int SIZE_OF_INT_IN_BYTES = 4;

	private NumberUtils() {
	}

	public static byte[] intToByteArray(final int value) {
		return ByteBuffer.allocate(SIZE_OF_INT_IN_BYTES).putInt(value).array();
	}

	public static int byteArrayToInt(final byte[] array) {
		return ByteBuffer.wrap(array).getInt();
	}

	public static int byteArrayToInt(final byte[] array, final int offset) {
		return ByteBuffer.wrap(array, offset, SIZE_OF_INT_IN_BYTES).getInt();
	}

	public static int intFromInputStream(final InputStream inputStream) throws IOException {
		final byte[] bytes = new byte[SIZE_OF_INT_IN_BYTES];
		final int numBytes = inputStream.read(bytes);
		if (numBytes == SIZE_OF_INT_IN_BYTES) {
			return byteArrayToInt(bytes);
		} else {
			throw new IOException("Not able to read 4 bytes in File stream");
		}
	}
}
