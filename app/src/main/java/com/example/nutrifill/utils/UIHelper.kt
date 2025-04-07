import android.content.Context
import android.view.View
import android.widget.Toast

object UIHelper {
    fun showLoading(view: View) {
        view.visibility = View.VISIBLE
    }

    fun hideLoading(view: View) {
        view.visibility = View.GONE
    }

    fun showError(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    fun showSuccess(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}