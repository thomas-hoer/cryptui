/*
 * Copyright 2017 thomas-hoer.
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

public class NumberUtils {

    public static final byte[] intToByteArray(int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }

    public static final int byteArrayToInt(byte[] array) {
        return ByteBuffer.wrap(array).getInt();
    }

    public static final int byteArrayToInt(byte[] array, int offset) {
        return ByteBuffer.wrap(array, offset, 4).getInt();
    }

    public static final int intFromInputStream(InputStream is) throws IOException {
        byte[] bytes = new byte[4];
        is.read(bytes);
        return byteArrayToInt(bytes);
    }
}