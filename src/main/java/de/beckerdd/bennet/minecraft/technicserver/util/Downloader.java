package de.beckerdd.bennet.minecraft.technicserver.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Consumer;
import java.util.function.Function;

/*
 * Created by bennet on 8/7/17.
 *
 * technicserver - run modpacks from technicpack.net as server with ease.
 * Copyright (C) 2017 Bennet Becker <bennet@becker-dd.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Static Class for handling Downloads
 */
public class Downloader {
    /**
     * Download a File
     * @param url URL to the File
     * @param path Destination Path
     * @throws IOException throws if file not downloadable or destination not writeable
     */
    public static void downloadFile(URL url, String path) throws IOException {
        FileUtils.forceMkdirParent(new File(path));

        HttpURLConnection httpConnection = (HttpURLConnection) (url.openConnection());
        //long completeFileSize = httpConnection.getContentLength();

        BufferedInputStream in = new BufferedInputStream(httpConnection.getInputStream());
        FileOutputStream fos = new FileOutputStream(path);
        BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
        byte[] data = new byte[1024];
        //long downloadedFileSize = 0;

        int x;
        Logging.log("starting package download from " + url);
        while ((x = in.read(data, 0, 1024)) >= 0) {
            //downloadedFileSize += x;

            /*// calculate progress
            int currentProgress = (int) ((((double)downloadedFileSize) / ((double)completeFileSize)) * 100.0);

            // update progress bar
            System.out.print("Downloading '" + path + "': [");
            for(int i = 0; i < currentProgress/10; i++){
                System.out.print("#");
            }
            for(int i = 0; i < 10-currentProgress/10; i++){
                System.out.print(" ");
            }
            System.out.print("] " + currentProgress + "%\r");*/

            bout.write(data, 0, x);
        }
        bout.close();
        in.close();
        //System.out.println();
    }

    /**
     * Download a File
     * @param url URL to the File
     * @param path Destination Path
     * @throws IOException throws if file not downloadable or destination not writeable
     * @throws java.net.MalformedURLException URL was malformed
     */
    public static void downloadFile(String url, String path) throws IOException {
        downloadFile(new URL(url), path);
    }

    /**
     * Download a File and check it's MD5 sum
     * @param url URL to the File
     * @param path Destination
     * @param md5 expected MD5
     * @throws IOException throws if file not downloadable or destination not writeable
     * @throws DownloadException Md5 Missmatches
     */
    public static void downloadFile(URL url, String path, String md5) throws IOException {
        downloadFile(url, path);
        FileInputStream fis = new FileInputStream(new File(path));

        if(!DigestUtils.md5Hex(fis).equalsIgnoreCase(md5)){
            throw new DownloadException("MD5 sum missmatch");
        }
    }

    /**
     * Download a File and check it's MD5 sum
     * @param url URL to the File
     * @param path Destination
     * @param md5 expected MD5
     * @throws IOException throws if file not downloadable or destination not writeable
     * @throws java.net.MalformedURLException URL was malformed
     * @throws DownloadException Md5 Missmatches
     */
    public static void downloadFile(String url, String path, String md5) throws IOException {
        downloadFile(new URL(url), path, md5);
    }

    public static class DownloadException extends IOException{
        /**
         * Constructs an {@code DownloadException} with {@code null}
         * as its error detail message.
         */
        public DownloadException() {
            super();
        }

        /**
         * Constructs an {@code DownloadException} with the specified detail message.
         *
         * @param message
         *        The detail message (which is saved for later retrieval
         *        by the {@link #getMessage()} method)
         */
        public DownloadException(String message) {
            super(message);
        }

        /**
         * Constructs an {@code DownloadException} with the specified detail message
         * and cause.
         *
         * <p> Note that the detail message associated with {@code cause} is
         * <i>not</i> automatically incorporated into this exception's detail
         * message.
         *
         * @param message
         *        The detail message (which is saved for later retrieval
         *        by the {@link #getMessage()} method)
         *
         * @param cause
         *        The cause (which is saved for later retrieval by the
         *        {@link #getCause()} method).  (A null value is permitted,
         *        and indicates that the cause is nonexistent or unknown.)
         *
         * @since 1.6
         */
        public DownloadException(String message, Throwable cause) {
            super(message, cause);
        }

        /**
         * Constructs an {@code DownloadException} with the specified cause and a
         * detail message of {@code (cause==null ? null : cause.toString())}
         * (which typically contains the class and detail message of {@code cause}).
         * This constructor is useful for Download Exceptions that are little more
         * than wrappers for other throwables.
         *
         * @param cause
         *        The cause (which is saved for later retrieval by the
         *        {@link #getCause()} method).  (A null value is permitted,
         *        and indicates that the cause is nonexistent or unknown.)
         *
         * @since 1.6
         */
        public DownloadException(Throwable cause) {
            super(cause);
        }
    }
}
