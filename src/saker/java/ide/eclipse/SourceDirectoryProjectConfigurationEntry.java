package saker.java.ide.eclipse;

import java.util.Collection;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import saker.build.ide.eclipse.extension.ideconfig.AbstractIDEProjectConfigurationEntry;

public class SourceDirectoryProjectConfigurationEntry extends AbstractIDEProjectConfigurationEntry
		implements ClassPathConfigurationEntry {

	private static final Image IMG_ENTRY = JavaIDEConfigurationTypeHandler.getComposedImage(
			AbstractUIPlugin.imageDescriptorFromPlugin(JavaUI.ID_PLUGIN, "icons/full/obj16/packagefolder_obj.png"),
			AbstractUIPlugin.imageDescriptorFromPlugin(JavaIDEConfigurationTypeHandler.PLUGIN_ID, "icons/add_sub.png"));

	private IPath path;
	private String projectRelativePath;

	public SourceDirectoryProjectConfigurationEntry(IPath path, String projectRelativePath) {
		this.path = path;
		this.projectRelativePath = projectRelativePath;
	}

	@Override
	public void contribute(Collection<IClasspathEntry> entries) {
		IPath[] inclusionpatterns = null;
		IPath[] exclusionpatterns = null;
		IPath specificoutputlocation = null;
		IClasspathAttribute[] extraattributes = null;
		IClasspathEntry presententry = JavaIDEConfigurationTypeHandler
				.getAlreadyPresentClassPathEntryWithPathAndKind(path, IClasspathEntry.CPE_SOURCE, entries);
		if (presententry != null) {
			inclusionpatterns = presententry.getInclusionPatterns();
			exclusionpatterns = presententry.getExclusionPatterns();
			specificoutputlocation = presententry.getOutputLocation();
			extraattributes = presententry.getExtraAttributes();
			entries.remove(presententry);
		}
		entries.add(JavaCore.newSourceEntry(path, inclusionpatterns, exclusionpatterns, specificoutputlocation,
				extraattributes));
	}

	@Override
	public String getLabel() {
		return projectRelativePath;
	}

	@Override
	public Image getImage() {
		return IMG_ENTRY;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + projectRelativePath + "]";
	}

}
