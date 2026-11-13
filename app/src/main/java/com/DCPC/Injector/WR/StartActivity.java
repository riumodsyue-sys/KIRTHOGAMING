package com.DCPC.Injector.WR;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class StartActivity extends Activity {

    private static final String TAG = "InjectorLocal";
    private TextView tvStatus;
    private Button   btnActivar;
    private boolean  injected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ── UI ───────────────────────────────────────────────────────────
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(Color.parseColor("#0f0f1a"));
        root.setPadding(60, 60, 60, 60);

        TextView title = new TextView(this);
        title.setText("WR Injector");
        title.setTextColor(Color.parseColor("#e94560"));
        title.setTextSize(28f);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 16);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("v12.5.0  ·  Local  ·  Sin servidor");
        sub.setTextColor(Color.parseColor("#666688"));
        sub.setTextSize(13f);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, 60);
        root.addView(sub);

        tvStatus = new TextView(this);
        tvStatus.setText("Abre War Robots y presiona Activar.");
        tvStatus.setTextColor(Color.parseColor("#cccccc"));
        tvStatus.setTextSize(14f);
        tvStatus.setGravity(Gravity.CENTER);
        tvStatus.setPadding(0, 0, 0, 60);
        root.addView(tvStatus);

        btnActivar = new Button(this);
        btnActivar.setText("ACTIVAR");
        btnActivar.setTextColor(Color.WHITE);
        btnActivar.setTextSize(18f);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#e94560"));
        bg.setCornerRadius(28f);
        btnActivar.setBackground(bg);
        btnActivar.setPadding(100, 36, 100, 36);
        root.addView(btnActivar);

        setContentView(root);

        // ── Botón Activar ─────────────────────────────────────────────────
        btnActivar.setOnClickListener(v -> {
            if (injected) { toast("Ya fue activado."); return; }
            btnActivar.setEnabled(false);
            setStatus("⏳ Preparando hook...", "#ffeaa7");

            new Thread(() -> {
                // Paso 1: extraer libunity.so de assets → /data/local/tmp/
                boolean ok = extractSo("libunity.so", "/data/local/tmp/libunity.so");
                if (!ok) {
                    runOnUiThread(() -> {
                        setStatus("✗ Error copiando .so (¿root?)", "#e74c3c");
                        btnActivar.setEnabled(true);
                    });
                    return;
                }

                // Paso 2: llamar Start() de libStart.so
                // Java_com_DCPC_Injector_WR_StartActivity_Start
                runOnUiThread(() -> setStatus("⏳ Inyectando en WR...", "#ffeaa7"));
                Start();

                runOnUiThread(() -> {
                    injected = true;
                    setStatus("✓ Hook inyectado. Revisa WR.", "#2ecc71");
                    btnActivar.setText("ACTIVADO ✓");
                    GradientDrawable bg2 = new GradientDrawable();
                    bg2.setColor(Color.parseColor("#2ecc71"));
                    bg2.setCornerRadius(28f);
                    btnActivar.setBackground(bg2);
                });

            }).start();
        });
    }

    // ── Extrae asset y lo copia con root a /data/local/tmp/ ──────────────
    private boolean extractSo(String assetName, String dstPath) {
        try {
            File tmp = new File(getFilesDir(), assetName);
            InputStream in  = getAssets().open(assetName);
            FileOutputStream out = new FileOutputStream(tmp);
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            in.close(); out.close();

            // Copiar a /data/local/tmp/ con permisos globales (root)
            String cmd = "cp '" + tmp.getAbsolutePath() + "' '" + dstPath
                       + "' && chmod 777 '" + dstPath + "'";
            Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", cmd});
            int code = p.waitFor();
            Log.i(TAG, "extractSo exit=" + code + " dst=" + dstPath);
            return code == 0;
        } catch (Exception e) {
            Log.e(TAG, "extractSo: " + e.getMessage());
            return false;
        }
    }

    private void setStatus(String msg, String hex) {
        tvStatus.setText(msg);
        tvStatus.setTextColor(Color.parseColor(hex));
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    // ── JNI: Java_com_DCPC_Injector_WR_StartActivity_Start ───────────────
    public native void Start();
}
