package io.github.jmatsu.license.dsl.validation

import java.io.File
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun optionalDirectoryFileProperty(defaultValue: File? = null, requireExist: Boolean = false): ReadWriteProperty<Any, File?> {
    return object : ReadWriteProperty<Any, File?> {
        var value: File? = defaultValue

        override fun getValue(thisRef: Any, property: KProperty<*>): File? = value

        override fun setValue(thisRef: Any, property: KProperty<*>, value: File?) {
            this.value = requireDirectoryFile(value, requireExist)
        }
    }
}

fun directoryFileProperty(defaultValue: File, requireExist: Boolean = false): ReadWriteProperty<Any, File> {
    return object : ReadWriteProperty<Any, File> {
        var value: File = defaultValue

        override fun getValue(thisRef: Any, property: KProperty<*>): File = value

        override fun setValue(thisRef: Any, property: KProperty<*>, value: File) {
            this.value = requireDirectoryFile(value, requireExist)!!
        }
    }
}

private fun requireDirectoryFile(value: File?, requireExist: Boolean): File? {
    if (value == null) {
        return null
    }

    if (value.exists()) {
        if (!value.isDirectory) {
            error("${value.absolutePath} is not a directory")
        }
    } else if (requireExist) {
        error("${value.absolutePath} does not exist")
    }

    return value
}
