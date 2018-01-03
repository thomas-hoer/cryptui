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

import java.util.Base64;

public class Base64Util {

    public static byte[] encode(byte[] src) {
        return Base64.getEncoder().encode(src);
    }
    
    public static String encodeToString(byte[] src) {
        return Base64.getEncoder().encodeToString(src);
    }

    public static byte[] decode(byte[] src) {
        return Base64.getDecoder().decode(src);
    }
}