package org.lit.filetool;

import android.content.Context;
import android.content.pm.*;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;

public class SecurityUtil {
    protected static final int SUCCESS = 0;
    protected static final int SIGNATURES_INVALIDATE = 3;
    protected static final int VERIFY_SIGNATURE_FAIL = 4;
    protected static final int IS_DEBUG = 2;
    private static final boolean NEED_VERIFY_CERT = true;

    public static int checkPackageSign(Context context, String fileSrc) {
        // 安装包的包信息
        boolean isDebugable = (0 != (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
        Signature[] apkSignatures = null;
        PackageInfo packageInfo = context.getPackageManager().getPackageArchiveInfo(fileSrc, PackageManager.GET_SIGNATURES);
        apkSignatures = packageInfo.signatures;
       /* if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            PackageInfo packageInfo = context.getPackageManager().getPackageArchiveInfo(fileSrc, PackageManager.GET_SIGNATURES);
            apkSignatures = packageInfo.signatures;
        } else {
            PackageInfo packageInfo = context.getPackageManager().getPackageArchiveInfo(fileSrc, PackageManager.GET_SIGNING_CERTIFICATES);
            apkSignatures = packageInfo.signingInfo.getApkContentsSigners();
        }*/
        if (apkSignatures == null) {
            // 没有签名
            new File(fileSrc).delete();
            return SIGNATURES_INVALIDATE;
        } else if (NEED_VERIFY_CERT) {
            // 验证APK证书是否和现在程序证书相同。
            // 当前应用的包信息
            Signature[] mainSignatures = null;
            try {
                PackageInfo pkgInfo = context.getPackageManager().getPackageInfo(
                        context.getPackageName(), PackageManager.GET_SIGNATURES);
                mainSignatures = pkgInfo.signatures;
                /*if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                    PackageInfo pkgInfo = context.getPackageManager().getPackageInfo(
                            context.getPackageName(), PackageManager.GET_SIGNATURES);
                    mainSignatures = pkgInfo.signatures;
                } else {
                    PackageInfo pkgInfo = context.getPackageManager().getPackageInfo(
                            context.getPackageName(), PackageManager.GET_SIGNING_CERTIFICATES);
                    mainSignatures = pkgInfo.signingInfo.getApkContentsSigners();
                }*/

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if (!isSignaturesSame(mainSignatures, apkSignatures)) {
                new File(fileSrc).delete();
                return VERIFY_SIGNATURE_FAIL;
            }
        }
        return SUCCESS;
    }

    public static boolean checkPackageName(Context context, String fileSrc) {
        PackageInfo packageInfo = context.getPackageManager().getPackageArchiveInfo(fileSrc, PackageManager.GET_ACTIVITIES);
        if (packageInfo != null) {
            boolean isSame = TextUtils.equals(context.getPackageName(), packageInfo.packageName);
            if (!isSame) {
                new File(fileSrc).delete();
            }
            return isSame;
        }
        return false;
    }

    public static boolean checkFile(Context context, String filePath) {
        File file = new File(filePath);
        if (file == null || !file.exists()) {
            return false;
        }
        if (context == null) {
            return false;
        }
        return true;
    }

    private static boolean isSignaturesSame(Signature[] mainSignatures, Signature[] apkSignatures) {
        Signature main = mainSignatures[0];
        Signature apk = apkSignatures[0];
        return main.toCharsString().toLowerCase().equals(apk.toCharsString().toLowerCase());
    }

}
