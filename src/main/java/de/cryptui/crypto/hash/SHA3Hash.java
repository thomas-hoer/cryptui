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
package de.cryptui.crypto.hash;

import org.bouncycastle.jcajce.provider.digest.SHA3;

public class SHA3Hash {

	private SHA3Hash() {
	}

	public static final int HASH_SIZE = 64;

	public static byte[] hash(final byte[] input, final byte[] salt) {
		final SHA3.DigestSHA3 digestSHA3 = new SHA3.Digest512();
		digestSHA3.update(input);
		return digestSHA3.digest(salt);
	}

}
