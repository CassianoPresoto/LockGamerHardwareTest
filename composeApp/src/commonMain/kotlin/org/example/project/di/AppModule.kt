package org.example.project.di

import org.example.project.database.DatabaseDriverFactory
import org.example.project.database.HardwareDatabase
import org.example.project.database.Hardware_config
import org.example.project.repository.ConfigRepository
import org.example.project.repository.SqlDelightConfigRepository
import org.example.project.viewmodel.HardwareViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import app.cash.sqldelight.ColumnAdapter

val appModule = module {
    // Database
    single { get<DatabaseDriverFactory>().createDriver() }

    // Adapters necessários (INTEGER AS Boolean)
    single<ColumnAdapter<Boolean, Long>> {
        object : ColumnAdapter<Boolean, Long> {
            override fun decode(databaseValue: Long): Boolean = databaseValue != 0L
            override fun encode(value: Boolean): Long = if (value) 1L else 0L
        }
    }

    single {
        HardwareDatabase(
            driver = get(),
            hardware_configAdapter = Hardware_config.Adapter(
                rtx_enabledAdapter = get(),
                framegen_enabledAdapter = get()
            )
        )
    }
    
    // Repository
    singleOf(::SqlDelightConfigRepository) bind ConfigRepository::class
    
    // ViewModel
    single { HardwareViewModel(get()) }
}

// Platform-specific module (deve ser definido em cada plataforma)
expect val platformModule: org.koin.core.module.Module
