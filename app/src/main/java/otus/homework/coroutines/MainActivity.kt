package otus.homework.coroutines

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val diContainer = DiContainer()

    private val catsViewModel: CatsViewModel by viewModels {
        CatsViewModel.Factory(
            diContainer.catFactService,
            diContainer.catImageService,
            diContainer.crashMonitor
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = layoutInflater.inflate(R.layout.activity_main, null) as CatsView
        setContentView(view)

        findViewById<Button>(R.id.button).setOnClickListener {
            catsViewModel.loadCatFact()
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                catsViewModel.viewState.collectLatest { result ->
                    when (result) {
                        is Result.Loading -> {
                            // show loading
                        }
                        is Result.Success -> view.populate(result.catFact, diContainer.imageLoader)
                        is Result.SocketTimeoutException -> view.showSocketTimeoutToast()
                        is Result.Error -> view.showErrorToast(message = result.exception.message)
                    }
                }
            }
        }
    }
}