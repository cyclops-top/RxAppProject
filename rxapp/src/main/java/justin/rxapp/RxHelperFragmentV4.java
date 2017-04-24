package justin.rxapp;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author justin on 2017/04/19 09:54
 * @version V1.0
 */
public class RxHelperFragmentV4 extends Fragment{

    private final static String FRAGMENT_TAG = "justin.common.rx.RxHelperFragmentV4";

    private RxAppHelper mRxAppHelper = new RxAppHelper(this);


    static IRxAppSupport getRxAppSupport(AppCompatActivity activity){
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        return getRxAppSupport(fragmentManager);
    }

    static IRxAppSupport getRxAppSupport(Fragment fragment) {
        FragmentManager fragmentManager = fragment.getChildFragmentManager();
        return getRxAppSupport(fragmentManager);
    }


    static IRxAppSupport getRxAppSupport(FragmentManager fragmentManager) {
        RxHelperFragmentV4 rxHelperFragment = (RxHelperFragmentV4) fragmentManager.findFragmentByTag(FRAGMENT_TAG);
        if (rxHelperFragment == null) {
            rxHelperFragment = new RxHelperFragmentV4();
            fragmentManager
                    .beginTransaction()
                    .add(rxHelperFragment, FRAGMENT_TAG)
                    .commitAllowingStateLoss();
            fragmentManager.executePendingTransactions();
        }
        return rxHelperFragment.mRxAppHelper;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mRxAppHelper.init();
        return view;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mRxAppHelper.onActivityResult(requestCode,resultCode,data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mRxAppHelper.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }

    @Override
    public void onDestroyView() {
        mRxAppHelper.unregisterAllReceiver();
        mRxAppHelper.release();
        super.onDestroyView();
    }
}
