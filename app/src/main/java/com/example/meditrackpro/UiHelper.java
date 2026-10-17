package com.example.meditrackpro;

import android.content.Context;
import android.content.res.ColorStateList;
import android.widget.Button;

import androidx.annotation.ColorRes;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

/** Small helpers so every screen draws "selected" and "unselected" the same way, in both themes. */
final class UiHelper {

    private UiHelper() {
    }

    static int color(Context context, @ColorRes int res) {
        return context.getColor(res);
    }

    static int dp(Context context, int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }

    /** Chip-style button (tabs, gender, blood type, record type). */
    static void setChoiceSelected(Button button, boolean selected) {
        Context context = button.getContext();
        int fill = color(context, selected ? R.color.accent_container : R.color.surface_alt);
        int text = color(context, selected ? R.color.accent : R.color.text_secondary);
        int stroke = color(context, selected ? R.color.accent : R.color.outline);

        button.setTextColor(text);
        if (button instanceof MaterialButton) {
            MaterialButton mb = (MaterialButton) button;
            mb.setBackgroundTintList(ColorStateList.valueOf(fill));
            mb.setStrokeColor(ColorStateList.valueOf(stroke));
            mb.setStrokeWidth(dp(context, selected ? 2 : 1));
        } else {
            button.setBackgroundTintList(ColorStateList.valueOf(fill));
        }
    }

    /** Selectable card (theme picker). */
    static void setCardSelected(MaterialCardView card, boolean selected) {
        Context context = card.getContext();
        card.setCardBackgroundColor(color(context, selected ? R.color.accent_container : R.color.surface));
        card.setStrokeColor(color(context, selected ? R.color.accent : R.color.outline));
        card.setStrokeWidth(dp(context, selected ? 2 : 1));
    }
}
