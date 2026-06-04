package org.sosalerter.app;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class AboutActivity extends AppCompatActivity {

    private static final String GITHUB_REPO_URL = "https://github.com/dhilipmpms/SOS-alerter";
    private static final String LEAD_GITHUB_URL = "https://github.com/dhilipmpms";
    private static final String TELEGRAM_COMMUNITY_URL = "https://t.me/sos_alerter_community";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Setup Dynamic App Info
        setupAppInfo();

        // Setup Click Listeners for Interactive Items
        setupClickListeners();

        // Run Staggered Cascade Entrance Animation
        runStaggeredEntranceAnimation();
    }

    private void setupAppInfo() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = packageInfo.versionName;
            long versionCode = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ? packageInfo.getLongVersionCode()
                    : packageInfo.versionCode;

            TextView tvVersion = findViewById(R.id.tv_app_version);
            TextView tvBuild = findViewById(R.id.tv_app_build);

            if (tvVersion != null) {
                tvVersion.setText(versionName);
            }
            if (tvBuild != null) {
                tvBuild.setText(String.valueOf(versionCode));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void setupClickListeners() {
        // Project Lead GitHub profile card
        View btnLeadGithub = findViewById(R.id.btn_lead_github);
        if (btnLeadGithub != null) {
            btnLeadGithub.setOnClickListener(v -> openUrl(LEAD_GITHUB_URL));
        }

        // Contributors Card
        View cardContributors = findViewById(R.id.card_contributors);
        if (cardContributors != null) {
            cardContributors.setOnClickListener(v -> showContributorsDialog());
        }

        // Community Rows
        View rowRepo = findViewById(R.id.row_community_repo);
        if (rowRepo != null) {
            rowRepo.setOnClickListener(v -> openUrl(GITHUB_REPO_URL));
        }

        View rowIssues = findViewById(R.id.row_community_issues);
        if (rowIssues != null) {
            rowIssues.setOnClickListener(v -> openUrl(GITHUB_REPO_URL + "/issues"));
        }

        View rowDiscussions = findViewById(R.id.row_community_discussions);
        if (rowDiscussions != null) {
            rowDiscussions.setOnClickListener(v -> openUrl(GITHUB_REPO_URL + "/discussions"));
        }

        View rowTelegram = findViewById(R.id.row_community_telegram);
        if (rowTelegram != null) {
            rowTelegram.setOnClickListener(v -> openUrl(TELEGRAM_COMMUNITY_URL));
        }

        // Support Card
        View cardSupport = findViewById(R.id.card_support_dev);
        if (cardSupport != null) {
            cardSupport.setOnClickListener(v -> showSupportDialog());
        }

        // Legal Rows
        View rowPrivacy = findViewById(R.id.row_legal_privacy);
        if (rowPrivacy != null) {
            rowPrivacy.setOnClickListener(v -> showPrivacyDialog());
        }

        View rowLicense = findViewById(R.id.row_legal_license);
        if (rowLicense != null) {
            rowLicense.setOnClickListener(v -> showLicenseDialog());
        }

        View rowThirdParty = findViewById(R.id.row_legal_third_party);
        if (rowThirdParty != null) {
            rowThirdParty.setOnClickListener(v -> showThirdPartyLicensesDialog());
        }

        View rowNotices = findViewById(R.id.row_legal_notices);
        if (rowNotices != null) {
            rowNotices.setOnClickListener(v -> showOpenSourceNoticesDialog());
        }
    }

    private void runStaggeredEntranceAnimation() {
        final View[] cards = new View[] {
                findViewById(R.id.card_about_app),
                findViewById(R.id.card_app_info),
                findViewById(R.id.card_project_info),
                findViewById(R.id.card_project_lead),
                findViewById(R.id.card_contributors),
                findViewById(R.id.card_community),
                findViewById(R.id.card_support_dev),
                findViewById(R.id.card_legal)
        };

        Handler handler = new Handler();
        for (int i = 0; i < cards.length; i++) {
            final View card = cards[i];
            if (card != null) {
                card.setVisibility(View.INVISIBLE);
                final int delay = i * 70; // 70ms stagger offset
                handler.postDelayed(() -> {
                    card.setVisibility(View.VISIBLE);
                    Animation anim = AnimationUtils.loadAnimation(AboutActivity.this, R.anim.slide_up_fade);
                    card.startAnimation(anim);
                }, delay);
            }
        }
    }

    private void openUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Could not open URL link", Toast.LENGTH_SHORT).show();
        }
    }

    private void showContributorsDialog() {
        if (isFinishing() || isDestroyed()) return;
        new MaterialAlertDialogBuilder(this)
                .setTitle("Contributors")
                .setMessage("We thank all project contributors and community members:\n\n" +
                        "• Dhilip S — Project Lead & Primary Developer\n" +
                        "• Open Source Community — Quality Testers & Translators\n" +
                        "• F-Droid Community — Package Maintainers & Audit Reviewers\n\n" +
                        "Want to be listed here? Join the project on GitHub!")
                .setPositiveButton("GitHub Contributors",
                        (dialog, which) -> openUrl(GITHUB_REPO_URL + "/graphs/contributors"))
                .setNegativeButton("Close", null)
                .show();
    }

    private void showSupportDialog() {
        if (isFinishing() || isDestroyed()) return;
        new MaterialAlertDialogBuilder(this)
                .setTitle("Support SOS Alerter")
                .setMessage(
                        "SOS Alerter is and will always remain a 100% free and open-source utility, built to offer community safety with zero trackers or subscriptions.\n\n"
                                +
                                "If you would like to support us, please consider:\n" +
                                "• Giving a star 🌟 on GitHub\n" +
                                "• Spreading the word to family and friends\n" +
                                "• Contributing code contributions or localized translations\n\n" +
                                "Your non-financial and community support keeps this project alive!")
                .setPositiveButton("Star Repository", (dialog, which) -> openUrl(GITHUB_REPO_URL))
                .setNegativeButton("Close", null)
                .show();
    }

    private void showPrivacyDialog() {
        if (isFinishing() || isDestroyed()) return;
        new MaterialAlertDialogBuilder(this)
                .setTitle("Privacy Policy")
                .setMessage("Privacy is the fundamental tenet of SOS Alerter.\n\n" +
                        "1. Local Storage: All configuration settings, emergency contacts, and local data records are stored securely on the device and never sent to cloud servers.\n"
                        +
                        "2. Location Data: Fused Location data is only fetched upon active emergency trigger and sent exclusively via SMS to user-designated contacts.\n"
                        +
                        "3. Camera & Microphone: Media files are recorded locally on your device for evidence storage and not transmitted externally.\n"
                        +
                        "4. Telemetry: The application contains absolutely no analytics tools, advertising trackers, or telemetry code.")
                .setPositiveButton("Close", null)
                .show();
    }

    private void showLicenseDialog() {
        if (isFinishing() || isDestroyed()) return;
        new MaterialAlertDialogBuilder(this)
                .setTitle("GPL-3.0 License")
                .setMessage("SOS Alerter is licensed under the GNU General Public License v3.0.\n\n" +
                        "This code represents Free Software. You are free to run, copy, distribute, study, change, and improve the software, provided that any derivative works are also distributed under the same license terms.\n\n"
                        +
                        "This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.")
                .setPositiveButton("Full License",
                        (dialog, which) -> openUrl("https://www.gnu.org/licenses/gpl-3.0.html"))
                .setNegativeButton("Close", null)
                .show();
    }

    private void showThirdPartyLicensesDialog() {
        if (isFinishing() || isDestroyed()) return;
        new MaterialAlertDialogBuilder(this)
                .setTitle("Third Party Licenses")
                .setMessage("This application relies on the following open source libraries:\n\n" +
                        "1. AndroidX Libraries\n" +
                        "   - Lic: Apache License 2.0\n\n" +
                        "2. Google Material Components (M3)\n" +
                        "   - Lic: Apache License 2.0\n\n" +
                        "3. Play Services Location Client\n" +
                        "   - Lic: Android Software Development Kit License\n\n" +
                        "4. Room Database Compiler & Runtime\n" +
                        "   - Lic: Apache License 2.0")
                .setPositiveButton("Close", null)
                .show();
    }

    private void showOpenSourceNoticesDialog() {
        if (isFinishing() || isDestroyed()) return;
        new MaterialAlertDialogBuilder(this)
                .setTitle("Open Source Notice")
                .setMessage(
                        "This application is fully open source under the GPL-3.0 license. The source code is publicly accessible, allowing for user audits, custom builds, and security verification.\n\n"
                                +
                                "You can verify the source, review security implementations, and compile the code directly from our public GitHub repository.")
                .setPositiveButton("View Source", (dialog, which) -> openUrl(GITHUB_REPO_URL))
                .setNegativeButton("Close", null)
                .show();
    }
}
