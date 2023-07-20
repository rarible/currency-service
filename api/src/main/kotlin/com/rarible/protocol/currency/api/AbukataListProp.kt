package com.rarible.protocol.currency.api

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "abukata")
@ConstructorBinding
data class AbukataListProp(
    val abc: String?,
    val nodes: List<Node>?,
)

data class Node(
    val http: String,
    val websocket: String,
)

