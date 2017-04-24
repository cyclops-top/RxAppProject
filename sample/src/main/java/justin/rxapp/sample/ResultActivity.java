package justin.rxapp.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;

import justin.rxapp.R;

public class ResultActivity extends AppCompatActivity {

    private EditText result;
    private Button confirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        initView();
    }

    private void initView() {
        result = (EditText) findViewById(R.id.result);
        confirm = (Button) findViewById(R.id.confirm);
        confirm.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.putExtra("TEXT",result.getText().toString());
            setResult(RESULT_OK,intent);
            finish();
        });
    }
}
