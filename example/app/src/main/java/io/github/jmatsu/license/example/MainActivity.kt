package io.github.jmatsu.license.example

import android.os.Bundle
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

        val text = assets.open("license.json").bufferedReader().readText()
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
}
