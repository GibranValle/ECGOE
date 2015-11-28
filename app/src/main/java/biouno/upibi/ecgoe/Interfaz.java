package biouno.upibi.ecgoe;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


public class Interfaz extends Activity implements CompoundButton.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener, View.OnClickListener {
    /*//////////////////////// CONSTANTES PARA BLUETOOTH//////////////////////////////////////////*/
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    // Name of the connected device
    private String connectedDeviceName = null;
    // String buffer for outgoing messages
    private StringBuffer outStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter BTadaptador = null;
    // Member object for the chat services
    private BluetoothManager BTservice = null;
    private static final boolean D = true;
    /*//////////////////////// CONSTANTES PARA BLUETOOTH//////////////////////////////////////////*/

    TextView estado, consola, velocidad, regla;
    ToggleButton power, direccion;
    ImageButton emergencia;
    Intent i;
    String TAG = "Interfaz";
    SharedPreferences respaldo;
    SeekBar barra;
    int porcentaje = 50;
    boolean activo = false;
    boolean dir = false;
    boolean usuario = true;
    /* METODOS SECUENCIADOS */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, " onCreate ");
        setContentView(R.layout.activity_interfaz);

        //cargar recurso xml
        consola = (TextView) findViewById(R.id.consola);
        regla = (TextView) findViewById(R.id.regla);
        estado = (TextView) findViewById(R.id.estado_conexion);
        velocidad = (TextView) findViewById(R.id.velocidad);
        power = (ToggleButton) findViewById(R.id.power);
        direccion = (ToggleButton) findViewById(R.id.direccion);
        emergencia = (ImageButton) findViewById(R.id.emergencia);
        barra = (SeekBar) findViewById(R.id.speed);

        // asignar un listener
        power.setOnCheckedChangeListener(this);
        direccion.setOnCheckedChangeListener(this);
        emergencia.setOnClickListener(this);
        barra.setOnSeekBarChangeListener(this);

        //////////////////*BLUETOOH ////////////////*/////////////////*/////////////////*/////////////////*/
        // Obtener el adaptador y comprobar soporte de BT
        BTadaptador = BluetoothAdapter.getDefaultAdapter();
        if (BTadaptador == null) {
            Log.e(TAG, "NO SOPORTA BT");
            finish();
        }
        //////////////////*BLUETOOH ////////////////*/////////////////*/////////////////*/////////////////*/
    }
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "RESUMIENDO");
        //////////////////*BLUETOOH ////////////////*/////////////////*/////////////////*/////////////////*/
        if (!BTadaptador.isEnabled())//habilitar si no lo esta
        {
            BTadaptador.enable();
        }
        if (BTservice != null)  //si ya se configuró el servicio de BT
        {
            //iniciar si no se ha iniciado
            if (BTservice.getState() == BluetoothManager.STATE_NONE) {
                BTservice.start();
                Toast.makeText(this, "Encendiendo Bluetooth", Toast.LENGTH_SHORT).show();
            }
        }
        // configurar el servicio de BT
        if (BTservice == null) configurar();
        //////////////////*BLUETOOH ////////////////*/////////////////*/////////////////*/////////////////*/
    }
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "DETENIENDO");
        enviarMensaje("F");
        if (BTservice != null)  //si ya se configuró el servicio de BT
        {
            //iniciar si no se ha iniciado
            if (BTservice.getState() == BluetoothManager.STATE_CONNECTED) {
                BTservice.stop();
                Toast.makeText(this, "Apagando Bluetooth", Toast.LENGTH_SHORT).show();
            }
        }
    }
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Destruyendo");
        enviarMensaje("S0");
        if (BTadaptador.isEnabled())//habilitar si no lo esta
        {
            BTadaptador.disable();
        }
        if (BTservice != null)  //si ya se configuró el servicio de BT
        {
            //iniciar si no se ha iniciado
            if (BTservice.getState() == BluetoothManager.STATE_CONNECTED) {
                BTservice.stop();
                Toast.makeText(this, "Apagando Bluetooth", Toast.LENGTH_SHORT).show();
            }
        }
    }
    /* METODOS SECUENCIADOS */

    void vibrar(int ms)
    {
        Vibrator vibrador = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);        // Vibrate for 500 milliseconds
        vibrador.vibrate(ms);
    }


   /* TOGGLE BUTTONS */
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)  //BOTON POWER
    {
        if(buttonView.getId() == R.id.power) // primer boton
        {
            vibrar(100);
            if(isChecked)
            {
                activo = true;
                Log.d(TAG, "POWER ON");
                if(dir)
                {
                    enviarMensaje("A" + porcentaje);
                    Log.d(TAG, "SUBIR");
                }
                else
                {
                    enviarMensaje("B" + porcentaje);
                    Log.d(TAG, "BAJAR");
                }
            }
            else
            {
                activo = false;
                Log.d(TAG, "POWER OFF");
                // VER SI EL USUARIO PRESIONÓ EL BOTON
                if(usuario)
                {
                    enviarMensaje("S"); // ENVIAR ID DE PARO NORMAL
                    Log.d(TAG, "STOP NORMAL");
                }
                else    // PARO DE EMERGENCIA
                {
                    // se realizó paro de emergencia
                    usuario = true; // REACTIVAR FUNCION USUARIO
                    //NO ENVIAR ID
                    Log.d(TAG, "PARO DE EMERGENCIA");
                }
            }
        }

        if(buttonView.getId() == R.id.direccion) // segundo boton
        {
            vibrar(100);
            if(activo)
            {
                if(isChecked)   // SUBIR
                {
                    dir = true;
                    Log.d(TAG, "SUBIR");
                    enviarMensaje("A" + porcentaje);    // SUBIR
                }
                else    // BAJAR
                {
                    dir = false;
                    Log.d(TAG, "BAJAR");
                    enviarMensaje("B" + porcentaje);   // BAJAR
                }
            }
            else
            {
                if(isChecked)   // SUBIR
                {
                    dir = true;
                    Log.d(TAG, "CAMBIAR DIRECCION SIN ENVIAR, SUBIR");
                }
                else    // BAJAR
                {
                    dir = false;
                    Log.d(TAG, "CAMBIAR DIRECCION SIN ENVIAR, BAJAR");
                }
            }
        }
    }
   /* TOGGLE BUTTONS */


    @Override   // BOTON DE PARO DE EMERGENCIA
    public void onClick(View v)
    {
        vibrar(100);
        barra.setProgress(0);
        velocidad.setText("Velocidad: 0%");
        Log.d(TAG, "PARO DE EMERGENCIA ON");
        usuario = false;
        power.setChecked(false);
        enviarMensaje("E");
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        porcentaje = progress;
        velocidad.setText("Velocidad: " + porcentaje+"%");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if(activo)
        {
            if(dir)
            {
                enviarMensaje("A" + porcentaje);
            }
            else
            {
                enviarMensaje("B" + porcentaje);
            }
        }
    }

    /* ///////////////////////////////MENU/////////////////////////////////////////////////////// */
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.control, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "BUTON PUSHADO");
        int id = item.getItemId();
        switch (id) {
            case R.id.buscar:
                Log.d(TAG, "ABRIR FRAGMENT");
                i = new Intent(this, DeviceList.class);
                startActivityForResult(i, REQUEST_CONNECT_DEVICE_SECURE);
                break;

            case R.id.visible: //hacer BT visible
                Log.d(TAG, "HACER VISIBLIE EL BT");
                hacerVisible();
                break;

            case R.id.voltaje: // pedir medicion de voltaje
                Log.d(TAG, "MEDIR VOLTAJE");
                enviarMensaje("V");
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    /* ///////////////////////////////MENU/////////////////////////////////////////////////////// */

    /* ///////////////////////////////METODOS BLUETOOTH/////////////////////////////////////////////// */
    private void hacerVisible() {
        if (D) Log.d(TAG, "ensure discoverable");
        if (BTadaptador.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            // elegir el intent para hacer visible
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            // 300 segundos de hacerlo visible
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            // lanzar el intent
            startActivity(discoverableIntent);
        }
    }
    private void configurar() {
        Log.d(TAG, "setupChat()");
        // Initialize the BluetoothChatService to perform bluetooth connections
        BTservice = new BluetoothManager(this, mHandler);
        // Initialize the buffer for outgoing messages
        outStringBuffer = new StringBuffer("");
    }
    private void enviarMensaje(String mensaje) //recibeel mensaje enviar de tipo string
    {
        //checar la conexion antes de enviar
        if (BTservice.getState() != BluetoothManager.STATE_CONNECTED) {
            Toast.makeText(this, "NO CONECTADO", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "NO CONECTADO");
            return;
        }
        //comprobar que haya algo para enviar
        if (mensaje.length() > 0) {
            //convertir a bytes para enviar por serial
            byte[] send = mensaje.getBytes();
            BTservice.write(send);
            Log.d(TAG, "ENVIANDO MENSAJE: " + mensaje);
        }
    }
    private void conectarDevice(Intent data, boolean secure) {
        // RECUPERAR LA DIRECCIÓN MAC
        String address = data.getExtras().getString(DeviceList.EXTRA_DEVICE_ADDRESS);
        String nombre = data.getExtras().getString(DeviceList.EXTRA_DEVICE_NAME);
        // Recupera el objeto BluetoothDevice
        BluetoothDevice device = BTadaptador.getRemoteDevice(address);
        // Intentar conectar el device
        BTservice.connect(device, secure);
        Log.d(TAG, "CONECTANDO A DEVICE.... " + device);
        Log.d(TAG, "CONECTANDO A DEVICE.... " + nombre);
        Toast.makeText(this, "Conectando a " + nombre, Toast.LENGTH_LONG).show();
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    conectarDevice(data, true);
                    Log.d(TAG, "CONEXION SEGURA, DISPOSITIVO");
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    conectarDevice(data, false);
                    Log.d(TAG, "CONEXION SEGURA, INSEGURA");
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    configurar();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.e(TAG, "ERROR DE CONEXION");
                    Toast.makeText(this, "ERROR DE CONEXION", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }
    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // APLICAR LOS CAMBIOS DE COLOR EN LA INTERFAZ CUANDO DETECTA UN CAMBIO EN ESTADO
                case MESSAGE_STATE_CHANGE:
                    if (D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothManager.STATE_CONNECTED:
                            estado.setText(R.string.bt_CT);
                            estado.setBackgroundColor(0x4300ff00);
                            Log.d(TAG, " BT CONECTADO");
                            enviarMensaje("O");
                            break;
                        case BluetoothManager.STATE_CONNECTING:
                            estado.setText(R.string.bt_CTING);
                            estado.setBackgroundColor(0x430000ff);
                            Log.d(TAG, " BT CONECTANDO");
                            break;
                        case BluetoothManager.STATE_LISTEN:
                            estado.setText(R.string.bt_DC);
                            estado.setBackgroundColor(0x43ff0000);
                            Log.d(TAG, " BT DESCONECTADO");
                            break;
                        case BluetoothManager.STATE_NONE:
                            estado.setText(R.string.bt_DC);
                            estado.setBackgroundColor(0x43ff0000);
                            Log.d(TAG, " BT DESCONECTADO");
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    String writeMessage = new String(writeBuf);
                    break;
                case MESSAGE_READ:
                    String readMessage = (String) msg.obj;
                    //Log.e(TAG, "armado2: " + readMessage + "prueba");

                    if(readMessage.endsWith("\n"))
                    {
                        if(readMessage.startsWith("H"))
                        {
                            String valor = readMessage.substring(1,5);
                            float valors = Float.parseFloat(valor)/100;
                            regla.setText("Posición actual: "+valors+"cm");
                        }
                        else if(readMessage.startsWith("V"))
                        {
                            String valor = readMessage.substring(1,5);
                            float valors = Float.parseFloat(valor)/100;
                            consola.setText("Voltaje Bateria: "+valors+"V");
                        }
                        else if(readMessage.startsWith("P"))
                        {
                            vibrar(100);
                            barra.setProgress(0);
                            velocidad.setText("Velocidad: 0%");
                            consola.setText(readMessage);
                            usuario = false; // PARO DE EMERGENCIA, NO FUE USUARIO
                            power.setChecked(false);
                        }
                        else
                        {
                            consola.setText(readMessage);
                        }
                    }
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    connectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Conectado a " + connectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

}
    /* ///////////////////////////////METODOS BLUETOOTH/////////////////////////////////////////////// */
