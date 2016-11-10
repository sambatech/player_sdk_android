package com.sambatech.player.utils;

import com.sambatech.player.model.SambaMedia;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Comparator;

/**
 * Useful stuff for whole SambaPlayer project.
 *
 * @author Leandro Zanol - 29/12/15
 */
public final class Helpers {

	public static String getSessionId() {
		StringBuilder s = new StringBuilder();

		for (int i = 8; i-- > 0;) {
			s.append(getSessionComponent());

			switch (i) {
				case 1:
				case 3:
				case 5:
				case 7:
					s.append("-");
					break;
			}
		}

		return s.toString();
	}

	private static String getSessionComponent() {
		return Integer.toHexString((int)((Math.random() + 1) * 0x10000)).substring(1);
	}

	public static boolean isDeviceRooted() {
		return RootUtil.isDeviceRooted();
	}

	//Order outputs
	public static class CustomSorter implements Comparator<SambaMedia.Output> {

		@Override
		public int compare(SambaMedia.Output lhs, SambaMedia.Output rhs) {
			return lhs.position - rhs.position;
		}
	}
}

/** @author Kevin Kowalewski */
class RootUtil {
	public static boolean isDeviceRooted() {
		return checkRootMethod1() || checkRootMethod2() || checkRootMethod3();
	}

	private static boolean checkRootMethod1() {
		String buildTags = android.os.Build.TAGS;
		return buildTags != null && buildTags.contains("test-keys");
	}

	private static boolean checkRootMethod2() {
		String[] paths = { "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
				"/system/bin/failsafe/su", "/data/local/su", "/su/bin/su"};
		for (String path : paths) {
			if (new File(path).exists()) return true;
		}
		return false;
	}

	private static boolean checkRootMethod3() {
		Process process = null;
		try {
			process = Runtime.getRuntime().exec(new String[] { "/system/xbin/which", "su" });
			BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
			if (in.readLine() != null) return true;
			return false;
		} catch (Throwable t) {
			return false;
		} finally {
			if (process != null) process.destroy();
		}
	}
}
