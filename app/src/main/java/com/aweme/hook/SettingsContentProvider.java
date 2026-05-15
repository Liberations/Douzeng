package com.aweme.hook;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class SettingsContentProvider extends ContentProvider {
    private static final String TAG = "SettingsContentProvider";
    private static final String PREF_NAME = "OfflineCacheSettings";
    private static final String KEY_CACHE_COUNT = "cache_count";

    public static final String AUTHORITY = "com.aweme.hook.settings";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/settings");

    @Override
    public boolean onCreate() {
        Log.d(TAG, "ContentProvider onCreate");
        // 使SharedPreferences可被其他应用读取
        SharedPreferences prefs = getContext().getSharedPreferences(PREF_NAME, 0);
        prefs.getAll(); // 初始化
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        try {
            SharedPreferences prefs = getContext().getSharedPreferences(PREF_NAME, 0);
            prefs.getAll(); // 重新加载
            
            int cacheCount = prefs.getInt(KEY_CACHE_COUNT, 1000);
            Log.d(TAG, "query: 返回缓存数量 " + cacheCount);
            
            MatrixCursor cursor = new MatrixCursor(new String[]{KEY_CACHE_COUNT, "value"});
            cursor.addRow(new Object[]{KEY_CACHE_COUNT, cacheCount});
            return cursor;
        } catch (Exception e) {
            Log.e(TAG, "query failed", e);
            return new MatrixCursor(new String[]{KEY_CACHE_COUNT, "value"});
        }
    }

    @Override
    public String getType(Uri uri) {
        return "vnd.android.cursor.item/vnd." + AUTHORITY + ".settings";
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        try {
            if ("getCacheCount".equals(method)) {
                SharedPreferences prefs = getContext().getSharedPreferences(PREF_NAME, 0);
                prefs.getAll(); // 重新加载
                int cacheCount = prefs.getInt(KEY_CACHE_COUNT, 1000);
                Log.d(TAG, "call method: 返回缓存数量 " + cacheCount);
                Bundle result = new Bundle();
                result.putInt(KEY_CACHE_COUNT, cacheCount);
                return result;
            }
        } catch (Exception e) {
            Log.e(TAG, "call failed", e);
        }
        return null;
    }
}
