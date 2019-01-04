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
package de.cryptui.crypto.container;

import de.cryptui.DataType;
import de.cryptui.crypto.asymetric.RSABase;
import static de.cryptui.util.Assert.assertTrue;
import de.cryptui.util.Base64Util;
import de.cryptui.util.NumberUtils;
import java.io.IOException;
import java.io.OutputStream;

public class RSAEncryptedData {

    private final byte[] encryptedData;
    private final byte[] keyHash;

    public RSAEncryptedData(byte[] encryptedData, byte[] keyHash) {
        assertTrue(encryptedData.length == RSABase.KEY_LENGHT_BYTES);
        this.encryptedData = encryptedData;
        this.keyHash = keyHash;
    }

    public byte[] getEncryptedData() {
        return encryptedData;
    }

    public String getKeyHash() {
        return Base64Util.encodeToString(keyHash);
    }

    public void writeToOutputStream(OutputStream fos) throws IOException {
        fos.write(DataType.RSA_ENCRYPTED_DATA.getNumber());
        fos.write(keyHash);
        fos.write(NumberUtils.intToByteArray(encryptedData.length));
        fos.write(encryptedData);
    }

}
