package justin.rxapp.sample;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import justin.rxapp.R;
import justin.rxapp.RxApp;

public class MainActivity extends AppCompatActivity {

    private Button openSettings;
    private TextView showResult;
    private Button getCameraPermission;
    private Button getCameraAndReadSmsPermission;
    private Button startActivityForResult;
    private TextView send;
    private TextView received;
    private Button startBroadcast;
    private Button registerBroadcast;
    private Button unregisterBroadcast;
    private Disposable broadcastDisposable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private Intent getAppDetailSettingIntent() {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
        localIntent.setData(Uri.fromParts("package", getPackageName(), null));
        return localIntent;
    }

    private void initView() {
        openSettings = (Button) findViewById(R.id.openSettings);
        openSettings.setOnClickListener(v -> {
            Intent intent = getAppDetailSettingIntent();
            startActivity(intent);
        });

        showResult = (TextView) findViewById(R.id.showResult);
        getCameraPermission = (Button) findViewById(R.id.getCameraPermission);
        getCameraAndReadSmsPermission = (Button) findViewById(R.id.getCameraAndReadSmsPermission);
        getCameraPermission.setOnClickListener(v ->
                RxApp.with(MainActivity.this).ensure(Manifest.permission.CAMERA)
                        .subscribe(aBoolean ->
                                showResult.setText(
                                        String.format(Locale.CHINA, "获取Camera权限为：%s", String.valueOf(aBoolean))))
        );

        getCameraAndReadSmsPermission.setOnClickListener(v ->
                RxApp.with(MainActivity.this).ensure(Manifest.permission.CAMERA, Manifest.permission.READ_SMS)
                        .subscribe(aBoolean ->
                                showResult.setText(
                                        String.format(Locale.CHINA, "获取Camera,sms权限为：%s", String.valueOf(aBoolean)))));
        startActivityForResult = (Button) findViewById(R.id.startActivityForResult);
        startActivityForResult.setOnClickListener(v -> {
                    Intent intent = new Intent(this, ResultActivity.class);
                    RxApp.with(this).startActivityForObservable(intent, null)
                            .subscribe(activityResult -> {
                                if (activityResult.isOk()) {
                                    showResult.setText("返回结果：" + activityResult.getData().getStringExtra("TEXT"));
                                } else {
                                    showResult.setText("用户取消");
                                }
                            });

                }
        );
        send = (TextView) findViewById(R.id.send);
        received = (TextView) findViewById(R.id.received);
        startBroadcast = (Button) findViewById(R.id.startBroadcast);
        registerBroadcast = (Button) findViewById(R.id.registerBroadcast);
        unregisterBroadcast = (Button) findViewById(R.id.unregisterBroadcast);


        startBroadcast.setOnClickListener(v ->{
            Observable.interval(1, TimeUnit.SECONDS)
                    .compose(RxApp.with(this).bindLife())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(time->{
                        Intent intent = new Intent("RxApp.Test");
                        intent.putExtra("Time",(long)time);
                        sendBroadcast(intent);
                        send.setText("发送："+time);
                    });
            startBroadcast.setEnabled(false);
        });
        registerBroadcast.setOnClickListener(v -> {
            broadcastDisposable = RxApp.with(this).broadcast("RxApp.Test")
                    .subscribe(intent ->
                            received.setText("收到："+ intent.getLongExtra("Time",0)));
        });
        unregisterBroadcast.setOnClickListener(v -> {
            if(broadcastDisposable!= null){
                broadcastDisposable.dispose();
            }
        });

    }


}
