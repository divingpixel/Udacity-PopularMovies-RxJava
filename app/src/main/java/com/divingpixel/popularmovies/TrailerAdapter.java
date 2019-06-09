package com.divingpixel.popularmovies;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.MyViewHolder> {

    private ArrayList<MovieTrailer> mTrailers;

    TrailerAdapter (ArrayList<MovieTrailer> trailers){
        mTrailers = trailers;
    }

    @NonNull
    @Override
    public TrailerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.trailer_layout, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TrailerAdapter.MyViewHolder myViewHolder, int position) {
        MovieTrailer movieTrailer = mTrailers.get(position);
        myViewHolder.title.setText(movieTrailer.getTitle());
    }

    @Override
    public int getItemCount() {
        if (mTrailers != null)
            return mTrailers.size();
        else
            return 0;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView title;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.trailer_title);
        }
    }
}
