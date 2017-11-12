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
package cryptui.crypto.hash;

import static java.security.CryptoPrimitive.MAC;
import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.bouncycastle.util.encoders.Hex;

/**
 *
 * @author Ich
 */
public class SHA3Hash {

    public static byte[] hash(byte[] input) {
        
        //MAC.
        SHA3.DigestSHA3 digestSHA3 = new SHA3.Digest512();
        byte[] digest = digestSHA3.digest(input);
        System.out.println("SHA3-512 = " + Hex.toHexString(digest));
        return digest;
    }
    public static void main(String[] args) throws Exception {
        byte[] hash = hash("test".getBytes());
        System.out.println(hash.length);
    }
}
