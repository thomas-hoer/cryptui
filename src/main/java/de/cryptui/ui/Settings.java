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
package de.cryptui.ui;

import de.cryptui.util.UserConfiguration;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class Settings extends javax.swing.JFrame {

	private static final long serialVersionUID = -2131371161881622162L;

	private JTextField serverTextField;

	/**
	 * Creates new form for the Settings.
	 */
	public Settings() {
		initComponents();
		setResizable(false);
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 */
	private void initComponents() {

		final JLabel jLabel1 = new JLabel();
		serverTextField = new JTextField();
		serverTextField.setText(UserConfiguration.getServer());
		final JButton okButton = new JButton();
		final JButton cancelButton = new JButton();

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

		jLabel1.setText("Server:");

		okButton.setText("OK");
		okButton.addActionListener(event -> okButtonActionPerformed());

		cancelButton.setText("Cancel");
		cancelButton.addActionListener(event -> cancelButtonActionPerformed());

		final GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
				GroupLayout.Alignment.TRAILING,
				layout.createSequentialGroup().addContainerGap().addComponent(jLabel1)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 31, Short.MAX_VALUE)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addGroup(layout.createSequentialGroup().addComponent(okButton).addGap(85, 85, 85)
										.addComponent(cancelButton))
								.addComponent(serverTextField, GroupLayout.PREFERRED_SIZE, 313,
										GroupLayout.PREFERRED_SIZE))
						.addContainerGap()));
		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout
				.createSequentialGroup().addContainerGap()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(jLabel1).addComponent(
						serverTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE))
				.addGap(112, 112, 112).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(okButton).addComponent(cancelButton))
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		pack();
	}

	private void okButtonActionPerformed() {
		UserConfiguration.setProperty(UserConfiguration.SERVER_KEY, serverTextField.getText());
		dispose();
	}

	private void cancelButtonActionPerformed() {
		dispose();
	}

}
