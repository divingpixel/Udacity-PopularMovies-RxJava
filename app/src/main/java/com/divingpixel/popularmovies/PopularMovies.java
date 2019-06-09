package com.divingpixel.popularmovies;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import com.divingpixel.popularmovies.database.MoviesDatabase;
import com.divingpixel.popularmovies.database.MyMovieEntry;
import com.divingpixel.popularmovies.internet.InternetCheck;
import com.divingpixel.popularmovies.internet.TheMovieDB;

import java.util.ArrayList;
import java.util.List;

public class PopularMovies extends AppCompatActivity implements InternetCheck.ConnectionChangeListener, TheMovieDB.DownloadFinishListener {

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
    private Menu actionMenu;
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
            startDate = savedInstanceState.getString(INSTANCE_START_DATE, Utils.makeTimeStamp());
        }

        mainContext = this;
        moviesDB = MoviesDatabase.getInstance(getApplicationContext());
        movieAdapter = new MovieAdapter(movieList);

        //gets the internet connection status and downloads the movie list
        internetStatus = new InternetCheck(this.getApplication(), mainContext);
        isConnected = internetStatus.hasConnection();

        // set-up the views
        emptyView = findViewById(R.id.empty_view);
        loadingProgress = findViewById(R.id.loading_progress);
        recyclerView = findViewById(R.id.movie_list);
        updateUi(UI_LOADING);

        //set up the recyclerView column count according to the orientation
        int orientation = this.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setLayoutManager(new GridLayoutManager(getBaseContext(), 3));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(getBaseContext(), 5));
        }
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(movieAdapter);

        // set-up the click listeners for the elements in the recyclerView
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

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!Utils.makeTimeStamp().equalsIgnoreCase(startDate)) {
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
        internetStatus.disable();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.settings_menu, menu);
        actionMenu = menu;
        actionMenu.findItem(R.id.action_popular).setIcon(R.drawable.ic_popular_menu_selected_24dp);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        actionMenu.findItem(R.id.action_popular).setIcon(R.drawable.ic_popular_menu_24dp);
        actionMenu.findItem(R.id.action_topRated).setIcon(R.drawable.ic_star_menu_24dp);
        actionMenu.findItem(R.id.action_favorites).setIcon(R.drawable.ic_favorite_menu_24dp);
        if (id == R.id.action_popular) {
            this.setTitle(R.string.menu_popular);
            showFavorites = false;
            actionMenu.findItem(R.id.action_popular).setIcon(R.drawable.ic_popular_menu_selected_24dp);
            if (!category.equalsIgnoreCase(Utils.CATEGORY_POPULAR)) {
                category = Utils.CATEGORY_POPULAR;
                updateMovies();
            }
        }
        if (id == R.id.action_topRated) {
            this.setTitle(R.string.menu_topRated);
            actionMenu.findItem(R.id.action_topRated).setIcon(R.drawable.ic_star_menu_selected_24dp);
            showFavorites = false;
            if (!category.equalsIgnoreCase(Utils.CATEGORY_TOP_RATED)) {
                category = Utils.CATEGORY_TOP_RATED;
                updateMovies();
            }
        }
        if (id == R.id.action_favorites) {
            this.setTitle(R.string.menu_favorites);
            actionMenu.findItem(R.id.action_favorites).setIcon(R.drawable.ic_favorite_menu_selected_24dp);
            showFavorites = true;
        }
        setUpViewModel();
        return super.onOptionsItemSelected(item);
    }

    private void updateMovies() {
        if (isConnected) {
            updateUi(UI_LOADING);
            AppExecutors.getInstance().networkIO().execute(new Runnable() {
                @Override
                public void run() {
                    startDate = Utils.makeTimeStamp();
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
                if ((movieList.size() == 0) && showFavorites) updateUi(UI_NO_MOVIES);
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
