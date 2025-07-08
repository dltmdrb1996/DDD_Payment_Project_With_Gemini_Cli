package com.lsg.paymentwithgeminiclitool.application.common

interface UseCase<in Request : Any, out Response : Any> {
    fun invoke(request: Request): Response
}
