package com.vesta.android;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.provider.Settings.Secure;

import com.google.zxing.Result;
import com.vesta.android.model.KeyPairManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import kotlin.text.Charsets;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

//import com.myhexaville.androidwebrtc.databinding.ActivitySampleDataChannelBinding;

public class QRScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView mScannerView;
    private final int MY_CAMERA_REQUEST_CODE = 1888;
    private JSONObject result;
    private static final String TAG = "ScannerActivity";
    private static PeerConnectionFactory peerConnectionFactory;
    private static PeerConnection localPeerConnection;
    private static PeerConnection remotePeerConnection;
    private static DataChannel localDataChannel;
    private static  PeerConnection.Observer pcObserver;
    //private ActivitySampleDataChannelBinding binding;



    /*
     * The name of our shared preferences
     * Used to instantiate the shared preferences object with this name
     * */
    private static final String SHARED_PREFERENCES = "SharedPreferences";

    private void setScannerProperties(ZXingScannerView qrCodeScanner) {
        qrCodeScanner.setAutoFocus(true);
        qrCodeScanner.setLaserColor(R.color.colorAccent);
        qrCodeScanner.setMaskColor(R.color.colorAccent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view

    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                String [] perms = {Manifest.permission.CAMERA};
                ActivityCompat.requestPermissions(this, perms,
                        MY_CAMERA_REQUEST_CODE);
                return;
            }
        }

        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    /**
     * The rawResult variable contains the data which was present in the QR code
     * Used to get the contents from the QR code
     * @param rawResult, Result object retrieved from the QR code
     */
    @Override
    public void handleResult(Result rawResult) {
        //Storing the rawResult which is the public key in shared preferences with the
        // unique identifier of the device
        /*String androidID = Secure.getString(this.getBaseContext().getContentResolver(),
                Secure.ANDROID_ID);
        Log.i("Retrieve shared pref",
                KeyPairManager.retrievePublicKeySharedPrefsFile(SHARED_PREFERENCES, this.getBaseContext()));*/
        System.out.println("#&$*&#^%*&#^%*&#^%");
        System.out.println(rawResult.getText());
        //if (rawResult.getText()) //parse the string and check if its from the desktop

        try {
            //this is the symetric key that is retrieved from the desktop
            result = new JSONObject(rawResult.getText());
            System.out.println(result.get("fromDesktop") instanceof Boolean);
            Log.i("fromDesktop", result.get("fromDesktop").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }


        try {
            //socket connection initiated when QR code is from the desktop
            if ((Boolean) result.get("fromDesktop")) {


                //returns public key object from the shared pref
                String pubKeySharedPref = KeyPairManager
                        .retrievePublicKeySharedPrefsFile(SHARED_PREFERENCES,this.getBaseContext());

                //encrypt public key in shared pref with the sym key in the result
                String symKey = result.getString("key");

                try {
                    KeyPairManager.encrypt(symKey, pubKeySharedPref);
                    Log.i("EncPubKeyWithSymKey", KeyPairManager.encrypt(symKey, pubKeySharedPref));
                } catch (KeyStoreException e) {
                    e.printStackTrace();
                } catch (CertificateException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                } catch (UnrecoverableEntryException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (NoSuchProviderException e) {
                    e.printStackTrace();
                }


                Log.i("SocketConnection", "Establishing Socket Connection");
                new SocketConnection().sendMessage("KUNAL IS SENDING A MESSAGE");
            }
            //otherwise, store the public key in the shared preferences for future reference
            //will retrieve it from shared pref when it has to be encrypted it QR code on Desktop
            else {
                //Only call store shared pref if key does not exist already
                if (!KeyPairManager.retrievePublicKeySharedPrefsFile(SHARED_PREFERENCES,this.getBaseContext()).equals(KeyPairManager.DEFAULT_VALUE_KEY_DOES_NOT_EXIST)) {
                    KeyPairManager.storePublicKeySharedPref(SHARED_PREFERENCES, this.getBaseContext(),
                            result.getString("key"));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        this.finish();
    }
}