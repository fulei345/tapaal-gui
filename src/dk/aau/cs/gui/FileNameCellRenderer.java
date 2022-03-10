package dk.aau.cs.gui;

import java.awt.Component;
import java.io.File;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class FileNameCellRenderer<T> extends JLabel implements ListCellRenderer<T>  {
	// Custom cell renderer for the file list to only display the name of the
	// file	instead of the whole path.{
	
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		if (value instanceof File)
			setText(((File) value).getName());
		else
			setText(value.toString());
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}
		setEnabled(list.isEnabled());
		setFont(list.getFont());
		setOpaque(true);
		return this;
	}
}
