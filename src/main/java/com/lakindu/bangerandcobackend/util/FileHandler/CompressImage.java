package com.lakindu.bangerandcobackend.util.FileHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;

public class CompressImage extends ImageHandler {
    @Override
    protected byte[] handleTheImage(byte[] rawImage) throws IOException {
        Deflater theDeflater = new Deflater();
        theDeflater.setInput(rawImage); //set the bytes to be compressed
        theDeflater.finish(); //compression should end with current contents of input buffer

        byte[] compressedData = new byte[1024]; //compress data for 1024 Bytes

        //create output stream with size of image bytes
        ByteArrayOutputStream theByteArrayOutputStream = new ByteArrayOutputStream(rawImage.length);

        while (!theDeflater.finished()) {
            //while the deflater compresses the data
            int compressedBytes = theDeflater.deflate(compressedData); //compresses the image and inserts to compressed byte array
            theByteArrayOutputStream.write(compressedData, 0, compressedBytes); //write the compressed data to the output buffer
        }

        theDeflater.end();
        theByteArrayOutputStream.flush();
        theByteArrayOutputStream.close();

        return theByteArrayOutputStream.toByteArray(); //returns the bytes written through the stream
    }
}

