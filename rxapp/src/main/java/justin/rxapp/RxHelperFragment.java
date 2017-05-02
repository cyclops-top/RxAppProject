package justin.rxapp;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * @author justin on 2017/04/19 09:54
 * @version V1.0
 */
public class RxHelperFragment extends Fragment {

    private final static String FRAGMENT_TAG = "justin.common.rx.RxHelperFragment";
    private RxAppHelper mRxAppHelper = new RxAppHelper(this);

    static IRxAppSupport getRxAppSupport(Activity activity) {
        FragmentManager fragmentManager = activity.getFragmentManager();
        return getRxAppSupport(fragmentManager);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    static IRxAppSupport getRxAppSupport(Fragment fragment) {
        FragmentManager fragmentManager = fragment.getChildFragmentManager();
        return getRxAppSupport(fragmentManager);
    }

    static IRxAppSupport getRxAppSupport(FragmentManager fragmentManager) {
        RxHelperFragment rxHelperFragment = (RxHelperFragment) fragmentManager.findFragmentByTag(FRAGMENT_TAG);
        if (rxHelperFragment == null) {
            rxHelperFragment = new RxHelperFragment();
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
        mRxAppHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mRxAppHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onDestroyView() {
        mRxAppHelper.unregisterAllReceiver();
        mRxAppHelper.release();
        super.onDestroyView();
    }
}
