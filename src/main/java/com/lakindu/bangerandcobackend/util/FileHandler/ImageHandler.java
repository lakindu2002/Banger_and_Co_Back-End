package com.lakindu.bangerandcobackend.util.FileHandler;

import com.lakindu.bangerandcobackend.entity.User;

public abstract class ImageHandler {
    protected byte[] unHandledPicture;
    private User theUser;

    public final void processUnhandledImage(User theUser) throws Exception {
        initializeData(theUser);
        byte[] handledDataForUser = handleTheImage();
        setHandledData(handledDataForUser);
    }

    private void setHandledData(byte[] handledDataForUser) {
        this.theUser.setProfilePicture(handledDataForUser);
    }

    protected abstract byte[] handleTheImage() throws Exception;

    private void initializeData(User theUser) {
        this.theUser = theUser;
        this.unHandledPicture = theUser.getProfilePicture();
    }
}
