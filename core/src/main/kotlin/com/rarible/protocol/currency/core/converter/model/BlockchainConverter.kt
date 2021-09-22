package com.rarible.protocol.currency.core.converter.model

import com.rarible.protocol.currency.core.model.Blockchain
import com.rarible.protocol.currency.dto.BlockchainDto
import org.springframework.core.convert.converter.Converter

object BlockchainConverter : Converter<BlockchainDto, Blockchain> {
    override fun convert(source: BlockchainDto): Blockchain {
        return when (source) {
            BlockchainDto.ETHEREUM -> Blockchain.ETHEREUM
            BlockchainDto.POLYGON -> Blockchain.POLYGON
            BlockchainDto.FLOW -> Blockchain.FLOW
        }
    }
}
