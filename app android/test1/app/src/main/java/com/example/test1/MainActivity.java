package com.example.test1;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private DrawingView drawView;
    private ImageButton currPaint, drawBtn, eraseBtn, newBtn, saveBtn;
    private float smallBrush, mediumBrush, largeBrush;
    private Bitmap grayBitmap = null;
    private LinearLayout inferencePreview;
    public ImageView previewImage;
    private TextView mResultText;

    private static final int PIXEL_WIDTH = 28;
    private DigitsDetector mnistClassifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawView = (DrawingView) findViewById(R.id.drawing);
        mnistClassifier = new DigitsDetector(this);


        inferencePreview = (LinearLayout) findViewById(R.id.inference_preview);
        previewImage = (ImageView) findViewById(R.id.preview_image);
        mResultText = (TextView) findViewById(R.id.detect);

        mediumBrush = getResources().getInteger(R.integer.medium_size);
        largeBrush = getResources().getInteger(R.integer.large_size);

        drawBtn = (ImageButton) findViewById(R.id.draw_btn);
        drawBtn.setOnClickListener(this);

        drawView.setBrushSize(mediumBrush);

        eraseBtn = (ImageButton) findViewById(R.id.erase_btn);
        eraseBtn.setOnClickListener(this);

        newBtn = (ImageButton) findViewById(R.id.new_btn);
        newBtn.setOnClickListener(this);

        saveBtn = (ImageButton) findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        //respond to clicks
        if (view.getId() == R.id.draw_btn) {
            //draw button clicked

            drawView.setColor("#FFFFFFFF");
            drawView.setBrushSize(mediumBrush);
            drawView.setLastBrushSize(largeBrush);
            drawView.setErase(false);

        } else if (view.getId() == R.id.erase_btn) {
            //switch to erase - choose size
            Log.d("click erase", "yes");
            drawView.setErase(false);
//            drawView.setColor("#FFFF0000");

            drawView.setBrushSize(largeBrush);
            drawView.setLastBrushSize(largeBrush);

            drawView.setErase(true);


        } else if (view.getId() == R.id.new_btn) {
            drawView.startNew();
            mResultText.setText("Số dự đoán = ? \n Độ chính xác = ?");
            previewImage.setImageBitmap(null );

        } else if (view.getId() == R.id.save_btn) {

            //save drawing
            onDetectClicked();
        }
    }

    private void onDetectClicked() {
        inferencePreview.setVisibility(View.VISIBLE);
        //save drawing
        drawView.setDrawingCacheEnabled(true);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(drawView.getDrawingCache(), PIXEL_WIDTH, PIXEL_WIDTH, true);


        String digit = mnistClassifier.classify((scaledBitmap));

        previewImage.setImageBitmap(scaledBitmap );
        if (digit != "") {
            Toast.makeText(getApplicationContext(),
                    "" + digit, Toast.LENGTH_SHORT).show();
            mResultText.setText(getString(R.string.found_digits, String.valueOf(digit)));
        } else {
            mResultText.setText(getString(R.string.not_detected));
        }
//                String imgSaved = MediaStore.Images.Media.insertImage(
//                getContentResolver(),  scaledBitmap,
//                UUID.randomUUID().toString() + ".jpg", "drawing");

        drawView.destroyDrawingCache();
    }
}