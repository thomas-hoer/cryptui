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

import java.awt.Component;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.filechooser.FileSystemView;

public class FileListRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = -3395399290385642832L;
	private static final FileSystemView FILE_SYSTEM_VIEW = FileSystemView.getFileSystemView();
	private static final Map<String, Icon> ICON_CACHE = new HashMap<>();

	@Override
	public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index,
			final boolean selected, final boolean expanded) {
		super.getListCellRendererComponent(list, value, index, selected, expanded);
		final File file = (File) value;
		final String fileName = file.getAbsolutePath();
		final Icon systemIcon;
		if (ICON_CACHE.containsKey(fileName)) {
			systemIcon = ICON_CACHE.get(fileName);
		} else {
			systemIcon = FILE_SYSTEM_VIEW.getSystemIcon(file);
			ICON_CACHE.put(fileName, systemIcon);
		}
		setIcon(systemIcon);
		setText(FILE_SYSTEM_VIEW.getSystemDisplayName(file));

		return this;
	}
}
