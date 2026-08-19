package ir.moneymanager.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;

public class SecurityActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "app_lock_prefs";
    public static final String KEY_PIN_ENABLED = "pin_enabled";
    public static final String KEY_PIN_HASH = "pin_hash";
    public static final String KEY_FINGERPRINT_ENABLED = "fingerprint_enabled";

    private TextView tvPinStatus, tvFingerprintStatus;
    private Button btnEnablePin, btnChangePin, btnDisablePin, btnToggleFingerprint;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        tvPinStatus = findViewById(R.id.tvPinStatus);
        tvFingerprintStatus = findViewById(R.id.tvFingerprintStatus);
        btnEnablePin = findViewById(R.id.btnEnablePin);
        btnChangePin = findViewById(R.id.btnChangePin);
        btnDisablePin = findViewById(R.id.btnDisablePin);
        btnToggleFingerprint = findViewById(R.id.btnToggleFingerprint);

        btnEnablePin.setOnClickListener(v -> startActivity(new Intent(this, SetPinActivity.class)));
        btnChangePin.setOnClickListener(v -> startActivity(new Intent(this, SetPinActivity.class)));

        btnDisablePin.setOnClickListener(v -> {
            prefs.edit()
                    .putBoolean(KEY_PIN_ENABLED, false)
                    .putBoolean(KEY_FINGERPRINT_ENABLED, false)
                    .remove(KEY_PIN_HASH)
                    .apply();
            refreshUi();
        });

        btnToggleFingerprint.setOnClickListener(v -> {
            boolean current = prefs.getBoolean(KEY_FINGERPRINT_ENABLED, false);
            prefs.edit().putBoolean(KEY_FINGERPRINT_ENABLED, !current).apply();
            refreshUi();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUi();
    }

    private void refreshUi() {
        boolean pinEnabled = prefs.getBoolean(KEY_PIN_ENABLED, false);
        boolean fingerprintEnabled = prefs.getBoolean(KEY_FINGERPRINT_ENABLED, false);

        BiometricManager biometricManager = BiometricManager.from(this);
        boolean biometricAvailable = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                == BiometricManager.BIOMETRIC_SUCCESS;

        tvPinStatus.setText(pinEnabled ? getString(R.string.pin_status_enabled) : getString(R.string.pin_status_disabled));
        tvFingerprintStatus.setText(fingerprintEnabled ? getString(R.string.fingerprint_status_enabled) : getString(R.string.fingerprint_status_disabled));

        btnEnablePin.setVisibility(pinEnabled ? android.view.View.GONE : android.view.View.VISIBLE);
        btnChangePin.setVisibility(pinEnabled ? android.view.View.VISIBLE : android.view.View.GONE);
        btnDisablePin.setVisibility(pinEnabled ? android.view.View.VISIBLE : android.view.View.GONE);

        btnToggleFingerprint.setVisibility(pinEnabled && biometricAvailable ? android.view.View.VISIBLE : android.view.View.GONE);
        btnToggleFingerprint.setText(fingerprintEnabled ? R.string.disable_fingerprint : R.string.enable_fingerprint);
    }
          }
