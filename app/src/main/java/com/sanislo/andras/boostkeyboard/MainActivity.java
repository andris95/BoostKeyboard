package com.sanislo.andras.boostkeyboard;

import android.support.v13.view.inputmethod.EditorInfoCompat;
import android.support.v13.view.inputmethod.InputConnectionCompat;
import android.support.v13.view.inputmethod.InputContentInfoCompat;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();

    private LinearLayout mLinearLayout;
    private ImageView ivTest;

    private EditText edtTest;
    private InputContentInfoCompat mCurrentInputContentInfo;
    private int mCurrentFlags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLinearLayout = (LinearLayout) findViewById(R.id.ll_root);
        ivTest = (ImageView) findViewById(R.id.iv_test);
        edtTest = (EditText)findViewById(R.id.edt_test);
        edtTest.setHint("does not support IME gif/image*s");
        edtTest.getText().append("\ud83d\ude01");

        String[] imageMimeTypes = new String[] {"image/png",
                "image/jpeg"};
        EditText edtWithImageSupport = createEditTextWithContentMimeTypes(imageMimeTypes);
        mLinearLayout.addView(edtWithImageSupport);
    }

    private boolean onCommitContent(InputContentInfoCompat inputContentInfo, int flags,
                                    Bundle opts, String[] contentMimeTypes) {
        // Clear the temporary permission (if any).  See below about why we do this here.
        try {
            if (mCurrentInputContentInfo != null) {
                mCurrentInputContentInfo.releasePermission();
            }
        } catch (Exception e) {
            Log.e(TAG, "InputContentInfoCompat#releasePermission() failed.", e);
        } finally {
            mCurrentInputContentInfo = null;
        }

        boolean supported = false;
        for (final String mimeType : contentMimeTypes) {
            if (inputContentInfo.getDescription().hasMimeType(mimeType)) {
                supported = true;
                break;
            }
        }
        if (!supported) {
            return false;
        }

        return onCommitContentInternal(inputContentInfo, flags);
    }

    private boolean onCommitContentInternal(InputContentInfoCompat inputContentInfo, int flags) {
        if ((flags & InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION) != 0) {
            try {
                inputContentInfo.requestPermission();
            } catch (Exception e) {
                Log.e(TAG, "InputContentInfoCompat#requestPermission() failed.", e);
                return false;
            }
        }

        Log.d(TAG, "onCommitContentInternal: 0 " + inputContentInfo.toString());
        Log.d(TAG, "onCommitContentInternal: 1 " + inputContentInfo.getContentUri());
        Log.d(TAG, "onCommitContentInternal: 2 " + inputContentInfo.getDescription());
        Log.d(TAG, "onCommitContentInternal: 3 " + inputContentInfo.getLinkUri());
        Uri linkUri = inputContentInfo.getLinkUri();
        if (linkUri == null) {
            linkUri = inputContentInfo.getContentUri();
        }
        Toast.makeText(getApplicationContext(), "uri null? " + (linkUri == null), Toast.LENGTH_LONG).show();
        Glide.with(this)
                .load(linkUri)
                .listener(new RequestListener<Uri, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                        if (e != null) e.printStackTrace();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(ivTest);

        // Due to the asynchronous nature of WebView, it is a bit too early to call
        // inputContentInfo.releasePermission() here. Hence we call IC#releasePermission() when this
        // method is called next time.  Note that calling IC#releasePermission() is just to be a
        // good citizen. Even if we failed to call that method, the system would eventually revoke
        // the permission sometime after inputContentInfo object gets garbage-collected.
        mCurrentInputContentInfo = inputContentInfo;
        mCurrentFlags = flags;

        return true;
    }

    /**
     * Creates a new instance of {@link EditText} that is configured to specify the given content
     * MIME types to EditorInfo#contentMimeTypes so that developers can locally test how the current
     * input method behaves for such content MIME types.
     *
     * @param contentMimeTypes A {@link String} array that indicates the supported content MIME
     *                         types
     * @return a new instance of {@link EditText}, which specifies EditorInfo#contentMimeTypes with
     * the given content MIME types
     */
    private EditText createEditTextWithContentMimeTypes(String[] contentMimeTypes) {
        final CharSequence hintText;
        final String[] mimeTypes;  // our own copy of contentMimeTypes.
        if (contentMimeTypes == null || contentMimeTypes.length == 0) {
            hintText = "MIME: []";
            mimeTypes = new String[0];
        } else {
            hintText = "MIME: " + Arrays.toString(contentMimeTypes);
            mimeTypes = Arrays.copyOf(contentMimeTypes, contentMimeTypes.length);
        }
        EditText exitText = new EditText(this) {
            @Override
            public InputConnection onCreateInputConnection(EditorInfo editorInfo) {
                final InputConnection ic = super.onCreateInputConnection(editorInfo);
                EditorInfoCompat.setContentMimeTypes(editorInfo, mimeTypes);
                final InputConnectionCompat.OnCommitContentListener callback =
                        new InputConnectionCompat.OnCommitContentListener() {
                            @Override
                            public boolean onCommitContent(InputContentInfoCompat inputContentInfo,
                                                           int flags, Bundle opts) {
                                return MainActivity.this.onCommitContent(
                                        inputContentInfo, flags, opts, mimeTypes);
                            }
                        };
                return InputConnectionCompat.createWrapper(ic, editorInfo, callback);
            }
        };
        exitText.setHint(hintText);
        //exitText.setTextColor(Color.WHITE);
        //exitText.setHintTextColor(Color.WHITE);
        return exitText;
    }
}
