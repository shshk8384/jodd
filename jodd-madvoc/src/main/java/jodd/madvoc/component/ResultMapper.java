// Copyright (c) 2003-2014, Jodd Team (jodd.org). All Rights Reserved.

package jodd.madvoc.component;

import jodd.madvoc.ActionConfig;
import jodd.madvoc.ResultPath;
import jodd.petite.meta.PetiteInject;
import jodd.madvoc.MadvocUtil;
import jodd.util.StringUtil;

/**
 * Mapper from action results paths to result path. Certain set of results
 * defines path where to forward/redirect etc. This mapper converts
 * result path to real path. Result path may contain some macros that
 * will be resolved. Here are the macros that can be used:
 * <ul>
 *     <li>&lt;alias&gt; - replaced with alias value</li>
 *     <li># - strips words from path (goes 'back')</li>
 * </ul>
 */
public class ResultMapper {

	@PetiteInject
	protected ActionsManager actionsManager;

	@PetiteInject
	protected MadvocConfig madvocConfig;

	/**
	 * Lookups value as an alias and, if not found, as a default alias.
	 */
	protected String lookupAlias(String alias) {
		String value = actionsManager.lookupPathAlias(alias);
		if (value == null) {
			ActionConfig cfg = actionsManager.lookup(alias);
			if (cfg != null) {
				value = cfg.actionPath;
			}
		}
		return value;
	}

	/**
	 * Returns resolved alias result value or passed on, if alias doesn't exist.
	 */
	protected String resolveAlias(String value) {
		StringBuilder result = new StringBuilder(value.length());
		int i = 0;
		int len = value.length();
		while (i < len) {
			int ndx = value.indexOf('<', i);
			if (ndx == -1) {
				// alias markers not found
				if (i == 0) {
					// try whole string as an alias
					String alias = lookupAlias(value);
					return (alias != null ? alias : value);
				} else {
					result.append(value.substring(i));
				}
				break;
			}

			// alias marked found
			result.append(value.substring(i, ndx));
			ndx++;
			int ndx2 = value.indexOf('>', ndx);
			String alias = (ndx2 == -1 ? value.substring(ndx) : value.substring(ndx, ndx2));

			// process alias
			alias = lookupAlias(alias);
			if (alias != null) {
				result.append(alias);
			}
			i = ndx2 + 1;
		}

		// fix prefix '//' - may happened when aliases are used
		i = 0; len = result.length();
		while (i < len) {
			if (result.charAt(i) != '/') {
				break;
			}
			i++;
		}
		if (i > 1) {
			return result.substring(i - 1, len);
		}
		return result.toString();
	}

	/**
	 * Resolves result path.
	 */
	public ResultPath resolveResultPath(String path, String value) {

		boolean absolutePath = false;

		if (value != null) {
			// [*] resolve alias in value
			value = resolveAlias(value);

			// [*] absolute paths
			if (StringUtil.startsWithChar(value, '/')) {
				absolutePath = true;
				int dotNdx = value.indexOf("..");
				if (dotNdx != -1) {
					path = value.substring(0, dotNdx);
					value = value.substring(dotNdx + 2);
				} else {
					path = value;
					value = null;
				}
			} else {
				// [*] resolve # in value and path
				int i = 0;
				while (i < value.length()) {
					if (value.charAt(i) != '#') {
						break;
					}
					int dotNdx = MadvocUtil.lastIndexOfSlashDot(path);
					if (dotNdx != -1) {
						// dot found
						path = path.substring(0, dotNdx);
					}
					i++;
				}
				if (i > 0) {
					// remove # from value
					value = value.substring(i);

					// [*] update path and value

					if (StringUtil.startsWithChar(value, '.')) {
						value = value.substring(1);
					} else {
						int dotNdx = value.indexOf("..");
						if (dotNdx != -1) {
							path += '.' + value.substring(0, dotNdx);
							value = value.substring(dotNdx + 2);
						} else {
							if (value.length() > 0) {
								if (StringUtil.endsWithChar(path, '/')) {
									path += value;
								} else {
									path += '.' + value;
								}
							}
							value = null;
						}
					}
				}
			}
		}

		if (!absolutePath) {
			String resultPathPrefix = madvocConfig.getResultPathPrefix();
			if (resultPathPrefix != null) {
				path = resultPathPrefix + path;
			}
		}

		return new ResultPath(path, value);
	}

	/**
	 * Resolves result path as a string, when parts are not important
	 * and when only full string matters. Additional alias resolving
	 * on full path is done.
	 */
	public String resolveResultPathString(String path, String value) {
		ResultPath resultPath = resolveResultPath(path, value);
		String result = resultPath.getPathValue();

		return resolveAlias(result);
	}

}