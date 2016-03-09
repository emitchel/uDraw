package erm.udraw.activities;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import erm.udraw.R;
import erm.udraw.fragments.HomeFragment;
import erm.udraw.objects.CapturePhotoUtils;
import erm.udraw.objects.Utils;

/**
 * Main Activity holing one fragment for drawing.
 * <p/>
 * Main features are methods bubbled down to CanvasView level
 */
public class HomeActivity extends BaseActivity {


    private static final int REQUEST_CODE_PICTURE = 1;
    private static final int SELECT_PHOTO = 100;
    public static final int TAKE_PHOTO = 0;
    public static final int CHOOSE_EXISTING = 1;
    public static final int NEW_PHOTO = 2;
    public static final int SHARE_PHOTO = 3;
    public static final int SAVE_PHOTO = 4;

    /**
     * Vars
     */
    private Uri mUriPhoto;
    private String mNewFileName;

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

    /**
     * Determines best location for storing draw files
     *
     * @return
     */
    private String getFullPathOfUDrawFiles() {

        String state = Environment.getExternalStorageState();
        String path;
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            //SD card available
            path = Environment.getExternalStorageDirectory().getPath().toString();
        } else {
            path = getFilesDir().getPath().toString();
        }

        return path + File.separator + U_DRAW;
    }

    private String getTimeDateFileName() {
        return Utils.getCurrentDateTime() + PNG;
    }


    /**
     * Simple save to file system given file and bitmap
     *
     * @param file
     * @param bitmap
     * @return
     */
    private boolean saveBitmapToFile(File file, Bitmap bitmap) {

        FileOutputStream out = null;
        try {
            if (!file.exists()) {
                if (!file.getParentFile().exists()) {
                    if (!file.getParentFile().mkdirs()) {
                        errorPopup(getString(R.string.oops), getString(R.string.unable_to_store), getString(R.string.ok));
                        return false;
                    }
                }

                file.createNewFile();
            }
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
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

    /**
     * This method was strictly made to hit the requirement of
     * "SCREENSHOTTING" and then sharing - would probably rather
     * just share a picture of the canvas, and not the UI controls
     */
    private void screenshotThenShare() {
        ActivityCompat.requestPermissions(HomeActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                SHARE_PHOTO);

    }

    /**
     * Brings up apps that can share a given image
     *
     * @param imageFile
     */
    private void shareImageFile(File imageFile) {
        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(imageFile));
        log("Saving image to: " + imageFile.getAbsolutePath().toString());
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_screenshot)));
    }


    private void shareImage() {
        screenshotThenShare();
    }

    private Bitmap getBitmapFromCanvas() {
        HomeFragment frag = getFragment();
        if (frag != null) {
            return frag.getBitmap();
        } else {
            return null;
        }
    }

    /**
     * Popup asking for file name before saving.
     */
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
                            mNewFileName = fileName.getText().toString();
                            //Gotta ask for these permissions now
                            ActivityCompat.requestPermissions(HomeActivity.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    SAVE_PHOTO);


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


    /**
     * Reseting the canvas
     */
    private void newImage() {
        new AlertDialog.Builder(this)

                .setTitle(getString(R.string.undo_all_changes))
                .setMessage(getString(R.string.sure_undo))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        HomeFragment frag = getFragment();
                        if (frag != null) {
                            frag.clearCanvas();
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .show();
    }

    /**
     * Feature to play back the drawn picture.
     */
    private void playBack() {
        HomeFragment frag = getFragment();
        frag.playBack();
    }

    /**
     * Will show "Choose or Take picture" for the user to draw on
     */
    private void pickOrTakePicture() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.take_or_select))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.take_photo),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                //Gotta ask for these permissions now
                                ActivityCompat.requestPermissions(HomeActivity.this,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        NEW_PHOTO);

                            }
                        })
                .setNegativeButton(getString(R.string.choose_existing),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                Intent intent = new Intent(
                                        Intent.ACTION_PICK,
                                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(intent,
                                        CHOOSE_EXISTING);
                            }
                        })
                .setNeutralButton(getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();

    }

    /**
     * For API >=23 we need to ask for permission when accessing storage, shouldn't effect prior APIs... which is nice
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case NEW_PHOTO: {
                // If request is cancelled, the result arrays are empty.

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        String fileName = getTimeDateFileName()
                                + ".jpg";
                        ContentValues values = new ContentValues();
                        values.put(
                                MediaStore.Images.Media.TITLE,
                                fileName);
                        mUriPhoto =
                                getContentResolver()
                                        .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                                values);

                        Intent intent = new Intent(
                                MediaStore.ACTION_IMAGE_CAPTURE);
                        intent.putExtra(
                                MediaStore.EXTRA_OUTPUT,
                                mUriPhoto);
                        startActivityForResult(intent,
                                TAKE_PHOTO);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(mContext,
                                getString(R.string.no_camera),
                                Toast.LENGTH_LONG).show();
                    } catch (UnsupportedOperationException e2) {
                        Toast.makeText(mContext, getString(R.string.no_sd_card),
                                Toast.LENGTH_LONG).show();
                    }


                } else {

                    Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case SAVE_PHOTO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Bitmap bitmap = getBitmapFromCanvas();

                    if (bitmap != null) {
                        /**
                         * Nice method to set image in phone's native gallery
                         */
                        String msg = CapturePhotoUtils.insertImage(getContentResolver(), bitmap, mNewFileName, "uDraw photo taken at " + getTimeDateFileName());
                        log("Photo to gallery" + msg);
                        if (msg != null)
                            Toast.makeText(mContext, getString(R.string.image_saved_successfully), Toast.LENGTH_LONG).show();
                        else
                            errorPopup(getString(R.string.oops), getString(R.string.unable_to_store), getString(R.string.ok));

                    } else {
                        errorPopup(getString(R.string.no_bitmap), getString(R.string.could_not_get_bitmap), getString(R.string.ok));
                    }
                } else {

                    Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case SHARE_PHOTO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    try {

                        String mPath = getFullPathOfUDrawFiles();

                        /**
                         * I could easily just get the Bitmap of the canvas but
                         * requirements specified a screen shot, so that's what this is
                         */
                        View v1 = getWindow().getDecorView().getRootView();
                        v1.setDrawingCacheEnabled(true);
                        Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
                        v1.setDrawingCacheEnabled(false);

                        File imageFile = new File(mPath, getTimeDateFileName());

                        if (saveBitmapToFile(imageFile, bitmap))
                            shareImageFile(imageFile);
                    } catch (Throwable e) {
                        log(e.getMessage());
                        errorPopup(getString(R.string.oops), getString(R.string.error_saving_image), getString(R.string.ok));
                    }
                } else {
                    Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
                }

                return;
            }

        }
    }

    /**
     * Retrieves the bitmap of the chosen (or taken) photo
     * <p/>
     * TODO: needs work on rotating if needed.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        String filePath = "";
        if (requestCode == TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            CursorLoader cursorLoader = new CursorLoader(mContext,
                    mUriPhoto, filePathColumn, null, null, null);
            Cursor cursor = cursorLoader.loadInBackground();
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            filePath = cursor.getString(columnIndex);
            cursor.close();


        } else if (requestCode == CHOOSE_EXISTING
                && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            if (selectedImage.toString().contains("gallery3d")) {
                Toast.makeText(mContext,
                        getString(R.string.cloud_image),
                        Toast.LENGTH_LONG).show();
            } else {
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(
                        selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                filePath = cursor.getString(columnIndex);
                cursor.close();

            }
        }

        if (Utils.isValidString(filePath)) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(Uri.fromFile(new File(filePath)));
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                HomeFragment frag = getFragment();
                if (bitmap != null) {

                    frag.setBitmapBackground(bitmap);
                } else {
                    errorPopup(getString(R.string.oops), getString(R.string.could_not_get_bitmap), getString(R.string.ok));
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Will bubble down to view level.
     */
    public void rotateBackground() {
        HomeFragment fragment = getFragment();
        fragment.rotateBackground();

    }

    /**
     * Simple popup to basically show the Splash page again
     * but could used to show more information
     */
    public void showAbout() {
        final RelativeLayout dialogView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.dialog_about, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //nothing
                    }
                })
                .create();
        alertDialog.show();
    }

    /**
     * Quick method to retrieve the nested fragment
     *
     * @return
     */
    public HomeFragment getFragment() {
        FragmentManager fm = getSupportFragmentManager();
        HomeFragment fragment = (HomeFragment) fm.findFragmentById(R.id.canvas_fragment);
        return fragment;
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
            pickOrTakePicture();
            return true;
        } else if (id == R.id.playback) {
            playBack();
            return true;
        } else if (id == R.id.rotate) {
            rotateBackground();
            return true;

        } else if (id == R.id.about) {
            showAbout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //
    }


}
