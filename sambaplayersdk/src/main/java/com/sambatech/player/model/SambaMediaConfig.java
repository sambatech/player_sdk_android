package com.sambatech.player.model;


import com.google.android.exoplayer2.util.UriUtil;
import com.sambatech.player.plugins.DrmRequest;
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
	public String downloadUrl;
	public String qualifier;
	public int projectId;
	public  int clientId;
	public int categoryId;
	public String sessionId = Helpers.getSessionId();
	public int themeColor = 0xFF72BE44;
	public String themeColorHex = "#72BE44";
	public String sttmUrl;
	public String sttmKey;
	public String sttm2Url;
	public String sttm2Key;
	public SambaMediaRequest request;
	public DrmRequest drmRequest;
	public boolean blockIfRooted;
	public int retriesTotal = 3;
	public int defaultOutputIndex = 0;
	public boolean isOffline;
	public long bitrate;

	public SambaMediaConfig() {}

	public SambaMediaConfig(SambaMedia media) {
		super(media);

		if (media instanceof SambaMediaConfig) {
			SambaMediaConfig m = (SambaMediaConfig)media;
			id = m.id;
			projectHash = m.projectHash;
			qualifier = m.qualifier;
			projectId = m.projectId;
			clientId = m.clientId;
			categoryId = m.categoryId;
			sessionId = m.sessionId;
			themeColor = m.themeColor;
			themeColorHex = m.themeColorHex;
			sttmUrl = m.sttmUrl;
			sttmKey = m.sttmKey;
			sttm2Url = m.sttm2Url;
			sttm2Key = m.sttm2Key;
			request = m.request;
			drmRequest = m.drmRequest;
			// TODO: Reabilitar após correção (https://github.com/sambatech-desenv/player/issues/589)
			//blockIfRooted = m.blockIfRooted;
			retriesTotal = m.retriesTotal;
			downloadUrl = m.downloadUrl;
			isOffline = m.isOffline;
			bitrate = m.bitrate;
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
