import {requireNativeModule} from 'expo-modules-core';
export type WifiNetwork={ssid:string;signal:number;channel:number|null;authentication:string;band:string};
export type WifiStatus={ssid:string;state:string;signal:string};
type NativeWifi={scan():Promise<WifiNetwork[]>;status():Promise<WifiStatus>;requestConnection(ssid:string,password:string,authentication:string):Promise<{requested:boolean;message:string}>};
export default requireNativeModule<NativeWifi>('WifiManager');
