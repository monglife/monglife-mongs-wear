package com.monglife.mongs.domain.member.collection.model

class CollectionMong(
    code: String,
    name: String,
    isIncluded: Boolean,
) {
    var code: String = code
        private set
    var name: String = name
        private set
    var isIncluded: Boolean = isIncluded
        private set
}