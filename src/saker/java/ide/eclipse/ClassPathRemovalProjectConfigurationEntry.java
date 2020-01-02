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

import java.util.Collection;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import saker.build.ide.eclipse.extension.ideconfig.AbstractIDEProjectConfigurationEntry;

public class ClassPathRemovalProjectConfigurationEntry extends AbstractIDEProjectConfigurationEntry
		implements ClassPathConfigurationEntry {
	private static final Image IMG_JAR_ENTRY = JavaIDEConfigurationTypeHandler.getComposedImage(
			AbstractUIPlugin.imageDescriptorFromPlugin(JavaUI.ID_PLUGIN, "icons/full/obj16/jar_l_obj.png"),
			AbstractUIPlugin.imageDescriptorFromPlugin(JavaIDEConfigurationTypeHandler.PLUGIN_ID,
					"icons/clear_sub.png"));
	private static final Image IMG_DIR_ENTRY = JavaIDEConfigurationTypeHandler.getComposedImage(
			AbstractUIPlugin.imageDescriptorFromPlugin(JavaUI.ID_PLUGIN, "icons/full/obj16/cf_obj.png"),
			AbstractUIPlugin.imageDescriptorFromPlugin(JavaIDEConfigurationTypeHandler.PLUGIN_ID, "icons/clear_sub.png"));

	private IPath path;
	private String labelPath;
	private String fileNameLabel;
	private boolean dir;

	public ClassPathRemovalProjectConfigurationEntry(IPath path, String labelPath) {
		this.path = path;
		this.labelPath = labelPath;
	}

	public void setDirectory(boolean dir) {
		this.dir = dir;
	}

	public void setFileNameLabel(String fileNameLabel) {
		this.fileNameLabel = fileNameLabel;
	}

	@Override
	public void contribute(Collection<IClasspathEntry> entries) {
		IClasspathEntry presententry = JavaIDEConfigurationTypeHandler
				.getAlreadyPresentClassPathEntryWithPathAndKind(path, IClasspathEntry.CPE_LIBRARY, entries);
		if (presententry != null) {
			entries.remove(presententry);
		}
	}

	@Override
	public String getLabel() {
		return "Remove: " + labelPath;
	}

	@Override
	public StyledString getStyledLabel() {
		if (fileNameLabel == null) {
			return super.getStyledLabel();
		}
		StyledString result = new StyledString("Remove: ");
		result.append(fileNameLabel);
		result.append(" - " + labelPath, StyledString.QUALIFIER_STYLER);
		return result;
	}

	@Override
	public Image getImage() {
		if (dir) {
			return IMG_DIR_ENTRY;
		}
		return IMG_JAR_ENTRY;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + path + "]";
	}
}
