package com.example.waterflower124.stamp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    ImageView frameImageView;

    Bitmap imageBitmap;
    Bitmap frameBitmap;
    Bitmap tempimageBitmap;
    Bitmap tempframeBitmap;

    Drawable framedrawable;

    boolean custom_frame = false;//if select custom frame then true, if select default frame then false

    boolean select_frame_notext = false;
    boolean select_frame_withtext = false;

    String openType;
    String filepath;

    int select_frame;

    int   imageWidth, imageHeight;

//    int image_frame_layoutWidth, image_frame_layoutHeight;

    RelativeLayout image_frame_layout;

    ProgressBar progressBar;


    int deviceWidth, deviceHeight;

    private static final int SELECTED_PIC = 1;

    private int REQUEST_CODE = 1;

    String filename, extension;



    int[] frame_array_withtext = {R.drawable.with_text_1, R.drawable.with_text_2, R.drawable.with_text_3, R.drawable.with_text_4, R.drawable.with_text_5};
    int[] frame_array_notext = {R.drawable.without_text_1, R.drawable.without_text_2, R.drawable.without_text_3, R.drawable.without_text_4, R.drawable.without_text_5};
    double[] rate = {1, 1.7777, 1.3333, 0.5625, 0.75};

    double RATIO = 0.2;
    double FRAME_MARGIN_RATIO = 0.03;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        deviceHeight = displayMetrics.heightPixels;
        deviceWidth = displayMetrics.widthPixels;


        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        imageView = (ImageView)findViewById(R.id.image);
        frameImageView = (ImageView)findViewById(R.id.frame);
        image_frame_layout = (RelativeLayout)findViewById(R.id.image_frame);
        progressBar = (ProgressBar)findViewById(R.id.progressbar);

        imageBitmap = null;
        frameBitmap = null;
        openType = "";


    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
//            Toast.makeText(this, "open", Toast.LENGTH_SHORT).show();
            Uri uri = data.getData();
            try {
                if(openType.equals("Image")) {

                    imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

                    String[] projection = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(projection[0]);
                    filepath = cursor.getString(columnIndex);

                    String fullname = filepath.substring(filepath.lastIndexOf("/") + 1);

                    filename = fullname.substring(0, fullname.lastIndexOf("."));

                    extension = fullname.substring(fullname.lastIndexOf(".") + 1);


                    cursor.close();

                    ExifInterface exif = new ExifInterface(filepath);
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                    Matrix matrix = new Matrix();
                    if (orientation == 6) {
                        matrix.postRotate(90);
                    } else if (orientation == 3) {
                        matrix.postRotate(180);
                    } else if (orientation == 8) {
                        matrix.postRotate(270);
                    }

                    imageBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(), imageBitmap.getHeight(), matrix, true);

//
//                    Matrix matriximage = new Matrix();
//                    matriximage.postScale((float)0.1, (float)0.1);
//                    Bitmap scaledimageBitmap = Bitmap.createBitmap(originimageBitmap, 0, 0, originimageBitmap.getWidth(), originimageBitmap.getHeight(), matriximage, false);
//
//
//
//                    ByteArrayOutputStream out = new ByteArrayOutputStream();
//                    scaledimageBitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
//                    imageBitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));



                    imageView.setImageBitmap(imageBitmap);

                    frameImageView.setImageDrawable(null);


                    imageWidth = imageBitmap.getWidth();
                    imageHeight = imageBitmap.getHeight();


//                    image_frame_layoutWidth = image_frame_layout.getWidth();
//                    image_frame_layoutHeight = image_frame_layout.getHeight();

                } else if(openType.equals("Frame")) {

                    custom_frame = true;

                    if (imageView.getDrawable() instanceof BitmapDrawable) {
                        imageBitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                    } else {
                        Drawable d = imageView.getDrawable();
                        imageBitmap = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(imageBitmap);
                        d.draw(canvas);
                    }
                    imageWidth = imageBitmap.getWidth();
                    imageHeight = imageBitmap.getHeight();

//                    image_frame_layoutWidth = image_frame_layout.getWidth();
//                    image_frame_layoutHeight = image_frame_layout.getHeight();

                    String[] projection = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(projection[0]);
                    String framefilepath = cursor.getString(columnIndex);
                    cursor.close();

                    frameBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

                    ExifInterface exif = new ExifInterface(framefilepath);
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                    Matrix matrix = new Matrix();
                    if (orientation == 6) {
                        matrix.postRotate(90);
                    } else if (orientation == 3) {
                        matrix.postRotate(180);
                    } else if (orientation == 8) {
                        matrix.postRotate(270);
                    }

                    frameBitmap = Bitmap.createBitmap(frameBitmap, 0, 0, frameBitmap.getWidth(), frameBitmap.getHeight(), matrix, true);

                    layout_FrameView(imageBitmap, frameBitmap);

//                    RelativeLayout.LayoutParams layoutParams=new RelativeLayout.LayoutParams(138, 188);
//                    layoutParams.setMargins(2265, 215, 0, 0);
//                    frameImageView.setLayoutParams(layoutParams);
//

                    frameImageView.setImageBitmap(frameBitmap);
                    framedrawable = new BitmapDrawable(getResources(), frameBitmap);


                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            imageBitmap = tempimageBitmap;
            frameBitmap = tempframeBitmap;
//            if(imageBitmap == null)
//                Toast.makeText(this, "ImageBitmap is NULL", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        if(imageBitmap != null) {

            Bitmap bitmap_image;
            if (imageView.getDrawable() instanceof BitmapDrawable) {
                bitmap_image = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            }
            else {
                Drawable d = imageView.getDrawable();
                bitmap_image = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap_image);
                d.draw(canvas);
            }
            outState.putParcelable("image", bitmap_image);
        } else
            outState.putParcelable("image", null);

        if(frameBitmap != null) {

            Bitmap bitmap_frame;
            if (frameImageView.getDrawable() instanceof BitmapDrawable) {
                bitmap_frame = ((BitmapDrawable) frameImageView.getDrawable()).getBitmap();
            } else {
                Drawable d = frameImageView.getDrawable();
                bitmap_frame = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap_frame);
                d.draw(canvas);
            }

            outState.putParcelable("frame", bitmap_frame);
        } else
            outState.putParcelable("frame", null);

        outState.putBoolean("custom_frame", custom_frame);
        outState.putString("image_filepath", filepath);
        outState.putString("image_filename", filename);
        outState.putString("image_fileextension", extension);
        outState.putInt("select_frame", select_frame);


//        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {

        super.onRestoreInstanceState(savedInstanceState);

//        image_frame_layout = findViewById(R.id.image_frame);

        custom_frame = savedInstanceState.getBoolean("custom_frame");
        filename = savedInstanceState.getString("image_filename");
        filepath = savedInstanceState.getString("image_filepath");
        extension = savedInstanceState.getString("image_fileextension");
        select_frame = savedInstanceState.getInt("select_frame");

        imageView.setImageBitmap((Bitmap)savedInstanceState.getParcelable("image"));
        imageBitmap = (Bitmap)savedInstanceState.getParcelable("image");

//        frameImageView.setImageBitmap((Bitmap)savedInstanceState.getParcelable("frame"));
//        frameBitmap = (Bitmap)savedInstanceState.getParcelable("frame");

//        RelativeLayout.LayoutParams layoutParams=new RelativeLayout.LayoutParams(200, 200);
//        layoutParams.setMargins(500, 50, 0, 0);
//        frameImageView.setLayoutParams(layoutParams);

        frameBitmap = (Bitmap)savedInstanceState.getParcelable("frame");
        if(imageBitmap != null) {
            imageWidth = imageBitmap.getWidth();
            imageHeight = imageBitmap.getHeight();
            if(frameBitmap != null && custom_frame)
                layout_FrameView(imageBitmap, frameBitmap);
        }
        frameImageView.setImageBitmap((Bitmap)savedInstanceState.getParcelable("frame"));
//        frameBitmap = (Bitmap)savedInstanceState.getParcelable("frame");

//        if(custom_frame)
//            Toast.makeText(this, "TRUE CUSTOM", Toast.LENGTH_LONG).show();
//        else
//            Toast.makeText(this, "FALSE CUSTOM", Toast.LENGTH_LONG).show();

//        super.onRestoreInstanceState(savedInstanceState);

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    void layout_FrameView(Bitmap imagebitmap, Bitmap framebitmap) {

//        image_frame_layout = findViewById(R.id.image_frame);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        deviceWidth = displayMetrics.widthPixels;
        deviceHeight = displayMetrics.heightPixels;


        int imagewidth = imagebitmap.getWidth();
        int imageheight = imagebitmap.getHeight();
        int framewidth = framebitmap.getWidth();
        int frameheight = framebitmap.getHeight();

//        int image_frame_layoutwidth = image_frame_layout.getWidth();
//        int image_frame_layoutheight = image_frame_layout.getHeight();
        int image_frame_layoutwidth , image_frame_layoutheight;
        int orientation = this.getResources().getConfiguration().orientation;
        if(orientation == Configuration.ORIENTATION_PORTRAIT) {
            image_frame_layoutwidth = deviceWidth;
            image_frame_layoutheight = deviceHeight - 96;
        } else {
            image_frame_layoutwidth = deviceWidth;
            image_frame_layoutheight = deviceHeight - 96;
        }

//        Toast.makeText(this, imageView.getWidth() + "::" + imageView.getHeight(), Toast.LENGTH_LONG).show();


        int frameMarginTop, frameMarginLeft, frameMarginBottom, frameMarginRight;
        int showframewidth, showframeheight;

        int showImagewidth, showImageheight;

        double image_ratio = (double)imagewidth / (double)imageheight;
        double layout_ratio = (double)image_frame_layoutwidth / (double)image_frame_layoutheight;

        double zoom_ratio;

        if(image_ratio > layout_ratio) {
            zoom_ratio = (double)image_frame_layoutwidth / (double)imagewidth;
            showImagewidth = image_frame_layoutwidth;
            showImageheight = (int)(imageheight * zoom_ratio);

        } else {
            zoom_ratio = (double)image_frame_layoutheight / (double)imageheight;
            showImagewidth = (int)(imagewidth * zoom_ratio);
            showImageheight = image_frame_layoutheight;
        }

        int margin = (int)(showImagewidth * FRAME_MARGIN_RATIO);

        if(image_ratio < 1) {
            showframewidth = (int) (showImagewidth * RATIO);
            showframeheight = (int) (showframewidth * (double) frameheight / (double) framewidth);
        } else {
            showframeheight = (int)(showImageheight * RATIO);
            showframewidth = (int)(showframeheight * (double)framewidth / (double)frameheight);
        }

        frameMarginTop = image_frame_layoutheight - ((image_frame_layoutheight - showImageheight) / 2 + showframeheight + margin);
        frameMarginLeft = ((image_frame_layoutwidth - showImagewidth) / 2 + margin);

        RelativeLayout.LayoutParams layoutParams=new RelativeLayout.LayoutParams(showframewidth, showframeheight);
        layoutParams.setMargins(frameMarginLeft, frameMarginTop, 0, 0);
        frameImageView.setLayoutParams(layoutParams);

//        RelativeLayout.LayoutParams layoutParams=new RelativeLayout.LayoutParams(138, 188);
//        layoutParams.setMargins(2265, 215, 0, 0);
//        frameImageView.setLayoutParams(layoutParams);

//        Toast.makeText(this, margin + "||||" + imagewidth + "::" + imageheight + "::" + showImageWidth + "::" + showImageheight, Toast.LENGTH_LONG).show();
//        Toast.makeText(this, image_frame_layoutwidth + "::" + image_frame_layoutheight + "::" + showImagewidth + "::" + showImageheight + "::" + deviceWidth + "::" + deviceHeight, Toast.LENGTH_LONG).show();
//        Toast.makeText(this, showframewidth + "::" + showframeheight + "::" + frameMarginTop + "::" + frameMarginLeft, Toast.LENGTH_LONG).show();

//        frameImageView.setImageBitmap(frameBitmap);


    }

    void openImage() {
        tempimageBitmap = imageBitmap;
        tempframeBitmap = frameBitmap;
        imageBitmap = null;
        frameBitmap = null;
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, SELECTED_PIC);
    }

    void saveImage(boolean custom_frame) {
        progressBar.setVisibility(View.VISIBLE);
        File path = Environment.getExternalStorageDirectory();
        File dir = new File(path + "/StampedPicture/");
        if(!dir.exists())
            dir.mkdir();

        File file = new File(dir, filename +  "_stamped" + Long.toString(System.currentTimeMillis()) + "." + extension);

        Bitmap newImage = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(newImage);
//        canvas.drawBitmap(imageBitmap, 0f, 0f, null);





        File originImageBitmapFile = new File(filepath);;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap originImageBitmap = null;
        try {

            ExifInterface exif = new ExifInterface(filepath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            } else if (orientation == 3) {
                matrix.postRotate(180);
            } else if (orientation == 8) {
                matrix.postRotate(270);
            }


            originImageBitmap = BitmapFactory.decodeStream(new FileInputStream(originImageBitmapFile), null, options);
            originImageBitmap = Bitmap.createBitmap(originImageBitmap, 0, 0, originImageBitmap.getWidth(), originImageBitmap.getHeight(), matrix, true);

        } catch (IOException e) {
            e.printStackTrace();
        }

        canvas.drawBitmap(originImageBitmap, 0f, 0f, null);






        Drawable drawable;
        if(custom_frame) {
            drawable = framedrawable;

            int framewidth = frameBitmap.getWidth();
            int frameheight = frameBitmap.getHeight();
            double imageBitmapRatio = (double)originImageBitmap.getWidth() / (double)originImageBitmap.getHeight();
            int drawframewidth, drawframeheight;
            if(imageBitmapRatio < 1) {
                drawframewidth = (int) (originImageBitmap.getWidth() * RATIO);
                drawframeheight = (int) ((double) frameBitmap.getHeight() / (double) frameBitmap.getWidth() * drawframewidth);
            } else {
                drawframeheight = (int) (originImageBitmap.getHeight() * RATIO);
                drawframewidth = (int) ((double) frameBitmap.getWidth() / (double) frameBitmap.getHeight() * drawframeheight);
            }
            int margin = (int)(originImageBitmap.getWidth() * FRAME_MARGIN_RATIO);
            int marginleft = margin;
            int margintop = originImageBitmap.getHeight() - (drawframeheight + margin);

//            Toast.makeText(this, file.getAbsolutePath(), Toast.LENGTH_LONG).show();

            Bitmap tempframeBitmap = frameBitmap;
            Matrix matrix = new Matrix();
            matrix.postScale((float)drawframewidth / (float)framewidth, (float)drawframeheight / (float)frameheight);
            Bitmap drawframeBitmap = Bitmap.createBitmap(tempframeBitmap, 0, 0, framewidth, frameheight, matrix, false);
            canvas.drawBitmap(drawframeBitmap, marginleft, margintop, null);



//            drawable.setBounds(0, 0, drawframewidth, drawframeheight);

//            Toast.makeText(this, marginleft + "||" + imageWidth + "::" + margintop + "|||" + imageHeight + "::" + drawframewidth + "::" + drawframeheight, Toast.LENGTH_LONG).show();

        }
        else {
            drawable = getResources().getDrawable(select_frame);
            drawable.setBounds(0, 0, originImageBitmap.getWidth(), originImageBitmap.getHeight());
            drawable.draw(canvas);

        }

        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            if(extension.equals("png"))
                newImage.compress(Bitmap.CompressFormat.PNG, 100, out);
            else if(extension.equals("jpg"))
                newImage.compress(Bitmap.CompressFormat.JPEG, 100, out);

            out.flush();
            out.close();
            Toast.makeText(this, "Image Saved", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Exception", Toast.LENGTH_LONG).show();
        }
        addPicToGallery(file);
        progressBar.setVisibility(View.INVISIBLE);

    }


    void addPicToGallery(File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//        File f = new File(file);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }


    public void Open_Image(View view) {
        openType = "Image";
        openImage();
    }

    public void Save_Image(View view) {
        if(imageBitmap != null & frameBitmap != null)
            saveImage(custom_frame);
        else {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setCancelable(true);
            alertDialogBuilder.setTitle("Attention");
            alertDialogBuilder.setMessage("Please open image and stamp before saving!");
            alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.setCanceledOnTouchOutside(true);
            alertDialog.show();

        }
    }

    public void selectFrame(View view) {

//        Toast.makeText(this, "framefasfasdf", Toast.LENGTH_LONG).show();

        RelativeLayout.LayoutParams layoutParams=new RelativeLayout.LayoutParams(deviceWidth, deviceHeight);
//        layoutParams.setMargins(frameMarginLeft, frameMarginTop, 0, 0);
        frameImageView.setLayoutParams(layoutParams);

        double image_rate = ((double)imageWidth / (double)imageHeight);
        double diff_rate = Math.abs(image_rate - rate[0]);

        if(view.getId() == R.id.select_framenotext) {
            select_frame_notext = true;
            select_frame_withtext = false;

            select_frame = R.drawable.without_text_1;

            if(imageBitmap != null) {

                for(int i = 1; i < rate.length; i ++) {
                    if(Math.abs(image_rate - rate[i]) < diff_rate) {
                        select_frame = frame_array_notext[i];
                        diff_rate = Math.abs(image_rate - rate[i]);
                    }
                }
                frameBitmap = BitmapFactory.decodeResource(getResources(), select_frame);

                frameImageView.setImageResource(select_frame);
                custom_frame = false;

            } else Toast.makeText(this, "Please open image first", Toast.LENGTH_LONG).show();
        } else if(view.getId() == R.id.select_framewithtext) {
            select_frame_notext = false;
            select_frame_withtext = true;

            select_frame = R.drawable.with_text_1;

            if(imageBitmap != null) {

                for(int i = 1; i < rate.length; i ++) {
                    if(Math.abs(image_rate - rate[i]) < diff_rate) {
                        select_frame = frame_array_withtext[i];
                        diff_rate = Math.abs(image_rate - rate[i]);
                    }
                }
                frameBitmap = BitmapFactory.decodeResource(getResources(), select_frame);

                frameImageView.setImageResource(select_frame);
                custom_frame = false;
            } else Toast.makeText(this, "Please open image first", Toast.LENGTH_LONG).show();
        } else if(view.getId() == R.id.select_framecustom) {
            if(imageBitmap != null) {
                openType = "Frame";
                openImage();
            } else Toast.makeText(this, "Please open image first", Toast.LENGTH_LONG).show();
        }


    }
}
