package com.divingpixel.popularmovies;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.divingpixel.popularmovies.database.MoviesDatabase;
import com.divingpixel.popularmovies.database.MyMovieEntry;
import com.divingpixel.popularmovies.internet.InternetCheck;
import com.divingpixel.popularmovies.internet.TheMovieDB;

import java.util.ArrayList;
import java.util.List;

public class PopularMovies extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener,
        InternetCheck.ConnectionChangeListener, TheMovieDB.DownloadFinishListener {

    //UI states constants
    private static final int UI_LOADING = 0;
    private static final int UI_NO_INTERNET = -1;
    private static final int UI_NO_DATA = -2;
    private static final int UI_NO_MOVIES = -3;
    private static final int UI_COMPLETE = 1;

    private static final String LOG_TAG = PopularMovies.class.getSimpleName();
    public static final String INSTANCE_SHOW_FAVORITES = "instance_show_favorites";
    public static final String INSTANCE_START_DATE = "instance_start_date";
    private ArrayList<MyMovieEntry> movieList;
    private MovieAdapter movieAdapter;
    RecyclerView recyclerView;
    TextView emptyView;
    View loadingProgress;
    private String startDate;
    private InternetCheck internetStatus;
    private MoviesDatabase moviesDB;
    private Context mainContext;
    public static boolean isConnected;
    public static String category = "";
    public static boolean showFavorites = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_screen);

        if (savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_SHOW_FAVORITES)) {
            showFavorites = savedInstanceState.getBoolean(INSTANCE_SHOW_FAVORITES, false);
            startDate = savedInstanceState.getString(INSTANCE_START_DATE, Utils.makeDate());
        }

        mainContext = this;
        moviesDB = MoviesDatabase.getInstance(getApplicationContext());
        movieAdapter = new MovieAdapter(movieList);

        // set-up the views
        emptyView = findViewById(R.id.empty_view);
        loadingProgress = findViewById(R.id.loading_progress);
        recyclerView = findViewById(R.id.movie_list);
        updateUi(UI_LOADING);

        //set up the recyclerview column count according to the orientation
        int orientation = this.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setLayoutManager(new GridLayoutManager(getBaseContext(), 3));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(getBaseContext(), 5));
        }
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(movieAdapter);

        // set-up the click listeners for the elements in the recyclerview
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getBaseContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Intent intent = new Intent(getApplicationContext(), MovieDetails.class);
                intent.putExtra(MovieDetails.EXTRA_MOVIE_ID, movieList.get(position).getId());
                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {
            }
        }));

        //gets the preferences from PreferencesManager
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        category = sharedPreferences.getString("movie_filter", "popular");

        //gets the internet connection status and downloads the movie list
        internetStatus = new InternetCheck(this.getApplication(), mainContext);
        isConnected = internetStatus.hasConnection();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!Utils.makeDate().equalsIgnoreCase(startDate)) {
            updateMovies();
        } else {
            updateUi(UI_COMPLETE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpViewModel();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(INSTANCE_SHOW_FAVORITES, showFavorites);
        outState.putString(INSTANCE_START_DATE, startDate);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        internetStatus.disable();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.settings_menu, menu);
        MenuItem item = menu.findItem(R.id.action_favorites);
        if (showFavorites)
            item.setIcon(R.drawable.ic_list_menu_24dp);
        else item.setIcon(R.drawable.ic_favorite_menu_24dp);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_favorites) {
            showFavorites = !showFavorites;
            if (showFavorites)
                item.setIcon(R.drawable.ic_list_menu_24dp);
            else item.setIcon(R.drawable.ic_favorite_menu_24dp);
            setUpViewModel();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("movie_filter")) {
            String inQuery = sharedPreferences.getString("movie_filter", "popular");
            Log.i("PREFERENCES CHANGED", "THE NEW MOVIE FILTER IS : " + inQuery);
            if (!category.equalsIgnoreCase(inQuery)) {
                category = inQuery;
                updateMovies();
            }
        }
    }

    private void updateMovies() {
        if (isConnected) {
            updateUi(UI_LOADING);
            AppExecutors.getInstance().networkIO().execute(new Runnable() {
                @Override
                public void run() {
                    startDate = Utils.makeDate();
                    Log.i(LOG_TAG, "DOWNLOADING MOVIE DATA FOR QUERY " + PopularMovies.category);
                    TheMovieDB.getMovieList(category, moviesDB, mainContext);
                }
            });
        } else {
            updateUi(UI_NO_INTERNET);
        }
    }

    private void setUpViewModel() {
        final MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getMovies(showFavorites).observe(this, new Observer<List<MyMovieEntry>>() {
            @Override
            public void onChanged(@Nullable List<MyMovieEntry> myMovieEntries) {
                movieList = (ArrayList<MyMovieEntry>) myMovieEntries;
                movieAdapter.setMovies(movieList);
                if (movieList.size() == 0) updateUi(UI_NO_MOVIES);
                else updateUi(UI_COMPLETE);
            }
        });
    }

    @Override
    public void onConnectionChange(boolean status) {
        Log.d(LOG_TAG, "CONNECTION STATUS CHANGED TO : " + status);
        isConnected = status;
    }

    @Override
    public void onDownloadFinish(final Boolean status, String caller) {
        AppExecutors.getInstance().mainThread().execute(new Runnable() {
            @Override
            public void run() {
                if (status) {
                    updateUi(UI_COMPLETE);
                } else {
                    updateUi(UI_NO_DATA);
                }
            }
        });
    }

    private void updateUi(int state) {
        switch (state) {
            case UI_LOADING:
                recyclerView.setVisibility(View.INVISIBLE);
                emptyView.setVisibility(View.GONE);
                loadingProgress.setVisibility(View.VISIBLE);
                break;
            case UI_NO_DATA:
                loadingProgress.setVisibility(View.GONE);
                emptyView.setText(R.string.data_error);
                emptyView.setVisibility(View.VISIBLE);
                break;
            case UI_NO_INTERNET:
                loadingProgress.setVisibility(View.GONE);
                emptyView.setText(R.string.no_internet);
                emptyView.setVisibility(View.VISIBLE);
                break;
            case UI_NO_MOVIES:
                loadingProgress.setVisibility(View.GONE);
                emptyView.setText(R.string.no_movies);
                emptyView.setVisibility(View.VISIBLE);
                break;
            case UI_COMPLETE:
                loadingProgress.setVisibility(View.GONE);
                emptyView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                break;
        }
    }
}
