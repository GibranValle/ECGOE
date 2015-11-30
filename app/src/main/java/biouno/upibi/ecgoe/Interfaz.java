package biouno.upibi.ecgoe;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


public class Interfaz extends Activity implements View.OnClickListener{
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

    ImageView espacio, corazon;
    Bitmap bitmap;
    Canvas canvas;
    Paint paint;
    TextView estado, paciente;
    Intent i;
    String TAG = "Interfaz";
    SharedPreferences respaldo;
    SeekBar barra;
    Button boton;
    ObjectAnimator animador;
    LinearInterpolator lineal;
    AnimatorSet set,set1,set2,set3;
    LinearLayout layout;

    private String nombrePaciente, edadPaciente;

    boolean usuario = true;
    int to, tf;
    float vo, vf;
    int a;
    int xo,x1,yo,y1,paso;
    int toggle = 0;
    int alto, ancho, origeny;

    /* METODOS SECUENCIADOS */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, " onCreate ");
        setContentView(R.layout.activity_interfaz);

        set = new AnimatorSet();
        set1 = new AnimatorSet();
        set2 = new AnimatorSet();
        set3 = new AnimatorSet();

        estado = (TextView) findViewById(R.id.estado);
        paciente = (TextView) findViewById(R.id.IDpaciente);
        corazon = (ImageView) findViewById(R.id.corazon);
        boton = (Button) findViewById(R.id.refresh);
        espacio = (ImageView) findViewById(R.id.Grafica);

        // Acondicionamiento del canvas para empezar a graficar
        alto = 240;
        ancho = 700;
        xo = 0;
        x1 = ancho;
        yo = 0;
        y1=alto;

        origeny = alto/2;
        boton.setOnClickListener(this);

        // Elegir el ImageView como un canvas
        bitmap = Bitmap.createBitmap(ancho, alto, Bitmap.Config.ARGB_8888);
        espacio.setImageBitmap(bitmap);
        canvas = new Canvas(bitmap);
        paint = new Paint(Paint.FILTER_BITMAP_FLAG);

        empezarCanvas();

        // Graficar función seno
        paso = 1;
        to = 0;
        vo = origeny;
        tf = to+paso;

        for (a = 0; a<ancho; a++)
        {
            vf = (float) (origeny + Math.sin(0.08*a)*100); //agregar offset de yo
            canvas.drawLine(to,vo,tf,vf,paint);
            to = to+paso;
            tf = tf+paso;
            vo = vf;
        }

        // carga datos y los despliega en el textview
        cargarDatos();

        //animarCorazon
        animarCorazon();

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
        // carga datos y los despliega en el textview
        cargarDatos();

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

    @Override
    public void onClick(View v)
    {
        Vibrator vibrador = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);        // Vibrate for 500 milliseconds
        vibrador.vibrate(100);

        if(v.getId() == R.id.refresh) // FRECUENCIA PORTADORA
        {
            // Graficar función seno
            paso = 1;
            to = 0;
            vo = origeny;
            tf = to+paso;
            float f=0.075f;
            if (toggle == 0)
            {
                f = 0.1f;
                toggle = 1;
            }
            else if(toggle == 1)
            {
                f = 0.05f;
                toggle = 0;
            }
            empezarCanvas();
            for (a = 0; a<700; a++)
            {
                vf = (float) (origeny + Math.sin(f*a)*100); //agregar offset de yo
                canvas.drawLine(to,vo,tf,vf,paint);
                to = to+paso;
                tf = tf+paso;
                vo = vf;
            }
        }
    }

    //* METODOS PERSONALIZADOS
    void empezarCanvas()
    {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        paint.setColor(0xff1d721d);
        paint.setStrokeWidth(1);
        canvas.drawLine(xo, origeny, x1, origeny, paint);
        canvas.drawLine(xo, alto/4, x1, alto/4, paint);
        canvas.drawLine(xo, 3*alto/4, x1, 3*alto/4, paint);

        canvas.drawLine(ancho/10, 0, ancho/10, alto, paint);
        canvas.drawLine(2*ancho/10, 0, 2*ancho/10, alto, paint);
        canvas.drawLine(3*ancho/10, 0, 3*ancho/10, alto, paint);
        canvas.drawLine(4*ancho/10, 0, 4*ancho/10, alto, paint);
        canvas.drawLine(5*ancho/10, 0, 5*ancho/10, alto, paint);
        canvas.drawLine(6*ancho/10, 0, 6*ancho/10, alto, paint);
        canvas.drawLine(7*ancho/10, 0, 7*ancho/10, alto, paint);
        canvas.drawLine(8*ancho/10, 0, 8*ancho/10, alto, paint);
        canvas.drawLine(9*ancho/10, 0, 9*ancho/10, alto, paint);


        paint.setStrokeWidth(3);
    }

    void animarCorazon()
    {
        //animacion
        if (animador != null)
        {
            set.cancel();
            set1.cancel();
            set2.cancel();
            set3.cancel();
            animador.cancel();
        }
        animador = ObjectAnimator.ofFloat(corazon, "alpha", 1, 0.25f,1);
        animador.setRepeatMode(ObjectAnimator.RESTART);
        animador.setDuration(750);
        animador.setRepeatCount(ObjectAnimator.INFINITE);
        animador.setInterpolator(lineal);
        set.play(animador);

        animador = ObjectAnimator.ofFloat(corazon, "scaleX", 1, 0.8f, 1);
        animador.setRepeatMode(ObjectAnimator.RESTART);
        animador.setDuration(750);
        animador.setRepeatCount(ObjectAnimator.INFINITE);
        animador.setInterpolator(lineal);
        set1.play(animador);

        animador = ObjectAnimator.ofFloat(corazon,"scaleY",1, 0.8f, 1);
        animador.setRepeatMode(ObjectAnimator.RESTART);
        animador.setDuration(750);
        animador.setRepeatCount(ObjectAnimator.INFINITE);
        animador.setInterpolator(lineal);
        set2.play(animador);

        set3.playTogether(set,set1,set2);
        set3.start();
        Log.i(TAG, "Refresh");
    }

    void vibrar(int ms)
    {
        Vibrator vibrador = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);        // Vibrate for 500 milliseconds
        vibrador.vibrate(ms);
    }

    void cargarDatos()
    {
        // Cargar datos guardados
        final SharedPreferences respaldo = getSharedPreferences("MisDatos", Context.MODE_PRIVATE);
        // cargar el dato en la variable, o elegir por default la segunda opcion
        nombrePaciente = respaldo.getString("patientName","Paciente");
        Log.d(TAG, "Nombre cargado: "+ nombrePaciente);
        edadPaciente = respaldo.getString("patientAge", "Edad");
        Log.d(TAG, "Edad cargado: "+edadPaciente);
        paciente.setText(nombrePaciente +"\n"+edadPaciente+" años");
    }
    //** METODOS PERSONALIZADOS


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

            case R.id.datos: //Modificar Datos Paciente
                startActivity(new Intent(this,DatosPaciente.class));
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
                        }
                        else if(readMessage.startsWith("V"))
                        {
                            String valor = readMessage.substring(1,5);
                            float valors = Float.parseFloat(valor)/100;
                        }
                        else if(readMessage.startsWith("P"))
                        {
                            vibrar(100);
                            barra.setProgress(0);
                            usuario = false; // PARO DE EMERGENCIA, NO FUE USUARIO
                        }
                        else
                        {
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
