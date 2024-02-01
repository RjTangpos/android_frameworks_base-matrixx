/*
 * Copyright (C) 2020 The Pixel Experience Project
 *               2021-2024 crDroid Android Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.internal.util.crdroid;

import android.app.ActivityTaskManager;
import android.app.Application;
import android.app.TaskStackListener;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Process;
import android.os.SystemProperties;
import android.util.Log;

import com.android.internal.R;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.android.internal.util.crdroid.Utils;

public class PixelPropsUtils {

    private static final String TAG = PixelPropsUtils.class.getSimpleName();
    private static final String DEVICE = "ro.product.device";
    private static final boolean DEBUG = false;

    private static final String PACKAGE_GMS = "com.google.android.gms";
    private static final String PROCESS_GMS_UNSTABLE = PACKAGE_GMS + ".unstable";
    private static final String SPOOF_PIXEL_GAMES = "persist.sys.pixelprops.games";
    private static final String SPOOF_PIXEL_GPHOTOS = "persist.sys.pixelprops.gphotos";
    private static final String SPOOF_PIXEL_NETFLIX = "persist.sys.pixelprops.netflix";

    private static final Boolean sEnablePixelProps =
            Resources.getSystem().getBoolean(R.bool.config_enablePixelProps);

    private static final Map<String, Object> propsToChangeGeneric;
    private static final Map<String, Object> propsToChangePixel8Pro;
    private static final Map<String, Object> propsToChangePixelXL;
    private static final Map<String, Object> propsToChangePixel5a;
    private static final Map<String, Object> propsToChangeROG6;
    private static final Map<String, Object> propsToChangeXP5;
    private static final Map<String, Object> propsToChangeOP8P;
    private static final Map<String, Object> propsToChangeOP9P;
    private static final Map<String, Object> propsToChangeMI11TP;
    private static final Map<String, Object> propsToChangeMI13P;
    private static final Map<String, Object> propsToChangeF5;
    private static final Map<String, Object> propsToChangeBS4;
    private static final Map<String, ArrayList<String>> propsToKeep;

    // Packages to Spoof as Pixel 8 Pro
    private static final String[] packagesToChangePixel8Pro = {
            "com.android.chrome",
            "com.android.vending",
            "com.google.android.apps.customization.pixel",
            "com.google.android.apps.emojiwallpaper",
            "com.google.android.apps.privacy.wildlife",
            "com.google.android.apps.subscriptions.red",
            "com.google.android.apps.wallpaper",
            "com.google.android.apps.wallpaper.pixel",
            "com.google.android.googlequicksearchbox",
            "com.google.android.wallpaper.effects",
            "com.google.pixel.livewallpaper",
            "com.nhs.online.nhsonline",
            "com.netflix.mediaclient"
    };

    // Packages to Spoof as Pixel 5a
    private static final String[] packagesToChangePixel5a = {
            "com.google.android.tts",
            "com.breel.wallpapers20"
    };

    // Packages to Keep with original device
    private static final String[] packagesToKeep = {
            "com.google.android.apps.motionsense.bridge",
            "com.google.android.apps.nexuslauncher",
            "com.google.android.apps.pixelmigrate",
            "com.google.android.apps.recorder",
            "com.google.android.apps.restore",
            "com.google.android.apps.tachyon",
            "com.google.android.apps.tycho",
            "com.google.android.apps.wearables.maestro.companion",
            "com.google.android.apps.youtube.kids",
            "com.google.android.apps.youtube.music",
            "com.google.android.as",
            "com.google.android.dialer",
            "com.google.android.euicc",
            "com.google.android.setupwizard",
            "com.google.android.youtube",
            "com.google.ar.core",
            "com.google.intelligence.sense",
            "com.google.oslo"
    };

    // Packages to Spoof as ROG Phone 6
    private static final String[] packagesToChangeROG6 = {
            "com.activision.callofduty.shooter",
    	    "com.ea.gp.fifamobile",
            "com.gameloft.android.ANMP.GloftA9HM",
            "com.madfingergames.legends",
            "com.pearlabyss.blackdesertm",
            "com.pearlabyss.blackdesertm.gl"
    };

    // Packages to Spoof as Xperia 5
    private static final String[] packagesToChangeXP5 = {
            "com.garena.game.codm",
            "com.tencent.tmgp.kr.codm",
            "com.vng.codmvn"
    };

    // Packages to Spoof as OnePlus 8 Pro
    private static final String[] packagesToChangeOP8P = {
            "com.netease.lztgglobal",
            "com.pubg.imobile",
            "com.pubg.krmobile",
            "com.rekoo.pubgm",
            "com.riotgames.league.wildrift",
            "com.riotgames.league.wildrifttw",
            "com.riotgames.league.wildriftvn",
            "com.riotgames.league.teamfighttactics",
            "com.riotgames.league.teamfighttacticstw",
            "com.riotgames.league.teamfighttacticsvn",
            "com.tencent.ig",
            "com.tencent.tmgp.pubgmhd",
            "com.vng.pubgmobile"
    };

    // Packages to Spoof as OnePlus 9 Pro
    private static final String[] packagesToChangeOP9P = {
            "com.epicgames.fortnite",
            "com.epicgames.portal",
            "com.tencent.lolm"
    };

    // Packages to Spoof as Mi 11T Pro
    private static final String[] packagesToChangeMI11TP = {
            "com.ea.gp.apexlegendsmobilefps",
            "com.levelinfinite.hotta.gp",
            "com.supercell.clashofclans",
            "com.vng.mlbbvn"
    };

    // Packages to Spoof as Xiaomi 13 Pro
    private static final String[] packagesToChangeMI13P = {
            "com.levelinfinite.sgameGlobal",
            "com.tencent.tmgp.sgame"
    };

    // Packages to Spoof as POCO F5
    private static final String[] packagesToChangeF5 = {
            "com.dts.freefiremax",
            "com.dts.freefireth",
            "com.mobile.legends"
    };

    // Packages to Spoof as Black Shark 4
    private static final String[] packagesToChangeBS4 = {
            "com.proximabeta.mf.uamo"
    };

    private static final ComponentName GMS_ADD_ACCOUNT_ACTIVITY = ComponentName.unflattenFromString(
            "com.google.android.gms/.auth.uiflows.minutemaid.MinuteMaidActivity");

    private static volatile boolean sIsGms, sIsFinsky;
    private static volatile String sProcessName;

    static {
        propsToKeep = new HashMap<>();
        propsToKeep.put("com.google.android.settings.intelligence", new ArrayList<>(Collections.singletonList("FINGERPRINT")));
        propsToChangeGeneric = new HashMap<>();
        propsToChangeGeneric.put("TYPE", "user");
        propsToChangeGeneric.put("TAGS", "release-keys");
        propsToChangePixel8Pro = new HashMap<>();
        propsToChangePixel8Pro.put("BRAND", "google");
        propsToChangePixel8Pro.put("MANUFACTURER", "Google");
        propsToChangePixel8Pro.put("DEVICE", "husky");
        propsToChangePixel8Pro.put("PRODUCT", "husky");
        propsToChangePixel8Pro.put("HARDWARE", "husky");
        propsToChangePixel8Pro.put("MODEL", "Pixel 8 Pro");
        propsToChangePixel8Pro.put("ID", "UQ1A.240205.004");
        propsToChangePixel8Pro.put("FINGERPRINT", "google/husky/husky:14/UQ1A.240205.004/11269751:user/release-keys");
        propsToChangePixel5a = new HashMap<>();
        propsToChangePixel5a.put("BRAND", "google");
        propsToChangePixel5a.put("MANUFACTURER", "Google");
        propsToChangePixel5a.put("DEVICE", "barbet");
        propsToChangePixel5a.put("PRODUCT", "barbet");
        propsToChangePixel5a.put("HARDWARE", "barbet");
        propsToChangePixel5a.put("MODEL", "Pixel 5a");
        propsToChangePixel5a.put("ID", "UQ1A.240205.002");
        propsToChangePixel5a.put("FINGERPRINT", "google/barbet/barbet:14/UQ1A.240205.002/11224170:user/release-keys");
        propsToChangePixelXL = new HashMap<>();
        propsToChangePixelXL.put("BRAND", "google");
        propsToChangePixelXL.put("MANUFACTURER", "Google");
        propsToChangePixelXL.put("DEVICE", "marlin");
        propsToChangePixelXL.put("PRODUCT", "marlin");
        propsToChangePixelXL.put("HARDWARE", "marlin");
        propsToChangePixelXL.put("MODEL", "Pixel XL");
        propsToChangePixelXL.put("ID", "QP1A.191005.007.A3");
        propsToChangePixelXL.put("FINGERPRINT", "google/marlin/marlin:10/QP1A.191005.007.A3/5972272:user/release-keys");
        propsToChangeROG6 = new HashMap<>();
        propsToChangeROG6.put("BRAND", "asus");
        propsToChangeROG6.put("MANUFACTURER", "asus");
        propsToChangeROG6.put("DEVICE", "AI2201");
        propsToChangeROG6.put("MODEL", "ASUS_AI2201");
        propsToChangeXP5 = new HashMap<>();
        propsToChangeXP5.put("MODEL", "SO-52A");
        propsToChangeXP5.put("MANUFACTURER", "Sony");
        propsToChangeOP8P = new HashMap<>();
        propsToChangeOP8P.put("MODEL", "IN2020");
        propsToChangeOP8P.put("MANUFACTURER", "OnePlus");
        propsToChangeOP9P = new HashMap<>();
        propsToChangeOP9P.put("MODEL", "LE2123");
        propsToChangeOP9P.put("MANUFACTURER", "OnePlus");
        propsToChangeMI11TP = new HashMap<>();
        propsToChangeMI11TP.put("MODEL", "2107113SI");
        propsToChangeMI11TP.put("MANUFACTURER", "Xiaomi");
        propsToChangeMI13P = new HashMap<>();
        propsToChangeMI13P.put("BRAND", "Xiaomi");
        propsToChangeMI13P.put("MANUFACTURER", "Xiaomi");
        propsToChangeMI13P.put("MODEL", "2210132C");
        propsToChangeF5 = new HashMap<>();
        propsToChangeF5.put("MODEL", "23049PCD8G");
        propsToChangeF5.put("MANUFACTURER", "Xiaomi");
        propsToChangeBS4 = new HashMap<>();
        propsToChangeBS4.put("MODEL", "2SM-X706B");
        propsToChangeBS4.put("MANUFACTURER", "blackshark");
    }

    private static boolean shouldTryToCertifyDevice() {
        final boolean was = isGmsAddAccountActivityOnTop();
        final String reason = "GmsAddAccountActivityOnTop";
        if (!was) {
            return true;
        }
        dlog("Skip spoofing build for GMS, because " + reason + "!");
        TaskStackListener taskStackListener = new TaskStackListener() {
            @Override
            public void onTaskStackChanged() {
                final boolean isNow = isGmsAddAccountActivityOnTop();
                if (isNow ^ was) {
                    dlog(String.format("%s changed: isNow=%b, was=%b, killing myself!", reason, isNow, was));
                    Process.killProcess(Process.myPid());
                }
            }
        };
        try {
            ActivityTaskManager.getService().registerTaskStackListener(taskStackListener);
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Failed to register task stack listener!", e);
            return true;
        }
    }

    public static void setProps(Context context) {
        final String packageName = context.getPackageName();
        final String processName = Application.getProcessName();

        propsToChangeGeneric.forEach((k, v) -> setPropValue(k, v));
        if (packageName == null || processName == null || packageName.isEmpty()) {
            return;
        }
        if (Arrays.asList(packagesToKeep).contains(packageName)) {
            return;
        }
        if (Arrays.asList(packagesToKeep).contains(packageName) ||
                packageName.startsWith("com.google.android.GoogleCamera")) {
            return;
        }
        Map<String, Object> propsToChange = new HashMap<>();
        sProcessName = processName;
        sIsGms = packageName.equals(PACKAGE_GMS) && processName.equals(PROCESS_GMS_UNSTABLE);
        sIsFinsky = packageName.equals("com.android.vending");

        if (sIsGms) {
            if (shouldTryToCertifyDevice()) {
                dlog("Spoofing build for GMS");
                spoofBuildGms(context);
            }
        } else if (packageName.equals(PACKAGE_GMS)) {
            setPropValue("TIME", System.currentTimeMillis());
        } else if (packageName.startsWith("com.google.")
                || packageName.startsWith("com.samsung.")
                || Arrays.asList(packagesToChangePixel8Pro).contains(packageName)) {

            if (!sEnablePixelProps) {
                dlog("Pixel props is disabled by config");
                return;
            } else if (Arrays.asList(packagesToChangePixel8Pro).contains(packageName)) {
                propsToChange.putAll(propsToChangePixel8Pro);
            } else {
                propsToChange.putAll(propsToChangePixel5a);
            }
        } else if (packageName.equals("com.google.android.apps.photos")) {
            if (SystemProperties.getBoolean(SPOOF_PIXEL_GPHOTOS, true)) {
                propsToChange.putAll(propsToChangePixelXL);
            } else {
                propsToChange.putAll(propsToChangePixel5a);
            }
        } else if (packageName.equals("com.netflix.mediaclient") && 
                    !SystemProperties.getBoolean(SPOOF_PIXEL_NETFLIX, false)) {
               if (DEBUG) Log.d(TAG, "Netflix spoofing disabled by system prop");
                return;
        } else {
            if (!SystemProperties.getBoolean(SPOOF_PIXEL_GAMES, false))
                return;

            if (Arrays.asList(packagesToChangeROG6).contains(packageName)) {
                if (DEBUG) Log.d(TAG, "Defining props for: " + packageName);
                for (Map.Entry<String, Object> prop : propsToChangeROG6.entrySet()) {
                    String key = prop.getKey();
                    Object value = prop.getValue();
                    setPropValue(key, value);
                }
            } else if (Arrays.asList(packagesToChangeXP5).contains(packageName)) {
                if (DEBUG) Log.d(TAG, "Defining props for: " + packageName);
                for (Map.Entry<String, Object> prop : propsToChangeXP5.entrySet()) {
                    String key = prop.getKey();
                    Object value = prop.getValue();
                    setPropValue(key, value);
                }
            } else if (Arrays.asList(packagesToChangeOP8P).contains(packageName)) {
                if (DEBUG) Log.d(TAG, "Defining props for: " + packageName);
                for (Map.Entry<String, Object> prop : propsToChangeOP8P.entrySet()) {
                    String key = prop.getKey();
                    Object value = prop.getValue();
                    setPropValue(key, value);
                }
            } else if (Arrays.asList(packagesToChangeOP9P).contains(packageName)) {
                if (DEBUG) Log.d(TAG, "Defining props for: " + packageName);
                for (Map.Entry<String, Object> prop : propsToChangeOP9P.entrySet()) {
                    String key = prop.getKey();
                    Object value = prop.getValue();
                    setPropValue(key, value);
                }
            } else if (Arrays.asList(packagesToChangeMI11TP).contains(packageName)) {
                if (DEBUG) Log.d(TAG, "Defining props for: " + packageName);
                for (Map.Entry<String, Object> prop : propsToChangeMI11TP.entrySet()) {
                    String key = prop.getKey();
                    Object value = prop.getValue();
                    setPropValue(key, value);
                }
            } else if (Arrays.asList(packagesToChangeMI13P).contains(packageName)) {
                if (DEBUG) Log.d(TAG, "Defining props for: " + packageName);
                for (Map.Entry<String, Object> prop : propsToChangeMI13P.entrySet()) {
                    String key = prop.getKey();
                    Object value = prop.getValue();
                    setPropValue(key, value);
                }
            } else if (Arrays.asList(packagesToChangeF5).contains(packageName)) {
                if (DEBUG) Log.d(TAG, "Defining props for: " + packageName);
                for (Map.Entry<String, Object> prop : propsToChangeF5.entrySet()) {
                    String key = prop.getKey();
                    Object value = prop.getValue();
                    setPropValue(key, value);
                }
            } else if (Arrays.asList(packagesToChangeBS4).contains(packageName)) {
                if (DEBUG) Log.d(TAG, "Defining props for: " + packageName);
                for (Map.Entry<String, Object> prop : propsToChangeBS4.entrySet()) {
                    String key = prop.getKey();
                    Object value = prop.getValue();
                    setPropValue(key, value);
                }
            }
        }
        dlog("Defining props for: " + packageName);
        for (Map.Entry<String, Object> prop : propsToChange.entrySet()) {
            String key = prop.getKey();
            Object value = prop.getValue();
            if (propsToKeep.containsKey(packageName) && propsToKeep.get(packageName).contains(key)) {
                dlog("Not defining " + key + " prop for: " + packageName);
                continue;
            }
            dlog("Defining " + key + " prop for: " + packageName);
            setPropValue(key, value);
        }
        // Set proper indexing fingerprint
        if (packageName.equals("com.google.android.settings.intelligence")) {
            setPropValue("FINGERPRINT", String.valueOf(Build.TIME));
            return;
        }
    }

    private static void setPropValue(String key, Object value) {
        try {
            if (DEBUG) Log.d(TAG, "Defining prop " + key + " to " + value.toString());
            Field field = Build.class.getDeclaredField(key);
            field.setAccessible(true);
            field.set(null, value);
            field.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Log.e(TAG, "Failed to set prop " + key, e);
        }
    }

    private static void setVersionField(String key, Object value) {
        try {
            if (DEBUG) Log.d(TAG, "Defining prop " + key + " to " + value.toString());
            Field field = Build.VERSION.class.getDeclaredField(key);
            field.setAccessible(true);
            field.set(null, value);
            field.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Log.e(TAG, "Failed to set prop " + key, e);
        }
    }

    private static void setVersionFieldString(String key, String value) {
        try {
            if (DEBUG) Log.d(TAG, "Defining prop " + key + " to " + value);
            Field field = Build.VERSION.class.getDeclaredField(key);
            field.setAccessible(true);
            field.set(null, value);
            field.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Log.e(TAG, "Failed to set prop " + key, e);
        }
    }

    private static void setVersionFieldInt(String key, int value) {
        try {
            Field field = Build.VERSION.class.getDeclaredField(key);
            field.setAccessible(true);
            field.set(null, value);
            field.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Log.e(TAG, "Failed to spoof Build." + key, e);
        }
    }

    private static String getBuildID(String fingerprint) {
        Pattern pattern = Pattern.compile("([A-Za-z0-9]+\\.\\d+\\.\\d+\\.\\w+)");
        Matcher matcher = pattern.matcher(fingerprint);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    private static String getDeviceName(String fingerprint) {
        String[] parts = fingerprint.split("/");
        if (parts.length >= 2) {
            return parts[1];
        }
        return "";
    }

    private static void spoofBuildGms(Context context) {
        String packageName = "com.goolag.pif";

        if (!Utils.isPackageInstalled(context, packageName)) {
            Log.e(TAG, "'" + packageName + "' is not installed.");
            return;
        }

        PackageManager pm = context.getPackageManager();

        try {
            Resources resources = pm.getResourcesForApplication(packageName);

            int resourceId = resources.getIdentifier("device_arrays", "array", packageName);
            if (resourceId != 0) {
                String[] deviceArrays = resources.getStringArray(resourceId);

                if (deviceArrays.length > 0) {
                    int randomIndex = new Random().nextInt(deviceArrays.length);
                    int selectedArrayResId = resources.getIdentifier(deviceArrays[randomIndex], "array", packageName);
                    String[] selectedDeviceProps = resources.getStringArray(selectedArrayResId);

                    setPropValue("PRODUCT", selectedDeviceProps[0]);
                    setPropValue("DEVICE", selectedDeviceProps[1].isEmpty() ? getDeviceName(selectedDeviceProps[5]) : selectedDeviceProps[1]);
                    setPropValue("MANUFACTURER", selectedDeviceProps[2]);
                    setPropValue("BRAND", selectedDeviceProps[3]);
                    setPropValue("MODEL", selectedDeviceProps[4]);
                    setPropValue("FINGERPRINT", selectedDeviceProps[5]);
                    setVersionFieldString("SECURITY_PATCH", selectedDeviceProps[6]);
                    if (!selectedDeviceProps[7].isEmpty() && selectedDeviceProps[7].matches("2[3-6]")) {
                        setVersionFieldInt("DEVICE_INITIAL_SDK_INT", Integer.parseInt(selectedDeviceProps[7]));
                    } else {
                        Log.e(TAG, "Value for DEVICE_INITIAL_SDK_INT must be between 23-26!");
                    }
                    setPropValue("ID", selectedDeviceProps[8].isEmpty() ? getBuildID(selectedDeviceProps[5]) : selectedDeviceProps[8]);
                    setPropValue("TYPE", selectedDeviceProps[9].isEmpty() ? "user" : selectedDeviceProps[9]);
                    setPropValue("TAGS", selectedDeviceProps[10].isEmpty() ? "release-keys" : selectedDeviceProps[10]);
                } else {
                    Log.e(TAG, "No device arrays found.");
                }
            } else {
                Log.e(TAG, "Resource 'device_arrays' not found.");
            }

        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Error getting resources for '" + packageName + "': " + e.getMessage());
        }
    }

    private static boolean isGmsAddAccountActivityOnTop() {
        try {
            final ActivityTaskManager.RootTaskInfo focusedTask =
                    ActivityTaskManager.getService().getFocusedRootTaskInfo();
            return focusedTask != null && focusedTask.topActivity != null
                    && focusedTask.topActivity.equals(GMS_ADD_ACCOUNT_ACTIVITY);
        } catch (Exception e) {
            Log.e(TAG, "Unable to get top activity!", e);
        }
        return false;
    }

    private static boolean isCallerSafetyNet() {
        return shouldTryToCertifyDevice() && sIsGms && Arrays.stream(Thread.currentThread().getStackTrace())
                .anyMatch(elem -> elem.getClassName().contains("DroidGuard"));
    }

    public static void onEngineGetCertificateChain() {
        // Check stack for SafetyNet or Play Integrity
        if (isCallerSafetyNet() || sIsFinsky) {
            Log.i(TAG, "Blocked key attestation sIsGms=" + sIsGms + " sIsFinsky=" + sIsFinsky);
            throw new UnsupportedOperationException();
        }
    }

    public static void dlog(String msg) {
        if (DEBUG) Log.d(TAG, "[" + sProcessName + "] " + msg);
    }
}
