package com.trailblazing.scanmage;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
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
    private SearchView searchFiles;

    public FileListFragment() {
        super(R.layout.fragment_file_list);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listFiles = view.findViewById(R.id.fileList);
        searchFiles = view.findViewById(R.id.search);
        searchFiles.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                fileAdapter.getFilter().filter(newText);
                return false;
            }
        });
        listFiles.setLayoutManager(new LinearLayoutManager(getActivity()));
        refresh();

    }

    private void refresh() {
        getFileList();
        fileAdapter = new MyItemRecyclerViewAdapter(getContext(), files);

        fileAdapter.setOnClickListener((ScannedFile scannedFile) -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);

            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            File file;

            final long token = Binder.clearCallingIdentity();
            try {
                file = new File(scannedFile.filePath);
                Uri uri = FileProvider.getUriForFile(getActivity().getApplicationContext()
                        , "com.trailblazing.scanmage.provider", file);
                intent.setDataAndType(uri, "application/pdf");
                PackageManager pm = getActivity().getPackageManager();
                if (intent.resolveActivity(pm) != null) {
                    startActivity(intent);
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        });

        fileAdapter.setOnDeleteListener((ScannedFile file) -> {
            File fileF = new File(file.filePath);
            fileF.delete();
            AppDatabase.getInstance(getContext()).filesDao().delete(file);
            refresh();
        });

        fileAdapter.setOnShareListener((ScannedFile scannedFile) -> {
//            Intent intent = new Intent(Intent.ACTION_VIEW);
//            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//
//            File file;
//

            Uri path = FileProvider.getUriForFile(getActivity().getApplicationContext(), "com.trailblazing.scanmage.provider", new File(scannedFile.filePath));
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, path);
            shareIntent.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            shareIntent.setType("application/pdf");
            startActivity(Intent.createChooser(shareIntent, "Share"));

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