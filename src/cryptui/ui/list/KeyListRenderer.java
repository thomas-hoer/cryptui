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
package cryptui.ui.list;

import cryptui.crypto.asymetric.RSAKeyPair;
import java.awt.Component;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import org.apache.commons.io.IOUtils;

public class KeyListRenderer extends DefaultListCellRenderer {

    private ImageIcon keyPairIcon;
    private ImageIcon publicKeyIcon;
    private final JLabel label;

    public KeyListRenderer() {
        label = new JLabel();
        label.setOpaque(true);
        try (InputStream in = KeyListRenderer.class.getResourceAsStream("/cryptui/ui/list/key_pair.png")) {
            byte[] b = IOUtils.toByteArray(in);
            keyPairIcon = new ImageIcon(b);
        } catch (IOException ex) {
            Logger.getLogger(KeyListRenderer.class.getName()).log(Level.SEVERE, null, ex);
        }
        try (InputStream in = KeyListRenderer.class.getResourceAsStream("/cryptui/ui/list/public_key.png")) {
            byte[] b = IOUtils.toByteArray(in);
            publicKeyIcon = new ImageIcon(b);
        } catch (IOException ex) {
            Logger.getLogger(KeyListRenderer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean selected, boolean expanded) {
        super.getListCellRendererComponent(list, value, index, selected, expanded);

        if (value instanceof RSAKeyPair) {
            setIcon(keyPairIcon);
        } else {
            setIcon(publicKeyIcon);
        }

        return this;
    }
}
