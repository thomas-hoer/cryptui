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

import java.util.HashMap;
import java.util.Map;

public enum DataType {
	PRIVATE_KEY(1), PUBLIC_KEY(2), OBJECT_NAME(3), DESCRIPTION_SHORT(4), AES_ENCRYPTED_DATA(5), RSA_ENCRYPTED_DATA(6),
	AES_KEY(7), GZIP(8), SENDER_HASH(9);

	private static final Map<Integer, DataType> numberToType;

	static {
		numberToType = new HashMap<>();
		for (final DataType dataType : DataType.values()) {
			numberToType.put((int) dataType.getNumber(), dataType);
		}
	}
	private final byte number;

	DataType(final int number) {
		this.number = (byte) number;
	}

	public byte getNumber() {
		return number;
	}

	public static DataType fromByte(final int type) {
		return numberToType.get(type);
	}

}
