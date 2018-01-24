package com.hotteam67.firebaseviewer.firebase;

import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.Callable;

import org.json.JSONObject;
import org.restonfire.FirebaseRestDatabase;


/**
 * Created by Jakob on 1/13/2018.
 */

public class FirebaseHelper {
    FirebaseRestDatabase database;
    AssetManager assetManager;

    String firebaseEvent;
    String firebaseUrl;

    Callable firebaseCompleteEvent = null;

    HashMap<String, Object> results = null;

    public FirebaseHelper(String url, String event)
    {
        firebaseUrl = url;
        firebaseEvent = event;
    }

    public void Download(Callable completeEvent, AssetManager assets)
    {
        firebaseCompleteEvent = completeEvent;
        this.assetManager = assets;
        new RetreiveFirebaseTask().execute();
    }

    class RetreiveFirebaseTask extends AsyncTask<Void, Void, String> {
        protected String doInBackground(Void... nothing)
        {
            try
            {
                String authToken = GetAuthToken(assetManager);
                String finalUrl = firebaseUrl + "/" + firebaseEvent + ".json" + "?auth=" + authToken;
                Log.d("FirebaseScouter", "URL: " + finalUrl);

                URL url = new URL(finalUrl);
                HttpURLConnection conn = (HttpURLConnection) new URL(finalUrl).openConnection();
                conn.setRequestMethod("GET");

                Log.d("FirebaseScouter", "Response code: " + conn.getResponseCode());
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) { // 200

                    InputStream responseStream = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream));
                    String line = reader.readLine();
                    String response = "";
                    while (line != null)
                    {
                        response += line;
                        line = reader.readLine();
                    }

                    Log.d("FirebaseScouter", "Response: " + response);

                    conn.disconnect();
                    return response;
                }
                conn.disconnect();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return "";
        }
        protected void onPostExecute(String result)
        {
            DoLoad(result);
        }
    }

    private String GetAuthToken(AssetManager manager)
    {
        try {
            InputStream is = manager.open("credentials");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String auth = new String(buffer);
            Log.d("FirebaseScouter", "Authentication token: " + auth);

            return auth;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "";
        }
    }


    private void DoLoad(String json)
    {
        try
        {
            results = new HashMap<>();
            JSONObject jsonObject = new JSONObject(json);

            Iterator<?> iterator = jsonObject.keys();
            while (iterator.hasNext())
            {
                String key = (String) iterator.next();
                JSONObject row = (JSONObject) jsonObject.get(key);
                HashMap<String, String> rowMap = new HashMap<>();

                Iterator<?> rowIterator = row.keys();
                while (rowIterator.hasNext())
                {
                    String columnKey = (String) rowIterator.next();
                    rowMap.put(columnKey, row.get(columnKey).toString());
                }

                results.put(key, rowMap);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        DoFinish();
    }

    private void DoFinish()
    {
        try {
            firebaseCompleteEvent.call();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.e("FirebaseScouter", "Failed to call completeEvent");
        }
    }

    public HashMap<String, Object> getResult()
    {
        return results;
    }
}
