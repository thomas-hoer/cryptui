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
package cryptui.crypto.asymetric;

import cryptui.crypto.hash.SHA3Hash;
import java.security.PublicKey;

public class RSAPublicKey extends RSABase {

    private final PublicKey publicKey;
    private final byte[] salt;

    public RSAPublicKey(PublicKey publicKey, byte[] salt) {
        this.publicKey = publicKey;
        this.salt = salt;
    }

    public RSAEncryptedData encrypt(byte[] data) throws RSAException {
        return encrypt(publicKey, data);
    }

    @Override
    public byte[] getHash() {
        return SHA3Hash.hash(publicKey.getEncoded(), salt);
    }

}
