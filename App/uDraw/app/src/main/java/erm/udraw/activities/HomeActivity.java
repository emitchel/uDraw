package erm.udraw.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

import erm.udraw.R;
import erm.udraw.objects.Utils;

public class HomeActivity extends BaseActivity {

    Context mContext;

    @Override
    public String getTag() {
        return "Home Activity";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setUpToolbar();
        setUpObjects();

    }

    private void setUpObjects(){
        this.mContext = this;
    }

    private void setUpToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }


    private void takeScreenshot() {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";

            // create bitmap screen capture
            View v1 = getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            shareScreenshot(imageFile);
        } catch (Throwable e) {
            // Several error may come out with file handling or OOM
            e.printStackTrace();
        }
    }

    private void shareScreenshot(File imageFile) {
        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(imageFile));
        log(imageFile.getAbsolutePath().toString());
        startActivity(Intent.createChooser(shareIntent, "share"));
    }

    private void shareImage() {
        //TODO:Save image, then share the image?
        //TODO: (option 2) screenshot?
        takeScreenshot();
    }

    private void saveFile(){

        final RelativeLayout dialogView = (RelativeLayout)LayoutInflater.from(this).inflate(R.layout.dialog_image_name,null);
        final EditText fileName = (EditText) dialogView.findViewById(R.id.file_name);

        fileName.setText(Utils.getCurrentDateTime() + ".jpg");

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle(getString(R.string.save_picture))
                .setMessage(getString(R.string.give_picture_name))
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        if (Utils.isValidString(fileName.getText().toString())) {
                            //TODO: save image
                        } else {
                            Toast.makeText(mContext,getString(R.string.failed_to_specify),Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .show();
    }

    private void newImage(){
        new AlertDialog.Builder(this)

                .setTitle(getString(R.string.undo_all_changes))
                .setMessage(getString(R.string.sure_undo))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //TODO: Clear canvas
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        if (id == R.id.new_image) {
            newImage();
            return true;
        } else if(id==R.id.save_image){
            saveFile();
            return true;
        } else if(id==R.id.share_image){
            shareImage();
            return true;
        } else if(id==R.id.import_image){
            //TODO:
            //"From... Gallery or New Picture"
            return true;
        } else if(id==R.id.about){
            //TODO:
            //New activity showing
            //Version number, last updated, name, github link
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
       //
    }

}
