package org.lit.filetool;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import org.apache.cordova.CordovaPlugin;

import java.io.File;

import org.apache.cordova.CallbackContext;

import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FileTransferOpener extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("install")) {
            String fileUrl = args.getString(0);
            String contentType = "application/vnd.android.package-archive";
            this.install(fileUrl, contentType, callbackContext);
            return true;
        }
        return false;
    }

    private void install(String fileArg, String contentType, CallbackContext callbackContext) throws JSONException {
        String fileName = "";
        try {
            CordovaResourceApi resourceApi = webView.getResourceApi();
            Uri fileUri = resourceApi.remapUri(Uri.parse(fileArg));
            fileName = fileUri.getPath();
        } catch (Exception e) {
            fileName = fileArg;
        }
        File file = new File(fileName);
        Context context = cordova.getActivity().getApplicationContext();
        boolean fileExist = SecurityUtil.checkFile(context, file.getPath());
        if (fileExist) {
            boolean packageNameSame = SecurityUtil.checkPackageName(context, file.getPath());
            if (packageNameSame) {
                int signStatus = SecurityUtil.checkPackageSign(context, file.getPath());
                if (signStatus == SecurityUtil.SUCCESS) {
                    try {
                        context.getPackageManager().getPackageArchiveInfo(fileName, 0);
                        Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                        Uri path;
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                            path = Uri.fromFile(file);
                        } else {
                            path = FileProvider.getUriForFile(context, cordova.getActivity().getPackageName() + ".provider", file);
                        }
                        intent.setDataAndType(path, contentType);
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
                        cordova.getActivity().startActivity(intent);
                    } catch (android.content.ActivityNotFoundException e) {
                        JSONObject errorObj = new JSONObject();
                        errorObj.put("status", PluginResult.Status.ERROR.ordinal());
                        errorObj.put("message", "Activity not found: " + e.getMessage());
                        callbackContext.error(errorObj);
                    }
                } else {
                    JSONObject errorObj = new JSONObject();
                    errorObj.put("status", PluginResult.Status.ERROR.ordinal());
                    if (signStatus == SecurityUtil.VERIFY_SIGNATURE_FAIL) {
                        errorObj.put("message", "Signature is not same");
                    } else if (signStatus == SecurityUtil.SIGNATURES_INVALIDATE) {
                        errorObj.put("message", "Has no signature");
                    } else if(signStatus == SecurityUtil.IS_DEBUG) {
                        errorObj.put("message", "Debuggable");
                    }
                    callbackContext.error(errorObj);
                }
            } else {
                JSONObject errorObj = new JSONObject();
                errorObj.put("status", PluginResult.Status.ERROR.ordinal());
                errorObj.put("message", "PackageName is not same");
                callbackContext.error(errorObj);
            }
        } else {
            JSONObject errorObj = new JSONObject();
            errorObj.put("status", PluginResult.Status.ERROR.ordinal());
            if (!fileExist) {
                errorObj.put("message", "File not found");
            } else {
                errorObj.put("message", "Other error");
            }
            callbackContext.error(errorObj);
        }
    }


}
