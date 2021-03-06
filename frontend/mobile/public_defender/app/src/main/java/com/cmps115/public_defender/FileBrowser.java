package com.cmps115.public_defender;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.cmps115.public_defender.fileUtil.MediaFile;
import com.cmps115.public_defender.fileUtil.MimeUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Author: Payton
 */

public class FileBrowser extends ListActivity {
    private String path;
    private File dir;
    private List<String> values = new ArrayList<String>();
    private final int PICK_FILE_RESULT_CODE = 999;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Sets the "view" to be the following XML file.
        setContentView(R.layout.file_browser_activity);

        // Get this app's external cache directory.
        path = getExternalCacheDir().getAbsolutePath();
        // WHY CAN'T I SEE ANYTHING IN THIS DIRECTORY??
        //path = Environment.getExternalStorageDirectory().toString();
        Log.d("PATH IS: ", path);

        // Make directory on external storage if it doesn't already exist.
        // ExternalFilesDir is the external storage private to this app.
        // BUT, this cache is deleted when the user uninstalls our app.
        /*File folder = new File (path);
        if (!folder.exists()) {
            folder.mkdir();
        }*/

        if (getIntent().hasExtra("path")) {
            path = getIntent().getStringExtra("path");
        }
        setTitle(path);

        // Read all files into the values-array
        //List values = new ArrayList();
        dir = new File(path);

        File f_list[] = dir.listFiles();

        if (!dir.canRead()) {
            setTitle(getTitle() + " (inaccessible");
        }

        String[] list = dir.list();

        if (list != null) {
            // For each file in our list of files.
            //for (String file : list) {
            for (int i = 0; i < f_list.length; i++) {
                if (!f_list[i].getName().startsWith(".")) {
                    //values.add(file);
                    values.add(f_list[i].getName());
                }
            }
        }
        Collections.sort(values);

        // Putting the data into our list.
        ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_2, android.R.id.text1, values);
        setListAdapter(adapter);
    }

    // I can't get this to open other directories...
    // But this probably isn't necessary for our purposes.
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        String filename = (String) getListAdapter().getItem(position);
        File temp_file = new File(dir, values.get(position));

        /*if (path.endsWith(File.separator)) {
            filename = path + filename;
        } else {
            filename = path + File.separator + filename;
        }
        if (new File(filename).isDirectory()) {
            Intent intent = new Intent(this, FileBrowser.class);
            intent.putExtra("path", filename);
            startActivity(intent);
        } else { // If not a folder, then it's a file!
            Toast.makeText(this, filename + " is not a directory", Toast.LENGTH_LONG).show();
        }*/

        if (!temp_file.isFile()) {      // If it's a folder.
            File list[] = temp_file.listFiles();

            if (list != null && list.length > 0) {
                dir = temp_file;
                values.clear();
                for (int i = 0; i < list.length; i++) {
                    values.add(list[i].getName());
                }
                setListAdapter(new ArrayAdapter(this,
                        android.R.layout.simple_list_item_2, android.R.id.text1, values));
            }
        } else {                        // If it's a file.
            String mimeType = MediaFile.getMimeTypeForFile(temp_file.toString());
            String fileName = temp_file.getName();
            int dot_position = fileName.lastIndexOf(".");
            String file_extension = "";

            if (dot_position != -1) {
                String filename_wo_ext = filename.substring(0, dot_position);
                file_extension = fileName.substring(dot_position + 1, fileName.length());
            }
            if (mimeType == null) {
                mimeType = MimeUtils.guessMimeTypeFromExtension(file_extension);
            }

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse("file://" + temp_file), mimeType);
            ResolveInfo info = getPackageManager().resolveActivity(intent,
                    PackageManager.MATCH_DEFAULT_ONLY);
            if (info != null) {
                startActivity(Intent.createChooser(intent, "Complete action using"));
            }
        }
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICK_FILE_RESULT_CODE: {
                if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                    String theFilePath = data.getData().getPath();
                }
                break;
            }
        }
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state));
    }

}
