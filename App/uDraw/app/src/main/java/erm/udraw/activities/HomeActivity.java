package erm.udraw.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import erm.udraw.R;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
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
            //TODO:
            //Popup for new name
            return true;
        } else if(id==R.id.share_image){
            //TODO:
            //Native share popup
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
