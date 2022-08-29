package com.example.mymap.Utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import org.apache.commons.io.FileUtils;
import java.io.IOException;
import java.io.InputStream;

public class FileHelper {
    public static byte[] readBytesFromFile(String path) {
        File file = new File(path);
        try {
            return FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] readBytesFromFile(File file) {
        try {
            return FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
