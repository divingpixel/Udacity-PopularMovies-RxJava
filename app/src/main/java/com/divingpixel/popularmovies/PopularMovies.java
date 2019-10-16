package com.divingpixel.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.divingpixel.popularmovies.database.MoviesDatabase;
import com.divingpixel.popularmovies.datamodel.MyMovieEntry;
import com.divingpixel.popularmovies.internet.InternetCheck;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class PopularMovies extends AppCompatActivity implements InternetCheck.ConnectionChangeListener {

    //UI states constants
    private static final int UI_LOADING = 0;
    private static final int UI_NO_INTERNET = -1;
    private static final int UI_NO_DATA = -2;
    private static final int UI_NO_MOVIES = -3;
    private static final int UI_COMPLETE = 1;

    static final String CATEGORY_FAVORITES = "favorites";
    public static final String CATEGORY_TOP_RATED = "top_rated";
    public static final String CATEGORY_POPULAR = "popular";

    private static final String LOG_TAG = PopularMovies.class.getSimpleName();
    public static final String INSTANCE_CATEGORY = "instance_category";
    private List<MyMovieEntry> movieList;
    private MovieAdapter movieAdapter;
    private MainViewModel viewModel;

    RecyclerView recyclerView;
    TextView emptyView;
    View loadingProgress;
    MoviesDatabase moviesDB;
    Context mainContext;

    private DisposableObserver<List<MyMovieEntry>> disposable;
    private Menu actionMenu;
    private InternetCheck internetStatus;
    public static boolean isConnected;
    public static String category = CATEGORY_POPULAR;

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
            category = savedInstanceState.getString(INSTANCE_CATEGORY, CATEGORY_POPULAR);
        } else {
            viewModel.updateMovieDatabase();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isConnected) {
            setUpMovieList();
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
        internetStatus.disable();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        super.onDestroy();
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
            if (!category.equals(CATEGORY_POPULAR)) {
                this.setTitle(R.string.menu_popular);
                category = CATEGORY_POPULAR;
            }
        }
        if (id == R.id.action_topRated) {
            if (!category.equals(CATEGORY_TOP_RATED)) {
                this.setTitle(R.string.menu_topRated);
                category = CATEGORY_TOP_RATED;
            }
        }
        if (id == R.id.action_favorites) {
            if (!category.equals(CATEGORY_FAVORITES)) {
                this.setTitle(R.string.menu_favorites);
                category = CATEGORY_FAVORITES;
            }
        }
        setUpMenuButtons();
        setUpMovieList();
        return super.onOptionsItemSelected(item);
    }

    private void setUpMenuButtons() {
        actionMenu.findItem(R.id.action_favorites).setIcon(R.drawable.ic_favorite_menu_24dp);
        actionMenu.findItem(R.id.action_popular).setIcon(R.drawable.ic_popular_menu_24dp);
        actionMenu.findItem(R.id.action_topRated).setIcon(R.drawable.ic_star_menu_24dp);
        switch (category) {
            case CATEGORY_FAVORITES:
                actionMenu.findItem(R.id.action_favorites).setIcon(R.drawable.ic_favorite_menu_selected_24dp);
                break;
            case CATEGORY_POPULAR:
                actionMenu.findItem(R.id.action_popular).setIcon(R.drawable.ic_popular_menu_selected_24dp);
                break;
            case CATEGORY_TOP_RATED:
                actionMenu.findItem(R.id.action_topRated).setIcon(R.drawable.ic_star_menu_selected_24dp);
                break;
        }
    }

    private void setUpMovieList() {
        updateUi(UI_LOADING);
        disposable = viewModel.getMovies(category)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<List<MyMovieEntry>>() {
                    @Override
                    public void onNext(List<MyMovieEntry> movieEntries) {
                        movieList = movieEntries;
                        movieAdapter.setMovies(movieList);
                        movieAdapter.notifyDataSetChanged();
                        updateUi(UI_COMPLETE);
                    }

                    @Override
                    public void onError(Throwable e) {
                        updateUi(UI_NO_DATA);
                    }

                    @Override
                    public void onComplete() {
                        if (movieAdapter.getItemCount() == 0) updateUi(UI_NO_MOVIES);
                    }
                });
    }

    @Override
    public void onConnectionChange(final boolean status) {
        Log.d(LOG_TAG, "CONNECTION STATUS CHANGED TO : " + status);
        if (!isConnected && status) {
            isConnected = true;
            updateUi(UI_LOADING);
            viewModel.updateMovieDatabase();
            setUpMovieList();
        } else if (!status) {
            isConnected = false;
            updateUi(UI_NO_INTERNET);
        }
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
