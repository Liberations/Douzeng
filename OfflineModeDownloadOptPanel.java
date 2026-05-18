//
// Decompiled by Jadx - 802ms
//
package com.ss.android.ugc.aweme.feed.cache.offlinemode;

import X.03EM;
import X.04nj;
import X.0bOs;
import X.0zVS;
import X.14zq;
import X.1KWD;
import X.1KWE;
import X.1NPF;
import X.1NPH;
import X.1NPI;
import X.1NPJ;
import X.1NPK;
import X.1NPL;
import X.1NPM;
import X.1NPP;
import X.1S4G;
import Y.ACListenerS135S0100000_37;
import Y.ACListenerS149S0100000_51;
import Y.AObserverS357S0100000_37;
import Y.AObserverS369S0100000_51;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;
import com.bytedance.dux.dialog.alert.DuxAlertDialogBuilder;
import com.bytedance.dux.duxswitch.DuxSwitch;
import com.bytedance.dux.radio.DuxRadio;
import com.bytedance.dux.toast.DuxToastLocation;
import com.bytedance.dux.toast.DuxToastV2;
import com.bytedance.memoryx.StringBuilderCache;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.ss.android.ugc.aweme.arch.widgets.base.NextLiveData;
import com.ss.android.ugc.aweme.base.utils.ThreadUtils;
import com.ss.android.ugc.aweme.detail.extensions.IDetailFeedContext;
import com.ss.android.ugc.aweme.feed.cache.experiment.OfflineModeDownloadOptExp;
import com.ss.android.ugc.aweme.feed.model.Aweme;
import com.ss.android.ugc.aweme.feed.param.FeedParam;
import com.ss.android.ugc.aweme.feed.ui.masklayer2.layout.CircleProgressView;
import com.ss.android.ugc.aweme.utils.DeviceInfo;
import com.ss.android.ugc.aweme.utils.NetworkUtils;
import com.ss.android.ugc.bytex.kt_intermediate.lib.CheckNpeV2;
import java.util.Arrays;
import kotlin.Lazy;
import kotlin.LazyKt__LazyJVMKt;
import kotlin.NoWhenBranchMatchedException;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.AFLambdaS497S0000000_51;
import kotlin.jvm.internal.AFLambdaS616S0000000_49;
import kotlin.jvm.internal.ALambdaS1161S0100000_49;
import kotlin.jvm.internal.ALambdaS143S0210000_51;
import kotlin.jvm.internal.ALambdaS170S0110000_51;
import kotlin.jvm.internal.ALambdaS830S0200000_49;
import kotlin.jvm.internal.Intrinsics;
import kotlin.math.MathKt__MathJVMKt;
import kotlin.ranges.RangesKt;
import t.TimonTransmit;

public final class OfflineModeDownloadOptPanel extends BottomSheetDialogFragment {
    public static final 1NPM Companion = new 1NPM();
    public DuxSwitch autoSwitch;
    public TextView clearCacheView;
    public View count100;
    public DuxRadio count100Radio;
    public View count150;
    public DuxRadio count150Radio;
    public View count200;
    public DuxRadio count200Radio;
    public View count50;
    public DuxRadio count50Radio;
    public OfflineModeDownloadOptEmptyPanel emptyPanel;
    public IDetailFeedContext<FeedParam> feedDetailContext;
    public TextView infoCountView;
    public TextView infoDescView;
    public boolean isScene1Layout;
    public TextView memoryInfoView;
    public PauseReason pauseReason;
    public Runnable pendingStartDownload;
    public View progressActionContainer;
    public TextView progressActionView;
    public View progressCard;
    public TextView progressDescView;
    public TextView progressValueView;
    public View selectCard;
    public 1NPF settingDialogVM;
    public TextView updateBtn;
    public View updateContainer;
    public View updateProgressContainer;
    public View updateProgressPause;
    public View updateProgressPlay;
    public CircleProgressView updateProgressView;
    public final String TAG = "OfflineModeDownloadOpt";
    public final long lowStorageThresholdBytes = 0x1f400000;
    public final Lazy cacheLoader$delegate = LazyKt__LazyJVMKt.lazy(AFLambdaS497S0000000_51.get$arr$(154));

    public final boolean isDialogShowing(OfflineModeDownloadOptPanel offlineModeDownloadOptPanel) {
        CheckNpeV2.throwNpe1(offlineModeDownloadOptPanel, 0);
        Dialog dialog = offlineModeDownloadOptPanel.getDialog();
        return dialog != null && dialog.isShowing();
    }

    private final String getPauseDescText() {
        int i;
        PauseReason pauseReason = this.pauseReason;
        if (pauseReason == null) {
            pauseReason = inferPauseReason();
        }
        if (pauseReason != null && (i = 1NPH.LIZ[pauseReason.ordinal()]) != -1) {
            if (i != 1) {
                if (i == 2) {
                    return "空间不足，缓存已暂停";
                }
                throw new NoWhenBranchMatchedException();
            }
            return "网络变化，缓存已暂停";
        }
        return "缓存已暂停";
    }

    private final void showAdjustCacheCountDialog(int i) {
        FragmentActivity activity;
        IDetailFeedContext<FeedParam> iDetailFeedContext = this.feedDetailContext;
        if (iDetailFeedContext != null && (activity = iDetailFeedContext.getActivity()) != null) {
            DuxAlertDialogBuilder duxAlertDialogBuilder = new DuxAlertDialogBuilder(activity);
            duxAlertDialogBuilder.title("调整缓存上限");
            StringBuilder sb = StringBuilderCache.get();
            sb.append("将清空已有缓存，并将缓存下载上限调整至 ");
            sb.append(i);
            sb.append(" 条");
            duxAlertDialogBuilder.message(StringBuilderCache.release(sb));
            duxAlertDialogBuilder.negativeButton("取消", new showAdjustCacheCountDialog.1.1(i));
            duxAlertDialogBuilder.positiveButton("确定", new showAdjustCacheCountDialog.1.2(i, this));
            duxAlertDialogBuilder.show();
        }
    }

    public final void applyWifiAutoCacheState(boolean z) {
        1NPF r0 = this.settingDialogVM;
        if (r0 != null) {
            r0.SL1(z);
        }
        14zq.LIZIZ(z);
        1KWD.LIZ.getClass();
    }

    public final void flowChangeToPauseView() {
        this.pauseReason = PauseReason.NETWORK_CHANGE;
        getCacheLoader().LJIIIZ(4);
        1NPF r0 = this.settingDialogVM;
        if (r0 != null) {
            r0.RL1(4);
        }
    }

    public final 1NPP getCacheLoader() {
        return (1NPP) this.cacheLoader$delegate.getValue();
    }

    public final 1NPF getSettingDialogVM() {
        return this.settingDialogVM;
    }

    public final void handleCacheCountClick(int i) {
        int LJFF;
        MutableLiveData mutableLiveData;
        Integer num;
        1NPF r0 = this.settingDialogVM;
        if (r0 != null && (mutableLiveData = r0.e) != null && (num = (Integer) mutableLiveData.getValue()) != null) {
            LJFF = num.intValue();
        } else {
            LJFF = OfflineKevaUtils.LJFF();
        }
        if (LJFF == i) {
            OfflineKevaUtils.LJJ(i);
        } else {
            showAdjustCacheCountDialog(i);
        }
    }

    public final PauseReason inferPauseReason() {
        FragmentActivity activity;
        MutableLiveData mutableLiveData;
        Integer num;
        1NPF r0 = this.settingDialogVM;
        if (r0 != null && (mutableLiveData = r0.g) != null && (num = (Integer) mutableLiveData.getValue()) != null && (num.intValue() == 2 || num.intValue() == 3)) {
            return PauseReason.NETWORK_CHANGE;
        }
        IDetailFeedContext<FeedParam> iDetailFeedContext = this.feedDetailContext;
        if (iDetailFeedContext != null && (activity = iDetailFeedContext.getActivity()) != null) {
            if (!NetworkUtils.isNetworkAvailable(activity)) {
                return PauseReason.NETWORK_CHANGE;
            }
            if (!NetworkUtils.LJFF(activity) && !OfflineKevaUtils.LJIIJJI().getBoolean("offline_allow_use_data", false)) {
                return PauseReason.NETWORK_CHANGE;
            }
        }
        if (DeviceInfo.getAvailableInternalStorageSize() < this.lowStorageThresholdBytes) {
            return PauseReason.STORAGE_NOT_ENOUGH;
        }
        return null;
    }

    public void onDestroyView() {
        Runnable runnable = this.pendingStartDownload;
        if (runnable != null) {
            ThreadUtils.LIZ(runnable);
        }
        this.pendingStartDownload = null;
        super/*androidx.fragment.app.DialogFragment*/.onDestroyView();
    }

    public final void setBtnClearView(boolean z) {
        int i;
        TextView textView;
        if (z) {
            i = 0x7f060ce7;
        } else {
            i = 0x7f060fe3;
        }
        Context context = getContext();
        if (context != null && (textView = this.clearCacheView) != null) {
            textView.setTextColor(0bOs.LJJLL(i, context));
        }
        TextView textView2 = this.clearCacheView;
        if (textView2 != null) {
            textView2.setClickable(z);
        }
    }

    public final void setSettingDialogVM(1NPF r1) {
        this.settingDialogVM = r1;
    }

    public final void showAutoCacheCloseDialog(Function0<Unit> function0) {
        FragmentActivity activity;
        IDetailFeedContext<FeedParam> iDetailFeedContext = this.feedDetailContext;
        if (iDetailFeedContext != null && (activity = iDetailFeedContext.getActivity()) != null) {
            DuxAlertDialogBuilder duxAlertDialogBuilder = new DuxAlertDialogBuilder(activity);
            duxAlertDialogBuilder.title("关闭自动缓存");
            duxAlertDialogBuilder.message("关闭后会删除所有已下载视频");
            duxAlertDialogBuilder.negativeButton("取消", new ALambdaS1161S0100000_49(function0, 13));
            duxAlertDialogBuilder.positiveButton("确认", new ALambdaS830S0200000_49(activity, this, 3));
            duxAlertDialogBuilder.show();
        }
    }

    public final void showCacheEmptyPanel() {
        FragmentActivity activity;
        IDetailFeedContext<FeedParam> iDetailFeedContext;
        0zVS feedContextNullable;
        Fragment fragmentP;
        FragmentManager childFragmentManager;
        OfflineModeDownloadOptEmptyPanel offlineModeDownloadOptEmptyPanel;
        OfflineModeDownloadOptEmptyPanel offlineModeDownloadOptEmptyPanel2;
        OfflineModeDownloadOptEmptyPanel offlineModeDownloadOptEmptyPanel3;
        IDetailFeedContext<FeedParam> iDetailFeedContext2 = this.feedDetailContext;
        if (iDetailFeedContext2 != null && (activity = iDetailFeedContext2.getActivity()) != null && !activity.isFinishing() && (iDetailFeedContext = this.feedDetailContext) != null && (feedContextNullable = iDetailFeedContext.getFeedContextNullable()) != null && (fragmentP = feedContextNullable.getFragmentP()) != null && (childFragmentManager = fragmentP.getChildFragmentManager()) != null) {
            if (this.emptyPanel == null) {
                OfflineModeDownloadOptEmptyPanel findFragmentByTag = childFragmentManager.findFragmentByTag("offline_mode_opt_empty_panel");
                if (findFragmentByTag instanceof OfflineModeDownloadOptEmptyPanel) {
                    offlineModeDownloadOptEmptyPanel2 = findFragmentByTag;
                } else {
                    offlineModeDownloadOptEmptyPanel2 = null;
                }
                this.emptyPanel = offlineModeDownloadOptEmptyPanel2;
                if (offlineModeDownloadOptEmptyPanel2 == null) {
                    OfflineModeDownloadOptEmptyPanel.Companion.getClass();
                    this.emptyPanel = new OfflineModeDownloadOptEmptyPanel();
                }
                IDetailFeedContext<FeedParam> iDetailFeedContext3 = this.feedDetailContext;
                if (iDetailFeedContext3 != null && (offlineModeDownloadOptEmptyPanel3 = this.emptyPanel) != null) {
                    offlineModeDownloadOptEmptyPanel3.setFeedDetailContext(iDetailFeedContext3);
                }
            }
            OfflineModeDownloadOptEmptyPanel offlineModeDownloadOptEmptyPanel4 = this.emptyPanel;
            if (offlineModeDownloadOptEmptyPanel4 == null || !offlineModeDownloadOptEmptyPanel4.isDialogShowing(offlineModeDownloadOptEmptyPanel4)) {
                dismiss();
                try {
                    OfflineModeDownloadOptEmptyPanel offlineModeDownloadOptEmptyPanel5 = this.emptyPanel;
                    if ((offlineModeDownloadOptEmptyPanel5 == null || !offlineModeDownloadOptEmptyPanel5.isAdded()) && (offlineModeDownloadOptEmptyPanel = this.emptyPanel) != null) {
                        offlineModeDownloadOptEmptyPanel.showNow(childFragmentManager, "offline_mode_opt_empty_panel");
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    public final void showClearDialog() {
        FragmentActivity activity;
        IDetailFeedContext<FeedParam> iDetailFeedContext = this.feedDetailContext;
        if (iDetailFeedContext != null && (activity = iDetailFeedContext.getActivity()) != null) {
            DuxAlertDialogBuilder duxAlertDialogBuilder = new DuxAlertDialogBuilder(activity);
            duxAlertDialogBuilder.title("清空自动缓存");
            duxAlertDialogBuilder.message("删除所有已下载视频并关闭自动缓存功能");
            duxAlertDialogBuilder.negativeButton("取消", AFLambdaS616S0000000_49.get$arr$(0));
            duxAlertDialogBuilder.positiveButton("清空", new ALambdaS830S0200000_49(activity, this, 4));
            duxAlertDialogBuilder.show();
        }
    }

    public final void showNotWifiAlert(boolean z) {
        FragmentActivity activity;
        IDetailFeedContext<FeedParam> iDetailFeedContext = this.feedDetailContext;
        if (iDetailFeedContext != null && (activity = iDetailFeedContext.getActivity()) != null) {
            OfflineModeDataUsageDialog.LIZJ(activity, new ALambdaS170S0110000_51(z, this, 0), new ALambdaS170S0110000_51(z, this, 1), 2);
        }
    }

    public final void showUpdateDialog() {
        FragmentActivity activity;
        IDetailFeedContext<FeedParam> iDetailFeedContext = this.feedDetailContext;
        if (iDetailFeedContext != null && (activity = iDetailFeedContext.getActivity()) != null) {
            DuxAlertDialogBuilder duxAlertDialogBuilder = new DuxAlertDialogBuilder(activity);
            duxAlertDialogBuilder.title("更新视频内容");
            duxAlertDialogBuilder.message("将清空已有缓存，重新下载新视频");
            duxAlertDialogBuilder.negativeButton("取消", AFLambdaS616S0000000_49.get$arr$(1));
            duxAlertDialogBuilder.positiveButton("更新", new ALambdaS1161S0100000_49(this, 14));
            duxAlertDialogBuilder.show();
        }
    }

    public final void updateCacheCountSelection(int i) {
        TextView textView;
        TextView textView2;
        TextView textView3;
        TextView textView4;
        if (this.isScene1Layout) {
            DuxRadio duxRadio = this.count50Radio;
            if (duxRadio != null) {
                duxRadio.setChecked(i == 50);
            }
            DuxRadio duxRadio2 = this.count100Radio;
            if (duxRadio2 != null) {
                duxRadio2.setChecked(i == 100);
            }
            DuxRadio duxRadio3 = this.count150Radio;
            if (duxRadio3 != null) {
                duxRadio3.setChecked(i == 150);
            }
            DuxRadio duxRadio4 = this.count200Radio;
            if (duxRadio4 != null) {
                duxRadio4.setChecked(i == 200);
                return;
            }
            return;
        }
        Context context = getContext();
        if (context != null) {
            int LJJLL = 0bOs.LJJLL(0x7f0609a7, context);
            int LJJLL2 = 0bOs.LJJLL(0x7f060fe3, context);
            View view = this.count50;
            if ((view instanceof TextView) && (textView4 = (TextView) view) != null) {
                textView4.setTextColor(i == 50 ? LJJLL : LJJLL2);
            }
            View view2 = this.count100;
            if ((view2 instanceof TextView) && (textView3 = (TextView) view2) != null) {
                textView3.setTextColor(i == 100 ? LJJLL : LJJLL2);
            }
            View view3 = this.count150;
            if ((view3 instanceof TextView) && (textView2 = (TextView) view3) != null) {
                textView2.setTextColor(i == 150 ? LJJLL : LJJLL2);
            }
            View view4 = this.count200;
            if ((view4 instanceof TextView) && (textView = (TextView) view4) != null) {
                if (i == 200) {
                    LJJLL2 = LJJLL;
                }
                textView.setTextColor(LJJLL2);
            }
        }
    }

    public final void adjustBottomSheetToContentHeight() {
        AppCompatDialog appCompatDialog;
        View findViewById;
        AppCompatDialog dialog = getDialog();
        if ((dialog instanceof BottomSheetDialog) && (appCompatDialog = dialog) != null && (findViewById = appCompatDialog.findViewById(0x7f0a21a3)) != null) {
            ViewGroup.LayoutParams layoutParams = findViewById.getLayoutParams();
            if (layoutParams == null) {
                layoutParams = null;
            } else {
                layoutParams.height = -2;
            }
            0bOs.LLLLZLLLI(findViewById, layoutParams);
            BottomSheetBehavior from = BottomSheetBehavior.from(findViewById);
            from.setFitToContents(true);
            from.setSkipCollapsed(true);
            from.setState(3);
        }
    }

    public final void clearCacheAndReset(boolean z, Function0<Unit> function0) {
        this.pauseReason = null;
        getCacheLoader().LJIJJLI(false);
        getCacheLoader().LJIILLIIL();
        getCacheLoader().LIZJ(false);
        1KWD.LIZ.getClass();
        1KWD.LIZJ(1);
        1KWD.LJIILL(OfflineKevaUtils.LJIIJ(), (Aweme) null, "offline_mode", "manual_clear");
        1KWE.LIZJ(1KWE.LIZ, false, new ALambdaS143S0210000_51(this, function0, z, 2), 1);
    }

    public void onCreate(Bundle bundle) {
        1NPF r0;
        FragmentActivity activity;
        super/*androidx.fragment.app.DialogFragment*/.onCreate(bundle);
        setStyle(1, 0x7f12093a);
        IDetailFeedContext<FeedParam> iDetailFeedContext = this.feedDetailContext;
        if (iDetailFeedContext != null && (activity = iDetailFeedContext.getActivity()) != null) {
            r0 = (1NPF) ViewModelProviders.of(activity).get(1NPF.class);
        } else {
            r0 = null;
        }
        this.settingDialogVM = r0;
    }

    public Dialog onCreateDialog(Bundle bundle) {
        Dialog onCreateDialog = super.onCreateDialog(bundle);
        Intrinsics.checkNotNullExpressionValue(onCreateDialog, "");
        if (onCreateDialog instanceof BottomSheetDialog) {
            0bOs.LJJJJZI(onCreateDialog, new 1NPL(this));
        }
        return onCreateDialog;
    }

    public void onStart() {
        super/*androidx.fragment.app.DialogFragment*/.onStart();
        adjustBottomSheetToContentHeight();
    }

    public final void updateCacheCount(int i) {
        int i2;
        OfflineKevaUtils.LJJ(i);
        getCacheLoader().LJJIII(i);
        1NPF r0 = this.settingDialogVM;
        if (r0 != null) {
            r0.QL1(i);
        }
        1KWD.LIZ.getClass();
        1KWD.LJJIFFI = 1KWD.LJJI;
        if (i != 100) {
            if (i != 150) {
                if (i != 200) {
                    i2 = 1KWD.LJJI;
                } else {
                    i2 = 3;
                }
            } else {
                i2 = 2;
            }
        } else {
            i2 = 1;
        }
        1KWD.LJJI = i2;
    }

    /* JADX WARN: Removed duplicated region for block: B:16:0x0044  */
    /* JADX WARN: Removed duplicated region for block: B:72:0x00eb  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final void updateLayoutView() {
        int i;
        boolean z;
        boolean z2;
        String release;
        int i2;
        int i3;
        TextView textView;
        String str;
        String str2;
        int LJIIJ = OfflineKevaUtils.LJIIJ();
        int LJFF = OfflineKevaUtils.LJFF();
        if (LJFF > 0) {
            int coerceIn = RangesKt.coerceIn((LJIIJ * 100) / LJFF, 0, 100);
            if (LJFF <= LJIIJ && getCacheLoader().LJIILL() != 3) {
                getCacheLoader().LJIIIZ(3);
                1NPF r3 = this.settingDialogVM;
                if (r3 != null) {
                    r3.RL1(3);
                    i = coerceIn;
                }
            }
            i = coerceIn;
        } else {
            i = 0;
        }
        int LJIILL = getCacheLoader().LJIILL();
        if (LJIILL == 2) {
            z = true;
        } else {
            if (LJIILL == 4) {
                z = false;
                z2 = true;
                if (!this.isScene1Layout) {
                    boolean z3 = z || z2;
                    View view = this.selectCard;
                    if (view != null) {
                        view.setVisibility(z3 ? 8 : 0);
                    }
                    View view2 = this.progressCard;
                    if (view2 != null) {
                        view2.setVisibility(z3 ? 0 : 8);
                    }
                    View view3 = this.progressCard;
                    if (view3 != null) {
                        TimonTransmit.android_view_View_post(view3, new 1NPK(this));
                    }
                    if (z3) {
                        CircleProgressView circleProgressView = this.updateProgressView;
                        if (circleProgressView != null) {
                            circleProgressView.setProgress(i / 100.0d);
                        }
                        TextView textView2 = this.progressValueView;
                        if (textView2 != null) {
                            StringBuilder sb = StringBuilderCache.get();
                            sb.append(i);
                            sb.append('%');
                            textView2.setText(StringBuilderCache.release(sb));
                        }
                        TextView textView3 = this.progressDescView;
                        if (textView3 != null) {
                            if (z2) {
                                str2 = getPauseDescText();
                            } else {
                                str2 = "缓存更新中，退出页面不会中断缓存";
                            }
                            textView3.setText(str2);
                        }
                        TextView textView4 = this.progressActionView;
                        if (textView4 != null) {
                            if (z2) {
                                str = "继续缓存";
                            } else {
                                str = "暂停缓存";
                            }
                            textView4.setText(str);
                        }
                        if (z2) {
                            i2 = 0x7f0816a9;
                        } else {
                            i2 = 0x7f0816a5;
                        }
                        TextView textView5 = this.progressActionView;
                        if (textView5 != null) {
                            textView5.setBackgroundResource(i2);
                        }
                        if (z2) {
                            i3 = 0x7f061d72;
                        } else {
                            i3 = 0x7f0601ae;
                        }
                        Context context = getContext();
                        if (context != null && (textView = this.progressActionView) != null) {
                            textView.setTextColor(0bOs.LJJLL(i3, context));
                        }
                    }
                    setBtnClearView(LJIIJ > 0);
                    return;
                }
                int coerceAtMost = RangesKt.coerceAtMost(RangesKt.coerceAtLeast(LJIIJ - OfflineKevaUtils.LJIIL().size(), 0), LJFF);
                TextView textView6 = this.infoCountView;
                if (textView6 != null) {
                    StringBuilder sb2 = StringBuilderCache.get();
                    sb2.append(coerceAtMost);
                    sb2.append(" 条");
                    textView6.setText(StringBuilderCache.release(sb2));
                }
                if (!z && !z2) {
                    TextView textView7 = this.updateBtn;
                    if (textView7 != null) {
                        textView7.setVisibility(0);
                    }
                    View view4 = this.updateProgressContainer;
                    if (view4 != null) {
                        view4.setVisibility(8);
                    }
                    View view5 = this.updateProgressPause;
                    if (view5 != null) {
                        view5.setVisibility(8);
                    }
                    View view6 = this.updateProgressPlay;
                    if (view6 != null) {
                        view6.setVisibility(8);
                    }
                    int roundToInt = LJIIJ == 0 ? 0 : MathKt__MathJVMKt.roundToInt(LJIIJ * 0.3f);
                    TextView textView8 = this.infoDescView;
                    if (textView8 != null) {
                        textView8.setTextSize(2, 12.0f);
                    }
                    TextView textView9 = this.infoDescView;
                    if (textView9 != null) {
                        StringBuilder sb3 = StringBuilderCache.get();
                        sb3.append("已缓存 ");
                        sb3.append(LJIIJ);
                        sb3.append(" 条，可播放约");
                        sb3.append(roundToInt);
                        sb3.append("分钟");
                        textView9.setText(StringBuilderCache.release(sb3));
                    }
                } else {
                    TextView textView10 = this.updateBtn;
                    if (textView10 != null) {
                        textView10.setVisibility(8);
                    }
                    View view7 = this.updateProgressContainer;
                    if (view7 != null) {
                        view7.setVisibility(0);
                    }
                    CircleProgressView circleProgressView2 = this.updateProgressView;
                    if (circleProgressView2 != null) {
                        circleProgressView2.setProgress(i / 100.0d);
                    }
                    View view8 = this.updateProgressPause;
                    if (view8 != null) {
                        view8.setVisibility(z ? 0 : 8);
                    }
                    View view9 = this.updateProgressPlay;
                    if (view9 != null) {
                        view9.setVisibility(z2 ? 0 : 8);
                    }
                    TextView textView11 = this.infoDescView;
                    if (textView11 != null) {
                        textView11.setTextSize(2, 13.0f);
                    }
                    TextView textView12 = this.infoDescView;
                    if (textView12 != null) {
                        if (z2) {
                            release = getPauseDescText();
                        } else {
                            StringBuilder sb4 = StringBuilderCache.get();
                            sb4.append("退出页面不会中断缓存，缓存更新中...（");
                            sb4.append(i);
                            sb4.append("%）");
                            release = StringBuilderCache.release(sb4);
                        }
                        textView12.setText(release);
                    }
                }
                setBtnClearView(LJIIJ > 0);
                return;
            }
            z = false;
        }
        z2 = false;
        if (!this.isScene1Layout) {
        }
    }

    public final void updateMemoryInfo(long j) {
        long j2 = j / 1048576;
        if (this.isScene1Layout) {
            int LJIIJ = OfflineKevaUtils.LJIIJ();
            TextView textView = this.memoryInfoView;
            if (textView != null) {
                StringBuilder sb = StringBuilderCache.get();
                sb.append("已缓存 ");
                sb.append(LJIIJ);
                sb.append(" 条视频，使用 ");
                sb.append(j2);
                sb.append("MB");
                textView.setText(StringBuilderCache.release(sb));
                return;
            }
            return;
        }
        double availableInternalStorageSize = DeviceInfo.getAvailableInternalStorageSize() / 1.073741824E9d;
        TextView textView2 = this.memoryInfoView;
        if (textView2 != null) {
            StringBuilder sb2 = StringBuilderCache.get();
            sb2.append("缓存使用 ");
            sb2.append(j2);
            sb2.append("MB / 可用空间 ");
            String format = String.format("%.1f", Arrays.copyOf(new Object[]{Double.valueOf(availableInternalStorageSize)}, 1));
            Intrinsics.checkNotNullExpressionValue(format, "");
            sb2.append(format);
            sb2.append("GB");
            textView2.setText(StringBuilderCache.release(sb2));
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static void clearCacheAndReset$default(OfflineModeDownloadOptPanel offlineModeDownloadOptPanel, boolean z, Function0 function0, int i, Object obj) {
        if ((i & 2) != 0) {
            function0 = null;
        }
        offlineModeDownloadOptPanel.clearCacheAndReset(z, function0);
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        int i;
        CheckNpeV2.throwNpe1(layoutInflater, 0);
        Lazy lazy = OfflineModeDownloadOptExp.LIZ;
        boolean booleanValue = ((Boolean) OfflineModeDownloadOptExp.LIZIZ.getValue()).booleanValue();
        this.isScene1Layout = booleanValue;
        if (booleanValue) {
            i = 0x7f0d0a02;
        } else {
            i = 0x7f0d0a03;
        }
        return 0bOs.LLLLZLLIL(i, layoutInflater, viewGroup, false);
    }

    public void onViewCreated(View view, Bundle bundle) {
        CheckNpeV2.throwNpe1(view, 0);
        super/*androidx.fragment.app.Fragment*/.onViewCreated(view, bundle);
        initView(view);
        TimonTransmit.android_view_View_post(view, new 1NPJ(this));
    }

    public final void setFeedDetailContext(IDetailFeedContext<FeedParam> iDetailFeedContext) {
        CheckNpeV2.throwNpe1(iDetailFeedContext, 0);
        this.feedDetailContext = iDetailFeedContext;
    }

    public final void cacheClickAction() {
        FragmentActivity activity;
        NextLiveData nextLiveData;
        getCacheLoader().LJIJJLI(true);
        OfflineKevaUtils.LJJIFFI(false);
        1NPF r0 = this.settingDialogVM;
        if (r0 != null && (nextLiveData = r0.i) != null) {
            nextLiveData.setValue(Boolean.FALSE);
        }
        int LJIILL = getCacheLoader().LJIILL();
        1S4G r1 = 1S4G.LIZIZ;
        String str = this.TAG;
        StringBuilder sb = StringBuilderCache.get();
        sb.append("cacheClickAction(), current cacheBtnStatus = ");
        sb.append(LJIILL);
        r1.debug(str, StringBuilderCache.release(sb));
        if (LJIILL != 1) {
            if (LJIILL != 2) {
                if (LJIILL != 4) {
                    int LJIIJ = OfflineKevaUtils.LJIIJ();
                    int LJFF = OfflineKevaUtils.LJFF();
                    if (04nj.LIZ && LJIILL == -1 && 1 <= LJIIJ && LJIIJ < LJFF) {
                        getCacheLoader().LJIIIZ(2);
                        1NPF r02 = this.settingDialogVM;
                        if (r02 != null) {
                            r02.RL1(2);
                        }
                        getCacheLoader().LJFF(false, false);
                    } else {
                        getCacheLoader().LJIIIZ(3);
                        1NPF r03 = this.settingDialogVM;
                        if (r03 != null) {
                            r03.RL1(3);
                        }
                        IDetailFeedContext<FeedParam> iDetailFeedContext = this.feedDetailContext;
                        if (iDetailFeedContext != null && (activity = iDetailFeedContext.getActivity()) != null) {
                            DuxToastV2 duxToastV2 = DuxToastV2.INSTANCE;
                            StringBuilder sb2 = StringBuilderCache.get();
                            sb2.append("缓存视频已修改为");
                            String format = String.format("%d", Arrays.copyOf(new Object[]{Integer.valueOf(OfflineKevaUtils.LJFF())}, 1));
                            Intrinsics.checkNotNullExpressionValue(format, "");
                            sb2.append(format);
                            sb2.append((char) 26465);
                            DuxToastV2.makeShowSystemToast$default(duxToastV2, activity, StringBuilderCache.release(sb2), (Drawable) null, (Drawable) null, 0, false, (DuxToastLocation) null, false, 252, (Object) null);
                        }
                    }
                    1KWD.LIZ.getClass();
                    1KWD.LJJII = 3;
                    1KWD.LJ();
                } else {
                    this.pauseReason = null;
                    getCacheLoader().LJIIIZ(2);
                    1NPF r04 = this.settingDialogVM;
                    if (r04 != null) {
                        r04.RL1(2);
                    }
                    getCacheLoader().LJJ();
                    1KWD.LIZ.getClass();
                    1KWD.LJI(1);
                }
            } else {
                this.pauseReason = null;
                getCacheLoader().LJIIIZ(4);
                1NPF r05 = this.settingDialogVM;
                if (r05 != null) {
                    r05.RL1(4);
                }
                getCacheLoader().LIZJ(false);
                1KWD.LJFF(1KWD.LIZ, 1);
            }
        } else {
            getCacheLoader().LJIIIZ(2);
            1NPF r06 = this.settingDialogVM;
            if (r06 != null) {
                r06.RL1(2);
            }
            getCacheLoader().LJFF(false, false);
            1KWD.LIZ.getClass();
            1KWD.LJ();
            1KWD.LJJIII = SystemClock.uptimeMillis();
        }
        1KWD r07 = 1KWD.LIZ;
        int LJFF2 = OfflineKevaUtils.LJFF();
        r07.getClass();
        1KWD.LJIJI(LJFF2, 1, "offline_mode_management_page");
        if (03EM.LIZ()) {
            1KWD.LJII("offline_list_page");
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:11:0x002e  */
    /* JADX WARN: Removed duplicated region for block: B:16:0x005d  */
    /* JADX WARN: Removed duplicated region for block: B:37:0x0096  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final void handleProgressActionClick() {
        String str;
        FragmentActivity fragmentActivity;
        int LJIILL = getCacheLoader().LJIILL();
        int i = (LJIILL == 1 && getCacheLoader().LIZLLL()) ? 2 : LJIILL;
        if (i != 2) {
            if (i == 4) {
                str = "cache_resume";
            }
            if (i != LJIILL) {
                getCacheLoader().LJIIIZ(i);
                1NPF r0 = this.settingDialogVM;
                if (r0 != null) {
                    r0.RL1(i);
                }
            }
            1S4G r02 = 1S4G.LIZIZ;
            String str2 = this.TAG;
            StringBuilder sb = StringBuilderCache.get();
            sb.append("handleProgressActionClick(), current cacheBtnStatus = ");
            sb.append(LJIILL);
            sb.append(", effectiveStatus = ");
            sb.append(i);
            r02.debug(str2, StringBuilderCache.release(sb));
            if (i == 2) {
                if (i != 4) {
                    startDownload();
                } else {
                    IDetailFeedContext<FeedParam> iDetailFeedContext = this.feedDetailContext;
                    if (iDetailFeedContext != null) {
                        fragmentActivity = iDetailFeedContext.getActivity();
                        if (fragmentActivity != null && NetworkUtils.isNetworkAvailable(fragmentActivity)) {
                            this.pauseReason = null;
                            getCacheLoader().LJIIIZ(2);
                            1NPF r03 = this.settingDialogVM;
                            if (r03 != null) {
                                r03.RL1(2);
                            }
                            getCacheLoader().LJJ();
                            1KWD.LIZ.getClass();
                            1KWD.LJI(1);
                        }
                    } else {
                        fragmentActivity = null;
                    }
                    this.pauseReason = PauseReason.NETWORK_CHANGE;
                    if (fragmentActivity != null) {
                        DuxToastV2.makeShowSystemToast$default(DuxToastV2.INSTANCE, fragmentActivity, "目前无网络可用", (Drawable) null, (Drawable) null, 0, false, (DuxToastLocation) null, false, 252, (Object) null);
                    }
                    1KWD r04 = 1KWD.LIZ;
                    int LJIIJ = OfflineKevaUtils.LJIIJ();
                    r04.getClass();
                    1KWD.LJIL(LJIIJ, 7, 2);
                    updateLayoutView();
                    return;
                }
            } else {
                this.pauseReason = null;
                getCacheLoader().LJIIIZ(4);
                1NPF r05 = this.settingDialogVM;
                if (r05 != null) {
                    r05.RL1(4);
                }
                getCacheLoader().LIZJ(false);
                1KWD.LJFF(1KWD.LIZ, 1);
                1KWD.LJIJJLI(5);
            }
            updateLayoutView();
        }
        str = "cache_pause";
        1KWD.LIZ.getClass();
        1KWD.LJIILJJIL("offline_mode", "inner_flow_setting", str);
        if (i != LJIILL) {
        }
        1S4G r022 = 1S4G.LIZIZ;
        String str22 = this.TAG;
        StringBuilder sb2 = StringBuilderCache.get();
        sb2.append("handleProgressActionClick(), current cacheBtnStatus = ");
        sb2.append(LJIILL);
        sb2.append(", effectiveStatus = ");
        sb2.append(i);
        r022.debug(str22, StringBuilderCache.release(sb2));
        if (i == 2) {
        }
        updateLayoutView();
    }

    public final void startDownload() {
        boolean z;
        FragmentActivity activity;
        int LJIILL = getCacheLoader().LJIILL();
        if (LJIILL != 2 && LJIILL != 4) {
            z = true;
        } else {
            z = false;
        }
        long availableInternalStorageSize = DeviceInfo.getAvailableInternalStorageSize();
        int LJIIJ = OfflineKevaUtils.LJIIJ();
        IDetailFeedContext<FeedParam> iDetailFeedContext = this.feedDetailContext;
        if (iDetailFeedContext != null && (activity = iDetailFeedContext.getActivity()) != null) {
            if (z) {
                if (availableInternalStorageSize < this.lowStorageThresholdBytes) {
                    this.pauseReason = PauseReason.STORAGE_NOT_ENOUGH;
                    DuxToastV2.makeShowSystemToast$default(DuxToastV2.INSTANCE, activity, "空间不足，请清理存储空间", (Drawable) null, (Drawable) null, 0, false, (DuxToastLocation) null, false, 252, (Object) null);
                    1KWD.LIZ.getClass();
                    1KWD.LIZIZ(1);
                    1KWD.LJIL(LJIIJ, 6, 1);
                    return;
                }
                if (z) {
                    if (!NetworkUtils.LJFF(activity)) {
                        this.pauseReason = PauseReason.NETWORK_CHANGE;
                        if (!NetworkUtils.LJ(activity)) {
                            DuxToastV2.makeShowSystemToast$default(DuxToastV2.INSTANCE, activity, "目前无网络可用", (Drawable) null, (Drawable) null, 0, false, (DuxToastLocation) null, false, 252, (Object) null);
                            1KWD.LIZ.getClass();
                            1KWD.LIZIZ(2);
                            1KWD.LJIL(LJIIJ, 7, 2);
                            return;
                        }
                        showNotWifiAlert(false);
                        return;
                    }
                    if (!NetworkUtils.isNetworkAvailable(activity)) {
                        this.pauseReason = PauseReason.NETWORK_CHANGE;
                        DuxToastV2.makeShowSystemToast$default(DuxToastV2.INSTANCE, activity, "目前无网络可用", (Drawable) null, (Drawable) null, 0, false, (DuxToastLocation) null, false, 252, (Object) null);
                        1KWD.LIZ.getClass();
                        1KWD.LIZIZ(2);
                        1KWD.LJIL(LJIIJ, 7, 2);
                        return;
                    }
                }
            }
            this.pauseReason = null;
            cacheClickAction();
            OfflineKevaUtils.LJJII(false);
        }
    }

    private final void initView(View view) {
        boolean z;
        MutableLiveData mutableLiveData;
        MutableLiveData mutableLiveData2;
        MutableLiveData mutableLiveData3;
        MutableLiveData mutableLiveData4;
        View LLLLZIL = 0bOs.LLLLZIL(0x7f0a81f0, view);
        if (LLLLZIL != null) {
            0bOs.X0(new ACListenerS135S0100000_37(this, 126), LLLLZIL);
        }
        this.infoCountView = (TextView) 0bOs.LLLLZIL(0x7f0a81fd, view);
        this.infoDescView = (TextView) 0bOs.LLLLZIL(0x7f0a81fe, view);
        this.updateBtn = (TextView) 0bOs.LLLLZIL(0x7f0a820a, view);
        this.updateContainer = 0bOs.LLLLZIL(0x7f0a820b, view);
        this.updateProgressContainer = 0bOs.LLLLZIL(0x7f0a820e, view);
        this.updateProgressView = 0bOs.LLLLZIL(0x7f0a820f, view);
        this.updateProgressPause = 0bOs.LLLLZIL(0x7f0a820c, view);
        this.updateProgressPlay = 0bOs.LLLLZIL(0x7f0a820d, view);
        this.progressCard = 0bOs.LLLLZIL(0x7f0a8204, view);
        this.selectCard = 0bOs.LLLLZIL(0x7f0a8207, view);
        this.progressDescView = (TextView) 0bOs.LLLLZIL(0x7f0a8205, view);
        this.progressValueView = (TextView) 0bOs.LLLLZIL(0x7f0a8206, view);
        this.progressActionView = (TextView) 0bOs.LLLLZIL(0x7f0a8202, view);
        this.progressActionContainer = 0bOs.LLLLZIL(0x7f0a8203, view);
        this.autoSwitch = 0bOs.LLLLZIL(0x7f0a81ee, view);
        this.count50 = 0bOs.LLLLZIL(0x7f0a81f7, view);
        this.count100 = 0bOs.LLLLZIL(0x7f0a81f1, view);
        this.count150 = 0bOs.LLLLZIL(0x7f0a81f3, view);
        this.count200 = 0bOs.LLLLZIL(0x7f0a81f5, view);
        this.count50Radio = 0bOs.LLLLZIL(0x7f0a81f8, view);
        this.count100Radio = 0bOs.LLLLZIL(0x7f0a81f2, view);
        this.count150Radio = 0bOs.LLLLZIL(0x7f0a81f4, view);
        this.count200Radio = 0bOs.LLLLZIL(0x7f0a81f6, view);
        this.memoryInfoView = (TextView) 0bOs.LLLLZIL(0x7f0a8201, view);
        this.clearCacheView = (TextView) 0bOs.LLLLZIL(0x7f0a81ef, view);
        DuxSwitch duxSwitch = this.autoSwitch;
        if (duxSwitch != null) {
            duxSwitch.setChecked(14zq.LIZ());
        }
        DuxSwitch duxSwitch2 = this.autoSwitch;
        if (duxSwitch2 != null) {
            0bOs.X0(new initView.2(this), duxSwitch2);
        }
        View view2 = this.updateContainer;
        if (view2 != null || (view2 = this.updateBtn) != null) {
            0bOs.X0(new ACListenerS149S0100000_51(this, 77), view2);
        }
        View view3 = this.updateProgressContainer;
        if (view3 != null) {
            0bOs.X0(new ACListenerS149S0100000_51(this, 78), view3);
        }
        View view4 = this.progressActionContainer;
        if (view4 != null) {
            0bOs.X0(new ACListenerS149S0100000_51(this, 79), view4);
        }
        View view5 = this.count50;
        if (view5 != null) {
            0bOs.X0(new ACListenerS149S0100000_51(this, 80), view5);
        }
        View view6 = this.count100;
        if (view6 != null) {
            0bOs.X0(new ACListenerS149S0100000_51(this, 81), view6);
        }
        View view7 = this.count150;
        if (view7 != null) {
            0bOs.X0(new ACListenerS149S0100000_51(this, 82), view7);
        }
        View view8 = this.count200;
        if (view8 != null) {
            0bOs.X0(new ACListenerS149S0100000_51(this, 83), view8);
        }
        TextView textView = this.clearCacheView;
        if (textView != null) {
            0bOs.X0(new ACListenerS149S0100000_51(this, 76), textView);
        }
        1NPF r0 = this.settingDialogVM;
        if (r0 != null && (mutableLiveData4 = r0.e) != null) {
            mutableLiveData4.observe(getViewLifecycleOwner(), new AObserverS357S0100000_37(this, 17));
        }
        1NPF r02 = this.settingDialogVM;
        if (r02 != null && (mutableLiveData3 = r02.f) != null) {
            mutableLiveData3.observe(getViewLifecycleOwner(), new AObserverS357S0100000_37(this, 18));
        }
        1NPF r03 = this.settingDialogVM;
        if (r03 != null && (mutableLiveData2 = r03.d) != null) {
            mutableLiveData2.observe(getViewLifecycleOwner(), new AObserverS369S0100000_51(this, 36));
        }
        1NPF r04 = this.settingDialogVM;
        if (r04 != null && (mutableLiveData = r04.g) != null) {
            mutableLiveData.observe(getViewLifecycleOwner(), new AObserverS369S0100000_51(this, 37));
        }
        1NPP cacheLoader = getCacheLoader();
        LifecycleOwner viewLifecycleOwner = getViewLifecycleOwner();
        Intrinsics.checkNotNullExpressionValue(viewLifecycleOwner, "");
        cacheLoader.LJ(viewLifecycleOwner, new 1NPI(this));
        updateCacheCountSelection(OfflineKevaUtils.LJFF());
        updateLayoutView();
        updateMemoryInfo(OfflineKevaUtils.LJI());
        if (OfflineKevaUtils.LJIIJ() > 0) {
            z = true;
        } else {
            z = false;
        }
        setBtnClearView(z);
    }
}
