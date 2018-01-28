package org.kotlinq.api

import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
@Component(modules = [
  AdapterService.Companion::class,
  AdapterUtilities::class,
  PropertyProvider::class])
interface Kotlinq {
   fun utilities(util: AdapterUtilities)
}

