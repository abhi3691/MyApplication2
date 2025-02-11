package com.example.myapplication;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {
    private TextView nfcStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        nfcStatus = findViewById(R.id.nfcStatus);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            nfcStatus.setText(R.string.nfc_is_not_supported_on_this_device);
            return;
        }

        if (!nfcAdapter.isEnabled()) {
            nfcStatus.setText(R.string.nfc_is_disabled_please_enable_it_in_settings);
            return;
        }

        handleNfcIntent(getIntent());
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        handleNfcIntent(intent);
    }

    private void handleNfcIntent(Intent intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()) ||
                NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) {
                readNfcTag(tag);
            }
        }
    }

    private void readNfcTag(Tag tag) {
        Ndef ndef = Ndef.get(tag);
        if (ndef == null) {
            Toast.makeText(this, "NDEF not supported!", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            ndef.connect();
            NdefMessage ndefMessage = ndef.getNdefMessage();
            if (ndefMessage != null) {
                NdefRecord[] records = ndefMessage.getRecords();
                for (NdefRecord record : records) {
                    String payload = new String(record.getPayload(), StandardCharsets.UTF_8);
                    if (payload.length() > 3) {
                        payload = payload.substring(3);
                    }
                    nfcStatus.setText(String.format("%s%s", getString(R.string.received_nfc_data), payload));
                    Toast.makeText(this, "NFC Data Received: " + payload, Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error reading NFC!", Toast.LENGTH_SHORT).show();
        }
    }
}
