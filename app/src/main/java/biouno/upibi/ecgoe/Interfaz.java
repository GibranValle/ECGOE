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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;


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
    Timer timer;
    TimerTask timerTask;

    final Handler handler = new Handler();
    String nombrePaciente, edadPaciente;
    String lectura;
    int punto;
    boolean usuario = true;
    int toggle = 0;
    int to, tf, vo, vf;
    int xo,x1,yo,y1,paso;
    int alto, ancho, origeny,a;
    final int vectorECG[]={120,120,120,120,120,120,120,120,120,120,120,120,120,120,120,120,120,120,120,120,120,
            120,120,121,122,124,126,127,129,131,133,135,137,139,141,142,143,144,144,144,143,142,
            140,138,136,133,131,128,126,124,122,121,120,120,120,120,120,120,120,120,120,120,120,120,
            120,120,120,120,120,120,120,120,119,117,115,113,110,108,106,104,102,102,103,108,116,127,
            139,153,167,181,196,209,220,230,236,240,240,238,234,229,222,214,205,196,185,175,164,153,
            142,132,122,113,104,97,91,87,85,84,93,107,119,120,120,120,120,120,120,120,120,120,
            120,120,120,120,120,120,120,120,120,120,120,120,120,120,120,120,120,120,121,121,122,123,
            124,125,126,127,128,129,129,130,131,131,132,132,132,132,131,131,130,129,128,127,126,125,
            124,123,122,122,121,120,120,120,120,120,120,120,120,120,120,120,120,120,120,120,120,120,
            120,120,120,120,120,120,120,120,120,120,120,120,120,120,120,120,120,120,120,120,120,120,
            120,120,120,120,120,120,120,120,120,120,120,120,120,120,120,120,120,120,120,120,120,120,
            120,120,120,120,120,120,120,120,120,120};

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
        a = 0;
        to = 0;

        origeny = alto/2;
        boton.setOnClickListener(this);

        // Elegir el ImageView como un canvas
        bitmap = Bitmap.createBitmap(ancho, alto, Bitmap.Config.ARGB_8888);
        espacio.setImageBitmap(bitmap);
        canvas = new Canvas(bitmap);
        paint = new Paint(Paint.FILTER_BITMAP_FLAG);

        empezarCanvas();

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
        empezarCanvas();
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
        vibrar(100);
        if(v.getId() == R.id.refresh) // FRECUENCIA PORTADORA
        {
            empezarCanvas();
        }
    }

    public void startTimer() {
        //instanciar nuevo timer
        timer = new Timer();
        //inicializar el timer
        initializeTimerTask();
        //esperar 0ms para empezar, repetir cada 100ms
        timer.schedule(timerTask, 0, 4); //
    }

    public void stoptimertask() {
        //parar el timer, si no esta vacio
        if (timer != null)
        {
            timer.cancel();
            timer = null;
        }
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    public void run() {
                        //aquí va la acción a realizar
                        graficar();
                    }
                });
            }
        };
    }

    //* METODOS PERSONALIZADOS
    void graficarPunto(int punto)
    {
        if (to == 0)
        {
            corazon.setVisibility(View.VISIBLE);
            empezarCanvas();
        }
        if (to == ancho)
        {
            empezarCanvas();
            vo = vf;
        }
        // PRIMER PUNTO A GRAFICAR
        tf = to + paso;
        vf = alto-punto;
        canvas.drawLine(to,vo,tf,vf,paint);
        Log.w(TAG, "graficando puntos: "+" to "+to + " vo: "+vo +" tf: "+tf + " vf: "+vf);
        to = to + paso;
        vo = vf;

    }

    void graficar()
    {
        if (to == ancho)
        {
            empezarCanvas();
        }
        if(a==250)
        {
            a=0;
        }
        tf = to + paso;
        vf = 240-vectorECG[a];
        canvas.drawLine(to,vo,tf,vf,paint);
        to = to + paso;
        vo = vf;
        a = a+1;
    }

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

        to = 0;
        vo = origeny;
        tf = to+paso;
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
        animador.setDuration(500);
        animador.setRepeatCount(ObjectAnimator.INFINITE);
        animador.setInterpolator(lineal);
        set.play(animador);

        animador = ObjectAnimator.ofFloat(corazon, "scaleX", 1, 0.8f, 1);
        animador.setRepeatMode(ObjectAnimator.RESTART);
        animador.setDuration(500);
        animador.setRepeatCount(ObjectAnimator.INFINITE);
        animador.setInterpolator(lineal);
        set1.play(animador);

        animador = ObjectAnimator.ofFloat(corazon,"scaleY",1, 0.8f, 1);
        animador.setRepeatMode(ObjectAnimator.RESTART);
        animador.setDuration(500);
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
        paso = Integer.parseInt(respaldo.getString("paso", "1"));
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
            case R.id.demo:
                if(toggle == 0)
                {
                    toggle = 1;
                    empezarCanvas();
                    startTimer();
                    corazon.setVisibility(View.VISIBLE);
                }
                else if (toggle == 1)
                {
                    toggle = 0;
                    empezarCanvas();
                    stoptimertask();
                    corazon.setVisibility(View.INVISIBLE);
                }
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
                            enviarMensaje("F");
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    String writeMessage = new String(writeBuf);
                    break;
                case MESSAGE_READ:
                    String readMessage = (String) msg.obj;
                    //Log.e(TAG, "mensaje llegando: " + readMessage);
                    int largo = readMessage.length();
                    int inicio = readMessage.indexOf("V");
                    int fin = inicio + 4;
                    int palabrasEnteras = largo/4;

                    //Log.e(TAG, "largo : " + largo +" inicio: "+inicio +" fin: "+fin);
                    if (largo >= 4 && largo <= 1000 && inicio >= 0)
                    {
                        if(!readMessage.endsWith("\r") || !readMessage.endsWith("\n"))
                        {
                            palabrasEnteras = palabrasEnteras - 1;
                        }
                        //Log.e(TAG, "palabras enteras : " + palabrasEnteras);
                        for (int j=1;j<=palabrasEnteras;j++)
                        {

                            lectura = readMessage.substring(inicio+1,fin);
                            Log.e(TAG, "palabraSeparada : " + lectura+" punto: "+punto);
                            readMessage = readMessage.replaceFirst(readMessage,readMessage);
                            inicio =inicio +4;
                            fin = fin + 4;
                            punto = Integer.parseInt(lectura);
                            graficarPunto(punto);
                        }
                    }
                    /*
                    //Log.e(TAG, "largo : " + largo +" inicio: "+inicio +" fin: "+fin);
                    if (largo >= 4 && largo <= 200 && inicio > 0 && fin > 0 && fin > inicio)
                    {
                        int palabrasEnteras = largo/4;
                        for (int j=0; j<= palabrasEnteras; j++)
                        {
                            Log.e(TAG, "largo : " + largo +" inicio: "+inicio +" fin: "+fin);
                            lectura.substring(inicio,fin);
                        }
                        lectura = readMessage;
                    }
                    */

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
