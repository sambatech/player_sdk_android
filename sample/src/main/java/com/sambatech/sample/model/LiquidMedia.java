package com.sambatech.sample.model;

import com.sambatech.player.model.SambaMediaConfig;
import com.sambatech.player.model.SambaMediaRequest;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by tmiranda on 12/01/16.
 */
public class LiquidMedia implements Cloneable {
    public String id;
    public String title = "";
    public String description = "";
    public String shortDescription = "";
    public Long publishDate;
    public Boolean highlighted;
    public ArrayList<File> files;
    public ArrayList<Thumb> thumbs;
    public String streamUrl;
	public String qualifier = "VIDEO";
    public String url;
    public String type = "";

    //Non liquid api
    public String ph;
    public String ad_program;
    public AdTag adTag;
    public SambaMediaRequest.Environment environment;
    public ValidationRequest validationRequest;

    public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

    public static class File {
        public String qualifier;
        public FileInfo fileInfo;

        public static class FileInfo {
            public Long duration;
        }
    }

    public static class Thumb {
        public int size;
        public String url;
    }

    public static class AdTag {
        public String name;
        public String url;
    }

	public interface DrmCallback {
		void call(SambaMediaConfig media, String response);
	}

    public static class ValidationRequest {
        public String url;
	    public HashMap<String, String> headers;
	    public DrmCallback callback;
	    public SambaMediaConfig media;

	    public ValidationRequest(String url, DrmCallback callback) {
		    this(url, null, callback);
	    }

        public ValidationRequest(String url, HashMap<String, String> headers, DrmCallback callback) {
            this.url = url;
	        this.headers = headers;
	        this.callback = callback;
        }
    }
}
