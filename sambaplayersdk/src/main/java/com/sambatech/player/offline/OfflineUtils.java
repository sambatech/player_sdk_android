package com.sambatech.player.offline;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Pair;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.drm.DrmInitData;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.drm.OfflineLicenseHelper;
import com.google.android.exoplayer2.source.dash.DashUtil;
import com.google.android.exoplayer2.source.dash.manifest.DashManifest;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sambatech.player.model.SambaMediaConfig;
import com.sambatech.player.offline.listeners.LicenceDrmCallback;
import com.sambatech.player.offline.model.DownloadData;
import com.sambatech.player.utils.SharedPrefsUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

class OfflineUtils {

    private static final String MEDIAS_PERSISTED_KEY = "MEDIAS_PERSISTED_KEY";

    private OfflineUtils() {
        throw new IllegalArgumentException("Static class");
    }



    static void getLicenseDrm(SambaMediaConfig sambaMediaConfig, LicenceDrmCallback drmCallback) {

        if (sambaMediaConfig.drmRequest == null) {
            drmCallback.onLicenceError(new Error("Media without DRM datas"));
            return;
        }

        @SuppressLint("StaticFieldLeak") AsyncTask<Pair<SambaMediaConfig, LicenceDrmCallback>, String, Void> task = new AsyncTask<Pair<SambaMediaConfig, LicenceDrmCallback>, String, Void>() {
            @Override
            protected Void doInBackground(Pair<SambaMediaConfig, LicenceDrmCallback>... datas) {

                SambaMediaConfig sambaMediaConfig = datas[0].first;
                LicenceDrmCallback licenceDrmCallback = datas[0].second;

                try {

                    Uri uri = Uri.parse(sambaMediaConfig.url);

                    String licenseUrl = sambaMediaConfig.drmRequest.getLicenseUrl();


                    DefaultHttpDataSourceFactory httpDataSourceFactory = new DefaultHttpDataSourceFactory(SambaDownloadManager.getInstance().getUserAgent());
                    OfflineLicenseHelper<FrameworkMediaCrypto> offlineLicenseHelper = OfflineLicenseHelper.newWidevineInstance(licenseUrl, httpDataSourceFactory);

                    DataSource dataSource = httpDataSourceFactory.createDataSource();

                    DashManifest dashManifest = DashUtil.loadManifest(dataSource, uri); //movie url
                    DrmInitData drmInitData = DashUtil.loadDrmInitData(dataSource, dashManifest.getPeriod(0));
                    byte[] offlineAssetKeyId = offlineLicenseHelper.downloadLicense(drmInitData);

                    licenceDrmCallback.onLicencePrepared(offlineAssetKeyId);

                } catch (Exception e) {
                    licenceDrmCallback.onLicenceError(new Error(e));
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void s) {
                super.onPostExecute(s);
            }
        };


        task.execute(new Pair<>(sambaMediaConfig, drmCallback));

    }


    static void persistSambaMedias(List<SambaMediaConfig> sambaMediaConfigList) {

        Type listType = new TypeToken<List<SambaMediaConfig>>() {}.getType();
        Gson gson = new Gson();
        String json = gson.toJson(sambaMediaConfigList, listType);
        SharedPrefsUtils.setStringPreference(
                SambaDownloadManager.getInstance().getAppInstance().getApplicationContext(),
                MEDIAS_PERSISTED_KEY,
                json
        );
    }

    static List<SambaMediaConfig>  getPersistedSambaMedias() {
        String json = SharedPrefsUtils.getStringPreference(
                SambaDownloadManager.getInstance().getAppInstance().getApplicationContext(),
                MEDIAS_PERSISTED_KEY
        );

        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        } else {
            Type listType = new TypeToken<List<SambaMediaConfig>>() {}.getType();
            Gson gson = new Gson();
            return gson.fromJson(json, listType);
        }

    }

    static int inferPrimaryTrackType(Format format) {
        int trackType = MimeTypes.getTrackType(format.sampleMimeType);
        if (trackType != C.TRACK_TYPE_UNKNOWN) {
            return trackType;
        }
        if (MimeTypes.getVideoMediaMimeType(format.codecs) != null) {
            return C.TRACK_TYPE_VIDEO;
        }
        if (MimeTypes.getAudioMediaMimeType(format.codecs) != null) {
            return C.TRACK_TYPE_AUDIO;
        }
        if (format.width != Format.NO_VALUE || format.height != Format.NO_VALUE) {
            return C.TRACK_TYPE_VIDEO;
        }
        if (format.channelCount != Format.NO_VALUE || format.sampleRate != Format.NO_VALUE) {
            return C.TRACK_TYPE_AUDIO;
        }
        return C.TRACK_TYPE_UNKNOWN;
    }

    static Double getSizeInMB(long bitrate, long duration){
        return (double) (((bitrate / 1000000f) * duration) / 8);
    }


    static DownloadData getDownloadDataFromBytes(byte[] data) {
        String json = Util.fromUtf8Bytes(data);
        return new Gson().fromJson(json, DownloadData.class);
    }

}
