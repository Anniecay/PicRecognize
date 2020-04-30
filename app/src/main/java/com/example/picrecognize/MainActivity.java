package com.example.picrecognize;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.ml.vision.text.RecognizedLanguage;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ImageView mainpic;
    FloatingActionButton bt1;
    TextView wordTag;
    Button button;
    Bitmap bitmap = null;
    Switch aswitch;
    boolean isText;
    Canvas canvas;

    GridLayout grid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wordTag = findViewById(R.id.wordTags);
        mainpic = findViewById(R.id.imageView);
        bt1 = findViewById(R.id.choose);

        grid = findViewById(R.id.grid);
        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(bt1);
            }
        });
        aswitch=findViewById(R.id.switch1);
        button = findViewById(R.id.recognize);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isText==true){
                    catchword(getBitmap());
                }
                else{
                    catchobject(getBitmap());
                }
            }
        });
        aswitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(aswitch.isChecked()){
                    aswitch.setText("text: ON");
                    isText=true;
                }
                else{
                    aswitch.setText("text: OFF");
                    wordTag.setText(" ");
                    grid.removeAllViews();

                    isText=false;
                }
            }
        });
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view, Gravity.BOTTOM);
        popupMenu.getMenuInflater().inflate(R.menu.menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.choofg:
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        startActivityForResult(intent, 500);
                        break;
                    case R.id.picn:
                        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{Manifest.permission.CAMERA}, 300);
                        } else {
                            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(cameraIntent, 200);
                        }
                        break;

                }
                return true;
            }
        });
        popupMenu.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 500) {
            Uri imageUri = data.getData();
            grid.removeAllViews();
            mainpic.setImageURI(imageUri);
            try {

                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                setBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void catchobject(Bitmap bitmap) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        final FirebaseVisionImageLabeler labeler = FirebaseVision.getInstance()
                .getOnDeviceImageLabeler();

        labeler.processImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
            @Override
            public void onSuccess(final List<FirebaseVisionImageLabel> l) {
                grid.removeAllViews();
                for(int i = 0; i < l.size();i++){
                    TextView t = new TextView(getApplicationContext());
                    t.setText(l.get(i).getText());
                    ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(5,5,5,5);
                    t.setTextSize(18);
                    t.setPadding(15,15,15,15);
                    t.setBackground(getResources().getDrawable(R.drawable.chip));
                    t.setTextColor(getResources().getColor(R.color.text));
                    t.setTypeface(Typeface.DEFAULT_BOLD);
                    t.setLayoutParams(params);
                    grid.addView(t);
                }
                mainpic.setImageBitmap(getBitmap());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("error", e.toString());
            }
        });
    }

    private void catchword(final Bitmap bitmap) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        final FirebaseVisionTextRecognizer textRecognizer = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();
        textRecognizer.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {

                grid.removeAllViews();
                Bitmap bitmap1 = bitmap.copy(Bitmap.Config.ARGB_8888,true);

                 canvas = new Canvas(bitmap1);

                String resultText = firebaseVisionText.getText();
                EditText editText = new EditText(getApplicationContext());
                editText.setText(resultText);
                editText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                editText.setTextSize(15);
                editText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                editText.setMaxLines(7);

                grid.addView(editText);

                Paint paint = new Paint();
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(3);
                paint.setColor(Color.RED);
                paint.setStrokeWidth(3);

                for (FirebaseVisionText.TextBlock block : firebaseVisionText.getTextBlocks()) {
                    String blockText = block.getText();
                    Float blockConfidence = block.getConfidence();
                    List<RecognizedLanguage> blockLanguages = block.getRecognizedLanguages();
                    for (FirebaseVisionText.Line line : block.getLines()) {
                        String lineText = line.getText();
                        Float lineConfidence = line.getConfidence();
                        List<RecognizedLanguage> lineLanguages = line.getRecognizedLanguages();
                        for (FirebaseVisionText.Element element : line.getElements()) {
                            String elementText = element.getText();
                            Float elementConfidence = element.getConfidence();
                            List<RecognizedLanguage> elementLanguages = element.getRecognizedLanguages();
                            wordTag.append(elementText+ " , ");
                            Rect elementFrame = element.getBoundingBox();
                            canvas.drawRect(elementFrame,paint);
                        }

                    }
                }mainpic.setImageBitmap(bitmap1);
            }
        });
    }


    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}