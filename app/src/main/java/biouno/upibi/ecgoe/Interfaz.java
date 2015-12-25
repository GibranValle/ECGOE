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
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
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
    ///////////////////////////////////* CONSTANTE  *//////////////////////////////////////////////
    static final int vectorECG[] = {
            // BASAL
            120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120,
            120, 120, 120, 120, 120, 120, 120,
            // P
            121, 121, 122, 123, 124, 125, 126, 127, 128, 129, 129, 130, 131, 131, 132, 132, 132, 132, 131, 131, 130, 129,
            128, 127, 126, 125, 124, 123, 122, 122, 121,
            // BASAL
            120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120,
            // QRS
            119, 117, 115, 113, 110, 108, 106, 104, 102, 102, 103, 108, 116, 127,
            139, 153, 167, 181, 196, 209, 220, 230, 236, 240, 240, 238, 234, 229, 222, 214, 205, 196, 185, 175, 164, 153,
            142, 132, 122, 113, 104, 97, 91, 87, 85, 84, 93, 107, 119,
            // BASAL
            120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120,
            120, 120, 120, 120, 120,
            // T
            121, 122, 124, 126, 127, 129, 131, 133, 135, 137, 139, 141, 142, 143, 144, 144, 144, 143, 142, 140, 138, 136,
            133, 131, 128, 126, 124, 122, 121,
            // BASAL
            120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120,
            120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120,
            120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120, 120,
            120, 120, 120, 120, 120};
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    private static final boolean D = true;
    final Handler handler = new Handler();      // VARIABLE PARA TIMER
    final Handler handler2 = new Handler();     // VARIABLE PARA TIMER
    ///////////////////////////////* VARIABLES DE OBJETOS */////////////////////////////////////////
    AnimatorSet set, set1, set2, set3;             // VARIABLE PARA ANIMACION DE CORAZON
    /*//////////////////////// CONSTANTES PARA BLUETOOTH//////////////////////////////////////////*/
    Bitmap bitmap;                              // VARIABLE PARA CANVAS
    Button boton;                               // VARIABLE PARA BOTON
    Canvas canvas;                              // VARIABLE PARA CANVAS
    ImageView espacio, corazon;                 // VARIABLES PARA ANIMACION DE CORAZON
    Intent i;                                   // VARIABLE PARA CAMBIO DE ACTIVITY
    LinearInterpolator lineal;                  // VARIABLE PARA ANIMACION DE CORAZON
    MediaPlayer mp;                             // VARIABLES PARA SONIDO
    ObjectAnimator animador;                    // VARIABLE PARA ANIMACION DE CORAZON
    Paint paint;                                // VARIABLE PARA CANVAS
    TextView estado, paciente, salud;           // VARIABLE PARA TEXTOS CAMBIANTES
    SharedPreferences respaldo;                 // VARIABLE PARA RECUPERAR DATOS
    SoundPool soundPool;                        // VARIABLE PARA SONIDO
    String nombrePaciente, edadPaciente;        // VARIABLE PARA RECUPERAR DATOS
    String lectura;                             // VARIABLE PARA COMUNICACION BLUETOOTH
    String TAG = "Interfaz";                    // VARIABLE PARA DEBUG
    Timer timer, timerAlarma;                   // VARIABLE PARA TIMER
    TimerTask timerTask, alarmaTask;            // VARIABLE PARA TIMER
    ///////////////////////////////////* VARIABLES  *//////////////////////////////////////////////
    int bip, paro;                              // VARIABLE PARA SONIDO
    boolean sonando,cargado;                    // VARIABLE PARA SONIDO
    boolean invertir;
    long inicio, periodo;                       // VARIABLE PARA MEDIR QRS
    int amplitud, max, contador, min, mid;           // VARIABLE PARA MEDIR QRS
    float FC;                                   // VARIABLE PARA MEDIR FRECUENCIA CARDIACA
    float amplificacion;                        // VARIABLE PARA MODIFICAR GANANCIA DIGITAL
    int valor;                                   // VARIABLE PARA MODIFICAR GANANCIA DIGITAL
    int toggle = 0;                             // VARIABLE PARA DEMO DE ECG
    int punto;                                  // VARIABLE PARA GRAFICAR ECG
    int to, tf, vo, vf, VF, VO, offset;         // VARIABLE PARA GRAFICA DE ECG
    int xo,x1,yo,y1;                            // VARIABLE PARA GRAFICA DE ECG
    int contadorMin, contadorMax;               // VARIABLES PARA AJUSTE OFFSET
    int paso, amplitudQRS;                      // VARIABLE GUARDADA PARA GRAFICA Y QRS
    int alto, ancho, origeny,a, inversion;      // VARIABLE PARA GRAFICA DE ECG
    int contadorAlarma;                         // VARIABLE PARA PARAR ALARMA AL SALIR
    // Name of the connected device
    private String connectedDeviceName = null;
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
                            // INICIA EL TIMER PARA DETECCION DE ASISTOLIA, DEBE DETENERSE CADA QUE ENCUENTRA QRS
                            empezarAlarma();
                            Log.i(TAG, "RECIBIENDO DATOS, ESPERANDO ASISTOLIA");
                            break;
                        case BluetoothManager.STATE_CONNECTING:
                            stoptimertask();
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
                    int palabrasEnteras = largo / 4;

                    //Log.e(TAG, "largo : " + largo +" inicio: "+inicio +" fin: "+fin);
                    if (largo >= 4 && largo <= 1000 && inicio >= 0) {
                        if (!readMessage.endsWith("\r") || !readMessage.endsWith("\n")) {
                            palabrasEnteras = palabrasEnteras - 1;
                        }
                        //Log.e(TAG, "palabras enteras : " + palabrasEnteras);
                        for (int j = 1; j <= palabrasEnteras; j++) {

                            lectura = readMessage.substring(inicio + 1, fin);
                            //Log.e(TAG, "palabraSeparada : " + lectura+" punto: "+punto);
                            readMessage = readMessage.replaceFirst(readMessage, readMessage);
                            inicio = inicio + 4;
                            fin = fin + 4;
                            punto = Integer.parseInt(lectura);
                            graficarPunto(punto);
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
    // String buffer for outgoing messages
    private StringBuffer outStringBuffer;
    ///////////////////////////////////* VARIABLES  *//////////////////////////////////////////////
    // Local Bluetooth adapter
    private BluetoothAdapter BTadaptador = null;
    ///////////////////////////////////* CONSTANTE  *//////////////////////////////////////////////
    // Member object for the chat services
    private BluetoothManager BTservice = null;

    /* METODOS SECUENCIADOS */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, " onCreate ");
        setContentView(R.layout.activity_interfaz);

        ///////////// ASIGNACIONES DE OBJETOS A VARIABLES /////////////////
        estado = (TextView) findViewById(R.id.estado);
        paciente = (TextView) findViewById(R.id.IDpaciente);
        corazon = (ImageView) findViewById(R.id.corazon);
        boton = (Button) findViewById(R.id.refresh);
        espacio = (ImageView) findViewById(R.id.Grafica);
        salud = (TextView) findViewById(R.id.saludPaciente);
        ///////////// ASIGNACIONES DE OBJETOS A VARIABLES /////////////////

        ///////////// ASIGNACIONES PARA SONIDO /////////////////
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            public void onLoadComplete(SoundPool sp, int sid, int status) {
                Log.d(getClass().getSimpleName(), "Sound is now loaded");
                cargado = true;
            }
        });
        bip = soundPool.load(this, R.raw.bip, 0);
        paro = soundPool.load(this, R.raw.paro, 0);
        mp = MediaPlayer.create(this, R.raw.paro);
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp.setLooping(true);
        sonando = false;
        cargado = false;
        contadorAlarma = 0;
        ///////////// ASIGNACIONES PARA SONIDO /////////////////

        ///////////// ASIGNACIONES PARA QRS /////////////////
        max = 0;
        min = 240;
        inicio = 0;
        contador = 0;
        ///////////// ASIGNACIONES PARA QRS /////////////////

        ///////////// ASIGNACIONES PARA GRAFICAR /////////////////
        alto = 240;
        ancho = 700;
        xo = 0;
        x1 = ancho;
        yo = 0;
        y1 = alto;
        a = 0;
        to = 0;
        origeny = alto / 2;
        ///////////// ASIGNACIONES PARA GRAFICAR /////////////////

        ///////////// ASIGNACIONES PARA ANIMACION /////////////////
        set = new AnimatorSet();
        set1 = new AnimatorSet();
        set2 = new AnimatorSet();
        set3 = new AnimatorSet();
        ///////////// ASIGNACIONES PARA ANIMACION /////////////////

        ///////////// ASIGNACIONES PARA LISTENER /////////////////
        boton.setOnClickListener(this);
        ///////////// ASIGNACIONES PARA LISTENER /////////////////

        ///////////// ASIGNACIONES PARA AGREGAR CANVAS /////////////////
        bitmap = Bitmap.createBitmap(ancho, alto, Bitmap.Config.ARGB_8888);
        espacio.setImageBitmap(bitmap);
        canvas = new Canvas(bitmap);
        paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        empezarCanvas();
        ///////////// ASIGNACIONES PARA AGREGAR CANVAS /////////////////

        ///////////// ASIGNACIONES PARA RECUPERAR DATOS /////////////////
        cargarDatos();

        ///////////// ASIGNACIONES PARA ANIMAR CORAZON /////////////////
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
        contadorAlarma = 1;
        muteAlarma();
        enviarMensaje("F");
        stoptimertask();
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
        contadorAlarma = 1;
        enviarMensaje("S0");
        pararAlarma();
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
    public void onClick(View v) {
        vibrar(100);
        if (v.getId() == R.id.refresh) // FRECUENCIA PORTADORA
        {
            empezarCanvas();
        }
        max = 0;
        min = 240;
        pararAlarma();
    }

    //////////////////////////////////////* METODOS PARA TIMER *////////////////////////////////////
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
        if (timer != null) {
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
    //////////////////////////////////////* METODOS PARA TIMER *////////////////////////////////////

    public void empezarAlarma() {
        sonando = true;
        Log.e(TAG, "empezando alarma: ");
        //instanciar nuevo timer
        timerAlarma = new Timer();
        //inicializar el timer
        alarmaTimerTask();
        //esperar 0ms para empezar, repetir cada 100ms
        if (contadorAlarma == 1)
        {
            timerAlarma.schedule(alarmaTask, 2000); //
        } else
        {
            timerAlarma.schedule(alarmaTask, 2000, 50000); //
        }
    }

    public void pararAlarma() {
        //parar el timer, si no esta vacio
        Log.e(TAG, "parando alarma: ");
        if (timerAlarma != null) {
            timerAlarma.cancel();
            timerAlarma = null;
        }
        if (sonando) {
            muteAlarma();
            sonando = false;
            corazon.setVisibility(View.VISIBLE);
        }
    }

    public void alarmaTimerTask() {
        alarmaTask = new TimerTask() {
            public void run() {
                //use a handler to run a toast that shows the current timestamp
                handler2.post(new Runnable() {
                    public void run() {
                        //aquí va la acción a realizar
                        salud.setText("BPM: - -" + "\n" + "Asistolia");
                        sonidoAlarma();
                        max = 0;
                        min = 240;
                        corazon.setVisibility(View.INVISIBLE);
                    }
                });
            }
        };
    }
    //////////////////////////////////////* MEDIA PLAYER */////////////////////////////////////////

    //////////////////////////////////////* MEDIA PLAYER *////////////////////////////////////
    public void sonidoAlarma() {
        if (!mp.isPlaying()) {
            mp.start();
        }
    }

    public void muteAlarma()
    {
        if (mp.isPlaying() || mp.isLooping()) {
            mp.stop();
            mp.release();
            mp = MediaPlayer.create(this, R.raw.paro);
            if (contadorAlarma == 1) {
                mp.setLooping(false);
            } else {
                mp.setLooping(true);
            }
            sonando = false;
        }
    }

    public void sonidoBip()
    {
        soundPool.play(bip, 0.5f, 0.5f, 1, 0, 1);
    }

    //////////////////////////////////* METODOS PERSONALIZADOS /////////////////////////////////////
    void graficarPunto(int punto) {
        punto = (int) amplificacion * punto / 100;
        if (to == 0) {
            corazon.setVisibility(View.VISIBLE);
            empezarCanvas();
        }
        if (to >= ancho) {
            empezarCanvas();
            vo = vf;
        }
        // PRIMER PUNTO A GRAFICAR
        tf = to + paso;
        vf = punto;
        calcularQRS();
        vf = alto - punto;
        canvas.drawLine(to, vo, tf, vf, paint);
        //Log.w(TAG, "graficando puntos: "+" to "+to + " vo: "+vo +" tf: "+tf + " vf: "+vf);
        to = to + paso;
        vo = vf;
    }

    void graficar() {
        if (to == 0) {
            corazon.setVisibility(View.VISIBLE);
            empezarCanvas();
        }
        if (to >= ancho) {
            empezarCanvas();
        }
        if (a >= 250) {
            a = 0;
        }

        if (a < 20) {
            ajustarOffSet();
        }

        tf = to + paso;
        vf = alto - vectorECG[a];
        vf = (int) (vf * amplificacion / 100);
        VF = vf + offset;
        VO = VO + offset;
        calcularQRS();
        canvas.drawLine(to, VO, tf, VF, paint);
        to = to + paso;
        vo = vf;
        a = a + 1;
    }

    public void calcularQRS() {
        inversion = alto - vf;
        //Log.w(TAG, "max; "+max +" min: "+min+" amplitud: "+amplitud+ " contador: "+contador + " punto: "+inversion);

        if (inversion <= min) {
            min = inversion;
        }

        if (inversion >= max) {
            max = inversion;
        }

        amplitud = max - min;

        // DETECCION DE QRS
        if (inversion >= max - 1 && amplitud > amplitudQRS) {
            // DETECCION DE QRS, PARAR ALARMA DE ASISTOLIA
            pararAlarma();
            Log.i(TAG, "QRS detectado, parando alarma " + " SONANDO: " + sonando);

            // INICIA EL TIMER PARA DETECCION DE ASISTOLIA, DEBE DETENERSE CADA QUE ENCUENTRA QRS
            empezarAlarma();
            Log.i(TAG, "RECIBIENDO DATOS, ESPERANDO ASISTOLIA");
            periodo = System.currentTimeMillis() - inicio;
            inicio = System.currentTimeMillis();
            if (periodo > 200 && periodo < 2000) {
                sonidoBip();
                max = 0;
                min = 240;
                FC = Math.round(1000 * 60 / periodo);
                //Log.e(TAG,"QRS encontrado periodo: "+periodo +" amplitud: "+amplitud+" FC: "+FC);
                if (FC < 50) {
                    salud.setText("BPM: " + (int) FC + "\n" + "Bradicardia");

                } else if (FC > 50 && FC < 100) {
                    salud.setText("BPM: " + (int) FC + "\n" + "Normal");

                } else if (FC > 100) {
                    salud.setText("BPM: " + (int) FC + "\n" + "Taquicardia");

                }
            }
        }

    }

    public void ajustarOffSet() {
        mid = (max + min) / 2;
        offset = mid - 120;
    }

    void empezarCanvas() {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        paint.setColor(0xff1d721d);
        paint.setStrokeWidth(1);
        canvas.drawLine(xo, origeny, x1, origeny, paint);
        canvas.drawLine(xo, alto / 4, x1, alto / 4, paint);
        canvas.drawLine(xo, 3 * alto / 4, x1, 3 * alto / 4, paint);

        canvas.drawLine(ancho / 10, 0, ancho / 10, alto, paint);
        canvas.drawLine(2 * ancho / 10, 0, 2 * ancho / 10, alto, paint);
        canvas.drawLine(3 * ancho / 10, 0, 3 * ancho / 10, alto, paint);
        canvas.drawLine(4 * ancho / 10, 0, 4 * ancho / 10, alto, paint);
        canvas.drawLine(5 * ancho / 10, 0, 5 * ancho / 10, alto, paint);
        canvas.drawLine(6 * ancho / 10, 0, 6 * ancho / 10, alto, paint);
        canvas.drawLine(7 * ancho / 10, 0, 7 * ancho / 10, alto, paint);
        canvas.drawLine(8 * ancho / 10, 0, 8 * ancho / 10, alto, paint);
        canvas.drawLine(9 * ancho / 10, 0, 9 * ancho / 10, alto, paint);

        paint.setStrokeWidth(3);

        to = 0;
        vo = origeny;
        tf = to + paso;
    }
    /////////////////////////////////** METODOS PERSONALIZADOS///////////////////////////////////////

    void animarCorazon()
    {
        //animacion
        if (animador != null) {
            set.cancel();
            set1.cancel();
            set2.cancel();
            set3.cancel();
            animador.cancel();
        }
        animador = ObjectAnimator.ofFloat(corazon, "alpha", 1, 0.25f, 1);
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

        animador = ObjectAnimator.ofFloat(corazon, "scaleY", 1, 0.8f, 1);
        animador.setRepeatMode(ObjectAnimator.RESTART);
        animador.setDuration(500);
        animador.setRepeatCount(ObjectAnimator.INFINITE);
        animador.setInterpolator(lineal);
        set2.play(animador);

        set3.playTogether(set, set1, set2);
        set3.start();
        Log.i(TAG, "Refresh");
    }

    void vibrar(int ms)
    {
        Vibrator vibrador = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);        // Vibrate for 500 milliseconds
        vibrador.vibrate(ms);
    }
    /* ///////////////////////////////MENU/////////////////////////////////////////////////////// */

    void cargarDatos() {
        // RECUPERAR LOS DATOS GUARDADOS POR EL USUARIO PREVIAMENTE
        respaldo = getSharedPreferences("MisDatos", Context.MODE_PRIVATE);
        // cargar el dato en la variable, o elegir por default la segunda opcion
        nombrePaciente = respaldo.getString("patientName", "Paciente");
        edadPaciente = respaldo.getString("patientAge", "Edad");
        paciente.setText(nombrePaciente + "\n" + edadPaciente + " años");
        paso = Integer.parseInt(respaldo.getString("paso", "2"));
        amplitudQRS = Integer.parseInt(respaldo.getString("amplitud", "60"));
        valor = Integer.parseInt(respaldo.getString("amplificacion", "2"));
        invertir = respaldo.getBoolean("invertir", false);
        amplificacion = Math.round(100 * ((valor * valor * valor * valor * 0.010417f) - (valor * valor * valor * 0.020833f) + (valor * valor * 0.114583f) + (valor * 0.145833f) + (0.25f)));
        Log.e(TAG, "Gain x " + amplificacion / 100);
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

            case R.id.datos: //Modificar Datos Paciente
                startActivity(new Intent(this, DatosPaciente.class));
                break;

            case R.id.config: // Modificar parametros de monitor
                startActivity(new Intent(this, DatosConfig.class));
                break;

            case R.id.demo:
                if (toggle == 0) {
                    toggle = 1;
                    empezarCanvas();
                    startTimer();
                    corazon.setVisibility(View.VISIBLE);
                } else if (toggle == 1) {
                    toggle = 0;
                    empezarCanvas();
                    stoptimertask();
                    corazon.setVisibility(View.INVISIBLE);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

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
}
    /* ///////////////////////////////METODOS BLUETOOTH/////////////////////////////////////////////// */
