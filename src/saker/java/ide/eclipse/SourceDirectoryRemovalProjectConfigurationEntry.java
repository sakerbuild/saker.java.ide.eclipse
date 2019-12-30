package saker.java.ide.eclipse;

import java.util.Collection;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import saker.build.ide.eclipse.extension.ideconfig.AbstractIDEProjectConfigurationEntry;

public class SourceDirectoryRemovalProjectConfigurationEntry extends AbstractIDEProjectConfigurationEntry
		implements ClassPathConfigurationEntry {
	private static final Image IMG_ENTRY = JavaIDEConfigurationTypeHandler.getComposedImage(
			AbstractUIPlugin.imageDescriptorFromPlugin(JavaUI.ID_PLUGIN, "icons/full/obj16/packagefolder_obj.png"),
			AbstractUIPlugin.imageDescriptorFromPlugin(JavaIDEConfigurationTypeHandler.PLUGIN_ID,
					"icons/clear_sub.png"));

	private IPath path;
	private String projectRelativePath;

	public SourceDirectoryRemovalProjectConfigurationEntry(IPath path, String projectRelativePath) {
		this.path = path;
		this.projectRelativePath = projectRelativePath;
	}

	@Override
	public void contribute(Collection<IClasspathEntry> entries) {
		IClasspathEntry presententry = JavaIDEConfigurationTypeHandler
				.getAlreadyPresentClassPathEntryWithPathAndKind(path, IClasspathEntry.CPE_SOURCE, entries);
		if (presententry != null) {
			entries.remove(presententry);
		}
	}

	@Override
	public String getLabel() {
		return "Remove: " + projectRelativePath;
	}

	@Override
	public Image getImage() {
		return IMG_ENTRY;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + path + "]";
	}
}
