package com.sambatech.player.cast;

import com.sambatech.player.R;
import com.sambatech.player.model.SambaMediaRequest;

import org.json.*;


public class CastObject {
	
    private String title;
    private String m;
    private String live;
    private long duration;
    private String theme;
    private String ph;
    private CastQuery qs;
    private String thumbURL;
    private String baseURL;
    private CastDRM drm;


	public CastObject () {
		
	}	
        
    public CastObject (JSONObject json) {
    
        this.title = json.optString("title");
        this.m = json.optString("m");
        this.live = json.optString("live");
        this.duration = json.optLong("duration");
        this.theme = json.optString("theme");
        this.ph = json.optString("ph");
        this.qs = new CastQuery(json.optJSONObject("qs"));
        this.thumbURL = json.optString("thumbURL");
        this.baseURL = json.optString("baseURL");

    }

    public CastObject(String title, String m, long duration, String theme, String ph, CastQuery qs, String thumbURL, String baseURL) {
        this.title = title;
        this.m = m;
        this.duration = duration;
        this.theme = theme;
        this.ph = ph;
        this.qs = qs;
        this.thumbURL = thumbURL;
        this.baseURL = baseURL;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getM() {
        return this.m;
    }

    public void setM(String m) {
        this.m = m;
    }

    public long getDuration() {
        return this.duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getTheme() {
        return this.theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getPh() {
        return this.ph;
    }

    public void setPh(String ph) {
        this.ph = ph;
    }

    public CastQuery getQs() {
        return this.qs;
    }

    public void setQs(CastQuery qs) {
        this.qs = qs;
    }

    public String getThumbURL() {
        return this.thumbURL;
    }

    public void setThumbURL(String thumbURL) {
        this.thumbURL = thumbURL;
    }

    public String getBaseURL() {
        return this.baseURL;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public void setDrm(CastDRM drm) {
        this.drm = drm;
    }

    public CastDRM getDrm() {
        return drm;
    }

    public String getLive() {
        return live;
    }

    public void setLive(String live) {
        this.live = live;
    }

    @Override
    public String toString() {
        return "{" +
                "\"title\":\"" + title + '\"' +
                ", \"m\":\"" + m + '\"' +
                (live != null && !live.isEmpty() ? ", \"live\":\"" + live + '\"' : "") +
                ", \"duration\":" + duration +
                ", \"theme\":\"" + theme + '\"' +
                ", \"ph\":\"" + ph + '\"' +
                ", \"qs\":" + qs +
                ", \"thumbURL\":\"" + thumbURL + '\"' +
                ", \"baseURL\":\"" + baseURL + '\"' +
                ", \"drm\":" + drm +
                '}';
    }
}
