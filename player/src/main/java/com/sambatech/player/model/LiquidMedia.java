package com.sambatech.player.model;

import java.util.ArrayList;

/**
 * Created by tmiranda on 12/01/16.
 */
public class LiquidMedia {
    public String id;
    public String title;
    public String description;
    public String shortDescription;
    public Long publishDate;
    public Boolean highlighted;
    public ArrayList<File> files;
    public ArrayList<Thumb> thumbs;

    //Non liquid api
    public String ph;
    public String ad_program;
    public AdTag adTag;


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
}

