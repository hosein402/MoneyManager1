package ir.moneymanager.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.util.concurrent.Executor;

public class LockActivity extends AppCompatActivity {

    private TextView tvPinDots, tvLockError;
    private StringBuilder enteredPin = new StringBuilder();
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);

        prefs = getSharedPreferences(SecurityActivity.PREFS_NAME, MODE_PRIVATE);

        tvPinDots = findViewById(R.id.tvPinDots);
        tvLockError = findViewById(R.id.tvLockError);
        Button btnFingerprint = findViewById(R.id.btnFingerprint);

        int[] numberIds = {R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9};
        for (int i = 0; i < numberIds.length; i++) {
            Button btn = findViewById(numberIds[i]);
            String digit = String.valueOf(i);
            btn.setOnClickListener(v -> onDigit(digit));
        }

        Button btnDelete = findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(v -> {
            if (enteredPin.length() > 0) {
                enteredPin.deleteCharAt(enteredPin.length() - 1);
                updateDots();
            }
        });

        boolean fingerprintEnabled = prefs.getBoolean(SecurityActivity.KEY_FINGERPRINT_ENABLED, false);
        BiometricManager biometricManager = BiometricManager.from(this);
        boolean biometricAvailable = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                == BiometricManager.BIOMETRIC_SUCCESS;

        if (fingerprintEnabled && biometricAvailable) {
            btnFingerprint.setVisibility(android.view.View.VISIBLE);
            btnFingerprint.setOnClickListener(v -> showBiometricPrompt());
            showBiometricPrompt();
        }
    }

    private void onDigit(String digit) {
        if (enteredPin.length() < 4) {
            enteredPin.append(digit);
            updateDots();
        }
        if (enteredPin.length() == 4) {
            checkPin();
        }
    }

    private void updateDots() {
        StringBuilder display = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            display.append(i < enteredPin.length() ? "●" : "○");
            if (i < 3) display.append(" ");
        }
        tvPinDots.setText(display.toString());
        tvLockError.setVisibility(android.view.View.INVISIBLE);
    }

    private void checkPin() {
        String storedHash = prefs.getString(SecurityActivity.KEY_PIN_HASH, "");
        String enteredHash = AppLockState.hashPin(enteredPin.toString());

        if (enteredHash.equals(storedHash)) {
            AppLockState.unlocked = true;
            setResult(RESULT_OK);
            finish();
        } else {
            tvLockError.setVisibility(android.view.View.VISIBLE);
            enteredPin.setLength(0);
            updateDots();
        }
    }

    private void showBiometricPrompt() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                AppLockState.unlocked = true;
                setResult(RESULT_OK);
                finish();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.lock_use_fingerprint))
                .setNegativeButtonText(getString(R.string.cancel))
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
