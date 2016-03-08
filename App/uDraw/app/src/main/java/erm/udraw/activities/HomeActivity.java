package erm.udraw.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import erm.udraw.R;
import erm.udraw.objects.CapturePhotoUtils;
import erm.udraw.objects.Utils;

public class HomeActivity extends BaseActivity {

    public static final String PNG = ".png";
    public static final String U_DRAW = "uDraw";

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

    private void setUpObjects() {
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

    private String getFullPathOfUDrawFiles() {
        //return Environment.getExternalStorageDirectory().getAbsolutePath().toString() + "/" + U_DRAW + "/";
        String state = Environment.getExternalStorageState();
        String path;
        if (Environment.MEDIA_MOUNTED.equals(state)){
            //SD card available
            path = Environment.getExternalStorageDirectory().getPath().toString();
        } else {
            path = getFilesDir().getPath().toString();
        }

        return path + "/" + U_DRAW + "/";
    }

    private String getTimeDateFileName() {
        return Utils.getCurrentDateTime() + PNG;
    }

    private boolean saveBitmapToFile(File file, Bitmap bitmap) {

        FileOutputStream out = null;
        try {
            if (!file.exists()) {
                if(!file.getParentFile().exists()){
                    file.getParentFile().mkdirs();
                }

                file.createNewFile();
            }
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            log(e.getMessage());
            errorPopup(getString(R.string.oops), getString(R.string.error_saving_image), getString(R.string.ok));
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                log(e.getMessage());
                errorPopup(getString(R.string.oops), getString(R.string.error_saving_image), getString(R.string.ok));
            }
        }

        return !bErrorPopupOpen;
    }

    private void screenshotThenShare() {
        try {
            //we're just going to automatically save it
            String mPath = getFullPathOfUDrawFiles() + getTimeDateFileName();

            // create bitmap screen capture
            View v1 = getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);

            if(saveBitmapToFile(imageFile, bitmap))
                shareImageFile(imageFile);
        } catch (Throwable e) {
            log(e.getMessage());
            errorPopup(getString(R.string.oops), getString(R.string.error_saving_image), getString(R.string.ok));
        }
    }

    private void shareImageFile(File imageFile) {
        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(imageFile));
        log("Saving image to: " + imageFile.getAbsolutePath().toString());
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_image)));
    }


    private void shareImage() {
        screenshotThenShare();
    }

    private Bitmap getBitmapFromCanvas() {
        //TODO:Get bitmap from fragment
        return null;
    }

    private void saveFile() {

        final RelativeLayout dialogView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.dialog_image_name, null);
        final EditText fileName = (EditText) dialogView.findViewById(R.id.file_name);
        final TextInputLayout fileTIL = (TextInputLayout) dialogView.findViewById(R.id.file_name_til);

        fileName.setText(getTimeDateFileName());
        fileName.selectAll();

        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle(getString(R.string.save_picture))
                .setMessage(getString(R.string.give_picture_name))
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        //Overriding the onClickListener so to have a simple check of name existence prior to saving
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button save = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Utils.isValidString(fileName.getText().toString())) {
                            Bitmap bitmap = getBitmapFromCanvas();

                            if (bitmap != null) {
                                String msg = CapturePhotoUtils.insertImage(getContentResolver(), bitmap, fileName.getText().toString(), "uDraw photo taken at " + getTimeDateFileName());
                                log("CAPTURE PHOTO " + msg);

                            } else {
                                errorPopup(getString(R.string.no_bitmap), getString(R.string.could_not_get_bitmap), getString(R.string.ok));
                            }

                            alertDialog.dismiss();
                        } else {
                            fileTIL.setError(getString(R.string.must_specifiy));
                        }
                    }
                });
            }
        });

        alertDialog.show();

    }

    private void newImage() {
        new AlertDialog.Builder(this)

                .setTitle(getString(R.string.undo_all_changes))
                .setMessage(getString(R.string.sure_undo))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //TODO: Clear canvas in fragment
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
        } else if (id == R.id.save_image) {
            saveFile();
            return true;
        } else if (id == R.id.share_image) {
            shareImage();
            return true;
        } else if (id == R.id.import_image) {
            //TODO:
            //"From... Gallery or New Picture"
            return true;
        } else if (id == R.id.about) {
            //TODO:
            //New activity showing
            //Version number, last updated, name, github link
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //
    }


}
