package com.sravanthi.quickcart.core.data

import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

/** Failures that mean "couldn't get fresh data", where the app falls back to its cache. */
internal fun Throwable.isRecoverableNetworkError(): Boolean =
    this is IOException || this is HttpException || this is SerializationException
