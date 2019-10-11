package com.divingpixel.popularmovies;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.divingpixel.popularmovies.internet.TheMovieDBReview;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.MyViewHolder> {

    private List<TheMovieDBReview> mReviews;

    ReviewAdapter (List<TheMovieDBReview> reviews){
        mReviews = reviews;
    }

    @NonNull
    @Override
    public ReviewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.review_layout, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewAdapter.MyViewHolder myViewHolder, int position) {
        TheMovieDBReview movieReview = mReviews.get(position);
        myViewHolder.text.setText(movieReview.getText());
        myViewHolder.author.setText(movieReview.getReviewer());
    }

    @Override
    public int getItemCount() {
        if (mReviews != null)
            return mReviews.size();
        else
            return 0;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView text, author;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.review_text);
            author = itemView.findViewById(R.id.review_author);
        }
    }
}
