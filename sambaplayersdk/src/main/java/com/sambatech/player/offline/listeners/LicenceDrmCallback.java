package com.sambatech.player.offline.listeners;

public interface LicenceDrmCallback {
    public void onLicencePrepared(byte[] licencePayload);
    public void onLicenceError(Error error);
}
