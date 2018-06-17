package erm.udraw.activities;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
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
import erm.udraw.R;
import erm.udraw.fragments.HomeFragment;
import erm.udraw.utils.CapturePhotoUtils;
import erm.udraw.utils.Constants;
import erm.udraw.utils.DateTimeUtils;
import erm.udraw.utils.StringUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Main Activity holding one fragment for drawing.
 * <p/>
 * Main features are methods bubbled down to CanvasView level
 */
public class HomeActivity extends BaseActivity {

  private Uri uriPhoto;
  private String newFileName;

  @Override
  public String getTag() {
    return "Home Activity";
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);
    setUpToolbar();
  }

  private void setUpToolbar() {
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_home, menu);
    return true;
  }

  /**
   * Determines best location for storing draw files
   */
  private String getFullPathOfUDrawFiles() {

    String state = Environment.getExternalStorageState();
    String path;
    if (Environment.MEDIA_MOUNTED.equals(state)) {
      //SD card available
      path = Environment.getExternalStorageDirectory().getPath();
    } else {
      path = getFilesDir().getPath();
    }

    return path + File.separator + Constants.Misc.U_DRAW;
  }

  private String getTimeDateFileName() {
    return DateTimeUtils.getCurrentDateTime() + Constants.Misc.PNG;
  }

  /**
   * Simple save to file system given file and bitmap
   */
  private boolean saveBitmapToFile(File file, Bitmap bitmap) {

    FileOutputStream out = null;
    try {
      if (!file.exists()) {
        if (!file.getParentFile().exists()) {
          if (!file.getParentFile().mkdirs()) {
            errorPopup(getString(R.string.oops), getString(R.string.unable_to_store),
                getString(R.string.ok));
            return false;
          }
        }

        file.createNewFile();
      }
      out = new FileOutputStream(file);
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
    } catch (Exception e) {
      log(e.getMessage());
      errorPopup(getString(R.string.oops), getString(R.string.error_saving_image),
          getString(R.string.ok));
    } finally {
      try {
        if (out != null) {
          out.close();
        }
      } catch (IOException e) {
        log(e.getMessage());
        errorPopup(getString(R.string.oops), getString(R.string.error_saving_image),
            getString(R.string.ok));
      }
    }

    return !errorPopupOpen;
  }

  /**
   * This method was strictly made to hit the requirement of
   * "SCREENSHOTTING" and then sharing - would probably rather
   * just share a picture of the canvas, and not the UI controls
   */
  private void screenshotThenShare() {
    ActivityCompat.requestPermissions(HomeActivity.this,
        new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
        Constants.IntentActions.SHARE_PHOTO);
  }

  /**
   * Brings up apps that can share a given image
   */
  private void shareImageFile(File imageFile) {
    Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
    shareIntent.setType(Constants.Misc.IMAGE_TYPE);
    shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(imageFile));
    log("Saving image to: " + imageFile.getAbsolutePath());
    startActivity(Intent.createChooser(shareIntent, getString(R.string.share_screenshot)));
  }

  private void shareImage() {
    screenshotThenShare();
  }

  private Bitmap getBitmapFromCanvas() {
    HomeFragment frag = getFragment();
    return frag != null ? frag.getBitmap() : null;
  }

  /**
   * Popup asking for file name before saving.
   */
  private void saveFile() {

    final RelativeLayout dialogView =
        (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.dialog_image_name, null);
    final EditText fileName = (EditText) dialogView.findViewById(R.id.file_name);
    final TextInputLayout fileNameInput =
        (TextInputLayout) dialogView.findViewById(R.id.file_name_til);

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
    //This is because we don't want the dialog to dismiss onclick
    alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
      @Override
      public void onShow(DialogInterface dialog) {
        Button save = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

        save.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            if (StringUtils.isValidString(fileName.getText().toString())) {
              newFileName = fileName.getText().toString();
              //Gotta ask for these permissions now
              ActivityCompat.requestPermissions(HomeActivity.this,
                  new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
                  Constants.IntentActions.SAVE_PHOTO);

              alertDialog.dismiss();
            } else {
              fileNameInput.setError(getString(R.string.must_specifiy));
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
    if (frag != null) {
      frag.playBack();
    }
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
                    new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
                    Constants.IntentActions.NEW_PHOTO);
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
                    Constants.IntentActions.CHOOSE_EXISTING);
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
   */
  @Override
  public void onRequestPermissionsResult(int requestCode,
      String permissions[], int[] grantResults) {
    switch (requestCode) {
      case Constants.IntentActions.NEW_PHOTO: {
        // If request is cancelled, the result arrays are empty.

        if (grantResults.length > 0
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          try {
            String fileName = getTimeDateFileName()
                + Constants.Misc.JPG;
            ContentValues values = new ContentValues();
            values.put(
                MediaStore.Images.Media.TITLE,
                fileName);
            uriPhoto =
                getContentResolver()
                    .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        values);

            Intent intent = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(
                MediaStore.EXTRA_OUTPUT,
                uriPhoto);
            startActivityForResult(intent,
                Constants.IntentActions.TAKE_PHOTO);
          } catch (ActivityNotFoundException e) {
            Toast.makeText(this,
                getString(R.string.no_camera),
                Toast.LENGTH_LONG).show();
          } catch (UnsupportedOperationException e2) {
            Toast.makeText(this, getString(R.string.no_sd_card),
                Toast.LENGTH_LONG).show();
          }
        } else {

          Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
        }
        return;
      }
      case Constants.IntentActions.SAVE_PHOTO: {
        if (grantResults.length > 0
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          Bitmap bitmap = getBitmapFromCanvas();

          if (bitmap != null) {
            /**
             * Nice method to set image in phone's native gallery
             */
            String msg = CapturePhotoUtils.insertImage(getContentResolver(), bitmap,
                newFileName, "uDraw photo taken at " + getTimeDateFileName());
            log("Photo to gallery" + msg);
            if (msg != null) {
              Toast.makeText(this, getString(R.string.image_saved_successfully),
                  Toast.LENGTH_LONG).show();
            } else {
              errorPopup(getString(R.string.oops), getString(R.string.unable_to_store),
                  getString(R.string.ok));
            }
          } else {
            errorPopup(getString(R.string.no_bitmap), getString(R.string.could_not_get_bitmap),
                getString(R.string.ok));
          }
        } else {

          Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
        }
        return;
      }
      case Constants.IntentActions.SHARE_PHOTO: {
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

            if (saveBitmapToFile(imageFile, bitmap)) {
              shareImageFile(imageFile);
            }
          } catch (Throwable e) {
            log(e.getMessage());
            errorPopup(getString(R.string.oops), getString(R.string.error_saving_image),
                getString(R.string.ok));
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
   */
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    String filePath = "";
    if (requestCode == Constants.IntentActions.TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
      String[] filePathColumn = { MediaStore.Images.Media.DATA };
      CursorLoader cursorLoader = new CursorLoader(this,
          uriPhoto, filePathColumn, null, null, null);
      Cursor cursor = cursorLoader.loadInBackground();
      cursor.moveToFirst();

      int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
      filePath = cursor.getString(columnIndex);
      cursor.close();
    } else if (requestCode == Constants.IntentActions.CHOOSE_EXISTING
        && resultCode == Activity.RESULT_OK) {
      Uri selectedImage = data.getData();
      if (selectedImage != null && selectedImage.toString().contains(Constants.Misc.GALLERY_3D)) {
        Toast.makeText(this,
            getString(R.string.cloud_image),
            Toast.LENGTH_LONG).show();
      } else {
        String[] filePathColumn = { MediaStore.Images.Media.DATA };

        Cursor cursor = getContentResolver().query(
            selectedImage, filePathColumn, null, null, null);
        if (cursor != null) {
          cursor.moveToFirst();
          int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
          filePath = cursor.getString(columnIndex);
          cursor.close();
        }
      }
    }

    if (StringUtils.isValidString(filePath)) {
      try {
        InputStream inputStream =
            getContentResolver().openInputStream(Uri.fromFile(new File(filePath)));
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

        HomeFragment frag = getFragment();
        if (bitmap != null) {

          frag.setBitmapBackground(bitmap);
        } else {
          errorPopup(getString(R.string.oops), getString(R.string.could_not_get_bitmap),
              getString(R.string.ok));
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
    if (fragment != null) {
      fragment.rotateBackground();
    }
  }

  /**
   * Simple popup to basically show the Splash page again
   * but could used to show more information
   */
  public void showAbout() {
    final RelativeLayout dialogView =
        (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.dialog_about, null);
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
   */
  public HomeFragment getFragment() {
    FragmentManager fm = getSupportFragmentManager();
    HomeFragment fragment = (HomeFragment) fm.findFragmentById(R.id.canvas_fragment);
    return fragment;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if (id == R.id.clear) {
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
    HomeFragment frag = getFragment();

    /**
     * Usability feature to close any open windows if they're open.
     */

    if (!frag.closeWidthOrColorPicker()) {
      new AlertDialog.Builder(this)

          .setTitle(getString(R.string.exit))
          .setMessage(getString(R.string.are_you_sure))
          .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              moveTaskToBack(true);
            }
          })
          .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              // do nothing
            }
          })
          .show();
    }
  }
}
