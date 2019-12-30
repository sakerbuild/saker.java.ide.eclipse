package saker.java.ide.eclipse;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public class AddExportsData {
	public final String module;
	public final String pack;
	public final Set<String> target;

	public AddExportsData(String module, String pack, Set<String> target) {
		this.module = module;
		this.pack = pack;
		this.target = target;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((module == null) ? 0 : module.hashCode());
		result = prime * result + ((pack == null) ? 0 : pack.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AddExportsData other = (AddExportsData) obj;
		if (module == null) {
			if (other.module != null)
				return false;
		} else if (!module.equals(other.module))
			return false;
		if (pack == null) {
			if (other.pack != null)
				return false;
		} else if (!pack.equals(other.pack))
			return false;
		if (target == null) {
			if (other.target != null)
				return false;
		} else if (!target.equals(other.target))
			return false;
		return true;
	}

	@Override
	public String toString() {
		//don't modify, keep this as the command line representation
		return module + "/" + pack + "=" + String.join(",", target);
	}

	public Collection<String> toCommandLineStrings() {
		Collection<String> result = new TreeSet<>();
		for (String targetstr : target) {
			result.add(module + "/" + pack + "=" + targetstr);
		}
		return result;
	}
}
