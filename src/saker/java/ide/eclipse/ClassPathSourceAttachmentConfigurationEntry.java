/*
 * Copyright (C) 2020 Bence Sipka
 *
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package saker.java.ide.eclipse;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import saker.build.ide.eclipse.extension.ideconfig.AbstractIDEProjectConfigurationEntry;

public class ClassPathSourceAttachmentConfigurationEntry extends AbstractIDEProjectConfigurationEntry {
	private static final Image IMG_ENTRY = JavaIDEConfigurationTypeHandler.getComposedImage(
			AbstractUIPlugin.imageDescriptorFromPlugin(JavaUI.ID_PLUGIN, "icons/full/obj16/source_attach_attrib.png"),
			AbstractUIPlugin.imageDescriptorFromPlugin(JavaIDEConfigurationTypeHandler.PLUGIN_ID, "icons/add_sub.png"));

	private IPath path;
	private String labelPath;
	private String fileNameLabel;

	public ClassPathSourceAttachmentConfigurationEntry(IPath path, String projectRelativePath) {
		this.path = path;
		this.labelPath = projectRelativePath;
	}

	public void setFileNameLabel(String fileNameLabel) {
		this.fileNameLabel = fileNameLabel;
	}

	public IPath getPath() {
		return path;
	}

	@Override
	public String getLabel() {
		return "Source attachment: " + labelPath;
	}

	@Override
	public StyledString getStyledLabel() {
		if (fileNameLabel == null) {
			return super.getStyledLabel();
		}
		StyledString result = new StyledString("Source attachment: ");
		result.append(fileNameLabel);
		result.append(" - " + labelPath, StyledString.QUALIFIER_STYLER);
		return result;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + labelPath + "]";
	}

	@Override
	public Image getImage() {
		return IMG_ENTRY;
	}
}
