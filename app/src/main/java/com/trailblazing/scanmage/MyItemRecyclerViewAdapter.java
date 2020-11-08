package com.trailblazing.scanmage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.annotations.NotNull;
import com.trailblazing.scanmage.model.ScannedFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> implements Filterable {
    private Context context;
    private List<ScannedFile> files;
    private List<ScannedFile> datasetFull;
    private OnClickListener onClickListener;
    private OnDeleteListener onDeleteListener;
    private OnShareListener onShareListener;

    public MyItemRecyclerViewAdapter(Context context, List<ScannedFile> scannedFiles) {
        this.context = context;
        this.files = scannedFiles;
        datasetFull = new ArrayList<>(scannedFiles);
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

    @Override
    public Filter getFilter() {
        return datasetFilter;
    }

    private final Filter datasetFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<ScannedFile> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(datasetFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (ScannedFile scannedFile : datasetFull) {
                    if ((new File(scannedFile.filePath).getName()).toLowerCase().contains(filterPattern)) {
                        filteredList.add(scannedFile);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            files.clear();
            files.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

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