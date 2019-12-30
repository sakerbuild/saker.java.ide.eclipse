package saker.java.ide.eclipse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import saker.build.ide.eclipse.extension.ideconfig.AbstractIDEProjectConfigurationEntry;
import saker.build.ide.eclipse.extension.ideconfig.IIDEProjectConfigurationEntry;

public class JREClassPathProjectConfigurationEntry extends AbstractIDEProjectConfigurationEntry
		implements ClassPathConfigurationEntry {

	private static final Image IMG_ENTRY = JavaIDEConfigurationTypeHandler.getComposedImage(
			AbstractUIPlugin.imageDescriptorFromPlugin(JavaUI.ID_PLUGIN, "icons/full/obj16/library_obj.png"),
			AbstractUIPlugin.imageDescriptorFromPlugin(JavaIDEConfigurationTypeHandler.PLUGIN_ID, "icons/add_sub.png"));

	private IVMInstall vmInstall;
	private IIDEProjectConfigurationEntry[] childArray;
	private IClasspathEntry presentClasspathEntry;
	private AddExportsContainerProjectConfigurationEntry addExportsContainer;

	public JREClassPathProjectConfigurationEntry(IVMInstall vmInstall, Collection<AddExportsData> addexportscoll,
			IClasspathEntry presententry) {
		this.presentClasspathEntry = presententry;
		this.vmInstall = vmInstall;

		List<IIDEProjectConfigurationEntry> children = new ArrayList<>();

		if (!addexportscoll.isEmpty()) {
			addExportsContainer = new AddExportsContainerProjectConfigurationEntry(addexportscoll);
			children.add(addExportsContainer);
		}

		childArray = children.toArray(new IIDEProjectConfigurationEntry[children.size()]);
	}

	@Override
	public void contribute(Collection<IClasspathEntry> entries) {
		IClasspathAttribute[] extraattrs = null;
		IAccessRule[] accessrules = null;
		boolean exported = false;
		if (presentClasspathEntry != null) {
			accessrules = presentClasspathEntry.getAccessRules();
			exported = presentClasspathEntry.isExported();
			extraattrs = presentClasspathEntry.getExtraAttributes();
			entries.remove(presentClasspathEntry);
		}

		if (addExportsContainer != null && addExportsContainer.isSelected()) {
			List<IClasspathAttribute> extraattrlist = new ArrayList<>();
			if (presentClasspathEntry != null) {
				IClasspathAttribute[] presentextraattributes = presentClasspathEntry.getExtraAttributes();
				for (IClasspathAttribute ea : presentextraattributes) {
					extraattrlist.add(ea);
				}
			}

			addModuleExtraAttribute(extraattrlist);
			addExportsContainer.contribute(extraattrlist);
			extraattrs = extraattrlist.toArray(new IClasspathAttribute[extraattrlist.size()]);
		}

		entries.add(JavaCore.newContainerEntry(JavaRuntime.newJREContainerPath(vmInstall), accessrules, extraattrs,
				exported));
	}

	private static void addModuleExtraAttribute(List<IClasspathAttribute> extraattrlist) {
		for (Iterator<IClasspathAttribute> it = extraattrlist.iterator(); it.hasNext();) {
			IClasspathAttribute a = it.next();
			if (IClasspathAttribute.MODULE.equals(a.getName())) {
				if ("true".equals(a.getValue())) {
					return;
				}
				it.remove();
			}
		}
		extraattrlist.add(JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true"));
	}

	@Override
	public IIDEProjectConfigurationEntry[] getSubEntries() {
		return childArray.clone();
	}

	@Override
	public String getLabel() {
		return vmInstall.getName();
	}

	@Override
	public Image getImage() {
		return IMG_ENTRY;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[vmInstall =" + vmInstall + "]";
	}

}
