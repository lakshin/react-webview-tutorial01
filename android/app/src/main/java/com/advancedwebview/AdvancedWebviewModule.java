package com.advancedwebview;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.Toast;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.common.annotations.VisibleForTesting;
import com.facebook.react.modules.core.PermissionAwareActivity;
import com.facebook.react.modules.core.PermissionListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;
import static android.content.Context.NOTIFICATION_SERVICE;

public class AdvancedWebviewModule extends ReactContextBaseJavaModule implements ActivityEventListener {
    private ValueCallback mUploadMessage;
    private ValueCallback<Uri[]> mUploadCallbackAboveL;
    private String mCM;
    public static final int MY_PERMISSIONS_REQUEST_STORAGE = 1;
    public static final int MY_PERMISSIONS_REQUEST_ALL = 2;
    private final int NOTIFICATION_ID = 1;
    public String downUrl = null;
    private final static int FCR=1;
    private ValueCallback<Uri[]> mUMA;
    private String[] permissions = {Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @VisibleForTesting
    public static final String REACT_CLASS = "AdvancedWebview";
    public ReactContext REACT_CONTEXT;

    public AdvancedWebviewModule(ReactApplicationContext context){

        super(context);
        REACT_CONTEXT = context;
        context.addActivityEventListener(this);
    }

    private AdvancedWebviewPackage aPackage;

    public void setPackage(AdvancedWebviewPackage aPackage) {
        this.aPackage = aPackage;
    }

    public AdvancedWebviewPackage getPackage() {
        return this.aPackage;
    }

    @Override
    public String getName(){
        return REACT_CLASS;
    }

    @SuppressWarnings("unused")
    public Activity getActivity() {
        return getCurrentActivity();
    }

    public ReactContext getReactContext(){
        return REACT_CONTEXT;
    }

    @ReactMethod
    public void getUrl(Callback errorCallback,
                       final Callback successCallback) {
        try {
            final WebView view = getPackage().getManager().webview;

            if(getPackage().getManager().webview != null) {
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        successCallback.invoke(view.getUrl());
                    }
                });
            }else{
                successCallback.invoke("");
            }
        }catch(Exception e){
            errorCallback.invoke(e.getMessage());
        }
    }

    public void setUploadMessage(ValueCallback uploadMessage) {
        mUploadMessage = uploadMessage;
    }


    public void setmUploadCallbackAboveL(ValueCallback<Uri[]> mUploadCallbackAboveL) {
        this.mUploadCallbackAboveL = mUploadCallbackAboveL;
    }

    public void setmCM(String mCM) {
        this.mCM = mCM;
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(activity,requestCode, resultCode, intent);
        if (requestCode == 1) {
            if (null == mUploadMessage && null == mUploadCallbackAboveL) return;
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if (mUploadCallbackAboveL != null) {
                onActivityResultAboveL(requestCode, resultCode, data);
            } else if (mUploadMessage != null) {
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }
        }
    }

    @Override
    public void onNewIntent(Intent intent) {

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onActivityResultAboveL(int requestCode, int resultCode, Intent data) {
        if (requestCode != 1
                || mUploadCallbackAboveL == null) {
            return;
        }
        Uri[] results = null;
        if (resultCode == RESULT_OK) {
            if (data == null) {
                if(mCM != null){
                    results = new Uri[]{Uri.parse(mCM)};
                }
            } else {
                String dataString = data.getDataString();
                ClipData clipData = data.getClipData();
                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }
                if (dataString != null)
                    results = new Uri[]{Uri.parse(dataString)};
            }
        }
        mUploadCallbackAboveL.onReceiveValue(results);
        mUploadCallbackAboveL = null;
        return;
    }

    private PermissionListener listener = new PermissionListener() {
        @Override
        public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
            switch (requestCode) {
                case MY_PERMISSIONS_REQUEST_ALL: {
                    // If request is cancelled, the result arrays are empty.
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        if(mUploadCallbackAboveL != null){
                            uploadImage(mUploadCallbackAboveL);
                        }
                    } else {
                        Toast.makeText(getActivity(),"Please allow App Name to access storage to upload the image", Toast.LENGTH_LONG).show();
                    }
                    return true;
                }
            }
            return false;
        }
    };

    public boolean grantPermissions() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            return true;
        }
        boolean result = true;
        for (String permission:permissions){
            if (ContextCompat.checkSelfPermission(this.getActivity(),
                    permission)
                    != PackageManager.PERMISSION_GRANTED) {
                result = false;
            }

        }

        if(!result){
            PermissionAwareActivity activity = getPermissionAwareActivity();

            activity.requestPermissions(permissions, MY_PERMISSIONS_REQUEST_ALL,listener);

        }
        return result;
    }

    private PermissionAwareActivity getPermissionAwareActivity() {
        Activity activity = getCurrentActivity();
        if (activity == null) {
            throw new IllegalStateException("Tried to use permissions API while not attached to an " +
                    "Activity.");
        } else if (!(activity instanceof PermissionAwareActivity)) {
            throw new IllegalStateException("Tried to use permissions API but the host Activity doesn't" +
                    " implement PermissionAwareActivity.");
        }
        return (PermissionAwareActivity) activity;
    }


    public void uploadImage(ValueCallback<Uri[]> filePathCallback){
        mUMA = filePathCallback;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
                takePictureIntent.putExtra("PhotoPath", mCM);
            } catch (IOException ex) {
                Log.e(TAG, "Image file creation failed", ex);
            }
            if (photoFile != null) {
                mCM = "file:" + photoFile.getAbsolutePath();
                setmCM(mCM);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            } else {
                takePictureIntent = null;
            }
        }
        Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
        contentSelectionIntent.setType("*/*");
        Intent[] intentArray;
        if (takePictureIntent != null) {
            intentArray = new Intent[]{takePictureIntent};
        } else {
            intentArray = new Intent[0];
        }
        Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
        chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
        getActivity().startActivityForResult(chooserIntent, FCR);
    }

    // Create an image file
    private File createImageFile() throws IOException{
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "img_"+timeStamp+"_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName,".jpg",storageDir);
    }
}
