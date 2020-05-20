package com.legend.cartridges;

import com.legend.utils.MD5Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * @author Legend
 * @data by on 20-4-4.
 * @description
 */
public class FileNesLoader extends InputStreamNesLoader {

    public FileNesLoader() throws IOException {
        this("");
    }

    public FileNesLoader(String filename) throws IOException {
        this(new File(filename));
    }

    public FileNesLoader(File file) throws IOException {
        super(new FileInputStream(file));
        setMd5(MD5Util.encrypt(new FileInputStream(file)));
        System.out.println(toString());
    }
}
