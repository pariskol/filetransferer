package gr.kgdev.fileuploader.utils;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import gr.kgdev.fileuploader.AppCache;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MyHttpClient extends HttpClient {

    private String base = "https://onelineman.eu/api";
//    private String base = "http://192.168.2.5:8080/api";

    private static final MediaType MEDIA_TYPE_WILDCARD = MediaType.parse("*/*");


    public Object download(String file) throws Exception {
        String[] split = file.split("/");
        String name = split[split.length - 1];
        Request request = new Request.Builder()
                                .header("Authorization", this.BASIC_AUTH)
                                .url(base + "/download/" + name)
                                .build();

        File file2 = new File("/storage/emulated/0/Download/" + name);
//        file2.createNewFile();

        try (Response response = client.newCall(request).execute();
             InputStream is = response.body().byteStream();
             BufferedInputStream input = new BufferedInputStream(is);
             OutputStream output = new FileOutputStream(AppCache.getDownloadLocation() + name);) {

            byte[] data = new byte[1024];

            int count = 0;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }

            output.flush();
            if (!response.isSuccessful())
                throw new IOException("Status code: " + response.code() + " , " + response.body().string());
            return "OK";
        }
    }

    public Object upload(String path, String toUser) throws Exception {
        checkForCredentials();
        File file = new File(path);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(file, MEDIA_TYPE_WILDCARD))
                .build();

        Request request = new Request.Builder()
                .header("Authorization", this.BASIC_AUTH)
                .header("Content-Type", "multipart/form-data")
                .url(base + "/upload?TO_USER=" + toUser)
                .post(requestBody)
                .build();

        try(Response response = client.newCall(request).execute();) {
            if (!response.isSuccessful())
                throw new IOException("Status code: " + response.code() + " , " + response.body().string());
            return serialize(response.body().string());
        }
    }

    @Override
    public Object get(String url) throws Exception {
        if (!url.startsWith("http"))
            url = base + url;
        return super.get(url);
    }

    @Override
    public Object post(String url, JSONObject json) throws Exception {
        if (!url.startsWith("http"))
            url = base + url;
        return super.post(url, json);
    }

    @Override
    protected Object serialize(String response) throws IOException, JSONException {
        if (response.startsWith("["))
            return new JSONArray(response);
        else
            return new JSONObject(response);
    }
}
