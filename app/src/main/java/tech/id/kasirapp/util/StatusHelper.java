package tech.id.kasirapp.util;

import android.app.Activity;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import tech.id.kasirapp.R;

public class StatusHelper {

    public interface OnDismissListener {
        void onDismiss();
    }

    public interface OnConfirmListener {
        void onConfirm();
    }

    public static void showSuccess(Activity activity, String title, String message, OnDismissListener listener) {
        playTone(true);
        showDialog(activity, true, title, message, listener);
    }

    public static void showError(Activity activity, String title, String message, OnDismissListener listener) {
        playTone(false);
        showDialog(activity, false, title, message, listener);
    }

    public static void showConfirm(Activity activity, String title, String message, OnConfirmListener listener) {
        playConfirmTone();
        showConfirmDialog(activity, title, message, listener);
    }

    private static void showDialog(Activity activity, boolean isSuccess, String title, String message, OnDismissListener listener) {
        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_status, null);
        
        ImageView imgStatus = view.findViewById(R.id.imgStatus);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvMessage = view.findViewById(R.id.tvMessage);
        View btnAction = view.findViewById(R.id.btnAction);

        imgStatus.setImageResource(isSuccess ? R.drawable.ic_success : R.drawable.ic_error);
        tvTitle.setText(title);
        tvMessage.setText(message);

        AlertDialog dialog = new MaterialAlertDialogBuilder(activity)
                .setView(view)
                .setCancelable(false)
                .create();

        btnAction.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) {
                listener.onDismiss();
            }
        });

        if (!activity.isFinishing()) {
            dialog.show();
        }
    }

    private static void showConfirmDialog(Activity activity, String title, String message, OnConfirmListener listener) {
        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_confirm, null);
        
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvMessage = view.findViewById(R.id.tvMessage);
        View btnCancel = view.findViewById(R.id.btnCancel);
        View btnConfirm = view.findViewById(R.id.btnConfirm);

        tvTitle.setText(title);
        tvMessage.setText(message);

        AlertDialog dialog = new MaterialAlertDialogBuilder(activity)
                .setView(view)
                .setCancelable(true)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) {
                listener.onConfirm();
            }
        });

        if (!activity.isFinishing()) {
            dialog.show();
        }
    }

    private static void playTone(boolean isSuccess) {
        try {
            ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
            if (isSuccess) {
                tg.startTone(ToneGenerator.TONE_PROP_ACK, 200);
            } else {
                tg.startTone(ToneGenerator.TONE_PROP_NACK, 500);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void playConfirmTone() {
        try {
            ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
            tg.startTone(ToneGenerator.TONE_PROP_BEEP, 150);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}