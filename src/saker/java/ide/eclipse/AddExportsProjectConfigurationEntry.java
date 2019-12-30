package saker.java.ide.eclipse;

import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import saker.build.ide.eclipse.extension.ideconfig.AbstractIDEProjectConfigurationEntry;

public class AddExportsProjectConfigurationEntry extends AbstractIDEProjectConfigurationEntry {
	private static final Image IMG_ENTRY = JavaIDEConfigurationTypeHandler.getComposedImage(
			AbstractUIPlugin.imageDescriptorFromPlugin(JavaUI.ID_PLUGIN, "icons/full/obj16/module_obj.png"),
			AbstractUIPlugin.imageDescriptorFromPlugin(JavaIDEConfigurationTypeHandler.PLUGIN_ID, "icons/add_sub.png"));

	private AddExportsData addExports;

	public AddExportsProjectConfigurationEntry(AddExportsData addExports) {
		this.addExports = addExports;
	}

	public AddExportsData getAddExports() {
		return addExports;
	}

	@Override
	public String getLabel() {
		return addExports.toString();
	}

	@Override
	public Image getImage() {
		return IMG_ENTRY;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[addExports=" + addExports + "]";
	}

}
