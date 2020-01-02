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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import saker.build.ide.eclipse.extension.ideconfig.AbstractIDEProjectConfigurationEntry;
import saker.build.ide.eclipse.extension.ideconfig.IIDEProjectConfigurationEntry;

public class AddExportsContainerProjectConfigurationEntry extends AbstractIDEProjectConfigurationEntry {
	private static final Image IMG_ENTRY = JavaIDEConfigurationTypeHandler.getComposedImage(
			AbstractUIPlugin.imageDescriptorFromPlugin(JavaUI.ID_PLUGIN, "icons/full/obj16/module_obj.png"), null);

	private List<AddExportsProjectConfigurationEntry> addExportEntries = new ArrayList<>();

	public AddExportsContainerProjectConfigurationEntry(Collection<AddExportsData> addexportscoll) {
		for (AddExportsData addexport : addexportscoll) {
			addExportEntries.add(new AddExportsProjectConfigurationEntry(addexport));
		}
	}

	public void contribute(List<IClasspathAttribute> extraattrlist) {
		Set<String> exports = new TreeSet<>();
		for (AddExportsProjectConfigurationEntry entry : addExportEntries) {
			if (!entry.isSelected()) {
				continue;
			}
			exports.addAll(entry.getAddExports().toCommandLineStrings());
		}
		if (exports.isEmpty()) {
			//nothing to modify
			return;
		}
		for (Iterator<IClasspathAttribute> it = extraattrlist.iterator(); it.hasNext();) {
			IClasspathAttribute a = it.next();
			if (IClasspathAttribute.ADD_EXPORTS.equals(a.getName())) {
				for (String cmdopd : a.getValue().split(":")) {
					exports.add(cmdopd);
				}
				it.remove();
			}
		}
		extraattrlist.add(JavaCore.newClasspathAttribute(IClasspathAttribute.ADD_EXPORTS, String.join(":", exports)));
	}

	@Override
	public IIDEProjectConfigurationEntry[] getSubEntries() {
		return addExportEntries.toArray(new AddExportsProjectConfigurationEntry[addExportEntries.size()]);
	}

	@Override
	public String getLabel() {
		return "Add exports";
	}

	@Override
	public Image getImage() {
		return IMG_ENTRY;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[addExports=" + addExportEntries + "]";
	}

}
