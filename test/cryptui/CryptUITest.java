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
package cryptui;

import cryptui.crypto.asymetric.RSAEncryptedData;
import cryptui.crypto.asymetric.RSAException;
import cryptui.crypto.asymetric.RSAKeyPair;
import cryptui.crypto.symetric.AES;
import cryptui.crypto.symetric.AESEncryptedData;
import java.security.GeneralSecurityException;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

public class CryptUITest {

    public static final String TEST = "Teststring";

    @BeforeClass
    public static void setUpClass() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    public void encryptRSATest() throws GeneralSecurityException, RSAException {
        RSAKeyPair rsa = new RSAKeyPair("Test", "Test");

        RSAEncryptedData data1 = rsa.encrypt(TEST.getBytes());
        byte[] data2 = rsa.decrypt(data1);
        assertEquals(TEST, new String(data2));

    }

    @Test
    public void AESTest() throws Exception {
        AES aes = new AES();
        AESEncryptedData encryptedData = aes.encrypt(TEST.getBytes());
        byte[] data = aes.decrypt(encryptedData);
        assertEquals(TEST, new String(data));
    }
}
