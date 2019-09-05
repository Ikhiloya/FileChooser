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
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.BufferedReader;
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


        String split = DOC_MIME_TYPE.split(SPLIT)[0];

        System.out.println("*****************************" + split);
        mContext = MainActivity.this;


//        imageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                viewImage(Uri.parse("content://com.android.providers.media.documents/document/image%3A62"), MainActivity.this);
//            }
//        });
    }

    public void selectImage(View view) {
//        viewPdf();
//        createFile("image/png", "mypicture.png");
//        performFileSearch(Constant.IMAGE_MIME_TYPE);
//        performFileSearch(Constant.TEXT_MIME_TYPE);
        performFileSearch(DOC_MIME_TYPE);
//
//        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
//        photoPickerIntent.setType("image/*");
//        startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//
//        if (resultCode == RESULT_OK) {
//            try {
//                final Uri imageUri = data.getData();
//                System.out.println("****************URI" + imageUri);
//                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
//                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
//                imageView.setImageBitmap(selectedImage);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//                Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
//            }
//
//        } else {
//            Toast.makeText(MainActivity.this, "You haven't picked Image", Toast.LENGTH_LONG).show();
//        }
//    }


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
                Log.i(TAG, "Uri: " + uri.toString());
                try {
                    boolean isVirtualFile = IntentUtils.isVirtualFile(uri, mContext);
                    Log.i(TAG, "isVirtual File: " + isVirtualFile);
                    Log.i(TAG, String.format("MimeType is:: %s", IntentUtils.getMimeType(mContext, uri)));
                    final String mimeType = IntentUtils.getMimeType(MainActivity.this, uri);
//                    readTextFromUri(uri);
                    IntentUtils.dumpImageMetaData(uri, mContext);
                    imageView.setImageBitmap(IntentUtils.getBitmapFromUri(uri, mContext));
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


}
