const {withAndroidManifest}=require('expo/config-plugins');
module.exports=function withWifiPermissions(config){return withAndroidManifest(config, c=>{const a=c.modResults.manifest.application?.[0];if(a){a.$['android:usesCleartextTraffic']='false';}return c;});};
