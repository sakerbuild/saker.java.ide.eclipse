package saker.java.ide.eclipse;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import saker.build.ide.eclipse.extension.ideconfig.AbstractIDEProjectConfigurationEntry;

public class ClassPathSourceAttachmentConfigurationEntry extends AbstractIDEProjectConfigurationEntry {
	private static final Image IMG_ENTRY = JavaIDEConfigurationTypeHandler.getComposedImage(
			AbstractUIPlugin.imageDescriptorFromPlugin(JavaUI.ID_PLUGIN, "icons/full/obj16/source_attach_attrib.png"),
			AbstractUIPlugin.imageDescriptorFromPlugin(JavaIDEConfigurationTypeHandler.PLUGIN_ID, "icons/add_sub.png"));

	private IPath path;
	private String labelPath;
	private String fileNameLabel;

	public ClassPathSourceAttachmentConfigurationEntry(IPath path, String projectRelativePath) {
		this.path = path;
		this.labelPath = projectRelativePath;
	}

	public void setFileNameLabel(String fileNameLabel) {
		this.fileNameLabel = fileNameLabel;
	}

	public IPath getPath() {
		return path;
	}

	@Override
	public String getLabel() {
		return "Source attachment: " + labelPath;
	}

	@Override
	public StyledString getStyledLabel() {
		if (fileNameLabel == null) {
			return super.getStyledLabel();
		}
		StyledString result = new StyledString("Source attachment: ");
		result.append(fileNameLabel);
		result.append(" - " + labelPath, StyledString.QUALIFIER_STYLER);
		return result;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + labelPath + "]";
	}

	@Override
	public Image getImage() {
		return IMG_ENTRY;
	}
}
