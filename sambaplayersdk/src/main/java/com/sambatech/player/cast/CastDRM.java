package com.sambatech.player.cast;

import org.json.JSONObject;

public class CastDRM {

    private String sessionId;
    private String ticket;
    private String token;


    public CastDRM () {

    }

    public CastDRM (JSONObject json) {

        this.sessionId = json.optString("SessionId");
        this.ticket = json.optString("Ticket");
    }

    public CastDRM(String sessionId, String ticket) {
        this.sessionId = sessionId;
        this.ticket = ticket;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    @Override
    public String toString() {
        return "{" +
                "\"SessionId\":\"" + sessionId + '\"' +
                ", \"Ticket\":\"" + ticket + '\"' +
                (token != null ? ", \"Token\":\"" + token + '\"' : "") +
                '}';
    }
}
