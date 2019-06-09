package com.divingpixel.popularmovies.internet;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.divingpixel.popularmovies.AppExecutors;
import com.divingpixel.popularmovies.MovieReview;
import com.divingpixel.popularmovies.MovieTrailer;
import com.divingpixel.popularmovies.PopularMovies;
import com.divingpixel.popularmovies.Utils;
import com.divingpixel.popularmovies.database.MoviesDatabase;
import com.divingpixel.popularmovies.database.MyMovieEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class TheMovieDB {

    public static final String POSTER_PATH = "https://image.tmdb.org/t/p/";
    public static final String POSTER_SMALL = "w185/";
    public static final String POSTER_BIG = "w500/";

    private static URL generateUrl(String requestURL) {
        final String API_KEY = "32a2be514060aa29a632774e0649f353";

        // parse breaks apart the URI string that's passed into its parameter
        Uri baseUri = Uri.parse(requestURL);
        // buildUpon prepares the baseUri that we just parsed so we can add query parameters to it
        Uri.Builder uriBuilder = baseUri.buildUpon();
        // Append api_key parameter and its value.
        uriBuilder.appendQueryParameter("api_key", API_KEY);
        // Return the completed URL
        URL url = null;
        try {
            url = new URL(uriBuilder.toString());
        } catch (MalformedURLException e) {
            Log.e("generateURL", "Error with creating URL ", e);
        }
        return url;
    }

    //Make an HTTP request to the given URL and return a String as the response.
    private static String getJson(URL url) throws IOException {
        String jsonResponse = "";
        Log.i("HTTP REQUEST", url.toString());
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            // If the request was successful (response code 200), then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e("httpRequest", "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e("httpRequest", "Problem retrieving the JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, that's why the function throws an IOException
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    //Convert the InputStream into a String which contains the whole JSON response from the server.
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    private static MyMovieEntry getOneMovie(JSONObject jsonObject, int index) {
        MyMovieEntry movie = null;
        try {
            // Extract the values of the keys
            int id = jsonObject.getInt("id");
            String title = jsonObject.getString("title");
            String date = jsonObject.getString("release_date");
            String synopsis = jsonObject.getString("overview");
            String poster = jsonObject.getString("poster_path");
            float rating = jsonObject.getLong("vote_average");
            // Create a new {@link MyMovieEntry} object from the JSON response.
            movie = new MyMovieEntry(id, index + 1, Utils.makeDate(), title, date, synopsis, poster, rating, false, PopularMovies.category);
        } catch (JSONException e) {
            //  Print a log message if an error is thrown when executing any of the above statements in the "try" block,
            Log.e("GETONEMOVIE", "Problem parsing the JSON", e);
        }
        return movie;
    }

    public static void getMovieList(String query, final MoviesDatabase moviesDB, Context context) {
        // set up the DownloadFinishListener
        DownloadFinishListener downloadListener;
        if (context instanceof DownloadFinishListener) {
            downloadListener = (DownloadFinishListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnConnectionChangeListener");
        }
        // deletes the non favorites items from the database
        moviesDB.myMovieDAO().deleteNonFavorites();
        // Generates the url corresponding to the query and reads the JSON data
        String requestURL;
        switch (query) {
            default:
            case "popular": {
                requestURL = "https://api.themoviedb.org/3/movie/popular?api_key=null";
                break;
            }
            case "top_rated": {
                requestURL = "https://api.themoviedb.org/3/movie/top_rated?api_key=null";
                break;
            }
        }
        //gets the data from the movieDB API
        String readData;
        try {
            readData = getJson(generateUrl(requestURL));
        } catch (IOException e) {
            e.printStackTrace();
            readData = "";
        }
        // If the JSON string is empty or null, then return early.
        if (!TextUtils.isEmpty(readData)) {
            // Try to parse the JSON response string.
            try {
                // Create a JSONObject from the JSON response string
                JSONObject baseJson = new JSONObject(readData);
                // Extract the JSONObject associated with the key called "results"
                JSONArray moviesArray = baseJson.getJSONArray("results");
                // For each song in the songsArray, create an {@link MyMovieEntry} object
                for (int i = 0; i < moviesArray.length(); i++) {
                    // Get a single movie at position i within the list of movies
                    final MyMovieEntry movie = getOneMovie(moviesArray.getJSONObject(i), i);
                    // Add the new {@link MyMovieEntry} to the database
                    if (movie != null) {
                        AppExecutors.getInstance().diskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                //Log.v("GETMOVIELIST", "Adding new movie to database : " + movie.getTitle());
                                try {
                                    moviesDB.myMovieDAO().insertMovie(movie);
                                } catch (Exception e) {
                                    e.getStackTrace();
                                }
                            }
                        });
                    }
                }
                downloadListener.onDownloadFinish(true,"movies");
            } catch (JSONException e) {
                //  Print a log message if an error is thrown when executing any of the above statements in the "try" block,
                downloadListener.onDownloadFinish(false,"movies");
                Log.e("GETMOVIELIST", "Problem parsing the JSON", e);
            }
        }
    }

    public static ArrayList<MovieReview> getReviews (int id, Context context, String caller) {
        // set up the DownloadFinishListener
        DownloadFinishListener downloadListener;
        if (context instanceof DownloadFinishListener) {
            downloadListener = (DownloadFinishListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnConnectionChangeListener");
        }
        ArrayList<MovieReview> reviews = new ArrayList<>();
        String requestURL = "https://api.themoviedb.org/3/movie/"+id+"/reviews?api_key=null";
        String readData;
        try {
            readData = getJson(generateUrl(requestURL));
        } catch (IOException e) {
            e.printStackTrace();
            readData = "";
        }
        if (!TextUtils.isEmpty(readData)) {
            try {
                // Create a JSONObject from the JSON response string
                JSONObject baseJson = new JSONObject(readData);
                // Extract the JSONObject associated with the key called "results"
                JSONArray reviewsArray = baseJson.getJSONArray("results");
                for (int i = 0; i < reviewsArray.length(); i++) {
                    String author = reviewsArray.getJSONObject(i).getString("author");
                    String content = reviewsArray.getJSONObject(i).getString("content");
                    reviews.add(new MovieReview(id,author,content));
                }
            } catch (JSONException e) {
                //  Print a log message if an error is thrown when executing any of the above statements in the "try" block,
                Log.e("GET REVIEWS", "Problem parsing the JSON", e);
            }
        }
        downloadListener.onDownloadFinish(true,caller);
        return reviews;
    }

    public static ArrayList<MovieTrailer> getTrailers (int id, Context context, String caller) {
        // set up the DownloadFinishListener
        DownloadFinishListener downloadListener;
        if (context instanceof DownloadFinishListener) {
            downloadListener = (DownloadFinishListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnConnectionChangeListener");
        }
        ArrayList<MovieTrailer> trailers = new ArrayList<>();
        String requestURL = "https://api.themoviedb.org/3/movie/"+id+"/videos?api_key=null";
        String readData;
        try {
            readData = getJson(generateUrl(requestURL));
        } catch (IOException e) {
            e.printStackTrace();
            readData = "";
        }
        if (!TextUtils.isEmpty(readData)) {
            try {
                // Create a JSONObject from the JSON response string
                JSONObject baseJson = new JSONObject(readData);
                // Extract the JSONObject associated with the key called "results"
                JSONArray reviewsArray = baseJson.getJSONArray("results");
                for (int i = 0; i < reviewsArray.length(); i++) {
                    String name = reviewsArray.getJSONObject(i).getString("name");
                    String key = reviewsArray.getJSONObject(i).getString("key");
                    trailers.add(new MovieTrailer(id,name,key));
                }
            } catch (JSONException e) {
                //  Print a log message if an error is thrown when executing any of the above statements in the "try" block,
                Log.e("GET TRAILERS", "Problem parsing the JSON", e);
            }
        }
        downloadListener.onDownloadFinish(true, caller);
        return trailers;
    }

    public interface DownloadFinishListener {
        void onDownloadFinish(Boolean status, String caller);
    }
}
