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
package de.cryptui.crypto.asymetric;

import de.cryptui.crypto.asymetric.RSAException;
import de.cryptui.crypto.asymetric.RSAKeyPair;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

public class RSAKeyPairTest {

    private static RSAKeyPair rsaKeyPair;
    private static final byte[] TEST_DATA = "Teststring".getBytes();
    private static final byte[] USER_DATA = "Recipient".getBytes();

    @BeforeClass
    public static void setUpClass() throws RSAException {
        Security.addProvider(new BouncyCastleProvider());
        rsaKeyPair = new RSAKeyPair("Test", "Test");
    }

    @Test
    public void testSignAndVerify() throws Exception {
        byte[] sign = rsaKeyPair.createSignature(TEST_DATA, USER_DATA);
        boolean verify = rsaKeyPair.verifySignature(sign, TEST_DATA, USER_DATA);
        assertTrue(verify);
    }

    @Test
    public void testSignAndVerifyFail1() throws Exception {
        byte[] sign = rsaKeyPair.createSignature(USER_DATA, USER_DATA);
        boolean verify = rsaKeyPair.verifySignature(sign, TEST_DATA, USER_DATA);
        assertFalse(verify);
    }

    @Test
    public void testSignAndVerifyFail2() throws Exception {
        byte[] sign = rsaKeyPair.createSignature(TEST_DATA, TEST_DATA);
        boolean verify = rsaKeyPair.verifySignature(sign, TEST_DATA, USER_DATA);
        assertFalse(verify);
    }

    @Test
    public void testSignAndVerifyFail3() throws Exception {
        byte[] sign = rsaKeyPair.createSignature(USER_DATA, TEST_DATA);
        boolean verify = rsaKeyPair.verifySignature(sign, TEST_DATA, USER_DATA);
        assertFalse(verify);
    }

}
