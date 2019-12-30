package saker.java.ide.eclipse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import saker.build.ide.eclipse.extension.ideconfig.AbstractIDEProjectConfigurationEntry;
import saker.build.ide.eclipse.extension.ideconfig.IIDEProjectConfigurationEntry;

public class ClassPathProjectConfigurationEntry extends AbstractIDEProjectConfigurationEntry
		implements ClassPathConfigurationEntry {

	private static final Image IMG_JAR_ENTRY = JavaIDEConfigurationTypeHandler.getComposedImage(
			AbstractUIPlugin.imageDescriptorFromPlugin(JavaUI.ID_PLUGIN, "icons/full/obj16/jar_l_obj.png"),
			AbstractUIPlugin.imageDescriptorFromPlugin(JavaIDEConfigurationTypeHandler.PLUGIN_ID, "icons/add_sub.png"));
	private static final Image IMG_JAR_SRC_ENTRY = JavaIDEConfigurationTypeHandler.getComposedImage(
			AbstractUIPlugin.imageDescriptorFromPlugin(JavaUI.ID_PLUGIN, "icons/full/obj16/jar_lsrc_obj.png"),
			AbstractUIPlugin.imageDescriptorFromPlugin(JavaIDEConfigurationTypeHandler.PLUGIN_ID, "icons/add_sub.png"));

	private static final Image IMG_DIR_ENTRY = JavaIDEConfigurationTypeHandler.getComposedImage(
			AbstractUIPlugin.imageDescriptorFromPlugin(JavaUI.ID_PLUGIN, "icons/full/obj16/cf_obj.png"),
			AbstractUIPlugin.imageDescriptorFromPlugin(JavaIDEConfigurationTypeHandler.PLUGIN_ID, "icons/add_sub.png"));
	private static final Image IMG_DIR_SRC_ENTRY = JavaIDEConfigurationTypeHandler.getComposedImage(
			AbstractUIPlugin.imageDescriptorFromPlugin(JavaUI.ID_PLUGIN, "icons/full/obj16/cf_src_obj.png"),
			AbstractUIPlugin.imageDescriptorFromPlugin(JavaIDEConfigurationTypeHandler.PLUGIN_ID, "icons/add_sub.png"));

	private IPath path;
	private String labelPath;
	private String fileNameLabel;
	private ClassPathSourceAttachmentConfigurationEntry sourceAttachmentEntry;
	private boolean dir;

	public ClassPathProjectConfigurationEntry(IPath path, String labelPath) {
		this.path = path;
		this.labelPath = labelPath;
	}

	public void setFileNameLabel(String fileNameLabel) {
		this.fileNameLabel = fileNameLabel;
	}

	public void setSourceAttachmentEntry(ClassPathSourceAttachmentConfigurationEntry sourceAttachmentEntry) {
		this.sourceAttachmentEntry = sourceAttachmentEntry;
	}

	public void setDirectory(boolean dir) {
		this.dir = dir;
	}

	@Override
	public void contribute(Collection<IClasspathEntry> entries) {
		IClasspathEntry presententry = JavaIDEConfigurationTypeHandler
				.getAlreadyPresentClassPathEntryWithPathAndKind(path, IClasspathEntry.CPE_LIBRARY, entries);

		IPath srcattachmentpath = null;
		IPath sourceattachmentrootpath = null;
		IAccessRule[] accessrules = null;
		IClasspathAttribute[] extraattributes = null;
		boolean exported = false;
		if (presententry != null) {
			srcattachmentpath = presententry.getSourceAttachmentPath();
			sourceattachmentrootpath = presententry.getSourceAttachmentRootPath();
			accessrules = presententry.getAccessRules();
			extraattributes = presententry.getExtraAttributes();
			exported = presententry.isExported();

			entries.remove(presententry);

			extraattributes = removeModuleAttribute(extraattributes);
		}
		if (sourceAttachmentEntry != null && sourceAttachmentEntry.isSelected()) {
			srcattachmentpath = sourceAttachmentEntry.getPath();
		}
		IClasspathEntry ncp = JavaCore.newLibraryEntry(path, srcattachmentpath, sourceattachmentrootpath, accessrules,
				extraattributes, exported);
		entries.add(ncp);
	}

	private static IClasspathAttribute[] removeModuleAttribute(IClasspathAttribute[] attrs) {
		if (attrs == null) {
			return attrs;
		}
		for (int i = 0; i < attrs.length; i++) {
			IClasspathAttribute attr = attrs[i];
			if (IClasspathAttribute.MODULE.equals(attr.getName())) {
				//we need to remove instead of setting it to false, as eclipse is buggy?
				//it stays in the Modulepath section if we set to false.
				IClasspathAttribute[] result = new IClasspathAttribute[attrs.length - 1];
				System.arraycopy(attrs, 0, result, 0, i);
				System.arraycopy(attrs, i + 1, result, i, attrs.length - i - 1);
				return result;
			}
		}
		return attrs;
	}

	@Override
	public IIDEProjectConfigurationEntry[] getSubEntries() {
		List<IIDEProjectConfigurationEntry> entries = new ArrayList<>();
		if (sourceAttachmentEntry != null) {
			entries.add(sourceAttachmentEntry);
		}
		return entries.toArray(JavaIDEConfigurationTypeHandler.EMPTY_CONFIGURATIONENTRY_ARRAY);
	}

	@Override
	public String getLabel() {
		return labelPath;
	}

	@Override
	public StyledString getStyledLabel() {
		if (fileNameLabel == null) {
			return super.getStyledLabel();
		}
		StyledString result = new StyledString();
		result.append(fileNameLabel);
		result.append(" - " + labelPath, StyledString.QUALIFIER_STYLER);
		return result;
	}

	@Override
	public Image getImage() {
		if (dir) {
			if (sourceAttachmentEntry != null) {
				return IMG_DIR_SRC_ENTRY;
			}
			return IMG_DIR_ENTRY;
		}
		if (sourceAttachmentEntry != null) {
			return IMG_JAR_SRC_ENTRY;
		}
		return IMG_JAR_ENTRY;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + labelPath + "]";
	}

}
