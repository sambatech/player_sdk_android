package com.sambatech.player.utils;

import com.sambatech.player.model.SambaMedia;

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

	//Order ouputs
	public static class CustomSorter implements Comparator<SambaMedia.Outputs> {

		@Override
		public int compare(SambaMedia.Outputs lhs, SambaMedia.Outputs rhs) {
			return lhs.position - rhs.position;
		}
	}
}

