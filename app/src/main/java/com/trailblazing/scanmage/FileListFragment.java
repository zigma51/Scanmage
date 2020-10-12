package com.trailblazing.scanmage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.trailblazing.scanmage.database.AppDatabase;
import com.trailblazing.scanmage.model.ScannedFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class FileListFragment extends Fragment {

    private static final String TAG = "FileListFragment";
    private RecyclerView listFiles;
    private MyItemRecyclerViewAdapter fileAdapter;
    List<ScannedFile> files = new ArrayList<>();

    public FileListFragment() {
        super(R.layout.fragment_file_list);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listFiles = view.findViewById(R.id.fileList);
        listFiles.setLayoutManager(new LinearLayoutManager(getActivity()));
        refresh();

    }

    private void refresh() {
        getFileList();
        fileAdapter = new MyItemRecyclerViewAdapter(getContext(), files);
        fileAdapter.setOnDeleteListener(file -> {
            File fileF = new File(file.filePath);
            fileF.delete();
            AppDatabase.getInstance(getContext()).filesDao().delete(file);
            refresh();
        });

        listFiles.setAdapter(fileAdapter);
    }

    private void getFileList() {
        files = AppDatabase.getInstance(getContext()).filesDao().getAll();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                refresh();
            }
        }
    }
}