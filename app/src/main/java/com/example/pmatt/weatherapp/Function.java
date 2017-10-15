package com.example.pmatt.weatherapp;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by pmatt on 10/14/2017.
 * This serves to hold a lot of functions used regularly in MainActivity.java
 */

public class Function {

    //API stuff
    private static final String OPEN_WEATHER_MAP_URL = "http://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&units=metric";
    private static final String OPEN_WEATHER_MAP_API = "11eca09f2f026c309d0b205b08f9be73";

    //This sets the weather icon based on sunrise/sunset
    public static String setWeatherIcon(int actualId, long sunrise, long sunset)
    {
        int id = actualId / 100;
        String icon = "";
        if(actualId == 800)
        {
            long currentTime = new Date().getTime();
            if(currentTime>=sunrise && currentTime<sunset)
            {
                icon = "&#xf00d;";
            }
            else
            {
                icon = "&#xf02e;";
            }
        }
        else
        {
            switch(id)
            {
                case 2:
                    icon = "&#xf01e;";
                    break;
                case 3:
                    icon = "&#xf01c;";
                    break;
                case 7:
                    icon = "&#xf014;";
                    break;
                case 8:
                    icon = "&#xf013;";
                    break;
                case 6:
                    icon = "&#xf01b;";
                    break;
                case 5:
                    icon = "&#xf019;";
                    break;
            }
        }
        return icon;
    }

    //Passes all responses from the JSON object to the activity's fields.
    public interface AsyncResponse
    {
        void processFinish(String output1, String output2, String output3, String output4, String output5, String output6, String output7, String output8, String output9, String output10);
    }

    //A class for gathering data from the JSON which is a class so that it can be Async
    public static class placeIdTask extends AsyncTask<String, Void, JSONObject>
    {
        public AsyncResponse delegate = null;
        public placeIdTask(AsyncResponse asyncResponse)
        {
            delegate = asyncResponse;
        }

        //Overrides doInBackground to call getWeatherJSON
        @Override
        protected JSONObject doInBackground(String... params)
        {
            JSONObject jsonWeather = null;
            try
            {
                jsonWeather = getWeatherJSON(params[0],params[1]);
            }
            catch(Exception e)
            {
                //Log.d("Error", "Cannot process JSON results", e);
            }

            return jsonWeather;
        }

        //Overrides onPostExecute in order to parse the JSON from openweatherAPI
        @Override
        protected void  onPostExecute(JSONObject json)
        {
            try
            {
                if(json != null)
                {
                    JSONObject details = json.getJSONArray("weather").getJSONObject(0);
                    JSONObject main = json.getJSONObject("main");
                    DateFormat df = DateFormat.getDateTimeInstance();

                    String city = json.getString("name").toUpperCase(Locale.US) + ", " + json.getJSONObject("sys").getString("country");
                    String description = details.getString("description").toUpperCase(Locale.US);

                    String rain = "";
                    if(description.contains("rain"))
                    {
                        rain = "Bring a raincoat today.";
                    }
                    else
                    {
                        rain = "Don't bring a raincoat today.";
                    }

                    //this converts from c to f
                    double celcTemp = main.getDouble("temp");
                    double Fertemp = (celcTemp *1.8) +32;
                    String jeans = "";

                    if(Fertemp < 73)
                    {
                        jeans = "You should wear jeans today.";
                    }
                    else if (Fertemp < 60)
                    {
                        jeans = "You should wear a coat today.";
                    }
                    else
                    {
                        jeans = "You should wear shorts today.";
                    }

                    String temperature = String.format("%.2f", Fertemp)+ "°F";
                    String humidity = main.getString("humidity") + "%";
                    String pressure = main.getString("pressure") + " hPa";
                    String updatedOn = df.format(new Date(json.getLong("dt")*1000));
                    String iconText = setWeatherIcon(details.getInt("id"),
                            json.getJSONObject("sys").getLong("sunrise") * 1000,
                            json.getJSONObject("sys").getLong("sunset") * 1000);
                    delegate.processFinish(city, description, temperature, humidity, pressure, updatedOn, iconText, ""+ (json.getJSONObject("sys").getLong("sunrise") * 1000), jeans, rain);
                }
            }
            catch (JSONException e)
            {
                //Log.e(LOG_TAG, "Cannot process JSON results", e);
            }
        }
    }

    //recieves the JSON from the API
    public static JSONObject getWeatherJSON(String lat, String lon)
    {
        try
        {
            URL url = new URL(String.format(OPEN_WEATHER_MAP_URL, lat, lon));
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.addRequestProperty("x-api-key", OPEN_WEATHER_MAP_API);

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuffer json = new StringBuffer(1024);
            String tmp="";
            while((tmp=reader.readLine())!=null)json.append(tmp).append("\n");
            reader.close();

            JSONObject data = new JSONObject(json.toString());

            //This value will be 404 if the request was not successfull
            if(data.getInt("cod") != 200)
            {
                return null;
            }

            return data;
        }
        catch(Exception e)
        {
            return null;
        }
    }

}
