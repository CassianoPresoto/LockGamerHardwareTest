package org.example.project.database

import app.cash.sqldelight.db.SqlDriver

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        throw NotImplementedError("SQLDelight driver not configured for JS target. Consider adding sqljs-driver or web-worker driver.")
    }
}
