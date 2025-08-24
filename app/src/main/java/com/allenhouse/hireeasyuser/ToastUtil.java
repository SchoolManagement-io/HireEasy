package com.allenhouse.hireeasyuser;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.allenhouse.hireeasyuser.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class ToastUtil {

    public static final int TYPE_SUCCESS = 0;
    public static final int TYPE_ERROR = 1;
    public static final int TYPE_INFO = 2;

    public static void show(Context context, String message, int type) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.custom_toast, null);

        // Views
        TextView toastText = layout.findViewById(R.id.toast_text);
        CardView toastContainer = layout.findViewById(R.id.toast_container);
        CircleImageView toastIcon = layout.findViewById(R.id.toast_icon);

        toastText.setText(message);

        // Customize based on type
        int bgColor;

        switch (type) {
            case TYPE_SUCCESS:
                bgColor = R.color.colorSuccess;
                break;
            case TYPE_ERROR:
                bgColor = R.color.colorDanger;
                break;
            case TYPE_INFO:
            default:
                bgColor = R.color.colorPrimary;
                break;
        }

        // Apply them
        toastContainer.setCardBackgroundColor(ContextCompat.getColor(context, bgColor));

        Toast toast = new Toast(context);
        toast.setView(layout);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 100);
        toast.show();
    }

    // Shortcut methods
    public static void success(Context context, String msg) {
        show(context, msg, TYPE_SUCCESS);
    }

    public static void error(Context context, String msg) {
        show(context, msg, TYPE_ERROR);
    }

    public static void info(Context context, String msg) {
        show(context, msg, TYPE_INFO);
    }
}
