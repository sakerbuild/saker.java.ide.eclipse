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
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import saker.build.ide.eclipse.api.ISakerProject;
import saker.build.ide.eclipse.extension.ideconfig.AbstractIDEProjectConfigurationEntry;
import saker.build.ide.eclipse.extension.ideconfig.IIDEProjectConfigurationEntry;
import saker.build.ide.eclipse.extension.ideconfig.IIDEProjectConfigurationRootEntry;

public class JavaNatureProjectConfigurationEntry extends AbstractIDEProjectConfigurationEntry
		implements IIDEProjectConfigurationRootEntry {

	private static final Image IMG_ENTRY = JavaIDEConfigurationTypeHandler.getComposedImage(
			AbstractUIPlugin.imageDescriptorFromPlugin(JavaUI.ID_PLUGIN, "icons/full/eview16/jperspective.gif"),
			AbstractUIPlugin.imageDescriptorFromPlugin(JavaIDEConfigurationTypeHandler.PLUGIN_ID, "icons/add_sub.png"));

	private List<ClassPathConfigurationEntry> classpathEntries = new ArrayList<>();
	private ISakerProject sakerProject;

	private transient Set<ClassPathConfigurationEntry> removalClasspathEntries = new HashSet<>();

	public JavaNatureProjectConfigurationEntry(ISakerProject project) {
		this.sakerProject = project;
	}

	public void addClassPathEntry(ClassPathConfigurationEntry entry) {
		this.classpathEntries.add(entry);
	}

	public void addRemovalClassPathEntry(ClassPathConfigurationEntry entry) {
		this.removalClasspathEntries.add(entry);
	}

	@Override
	public void apply(IProgressMonitor monitor) throws CoreException {
		IProject project = sakerProject.getProject();
		if (!project.hasNature(JavaCore.NATURE_ID)) {
			IProjectDescription description = project.getDescription();
			String[] natures = description.getNatureIds();
			String[] copy = Arrays.copyOfRange(natures, 0, natures.length + 1);
			copy[natures.length] = JavaCore.NATURE_ID;
			description.setNatureIds(copy);

			project.setDescription(description, monitor);
		}
		IJavaProject jproject = JavaCore.create(project);
		if (isAnySelected(classpathEntries) || isAnySelected(removalClasspathEntries)) {
			IClasspathEntry[] cps = jproject.getRawClasspath();
			if (Arrays.equals(new IClasspathEntry[] { JavaCore.newSourceEntry(project.getFullPath()) }, cps)) {
				//if the project was just configured with the Java nature, then it will be initialized
				//with the default raw classpath of the source directory being the root project path
				//adding other source directories to this configuration will result in an error
				//by the java plugin
				//awful workaround to the nonsense default class path initialization by the java plugin
				//see JavaProject.defaultClasspath()
				//    JavaProject.readFileEntriesWithException
				cps = new IClasspathEntry[0];
			}
			Set<IClasspathEntry> nentries = new LinkedHashSet<>();
			for (IClasspathEntry rcp : cps) {
				nentries.add(rcp);
			}
			Set<IClasspathEntry> startentries = new LinkedHashSet<>(nentries);
			for (ClassPathConfigurationEntry sentry : removalClasspathEntries) {
				if (!sentry.isSelected()) {
					continue;
				}
				sentry.contribute(nentries);
			}
			for (ClassPathConfigurationEntry sentry : classpathEntries) {
				if (!sentry.isSelected() || removalClasspathEntries.contains(sentry)) {
					continue;
				}
				sentry.contribute(nentries);
			}
			if (!startentries.equals(nentries)) {
				IClasspathEntry[] nentriesarray = nentries.toArray(new IClasspathEntry[nentries.size()]);
				// save it
				jproject.setRawClasspath(nentriesarray, monitor);
			}
		}
	}

	private static boolean isAnySelected(Iterable<? extends IIDEProjectConfigurationEntry> entries) {
		for (IIDEProjectConfigurationEntry e : entries) {
			if (e.isSelected()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public IIDEProjectConfigurationEntry[] getSubEntries() {
		return classpathEntries.toArray(new IIDEProjectConfigurationEntry[0]);
	}

	@Override
	public String getLabel() {
		return "Java";
	}

	@Override
	public Image getImage() {
		return IMG_ENTRY;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[classpathEntries=" + classpathEntries + "]";
	}

}
