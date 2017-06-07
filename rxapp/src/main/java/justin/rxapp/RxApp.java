package justin.rxapp;

import android.app.Activity;
import android.app.Fragment;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;

/**
 * @author justin on 2017/04/19 14:32
 * @version V1.0
 */
public class RxApp {

    public static IRxAppSupport with(Activity activity){
        if(activity instanceof AppCompatActivity){
            return with((AppCompatActivity)activity);
        }
        return RxHelperFragment.getRxAppSupport(activity);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static IRxAppSupport with(Fragment fragment){
        return RxHelperFragment.getRxAppSupport(fragment);
    }

    public static IRxAppSupport with(AppCompatActivity activity){
        return RxHelperFragmentV4.getRxAppSupport(activity);
    }

    public static IRxAppSupport with(android.support.v4.app.Fragment fragment){
        return RxHelperFragmentV4.getRxAppSupport(fragment);
    }
}
