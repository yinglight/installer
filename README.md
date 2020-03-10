#### installer
用于ionic框架应用升级安装时校验。
#### 安装插件
```
npm i cordova-valley-installer --save
cordova plugin add cordova-valley-installer
```
#### js/ionic使用方法

```
(<any>window).installAPK.install("fileUrl", errorCallback, successCallback);
window.installAPK.install("fileUrl", errorCallback, successCallback);
```