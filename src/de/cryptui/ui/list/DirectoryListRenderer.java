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
package de.cryptui.ui.list;

import java.awt.Component;
import java.io.File;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JList;
import org.apache.commons.lang3.StringUtils;

public class DirectoryListRenderer extends DefaultListCellRenderer {

    private final JButton button;

    public DirectoryListRenderer() {
        button = new JButton();
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean selected, boolean expanded) {
        File file = (File) value;
        String text = file.getName();
        if (StringUtils.isEmpty(text)) {
            text = file.toString();
        }
        if (text.length() > 12) {
            text = text.substring(0, 10) + "...";
        }
        button.setText(text);
        return button;
    }
}