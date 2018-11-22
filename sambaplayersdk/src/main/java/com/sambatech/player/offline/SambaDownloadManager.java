package com.sambatech.player.offline;

import android.app.Application;
import android.app.PendingIntent;
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

    private boolean isConfigured;


    private static SambaDownloadManager instance;
    private PendingIntent pendingIntent;


    private SambaDownloadManager() {
    }

    public static SambaDownloadManager getInstance() {

        if (instance == null) {
            instance = new SambaDownloadManager();
        }

        return instance;
    }

    public void init(@NonNull Application application) {
        this.applicationInstance = application;

        if (application != null) {
            isConfigured = true;
        }

        userAgent = Util.getUserAgent(applicationInstance.getApplicationContext(), "SambaPlayer");
    }

    public void addDownloadListener(SambaDownloadListener listener) {
        checkConfig();
        getSambaDownloadTracker().addListener(listener);
    }

    public void removeDownloadListener(SambaDownloadListener listener) {
        checkConfig();
        getSambaDownloadTracker().removeListener(listener);
    }


    public void prepareDownload(@NonNull SambaDownloadRequest sambaDownloadRequest, @NonNull SambaDownloadRequestListener requestListener) {
        checkConfig();
        getSambaDownloadTracker().prepareDownload(sambaDownloadRequest, requestListener);
    }

    public void performDownload(@NonNull SambaDownloadRequest sambaDownloadRequest) {
        checkConfig();
        getSambaDownloadTracker().performDownload(sambaDownloadRequest);
    }

    public boolean isDownloaded(@NonNull String mediaId) {
        checkConfig();
        return getSambaDownloadTracker().isDownloaded(mediaId);
    }

    public boolean isDownloading(@NonNull String mediaId) {
        checkConfig();
        return getSambaDownloadTracker().isDownloading(mediaId);
    }

    public void cancelAllDownloads() {
        checkConfig();
        getSambaDownloadTracker().cancelAllDownloads();
    }

    public void cancelDownload(String mediaId) {
        checkConfig();
        getSambaDownloadTracker().cancelDownload(mediaId);
    }

    public void deleteDownload(String mediaId) {
        checkConfig();
        getSambaDownloadTracker().deleteDownload(mediaId);
    }

    public void deleteAllDownloads() {
        checkConfig();
        getSambaDownloadTracker().deleteAllDownloads();
    }

    public void startStoppedDownloads() {
        checkConfig();
        getSambaDownloadTracker().startStoppedDownloads();
    }

    public void stopAllDownloads() {
        checkConfig();
        getSambaDownloadTracker().stopAllDownloads();
    }

    public Application getAppInstance() {
        checkConfig();
        return applicationInstance;
    }


    public DataSource.Factory buildDataSourceFactory() {
        checkConfig();
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
        checkConfig();
        return applicationInstance.getSharedPreferences(SAMBA_PREF, Context.MODE_PRIVATE);
    }

    public String getUserAgent() {
        checkConfig();
        return userAgent;
    }

    public List<StreamKey> getOfflineStreamKeys(Uri uri) {
        checkConfig();
        return getSambaDownloadTracker().getOfflineStreamKeys(uri);
    }

    @Nullable
    public SambaMedia getDownloadedMedia(@NonNull String mediaId) {
        checkConfig();
        return getSambaDownloadTracker().getDownloadedMedia(mediaId);
    }

    @Nullable
    public List<SambaMedia> getDownloadedMedias() {
        checkConfig();
        return getSambaDownloadTracker().getDownloadedMedias();
    }

    public void updateDownloadedMedia(@NonNull SambaMedia sambaMedia) {
        checkConfig();
        getSambaDownloadTracker().updateDownloadedMedia(sambaMedia);
    }


    public void setPendingIntentForDownloadNotifications(@NonNull PendingIntent pendingIntent) {
        checkConfig();
        this.pendingIntent = pendingIntent;
    }

    PendingIntent getPendingIntentForDownloadNotifications() {
        return this.pendingIntent;
    }


    public boolean isConfigured() {
        return isConfigured;
    }

    private void checkConfig() {
        if (!isConfigured) {
            throw new RuntimeException("The SambaDownloadManager must be configured in the Application class. Call the \"SambaDownloadManager.getInstance().init(mApplication)\" in the \"onCreate()\" method of the Application class.");
        }
    }
}
