package io.github.jmatsu.license.dsl.validation

import java.io.File
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun optionalDirectoryProperty(defaultValue: File? = null): ReadWriteProperty<Any, File?> {
    return object : ReadWriteProperty<Any, File?> {
        var value: File? = defaultValue

        override fun getValue(thisRef: Any, property: KProperty<*>): File? = value

        override fun setValue(thisRef: Any, property: KProperty<*>, value: File?) {
            this.value = requireDirectoryFile(value)
        }
    }
}

fun directoryFileProperty(defaultValue: File): ReadWriteProperty<Any, File> {
    return object : ReadWriteProperty<Any, File> {
        var value: File = defaultValue

        override fun getValue(thisRef: Any, property: KProperty<*>): File = value

        override fun setValue(thisRef: Any, property: KProperty<*>, value: File) {
            this.value = requireDirectoryFile(value)!!
        }
    }
}

private fun requireDirectoryFile(value: File?): File? {
    if (value == null) {
        return null
    }

    if (value.exists()) {
        if (!value.isDirectory) {
            error("${value.absolutePath} is not a directory")
        }
    } else {
        value.mkdirs()
    }

    return value
}
