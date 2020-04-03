package io.github.jmatsu.license.dsl.validation

import java.io.File
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun optionalFileNameProperty(defaultValue: String? = null): ReadWriteProperty<Any, String?> {
    return object : ReadWriteProperty<Any, String?> {
        var value: String? = defaultValue

        override fun getValue(thisRef: Any, property: KProperty<*>): String? = value

        override fun setValue(thisRef: Any, property: KProperty<*>, value: String?) {
            this.value = requireFileName(value)
        }
    }
}

fun fileNameProperty(defaultValue: String): ReadWriteProperty<Any, String> {
    return object : ReadWriteProperty<Any, String> {
        var value: String = defaultValue

        override fun getValue(thisRef: Any, property: KProperty<*>): String = value

        override fun setValue(thisRef: Any, property: KProperty<*>, value: String) {
            this.value = requireFileName(value)!!
        }
    }
}

private fun requireFileName(value: String?): String? {
    if (File.pathSeparator in value ?: return null) {
        error("$value is not a valid filename. This may contain one or more path separators.")
    }

    return value
}
