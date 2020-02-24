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

import java.io.File;
import java.io.IOException;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.IVMInstall3;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import saker.build.ide.eclipse.api.ISakerProject;
import saker.build.ide.eclipse.extension.ideconfig.IIDEConfigurationTypeHandler;
import saker.build.ide.eclipse.extension.ideconfig.IIDEProjectConfigurationEntry;
import saker.build.ide.eclipse.extension.ideconfig.IIDEProjectConfigurationRootEntry;

public class JavaIDEConfigurationTypeHandler implements IIDEConfigurationTypeHandler {
	//XXX use ISharedImages from jdt
	//JavaUI.getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_JAR)
	public static final String PLUGIN_ID = "saker.java.ide.eclipse";

	public static final IIDEProjectConfigurationEntry[] EMPTY_CONFIGURATIONENTRY_ARRAY = new IIDEProjectConfigurationEntry[0];

	public static final String TYPE = "saker.java.compile.ide.configuration";

	public static final String FIELD_SOURCE_DIRECTORIES = "java.source.directories";
	public static final String FIELD_SOURCEDIRECTORY_PATH = "path";
	public static final String FIELD_SOURCEDIRECTORY_FILES = "files";

	public static final String FIELD_CLASSPATHS = "java.classpaths";

	public static final String FIELD_CLASSPATH_PATH = "path";
	public static final String FIELD_CLASSPATH_SOURCEPATH = "sourcepath";
	public static final String FIELD_CLASSPATH_DOCPATH = "docpath";
	public static final String FIELD_CLASSPATH_LOCAL_PATH = "path.local";
	public static final String FIELD_CLASSPATH_LOCAL_SOURCEPATH = "sourcepath.local";
	public static final String FIELD_CLASSPATH_LOCAL_DOCPATH = "docpath.local";
	public static final String FIELD_CLASSPATH_SOURCEDIRECTORIES = "sourcedirectories";
	public static final String FIELD_CLASSPATH_SOURCEGENDIRECTORY = "sourcegendirectory";

	public static final String FIELD_BOOT_CLASSPATHS = "java.bootclasspaths";

	public static final String FIELD_MODULEPATHS = "java.modulepaths";

	public static final String FIELD_ADDEXPORTS = "java.addexports";

	public static final String FIELD_COMPILER_JDKHOME = "java.compiler.install.location";

	public static final String FIELD_COMPILER_JAVAVERSION = "java.compiler.java.version";

	public static final String FIELD_PROCESSOR_GEN_DIRECTORIES = "java.processor.gendirectories";

	public static final String FIELD_OUTPUT_BIN_DIRECTORY = "java.output.directory.bin";

	private static final Pattern PATTERN_SPLIT_COMMA = Pattern.compile("[,]+");
	public static final Set<String> ALL_UNNAMED_SINGLETON_SET = Collections.singleton("ALL-UNNAMED");

	@Override
	public IIDEProjectConfigurationRootEntry[] parseConfiguration(ISakerProject project, Map<String, ?> configuration,
			IProgressMonitor monitor) throws CoreException {
		JavaNatureProjectConfigurationEntry natureentry = new JavaNatureProjectConfigurationEntry(project);
		addJavaConfigurationSubEntries(project, configuration, natureentry, monitor);
		return new IIDEProjectConfigurationRootEntry[] { natureentry };
	}

	private static void addJavaConfigurationSubEntries(ISakerProject project, Map<String, ?> configuration,
			JavaNatureProjectConfigurationEntry natureentry, IProgressMonitor monitor) throws CoreException {
		if (configuration.isEmpty()) {
			return;
		}
		IJavaProject jproject = JavaCore.create(project.getProject());
		Collection<? extends IClasspathEntry> currentclasspath;
		try {
			currentclasspath = Arrays.asList(jproject.getRawClasspath());
		} catch (CoreException e) {
			//probably because the project doesn't have Java nature yet
			currentclasspath = Collections.emptyList();
		}
		Object sourcedirs = configuration.get(FIELD_SOURCE_DIRECTORIES);
		Object modulepaths = configuration.get(FIELD_MODULEPATHS);
		Object classpaths = configuration.get(FIELD_CLASSPATHS);
		Object bootclasspaths = configuration.get(FIELD_BOOT_CLASSPATHS);
		Object gendirs = configuration.get(FIELD_PROCESSOR_GEN_DIRECTORIES);
		Object compilerinstalllocation = configuration.get(FIELD_COMPILER_JDKHOME);
		File installlocfile = compilerinstalllocation instanceof String ? new File((String) compilerinstalllocation)
				: null;
		String javaversionstr = null;
		Object compilerjreversion = configuration.get(FIELD_COMPILER_JAVAVERSION);
		Object addexports = configuration.get(FIELD_ADDEXPORTS);
		Collection<AddExportsData> addexportscoll = toAddExportsCollection(addexports);

		SourceDirectoriesContainerConfigurationEntry sourcedirentry = new SourceDirectoriesContainerConfigurationEntry();
		ClassPathContainerConfigurationEntry classpathsentry = new ClassPathContainerConfigurationEntry("Classpath");
		ModulePathContainerConfigurationEntry modulepathsentry = new ModulePathContainerConfigurationEntry();
		VMInstallContainerConfigurationEntry jreentry = new VMInstallContainerConfigurationEntry();

		boolean hasbootclasspath = false;

		if (sourcedirs instanceof Collection) {
			Collection<?> sourcedirscoll = (Collection<?>) sourcedirs;
			addSourceDirectories(project, sourcedirentry, sourcedirscoll, currentclasspath);
		}
		if (classpaths instanceof Collection) {
			Collection<?> classpathscoll = (Collection<?>) classpaths;
			addClassPathSourceDirectories(project, sourcedirentry, classpathscoll, currentclasspath);
		}
		//XXX don't add the source directories of the boot classpath? or should we?
//		if (bootclasspaths instanceof Collection) {
//			Collection<?> bootclasspathscoll = (Collection<?>) bootclasspaths;
//		}
		if (gendirs instanceof Collection) {
			Collection<?> gendirscoll = (Collection<?>) gendirs;
			addSourceGenDirectories(project, sourcedirentry, gendirscoll, currentclasspath);
		}
		if (modulepaths instanceof Collection) {
			Collection<?> modulepathscoll = (Collection<?>) modulepaths;
			addModulePaths(project, modulepathsentry, modulepathscoll, currentclasspath);
		}
		if (bootclasspaths instanceof Collection) {
			Collection<?> bootclasspathscoll = (Collection<?>) bootclasspaths;
			hasbootclasspath = !bootclasspathscoll.isEmpty();
			addClassPaths(project, classpathsentry, bootclasspathscoll, currentclasspath);
		}
		if (classpaths instanceof Collection) {
			Collection<?> classpathscoll = (Collection<?>) classpaths;
			addClassPaths(project, classpathsentry, classpathscoll, currentclasspath);
		}
		if (compilerjreversion instanceof String) {
			javaversionstr = (String) compilerjreversion;
		}

		//if a boot classpath was set by the user, don't auto-add the boot classpath for the given JRE
		addVmInstallClassPath(project, jreentry, hasbootclasspath ? null : javaversionstr,
				hasbootclasspath ? null : installlocfile, monitor, currentclasspath, addexportscoll);

		addSourceDirectoryRemovals(project, currentclasspath, sourcedirentry);
		addLibraryRemovals(project, currentclasspath, classpathsentry, modulepathsentry);

		if (!sourcedirentry.isEmpty()) {
			natureentry.addClassPathEntry(sourcedirentry);
			sourcedirentry.initSelection();
		}
		if (!modulepathsentry.isEmpty()) {
			natureentry.addClassPathEntry(modulepathsentry);
			modulepathsentry.initSelection();
		}
		if (!classpathsentry.isEmpty()) {
			natureentry.addClassPathEntry(classpathsentry);
			classpathsentry.initSelection();
		}
		if (jreentry != null) {
			if (!jreentry.isEmpty()) {
				natureentry.addClassPathEntry(jreentry);
				jreentry.initSelection();
			}
		}

		// TODO other java ide configurations
	}

	private static void addLibraryRemovals(ISakerProject project,
			Collection<? extends IClasspathEntry> currentclasspath,
			ClassPathContainerConfigurationEntry classpathsentry,
			ModulePathContainerConfigurationEntry modulepathsentry) {
		for (IClasspathEntry cp : currentclasspath) {
			if (cp.getEntryKind() != IClasspathEntry.CPE_LIBRARY) {
				continue;
			}
			IPath path = cp.getPath();
			IPath projectfullpath = project.getProject().getFullPath();
			boolean projrelative = projectfullpath.isPrefixOf(path);
			String labelpath = projrelative ? path.makeRelativeTo(projectfullpath).toString() : path.toString();
			ClassPathRemovalProjectConfigurationEntry removeentry = new ClassPathRemovalProjectConfigurationEntry(path,
					labelpath);

			String lastsegment = path.lastSegment();
			if (!projrelative) {
				removeentry.setFileNameLabel(lastsegment);
			}
			removeentry.setDirectory(lastsegment != null && !lastsegment.endsWith(".jar"));
			//else if project relative, we can display the full relative path in the entry
			removeentry.setSelected(false);
			if (isModulePathClasspathEntry(cp)) {
				modulepathsentry.addModulePathEntry(removeentry);
				modulepathsentry.addRemovalModulePathEntry(removeentry);
			} else {
				classpathsentry.addClassPathEntry(removeentry);
				classpathsentry.addRemovalClassPathEntry(removeentry);
			}
		}
	}

	private static boolean isModulePathClasspathEntry(IClasspathEntry cp) {
		IClasspathAttribute[] attrs = cp.getExtraAttributes();
		if (attrs == null) {
			return false;
		}
		for (IClasspathAttribute attr : attrs) {
			if (IClasspathAttribute.MODULE.equals(attr.getName())) {
				return Boolean.parseBoolean(attr.getValue());
			}
		}
		return false;
	}

	private static void addSourceDirectoryRemovals(ISakerProject project,
			Collection<? extends IClasspathEntry> currentclasspath,
			SourceDirectoriesContainerConfigurationEntry sourcedirentry) {
		for (IClasspathEntry cp : currentclasspath) {
			if (cp.getEntryKind() != IClasspathEntry.CPE_SOURCE) {
				continue;
			}
			IPath path = cp.getPath();
			IPath projectfullpath = project.getProject().getFullPath();
			SourceDirectoryRemovalProjectConfigurationEntry removeentry = new SourceDirectoryRemovalProjectConfigurationEntry(
					path, path.makeRelativeTo(projectfullpath).toString());

			removeentry.setSelected(false);
			sourcedirentry.addClassPathEntry(removeentry);
			sourcedirentry.addRemovalClassPathEntry(removeentry);
		}
	}

	private static Collection<AddExportsData> toAddExportsCollection(Object addexports) {
		if (!(addexports instanceof Collection)) {
			return Collections.emptySet();
		}
		Set<AddExportsData> result = new LinkedHashSet<>();
		Collection<?> coll = (Collection<?>) addexports;
		for (Object elem : coll) {
			if (!(elem instanceof String)) {
				continue;
			}
			String cmdlineoption = (String) elem;
			int slashidx = cmdlineoption.indexOf('/');
			if (slashidx < 0) {
				continue;
			}

			int eqidx = cmdlineoption.indexOf('=');
			String module = cmdlineoption.substring(0, slashidx);

			String modulepackage;
			Set<String> restarget;
			if (eqidx >= 0) {
				modulepackage = cmdlineoption.substring(slashidx + 1, eqidx);
				String[] target = PATTERN_SPLIT_COMMA.split(cmdlineoption.substring(eqidx + 1));
				restarget = new TreeSet<>();
				for (String t : target) {
					restarget.add(t);
				}
			} else {
				modulepackage = cmdlineoption.substring(slashidx + 1);
				restarget = ALL_UNNAMED_SINGLETON_SET;
			}
			result.add(new AddExportsData(module, modulepackage, restarget));
		}
		return result;
	}

	public static IClasspathEntry getAlreadyPresentClassPathEntryWithPathAndKind(IPath path, int kind,
			Iterable<? extends IClasspathEntry> classpath) {
		for (IClasspathEntry cpentry : classpath) {
			if (cpentry.getEntryKind() != kind) {
				continue;
			}
			if (path.equals(cpentry.getPath())) {
				return cpentry;
			}
		}
		return null;
	}

	private static Map<IVMInstall, IClasspathEntry> getPresentVmClassPaths(
			Collection<? extends IClasspathEntry> currentclasspath) {
		Map<IVMInstall, IClasspathEntry> result = new LinkedHashMap<>();
		for (IClasspathEntry cpentry : currentclasspath) {
			if (cpentry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
				//remove this need to be removed
				IVMInstall classpathvminstall = JavaRuntime.getVMInstall(cpentry.getPath());
				if (classpathvminstall != null) {
					//no need to change
					result.put(classpathvminstall, cpentry);
				}
			}
		}
		return result;
	}

	private static void addVmInstallClassPath(ISakerProject project,
			VMInstallContainerConfigurationEntry containerentry, String javaversionstr, File installlocfile,
			IProgressMonitor monitor, Collection<? extends IClasspathEntry> currentclasspath,
			Collection<AddExportsData> addexportscoll) throws CoreException {
		List<IVMInstall> matchingvminstalls = new ArrayList<>();
		IVMInstallType[] vminstalltypes = JavaRuntime.getVMInstallTypes();
		if (installlocfile != null) {
			filefinder:
			for (IVMInstallType vmitype : vminstalltypes) {
				for (IVMInstall install : vmitype.getVMInstalls()) {
					if (installlocfile.equals(install.getInstallLocation())) {
						matchingvminstalls.add(install);
						//no need to modify JRE classpath
						break filefinder;
					}
				}
			}
		}
		if (javaversionstr != null && matchingvminstalls.isEmpty()) {
			//no exact match found with the install location
			//search by java version
			for (IVMInstallType vmitype : vminstalltypes) {
				for (IVMInstall install : vmitype.getVMInstalls()) {
					if (!isVMInstallHasJavaVersion(install, javaversionstr, monitor)) {
						continue;
					}
					//matching java version for the vm
					matchingvminstalls.add(install);
				}
			}
		}
		Map<IVMInstall, IClasspathEntry> presentvminstalls = getPresentVmClassPaths(currentclasspath);
		boolean ismatchingpresent = false;
		for (IVMInstall vminstall : matchingvminstalls) {
			if (presentvminstalls.containsKey(vminstall)) {
				ismatchingpresent = true;
				break;
			}
		}
		boolean first = true;
		IVMInstall autoselected = null;
		for (IVMInstall vminstall : matchingvminstalls) {
			IClasspathEntry presententry = presentvminstalls.get(vminstall);
			JREClassPathProjectConfigurationEntry nentry = new JREClassPathProjectConfigurationEntry(vminstall,
					addexportscoll, presententry);
			containerentry.addClassPathEntry(nentry);

			if (ismatchingpresent || !first) {
				//if a matching vm is already present in the classpath, don't auto-select the added entries
				nentry.setSelected(false);
			} else {
				autoselected = vminstall;
			}
			first = false;
		}
		for (Entry<IVMInstall, IClasspathEntry> cpentry : presentvminstalls.entrySet()) {
			VMInstallRemovalProjectConfigurationEntry removalentry = new VMInstallRemovalProjectConfigurationEntry(
					cpentry.getValue(), cpentry.getKey());
			containerentry.addClassPathEntry(removalentry);
			containerentry.addRemovalClassPathEntry(removalentry);
			if (cpentry.getKey() == autoselected) {
				//unselect the removal for the already selected one
				removalentry.setSelected(false);
			} else if (autoselected == null) {
				//if there's no auto selected, don't select any of the removal
				removalentry.setSelected(false);
			}
			//else the removal entry stays selected
		}
	}

	private static boolean isVMInstallHasJavaVersion(IVMInstall install, String javaversionstr,
			IProgressMonitor monitor) throws CoreException {
		if (javaversionstr == null) {
			return false;
		}
		if (install instanceof IVMInstall2) {
			IVMInstall2 install2 = (IVMInstall2) install;
			String version = install2.getJavaVersion();
			if (javaversionstr.equals(version)) {
				//found vm
				return true;
			}
			//it can happen, that the getJavaVersion method returns 1.8.0, while the java version 
			//    property from the IDE configuration is 1.8.0_221 or similar
		}
		if (install instanceof IVMInstall3) {
			//XXX handle CoreException here?
			Map<String, String> evalprops = ((IVMInstall3) install)
					.evaluateSystemProperties(new String[] { "java.version" }, monitor);
			if (evalprops != null && javaversionstr.equals(evalprops.get("java.version"))) {
				return true;
			}
		}
		return false;
	}

	private static void addModulePaths(ISakerProject sakerproject,
			ModulePathContainerConfigurationEntry modulepathsentry, Collection<?> modulepaths,
			Collection<? extends IClasspathEntry> currentclasspath) {
		for (Object cpobj : modulepaths) {
			if (!(cpobj instanceof Map)) {
				continue;
			}
			Map<?, ?> mapobj = (Map<?, ?>) cpobj;
			Object cppath = getMapField(mapobj, FIELD_CLASSPATH_PATH);
			if (cppath instanceof String) {
				addExecutionModulePath(sakerproject, modulepathsentry, currentclasspath, mapobj, (String) cppath);
			} else {
				cppath = getMapField(mapobj, FIELD_CLASSPATH_LOCAL_PATH);
				if (cppath instanceof String) {
					addLocalModulePath(sakerproject, modulepathsentry, currentclasspath, mapobj, (String) cppath);
				}
			}
		}
	}

	private static void addClassPaths(ISakerProject sakerproject, ClassPathContainerConfigurationEntry classpathsentry,
			Collection<?> classpaths, Collection<? extends IClasspathEntry> currentclasspath) {
		for (Object cpobj : classpaths) {
			if (!(cpobj instanceof Map)) {
				continue;
			}
			Map<?, ?> mapobj = (Map<?, ?>) cpobj;
			Object sourcedirsobj = getMapField(mapobj, FIELD_CLASSPATH_SOURCEDIRECTORIES);
			//if the class path is not a jar, (e.g. bin output directory) then only auto select it if there are not associated source directories
			//    it is usually better to add a classpath directly as sources rather than the out for better IDE experience
			boolean autoselectnonjarclasspath = !(sourcedirsobj instanceof Collection)
					|| ((Collection<?>) sourcedirsobj).isEmpty();
			Object cppath = getMapField(mapobj, FIELD_CLASSPATH_PATH);
			if (cppath instanceof String) {
				addExecutionClassPath(sakerproject, classpathsentry, currentclasspath, mapobj, (String) cppath,
						autoselectnonjarclasspath);
			} else {
				cppath = getMapField(mapobj, FIELD_CLASSPATH_LOCAL_PATH);
				if (cppath instanceof String) {
					addLocalClassPath(sakerproject, classpathsentry, currentclasspath, mapobj, (String) cppath,
							autoselectnonjarclasspath);
				}
			}
		}
	}

	private static void addClassPathSourceDirectories(ISakerProject sakerproject,
			SourceDirectoriesContainerConfigurationEntry sourcedirentry, Collection<?> classpaths,
			Collection<? extends IClasspathEntry> currentclasspath) {
		for (Object cpobj : classpaths) {
			if (!(cpobj instanceof Map)) {
				continue;
			}
			Map<?, ?> mapobj = (Map<?, ?>) cpobj;
			Object cpsourcedirs = getMapField(mapobj, FIELD_CLASSPATH_SOURCEDIRECTORIES);
			if (cpsourcedirs instanceof Collection) {
				Collection<?> cpsourcedirscoll = (Collection<?>) cpsourcedirs;
				addSourceDirectories(sakerproject, sourcedirentry, cpsourcedirscoll, currentclasspath);
			}
		}
	}

	private static void addLocalClassPath(ISakerProject sakerproject,
			ClassPathContainerConfigurationEntry classpathsentry,
			Collection<? extends IClasspathEntry> currentclasspath, Map<?, ?> mapobj, String cppath,
			boolean autoselectnonjarclasspath) {
		IPath path;
		try {
			path = new Path(Paths.get(cppath).toRealPath(LinkOption.NOFOLLOW_LINKS).toString());
		} catch (IOException e) {
			return;
		}

		ClassPathProjectConfigurationEntry addconfigentry = new ClassPathProjectConfigurationEntry(path, cppath);
		addconfigentry.setFileNameLabel(path.lastSegment());
		boolean isjar = cppath.endsWith(".jar");
		addconfigentry.setDirectory(!isjar);

		if (!isjar && !autoselectnonjarclasspath) {
			addconfigentry.setSelected(false);
		}

		addconfigentry.setSourceAttachmentEntry(handleSourceAttachment(sakerproject, mapobj));

		if (getAlreadyPresentClassPathEntryWithPathAndKind(path, IClasspathEntry.CPE_LIBRARY,
				currentclasspath) != null) {
			//as it is already present, unselect by default
			addconfigentry.setSelected(false);
		}
		classpathsentry.addClassPathEntry(addconfigentry);
	}

	private static void addExecutionClassPath(ISakerProject sakerproject,
			ClassPathContainerConfigurationEntry classpathsentry,
			Collection<? extends IClasspathEntry> currentclasspath, Map<?, ?> mapobj, String cppath,
			boolean autoselectnonjarclasspath) {
		String projectrelativepath = sakerproject.executionPathToProjectRelativePath(cppath);
		if (projectrelativepath == null) {
			//TODO convert the path to a potentially local path and add that
			return;
		}
		IProject project = sakerproject.getProject();
		IPath path = new Path(projectrelativepath);
		if (!path.isAbsolute()) {
			path = project.getFullPath().append(projectrelativepath);
		}

		//XXX ignore-case extension check
		ClassPathProjectConfigurationEntry addconfigentry = new ClassPathProjectConfigurationEntry(path,
				projectrelativepath);
		boolean isjar = cppath.endsWith(".jar");
		addconfigentry.setDirectory(!isjar);

		if (!isjar && !autoselectnonjarclasspath) {
			addconfigentry.setSelected(false);
		}

		addconfigentry.setSourceAttachmentEntry(handleSourceAttachment(sakerproject, mapobj));

		if (getAlreadyPresentClassPathEntryWithPathAndKind(path, IClasspathEntry.CPE_LIBRARY,
				currentclasspath) != null) {
			//as it is already present, unselect by default
			addconfigentry.setSelected(false);
		}
		classpathsentry.addClassPathEntry(addconfigentry);
	}

	private static void addLocalModulePath(ISakerProject sakerproject,
			ModulePathContainerConfigurationEntry classpathsentry,
			Collection<? extends IClasspathEntry> currentclasspath, Map<?, ?> mapobj, String cppath) {
		IPath path;
		try {
			path = new Path(Paths.get(cppath).toRealPath(LinkOption.NOFOLLOW_LINKS).toString());
		} catch (IOException e) {
			return;
		}

		ModulePathProjectConfigurationEntry addconfigentry = new ModulePathProjectConfigurationEntry(path, cppath);
		addconfigentry.setFileNameLabel(path.lastSegment());
		addconfigentry.setDirectory(!cppath.endsWith(".jar"));

		addconfigentry.setSourceAttachmentEntry(handleSourceAttachment(sakerproject, mapobj));

		if (getAlreadyPresentClassPathEntryWithPathAndKind(path, IClasspathEntry.CPE_LIBRARY,
				currentclasspath) != null) {
			//as it is already present, unselect by default
			addconfigentry.setSelected(false);
		}
		classpathsentry.addModulePathEntry(addconfigentry);
	}

	private static void addExecutionModulePath(ISakerProject sakerproject,
			ModulePathContainerConfigurationEntry classpathsentry,
			Collection<? extends IClasspathEntry> currentclasspath, Map<?, ?> mapobj, String cppath) {
		String projectrelativepath = sakerproject.executionPathToProjectRelativePath(cppath);
		if (projectrelativepath == null) {
			//TODO convert the path to a potentially local path and add that
			return;
		}
		IProject project = sakerproject.getProject();
		IPath path = new Path(projectrelativepath);
		if (!path.isAbsolute()) {
			path = project.getFullPath().append(projectrelativepath);
		}

		//XXX ignore-case extension check
		ModulePathProjectConfigurationEntry addconfigentry = new ModulePathProjectConfigurationEntry(path,
				projectrelativepath);
		addconfigentry.setDirectory(!cppath.endsWith(".jar"));
		//don't set the file name label for project relative classpath

		addconfigentry.setSourceAttachmentEntry(handleSourceAttachment(sakerproject, mapobj));

		if (getAlreadyPresentClassPathEntryWithPathAndKind(path, IClasspathEntry.CPE_LIBRARY,
				currentclasspath) != null) {
			//as it is already present, unselect by default
			addconfigentry.setSelected(false);
		}
		classpathsentry.addModulePathEntry(addconfigentry);
	}

	private static ClassPathSourceAttachmentConfigurationEntry handleSourceAttachment(ISakerProject sakerproject,
			Map<?, ?> mapobj) {
		IPath srcpath;
		String displaylabel;
		Object sourcepath = getMapField(mapobj, FIELD_CLASSPATH_SOURCEPATH);
		if (sourcepath instanceof String) {
			String srcprojectrelativepath = sakerproject.executionPathToProjectRelativePath((String) sourcepath);
			if (srcprojectrelativepath != null) {
				srcpath = new Path(srcprojectrelativepath);
				if (!srcpath.isAbsolute()) {
					IProject project = sakerproject.getProject();
					srcpath = project.getFullPath().append(srcprojectrelativepath);
				}
				displaylabel = srcprojectrelativepath;
			} else {
				//cannot convert to project relative path
				return null;
			}
		} else {
			sourcepath = getMapField(mapobj, FIELD_CLASSPATH_LOCAL_SOURCEPATH);
			if (sourcepath instanceof String) {
				String sourcepathstr = (String) sourcepath;

				try {
					srcpath = new Path(Paths.get(sourcepathstr).toRealPath(LinkOption.NOFOLLOW_LINKS).toString());
				} catch (IOException e) {
					return null;
				}

				displaylabel = sourcepathstr;
			} else {
				//no source found.
				return null;
			}
		}
		ClassPathSourceAttachmentConfigurationEntry sourceattachmententry = new ClassPathSourceAttachmentConfigurationEntry(
				srcpath, displaylabel);
		sourceattachmententry.setFileNameLabel(srcpath.lastSegment());
		return sourceattachmententry;
	}

	private static void addSourceDirectories(ISakerProject sakerproject,
			SourceDirectoriesContainerConfigurationEntry sourcedirentry, Collection<?> sourcedirs,
			Collection<? extends IClasspathEntry> currentclasspath) {
		for (Object dirobj : sourcedirs) {
			if (!(dirobj instanceof Map)) {
				continue;
			}
			Map<?, ?> mapobj = (Map<?, ?>) dirobj;
			Object dirpath = getMapField(mapobj, FIELD_SOURCEDIRECTORY_PATH);
			if (!(dirpath instanceof String)) {
				continue;
			}
			//XXX handle files wildcards
			String projectrelativepath = sakerproject.executionPathToProjectRelativePath((String) dirpath);
			if (projectrelativepath != null) {
				IProject project = sakerproject.getProject();
				IPath path = new Path(projectrelativepath);
				if (!path.isAbsolute()) {
					path = project.getFullPath().append(projectrelativepath);
				}
				SourceDirectoryProjectConfigurationEntry addconfigentry = new SourceDirectoryProjectConfigurationEntry(
						path, projectrelativepath);
				sourcedirentry.addClassPathEntry(addconfigentry);
				if (getAlreadyPresentClassPathEntryWithPathAndKind(path, IClasspathEntry.CPE_SOURCE,
						currentclasspath) != null) {
					//as it is already present, unselect by default
					addconfigentry.setSelected(false);
				}
			}
		}
	}

	private static void addSourceGenDirectories(ISakerProject sakerproject,
			SourceDirectoriesContainerConfigurationEntry sourcedirentry, Collection<?> gendirscoll,
			Collection<? extends IClasspathEntry> currentclasspath) {
		for (Object dirobj : gendirscoll) {
			if (!(dirobj instanceof String)) {
				continue;
			}
			String projectrelativepath = sakerproject.executionPathToProjectRelativePath((String) dirobj);
			if (projectrelativepath != null) {
				IProject project = sakerproject.getProject();
				IPath path = new Path(projectrelativepath);
				if (!path.isAbsolute()) {
					path = project.getFullPath().append(projectrelativepath);
				}
				SourceDirectoryProjectConfigurationEntry addconfigentry = new SourceDirectoryProjectConfigurationEntry(
						path, projectrelativepath);
				sourcedirentry.addClassPathEntry(addconfigentry);
				if (getAlreadyPresentClassPathEntryWithPathAndKind(path, IClasspathEntry.CPE_SOURCE,
						currentclasspath) != null) {
					//as it is already present, unselect by default
					addconfigentry.setSelected(false);
				}
			}
		}
	}

	private static <V> V getMapField(Map<?, V> mapobj, String field) {
		try {
			return mapobj.get(field);
		} catch (RuntimeException e) {
			//in case of exceptions, if the map cannot accept string key
			return null;
		}
	}

	public static ImageDescriptor getComposedImageDescriptor(ImageDescriptor main, ImageDescriptor sub) {
		if (main == null) {
			if (sub == null) {
				return null;
			}
			return sub;
		}
		if (sub == null) {
			return main;
		}
		return new CompositeImageDescriptor() {
			@Override
			protected Point getSize() {
				CachedImageDataProvider libimg = createCachedImageDataProvider(main);
				return new Point(libimg.getWidth(), libimg.getHeight());
			}

			@Override
			protected void drawCompositeImage(int width, int height) {
				drawImage(createCachedImageDataProvider(main), 0, 0);
				drawImage(createCachedImageDataProvider(sub), 0, 0);
			}
		};
	}

	public static Image getComposedImage(ImageDescriptor main, ImageDescriptor sub) {
		ImageDescriptor descriptor = getComposedImageDescriptor(main, sub);
		if (descriptor == null) {
			return null;
		}
		return descriptor.createImage(false);
	}

	public static Image getImageFromPlugin(String pluginId, String imageFilePath) {
		ImageDescriptor descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(pluginId, imageFilePath);
		if (descriptor == null) {
			return null;
		}
		return descriptor.createImage(false);
	}

}
