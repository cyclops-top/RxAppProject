package justin.rxapp


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * @author justin on 2017/04/19 09:54
 * *
 * @version V1.0
 */
class RxHelperFragmentV4 : Fragment() {

    val mRxAppHelper = RxAppHelper(this)

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        mRxAppHelper.init()
        return view
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mRxAppHelper.onActivityResult(requestCode, resultCode, data!!)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mRxAppHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onDestroyView() {
        mRxAppHelper.unregisterAllReceiver()
        mRxAppHelper.release()
        super.onDestroyView()
    }

}
