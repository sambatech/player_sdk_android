package com.sambatech.player.cast;

import com.sambatech.player.R;
import com.sambatech.player.model.SambaMediaRequest;

import org.json.*;


public class CastObject {
	
    private String title;
    private String m;
    private double duration;
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
        this.duration = json.optDouble("duration");
        this.theme = json.optString("theme");
        this.ph = json.optString("ph");
        this.qs = new CastQuery(json.optJSONObject("qs"));
        this.thumbURL = json.optString("thumbURL");
        this.baseURL = json.optString("baseURL");

    }

    public CastObject(String title, String m, double duration, String theme, String ph, CastQuery qs, String thumbURL, String baseURL) {
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

    public double getDuration() {
        return this.duration;
    }

    public void setDuration(double duration) {
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



    @Override
    public String toString() {
        return "{" +
                "\"title\":\"" + title + '\"' +
                ", \"m\":\"" + m + '\"' +
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
