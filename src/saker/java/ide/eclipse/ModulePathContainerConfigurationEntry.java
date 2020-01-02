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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.graphics.Image;

import saker.build.ide.eclipse.extension.ideconfig.AbstractIDEProjectConfigurationEntry;
import saker.build.ide.eclipse.extension.ideconfig.IIDEProjectConfigurationEntry;

public class ModulePathContainerConfigurationEntry extends AbstractIDEProjectConfigurationEntry
		implements ClassPathConfigurationEntry {
	private static final Image IMG_ENTRY = JavaIDEConfigurationTypeHandler.getImageFromPlugin(JavaUI.ID_PLUGIN,
			"icons/full/obj16/module_obj.png");

	private List<ClassPathConfigurationEntry> modulepathEntries = new ArrayList<>();
	private transient Set<ClassPathConfigurationEntry> removalModulepathEntries = new LinkedHashSet<>();

	public ModulePathContainerConfigurationEntry() {
	}

	public void addModulePathEntry(ClassPathConfigurationEntry entry) {
		this.modulepathEntries.add(entry);
	}

	public void addRemovalModulePathEntry(ClassPathConfigurationEntry entry) {
		this.removalModulepathEntries.add(entry);
	}

	public boolean isEmpty() {
		return modulepathEntries.isEmpty();
	}

	public void initSelection() {
		for (ClassPathConfigurationEntry cpentry : modulepathEntries) {
			if (cpentry.isSelected()) {
				this.setSelected(true);
				return;
			}
		}
		this.setSelected(false);
	}

	@Override
	public void contribute(Collection<IClasspathEntry> entries) {
		for (ClassPathConfigurationEntry sentry : removalModulepathEntries) {
			if (!sentry.isSelected()) {
				continue;
			}
			sentry.contribute(entries);
		}
		for (ClassPathConfigurationEntry sentry : modulepathEntries) {
			if (!sentry.isSelected() || removalModulepathEntries.contains(sentry)) {
				continue;
			}
			sentry.contribute(entries);
		}
	}

	@Override
	public IIDEProjectConfigurationEntry[] getSubEntries() {
		return modulepathEntries.toArray(new IIDEProjectConfigurationEntry[0]);
	}

	@Override
	public String getLabel() {
		return "Modulepath";
	}

	@Override
	public Image getImage() {
		return IMG_ENTRY;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + modulepathEntries + "]";
	}
}
