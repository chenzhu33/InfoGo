package com.carelife.infogo.ui;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.carelife.infogo.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by wangrh on 2016/10/21.
 */
public abstract class BaseActivityWithTakePhoto extends AppCompatActivity implements View.OnClickListener{

    protected String photoPath;
    protected String photoThumbPath;
    private Dialog savePhotoDialog;
    private View dialogView;
    private ImageView dialogImageView;
    private EditText dialogEditText;
    private Bitmap bitmap;
    private Button saveButton;
    private Button deleteButton;
    private long timeStamp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialogView = LayoutInflater.from(this).inflate(R.layout.save_photo_dialog, null);
        dialogImageView = (ImageView)dialogView.findViewById(R.id.save_photo_iv);
        dialogEditText = (EditText)dialogView.findViewById(R.id.save_photo_description);
        saveButton = (Button)dialogView.findViewById(R.id.save_button);
        saveButton.setOnClickListener(this);
        deleteButton = (Button)dialogView.findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(this);
        savePhotoDialog = new Dialog(this);
        savePhotoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        savePhotoDialog.setContentView(dialogView);
        savePhotoDialog.setCanceledOnTouchOutside(false);
    }

    @Override
    public void onClick(View v) {
        if(v == saveButton){
            String description = dialogEditText.getText().toString();
            if(TextUtils.isEmpty(description)){
                Toast.makeText(this,"Description can not be null", Toast.LENGTH_SHORT).show();
            }else {
                genThumb();
                saveToDb();
                Toast.makeText(this,"Save successfully", Toast.LENGTH_SHORT).show();
                savePhotoDialog.dismiss();
            }
        }else if(v == deleteButton){
            File file = new File(photoPath);
            if(file.exists())
                file.delete();
            Toast.makeText(this,"Delete successfully", Toast.LENGTH_SHORT).show();
            savePhotoDialog.dismiss();
        }
    }

    private void genThumb(){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(photoPath, options);
        options.inJustDecodeBounds = false;
        int be = (int)(options.outHeight / (float)200);
        if (be <= 0)
            be = 1;
        options.inSampleSize = be;
        bitmap = BitmapFactory.decodeFile(photoPath,options);
        File file = new File(photoThumbPath);
        try {
            FileOutputStream out=new FileOutputStream(file);
            if(bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)){
                out.flush();
                out.close();
            }
        }  catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveToDb(){

    }

    protected void takePhoto(){

        timeStamp = System.currentTimeMillis();
        photoPath = Environment.getExternalStorageDirectory() + "/infoGo/"+timeStamp+".jpg";
        photoThumbPath = Environment.getExternalStorageDirectory() + "/infoGo/"+timeStamp+"_thumb.jpg";
        File photoFile = new File(photoPath);
        if (!photoFile.getParentFile().exists()) {
            photoFile.getParentFile().mkdirs();
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
        startActivityForResult(intent, 99);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == 99){
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }
                bitmap = loadBitmap(photoPath, true);
                dialogImageView.setImageBitmap(bitmap);
                savePhotoDialog.show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }


    public Bitmap loadBitmap(String imgpath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bm = null;
        {
            options.inJustDecodeBounds = true;
            bm = BitmapFactory.decodeFile(imgpath, options);

            int Wight = getWindowManager().getDefaultDisplay().getWidth();

            int ratio = options.outWidth / Wight;
            if (ratio <= 0)
                ratio = 1;
            options.inSampleSize = ratio;
            options.inJustDecodeBounds = false;
        }

        return BitmapFactory.decodeFile(imgpath, options);
    }

    public Bitmap loadBitmap(String imgpath, boolean adjustOritation) {
        if (!adjustOritation) {
            return loadBitmap(imgpath);
        } else {
            Bitmap bm = loadBitmap(imgpath);
            int digree = 0;
            ExifInterface exif = null;
            try {
                exif = new ExifInterface(imgpath);
            } catch (IOException e) {
                e.printStackTrace();
                exif = null;
            }
            if (exif != null) {
                int ori = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                switch (ori) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        digree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        digree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        digree = 270;
                        break;
                    default:
                        digree = 0;
                        break;
                }
            }
            if (digree != 0) {
                Matrix m = new Matrix();
                m.postRotate(digree);
                bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
            }
            return bm;
        }
    }
}
