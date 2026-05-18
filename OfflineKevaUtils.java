//
// Decompiled by Jadx - 602ms
//
package com.ss.android.ugc.aweme.feed.cache.offlinemode;

import X.04nj;
import X.08ll;
import X.08lm;
import X.08ln;
import X.08lo;
import X.1S4G;
import com.bytedance.keva.Keva;
import com.bytedance.memoryx.StringBuilderCache;
import com.ss.android.agilelogger.ALog;
import com.ss.android.ugc.aweme.account.AccountProxyService;
import com.ss.android.ugc.aweme.experiment.OfflineModeLiteExp;
import com.ss.android.ugc.aweme.feed.cache.experiment.OfflineModeDownloadOptExp;
import com.ss.android.ugc.aweme.utils.GsonUtil;
import com.ss.android.ugc.bytex.kt_intermediate.lib.CheckNpeV2;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import kotlin.Lazy;
import kotlin.LazyKt__LazyJVMKt;
import kotlin.jvm.internal.AFLambdaS445S0000000_1;
import kotlin.jvm.internal.AFLambdaS471S0000000_25;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.StringsKt;

public final class OfflineKevaUtils {
    public static final Lazy LIZ = LazyKt__LazyJVMKt.lazy(AFLambdaS445S0000000_1.get$arr$(455));
    public static final Lazy LIZIZ = LazyKt__LazyJVMKt.lazy(AFLambdaS471S0000000_25.get$arr$(74));

    public static boolean LJIIIZ() {
        return ((Keva) LIZIZ.getValue()).getBoolean("offline_off_first_usr", true);
    }

    public static Keva LJIIJJI() {
        return (Keva) LIZ.getValue();
    }

    public static int LJIILIIL() {
        int i;
        if (04nj.LIZ) {
            i = 0;
        } else {
            i = 100;
        }
        return LJIIJJI().getInt("true_cache_count", i);
    }

    public static void LJIL(int i) {
        1S4G r0 = 1S4G.LIZIZ;
        StringBuilder sb = StringBuilderCache.get();
        sb.append("set cache btn status: ");
        sb.append(i);
        r0.debug("OfflineKevaUtils", StringBuilderCache.release(sb));
        LJIIJJI().storeInt("cache_btn_status", i);
    }

    /* JADX WARN: Removed duplicated region for block: B:11:? A[RETURN, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:5:0x001c A[Catch: Exception -> 0x0039, TRY_LEAVE, TryCatch #0 {Exception -> 0x0039, blocks: (B:17:0x0013, B:5:0x001c), top: B:16:0x0013 }] */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0031  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static Map LIZ() {
        boolean z;
        Object obj;
        Map map;
        Keva LJIIJJI = LJIIJJI();
        Intrinsics.checkNotNullExpressionValue(LJIIJJI, "");
        String string = LJIIJJI.getString("offline_active_download_aids", "");
        if (string != null) {
            try {
            } catch (Exception e) {
                StringBuilder sb = StringBuilderCache.get();
                sb.append("can not read or parse data to ");
                sb.append(Map.class.getSimpleName());
                sb.append(", content=");
                sb.append(string);
                ALog.w("KevaStoreGsonObjectError", StringBuilderCache.release(sb), e);
            }
            if (!StringsKt.isBlank(string)) {
                z = false;
                if (!z) {
                    obj = GsonUtil.getGson().fromJson(string, new 08ll().getType());
                    map = (Map) obj;
                    if (map == null) {
                        return new LinkedHashMap();
                    }
                    return map;
                }
                obj = null;
                map = (Map) obj;
                if (map == null) {
                }
            }
        }
        z = true;
        if (!z) {
        }
        obj = null;
        map = (Map) obj;
        if (map == null) {
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:11:? A[RETURN, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:5:0x001c A[Catch: Exception -> 0x0039, TRY_LEAVE, TryCatch #0 {Exception -> 0x0039, blocks: (B:17:0x0013, B:5:0x001c), top: B:16:0x0013 }] */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0031  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static Map LIZIZ() {
        boolean z;
        Object obj;
        Map map;
        Keva LJIIJJI = LJIIJJI();
        Intrinsics.checkNotNullExpressionValue(LJIIJJI, "");
        String string = LJIIJJI.getString("offline_active_download_failed_aids", "");
        if (string != null) {
            try {
            } catch (Exception e) {
                StringBuilder sb = StringBuilderCache.get();
                sb.append("can not read or parse data to ");
                sb.append(Map.class.getSimpleName());
                sb.append(", content=");
                sb.append(string);
                ALog.w("KevaStoreGsonObjectError", StringBuilderCache.release(sb), e);
            }
            if (!StringsKt.isBlank(string)) {
                z = false;
                if (!z) {
                    obj = GsonUtil.getGson().fromJson(string, new 08lm().getType());
                    map = (Map) obj;
                    if (map == null) {
                        return new LinkedHashMap();
                    }
                    return map;
                }
                obj = null;
                map = (Map) obj;
                if (map == null) {
                }
            }
        }
        z = true;
        if (!z) {
        }
        obj = null;
        map = (Map) obj;
        if (map == null) {
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:11:? A[RETURN, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:5:0x001c A[Catch: Exception -> 0x0039, TRY_LEAVE, TryCatch #0 {Exception -> 0x0039, blocks: (B:17:0x0013, B:5:0x001c), top: B:16:0x0013 }] */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0031  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static Map LIZJ() {
        boolean z;
        Object obj;
        Map map;
        Keva LJIIJJI = LJIIJJI();
        Intrinsics.checkNotNullExpressionValue(LJIIJJI, "");
        String string = LJIIJJI.getString("offline_active_download_played_aids", "");
        if (string != null) {
            try {
            } catch (Exception e) {
                StringBuilder sb = StringBuilderCache.get();
                sb.append("can not read or parse data to ");
                sb.append(Map.class.getSimpleName());
                sb.append(", content=");
                sb.append(string);
                ALog.w("KevaStoreGsonObjectError", StringBuilderCache.release(sb), e);
            }
            if (!StringsKt.isBlank(string)) {
                z = false;
                if (!z) {
                    obj = GsonUtil.getGson().fromJson(string, new 08ln().getType());
                    map = (Map) obj;
                    if (map == null) {
                        return new LinkedHashMap();
                    }
                    return map;
                }
                obj = null;
                map = (Map) obj;
                if (map == null) {
                }
            }
        }
        z = true;
        if (!z) {
        }
        obj = null;
        map = (Map) obj;
        if (map == null) {
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:11:? A[RETURN, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:5:0x001c A[Catch: Exception -> 0x0039, TRY_LEAVE, TryCatch #0 {Exception -> 0x0039, blocks: (B:17:0x0013, B:5:0x001c), top: B:16:0x0013 }] */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0031  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static Map LIZLLL() {
        boolean z;
        Object obj;
        Map map;
        Keva LJIIJJI = LJIIJJI();
        Intrinsics.checkNotNullExpressionValue(LJIIJJI, "");
        String string = LJIIJJI.getString("offline_active_download_played_aids_for_stat", "");
        if (string != null) {
            try {
            } catch (Exception e) {
                StringBuilder sb = StringBuilderCache.get();
                sb.append("can not read or parse data to ");
                sb.append(Map.class.getSimpleName());
                sb.append(", content=");
                sb.append(string);
                ALog.w("KevaStoreGsonObjectError", StringBuilderCache.release(sb), e);
            }
            if (!StringsKt.isBlank(string)) {
                z = false;
                if (!z) {
                    obj = GsonUtil.getGson().fromJson(string, new 08lo().getType());
                    map = (Map) obj;
                    if (map == null) {
                        return new LinkedHashMap();
                    }
                    return map;
                }
                obj = null;
                map = (Map) obj;
                if (map == null) {
                }
            }
        }
        z = true;
        if (!z) {
        }
        obj = null;
        map = (Map) obj;
        if (map == null) {
        }
    }

    public static int LJ() {
        boolean z;
        if (LJII().length() > 0) {
            z = true;
        } else {
            z = false;
        }
        if (z && !Intrinsics.areEqual(LJII(), AccountProxyService.userService().getCurUserId())) {
            LJIL(-1);
            return -1;
        }
        return LJIIJJI().getInt("cache_btn_status", -1);
    }

    public static int LJFF() {
        int i;
        int LJIILL = LJIILL();
        if (!OfflineModeDownloadOptExp.LIZ() ? LJIILL == 2 || LJIILL == 3 || OfflineModeLiteExp.LIZ() : OfflineModeLiteExp.LIZ()) {
            i = 50;
        } else {
            i = 100;
        }
        return LJIIJJI().getInt("cache_count", i);
    }

    public static long LJI() {
        return LJIIJJI().getLong("has_cache_bytes", 0L);
    }

    public static String LJII() {
        String string = LJIIJJI().getString("user_id", "");
        return string != null ? string : "";
    }

    public static int LJIIJ() {
        return LJIIJJI().getInt("local_video_count", 0);
    }

    public static Set LJIIL() {
        Set stringSet = LJIIJJI().getStringSet("played_aids_for_replace", new LinkedHashSet());
        if (stringSet == null) {
            return new LinkedHashSet();
        }
        return stringSet;
    }

    public static boolean LJIILJJIL() {
        Keva LJIIJJI = LJIIJJI();
        StringBuilder sb = StringBuilderCache.get();
        sb.append("offline_use_data_dialog_checkbox");
        sb.append(AccountProxyService.userService().getCurUserId());
        return LJIIJJI.getBoolean(StringBuilderCache.release(sb), false);
    }

    public static int LJIILL() {
        return LJIIJJI().getInt("user_enable", 0);
    }

    public static void LJJ(int i) {
        LJIIJJI().storeInt("cache_count", i);
    }

    public static void LJJIFFI(boolean z) {
        LJIIJJI().storeBoolean("offline_auto_clean_cache", z);
    }

    public static void LJJII(boolean z) {
        LJIIJJI().storeBoolean("offline_warning_snackbar_showed", z);
    }

    public static void LJJIII(int i) {
        LJIIJJI().storeInt("true_cache_count", i);
    }

    public static void LJJIIJ(int i) {
        LJIIJJI().storeInt("user_enable", i);
    }

    public static boolean LJIIIIZZ(String str) {
        CheckNpeV2.throwNpe1(str, 0);
        return LJIIJJI().getBoolean(str, false);
    }

    public static long LJIILLIIL(String str) {
        CheckNpeV2.throwNpe1(str, 0);
        Keva LJIIJJI = LJIIJJI();
        StringBuilder sb = StringBuilderCache.get();
        sb.append("offline_active_start_time_aid");
        sb.append(AccountProxyService.userService().getCurUserId());
        sb.append(str);
        return LJIIJJI.getLong(StringBuilderCache.release(sb), -1L);
    }

    public static void LJIIZILJ(String str) {
        CheckNpeV2.throwNpe1(str, 0);
        if (LJIILLIIL(str) != -1) {
            Keva LJIIJJI = LJIIJJI();
            StringBuilder sb = StringBuilderCache.get();
            sb.append("offline_active_start_time_aid");
            sb.append(AccountProxyService.userService().getCurUserId());
            sb.append(str);
            LJIIJJI.erase(StringBuilderCache.release(sb));
        }
    }

    public static void LJIJ(Map map) {
        CheckNpeV2.throwNpe1(map, 0);
        Keva LJIIJJI = LJIIJJI();
        Intrinsics.checkNotNullExpressionValue(LJIIJJI, "");
        try {
            LJIIJJI.storeString("offline_active_download_aids", GsonUtil.toJson(map));
        } catch (Exception e) {
            StringBuilder sb = StringBuilderCache.get();
            sb.append("can not read or parse data to ");
            sb.append(Map.class.getSimpleName());
            sb.append(", content=");
            sb.append(map);
            ALog.w("KevaStoreGsonObjectError", StringBuilderCache.release(sb), e);
        }
    }

    public static void LJIJI(Map map) {
        CheckNpeV2.throwNpe1(map, 0);
        Keva LJIIJJI = LJIIJJI();
        Intrinsics.checkNotNullExpressionValue(LJIIJJI, "");
        try {
            LJIIJJI.storeString("offline_active_download_failed_aids", GsonUtil.toJson(map));
        } catch (Exception e) {
            StringBuilder sb = StringBuilderCache.get();
            sb.append("can not read or parse data to ");
            sb.append(Map.class.getSimpleName());
            sb.append(", content=");
            sb.append(map);
            ALog.w("KevaStoreGsonObjectError", StringBuilderCache.release(sb), e);
        }
    }

    public static void LJIJJ(Map map) {
        CheckNpeV2.throwNpe1(map, 0);
        Keva LJIIJJI = LJIIJJI();
        Intrinsics.checkNotNullExpressionValue(LJIIJJI, "");
        try {
            LJIIJJI.storeString("offline_active_download_played_aids", GsonUtil.toJson(map));
        } catch (Exception e) {
            StringBuilder sb = StringBuilderCache.get();
            sb.append("can not read or parse data to ");
            sb.append(Map.class.getSimpleName());
            sb.append(", content=");
            sb.append(map);
            ALog.w("KevaStoreGsonObjectError", StringBuilderCache.release(sb), e);
        }
    }

    public static void LJIJJLI(Map map) {
        CheckNpeV2.throwNpe1(map, 0);
        Keva LJIIJJI = LJIIJJI();
        Intrinsics.checkNotNullExpressionValue(LJIIJJI, "");
        try {
            LJIIJJI.storeString("offline_active_download_played_aids_for_stat", GsonUtil.toJson(map));
        } catch (Exception e) {
            StringBuilder sb = StringBuilderCache.get();
            sb.append("can not read or parse data to ");
            sb.append(Map.class.getSimpleName());
            sb.append(", content=");
            sb.append(map);
            ALog.w("KevaStoreGsonObjectError", StringBuilderCache.release(sb), e);
        }
    }

    public static void LJJI(String str) {
        CheckNpeV2.throwNpe1(str, 0);
        LJIIJJI().storeString("user_id", str);
    }

    public static void LJJIIJZLJL(String str) {
        CheckNpeV2.throwNpe1(str, 0);
        Keva LJIIJJI = LJIIJJI();
        StringBuilder sb = StringBuilderCache.get();
        sb.append("offline_active_start_time_aid");
        sb.append(AccountProxyService.userService().getCurUserId());
        sb.append(str);
        LJIIJJI.storeLong(StringBuilderCache.release(sb), System.currentTimeMillis());
    }
}
