package com.example.meditrackpro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.List;

/**
 * Static list of home remedies and first aid tips. Tapping a card expands it to show the steps.
 */
public class RemediesActivity extends BaseActivity {

    private LinearLayout layoutTips;
    private Button btnRemedies;
    private Button btnFirstAid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AppThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remedies);

        layoutTips = findViewById(R.id.layoutTips);
        btnRemedies = findViewById(R.id.btnTabRemedies);
        btnFirstAid = findViewById(R.id.btnTabFirstAid);

        ((TextView) findViewById(R.id.tvDisclaimer)).setText(HealthTipsData.DISCLAIMER);
        findViewById(R.id.tvBack).setOnClickListener(v -> finish());
        btnRemedies.setOnClickListener(v -> showTab(true));
        btnFirstAid.setOnClickListener(v -> showTab(false));

        showTab(true);
    }

    private void showTab(boolean remedies) {
        UiHelper.setChoiceSelected(btnRemedies, remedies);
        UiHelper.setChoiceSelected(btnFirstAid, !remedies);
        List<HealthTipsData.Tip> tips = remedies ? HealthTipsData.HOME_REMEDIES : HealthTipsData.FIRST_AID;

        layoutTips.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (HealthTipsData.Tip tip : tips) {
            View card = inflater.inflate(R.layout.item_tip, layoutTips, false);
            ((TextView) card.findViewById(R.id.tvTipTitle)).setText(tip.title);
            ((TextView) card.findViewById(R.id.tvTipFor)).setText(tip.forWhat);
            ((TextView) card.findViewById(R.id.tvTipSteps)).setText(formatSteps(tip.steps));
            ((TextView) card.findViewById(R.id.tvTipCaution)).setText("Note: " + tip.caution);

            View detail = card.findViewById(R.id.layoutTipDetail);
            TextView arrow = card.findViewById(R.id.tvTipArrow);
            card.setOnClickListener(v -> {
                boolean open = detail.getVisibility() == View.VISIBLE;
                detail.setVisibility(open ? View.GONE : View.VISIBLE);
                arrow.setText(open ? "+" : "−");
            });
            layoutTips.addView(card);
        }
    }

    private static String formatSteps(List<String> steps) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < steps.size(); i++) {
            if (i > 0) sb.append("\n");
            sb.append(i + 1).append(". ").append(steps.get(i));
        }
        return sb.toString();
    }
}
