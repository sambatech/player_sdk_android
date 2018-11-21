package com.sambatech.player.offline;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloaderConstructorHelper;
import com.google.android.exoplayer2.offline.StreamKey;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;
import com.sambatech.player.model.SambaMedia;
import com.sambatech.player.offline.listeners.SambaDownloadListener;
import com.sambatech.player.offline.listeners.SambaDownloadRequestListener;
import com.sambatech.player.offline.model.SambaDownloadRequest;

import java.io.File;
import java.util.List;

public class SambaDownloadManager {

    private static final String DOWNLOAD_ACTION_FILE = "actions";
    private static final String DOWNLOAD_TRACKER_ACTION_FILE = "tracked_actions";
    private static final String DOWNLOAD_CONTENT_DIRECTORY = "downloads";

    private static final String SAMBA_PREF = "samba_pref";

    private String userAgent;

    private File downloadDirectory;
    private Cache downloadCache;
    private DownloadManager downloadManager;
    private SambaDownloadTracker sambaDownloadTracker;
    private Application applicationInstance;


    private static SambaDownloadManager instance;


    private SambaDownloadManager() {
    }

    public static SambaDownloadManager getInstance() {

        if (instance == null) {
            instance = new SambaDownloadManager();
        }

        return instance;
    }

    public void init(Application application) {
        this.applicationInstance = application;
        userAgent = Util.getUserAgent(applicationInstance.getApplicationContext(), "SambaPlayer");
    }

    public void addDownloadListener(SambaDownloadListener listener) {
        getSambaDownloadTracker().addListener(listener);
    }

    public void removeDownloadListener(SambaDownloadListener listener) {
        getSambaDownloadTracker().removeListener(listener);
    }


    public void prepareDownload(@NonNull SambaDownloadRequest sambaDownloadRequest, @NonNull SambaDownloadRequestListener requestListener) {
        getSambaDownloadTracker().prepareDownload(sambaDownloadRequest, requestListener);
    }

    public void performDownload(@NonNull SambaDownloadRequest sambaDownloadRequest) {
        getSambaDownloadTracker().performDownload(sambaDownloadRequest);
    }

    public boolean isDownloaded(@NonNull String mediaId) {
        return getSambaDownloadTracker().isDownloaded(mediaId);
    }

    public boolean isDownloading(@NonNull String mediaId) {
        return getSambaDownloadTracker().isDownloading(mediaId);
    }

    public void cancelAllDownloads() {
        getSambaDownloadTracker().cancelAllDownloads();
    }

    public void cancelDownload(String mediaId) {
        getSambaDownloadTracker().cancelDownload(mediaId);
    }

    public void deleteDownload(String mediaId) {
        getSambaDownloadTracker().deleteDownload(mediaId);
    }

    public void deleteAllDownloads() {
        getSambaDownloadTracker().deleteAllDownloads();
    }

    public void startStoppedDownloads() {
        getSambaDownloadTracker().startStoppedDownloads();
    }

    public void stopAllDownloads() {
        getSambaDownloadTracker().stopAllDownloads();
    }

    public Application getAppInstance() {
        return applicationInstance;
    }


    public DataSource.Factory buildDataSourceFactory() {
        DefaultDataSourceFactory upstreamFactory = new DefaultDataSourceFactory(
                applicationInstance.getApplicationContext(), buildHttpDataSourceFactory()
        );
        return buildReadOnlyCacheDataSource(upstreamFactory, getDownloadCache());
    }


    HttpDataSource.Factory buildHttpDataSourceFactory() {
        return new DefaultHttpDataSourceFactory(userAgent);
    }


    DownloadManager getDownloadManager() {
        initDownloadManager();
        return downloadManager;
    }

    SambaDownloadTracker getSambaDownloadTracker() {
        initDownloadManager();
        return sambaDownloadTracker;
    }

    private synchronized void initDownloadManager() {
        if (downloadManager == null) {
            DownloaderConstructorHelper downloaderConstructorHelper = new DownloaderConstructorHelper(getDownloadCache(), buildHttpDataSourceFactory());
            downloadManager = new DownloadManager(
                    downloaderConstructorHelper,
                    DownloadManager.DEFAULT_MAX_SIMULTANEOUS_DOWNLOADS,
                    DownloadManager.DEFAULT_MIN_RETRY_COUNT,
                    new File(getDownloadDirectory(), DOWNLOAD_ACTION_FILE)
            );
            sambaDownloadTracker = new SambaDownloadTracker(
                    applicationInstance.getApplicationContext(),
                    buildDataSourceFactory(),
                    new File(getDownloadDirectory(), DOWNLOAD_TRACKER_ACTION_FILE)
            );

            downloadManager.addListener(sambaDownloadTracker);
        }
    }

    private synchronized Cache getDownloadCache() {
        if (downloadCache == null) {
            File downloadContentDirectory = new File(getDownloadDirectory(), DOWNLOAD_CONTENT_DIRECTORY);
            downloadCache = new SimpleCache(downloadContentDirectory, new NoOpCacheEvictor());
        }
        return downloadCache;
    }

    private File getDownloadDirectory() {
        if (downloadDirectory == null) {
            downloadDirectory = applicationInstance.getExternalFilesDir(null);
            if (downloadDirectory == null) {
                downloadDirectory = applicationInstance.getFilesDir();
            }
        }
        return downloadDirectory;
    }

    private static CacheDataSourceFactory buildReadOnlyCacheDataSource(DefaultDataSourceFactory upstreamFactory, Cache cache) {
        return new CacheDataSourceFactory(
                cache,
                upstreamFactory,
                CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR,
                20 * 1024 * 1024
        );
    }

    public SharedPreferences getSharedPreferences() {
        return applicationInstance.getSharedPreferences(SAMBA_PREF, Context.MODE_PRIVATE);
    }

    public String getUserAgent() {
        return userAgent;
    }

    public List<StreamKey> getOfflineStreamKeys(Uri uri) {
        return getSambaDownloadTracker().getOfflineStreamKeys(uri);
    }

    @Nullable
    public SambaMedia getDownloadedMedia(@NonNull String mediaId) {
        return getSambaDownloadTracker().getDownloadedMedia(mediaId);
    }

    @Nullable
    public List<SambaMedia> getDownloadedMedias() {
        return getSambaDownloadTracker().getDownloadedMedias();
    }

    public void updateDownloadedMedia(SambaMedia sambaMedia) {
         getSambaDownloadTracker().updateDownloadedMedia(sambaMedia);
    }

}
