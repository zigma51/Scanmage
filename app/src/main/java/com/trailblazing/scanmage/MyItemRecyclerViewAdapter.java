package com.trailblazing.scanmage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.annotations.NotNull;
import com.trailblazing.scanmage.model.ScannedFile;

import java.io.File;
import java.util.List;

public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {
    private Context context;
    private List<ScannedFile> files;
    private OnClickListener onClickListener;
    private OnDeleteListener onDeleteListener;
    private OnShareListener onShareListener;

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

    public void setOnShareListener(OnShareListener onShareListener) {
        this.onShareListener = onShareListener;
    }


    public interface OnDeleteListener {
        void onDelete(ScannedFile myFile);
    }

    public interface OnShareListener {
        void onShare(ScannedFile myFile);
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

        holder.deleteBtn.setOnClickListener(v -> onDeleteListener.onDelete(files.get(position)));

        holder.shareBtn.setOnClickListener(v -> onShareListener.onShare(files.get(position)));
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView date;
        public ImageView shareBtn;
        public ImageView deleteBtn;

        public ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.fileTitle);
            date = view.findViewById(R.id.date);
            shareBtn = view.findViewById(R.id.share);
            deleteBtn = view.findViewById(R.id.delete);
        }
    }
}