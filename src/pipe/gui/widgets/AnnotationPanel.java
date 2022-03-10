package pipe.gui.widgets;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;

import javax.swing.JPanel;

import pipe.gui.graphicElements.AnnotationNote;

/*
 * ParameterPanel.java
 *
 * Created on April 15, 2007, 9:25 AM
 */

/**
 * @author Pere Bonet
 */
public class AnnotationPanel extends javax.swing.JPanel {

	private final AnnotationNote annotation;

	/**
	 * Creates new form ParameterPanel
	 */
	public AnnotationPanel(AnnotationNote _annotation) {
		annotation = _annotation;
		initComponents();
		textArea.setText(annotation.getText());
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 */
	private void initComponents() {
		JPanel container;
		container = new JPanel();
		container.setLayout(new GridBagLayout());

		java.awt.GridBagConstraints gridBagConstraints;

		panel = new javax.swing.JPanel();
		jScrollPane1 = new javax.swing.JScrollPane();
		textArea = new javax.swing.JTextArea();
		buttonPanel = new javax.swing.JPanel();
		okButton = new javax.swing.JButton();
		cancelButton = new javax.swing.JButton();

		setLayout(new BorderLayout());

		panel.setLayout(new java.awt.GridLayout(1, 0));

		panel.setBorder(javax.swing.BorderFactory
				.createTitledBorder("Edit Annotation"));
		textArea.setColumns(20);
		textArea.setRows(5);
		jScrollPane1.setViewportView(textArea);

		panel.add(jScrollPane1);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		buttonPanel.setLayout(new java.awt.GridBagLayout());
		okButton.setText("OK");
		okButton.setMaximumSize(new java.awt.Dimension(100, 25));
		okButton.setMinimumSize(new java.awt.Dimension(100, 25));
		okButton.setPreferredSize(new java.awt.Dimension(100, 25));
		okButton.setMnemonic(KeyEvent.VK_O);
		okButton.addActionListener(this::okButtonActionPerformed);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
		buttonPanel.add(okButton, gridBagConstraints);

		cancelButton.setText("Cancel");
		cancelButton.setMaximumSize(new java.awt.Dimension(100, 25));
		cancelButton.setMinimumSize(new java.awt.Dimension(100, 25));
		cancelButton.setPreferredSize(new java.awt.Dimension(100, 25));
		cancelButton.setMnemonic(KeyEvent.VK_C);
		cancelButton.addActionListener(this::cancelButtonActionPerformed);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
		buttonPanel.add(cancelButton, gridBagConstraints);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 3;
		gbc.weighty= 3;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.BOTH;
		container.add(panel,gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.EAST;
		container.add(buttonPanel,gbc);

		add(container);
		this.setPreferredSize(new Dimension(400, 300));
	}

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
		exit();
	}

	private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {
		annotation.setText(textArea.getText());
		annotation.repaint();
		exit();
	}

	private void exit() {
		// Provisional!
		this.getParent().getParent().getParent().getParent().setVisible(false);
	}

	private javax.swing.JPanel buttonPanel;
	private javax.swing.JButton cancelButton;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JButton okButton;
	private javax.swing.JPanel panel;
	private javax.swing.JTextArea textArea;

}
