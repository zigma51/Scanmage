package com.trailblazing.scanmage;

import android.content.Intent;
import android.net.Uri;
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
        fileAdapter.setOnDeleteListener((ScannedFile file) -> {
            File fileF = new File(file.filePath);
            fileF.delete();
            AppDatabase.getInstance(getContext()).filesDao().delete(file);
            refresh();
        });

        fileAdapter.setOnShareListener((ScannedFile file) -> {
            Intent intentShareFile = new Intent(Intent.ACTION_SEND);
            File fileWithinMyDir = new File(file.filePath);

            if (fileWithinMyDir.exists()) {
                intentShareFile.setType("application/pdf");
                intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse(file.filePath));
//                intentShareFile.putExtra(Intent.EXTRA_STREAM, file.filePath);

                intentShareFile.putExtra(Intent.EXTRA_SUBJECT,
                        "Sharing File...");

                startActivity(Intent.createChooser(intentShareFile, "Share File"));
            }
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