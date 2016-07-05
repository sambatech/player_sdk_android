package com.sambatech.player.model;

import com.sambatech.player.utils.Helpers;

import java.lang.reflect.Field;

/**
 * Internal extension of the media entity for player/plugins config purposes.
 *
 * @author Leandro Zanol - 2/12/15
 */
public class SambaMediaConfig extends SambaMedia {

	public String id;
	public String projectHash;
	public String qualifier;
	public int projectId;
	public int categoryId;
	public String sessionId = Helpers.getSessionId();
	public int themeColor = 0xFF72BE44;
	public String sttmUrl;
	public String sttmKey;

	public SambaMediaConfig() {}

	public SambaMediaConfig(SambaMedia media) {
		super(media);
		if(media instanceof SambaMediaConfig) {
			SambaMediaConfig m = (SambaMediaConfig)media;
			id = m.id;
			projectHash = m.projectHash;
			qualifier = m.qualifier;
			projectId = m.projectId;
			categoryId = m.categoryId;
			themeColor = m.themeColor;
			sttmKey = m.sttmKey;
			sttmUrl = m.sttmUrl;
		}
	}

	@Override
	public String toString() {
		String desc = "";
		Field[] fields = getClass().getSuperclass().getDeclaredFields();

		try {
			for (Field field : fields)
				desc += field.getName() + ": " + field.get(this) + '\n';
		}
		catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return desc;
	}
}
