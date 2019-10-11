package com.divingpixel.popularmovies;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.divingpixel.popularmovies.database.MyMovieEntry;
import com.squareup.picasso.Picasso;

import java.util.List;

import static com.divingpixel.popularmovies.PopularMovies.CATEGORY_FAVORITES;
import static com.divingpixel.popularmovies.PopularMovies.CATEGORY_POPULAR;
import static com.divingpixel.popularmovies.internet.TheMovieDBService.POSTER_PATH;
import static com.divingpixel.popularmovies.internet.TheMovieDBService.POSTER_SMALL;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MyViewHolder> {

    private List<MyMovieEntry> mMovies;

    MovieAdapter(List<MyMovieEntry> movies) {
        setMovies(movies);
    }

    void setMovies(List<MyMovieEntry> movies) {
        mMovies = movies;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_layout, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder viewHolder, int position) {
        MyMovieEntry movie = mMovies.get(position);
        viewHolder.favorite.setVisibility(View.GONE);
        int index;
        if (PopularMovies.category.equals(CATEGORY_POPULAR)) index = movie.getPopIndex();
        else index = movie.getTopIndex();
        String movieTitle = "#" + index + " : " + movie.getTitle();
        if (movie.isFavorite()) viewHolder.favorite.setVisibility(View.VISIBLE);

        if (movie.isFavorite() && (PopularMovies.category.equals(CATEGORY_FAVORITES))) {
            movieTitle = movie.getTitle();
            viewHolder.favorite.setVisibility(View.GONE);
        }

        viewHolder.title.setText(movieTitle);
        viewHolder.rating.setRating(movie.getRating() / 2);
        String posterUrl = POSTER_PATH + POSTER_SMALL + movie.getPosterUrl();
        Picasso.get().load(posterUrl).into(viewHolder.poster);
    }

    @Override
    public int getItemCount() {
        if (mMovies != null)
            return mMovies.size();
        else
            return 0;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView poster;
        private TextView title;
        private ImageView favorite;
        private RatingBar rating;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.movie_poster);
            title = itemView.findViewById(R.id.movie_title);
            favorite = itemView.findViewById(R.id.movie_favorite);
            rating = itemView.findViewById(R.id.movie_rating);
        }
    }

}
