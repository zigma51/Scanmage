package com.trailblazing.scanmage;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import com.bumptech.glide.Glide;
import com.google.firebase.database.annotations.NotNull;
import com.trailblazing.scanmage.model.ScannedFile;

import java.io.File;
import java.util.List;

public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {
    private Context context;
    private List<ScannedFile> files;
    private OnClickListener onClickListener;
    private OnDeleteListener onDeleteListener;

    public MyItemRecyclerViewAdapter(Context context, List<ScannedFile> scannedFiles) {
        this.context = context;
        this.files = scannedFiles;
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setOnDeleteListener(OnDeleteListener onDeleteListener) {
        this.onDeleteListener = onDeleteListener;
    }


    public interface OnDeleteListener {
        void onDelete(ScannedFile myFile);
    }

    public interface OnClickListener {
        void onClick(ScannedFile myFile);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_file, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.title.setText(new File(files.get(position).filePath).getName());
        holder.date.setText(new File(files.get(position).date).getName());
        String path = files.get(position).filePath;
        File file = new File(path);
        Uri imageUri = Uri.fromFile(file);
        Glide.with(holder.itemView.getContext()).load(imageUri).into(holder.snapshot);

        holder.deleteBtn.setOnClickListener(v -> onDeleteListener.onDelete(files.get(position)));

        holder.downloadBtn.setOnClickListener(v -> Toast.makeText(context, "Download button clicked!", Toast.LENGTH_SHORT).show());

        holder.shareBtn.setOnClickListener(v -> Toast.makeText(context, "Share button clicked!", Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView date;
        public ImageView snapshot;
        public ImageView shareBtn;
        public ImageView downloadBtn;
        public ImageView deleteBtn;

        public ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.fileTitle);
            date = view.findViewById(R.id.date);
            snapshot = view.findViewById(R.id.snapshot);
            shareBtn = view.findViewById(R.id.share);
            downloadBtn = view.findViewById(R.id.download);
            deleteBtn = view.findViewById(R.id.delete);
        }
    }
}