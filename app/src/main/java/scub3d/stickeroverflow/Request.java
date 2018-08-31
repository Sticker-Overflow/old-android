package scub3d.stickeroverflow;

/**
 * Created by scub3d on 11/01/18.
 */

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.ContentValues.TAG;

public class Request {
    private String ip = "http://stickeroverflow.io/";
    private String url;
    private JSONObject json;
    private String[] jsonKeys;
    private Activity activity;
    private HandleResponseInterface handleResponseInterface;
    private String filePath;
    private String attachmentName;
    private String attachmentFileName;
    private String apiKey;

    public Request (String urlAddon, JSONObject json, Activity activity, HandleResponseInterface handleResponseInterface) {
        this.url = this.ip + urlAddon;
        this.json = json;
        this.jsonKeys = null;
        this.activity = activity;
        this.handleResponseInterface = handleResponseInterface;
    }

    public Request (String urlAddon, JSONObject json, String[] jsonKeys, Activity activity, HandleResponseInterface handleResponseInterface) {
        this.url = this.ip + urlAddon;
        this.json = json;
        this.jsonKeys = jsonKeys;
        this.activity = activity;
        this.handleResponseInterface = handleResponseInterface;
    }
    // "", "", "", "", "", getActivity(), this
    public Request (String urlAddon, String apiKey, String filePath, String attachmentName, String attachmentFileName, Activity activity, HandleResponseInterface handleResponseInterface) {
        this.url = this.ip + urlAddon;
        this.activity = activity;
        this.handleResponseInterface = handleResponseInterface;
        this.filePath = filePath;
        this.attachmentFileName = attachmentFileName;
        this.attachmentName = attachmentName;
        this.apiKey = apiKey;
    }

    public void executePostRequest() {
        DoPost doPost = new DoPost();
        doPost.execute();
    }

    public void executeGetRequest() {
        DoGet doGet = new DoGet();
        doGet.execute();
    }

    public void executeUploadFile() {
        UploadFile uploadFile = new UploadFile();
        uploadFile.execute();
    }

    private void handleResponse(final String info) {
        final String information = info;
        this.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    handleResponseInterface.handleResponse(information);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d(TAG, "handleResponse:runOnUiThread: " + e);
                }
            }
        });
    }

    private void errorOccurred() {
        Log.d(TAG, "errorOccurred: SHITS BROKE");
        this.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(Request.this.activity, "Error connecting to stickerOverflow's servers.", Toast.LENGTH_LONG).show();
            }
        });
        this.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                handleResponseInterface.handleError();
            }
        });
    }

    private class DoPost extends AsyncTask<Void, Void, String> {
        private static final String TAG = "doPost";
        public final String serverURL = Request.this.url;
        public final JSONObject dataToSend = Request.this.json;

        @Override
        protected String doInBackground(Void... params) {
            try {

                URL obj = new URL(serverURL);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("User-Agent", "Mozilla/5.0");
                con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                System.out.println(dataToSend);
                if(dataToSend != null) {
                    con.setDoInput(true);
                    con.setDoOutput(true);

                    OutputStream os = con.getOutputStream();
                    BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    wr.write(dataToSend.toString());
                    wr.flush();
                    wr.close();
                }

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                final StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                handleResponse(response.toString());

            } catch(Exception ex) {
                Log.e(TAG, "Failed to send HTTP POST request due to: " + ex);
                errorOccurred();
            }
            return null;
        }
    }

    private class DoGet extends AsyncTask<Void, Void, String> {
        private static final String TAG = "doGet";
        public String serverURL = Request.this.url;
        public String[] dataToSendKeys = Request.this.jsonKeys;
        public final JSONObject dataToSend = Request.this.json;

        private void putDataInURL() throws JSONException {
            if(dataToSend != null && dataToSendKeys != null) {
                for(int keyIndex =0; keyIndex < dataToSend.length(); keyIndex++) {
                    this.serverURL += "?" + this.dataToSendKeys[keyIndex] + "=" + dataToSend.get(dataToSendKeys[keyIndex]);
                }
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                putDataInURL();
                //  Debug!
                System.out.println(serverURL);

                URL obj = new URL(serverURL);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("User-Agent", "Mozilla/5.0");
                con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                final StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                handleResponse(response.toString());

            } catch(Exception ex) {
                Log.e(TAG, "Failed to send HTTP POST request due to: " + ex);
                errorOccurred();
            }
            return null;
        }
    }
    
    private class UploadFile extends AsyncTask<Void, Void, String> {
        private static final String TAG = "uploadFile";
        public final String serverURL = Request.this.url;
        public final String apiKey = Request.this.apiKey;
        public final String filePath = Request.this.filePath;

        public final String crlf = "\r\n";
        public final String twoHyphens = "--";
        public final String boundary =  "*****";

        public final String attachmentName = Request.this.attachmentName;
        public final String attachmentFileName = Request.this.attachmentFileName;

        @Override
        protected String doInBackground(Void... params) {
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(filePath);

                HttpURLConnection httpUrlConnection = null;
                URL url = new URL(serverURL);// + "?apiKey=" + apiKey);
                httpUrlConnection = (HttpURLConnection) url.openConnection();
                httpUrlConnection.setUseCaches(false);
                httpUrlConnection.setDoOutput(true);

                httpUrlConnection.setRequestMethod("POST");
                httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
                httpUrlConnection.setRequestProperty("Cache-Control", "no-cache");
                httpUrlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + this.boundary);
                DataOutputStream request = new DataOutputStream(httpUrlConnection.getOutputStream());

                request.writeBytes(this.twoHyphens + this.boundary + this.crlf);
                request.writeBytes("Content-Disposition: form-data; name=\"" + this.attachmentName + "\";filename=\"" + this.attachmentFileName + "\"" + this.crlf);
                request.writeBytes(this.crlf);

                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, bao);
                byte[] ba = bao.toByteArray();
                request.write(ba);

                request.writeBytes(this.crlf);
                request.writeBytes(this.twoHyphens + this.boundary + this.twoHyphens + this.crlf);

                request.flush();
                request.close();

                InputStream responseStream = new BufferedInputStream(httpUrlConnection.getInputStream());
                BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream));

                String line = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((line = responseStreamReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }

                responseStreamReader.close();
                String response = stringBuilder.toString();
                responseStream.close();
                httpUrlConnection.disconnect();
                Log.d(TAG, "doInBackground Response: " + response);
                handleResponse(response);

            } catch(Exception ex) {
                Log.e(TAG, "Failed to upload the image due to: " + ex);
                errorOccurred();
            }
            return null;
        }
    }
}