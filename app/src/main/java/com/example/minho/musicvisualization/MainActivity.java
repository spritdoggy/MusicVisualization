package com.example.minho.musicvisualization;




import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity; // import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity { // public class MainActivity extends Activity {

    private String mFileName;
    private ListView lvFileControl;
    private Context mContext = this;

    private List<String> lItem = null;
    private List<String> lPath = null;
    private String mRoot = Environment.getExternalStorageDirectory().getAbsolutePath();
    private TextView mPath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPath = (TextView) findViewById(R.id.tvPath);
        lvFileControl = (ListView)findViewById(R.id.lvFileControl);
        getDir(mRoot);

        lvFileControl.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                File file = new File(lPath.get(position));

                if (file.isDirectory()) {
                    if (file.canRead())
                        getDir(lPath.get(position));
                    else {
                        Toast.makeText(mContext, "No files in this folder.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    mFileName = file.getName();
                    Log.i("Test", "ext:" + mFileName.substring(mFileName.lastIndexOf('.') + 1, mFileName.length()));
                }
            }
        });
    }

    private void getDir(String dirPath) {
        mPath.setText("Location: " + dirPath);

        lItem = new ArrayList<String>();
        lPath = new ArrayList<String>();

        File f = new File(dirPath);
        File[] files = f.listFiles();

        if (!dirPath.equals(mRoot)) {
            //item.add(root); //to root.
            //path.add(root);
            lItem.add("../"); //to parent folder
            lPath.add(f.getParent());
        }

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            lPath.add(file.getAbsolutePath());

            if (file.isDirectory())
                lItem.add(file.getName() + "/");
            else
                lItem.add(file.getName());
        }

        ArrayAdapter<String> fileList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, lItem);
        lvFileControl.setAdapter(fileList);
    }
}