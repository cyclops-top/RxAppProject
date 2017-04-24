package justin.rxapp;

import android.app.Activity;
import android.content.Intent;

/**
 * @author justin on 2017/04/19 13:29
 * @version V1.0
 */
public class ActivityResult {
    private final int code;
    private final Intent data;

    public ActivityResult(int code, Intent data) {
        this.code = code;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public Intent getData() {
        return data;
    }

    public boolean isOk() {
        return code == Activity.RESULT_OK;
    }

    public boolean isCancel() {
        return code == Activity.RESULT_CANCELED;
    }

    public boolean isFirstUser() {
        return code == Activity.RESULT_FIRST_USER;
    }

    @Override
    public String toString() {
        return "ActivityResult{" +
                "code=" + code +
                ", data=" + data +
                '}';
    }
}
