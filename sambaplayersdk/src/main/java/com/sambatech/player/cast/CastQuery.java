package com.sambatech.player.cast;

import org.json.*;


public class CastQuery {
	
    private boolean html5;
    private String scriptURL;
    private String castApi;
    private String castAppId;
    private String logger;
    private double initialTime;


	public CastQuery() {
		
	}	
        
    public CastQuery(JSONObject json) {
    
        this.html5 = json.optBoolean("html5");
        this.scriptURL = json.optString("scriptURL");
        this.castApi = json.optString("castApi");
        this.castAppId = json.optString("castAppId");
        this.logger = json.optString("logger");
        this.initialTime = json.optDouble("initialTime");

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

    @Override
    public String toString() {
        return "{" +
                "\"html5\":" + html5 +
                /*",\"scriptURL\":\"" + scriptURL + '\"' +*/
                ",\"castApi\":\"" + castApi + '\"' +
                ",\"castAppId\":\"" + castAppId + '\"' +
                ",\"logger\":\"" + logger + '\"' +
                ",\"initialTime\":" + initialTime +
                '}';
    }

    public CastQuery(boolean html5, /*String scriptURL,*/ String castApi, String castAppId, String logger, double initialTime) {
        this.html5 = html5;
        //this.scriptURL = scriptURL;
        this.castApi = castApi;
        this.castAppId = castAppId;
        this.logger = logger;
        this.initialTime = initialTime;
    }
}
