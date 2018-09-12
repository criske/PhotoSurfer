package com.crskdev.photosurfer.entities

import com.crskdev.photosurfer.data.remote.PagingData

/**
 * Created by Cristian Pela on 26.08.2018.
 */
abstract class BaseEntity {
    abstract val pagingData: PagingData?
}

class PairBE<L : BaseEntity, R>(val left: L, val right: R) : BaseEntity() {
    override val pagingData: PagingData? = left.pagingData
}

infix fun <A : BaseEntity, B> A.toBE(that: B): PairBE<A, B> = PairBE(this, that)