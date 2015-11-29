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
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

/** ESTA ACTIVITY TIENE APARIENCIA DE DIALOGO (CONFIGURADA EN EL MANIFEST)
 * SI EXISTEN DISPOSITIVOS VINCULADOS, LOS ARREGLA AL PRIMER ARREGO
 * SE ENLISTAN MAS DEVICES AL HACE UN DISCOVERY CUANDO SE ELIGE UN DEVICE
 * SE REGRESA LA DIRECCION MAC A LA PARENT ACTIVITY EN EL INTENT RESULT
 */
public class DatosPaciente extends Activity {
    // Debugging
    private static final String TAG = "CONTROL";
    private static final boolean D = true;

    Button actualizar, cancelar;
    String nombrePaciente, edadPaciente;
    TextView editPaciente, editEdad;
    Switch incrementos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // cargar la ventana
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.datos_paciente);

        editPaciente = (TextView) findViewById(R.id.paciente_nombre);
        editEdad = (TextView) findViewById(R.id.paciente_edad);
        actualizar = (Button) findViewById(R.id.b_actualizar);
        cancelar = (Button) findViewById(R.id.b_cancelar);
        incrementos = (Switch) findViewById(R.id.switch1);


        final SharedPreferences respaldo = getSharedPreferences("MisDatos", Context.MODE_PRIVATE);
        // cargar la clave en la variable clave, o 0000 por default (no encontrada, etc);
        nombrePaciente = respaldo.getString("patientName","Paciente");
        edadPaciente = respaldo.getString("patientAge","Edad");

        editPaciente.setText(nombrePaciente);
        editEdad.setText(edadPaciente);

        incrementos.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Vibrator vibrador = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);        // Vibrate for 500 milliseconds
                vibrador.vibrate(100);
            }
        });

        actualizar.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Vibrator vibrador = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);        // Vibrate for 500 milliseconds
                vibrador.vibrate(100);

                nombrePaciente = editPaciente.getText().toString();
                edadPaciente = editEdad.getText().toString();

                SharedPreferences.Editor editor = respaldo.edit();
                editor.putString("patientName", nombrePaciente);
                editor.putString("patientAge", edadPaciente);

                if(editor.commit())
                {
                    Toast.makeText(getBaseContext(),"Actualizado correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });

        cancelar.setOnClickListener(new View.OnClickListener()
                                    {
                                        public void onClick(View v)
                                        {
                                            Vibrator vibrador = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);        // Vibrate for 500 milliseconds
                                            vibrador.vibrate(100);
                                            finish();
                                        }
                                    }
        );

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
