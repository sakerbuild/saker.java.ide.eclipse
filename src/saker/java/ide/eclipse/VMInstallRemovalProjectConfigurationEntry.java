package saker.java.ide.eclipse;

import java.util.Collection;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import saker.build.ide.eclipse.extension.ideconfig.AbstractIDEProjectConfigurationEntry;

public class VMInstallRemovalProjectConfigurationEntry extends AbstractIDEProjectConfigurationEntry
		implements ClassPathConfigurationEntry {
	private static final Image IMG_ENTRY = JavaIDEConfigurationTypeHandler.getComposedImage(
			AbstractUIPlugin.imageDescriptorFromPlugin(JavaUI.ID_PLUGIN, "icons/full/obj16/library_obj.png"),
			AbstractUIPlugin.imageDescriptorFromPlugin(JavaIDEConfigurationTypeHandler.PLUGIN_ID,
					"icons/clear_sub.png"));

	private IClasspathEntry entry;
	private IVMInstall vmInstall;

	public VMInstallRemovalProjectConfigurationEntry(IClasspathEntry cpentry, IVMInstall vminstall) {
		this.entry = cpentry;
		this.vmInstall = vminstall;
	}

	@Override
	public void contribute(Collection<IClasspathEntry> entries) {
		entries.remove(entry);
	}

	@Override
	public String getLabel() {
		return "Remove: " + vmInstall.getName();
	}

	@Override
	public Image getImage() {
		return IMG_ENTRY;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[entry =" + entry + "]";
	}

}
