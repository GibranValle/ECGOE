package biouno.upibi.ecgoe;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

/**
 * ESTA ACTIVITY TIENE APARIENCIA DE DIALOGO (CONFIGURADA EN EL MANIFEST)
 * SI EXISTEN DISPOSITIVOS VINCULADOS, LOS ARREGLA AL PRIMER ARREGO
 * SE ENLISTAN MAS DEVICES AL HACE UN DISCOVERY CUANDO SE ELIGE UN DEVICE
 * SE REGRESA LA DIRECCION MAC A LA PARENT ACTIVITY EN EL INTENT RESULT
 */
public class DatosConfig extends Activity {
    // Debugging
    private static final String TAG = "CONTROL";

    Button actualizar, cancelar;
    String paso, umbralQRS;
    SeekBar barra, barra_derivacion;
    Switch switch_invertir, switch_autoset;
    TextView editPaso, editAmplitud, tagAmplificacion, tagDerivacion;
    LinearLayout umbral, gain;
    float amplificacion;
    int dato, derivacion_num;
    boolean invertir, autoset;

    //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // cargar la ventana
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.config);

        editPaso = (TextView) findViewById(R.id.edit_paso);
        editAmplitud = (TextView) findViewById(R.id.edit_amplitud);
        actualizar = (Button) findViewById(R.id.b_actualizar);
        cancelar = (Button) findViewById(R.id.b_cancelar);
        barra = (SeekBar) findViewById(R.id.barra);
        barra_derivacion = (SeekBar) findViewById(R.id.barra_derivacion);
        tagAmplificacion = (TextView) findViewById(R.id.tag_amplificacion);
        tagDerivacion = (TextView) findViewById(R.id.tag_derivacion);
        switch_invertir = (Switch) findViewById(R.id.invertir);
        switch_autoset = (Switch) findViewById(R.id.autoset);
        umbral = (LinearLayout) findViewById(R.id.umbral);
        gain = (LinearLayout) findViewById(R.id.gain);

        switch_autoset.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                autoset = isChecked;
                if (autoset) {
                    umbral.setVisibility(View.GONE);
                    gain.setVisibility(View.GONE);
                } else {
                    umbral.setVisibility(View.VISIBLE);
                    gain.setVisibility(View.VISIBLE);
                }
            }
        });

        switch_invertir.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                invertir = isChecked;
            }
        });

        barra.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                dato = progress;
                amplificacion = Math.round(100 * ((dato * dato * dato * dato * 0.010417f) - (dato * dato * dato * 0.020833f) + (dato * dato * 0.114583f) + (dato * 0.145833f) + (0.25f)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                tagAmplificacion.setText("Gain x " + amplificacion / 100);
            }
        });

        barra_derivacion.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                derivacion_num = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (derivacion_num == 3) // señal de calibración
                {
                    tagDerivacion.setText("Calibracion");

                } else if (derivacion_num == 4) {
                    tagDerivacion.setText("Barrido");
                } else {
                    tagDerivacion.setText("Derivación: " + (derivacion_num + 1));
                }
            }
        });

        final SharedPreferences respaldo = getSharedPreferences("MisDatos", Context.MODE_PRIVATE);
        // cargar la clave en la variable clave, o 0000 por default (no encontrada, etc);
        paso = respaldo.getString("paso", "2");
        umbralQRS = respaldo.getString("amplitud", "20");
        dato = Integer.parseInt(respaldo.getString("amplificacion", "2"));
        invertir = respaldo.getBoolean("invertir", false);
        autoset = respaldo.getBoolean("autoset", true);
        derivacion_num = respaldo.getInt("derivacion", 4);


        editPaso.setText(paso);
        editAmplitud.setText(umbralQRS);
        barra.setProgress(dato);
        barra_derivacion.setProgress(derivacion_num);
        amplificacion = Math.round(100 * ((dato * dato * dato * dato * 0.010417f) - (dato * dato * dato * 0.020833f) + (dato * dato * 0.114583f) + (dato * 0.145833f) + (0.25f)));
        tagAmplificacion.setText("Gain x " + amplificacion / 100);
        if (derivacion_num == 3) // señal de calibración
        {
            tagDerivacion.setText("Calibracion");

        } else if (derivacion_num == 4) {
            tagDerivacion.setText("Barrido");
        } else {
            tagDerivacion.setText("Derivación: " + (derivacion_num + 1));
        }
        switch_invertir.setChecked(invertir);
        switch_autoset.setChecked(autoset);

        actualizar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Vibrator vibrador = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);        // Vibrate for 500 milliseconds
                vibrador.vibrate(100);

                paso = editPaso.getText().toString();
                umbralQRS = editAmplitud.getText().toString();

                SharedPreferences.Editor editor = respaldo.edit();
                editor.putString("paso", paso);
                editor.putString("amplitud", umbralQRS);
                editor.putString("amplificacion", String.valueOf(dato));
                editor.putBoolean("invertir", invertir);
                editor.putBoolean("autoset", autoset);
                editor.putInt("derivacion", derivacion_num);


                if (Integer.parseInt(umbralQRS) >= 0 && Integer.parseInt(umbralQRS) <= 240) {
                    if (Integer.parseInt(paso) >= 1 && Integer.parseInt(paso) <= 3) {
                        if (editor.commit()) {
                            Toast.makeText(getBaseContext(), "Actualizado correctamente", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(getBaseContext(), "Error: Paso de grafica erronea", Toast.LENGTH_SHORT).show();
                    }
                } else if (Integer.parseInt(umbralQRS) < 0 && Integer.parseInt(umbralQRS) > 240) {
                    Toast.makeText(getBaseContext(), "Error: Amplitud del QRS erronea", Toast.LENGTH_SHORT).show();
                    if (Integer.parseInt(paso) < 1 && Integer.parseInt(paso) > 3) {
                        Toast.makeText(getBaseContext(), "Error: Paso de grafica erronea", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

        cancelar.setOnClickListener(new View.OnClickListener() {
                                        public void onClick(View v) {
                                            Vibrator vibrador = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);        // Vibrate for 500 milliseconds
                                            vibrador.vibrate(100);
                                            finish();
                                        }
                                    }
        );

    }
}
