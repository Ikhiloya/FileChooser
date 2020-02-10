package com.ikhiloyaimokhai.fileupload;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

import static com.ikhiloyaimokhai.fileupload.Constant.*;

public class MainActivity extends AppCompatActivity {

    private static final int RESULT_LOAD_IMG = 22;
    private static final int READ_REQUEST_CODE = 42;
    private static final String TAG = MainActivity.class.getSimpleName();
    // Unique request code.
    private static final int WRITE_REQUEST_CODE = 43;

    ImageView imageView;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);
        mContext = MainActivity.this;

    }

    public void selectImage(View view) {

//        performFileSearch(Constant.IMAGE_MIME_TYPE);
//        performFileSearch(Constant.TEXT_MIME_TYPE);
//        performFileSearch(DOC_MIME_TYPE);
        performFileSearch(ALL_FILES);

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


    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                File file = new File(Objects.requireNonNull(uri.getPath()));

                Log.i(TAG, "Uri: " + uri.toString());
                Log.i(TAG, "file: " + file);
                Log.i(TAG, "file: " + file.getName());
                try {
//                    InputStream inputStream = IntentUtils.readInputStreamFromUri(uri, MainActivity.this);

                    byte[] bytes = IntentUtils.convertImageToByte(uri, MainActivity.this);
//                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                    Log.i(TAG, "ibytes: " + bytes);


                    boolean isVirtualFile = IntentUtils.isVirtualFile(uri, mContext);
                    Log.i(TAG, "isVirtual File: " + isVirtualFile);
                    Log.i(TAG, String.format("MimeType is:: %s", IntentUtils.getMimeType(mContext, uri)));
                    final String mimeType = IntentUtils.getMimeType(MainActivity.this, uri);
//                    readTextFromUri(uri);
                    IntentUtils.dumpImageMetaData(uri, mContext);

                    Bitmap bitmapFromUriCompressed = getBitmapFromUri(uri, mContext);

                    imageView.setImageBitmap(bitmapFromUriCompressed);
                    final Uri finalUri = uri;
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (mimeType.startsWith(DOC_MIME_TYPE.split(SPLIT)[0])) {
                                IntentUtils.viewPdf(finalUri, mContext);
                            } else if (mimeType.startsWith(TEXT_MIME_TYPE.split(SPLIT)[0])) {
                                IntentUtils.viewText(finalUri, mContext);
                            } else if (mimeType.startsWith(IMAGE_MIME_TYPE.split(SPLIT)[0])) {
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


    /*
    Here are some examples of how you might call this method.
    The first parameter is the MIME type, and the second parameter is the name
    of the file you are creating:
    createFile("text/plain", "foobar.txt");
    createFile("image/png", "mypicture.png");
    */

    private void createFile(String mimeType, String fileName) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

        // Filter to only show results that can be "opened", such as
        // a file (as opposed to a list of contacts or timezones).
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Create a file with the requested MIME type.
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        startActivityForResult(intent, WRITE_REQUEST_CODE);
    }


//    private InputStream getInputStreamForVirtualFile(Uri uri, String mimeTypeFilter)
//            throws IOException {
//
//        ContentResolver resolver = getContentResolver();
//
//        String[] openableMimeTypes = resolver.getStreamTypes(uri, mimeTypeFilter);
//
//        if (openableMimeTypes == null || openableMimeTypes.length &lt; 1) {
//            throw new FileNotFoundException();
//        }
//
//        return resolver
//                .openTypedAssetFileDescriptor(uri, openableMimeTypes[0], null)
//                .createInputStream();
//    }

    public String BitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }


    /**
     * @param encodedString
     * @return bitmap (from given string)
     */
    public Bitmap StringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

}
