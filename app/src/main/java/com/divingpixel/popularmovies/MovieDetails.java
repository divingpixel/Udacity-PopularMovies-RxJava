package com.divingpixel.popularmovies;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.divingpixel.popularmovies.database.MoviesDatabase;
import com.divingpixel.popularmovies.database.MyMovieEntry;
import com.divingpixel.popularmovies.internet.TheMovieDB;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MovieDetails extends AppCompatActivity implements TheMovieDB.DownloadFinishListener {

    private static final String LOG_TAG = MovieDetails.class.getSimpleName();
    // Extra for the item ID to be received in the intent
    public static final String EXTRA_MOVIE_ID = "movie_index";
    // Extra for the item ID to be received after rotation
    public static final String INSTANCE_MOVIE_ID = "instance_movieId";
    // Default value for the item id
    private static final int DEFAULT_MOVIE_ID = -1;
    public static final String CALLER_REVIEWS = "reviews";
    public static final String CALLER_TRAILERS = "trailers";

    private int movieId = DEFAULT_MOVIE_ID;
    private MyMovieEntry selectedMovie;
    private ArrayList<MovieReview> reviews = new ArrayList<>();
    private ReviewAdapter reviewAdapter;
    private ArrayList<MovieTrailer> trailers = new ArrayList<>();
    private TrailerAdapter trailerAdapter;
    private MoviesDatabase mDb;
    ImageView favoriteButton;
    RecyclerView reviewsList, trailersList;
    private Context thisContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_details);

        thisContext = this;
        mDb = MoviesDatabase.getInstance(getApplicationContext());

        //set-up the heart button
        favoriteButton = findViewById(R.id.detail_favorite);
        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedMovie.setFavorite(!selectedMovie.isFavorite());
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        mDb.myMovieDAO().updateMovie(selectedMovie);
                    }
                });
                setFavoriteButton();
            }
        });

        //retrieve the item id and sets up the interface
        Intent intent = getIntent();

        if (movieId == DEFAULT_MOVIE_ID && intent.hasExtra(EXTRA_MOVIE_ID)) {
            movieId = intent.getIntExtra(EXTRA_MOVIE_ID, DEFAULT_MOVIE_ID);
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_MOVIE_ID)) {
            movieId = savedInstanceState.getInt(INSTANCE_MOVIE_ID, DEFAULT_MOVIE_ID);
        }

        if (movieId != DEFAULT_MOVIE_ID) {
            // populate the UI
            MovieDetailsViewModelFactory factory = new MovieDetailsViewModelFactory(mDb, movieId);
            final MovieDetailsViewModel viewModel = ViewModelProviders.of(this, factory).get(MovieDetailsViewModel.class);
            viewModel.getMovie().observe(this, new Observer<MyMovieEntry>() {
                @Override
                public void onChanged(@Nullable MyMovieEntry movie) {
                    viewModel.getMovie().removeObserver(this);
                    selectedMovie = movie;
                    initUi();
                }
            });

            //setUp TRAILERS recyclerView
            trailersList = findViewById(R.id.detail_trailers_rv);
            trailersList.setLayoutManager(new LinearLayoutManager(getBaseContext()));
            trailersList.setItemAnimator(new DefaultItemAnimator());
            getTrailers();

            //setUp REVIEWS recyclerView
            reviewsList = findViewById(R.id.detail_reviews_rv);
            reviewsList.setLayoutManager(new LinearLayoutManager(getBaseContext()));
            reviewsList.setItemAnimator(new DefaultItemAnimator());
            getReviews();

        } else {
            finish();
            Toast.makeText(this, "Invalid movie entry", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(LOG_TAG, "SAVING STATE FOR THE ITEM WITH THE ID : " + movieId);
        outState.putInt(INSTANCE_MOVIE_ID, movieId);
        super.onSaveInstanceState(outState);
    }

    private void setFavoriteButton() {
        if (selectedMovie.isFavorite()) {
            favoriteButton.setImageResource(R.drawable.ic_favorite_24dp);
        } else {
            favoriteButton.setImageResource(R.drawable.ic_favorite_border_24dp);
        }
    }

    private void initUi() {
        ImageView poster = findViewById(R.id.detail_poster);
        String posterUrl = TheMovieDB.POSTER_PATH + TheMovieDB.POSTER_BIG + selectedMovie.getPosterUrl();
        Picasso.get().load(posterUrl).into(poster);

        TextView title = findViewById(R.id.detail_title);
        title.setText(selectedMovie.getTitle());

        TextView dateText = findViewById(R.id.detail_date);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date = new Date();
        try {
            date = dateFormat.parse(selectedMovie.getDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String dateString = getResources().getString(R.string.release_date) + " : " + DateFormat.format("MMMM dd, yyyy", date).toString();
        dateText.setText(dateString);

        TextView ratingText = findViewById(R.id.detail_rating_text);
        String ratingString = getResources().getString(R.string.rating) + " : " + selectedMovie.getRating();
        ratingText.setText(ratingString);

        RatingBar ratingBar = findViewById(R.id.detail_rating);
        ratingBar.setRating(selectedMovie.getRating() / 2);

        TextView synopsis = findViewById(R.id.detail_synopsis);
        synopsis.setText(selectedMovie.getSynopsis());

        setFavoriteButton();
    }

    private void getReviews() {
        if (PopularMovies.isConnected) {
            AppExecutors.getInstance().networkIO().execute(new Runnable() {
                @Override
                public void run() {
                    Log.i(LOG_TAG, "DOWNLOADING Reviews DATA FOR MOVIE ID " + movieId);
                    reviews = TheMovieDB.getReviews(movieId, thisContext, CALLER_REVIEWS);
                }
            });
        }
    }

    private void getTrailers() {
        if (PopularMovies.isConnected) {
            AppExecutors.getInstance().networkIO().execute(new Runnable() {
                @Override
                public void run() {
                    Log.i(LOG_TAG, "DOWNLOADING Trailers DATA FOR MOVIE ID " + movieId);
                    trailers = TheMovieDB.getTrailers(movieId, thisContext, CALLER_TRAILERS);
                }
            });
        }
    }

    @Override
    public void onDownloadFinish(Boolean status, final String caller) {
        AppExecutors.getInstance().mainThread().execute(new Runnable() {
            @Override
            public void run() {
                if (trailers.size() == 0) {
                    trailersList.setVisibility(View.GONE);
                    findViewById(R.id.title_trailers).setVisibility(View.GONE);
                } else if (caller.equalsIgnoreCase(CALLER_TRAILERS)) {
                    trailerAdapter = new TrailerAdapter(trailers);
                    trailerAdapter.notifyDataSetChanged();
                    trailersList.setAdapter(trailerAdapter);
                    trailersList.setVisibility(View.VISIBLE);
                    findViewById(R.id.title_trailers).setVisibility(View.VISIBLE);
                }
                if (reviews.size() == 0) {
                    reviewsList.setVisibility(View.GONE);
                    findViewById(R.id.title_reviews).setVisibility(View.GONE);
                } else if (caller.equalsIgnoreCase(CALLER_REVIEWS)) {
                    reviewAdapter = new ReviewAdapter(reviews);
                    reviewAdapter.notifyDataSetChanged();
                    reviewsList.setAdapter(reviewAdapter);
                    reviewsList.setVisibility(View.VISIBLE);
                    trailersList.addOnItemTouchListener(new RecyclerTouchListener(getBaseContext(), trailersList, new RecyclerTouchListener.ClickListener() {
                        @Override
                        public void onClick(View view, int position) {
                            String key = trailers.get(position).getKey();
                            //prepares two intents ome for the youtube app other for the browser in case the app doesn't exist
                            Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + key));
                            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + key));
                            try {
                                startActivity(appIntent);
                            } catch (ActivityNotFoundException ex) {
                                startActivity(webIntent);
                            }
                        }

                        @Override
                        public void onLongClick(View view, int position) {
                        }
                    }));
                    findViewById(R.id.title_reviews).setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
