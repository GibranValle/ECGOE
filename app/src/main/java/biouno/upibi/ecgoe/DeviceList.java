package biouno.upibi.ecgoe;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Set;

/** ESTA ACTIVITY TIENE APARIENCIA DE DIALOGO (CONFIGURADA EN EL MANIFEST)
 * SI EXISTEN DISPOSITIVOS VINCULADOS, LOS ARREGLA AL PRIMER ARREGO
 * SE ENLISTAN MAS DEVICES AL HACE UN DISCOVERY CUANDO SE ELIGE UN DEVICE
 * SE REGRESA LA DIRECCION MAC A LA PARENT ACTIVITY EN EL INTENT RESULT
 */
public class DeviceList extends Activity {
    // Debugging
    private static final String TAG = "CONTROL";
    private static final boolean D = true;

    // Return Intent extra
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    public static String EXTRA_DEVICE_NAME = "device_name";


    // Objetos
    private BluetoothAdapter BTadaptador;
    private ArrayAdapter<String> arrayDevicesVinculados;
    private ArrayAdapter<String> arrayDevicesEncontrados;
    private IntentFilter filtro;
    private Set<BluetoothDevice> pairedDevices;
    private ProgressBar bolita;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // cargar la ventana
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.lista_devices);
        //recupera el xml del progressbar
        bolita = (ProgressBar)findViewById(R.id.bolita);

        // Set result CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED);

        // Inicializa el botonEnviar1, para realizar el discovery, lanza el metodo
        // doDiscovery y elimina el botonEnviar1.
        Button scanButton = (Button) findViewById(R.id.b_buscar);
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
                v.setVisibility(View.GONE);
            }
        });

        // inicializa los arreglos para devices vinculados y encontrados
        arrayDevicesVinculados = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        arrayDevicesEncontrados = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        // agrega los resources desde xml
        ListView pairedListView = (ListView) findViewById(R.id.lista_encontrados);
        ListView newDevicesListView = (ListView) findViewById(R.id.lista_nuevos);

        // setea los adaptadores necesarios
        pairedListView.setAdapter(arrayDevicesVinculados);
        newDevicesListView.setAdapter(arrayDevicesEncontrados);

        // agrega el responsable del listener
        pairedListView.setOnItemClickListener(mDeviceClickListener);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // Crea IntentFilter y registra los broadcast para cada accion respectivamente
        filtro = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filtro);
        filtro = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filtro);

        // abrir el adaptador local
        BTadaptador = BluetoothAdapter.getDefaultAdapter();

        //carga los dispositivos vinculados
        pairedDevices = BTadaptador.getBondedDevices();

        // si encuentra dispositivos vinculados, los agrega al array
        if (pairedDevices.size() > 0) {
            findViewById(R.id.titulo).setVisibility(View.VISIBLE); //hace visible el array
            for (BluetoothDevice device : pairedDevices) {
                arrayDevicesVinculados.add(device.getName() + "\n" + device.getAddress()); //agregando al array
            }
        } else {
            // si no existen dispositivos vinculados, carga un string
            String noDevices = getResources().getText(R.string.novinculados).toString();
            arrayDevicesVinculados.add(noDevices);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // cancelar el discovery antes de salir
        if (BTadaptador != null) {
            BTadaptador.cancelDiscovery();
        }

        // cancelar los registros de los broadcast
        this.unregisterReceiver(mReceiver);
    }

    private void doDiscovery() {
        if (D) Log.d(TAG, "doDiscovery()");
        // carga el progress bar
        bolita.setVisibility(View.VISIBLE);

        // cancelar el discovery si se esta efectuando
        if (BTadaptador.isDiscovering()) {
            BTadaptador.cancelDiscovery();
        }

        // empieza el discovery
        BTadaptador.startDiscovery();
    }

    //crea el listener para todos los listviews
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // cancelar el discovery, para empezar la conexion
            BTadaptador.cancelDiscovery();

            // recupera la direccion MAC del dispositivo, son los ultimos 17 caracteres del view
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            String name = info.substring(0,info.length() - 18);
            Log.d("TAG", name);

            // crea el intent y guarda la direccion MAC
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
            intent.putExtra(EXTRA_DEVICE_NAME, name);

            // Setea el result y termina la activity
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };

    // El broadcast que encuentra los dispositivos y los agrega al array cuando termina el discovery
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // carga la vista de dispositivos encontrados
            findViewById(R.id.subtitulo).setVisibility(View.VISIBLE);
            // Si se encuentra un device
            if (BluetoothDevice.ACTION_FOUND.equals(action))
            {
                //recupera el objeto device del intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // saltar si ya esta vinculado
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    arrayDevicesEncontrados.add(device.getName() + "\n" + device.getAddress());
                }
                //cuando termina el discovery, cambia la vista
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                bolita.setVisibility(View.GONE);
                if (arrayDevicesEncontrados.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.noencontrados).toString();
                    arrayDevicesEncontrados.add(noDevices);
                }
            }
        }
    };
}
