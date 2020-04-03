package io.github.jmatsu.license.example

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import io.github.jmatsu.license.example.liststyle.CardItem
import io.github.jmatsu.license.example.liststyle.SpaceItemDecoration
import io.github.jmatsu.license.example.poko.ArtifactDifinitionPoko
import io.github.jmatsu.license.example.poko.LicenseKeyPoko

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.addItemDecoration(SpaceItemDecoration())

        val adapter = GroupAdapter<GroupieViewHolder>()
        recyclerView.adapter = adapter

        val text = assets.open("license-list.json").bufferedReader().readText()
        val moshi = Moshi.Builder().add(LicenseKeyPoko.Adapter).build()

        val definitionListType = Types.newParameterizedType(List::class.java, ArtifactDifinitionPoko::class.java)
        val definitions = requireNotNull(moshi.adapter<List<ArtifactDifinitionPoko>>(definitionListType).fromJson(text))

        definitions.map { def ->
            CardItem(
                definition = def
            )
        }.also { items ->
            adapter.update(items)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.share, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.main)?.isEnabled = false
        menu?.findItem(R.id.html)?.setOnMenuItemClickListener {
            startActivity(Intent(this, HtmlLicenseActivity::class.java))
            finish()
            true
        }
        return super.onPrepareOptionsMenu(menu)
    }
}
