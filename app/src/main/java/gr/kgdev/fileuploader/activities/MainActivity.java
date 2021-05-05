package gr.kgdev.fileuploader.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import java.util.List;

import gr.kgdev.fileuploader.AppCache;
import gr.kgdev.fileuploader.R;
import gr.kgdev.fileuploader.views.Dialogs;
import gr.kgdev.fileuploader.views.SelectableListView;
import gr.kgdev.fileuploader.utils.MyHttpClient;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

//    private ArrayAdapter<String> adapter;
    private MyHttpClient httpClient;
    private SelectableListView upoadListView;
    private ListView downloadlistView;
    private FloatingActionButton addFab, removeFab, uploadFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        httpClient = new MyHttpClient();
        upoadListView = (SelectableListView) findViewById(R.id.listview);

        addFab = findViewById(R.id.add);
        addFab.setOnClickListener(this::openFileChooser);

        removeFab = findViewById(R.id.remove);
        removeFab.setOnClickListener(view -> {
            upoadListView.getOnItemClickListener().onItemClick(upoadListView, null, -1, 0);
            upoadListView.getAdapter().remove(upoadListView.getSelectedItem());
            upoadListView.getAdapter().notifyDataSetChanged();
        });

        uploadFab = findViewById(R.id.upload);
        uploadFab.setOnClickListener(v -> {
            Dialogs.showInputDialog(this, "Enter Receiver",
            (input) -> onUpload(input));
        });


        downloadlistView = (ListView) findViewById(R.id.listview2);
        ArrayAdapter<String> downloadListViewAdapter  = new ArrayAdapter<String>(this,
                R.layout.list_item, new ArrayList<>());
        downloadlistView.setAdapter(downloadListViewAdapter);
        getFilesToDownload(downloadListViewAdapter);

        FloatingActionButton refreshFab = findViewById(R.id.refresh);
        refreshFab.setOnClickListener(v -> getFilesToDownload(downloadListViewAdapter));
        FloatingActionButton downloadFab = findViewById(R.id.download);
        downloadFab.setOnClickListener(v -> {
            Dialogs.showLoadingDialog(this, "Downloading Files");
            httpClient.executeAsync(() -> {
                for (int i = 0; i < downloadListViewAdapter.getCount(); i++) {
                    try {
                        httpClient.download(downloadListViewAdapter.getItem(i));
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
                MainActivity.this.runOnUiThread(() -> {
                    Dialogs.showInfoDialog(MainActivity.this, "Download finished");
                });
            });

        });

        askForPermissions();
    }

    private void getFilesToDownload(ArrayAdapter<String> adapter) {
        adapter.clear();
        httpClient.executeAsync(() -> {
            try {
                Log.i(TAG, String.valueOf(AppCache.getAppUser().getId()));
                JSONArray json = (JSONArray) httpClient.get("/get/files?USER_ID=" + AppCache.getAppUser().getId());
                List<String> files = new ArrayList<>();
                for (int i = 0; i < json.length(); i++)
                    files.add(json.getString(i));

                MainActivity.this.runOnUiThread(() -> {
                    adapter.addAll(files);
                    adapter.notifyDataSetChanged();
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
        Dialogs.showLoadingDialog(this, "Uploading Files");
        httpClient.executeAsync(() -> {
            List<String> successfulUploads = new ArrayList<>();
            for (int i = 0; i < upoadListView.getAdapter().getCount(); i++) {
                final String path = upoadListView.getAdapter().getItem(i);
                try {
                    httpClient.upload(path, toUser);
                    successfulUploads.add(path);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
            MainActivity.this.runOnUiThread(() -> {
                successfulUploads.forEach(x -> upoadListView.getAdapter().remove(x));
                upoadListView.getAdapter().notifyDataSetChanged();
                if (!upoadListView.getAdapter().isEmpty())
                    Dialogs.showErrorDialog(MainActivity.this, "Some files failed to be uploaded! Check List!");
                else
                    Dialogs.showInfoDialog(MainActivity.this, "All files have been uploaded");

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
            Dialogs.showInputDialog(this,
                    AppCache.getDownloadLocation(),
                    "Set Download Location",
                    (input) -> AppCache.setDownloadLocation(input));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void askForPermissions() {
        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    public void openFileChooser(View view) {
        new ChooserDialog(MainActivity.this)
                .withChosenListener(new ChooserDialog.Result() {
                    @Override
                    public void onChoosePath(String path, File pathFile) {
//                                Toast.makeText(MainActivity.this, "FILE: " + path, Toast.LENGTH_SHORT).show();
                        upoadListView.getAdapter().add(path);
                        upoadListView.getAdapter().notifyDataSetInvalidated();
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
