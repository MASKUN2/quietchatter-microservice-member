package com.quietchatter.member.application

import org.springframework.stereotype.Component
import java.util.concurrent.ThreadLocalRandom

interface RandomNickNameSupplier {
    fun get(): String
}

@Component
class RandomNickNameSupplierImpl : RandomNickNameSupplier {
    private val prefixes = listOf(
        "현명한", "행복한", "밝은", "활기찬",
        "친절한", "사랑스러운", "사려깊은", "따뜻한"
    )

    private val names = listOf(
        "독서가", "여행자", "탐험가", "다독가",
        "책바라기", "문장수집가", "글벗", "글지기", "책수집가"
    )

    override fun get(): String {
        val prefix = prefixes[ThreadLocalRandom.current().nextInt(prefixes.size)]
        val name = names[ThreadLocalRandom.current().nextInt(names.size)]
        return "$prefix $name"
    }
}
