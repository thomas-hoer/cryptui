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

import de.cryptui.ui.CryptUI;

import java.security.Security;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class CryptUiMain {

	private static final Logger LOGGER = Logger.getLogger(CryptUiMain.class.getName());

	/**
	 * Main to start the Program.
	 *
	 * @param args the command line arguments
	 */
	public static void main(final String[] args) {
		Security.addProvider(new BouncyCastleProvider());
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			LOGGER.log(Level.WARNING, "Error occured while setting Look and Feel to SystemLookAndFeelClassName", e);
		}
		java.awt.EventQueue.invokeLater(() -> new CryptUI().setVisible(true));

	}

}
