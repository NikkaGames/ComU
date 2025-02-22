package ge.nikka.packages.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

class PageViewModel : ViewModel() {
    private val mIndex = MutableLiveData<Int>()
    val text = Transformations.map(mIndex) { input -> "section$input" }

    fun setIndex(index: Int) {
        mIndex.value = index
    }
}