package com.lakindu.bangerandcobackend.util.FileHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class ImageHandler {
    public final byte[] compressImage(byte[] rawImage) throws IOException {
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

    public final byte[] decompressImage(byte[] compressedImage) throws DataFormatException, IOException {
        Inflater theInflater = new Inflater();
        theInflater.setInput(compressedImage); //set the bytes to be decompressed

        byte[] decompressedData = new byte[1024]; //decompressed data for 1024 Bytes

        //create output stream with size of image bytes
        ByteArrayOutputStream theByteArrayOutputStream = new ByteArrayOutputStream(compressedImage.length);

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
