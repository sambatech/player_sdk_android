package com.sambatech.sample.model;

/**
 * @author Thiago Miranda - 12/01/16
 */
public class LiquidMedia implements Cloneable {

    private String ph;
	private String id;
	private String liveChannelId;
	private String title;
	private String description;
	private String env;
	private String type;
	private String qualifier;
	private String thumbnail;
	private String[] backupUrls;
	private boolean drm;
	private boolean highlighted;
	private Params params;

	public String getPh() {
		return ph;
	}

	public void setPh(String ph) {
		this.ph = ph;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLiveChannelId() {
		return liveChannelId;
	}

	public void setLiveChannelId(String liveChannelId) {
		this.liveChannelId = liveChannelId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getEnv() {
		return env;
	}

	public void setEnv(String env) {
		this.env = env;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getQualifier() {
		return qualifier;
	}

	public void setQualifier(String qualifier) {
		this.qualifier = qualifier;
	}

	public String getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}

	public String[] getBackupUrls() {
		return backupUrls;
	}

	public void setBackupUrls(String[] backupUrls) {
		this.backupUrls = backupUrls;
	}

	public boolean isDrm() {
		return drm;
	}

	public void setDrm(boolean drm) {
		this.drm = drm;
	}

	public boolean isHighlighted() {
		return highlighted;
	}

	public void setHighlighted(boolean highlighted) {
		this.highlighted = highlighted;
	}

	public Params getParams() {
		return params;
	}

	public void setParams(Params params) {
		this.params = params;
	}

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

    public static class Params {

	    private String title;
	    private boolean autoStart;
	    private String streamName;
	    private String primaryLive;
	    private String backupLive;
	    private String type;
	    private String gaAccount;
	    private int volume;
	    private boolean hasFacebook;
	    private boolean hasTwitter;
	    private String ad_program;
	    private String thumbnailURL;
	    private boolean enableControls = true;

	    public String getTitle() {
		    return title;
	    }

	    public void setTitle(String title) {
		    this.title = title;
	    }

	    public boolean getAutoStart() {
		    return autoStart;
	    }

	    public void setAutoStart(boolean autoStart) {
		    this.autoStart = autoStart;
	    }

	    public String getStreamName() {
		    return streamName;
	    }

	    public void setStreamName(String streamName) {
		    this.streamName = streamName;
	    }

	    public String getPrimaryLive() {
		    return primaryLive;
	    }

	    public void setPrimaryLive(String primaryLive) {
		    this.primaryLive = primaryLive;
	    }

	    public String getBackupLive() {
		    return backupLive;
	    }

	    public void setBackupLive(String backupLive) {
		    this.backupLive = backupLive;
	    }

	    public String getType() {
		    return type;
	    }

	    public void setType(String type) {
		    this.type = type;
	    }

	    public String getGaAccount() {
		    return gaAccount;
	    }

	    public void setGaAccount(String gaAccount) {
		    this.gaAccount = gaAccount;
	    }

	    public int getVolume() {
		    return volume;
	    }

	    public void setVolume(int volume) {
		    this.volume = volume;
	    }

	    public boolean isHasFacebook() {
		    return hasFacebook;
	    }

	    public void setHasFacebook(boolean hasFacebook) {
		    this.hasFacebook = hasFacebook;
	    }

	    public boolean isHasTwitter() {
		    return hasTwitter;
	    }

	    public void setHasTwitter(boolean hasTwitter) {
		    this.hasTwitter = hasTwitter;
	    }

	    public String getAd_program() {
		    return ad_program;
	    }

	    public void setAd_program(String ad_program) {
		    this.ad_program = ad_program;
	    }

	    public String getThumbnailURL() {
		    return thumbnailURL;
	    }

	    public void setThumbnailURL(String thumbnailURL) {
		    this.thumbnailURL = thumbnailURL;
	    }

	    public boolean getEnableControls() {
		    return enableControls;
	    }

	    public boolean isEnableControls() {
		    return enableControls;
	    }

	    public void setEnableControls(boolean enableControls) {
		    this.enableControls = enableControls;
	    }
    }
}
