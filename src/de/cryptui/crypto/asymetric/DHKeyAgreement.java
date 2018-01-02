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

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;
import javax.crypto.KeyAgreement;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 *
 * @author thomas-hoer
 */
public class DHKeyAgreement {

    public static void main(String[] args) throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH");

        keyGen.initialize(2048, new SecureRandom());
        KeyPair aPair = keyGen.generateKeyPair();
        KeyPair bPair = keyGen.generateKeyPair();

        KeyAgreement keyAgree = KeyAgreement.getInstance("DH");

        keyAgree.init(aPair.getPrivate());
        keyAgree.doPhase(bPair.getPublic(), true);

        final byte[] generateSecret = keyAgree.generateSecret();
        System.out.println(generateSecret.length);
        System.out.println(new String(generateSecret));

        keyAgree.init(bPair.getPrivate());
        keyAgree.doPhase(aPair.getPublic(), true);

        System.out.println(new String(generateSecret));
    }

}