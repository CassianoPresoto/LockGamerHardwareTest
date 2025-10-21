package org.example.project.di

import org.example.project.database.DatabaseDriverFactory
import org.koin.dsl.module

actual val platformModule = module {
    // WASM não possui persistência local por padrão; usamos um stub de driver
    single { DatabaseDriverFactory() }
}
