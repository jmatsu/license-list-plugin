package io.github.jmatsu.license.example.liststyle

import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.core.view.children
import com.xwray.groupie.databinding.BindableItem
import io.github.jmatsu.license.example.R
import io.github.jmatsu.license.example.databinding.ListitemBodyBinding
import io.github.jmatsu.license.example.databinding.ListitemLicenseBinding
import io.github.jmatsu.license.example.poko.ArtifactDifinitionPoko

class CardItem(
    val definition: ArtifactDifinitionPoko
) : BindableItem<ListitemBodyBinding>() {
    override fun getLayout(): Int = R.layout.listitem_body

    override fun bind(viewBinding: ListitemBodyBinding, position: Int) {
        viewBinding.name.text = definition.displayName

        if (definition.url != null) {
            viewBinding.projectUrl.text = HtmlCompat.fromHtml("""<a href="${definition.url}">${definition.key}</a>""", HtmlCompat.FROM_HTML_MODE_COMPACT)
            viewBinding.projectUrl.movementMethod = LinkMovementMethod.getInstance()
            viewBinding.projectUrl.visibility = View.VISIBLE
        } else {
            viewBinding.projectUrl.visibility = View.GONE
        }

        if (definition.copyrightHolders?.isNotEmpty() == true) {
            viewBinding.copylight.text = "Copyright : ${definition.copyrightHolders.joinToString(", ")}"
            viewBinding.copylight.visibility = View.VISIBLE
        } else {
            viewBinding.copylight.visibility = View.GONE
        }

        val layoutInflater = LayoutInflater.from(viewBinding.container.context)

        val anchorIndex = viewBinding.container.children.withIndex().first { it.value.id == R.id.anchor }.index
        viewBinding.container.removeViews(anchorIndex + 1, viewBinding.container.childCount - anchorIndex - 1)

        definition.licenses.forEach { license ->
            val licenseBinding = ListitemLicenseBinding.inflate(layoutInflater, viewBinding.container, true)

            licenseBinding.license.text = HtmlCompat.fromHtml("""- Under <a href="${license.url}">${license.name}</a>""", HtmlCompat.FROM_HTML_MODE_COMPACT)
            licenseBinding.license.movementMethod = LinkMovementMethod.getInstance()
        }
    }
}
