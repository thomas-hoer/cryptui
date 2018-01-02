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
package de.cryptui.crypto;

import de.cryptui.crypto.asymetric.IEncrypter;
import de.cryptui.crypto.asymetric.RSAKeyPair;
import de.cryptui.util.Base64Util;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultListModel;

/**
 *
 * @author Ich
 */
public class KeyStore {

    private static final Map<String, RSAKeyPair> KEY_MAP = new HashMap<>();
    private static final Map<String, IEncrypter> PUBLIC_KEY_MAP = new HashMap<>();

    private static final DefaultListModel<IEncrypter> publicKeyListModel = new DefaultListModel<>();
    private static final DefaultListModel<RSAKeyPair> privateKeyListModel = new DefaultListModel<>();

    public static void addPublic(IEncrypter publicKey) {
        PUBLIC_KEY_MAP.put(Base64Util.encodeToString(publicKey.getHash()), publicKey);
        publicKeyListModel.addElement(publicKey);
    }

    public static void addPrivate(RSAKeyPair keyPair) {
        final String keyHash = Base64Util.encodeToString(keyPair.getHash());
        KEY_MAP.put(keyHash, keyPair);
        privateKeyListModel.addElement(keyPair);
        PUBLIC_KEY_MAP.put(keyHash, keyPair);
        publicKeyListModel.addElement(keyPair);
    }

    public static RSAKeyPair getPrivate(String keyHash) {
        return KEY_MAP.get(keyHash);
    }

    public static IEncrypter getPublic(String keyHash) {
        return PUBLIC_KEY_MAP.get(keyHash);
    }

    public static DefaultListModel<IEncrypter> getPublicKeyListModel() {
        return publicKeyListModel;
    }

    public static DefaultListModel<RSAKeyPair> getPrivateKeyListModel() {
        return privateKeyListModel;
    }
}
