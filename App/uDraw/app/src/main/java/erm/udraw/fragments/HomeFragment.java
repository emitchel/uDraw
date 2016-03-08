package erm.udraw.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import erm.udraw.R;
import erm.udraw.views.CanvasView;

/**
 * A placeholder fragment containing a simple view.
 */
public class HomeFragment extends Fragment {
    CanvasView mCanvas;

    ImageButton mDraw, mErase, mChooseColor, mUndo, mRedo;
    View mDrawSelected, mEraseSelected;

    public HomeFragment() {
        //empty constr per the docs
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mCanvas = (CanvasView)view.findViewById(R.id.canvas);
        mDraw = (ImageButton)view.findViewById(R.id.draw);
        mErase = (ImageButton)view.findViewById(R.id.erase);
        mChooseColor = (ImageButton)view.findViewById(R.id.color);
        mUndo = (ImageButton)view.findViewById(R.id.undo);
        mRedo = (ImageButton)view.findViewById(R.id.redo);

        mDrawSelected =view.findViewById(R.id.draw_selected);
        mEraseSelected = view.findViewById(R.id.erase_selected);

        setCustomListeners();

    }

    

    private void setCustomListeners(){
        mDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    public Bitmap getBitmap(){
        return mCanvas.getBitmap();
    }

    public void clearCanvas(){
        mCanvas.clearCanvas();
    }
}

