package com.nimo.exceltool;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wupeng on 18-6-7.
 */

public class HttpUtils {
    public static final String TAG = "HttpUtils";
    public static final boolean DEBUG = false;
    public static final String HTTPS_HEADER = "https://";
    public static final String HTTP_HEADER = "http://";
    public static final String SERVER_PATH_FILE_DOWNLOAD = "/ocfs/file/download/";


    public static final Map<String, String> DEFAULT_REQUEST_HEADER = new HashMap<>();
    public static final int ACK_RETRY = 3;
    public static final String CONTENT_TYPE_JSON = "application/json; charset=utf-8";
    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType MEDIA_TYPE_ZIP = MediaType.parse("application/zip");
    public static final MediaType MEDIA_TYPE_TEXT = MediaType.parse("text/plain;charset=utf-8");
    public static final String CONTENT_TYPE_FILE = "multipart/form-data";
    public static final MediaType MEDIA_TYPE_FILE = MediaType.parse(CONTENT_TYPE_FILE);

    //public static final String SECRET_KEY = "K/1OTBT9j1rqyVjVPQdf"; //TODO define from SECRET_KEY
    private static int HTTP_CONNECTION_TIMEOUT = 20; // connection timeout 20s
    private static int HTTP_DOWNLOAD_TIMEOUT = 5; // connection timeout 20s
    private static int HTTP_READ_TIMEOUT = 10; // read timeout 20s
    private static int HTTP_WRITE_TIMEOUT = HTTP_READ_TIMEOUT; // write timeout 20s

    private String mUrlPostSummay;
    private String mUrlLogUpload;
    private String mUrlServiceHealth;
    private String mServerUrl;

    private String mIp;
    private String mPort;
    private String mKey;
    private String mSn = "FC242218490005";

    private OkHttpClient mClient = null;

    private class Methods {
        static final String GET = "GET";
        static final String POST = "POST";
        static final String HEAD = "HEAD";
        static final String OPTIONS = "OPTIONS";
        static final String PUT = "PUT";
        static final String DELETE = "DELETE";
        static final String TRACE = "TRACE";
    }

    private static HttpUtils sInstance = null;

    private HttpUtils() {
        super();
    }

    public void setIPAndPort(String ip, String port) {
        mIp = ip;
        mPort = port;
        //mPort = "80";
        mServerUrl = mIp;
    }


    public static HttpUtils getsInstance() {
        if (sInstance == null) {
            sInstance = new HttpUtils();
            sInstance.init();
        }
        return sInstance;
    }


    private void init() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        if (DEBUG)
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        else
            logging.setLevel(HttpLoggingInterceptor.Level.NONE);

        this.mClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String s, SSLSession sslSession) {
                        return true;
                    }
                })
                .build();
    }

    private OkHttpClient getOkHttpClient() {
        return mClient;
    }

    private Request createDownloadRequest(String url) {
        return createGetRequest(url, false);
    }

    private Request createGetRequest(String url, boolean use_https) {
        Request.Builder builder = new Request.Builder()
                .url((use_https ? HTTPS_HEADER : HTTP_HEADER) + url);

        return builder.build();
    }

    private String getFileName(List<String> list) {
        String filename = null;

        if (list == null)
            return null;

        try {
            for (int i = 0; i < list.size(); i++) {
                String str = list.get(i);
                if (str.startsWith("attachment")) {
                    String[] a = str.split(";");

                    for (int k = 0; k < a.length; k++) {
                        if (a[k].startsWith("filename=")) {
                            filename = a[k].substring("filename=".length());
                            filename = filename.replace('%', '@');
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (filename != null) {
            Log.i(TAG, "filename=" + filename);
        }

        return filename;
    }

    public boolean DownloadFile(String fieldid) {
        boolean result = false;
        HttpUtils http_util = HttpUtils.getsInstance();
        OkHttpClient client = http_util.getOkHttpClient();

        try {
            String url = "10.20.30.34" + SERVER_PATH_FILE_DOWNLOAD + fieldid;
            Request request = http_util.createDownloadRequest(url);
            Response response = client.newCall(request).execute();
            List<String> temp = response.headers("Content-Disposition");

            String filename = getFileName(temp);
            if (response.code() == 200) {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;

                if (response.body() == null)
                    return  false;

                try {
                    //long total = response.body().contentLength();
                    //Log.i(TAG, "total------>" + total);
                    //long current = 0;
                    is = response.body().byteStream();
                    fos = new FileOutputStream(new File(filename));
                    while ((len = is.read(buf)) != -1) {
                        //current += len;
                        fos.write(buf, 0, len);
                    }
                    fos.flush();
                } catch (IOException e) {
                    Log.i(TAG, e.toString());
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e) {
                        Log.i(TAG, e.toString());
                    }
                }
                result = true;
            }
        } catch (Exception e) {
            Log.i(TAG, "connect to saas log service fail.");
        }

        return result;
    }


    /**
     * Unit tests the {@code ST} data type.
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            HttpUtils.getsInstance().DownloadFile(args[i]);
        }
    }
}
