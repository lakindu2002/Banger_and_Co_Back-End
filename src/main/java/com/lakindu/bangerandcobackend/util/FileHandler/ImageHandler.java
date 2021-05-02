package com.lakindu.bangerandcobackend.util.FileHandler;

import com.lakindu.bangerandcobackend.entity.User;

public abstract class ImageHandler {
    public final byte[] processUnhandledImage(byte[] rawImage) throws Exception {
        byte[] handledDataForUser = handleTheImage(rawImage);
        return returnTheData(handledDataForUser);
    }

    private byte[] returnTheData(byte[] handledDataForUser) {
        return handledDataForUser;
    }

    protected abstract byte[] handleTheImage(byte[] rawImage) throws Exception;

}
