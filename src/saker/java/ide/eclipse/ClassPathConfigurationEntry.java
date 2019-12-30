package saker.java.ide.eclipse;

import java.util.Collection;

import org.eclipse.jdt.core.IClasspathEntry;

import saker.build.ide.eclipse.extension.ideconfig.IIDEProjectConfigurationEntry;

public interface ClassPathConfigurationEntry extends IIDEProjectConfigurationEntry {
	public void contribute(Collection<IClasspathEntry> entries);
}
