package com.lakindu.bangerandcobackend.util.FileHandler;

import java.io.IOException;
import java.util.zip.DataFormatException;

public abstract class ImageHandler {
    public final byte[] processUnhandledImage(byte[] rawImage) throws DataFormatException, IOException {
        byte[] handledDataForUser = handleTheImage(rawImage);
        return returnTheData(handledDataForUser);
    }

    private byte[] returnTheData(byte[] handledDataForUser) {
        return handledDataForUser;
    }

    protected abstract byte[] handleTheImage(byte[] rawImage) throws DataFormatException, IOException;

}
