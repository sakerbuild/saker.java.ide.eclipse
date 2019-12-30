package saker.java.ide.eclipse.params;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallChangedListener;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.PropertyChangeEvent;

import saker.build.ide.eclipse.api.ISakerPlugin;
import saker.build.ide.eclipse.extension.params.IEnvironmentUserParameterContributor;
import saker.build.ide.eclipse.extension.params.UserParameterModification;

public class JavaInstallationEnvironmentUserParameterContributor
		implements IEnvironmentUserParameterContributor, IVMInstallChangedListener {
	private static final String INSTALL_LOCATIONS_ENV_PARAMETER_NAME = "saker.java.jre.install.locations";

	private ISakerPlugin plugin;

	@Override
	public synchronized Set<UserParameterModification> contribute(ISakerPlugin plugin, Map<String, String> parameters,
			IProgressMonitor monitor) throws CoreException {
		JavaRuntime.addVMInstallChangedListener(this);
		StringJoiner installlocations = new StringJoiner(";");
		String present = parameters.get(INSTALL_LOCATIONS_ENV_PARAMETER_NAME);
		if (present != null) {
			installlocations.add(present);
		}
		//assign plugin later, because when we query the installs, the vmAdded events fire automatically
		//therefor we assign the plugin later, so we don't invalidate it unnecessarily
		//it is acceptable, as concurrency errors won't happen due to synchronization of methods
		boolean foundinstall = false;
		for (IVMInstallType vmitype : JavaRuntime.getVMInstallTypes()) {
			for (IVMInstall install : vmitype.getVMInstalls()) {
				File installoc = install.getInstallLocation();
				if (installoc == null) {
					continue;
				}
				foundinstall = true;
				installlocations.add(installoc.toString());
			}
		}
		this.plugin = plugin;

		if (!foundinstall) {
			return Collections.emptySet();
		}

		Set<UserParameterModification> result = new LinkedHashSet<>();
		result.add(UserParameterModification.set(INSTALL_LOCATIONS_ENV_PARAMETER_NAME, installlocations.toString()));
		return result;
	}

	@Override
	public synchronized void dispose() {
		this.plugin = null;
		JavaRuntime.removeVMInstallChangedListener(this);
	}

	@Override
	public synchronized void defaultVMInstallChanged(IVMInstall previous, IVMInstall current) {
		fireInvalidateContributions();
	}

	@Override
	public synchronized void vmChanged(PropertyChangeEvent event) {
		fireInvalidateContributions();
	}

	@Override
	public synchronized void vmAdded(IVMInstall vm) {
		fireInvalidateContributions();
	}

	@Override
	public synchronized void vmRemoved(IVMInstall vm) {
		fireInvalidateContributions();
	}

	private void fireInvalidateContributions() {
		ISakerPlugin plugin = this.plugin;
		if (plugin != null) {
			plugin.invalidateEnvironmentUserParameterContributions();
		}
	}
}
