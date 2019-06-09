package com.divingpixel.popularmovies;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.MyViewHolder> {

    private ArrayList<MovieReview> mReviews;

    ReviewAdapter (ArrayList<MovieReview> reviews){
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
        MovieReview movieReview = mReviews.get(position);
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
