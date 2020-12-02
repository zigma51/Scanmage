package com.trailblazing.scanmage;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.trailblazing.scanmage.activity.LoginActivity;
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
        setHasOptionsMenu(true);
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
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle("Delete PDF");
            alert.setMessage("Are you sure you want to delete?");
            alert.setPositiveButton(android.R.string.yes, (dialog, which) -> {
                File fileF = new File(file.filePath);
                fileF.delete();
                AppDatabase.getInstance(getContext()).filesDao().delete(file);
                refresh();
            });
            alert.setNegativeButton(android.R.string.no, (dialog, which) -> dialog.cancel());
            alert.show();
        });

        fileAdapter.setOnShareListener((ScannedFile scannedFile) -> {
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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}