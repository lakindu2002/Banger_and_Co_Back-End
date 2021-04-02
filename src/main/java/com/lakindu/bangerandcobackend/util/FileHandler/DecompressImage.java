package com.lakindu.bangerandcobackend.util.FileHandler;

import java.io.ByteArrayOutputStream;
import java.util.zip.Inflater;

public class DecompressImage extends ImageHandler {
    @Override
    protected byte[] handleTheImage() throws Exception {
        Inflater theInflater = new Inflater();
        theInflater.setInput(unHandledPicture); //set the bytes to be decompressed

        byte[] decompressedData = new byte[1024]; //decompressed data for 1024 Bytes

        //create output stream with size of image bytes
        ByteArrayOutputStream theByteArrayOutputStream = new ByteArrayOutputStream(unHandledPicture.length);

        while (!theInflater.finished()) {
            //while the deflater compresses the data
            int compressedBytes = theInflater.inflate(decompressedData); //compresses the image and inserts to compressed byte array
            theByteArrayOutputStream.write(decompressedData, 0, compressedBytes); //write the compressed data to the output buffer
        }

        theInflater.end();
        theByteArrayOutputStream.flush();
        theByteArrayOutputStream.close();

        return theByteArrayOutputStream.toByteArray(); //returns the bytes written through the stream
    }
}
