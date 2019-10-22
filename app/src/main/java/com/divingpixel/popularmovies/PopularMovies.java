package com.divingpixel.popularmovies;

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

import com.divingpixel.popularmovies.datamodel.MyMovieEntry;
import com.divingpixel.popularmovies.internet.ConnectionStatus;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class PopularMovies extends AppCompatActivity {

    //UI states constants
    private static final int UI_LOADING = 0;
    private static final int UI_NO_INTERNET = -1;
    private static final int UI_NO_DATA = -2;
    private static final int UI_NO_MOVIES = -3;
    private static final int UI_COMPLETE = 1;

    //movie categories
    static final String CATEGORY_FAVORITES = "favorites";
    static final String CATEGORY_TOP_RATED = "top_rated";
    static final String CATEGORY_POPULAR = "popular";

    //data models
    private List<MyMovieEntry> movieList;
    private MovieAdapter movieAdapter;
    private MainViewModel viewModel;
    private DisposableObserver<List<MyMovieEntry>> disposable;

    //UI elements
    private RecyclerView recyclerView;
    private TextView emptyView;
    private View loadingProgress;
    private Menu actionMenu;

    public static String category = CATEGORY_POPULAR;
    public static final String INSTANCE_CATEGORY = "instance_category";
    private static final String LOG_TAG = PopularMovies.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_screen);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setLogo(R.drawable.ic_launcher_foreground);
            getSupportActionBar().setDisplayUseLogoEnabled(true);
        }

        movieAdapter = new MovieAdapter(movieList);
        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        // set-up the views
        emptyView = findViewById(R.id.empty_view);
        loadingProgress = findViewById(R.id.loading_progress);
        recyclerView = findViewById(R.id.movie_list);

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
            Log.i(LOG_TAG, "DOWNLOADING MOVIES FROM THE INTERNET");
            viewModel.fillDatabase(CATEGORY_POPULAR);
            viewModel.fillDatabase(CATEGORY_TOP_RATED);
        }

    }


    @Override
    protected void onStart() {
        super.onStart();
        if (ConnectionStatus.isConnected(getApplicationContext())) {
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
        if (ConnectionStatus.isConnected(getApplicationContext())) {
            disposable = viewModel.getMovies(category)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableObserver<List<MyMovieEntry>>() {
                        @Override
                        public void onNext(List<MyMovieEntry> movieEntries) {
                            movieList = movieEntries;
                            movieAdapter.setMovies(movieList);
                            movieAdapter.notifyDataSetChanged();
                            if (movieAdapter.getItemCount() == 0)
                                updateUi(UI_NO_MOVIES);
                            else
                                updateUi(UI_COMPLETE);
                        }

                        @Override
                        public void onError(Throwable e) {
                            updateUi(UI_NO_DATA);
                        }

                        @Override
                        public void onComplete() {
                        }
                    });
        } else {
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
