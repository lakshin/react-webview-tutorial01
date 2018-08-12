package com.advancedwebview;

import android.content.Intent;
import android.net.Uri;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.views.webview.ReactWebViewManager;


public class AdvancedWebviewManager extends ReactWebViewManager {

    private ValueCallback mUploadMessage;
    private final static int FCR=1;

    public WebView webview = null;

    private AdvancedWebviewPackage aPackage;
    public String getName() {

        return "AdvancedWebView";
    }

    @ReactProp(name = "enabledUploadAndroid")
    public void enabledUploadAndroid(WebView view, boolean enabled) {
        if(enabled) {
            webview = view;
            final AdvancedWebviewModule module = this.aPackage.getModule();
            view.setWebChromeClient(new WebChromeClient(){

                //For Android 3.0+
                public void openFileChooser(ValueCallback uploadMsg){
                    module.setUploadMessage(uploadMsg);
                    mUploadMessage = uploadMsg;
                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    i.setType("*/*");
                    module.getActivity().startActivityForResult(Intent.createChooser(i, "File Chooser"), FCR);
                }
                // For Android 3.0+, above method not supported in some android 3+ versions, in such case we use this
                public void openFileChooser(ValueCallback uploadMsg, String acceptType){
                    module.setUploadMessage(uploadMsg);
                    mUploadMessage = uploadMsg;
                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    i.setType("*/*");
                    module.getActivity().startActivityForResult(
                            Intent.createChooser(i, "File Browser"),
                            FCR);
                }
                //For Android 4.1+
                public void openFileChooser(ValueCallback uploadMsg, String acceptType, String capture){
                    module.setUploadMessage(uploadMsg);
                    mUploadMessage = uploadMsg;
                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    i.setType("*/*");
                    module.getActivity().startActivityForResult(Intent.createChooser(i, "File Chooser"), FCR);
                }
                //For Android 5.0+
                public boolean onShowFileChooser(
                        WebView webView, ValueCallback<Uri[]> filePathCallback,
                        WebChromeClient.FileChooserParams fileChooserParams) {
                    module.setmUploadCallbackAboveL(filePathCallback);
                    /*if(mUMA != null){
                        mUMA.onReceiveValue(null);
                    }*/
                    if (module.grantPermissions()) {
                        module.uploadImage(filePathCallback);
                    }
                    return true;
                }
            });

        }
    }

    public void setPackage(AdvancedWebviewPackage aPackage){
        this.aPackage = aPackage;
    }

    public AdvancedWebviewPackage getPackage(){
        return this.aPackage;
    }
}