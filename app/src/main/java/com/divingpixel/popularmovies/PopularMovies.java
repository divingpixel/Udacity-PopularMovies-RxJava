package com.divingpixel.popularmovies;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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

public class PopularMovies extends AppCompatActivity implements InternetCheck.ConnectionChangeListener, TheMovieDB.DownloadFinishListener {

    //UI states constants
    private static final int UI_LOADING = 0;
    private static final int UI_NO_INTERNET = -1;
    private static final int UI_NO_DATA = -2;
    private static final int UI_NO_MOVIES = -3;
    private static final int UI_COMPLETE = 1;

    public static final String TheMovieDB_BASE_URL = "https://api.themoviedb.org/";
    public static final String API_KEY = "32a2be514060aa29a632774e0649f353";
    public static final String POSTER_PATH = "https://image.tmdb.org/t/p/";
    public static final String POSTER_SMALL = "w185/";
    public static final String POSTER_BIG = "w500/";

    private static final String LOG_TAG = PopularMovies.class.getSimpleName();
    public static final String INSTANCE_CATEGORY = "instance_category";
    private ArrayList<MyMovieEntry> movieList, popularList, topRatedList, favoriteList;
    private MovieAdapter movieAdapter;
    private MainViewModel viewModel;

    RecyclerView recyclerView;
    TextView emptyView;
    View loadingProgress;
    MoviesDatabase moviesDB;
    Context mainContext;

    private Menu actionMenu;
    private InternetCheck internetStatus;
    public static boolean isConnected;
    public static String category = Utils.CATEGORY_POPULAR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_screen);

        mainContext = this;
        moviesDB = MoviesDatabase.getInstance(mainContext);
        movieAdapter = new MovieAdapter(movieList);
        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        // set-up the views
        emptyView = findViewById(R.id.empty_view);
        loadingProgress = findViewById(R.id.loading_progress);
        recyclerView = findViewById(R.id.movie_list);

        //gets the internet connection status and downloads the movie list
        internetStatus = new InternetCheck(this.getApplication(), mainContext);
        isConnected = internetStatus.hasConnection();

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

        //gets the saved variables on rotation
        if (savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_CATEGORY)) {
            category = savedInstanceState.getString(INSTANCE_CATEGORY, Utils.CATEGORY_POPULAR);
        } else {
            if (isConnected) {
                updateUi(UI_LOADING);
                viewModel.updateMovieDatabase(moviesDB, mainContext);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isConnected) {
            setUpViewModel();
        } else {
            updateUi(UI_NO_INTERNET);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(INSTANCE_CATEGORY, category);
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
        setUpMenuButtons();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_popular) {
            if (!category.equals(Utils.CATEGORY_POPULAR)) {
                this.setTitle(R.string.menu_popular);
                category = Utils.CATEGORY_POPULAR;
            }
        }
        if (id == R.id.action_topRated) {
            if (!category.equals(Utils.CATEGORY_TOP_RATED)) {
                this.setTitle(R.string.menu_topRated);
                category = Utils.CATEGORY_TOP_RATED;
            }
        }
        if (id == R.id.action_favorites) {
            if (!category.equals(Utils.CATEGORY_FAVORITES)) {

                this.setTitle(R.string.menu_favorites);
                category = Utils.CATEGORY_FAVORITES;
            }
        }
        setUpMenuButtons();
        if (isConnected) updateViewModel();
        return super.onOptionsItemSelected(item);
    }

    private void setUpMenuButtons() {
        actionMenu.findItem(R.id.action_favorites).setIcon(R.drawable.ic_favorite_menu_24dp);
        actionMenu.findItem(R.id.action_popular).setIcon(R.drawable.ic_popular_menu_24dp);
        actionMenu.findItem(R.id.action_topRated).setIcon(R.drawable.ic_star_menu_24dp);
        switch (category) {
            case Utils.CATEGORY_FAVORITES:
                actionMenu.findItem(R.id.action_favorites).setIcon(R.drawable.ic_favorite_menu_selected_24dp);
                break;
            case Utils.CATEGORY_POPULAR:
                actionMenu.findItem(R.id.action_popular).setIcon(R.drawable.ic_popular_menu_selected_24dp);
                break;
            case Utils.CATEGORY_TOP_RATED:
                actionMenu.findItem(R.id.action_topRated).setIcon(R.drawable.ic_star_menu_selected_24dp);
                break;
        }
    }

    private void setUpViewModel() {
        viewModel.getMovies(Utils.CATEGORY_FAVORITES).observe(this, new Observer<List<MyMovieEntry>>() {
            @Override
            public void onChanged(@Nullable List<MyMovieEntry> myMovieEntries) {
                favoriteList = (ArrayList<MyMovieEntry>) myMovieEntries;
                updateViewModel();
            }
        });
        viewModel.getMovies(Utils.CATEGORY_POPULAR).observe(this, new Observer<List<MyMovieEntry>>() {
            @Override
            public void onChanged(@Nullable List<MyMovieEntry> myMovieEntries) {
                popularList = (ArrayList<MyMovieEntry>) myMovieEntries;
                updateViewModel();
            }
        });
        viewModel.getMovies(Utils.CATEGORY_TOP_RATED).observe(this, new Observer<List<MyMovieEntry>>() {
            @Override
            public void onChanged(@Nullable List<MyMovieEntry> myMovieEntries) {
                topRatedList = (ArrayList<MyMovieEntry>) myMovieEntries;
                updateViewModel();
            }
        });
    }

    private void updateViewModel() {
        switch (category) {
            case Utils.CATEGORY_FAVORITES: {
                movieList = favoriteList;
                break;
            }
            case Utils.CATEGORY_POPULAR: {
                movieList = popularList;
                break;
            }
            case Utils.CATEGORY_TOP_RATED: {
                movieList = topRatedList;
                break;
            }
        }
        movieAdapter.setMovies(movieList);
        if (movieAdapter.getItemCount() == 0) updateUi(UI_NO_MOVIES);
        else
            updateUi(UI_COMPLETE);
    }

    @Override
    public void onConnectionChange(final boolean status) {
        AppExecutors.getInstance().mainThread().execute(new Runnable() {
            @Override
            public void run() {
                Log.d(LOG_TAG, "CONNECTION STATUS CHANGED TO : " + status);
                if (!isConnected && status) {
                    isConnected = true;
                    updateUi(UI_LOADING);
                    viewModel.updateMovieDatabase(moviesDB, mainContext);
                    setUpViewModel();
                } else if (!status) {
                    isConnected = false;
                    updateUi(UI_NO_INTERNET);
                }
            }
        });
    }

    @Override
    public void onDownloadFinish(final Boolean status, String caller) {
        AppExecutors.getInstance().mainThread().execute(new Runnable() {
            @Override
            public void run() {
                if (status) {
                    updateUi(UI_COMPLETE);
                    updateViewModel();
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
