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
package de.cryptui.ui.list;

import de.cryptui.crypto.asymetric.RSAKeyPair;

import java.awt.Component;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JList;

import org.apache.commons.io.IOUtils;

public class KeyListRenderer extends DefaultListCellRenderer {

	private static final Logger LOGGER = Logger.getLogger(KeyListRenderer.class.getName());
	private static final long serialVersionUID = 3826152589439909614L;
	private ImageIcon keyPairIcon;
	private ImageIcon publicKeyIcon;

	public KeyListRenderer() {
		try (InputStream in = KeyListRenderer.class.getResourceAsStream("/de/cryptui/ui/list/key_pair.png")) {
			final byte[] b = IOUtils.toByteArray(in);
			keyPairIcon = new ImageIcon(b);
		} catch (final IOException ex) {
			LOGGER.log(Level.SEVERE, null, ex);
		}
		try (InputStream in = KeyListRenderer.class.getResourceAsStream("/de/cryptui/ui/list/public_key.png")) {
			final byte[] b = IOUtils.toByteArray(in);
			publicKeyIcon = new ImageIcon(b);
		} catch (final IOException ex) {
			LOGGER.log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index,
			final boolean selected, final boolean expanded) {
		super.getListCellRendererComponent(list, value, index, selected, expanded);

		if (value instanceof RSAKeyPair) {
			setIcon(keyPairIcon);
		} else {
			setIcon(publicKeyIcon);
		}

		return this;
	}
}
