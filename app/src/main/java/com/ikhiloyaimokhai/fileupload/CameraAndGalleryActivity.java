package com.ikhiloyaimokhai.fileupload;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.IOException;

import static com.ikhiloyaimokhai.fileupload.Constant.ALL_FILES;
import static com.ikhiloyaimokhai.fileupload.Constant.DOC_MIME_TYPE;
import static com.ikhiloyaimokhai.fileupload.Constant.IMAGE_MIME_TYPE;
import static com.ikhiloyaimokhai.fileupload.Constant.SPLIT;
import static com.ikhiloyaimokhai.fileupload.Constant.TEXT_MIME_TYPE;

public class CameraAndGalleryActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = CameraAndGalleryActivity.class.getSimpleName();
    ImageView imageView;
    private static final int REQUEST_IMAGE_CAPTURE = 3;
    private static final int READ_REQUEST_CODE = 32;
    private static final int WRITE_REQUEST_CODE = 33;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_and_gallery);
        imageView = findViewById(R.id.imageView);
        Button button = findViewById(R.id.imageBtn);
        mContext = CameraAndGalleryActivity.this;
        button.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.imageBtn) {
            //show selection dialog
            this.showSelectionDialog();
        }
    }

    private void showSelectionDialog() {

        // create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // set the custom layout
        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_upload_image, null);
        builder.setView(customLayout);

        LinearLayout cameraLayout = customLayout.findViewById(R.id.cameraLayout);
        LinearLayout galleryLayout = customLayout.findViewById(R.id.galleryLayout);


        // create and show the alert dialog
        final AlertDialog dialog = builder.create();
        dialog.show();

        cameraLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show camera intent
                Toast.makeText(CameraAndGalleryActivity.this, "Camera  clicked", Toast.LENGTH_LONG).show();
                dispatchTakePictureIntent();

                dialog.dismiss();
            }
        });

        galleryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show gallery intent
                Toast.makeText(CameraAndGalleryActivity.this, "gallery clicked", Toast.LENGTH_LONG).show();
                selectImage();
                dialog.dismiss();
            }
        });
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {   //camera
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
        }

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (data != null) {
                uri = data.getData();
                Log.i(TAG, "Uri: " + uri.toString());
                try {
                    boolean isVirtualFile = IntentUtils.isVirtualFile(uri, mContext);
                    Log.i(TAG, "isVirtual File: " + isVirtualFile);
                    Log.i(TAG, String.format("MimeType is:: %s", IntentUtils.getMimeType(mContext, uri)));
                    final String mimeType = IntentUtils.getMimeType(mContext, uri);
//                    readTextFromUri(uri);
                    IntentUtils.dumpImageMetaData(uri, mContext);

                    Bitmap bitmapFromUriCompressed = getBitmapFromUri(uri, mContext);

                    imageView.setImageBitmap(bitmapFromUriCompressed);
                    final Uri finalUri = uri;

                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (mimeType.startsWith(IMAGE_MIME_TYPE.split(SPLIT)[0])) {
                                IntentUtils.viewImage(finalUri, mContext);
                            }
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void selectImage() {
        performFileSearch(Constant.IMAGE_MIME_TYPE);
    }


    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    public void performFileSearch(String mimeType) {

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType(mimeType);

        startActivityForResult(intent, READ_REQUEST_CODE);
    }


    public Bitmap getBitmapFromUri(Uri uri, Context context) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                context.getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();


        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;


        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, bmOptions);
        parcelFileDescriptor.close();
        return image;
    }


}

