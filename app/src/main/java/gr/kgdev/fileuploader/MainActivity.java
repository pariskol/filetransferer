package gr.kgdev.fileuploader;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.obsez.android.lib.filechooser.ChooserDialog;

import org.json.JSONArray;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import gr.kgdev.fileuploader.utils.MyHttpClient;
import gr.kgdev.fileuploader.utils.StorageUtil;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

//    private ArrayAdapter<String> adapter;
    private MyHttpClient httpClient;
    private SelectableListView listView;
    private ListView listView2;
    private FloatingActionButton addFab, removeFab, uploadFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        httpClient = new MyHttpClient();
        listView = (SelectableListView) findViewById(R.id.listview);

        addFab = findViewById(R.id.add);
        addFab.setOnClickListener(this::openFileChooser);

        removeFab = findViewById(R.id.remove);
        removeFab.setOnClickListener(view -> {
            listView.getOnItemClickListener().onItemClick(listView, null, -1, 0);
            listView.getAdapter().remove(listView.getSelectedItem());
            listView.getAdapter().notifyDataSetChanged();
        });

        uploadFab = findViewById(R.id.upload);
        uploadFab.setOnClickListener(v -> {
            DialogService.showInputDialog(this, "Enter Receiver",
            (input) -> onUpload(input));
        });


        listView2 = (ListView) findViewById(R.id.listview2);
        ArrayAdapter<String> adapter2  = new ArrayAdapter<String>(this,
                R.layout.list_item, new ArrayList<>());
        listView2.setAdapter(adapter2);
        getFilesToDownload(adapter2);

        FloatingActionButton refreshFab = findViewById(R.id.refresh);
        refreshFab.setOnClickListener(v -> getFilesToDownload(adapter2));
        FloatingActionButton downloadFab = findViewById(R.id.download);
        downloadFab.setOnClickListener(v -> {
            DialogService.showLoadingDialog(this, "Downloading Files");
            httpClient.executeAsync(() -> {
                for (int i = 0; i < adapter2.getCount(); i++) {
                    try {
                        httpClient.download(adapter2.getItem(i));
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
                MainActivity.this.runOnUiThread(() -> {
                    DialogService.showInfoDialog(MainActivity.this, "Download finished");
                });
            });

        });
    }

    private void getFilesToDownload(ArrayAdapter<String> adapter2) {
        adapter2.clear();
        httpClient.executeAsync(() -> {
            try {
                Log.i(TAG, String.valueOf(AppCache.getAppUser().getId()));
                JSONArray json = (JSONArray) httpClient.get("/get/files?USER_ID=" + AppCache.getAppUser().getId());
                List<String> files = new ArrayList<>();
                for (int i = 0; i < json.length(); i++)
                    files.add(json.getString(i));

                MainActivity.this.runOnUiThread(() -> {
                    adapter2.addAll(files);
                    adapter2.notifyDataSetChanged();
                });
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        });
    }

    private void onUpload(String toUser) {
        uploadFab.setEnabled(false);
        addFab.setEnabled(false);
        removeFab.setEnabled(false);
        DialogService.showLoadingDialog(this, "Uploading Files");
        httpClient.executeAsync(() -> {
            List<String> successfulUploads = new ArrayList<>();
            for (int i = 0; i < listView.getAdapter().getCount(); i++) {
                final String path = listView.getAdapter().getItem(i);
                try {
                    httpClient.upload(path, toUser);
                    successfulUploads.add(path);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
            MainActivity.this.runOnUiThread(() -> {
                successfulUploads.forEach(x -> listView.getAdapter().remove(x));
                listView.getAdapter().notifyDataSetChanged();
                if (!listView.getAdapter().isEmpty())
                    DialogService.showErrorDialog(MainActivity.this, "Some files failed to be uploaded! Check List!");
                else
                    DialogService.showInfoDialog(MainActivity.this, "All files have been uploaded");

                uploadFab.setEnabled(true);
                addFab.setEnabled(true);
                removeFab.setEnabled(true);
            });
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            DialogService.showInputDialog(this,
                    AppCache.getDownloadLocation(),
                    "Set Download Location",
                    (input) -> AppCache.setDownloadLocation(input));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void openFileChooser(View view) {
        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            openFileChooser(view);
            return;
        }

        permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            openFileChooser(view);
            return;
        }
//            DialogService.showInfoDialog(this, this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
        // Your app already has the permission to access files and folders
        // so you can simply open FileChooser here.
        new ChooserDialog(MainActivity.this)
                .withChosenListener(new ChooserDialog.Result() {
                    @Override
                    public void onChoosePath(String path, File pathFile) {
//                                Toast.makeText(MainActivity.this, "FILE: " + path, Toast.LENGTH_SHORT).show();
                        listView.getAdapter().add(path);
                        listView.getAdapter().notifyDataSetInvalidated();
                    }
                })
                // to handle the back key pressed or clicked outside the dialog:
                .withOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        Log.d("CANCEL", "CANCEL");
                        dialog.cancel(); // MUST have
                    }
                })
                .build()
                .show();
    }
}
