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

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import de.cryptui.ui.CryptUI;

public class CryptUiMain {

	/**
	 * @param args the command line arguments
	 * @throws java.security.GeneralSecurityException
	 * @throws java.io.UnsupportedEncodingException
	 */
	public static void main(final String[] args) {
		Security.addProvider(new BouncyCastleProvider());
		java.awt.EventQueue.invokeLater(() -> new CryptUI().setVisible(true));

	}

}