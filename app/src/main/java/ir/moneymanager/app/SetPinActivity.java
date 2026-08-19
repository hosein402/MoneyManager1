package ir.moneymanager.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SetPinActivity extends AppCompatActivity {

    private EditText etNewPin, etConfirmPin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_pin);

        etNewPin = findViewById(R.id.etNewPin);
        etConfirmPin = findViewById(R.id.etConfirmPin);
        Button btnSave = findViewById(R.id.btnSavePin);

        btnSave.setOnClickListener(v -> {
            String pin = etNewPin.getText().toString().trim();
            String confirm = etConfirmPin.getText().toString().trim();

            if (pin.length() != 4) {
                Toast.makeText(this, R.string.pin_too_short, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!pin.equals(confirm)) {
                Toast.makeText(this, R.string.pin_mismatch, Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = getSharedPreferences(SecurityActivity.PREFS_NAME, MODE_PRIVATE);
            prefs.edit()
                    .putString(SecurityActivity.KEY_PIN_HASH, AppLockState.hashPin(pin))
                    .putBoolean(SecurityActivity.KEY_PIN_ENABLED, true)
                    .apply();

            Toast.makeText(this, R.string.pin_saved, Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
