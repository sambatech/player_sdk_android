package com.sambatech.player.cast;

import org.json.JSONObject;


public class CastQuery {
	
    private boolean html5;
    private String scriptURL;
    private String castApi;
    private String castAppId;
    private String logger;
    private double initialTime;
    private String captionTheme;


	public CastQuery() {
		
	}	
        
    public CastQuery(JSONObject json) {
    
        this.html5 = json.optBoolean("html5");
        this.scriptURL = json.optString("scriptURL");
        this.castApi = json.optString("castApi");
        this.castAppId = json.optString("castAppId");
        this.logger = json.optString("logger");
        this.initialTime = json.optDouble("initialTime");
        this.captionTheme = json.optString("captionTheme");
    }
    
    public boolean getHtml5() {
        return this.html5;
    }

    public void setHtml5(boolean html5) {
        this.html5 = html5;
    }

    public String getScriptURL() {
        return this.scriptURL;
    }

    public void setScriptURL(String scriptURL) {
        this.scriptURL = scriptURL;
    }

    public String getCastApi() {
        return this.castApi;
    }

    public void setCastApi(String castApi) {
        this.castApi = castApi;
    }

    public String getCastAppId() {
        return this.castAppId;
    }

    public void setCastAppId(String castAppId) {
        this.castAppId = castAppId;
    }

    public String getLogger() {
        return this.logger;
    }

    public void setLogger(String logger) {
        this.logger = logger;
    }

    public double getInitialTime() {
        return this.initialTime;
    }

    public void setInitialTime(double initialTime) {
        this.initialTime = initialTime;
    }


    public String getCaptionTheme() {
        return captionTheme;
    }

    public void setCaptionTheme(String captionTheme) {
        this.captionTheme = captionTheme;
    }

    @Override
    public String toString() {
        return "{" +
                "\"html5\":" + html5 +
                /*",\"scriptURL\":\"" + scriptURL + '\"' +*/
                ",\"castApi\":\"" + castApi + '\"' +
                ",\"castAppId\":\"" + castAppId + '\"' +
                ",\"captionTheme\":\"" + captionTheme + '\"' +
                ",\"initialTime\":" + initialTime +
                '}';
    }

    public CastQuery(boolean html5, /*String scriptURL,*/ String castApi, String castAppId, double initialTime, String captionTheme) {
        this.html5 = html5;
        //this.scriptURL = scriptURL;
        this.castApi = castApi;
        this.castAppId = castAppId;
        this.initialTime = initialTime;
        this.captionTheme = captionTheme;
    }
}
