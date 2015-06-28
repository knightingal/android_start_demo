package com.example.jianming.Tasks;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadWebpageTask extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... urls) {
        try {
            return downloadUrl(urls[0]);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        
    }



    private String downloadUrl(String strUrl) throws IOException {
        InputStream is = null;
        try {
            URL url = new URL(strUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();

            int response = conn.getResponseCode();
            int contentLen = conn.getContentLength();

            Log.d("network", "The response is: " + response);
            Log.d("network", "Content length is: " + contentLen);
            is = conn.getInputStream();
            return readIt(is, contentLen);

        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    private String readIt(InputStream is, int len) throws IOException {
        Reader reader = new InputStreamReader(is, "UTF-8");
        char[] buffer = new char[len];
        String content = "";
        int readLen;
        do {
            readLen = reader.read(buffer);
            if (readLen > 0) {
                content += new String(buffer).substring(0, readLen);
            }
        } while (readLen > 0);
        return content;
    }
}

