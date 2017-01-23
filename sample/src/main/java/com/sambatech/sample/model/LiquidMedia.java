package com.sambatech.sample.model;

import com.sambatech.player.model.SambaMediaRequest;

import java.util.ArrayList;

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
    public EntitlementScheme entitlementScheme;

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

    public static class EntitlementScheme {
        public final String contentId;

        public EntitlementScheme() {
            this(null);
        }

        public EntitlementScheme(String contentId) {
            this.contentId = contentId;
        }
    }
}
