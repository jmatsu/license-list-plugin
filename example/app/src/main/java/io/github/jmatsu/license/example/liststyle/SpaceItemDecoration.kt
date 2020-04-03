package io.github.jmatsu.license.example.liststyle

import android.content.res.Resources
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SpaceItemDecoration : RecyclerView.ItemDecoration() {
    val space: Int = (Resources.getSystem().displayMetrics.density * 4).toInt()

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.set(space, space, space, space)
    }
}
