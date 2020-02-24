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

public class ClassPathContainerConfigurationEntry extends AbstractIDEProjectConfigurationEntry
		implements ClassPathConfigurationEntry {
	private static final Image IMG_ENTRY = JavaIDEConfigurationTypeHandler.getImageFromPlugin(JavaUI.ID_PLUGIN,
			"icons/full/obj16/library_obj.png");

	private List<ClassPathConfigurationEntry> classpathEntries = new ArrayList<>();
	private transient Set<ClassPathConfigurationEntry> removalClasspathEntries = new LinkedHashSet<>();
	private String label;

	public ClassPathContainerConfigurationEntry() {
		this("Classpath");
	}

	public ClassPathContainerConfigurationEntry(String label) {
		this.label = label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void addClassPathEntry(ClassPathConfigurationEntry entry) {
		this.classpathEntries.add(entry);
	}

	public void addRemovalClassPathEntry(ClassPathConfigurationEntry entry) {
		this.removalClasspathEntries.add(entry);
	}

	public boolean isEmpty() {
		return classpathEntries.isEmpty();
	}

	public void initSelection() {
		for (ClassPathConfigurationEntry cpentry : classpathEntries) {
			if (cpentry.isSelected()) {
				this.setSelected(true);
				return;
			}
		}
		this.setSelected(false);
	}

	@Override
	public void contribute(Collection<IClasspathEntry> entries) {
		for (ClassPathConfigurationEntry sentry : removalClasspathEntries) {
			if (!sentry.isSelected()) {
				continue;
			}
			sentry.contribute(entries);
		}
		for (ClassPathConfigurationEntry sentry : classpathEntries) {
			if (!sentry.isSelected() || removalClasspathEntries.contains(sentry)) {
				continue;
			}
			sentry.contribute(entries);
		}
	}

	@Override
	public IIDEProjectConfigurationEntry[] getSubEntries() {
		return classpathEntries.toArray(new IIDEProjectConfigurationEntry[0]);
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public Image getImage() {
		return IMG_ENTRY;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + classpathEntries + "]";
	}
}
