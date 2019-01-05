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

import de.cryptui.DataType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

public final class GZip {

	private static final int PERCENT_MULTIPLICATOR = 100;
	private static final Logger LOGGER = Logger.getLogger(GZip.class.getName());

	private GZip() {
	}

	public static byte[] compress(final byte[] input) {
		final ByteArrayOutputStream obj = new ByteArrayOutputStream();
		obj.write(DataType.GZIP.getNumber());
		try (GZIPOutputStream gzip = new GZIPOutputStream(obj)) {
			gzip.write(input);
		} catch (final IOException ex) {
			LOGGER.log(Level.SEVERE, null, ex);
		}
		final byte[] compressed = obj.toByteArray();
		LOGGER.log(Level.INFO, "Compressed from {0} to {1} ({2}%)", new Object[] { input.length, compressed.length,
				compressed.length * PERCENT_MULTIPLICATOR / input.length });
		if (compressed.length < input.length) {
			return compressed;
		}
		return input;
	}

}
